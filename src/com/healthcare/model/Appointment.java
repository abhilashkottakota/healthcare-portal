package com.healthcare.model;

/**
 * Model representing an Appointment booked by a patient.
 */
public class Appointment {
    private String id;
    private String patientName;
    private String patientPhone;
    private String patientEmail;
    private int patientAge;
    private String patientGender;
    private String doctorId;
    private String doctorName;
    private String specialty;
    private String appointmentDate;
    private String timeSlot;
    private String type; // "In-Person" or "Virtual Teleconsultation"
    private String symptoms;
    private String status; // "Confirmed", "Pending", "Completed", "Cancelled"
    private String notes;
    private String createdAt;

    public Appointment(String id, String patientName, String patientPhone, String patientEmail,
                       int patientAge, String patientGender, String doctorId, String doctorName,
                       String specialty, String appointmentDate, String timeSlot, String type,
                       String symptoms, String status, String notes, String createdAt) {
        this.id = id;
        this.patientName = patientName;
        this.patientPhone = patientPhone;
        this.patientEmail = patientEmail;
        this.patientAge = patientAge;
        this.patientGender = patientGender;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.specialty = specialty;
        this.appointmentDate = appointmentDate;
        this.timeSlot = timeSlot;
        this.type = type;
        this.symptoms = symptoms;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getPatientName() { return patientName; }
    public String getPatientPhone() { return patientPhone; }
    public String getPatientEmail() { return patientEmail; }
    public int getPatientAge() { return patientAge; }
    public String getPatientGender() { return patientGender; }
    public String getDoctorId() { return doctorId; }
    public String getDoctorName() { return doctorName; }
    public String getSpecialty() { return specialty; }
    public String getAppointmentDate() { return appointmentDate; }
    public String getTimeSlot() { return timeSlot; }
    public String getType() { return type; }
    public String getSymptoms() { return symptoms; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }
    public String getCreatedAt() { return createdAt; }

    public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    public void setStatus(String status) { this.status = status; }
    public void setNotes(String notes) { this.notes = notes; }
}
