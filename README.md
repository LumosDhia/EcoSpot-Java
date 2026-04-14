# EcoSpot-Java Database Setup

### MariaDB Configuration
- **Database Name:** `projetdev`
- **User:** `root`
- **Password:** `root`
- **Host:** `localhost`
- **Port:** `3306`

### Connection String
```java
jdbc:mysql://localhost:3306/projetdev
```

### Driver
- **MySQL Connector/J:** `8.0.33` (Included in `pom.xml`)

### Scene Builder (FXML)
- Project setting added in `.vscode/settings.json`:
  - `javafx.sceneBuilder.path = C:\\Program Files\\SceneBuilder\\SceneBuilder.exe`
- If Scene Builder is installed in a different location, update that path.
