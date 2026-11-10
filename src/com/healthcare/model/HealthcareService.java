package com.healthcare.model;

/**
 * Model representing clinical or healthcare services offered by the facility.
 */
public class HealthcareService {
    private String id;
    private String name;
    private String category;
    private String description;
    private String duration;
    private double cost;
    private String preparation;
    private String icon;

    public HealthcareService(String id, String name, String category, String description,
                             String duration, double cost, String preparation, String icon) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.duration = duration;
        this.cost = cost;
        this.preparation = preparation;
        this.icon = icon;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getDuration() { return duration; }
    public double getCost() { return cost; }
    public String getPreparation() { return preparation; }
    public String getIcon() { return icon; }
}
