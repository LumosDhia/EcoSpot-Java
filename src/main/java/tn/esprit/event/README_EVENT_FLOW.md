<<<<
# 🌿 EcoSpot: Full System Flow & Architecture Documentation

This master guide explains the entire flow of the EcoSpot application, spanning the **Backend Services**, the **Logic Controllers**, and the **Frontend FXML Resources**.

---

## 🏛️ 1. Global Architecture
====
# 🌿 EcoSpot: Full System Flow & Architecture Documentation

This master guide explains the entire flow of the EcoSpot application, spanning the **Backend Services**, the **Logic Controllers**, and the **Frontend FXML Resources**.

---

## 🏛️ 1. Global Architecture
>>>>
<<<<
### [EventService.java](file:///d:/piecospot/src/main/java/tn/esprit/services/EventService.java)
Handles the `event` table. 
*   **`add()`**: Uses `PreparedStatement` to safely insert events and returns the generated DB ID.
*   **`getAll()`**: Fetches all events ordered by date.
*   **`delete()`**: Removes an event and handles errors if the ID is missing (e.g., mock data).
====
### [EventService.java](file:///d:/piecospot/src/main/java/tn/esprit/services/EventService.java)
Handles the `event` table with advanced relational logic. 
*   **`add()`**: Automatically generates SEO-friendly **Slugs** and stores Coordinates (Lat/Lon).
*   **`joinEvent()`**: A transactional method that decrements capacity, records the participant, and triggers an **Asynchronous Email Confirmation**.
*   **`generateSlug()`**: A regex-based utility that converts event names into URL-safe strings.

### [GeocodingService.java](file:///d:/piecospot/src/main/java/tn/esprit/services/GeocodingService.java) [NEW]
The "Spatial Intelligence" engine of the app.
*   **API Used**: **Nominatim API (OpenStreetMap)**.
*   **How Geocoding works**: It sends an HTTP GET request to Nominatim with a text address. The API returns a JSON array containing the exact `latitude` and `longitude`.
*   **`calculateDistance()`**: Implements the **Haversine Formula** to calculate the distance between two points on the Earth's surface in kilometers. This powers the "Nearby Events" feature.

### [EmailService.java](file:///d:/piecospot/src/main/java/tn/esprit/util/EmailService.java)
Handles SMTP communication.
*   Uses **Jakarta Mail** to send rich HTML emails.
*   Configured via `.env` for security (MAIL_USER, MAIL_PASS).
>>>>
<<<<
### B. Core FXML Files ([resources/event](file:///d:/piecospot/src/main/resources/event))
| FXML File | Purpose | Key UI Elements |
| :--- | :--- | :--- |
| **EventManagement.fxml** | Main Event Hub | Search bar, Sort ChoiceBox, FlowPane grid. |
| **EventAdmin.fxml** | Admin Dashboard | List of events with management controls. |
| **EventForm.fxml** | Data Entry | `DatePicker` for dates, `TextArea` for descriptions, and a link to Sponsor Picker. |
| **EventDetail.fxml** | Content View | Large `ImageView` for the hero image and a container for Sponsor icons. |
| **EventCard.fxml** | Component | Small card with image thumbnail, date badge, and "Details" button. |
====
### B. Core FXML Files ([resources/event](file:///d:/piecospot/src/main/resources/event))
| FXML File | Purpose | Key UI Elements |
| :--- | :--- | :--- |
| **EventManagement.fxml** | Main Event Hub | Search bar, **"Nearby"** Sort option, FlowPane grid. |
| **EventAdmin.fxml** | Admin Dashboard | List of events with management controls. |
| **EventForm.fxml** | Data Entry | Map Coordinate picker, Date/Time pickers. |
| **EventDetail.fxml** | Content View | **WebView (Leaflet Map)**, Nearby Events sidebar, Participant list. |
| **EventCard.fxml** | Component | Small card with image thumbnail, date badge, and "Details" button. |
>>>>
<<<<
---
*End of Documentation.*
====
---

## 🤖 5. Future AI Integration Ideas (Roadmap)

To take the Event module to the next level, the following AI implementations are proposed:

### A. AI Event Content Generator
*   **Concept**: Use **OpenRouter (Gemini/Llama)** to automatically generate compelling, SEO-friendly descriptions and titles for events based on a few keywords.
*   **Benefit**: Saves time for NGOs and ensures professional-looking event pages.

### B. Smart Attendance Predictor
*   **Concept**: A Machine Learning model that analyzes historical participation, event category, and **real-time Weather API data** to predict the expected turnout.
*   **Benefit**: Helps organizers manage logistics (food, security, transport) more efficiently.

### C. Eco-Impact Personalizer
*   **Concept**: An AI that calculates a user's "Carbon Offset" based on the events they attend (e.g., "By attending 3 Tree Planting events, you've offset 50kg of CO2").
*   **Benefit**: Gamifies the environmental contribution and increases user retention.

### D. Intelligent Event Matching
*   **Concept**: A recommendation engine that suggests events to users based on their past interests and their current geolocated city.
*   **Benefit**: Increases participation rates by showing users exactly what they care about.

---
*End of Documentation.*
>>>>
