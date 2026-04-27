# 🌿 EcoSpot: Advanced Event Module Technical Guide

This document provides a deep dive into the technical implementation of the Advanced Event Module in EcoSpot.

---

## 🏗️ 1. Module Overview
The Event Module is a full-stack implementation using **JavaFX** for the frontend, **JDBC** for persistence, and several **Third-Party APIs** for advanced features.

### Core Workflow:
1.  **Creation**: Admin/NGO creates an event with a location name.
2.  **Geocoding**: The system calls the **Nominatim API** to convert the location name into `Latitude` and `Longitude`.
3.  **Visualization**: The event is displayed with a **Leaflet Map** and **AI Insights**.
4.  **Interaction**: Users can join events, triggering an **Automated Email Confirmation**.

---

## 🌍 2. Geocoding & Proximity Logic
We use the **Nominatim (OpenStreetMap)** API to handle spatial data without requiring expensive Google Maps keys.

### Implementation Details:
*   **Service**: `GeocodingService.java`
*   **Process**:
    1.  Sends an HTTP GET request to `https://nominatim.openstreetmap.org/search`.
    2.  Parses the JSON response to extract `lat` and `lon`.
    3.  **Haversine Formula**: We implemented this spherical trigonometry formula to calculate the distance between two points on Earth:
        ```java
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        ```
*   **Use Case**: This powers the **"Nearby Events"** sorting and the proximity sidebar in the event details.

---

## 🗺️ 3. OpenStreetMap Integration
Instead of a static image, we provide an interactive map experience using **Leaflet.js** inside a JavaFX **WebView**.

### Implementation Details:
*   **Component**: `WebView` in `EventDetail.fxml`.
*   **Controller**: `EventDetailController.java`.
*   **Mechanism**:
    1.  We construct a dynamic HTML string containing a Leaflet script.
    2.  The event's coordinates are injected into the JavaScript `L.map().setView()`.
    3.  The map is loaded using `webView.getEngine().loadContent(html)`.
    4.  A custom marker and popup are added to show the event name and location.

---

## 🤖 4. AI Analyzer (OpenRouter)
We integrated the **OpenRouter API** to provide "Smart Attendance Prediction" using state-of-the-art LLMs (Gemini/Llama).

### Implementation Details:
*   **Service**: `OpenRouterEventService.java`.
*   **Flow**:
    1.  **Prompt Engineering**: We send a detailed prompt including the event's name, description, capacity, and date to the AI.
    2.  **Asynchronous Execution**: The AI call is wrapped in a `new Thread()` to prevent the JavaFX UI from freezing.
    3.  **JSON Response**: The AI is instructed to return *only* a JSON object: `{"success_level": "...", "analysis": "..."}`.
    4.  **UI Sync**: We use `Platform.runLater()` to safely update the UI badges once the AI finishes "thinking."

---

## 📧 5. Automated Email System
Communication is handled via **Jakarta Mail (formerly JavaMail)**.

### Implementation Details:
*   **Service**: `EmailService.java`.
*   **Trigger**: Success of `EventService.joinEvent()`.
*   **Features**:
    1.  Sends a rich HTML confirmation email.
    2.  Uses SSL/TLS on port 465 for secure communication.
    3.  Runs in a background thread to ensure a smooth user experience.

---

## 💾 6. Database & Persistence
The MySQL schema was expanded to match the advanced features:
*   **`event` table**: Added `latitude` and `longitude` (Double) and `slug` (Varchar).
*   **`user` table**: Added `address`, `city`, and `zipcode` to support proximity features.
*   **`event_participant`**: A link table managing the many-to-many relationship between users and events.

---
*Created by EcoSpot AI Team | 2026*
