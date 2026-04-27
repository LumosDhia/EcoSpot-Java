# 🌿 EcoSpot: Full System Flow & Architecture Documentation

This master guide explains the entire flow of the EcoSpot application, spanning the **Backend Services**, the **Logic Controllers**, and the **Frontend FXML Resources**.

---

## 🏛️ 1. Global Architecture

The application is built using a **JavaFX + JDBC** architecture, mirroring a modern web-like MVC flow.

*   **Logic (Controllers)**: Located in `tn.esprit.event` (and other packages). They orchestrate the UI.
*   **Persistence (Services)**: Located in `tn.esprit.services`. They handle SQL interactions.
*   **Visuals (FXML)**: Located in `src/main/resources`. They define the UI structure.
*   **Data Models**: Plain Java objects representing database tables.

---

## 🛠️ 2. Service Layer (Persistence)

All services implement a `GlobalInterface<T>`, ensuring a consistent CRUD (Create, Read, Update, Delete) contract.

### [EventService.java](file:///d:/piecospot/src/main/java/tn/esprit/services/EventService.java)
Handles the `event` table. 
*   **`add()`**: Uses `PreparedStatement` to safely insert events and returns the generated DB ID.
*   **`getAll()`**: Fetches all events ordered by date.
*   **`delete()`**: Removes an event and handles errors if the ID is missing (e.g., mock data).

### [SponsorService.java](file:///d:/piecospot/src/main/java/tn/esprit/services/SponsorService.java)
Manages sponsors and the **Many-to-Many** link table `event_sponsor`.
*   **`assignSponsorToEvent()`**: Links a sponsor to an event.
*   **`getSponsorsForEvent()`**: Uses a `JOIN` query to fetch all sponsors specifically linked to one event.

### [BlogService.java](file:///d:/piecospot/src/main/java/tn/esprit/services/BlogService.java)
Manages the `article` and `category` tables.
*   **`search(String query)`**: Performs a complex SQL query searching titles and content, while joining with the categories table to get descriptive names.
*   **`getAll()`**: Convenience method that calls `search("")`.

### [PersonneService.java](file:///d:/piecospot/src/main/java/tn/esprit/services/PersonneService.java)
A generic service for managing people (`Personne`). 
*   Demonstrates the difference between **Statement** (less secure) and **PreparedStatement** (SQL Injection proof).

---

## 🎨 3. Frontend Architecture (FXML & CSS)

The frontend is modular. Instead of large static pages, it uses **Reusable Components (Cards)**.

### A. Layout Structure
Most pages use a `BorderPane` with:
- **Top**: A global navigation bar (`top-bar` and `main-nav`).
- **Center**: A `ScrollPane` containing a `FlowPane` (grid) which dynamically fills with cards.

### B. Core FXML Files ([resources/event](file:///d:/piecospot/src/main/resources/event))
| FXML File | Purpose | Key UI Elements |
| :--- | :--- | :--- |
| **EventManagement.fxml** | Main Event Hub | Search bar, Sort ChoiceBox, FlowPane grid. |
| **EventAdmin.fxml** | Admin Dashboard | List of events with management controls. |
| **EventForm.fxml** | Data Entry | `DatePicker` for dates, `TextArea` for descriptions, and a link to Sponsor Picker. |
| **EventDetail.fxml** | Content View | Large `ImageView` for the hero image and a container for Sponsor icons. |
| **EventCard.fxml** | Component | Small card with image thumbnail, date badge, and "Details" button. |

### C. Styling ([style.css](file:///d:/piecospot/src/main/resources/style.css))
The application uses a modern, premium design system:
- **Colors**: Deep greens (`#2d6a4f`), soft whites (`#f8fbf9`), and rich shadows.
- **Components**: Rounded corners (`-fx-background-radius`), hover effects, and custom fonts.

---

## 🔄 4. The "Full Flow" Example: Adding an Event

1.  **UI Start**: User clicks "Manage Events" in **EventManagement.fxml**.
2.  **Navigation**: `EventManagementController` switches the scene to `EventAdmin.fxml`.
3.  **Form Entry**: Admin clicks "Add Event", loading `EventForm.fxml`.
4.  **Data Capture**: `EventFormController` collects strings from `TextFields` and dates from `DatePickers`.
5.  **Image Upload**: `ImageUploadUtils` copies the selected local file to project storage.
6.  **Persistence**: `EventService.add(currentEvent)` is called to save to MySQL.
7.  **Relational Link**: `SponsorService` is called for each selected sponsor to populate the link table.
8.  **Completion**: The UI returns to the Admin dashboard and calls `refreshData()`, which re-queries `EventService.getAll()`.

---

## 📋 Comprehensive Function-to-File Map

| Package / Directory | File Type | Responsibility |
| :--- | :--- | :--- |
| `tn.esprit.event` | **Controllers** | Handle Button clicks, fill FXML fields, trigger Services. |
| `tn.esprit.services` | **Services** | Direct SQL communication (C.R.U.D). |
| `src/resources/event` | **FXML** | The "Skeleton" of the UI (XML tags). |
| `src/resources` | **CSS** | The "Skin" of the UI (Appearance). |

---
*End of Documentation.*
