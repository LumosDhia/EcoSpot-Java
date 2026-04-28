# Statistics Module Roadmap — Ecospot Blog & Articles

WordPress-parity analytics for the JavaFX desktop app. Organized into phases: database, data collection, service layer, and UI.

---

## Feature Inventory (WordPress-Parity)

| Feature | WordPress Equivalent | Priority |
|---|---|---|
| Views over time (hourly/daily/weekly/monthly/yearly) | Site Stats → Views | P0 |
| Per-article breakdown | Site Stats → Top Posts | P0 |
| Top articles by views | Insights → Top Posts | P0 |
| Author performance | Site Stats → Authors | P0 |
| Category & tag stats | Site Stats → Categories/Tags | P0 |
| Comments analytics | Site Stats → Comments | P0 |
| Engagement rate (likes/dislikes ratio) | Likes stats | P1 |
| Reading time distribution | (custom) | P1 |
| Publish frequency heatmap | Activity → Post Calendar | P1 |
| Top commenters | Site Stats → Commenters | P1 |
| Search terms used inside app | Site Stats → Search Terms | P1 |
| Traffic by hour of day | Insights → Hours | P2 |
| Article status funnel (draft → published) | (custom) | P2 |
| Avg comments per article | Insights → Comments | P2 |
| Content velocity (articles/week) | (custom) | P2 |
| Best-performing tags | Tag cloud weighted by views | P2 |

---

## Phase 1 — Database Layer

### 1.1 New Tables

```sql
-- Granular view events (replaces simple view counter)
CREATE TABLE article_view_event (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id  INT NOT NULL,
    user_id     INT,                      -- NULL = anonymous
    session_id  VARCHAR(64),              -- 15-min dedup key (already used in BlogService)
    viewed_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (article_id) REFERENCES article(id) ON DELETE CASCADE,
    INDEX idx_article_viewed (article_id, viewed_at),
    INDEX idx_viewed_at (viewed_at)
);

-- Reaction events (like/dislike with timestamp)
CREATE TABLE article_reaction_event (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id  INT NOT NULL,
    user_id     INT,
    reaction    ENUM('LIKE','DISLIKE') NOT NULL,
    reacted_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (article_id) REFERENCES article(id) ON DELETE CASCADE,
    INDEX idx_article_reacted (article_id, reacted_at)
);

-- In-app search terms log
CREATE TABLE search_term_log (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    term        VARCHAR(255) NOT NULL,
    result_count INT NOT NULL DEFAULT 0,
    searched_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_searched_at (searched_at),
    INDEX idx_term (term)
);

-- Daily pre-aggregated snapshot (performance cache for charts)
CREATE TABLE article_stats_daily (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id  INT NOT NULL,
    stat_date   DATE NOT NULL,
    views       INT NOT NULL DEFAULT 0,
    likes       INT NOT NULL DEFAULT 0,
    dislikes    INT NOT NULL DEFAULT 0,
    comments    INT NOT NULL DEFAULT 0,
    UNIQUE KEY uq_article_date (article_id, stat_date),
    FOREIGN KEY (article_id) REFERENCES article(id) ON DELETE CASCADE,
    INDEX idx_stat_date (stat_date)
);
```

### 1.2 Migrations

Add `article_view_event` backfill from existing `article.views` column (approximate — set all to `published_at` date). Keep the existing `views` column as a denormalized fast-read counter.

---

## Phase 2 — Data Collection Layer

### 2.1 `StatisticsCollector.java` (new utility singleton)

Location: `src/main/java/tn/esprit/util/StatisticsCollector.java`

Responsibilities:
- `recordView(int articleId, String sessionId, Integer userId)` — insert into `article_view_event`, then increment `article.views` atomically
- `recordReaction(int articleId, Integer userId, String reactionType)` — insert into `article_reaction_event`
- `recordSearchTerm(String term, int resultCount)` — insert into `search_term_log`
- `flushDailySnapshot()` — called on app startup and at midnight via a background `ScheduledExecutorService`: aggregates yesterday's events into `article_stats_daily`

### 2.2 Wire into Existing Controllers

| File | Change |
|---|---|
| `BlogService.incrementViews()` | Replace direct UPDATE with `StatisticsCollector.recordView()` |
| `ReactionService` | After like/dislike persist, call `StatisticsCollector.recordReaction()` |
| `BlogManagementController` — search handler | After search executes, call `StatisticsCollector.recordSearchTerm(term, count)` |
| `ArticlesManagementController` — search handler | Same as above |

### 2.3 Daily Aggregator

```java
// StatisticsCollector — daily flush SQL
INSERT INTO article_stats_daily (article_id, stat_date, views, likes, dislikes, comments)
SELECT
    ave.article_id,
    DATE(ave.viewed_at) AS stat_date,
    COUNT(ave.id) AS views,
    COALESCE(likes.cnt, 0),
    COALESCE(dislikes.cnt, 0),
    COALESCE(cmt.cnt, 0)
FROM article_view_event ave
LEFT JOIN (...) likes ON ...
LEFT JOIN (...) dislikes ON ...
LEFT JOIN (...) cmt ON ...
WHERE DATE(ave.viewed_at) = CURDATE() - INTERVAL 1 DAY
GROUP BY ave.article_id, stat_date
ON DUPLICATE KEY UPDATE views = VALUES(views), ...;
```

---

## Phase 3 — Service Layer

### 3.1 `StatisticsService.java`

Location: `src/main/java/tn/esprit/services/StatisticsService.java`

All methods return plain DTOs or `Map<String, Object>` for easy JavaFX chart binding.

#### 3.1.1 Overview Stats
```java
// Total views across all articles in period
int getTotalViews(LocalDate from, LocalDate to);

// Total published articles count
int getTotalPublishedArticles();

// Total comments count
int getTotalComments();

// Total likes and dislikes
Map<String, Integer> getTotalReactions();
```

#### 3.1.2 Time-Series (for line/area charts)
```java
// Daily view counts for a range → [{"date":"2026-04-01","views":34}, ...]
List<Map<String, Object>> getViewsTimeSeries(LocalDate from, LocalDate to);

// Weekly aggregation
List<Map<String, Object>> getViewsWeekly(int weeksBack);

// Monthly aggregation
List<Map<String, Object>> getViewsMonthly(int monthsBack);

// Hourly distribution (0–23) across all time → for "best time to publish"
List<Map<String, Object>> getViewsByHourOfDay();
```

#### 3.1.3 Top Content
```java
// Top N articles by views in period
List<Map<String, Object>> getTopArticlesByViews(LocalDate from, LocalDate to, int limit);

// Top N articles by engagement (likes + comments)
List<Map<String, Object>> getTopArticlesByEngagement(int limit);

// Articles with highest like/dislike ratio
List<Map<String, Object>> getMostLikedArticles(int limit);

// Most commented articles
List<Map<String, Object>> getMostCommentedArticles(int limit);
```

#### 3.1.4 Author Stats
```java
// Per-author: article count, total views, avg views/article, total likes
List<Map<String, Object>> getAuthorStats(LocalDate from, LocalDate to);

// Author publish frequency (articles per week over last N weeks)
List<Map<String, Object>> getAuthorPublishFrequency(int authorId, int weeksBack);
```

#### 3.1.5 Category & Tag Stats
```java
// Per-category: article count, total views, avg engagement
List<Map<String, Object>> getCategoryStats();

// Per-tag: article count, total views (weighted tag cloud data)
List<Map<String, Object>> getTagStats(int limit);
```

#### 3.1.6 Comment Analytics
```java
// Comments per day over time
List<Map<String, Object>> getCommentTimeSeries(LocalDate from, LocalDate to);

// Top commenters by comment count
List<Map<String, Object>> getTopCommenters(int limit);

// Flagged vs. approved comment ratio
Map<String, Integer> getCommentModerationStats();
```

#### 3.1.7 Search Terms
```java
// Top N searched terms with frequency
List<Map<String, Object>> getTopSearchTerms(int limit);

// Search terms with zero results (content gap analysis)
List<Map<String, Object>> getZeroResultSearchTerms(int limit);
```

#### 3.1.8 Publishing Patterns
```java
// Article count per day-of-week → [{"day":"Monday","count":5}, ...]
List<Map<String, Object>> getPublishFrequencyByDayOfWeek();

// Publish heatmap: articles per week per month (GitHub-style)
List<Map<String, Object>> getPublishHeatmap(int monthsBack);

// Article status funnel counts: {draft: N, pending: N, published: N}
Map<String, Integer> getArticleStatusFunnel();
```

---

## Phase 4 — UI Layer

### 4.1 New Files

| File | Purpose |
|---|---|
| `src/main/java/tn/esprit/blog/StatisticsController.java` | Main statistics dashboard controller |
| `src/main/resources/blog/Statistics.fxml` | Dashboard FXML layout |
| `src/main/java/tn/esprit/blog/ArticleStatsController.java` | Per-article drilldown controller |
| `src/main/resources/blog/ArticleStats.fxml` | Per-article stats FXML |

### 4.2 Statistics Dashboard Layout (Statistics.fxml)

```
┌─────────────────────────────────────────────────────────────────┐
│  [← Back]  Blog Statistics               [Period: 7d ▾] [Refresh]│
├──────────────────────────────────────────────────────────────────┤
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐        │
│  │ 1,284    │  │ 42       │  │ 318      │  │ 87%      │        │
│  │ Total    │  │ Articles │  │ Comments │  │ Like     │        │
│  │ Views    │  │ Published│  │          │  │ Rate     │        │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘        │
├──────────────────────────────────────────────────────────────────┤
│  Views Over Time [Line/Area Chart — full width]                  │
│  [7 Days] [30 Days] [90 Days] [1 Year] [Custom]                  │
├────────────────────────────┬─────────────────────────────────────┤
│  Top Articles              │  Views by Category [PieChart]       │
│  ┌──────────────────────┐  │                                     │
│  │ 1. Article Title 312v│  │   ○ Ecology  45%                    │
│  │ 2. Article Title 201v│  │   ○ Events   30%                    │
│  │ 3. Article Title 187v│  │   ○ News     25%                    │
│  │ ...                  │  │                                     │
│  └──────────────────────┘  │                                     │
├────────────────────────────┼─────────────────────────────────────┤
│  Author Leaderboard        │  Top Search Terms [BarChart]        │
│  [BarChart — horizontal]   │  [BarChart — horizontal]            │
├────────────────────────────┴─────────────────────────────────────┤
│  Comments Over Time [BarChart]   │  Publish Heatmap [GridPane]   │
├──────────────────────────────────┴───────────────────────────────┤
│  Top Commenters         │  Tags Word Cloud  │  Status Funnel     │
│  (list)                 │  (weighted labels) │  (BarChart stacked)│
└─────────────────────────────────────────────────────────────────┘
```

### 4.3 JavaFX Chart Mapping

| Stat | JavaFX Chart Class | X-Axis | Y-Axis |
|---|---|---|---|
| Views over time | `AreaChart<String, Number>` | Date | Views |
| Top articles | `BarChart<String, Number>` (horizontal) | Article title | Views |
| Category breakdown | `PieChart` | — | — |
| Author leaderboard | `BarChart<String, Number>` | Author | Views |
| Top search terms | `BarChart<String, Number>` | Term | Count |
| Comments over time | `BarChart<String, Number>` | Date | Comments |
| Views by hour | `BarChart<Number, Number>` | Hour (0-23) | Views |
| Publish heatmap | `GridPane` + colored `Label` cells | Week | Day |
| Article status funnel | `StackedBarChart` or `PieChart` | — | Count |
| Engagement trend | `LineChart<String, Number>` | Date | Likes |

### 4.4 Period Selector

Dropdown (`ChoiceBox<String>`) with values:
- Today
- Last 7 Days
- Last 30 Days
- Last 3 Months
- Last 12 Months
- All Time
- Custom Range (opens `DatePicker` dialog for from/to)

All charts reload when period changes via a listener on the `ChoiceBox`.

### 4.5 Per-Article Drilldown (ArticleStats.fxml)

Accessible by clicking any article row in Top Articles list.

Sections:
1. **Header** — Title, author, published date, category, tags
2. **KPI row** — Total views, likes, dislikes, comments, avg reading time
3. **Views timeline** — `AreaChart` for this article only
4. **Reactions timeline** — `LineChart` with two series (likes / dislikes)
5. **Comment activity** — `BarChart` comments per day
6. **Top search terms that led to this article** — if search-log linkage is implemented

### 4.6 Navigation Integration

In `ArticlesManagementController.fxml`, add a "Statistics" button in the top action bar:
```java
// ArticlesManagementController.java
@FXML private Button statisticsBtn;

@FXML
void onStatisticsClick(ActionEvent e) {
    WindowUtils.navigateTo(statisticsBtn, "/blog/Statistics.fxml");
}
```

Also add per-row "Stats" icon button in the articles table that opens `ArticleStats.fxml` with the selected article ID.

---

## Phase 5 — Styling & Polish

### 5.1 CSS

Add to existing stylesheet or create `statistics.css`:

```css
.stat-kpi-card {
    -fx-background-color: #ffffff;
    -fx-background-radius: 8px;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);
    -fx-padding: 16px 24px;
    -fx-min-width: 160px;
}

.stat-kpi-value {
    -fx-font-size: 28px;
    -fx-font-weight: bold;
    -fx-text-fill: #1a1a2e;
}

.stat-kpi-label {
    -fx-font-size: 12px;
    -fx-text-fill: #6b7280;
    -fx-text-transform: uppercase;
}

.chart-area {
    -fx-background-color: #f9fafb;
    -fx-background-radius: 8px;
    -fx-padding: 16px;
}

/* Heatmap cells */
.heatmap-cell-0 { -fx-background-color: #ebedf0; }
.heatmap-cell-1 { -fx-background-color: #9be9a8; }
.heatmap-cell-2 { -fx-background-color: #40c463; }
.heatmap-cell-3 { -fx-background-color: #30a14e; }
.heatmap-cell-4 { -fx-background-color: #216e39; }
```

### 5.2 Loading States

Wrap each chart section in a `StackPane` containing the chart + a `ProgressIndicator`. Use `Task<Void>` for background DB queries; show spinner while loading, swap in chart on `Platform.runLater()`.

### 5.3 Empty States

When a chart has no data for the selected period, show a centered `Label` ("No data for this period") instead of an empty chart frame.

---

## Phase 6 — Export

### 6.1 CSV Export

Add "Export CSV" button on dashboard and per-article view.

```java
// ExportService.java (new)
void exportViewsCSV(List<Map<String, Object>> data, File target);
void exportTopArticlesCSV(List<Map<String, Object>> data, File target);
```

Use JavaFX `FileChooser` with `ExtensionFilter("CSV", "*.csv")` to pick save location.

### 6.2 PDF Report (optional P2)

Use Apache PDFBox (add Maven dependency) to generate a monthly report PDF:
- Cover page with date range and site name
- KPI summary table
- Embedded chart images (snapshot via `chart.snapshot(null, null)`)
- Top articles table
- Author leaderboard

---

## Implementation Order

```
Week 1
  ├── Phase 1: Create all new tables + run migration
  └── Phase 2: StatisticsCollector wired into BlogService + ReactionService

Week 2
  └── Phase 3: StatisticsService — all query methods with unit tests

Week 3
  ├── Phase 4.1–4.3: Statistics.fxml layout + StatisticsController skeleton
  └── Phase 4.5–4.6: KPI cards + period selector working end-to-end

Week 4
  ├── Phase 4.3: All charts wired to live data
  └── Phase 4.5: Per-article drilldown view

Week 5
  ├── Phase 5: CSS polish + loading spinners + empty states
  └── Phase 6.1: CSV export
```

---

## Dependencies to Add (pom.xml)

No new dependencies required for charts — JavaFX Charts is bundled with JavaFX 21.

Optional additions:
```xml
<!-- PDF export (P2 only) -->
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>3.0.1</version>
</dependency>
```

---

## SQL Query Reference

### Views time series (daily)
```sql
SELECT DATE(viewed_at) AS day, COUNT(*) AS views
FROM article_view_event
WHERE viewed_at BETWEEN ? AND ?
GROUP BY day
ORDER BY day;
```

### Top articles by views
```sql
SELECT a.id, a.title, COUNT(ave.id) AS view_count,
       COUNT(DISTINCT c.id) AS comment_count
FROM article a
LEFT JOIN article_view_event ave ON ave.article_id = a.id
    AND ave.viewed_at BETWEEN ? AND ?
LEFT JOIN comment c ON c.article_id = a.id
WHERE a.status = 'published'
GROUP BY a.id, a.title
ORDER BY view_count DESC
LIMIT ?;
```

### Author leaderboard
```sql
SELECT a.writer_id, COUNT(DISTINCT a.id) AS article_count,
       COALESCE(SUM(asd.views), 0) AS total_views,
       COALESCE(SUM(asd.likes), 0) AS total_likes
FROM article a
LEFT JOIN article_stats_daily asd ON asd.article_id = a.id
    AND asd.stat_date BETWEEN ? AND ?
GROUP BY a.writer_id
ORDER BY total_views DESC;
```

### Category breakdown
```sql
SELECT c.name, COUNT(DISTINCT a.id) AS article_count,
       COALESCE(SUM(asd.views), 0) AS total_views
FROM category c
JOIN article a ON a.category_id = c.id
LEFT JOIN article_stats_daily asd ON asd.article_id = a.id
GROUP BY c.id, c.name
ORDER BY total_views DESC;
```

### Top search terms
```sql
SELECT term, COUNT(*) AS search_count, AVG(result_count) AS avg_results
FROM search_term_log
WHERE searched_at BETWEEN ? AND ?
GROUP BY term
ORDER BY search_count DESC
LIMIT ?;
```

### Publish heatmap
```sql
SELECT DATE(published_at) AS pub_date, COUNT(*) AS count
FROM article
WHERE published_at >= DATE_SUB(CURDATE(), INTERVAL ? MONTH)
  AND status = 'published'
GROUP BY pub_date
ORDER BY pub_date;
```

### Views by hour of day
```sql
SELECT HOUR(viewed_at) AS hour_of_day, COUNT(*) AS views
FROM article_view_event
GROUP BY hour_of_day
ORDER BY hour_of_day;
```
