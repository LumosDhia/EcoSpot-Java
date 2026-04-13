package tn.esprit.event;

import java.util.Objects;

public class Sponsor {
    private int id;
    private String name;
    private String image;
    private String description;
    private String sector;
    private String location;

    public Sponsor() {}

    public Sponsor(int id, String name, String image, String description, String sector, String location) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.description = description;
        this.sector = sector;
        this.location = location;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sponsor sponsor = (Sponsor) o;
        return id == sponsor.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name;
    }
}
