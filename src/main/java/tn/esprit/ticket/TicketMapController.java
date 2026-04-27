package tn.esprit.ticket;

import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import tn.esprit.services.TicketService;
import tn.esprit.util.SessionManager;

import java.io.IOException;
import java.util.List;

public class TicketMapController {

    @FXML private VBox webViewContainer;
    private WebView webView;
    private WebEngine webEngine;
    private final TicketService ticketService = new TicketService();

    @FXML
    public void initialize() {
        webView = new WebView();
        webEngine = webView.getEngine();
        webViewContainer.getChildren().add(webView);
        javafx.scene.layout.VBox.setVgrow(webView, javafx.scene.layout.Priority.ALWAYS);
        
        tn.esprit.util.NavigationHistory.track(webViewContainer, "/ticket/TicketMap.fxml");

        loadMap();
    }

    private void loadMap() {
        List<Ticket> tickets = ticketService.getAll();
        
        StringBuilder markersJson = new StringBuilder("[");
        boolean first = true;
        for (Ticket t : tickets) {
            if (t.getLatitude() != 0 && t.getLongitude() != 0) {
                if (!first) markersJson.append(",");
                String color = t.getStatus() == TicketStatus.COMPLETED || t.getStatus() == TicketStatus.PUBLISHED ? "green" : "red";
                String title = t.getTitle().replace("'", "\\'").replace("\"", "\\\"");
                
                markersJson.append(String.format("{\"lat\": %f, \"lng\": %f, \"title\": \"%s\", \"color\": \"%s\"}", 
                        t.getLatitude(), t.getLongitude(), title, color));
                first = false;
            }
        }
        markersJson.append("]");

        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"utf-8\" />\n" +
                "    <title>EcoSpot Map</title>\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.css\" />\n" +
                "    <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet.markercluster@1.5.3/dist/MarkerCluster.css\" />\n" +
                "    <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet.markercluster@1.5.3/dist/MarkerCluster.Default.css\" />\n" +
                "    <script src=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.js\"></script>\n" +
                "    <script src=\"https://unpkg.com/leaflet.markercluster@1.5.3/dist/leaflet.markercluster.js\"></script>\n" +
                "    <style>\n" +
                "        body { padding: 0; margin: 0; }\n" +
                "        html, body, #map { height: 100%; width: 100%; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }\n" +
                "        .custom-popup .leaflet-popup-content-wrapper { background: #fff; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }\n" +
                "        .custom-popup .leaflet-popup-content { margin: 15px; font-weight: bold; color: #1f2937; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id=\"map\"></div>\n" +
                "    <script>\n" +
                "        var map = L.map('map').setView([36.8, 10.18], 6);\n" +
                "        L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {\n" +
                "            attribution: '&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors'\n" +
                "        }).addTo(map);\n" +
                "\n" +
                "        var markers = L.markerClusterGroup({\n" +
                "            maxClusterRadius: 50,\n" +
                "            spiderfyOnMaxZoom: true,\n" +
                "            showCoverageOnHover: false,\n" +
                "            zoomToBoundsOnClick: true\n" +
                "        });\n" +
                "\n" +
                "        var data = " + markersJson.toString() + ";\n" +
                "\n" +
                "        var redIcon = new L.Icon({\n" +
                "            iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',\n" +
                "            shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',\n" +
                "            iconSize: [25, 41],\n" +
                "            iconAnchor: [12, 41],\n" +
                "            popupAnchor: [1, -34],\n" +
                "            shadowSize: [41, 41]\n" +
                "        });\n" +
                "\n" +
                "        var greenIcon = new L.Icon({\n" +
                "            iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',\n" +
                "            shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',\n" +
                "            iconSize: [25, 41],\n" +
                "            iconAnchor: [12, 41],\n" +
                "            popupAnchor: [1, -34],\n" +
                "            shadowSize: [41, 41]\n" +
                "        });\n" +
                "\n" +
                "        data.forEach(function(item) {\n" +
                "            if (item.lat && item.lng) {\n" +
                "                var icon = item.color === 'green' ? greenIcon : redIcon;\n" +
                "                var marker = L.marker([item.lat, item.lng], {icon: icon});\n" +
                "                marker.bindPopup('<div style=\"text-align:center;\">' + item.title + '<br><span style=\"color:' + (item.color==='green'?'#16a34a':'#dc2626') + '; font-size: 12px;\">' + (item.color==='green'?'Resolved':'Pending') + '</span></div>', {className: 'custom-popup'});\n" +
                "                markers.addLayer(marker);\n" +
                "            }\n" +
                "        });\n" +
                "\n" +
                "        map.addLayer(markers);\n" +
                "        \n" +
                "        // Fit bounds if we have markers\n" +
                "        if (data.length > 0) {\n" +
                "            setTimeout(function() { map.fitBounds(markers.getBounds().pad(0.1)); }, 500);\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";

        webEngine.loadContent(html, "text/html");
    }

    @FXML
    void goBack(ActionEvent event) {
        if (!tn.esprit.util.NavigationHistory.goBack(event)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/Dashboard.fxml"));
                Parent root = loader.load();
                tn.esprit.user.DashboardController controller = loader.getController();
                if (controller != null && SessionManager.isLoggedIn()) {
                    controller.setUser(SessionManager.getCurrentUser());
                }
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
