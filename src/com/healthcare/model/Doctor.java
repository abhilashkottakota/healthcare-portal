package com.healthcare.model;

import java.util.List;

/**
 * Model representing a Doctor in the healthcare portal.
 */
public class Doctor {
    private String id;
    private String name;
    private String specialty;
    private String qualifications;
    private int experienceYears;
    private double rating;
    private int reviewsCount;
    private double consultationFee;
    private String hospital;
    private String availability; // e.g. "Available Today", "Mon - Fri"
    private List<String> availableSlots;
    private String bio;
    private String imageUrl;

    public Doctor(String id, String name, String specialty, String qualifications,
                  int experienceYears, double rating, int reviewsCount,
                  double consultationFee, String hospital, String availability,
                  List<String> availableSlots, String bio, String imageUrl) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.qualifications = qualifications;
        this.experienceYears = experienceYears;
        this.rating = rating;
        this.reviewsCount = reviewsCount;
        this.consultationFee = consultationFee;
        this.hospital = hospital;
        this.availability = availability;
        this.availableSlots = availableSlots;
        this.bio = bio;
        this.imageUrl = imageUrl;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getSpecialty() { return specialty; }
    public String getQualifications() { return qualifications; }
    public int getExperienceYears() { return experienceYears; }
    public double getRating() { return rating; }
    public int getReviewsCount() { return reviewsCount; }
    public double getConsultationFee() { return consultationFee; }
    public String getHospital() { return hospital; }
    public String getAvailability() { return availability; }
    public List<String> getAvailableSlots() { return availableSlots; }
    public String getBio() { return bio; }
    public String getImageUrl() { return imageUrl; }
}
