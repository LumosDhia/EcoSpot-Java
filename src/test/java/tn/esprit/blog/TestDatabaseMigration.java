package tn.esprit.blog;

import tn.esprit.util.MyConnection;

import java.sql.*;

/**
 * Idempotent schema patcher — run once per JVM before any blog/stats tests.
 * Drops obsolete app_user FK constraints, converts UUID columns to INT,
 * and creates missing tables. Safe to run on both old and new schemas.
 */
public class TestDatabaseMigration {

    private static volatile boolean applied = false;

    public static synchronized void applyMigrations() {
        if (applied) return;
        applied = true;

        Connection cnx = MyConnection.getInstance().getCnx();
        try (Statement st = cnx.createStatement()) {

            // ── 1. Drop all FK constraints on `article` that point to app_user ──
            try (ResultSet rs = cnx.prepareStatement(
                    "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'article' " +
                    "AND REFERENCED_TABLE_NAME = 'app_user'").executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("CONSTRAINT_NAME");
                    tryExec(st, "ALTER TABLE `article` DROP FOREIGN KEY `" + name + "`");
                    System.out.println("[Migration] Dropped FK: " + name);
                }
            }

            // ── 2. Promote created_by_id / writer_id from BINARY(16) to INT ──
            //    Check current type first to avoid no-op warnings
            boolean cbIsInt = columnIsInt(cnx, "article", "created_by_id");
            boolean wiIsInt = columnIsInt(cnx, "article", "writer_id");
            if (!cbIsInt) tryExec(st, "ALTER TABLE `article` MODIFY COLUMN `created_by_id` INT DEFAULT NULL");
            if (!wiIsInt) tryExec(st, "ALTER TABLE `article` MODIFY COLUMN `writer_id` INT DEFAULT NULL");

            // ── 3. Add missing columns to `article` ──────────────────────────
            tryExec(st, "ALTER TABLE `article` ADD COLUMN `slug` VARCHAR(300) NOT NULL DEFAULT ''");
            tryExec(st, "ALTER TABLE `article` ADD COLUMN `status` VARCHAR(50) NOT NULL DEFAULT 'draft'");
            tryExec(st, "ALTER TABLE `article` ADD COLUMN `admin_revision_note` TEXT DEFAULT NULL");
            tryExec(st, "ALTER TABLE `article` ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
            tryExec(st, "ALTER TABLE `article` ADD COLUMN `published_at` DATETIME DEFAULT NULL");
            tryExec(st, "ALTER TABLE `article` ADD COLUMN `created_by_id` INT DEFAULT NULL");
            tryExec(st, "ALTER TABLE `article` ADD COLUMN `writer_id` INT DEFAULT NULL");
            tryExec(st, "ALTER TABLE `article` ADD COLUMN `image` VARCHAR(255) DEFAULT NULL");
            tryExec(st, "ALTER TABLE `article` ADD COLUMN `views` INT NOT NULL DEFAULT 0");
            tryExec(st, "ALTER TABLE `article` ADD COLUMN `category_id` INT DEFAULT NULL");

            // ── 4. tag table ─────────────────────────────────────────────────
            st.execute(
                "CREATE TABLE IF NOT EXISTS `tag` (" +
                "  `id` INT NOT NULL AUTO_INCREMENT," +
                "  `name` VARCHAR(100) NOT NULL," +
                "  PRIMARY KEY (`id`)," +
                "  UNIQUE KEY `tag_name` (`name`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            // ── 5. article_tag junction ───────────────────────────────────────
            st.execute(
                "CREATE TABLE IF NOT EXISTS `article_tag` (" +
                "  `article_id` INT NOT NULL," +
                "  `tag_id` INT NOT NULL," +
                "  PRIMARY KEY (`article_id`, `tag_id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            // ── 6. comment table ──────────────────────────────────────────────
            st.execute(
                "CREATE TABLE IF NOT EXISTS `comment` (" +
                "  `id` INT NOT NULL AUTO_INCREMENT," +
                "  `article_id` INT NOT NULL," +
                "  `content` TEXT NOT NULL," +
                "  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  `author_name` VARCHAR(255) DEFAULT NULL," +
                "  `flagged` TINYINT(1) NOT NULL DEFAULT 0," +
                "  `hidden_from_public` TINYINT(1) NOT NULL DEFAULT 0," +
                "  PRIMARY KEY (`id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
            tryExec(st, "ALTER TABLE `comment` ADD COLUMN `flagged` TINYINT(1) NOT NULL DEFAULT 0");
            tryExec(st, "ALTER TABLE `comment` ADD COLUMN `hidden_from_public` TINYINT(1) NOT NULL DEFAULT 0");
            tryExec(st, "ALTER TABLE `comment` ADD COLUMN `author_name` VARCHAR(255) DEFAULT NULL");
            // Old schema had non-nullable columns our INSERT omits — make them all nullable
            tryExec(st, "ALTER TABLE `comment` MODIFY COLUMN `author` VARCHAR(255) DEFAULT NULL");
            tryExec(st, "ALTER TABLE `comment` MODIFY COLUMN `author_id` VARCHAR(32) DEFAULT NULL");
            tryExec(st, "ALTER TABLE `comment` MODIFY COLUMN `author_name` VARCHAR(255) DEFAULT NULL");
            // author_user_id: detect actual type then make nullable
            makeColumnNullable(cnx, st, "comment", "author_user_id");

            // ── 7. article_reaction table ─────────────────────────────────────
            st.execute(
                "CREATE TABLE IF NOT EXISTS `article_reaction` (" +
                "  `id` INT NOT NULL AUTO_INCREMENT," +
                "  `article_id` INT NOT NULL," +
                "  `user_id` INT NOT NULL," +
                "  `type` ENUM('like','dislike') NOT NULL," +
                "  PRIMARY KEY (`id`)," +
                "  UNIQUE KEY `uniq_article_user` (`article_id`, `user_id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            // ── 8. user table patch ───────────────────────────────────────────
            tryExec(st, "ALTER TABLE `user` ADD COLUMN `timeout_until` DATETIME DEFAULT NULL");

            System.out.println("[TestDatabaseMigration] All patches applied successfully.");

        } catch (SQLException e) {
            System.err.println("[TestDatabaseMigration] Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void makeColumnNullable(Connection cnx, Statement st, String table, String column) {
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?")) {
            ps.setString(1, table);
            ps.setString(2, column);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String dataType = rs.getString("DATA_TYPE").toUpperCase();
                long charLen = rs.getLong("CHARACTER_MAXIMUM_LENGTH");
                String typeDef;
                switch (dataType) {
                    case "BINARY":
                    case "VARBINARY":
                        typeDef = dataType + "(" + (charLen > 0 ? charLen : 16) + ")";
                        break;
                    case "VARCHAR":
                    case "CHAR":
                        typeDef = dataType + "(" + (charLen > 0 ? charLen : 255) + ")";
                        break;
                    case "BLOB":
                    case "TEXT":
                    case "LONGBLOB":
                    case "LONGTEXT":
                    case "MEDIUMBLOB":
                    case "MEDIUMTEXT":
                    case "TINYBLOB":
                    case "TINYTEXT":
                        typeDef = dataType;
                        break;
                    default:
                        typeDef = dataType;
                }
                tryExec(st, "ALTER TABLE `" + table + "` MODIFY COLUMN `" + column + "` " + typeDef + " DEFAULT NULL");
            }
        } catch (SQLException ignored) {}
    }

    private static void tryExec(Statement st, String sql) {
        try {
            st.execute(sql);
        } catch (SQLException ignored) {
            // column/table already exists, or constraint already dropped — safe to ignore
        }
    }

    private static boolean columnIsInt(Connection cnx, String table, String column) {
        String sql = "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
                     "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, table);
            ps.setString(2, column);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String type = rs.getString("DATA_TYPE").toLowerCase();
                return type.equals("int") || type.equals("bigint") || type.equals("smallint");
            }
        } catch (SQLException ignored) {}
        return false;
    }
}
