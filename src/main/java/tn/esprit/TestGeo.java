package tn.esprit;

import tn.esprit.services.GeocodingService;
import java.util.List;

public class TestGeo {
    public static void main(String[] args) {
        GeocodingService s = new GeocodingService();
        List<GeocodingService.Place> places = s.search("Sfax");
        for (GeocodingService.Place p : places) {
            System.out.println(p.getDisplayName() + " - " + p.getLat() + ", " + p.getLon());
        }
    }
}
