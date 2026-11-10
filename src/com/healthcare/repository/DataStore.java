package com.healthcare.repository;

import com.healthcare.model.Appointment;
import com.healthcare.model.Doctor;
import com.healthcare.model.HealthcareService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe DataStore for Chennai Doctors, Hospitals, Services, and Appointments.
 */
public class DataStore {
    private static DataStore instance;

    private final List<Doctor> doctors = new ArrayList<>();
    private final List<HealthcareService> services = new ArrayList<>();
    private final Map<String, Appointment> appointments = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1005);
    private final File dataFile = new File("data/appointments.json");

    private DataStore() {
        seedDoctors();
        seedServices();
        seedInitialAppointments();
        loadAppointmentsFromFile();
    }

    public static synchronized DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    private void seedDoctors() {
        doctors.add(new Doctor(
                "DOC-01",
                "Dr. K. Karthikeyan",
                "Cardiology",
                "MBBS, MD, DM (Cardiology) - Madras Medical College",
                16,
                4.95,
                248,
                900.0,
                "Apollo Hospitals, Greams Road, Chennai",
                "Available Today",
                Arrays.asList("09:00 AM", "10:30 AM", "02:00 PM", "04:30 PM"),
                "Senior Interventional Cardiologist. Specialist in coronary stenting, hypertension, and preventive cardiac wellness.",
                "https://images.unsplash.com/photo-1622253692010-333f2da6031d?auto=format&fit=crop&q=80&w=400"
        ));

        doctors.add(new Doctor(
                "DOC-02",
                "Dr. S. Balasubramanian",
                "Neurology",
                "MBBS, DM (Neurology) - Stanley Medical College, Chennai",
                18,
                4.88,
                195,
                1100.0,
                "SIMS Hospital, Vadapalani, Chennai",
                "Mon - Fri",
                Arrays.asList("09:30 AM", "11:00 AM", "03:00 PM", "05:00 PM"),
                "Renowned Chennai Neurologist specializing in migraine treatment, stroke prevention, and peripheral neuropathies.",
                "https://images.unsplash.com/photo-1537368910025-700350fe46c7?auto=format&fit=crop&q=80&w=400"
        ));

        doctors.add(new Doctor(
                "DOC-03",
                "Dr. Priya Sundaram",
                "Pediatrics",
                "MBBS, DNB (Pediatrics) - Institute of Child Health, Egmore",
                12,
                4.96,
                280,
                750.0,
                "Kauvery Hospital, Alwarpet, Chennai",
                "Available Today",
                Arrays.asList("08:30 AM", "10:00 AM", "01:30 PM", "03:30 PM", "05:00 PM"),
                "Compassionate child specialist with deep expertise in neonatal wellness, pediatric immunizations, and asthma care.",
                "https://images.unsplash.com/photo-1559839734-2b71ea197ec2?auto=format&fit=crop&q=80&w=400"
        ));

        doctors.add(new Doctor(
                "DOC-04",
                "Dr. Anand Natarajan",
                "Orthopedics",
                "MBBS, MS (Ortho), MCh - Sri Ramachandra Institute, Porur",
                19,
                4.9,
                175,
                1000.0,
                "MIOT International, Manapakkam, Chennai",
                "Tue, Thu, Sat",
                Arrays.asList("10:00 AM", "11:30 AM", "02:30 PM", "04:00 PM"),
                "Leading joint replacement and sports injury surgeon with 1,500+ successful robotic knee & hip arthroplasties.",
                "https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?auto=format&fit=crop&q=80&w=400"
        ));

        doctors.add(new Doctor(
                "DOC-05",
                "Dr. Meera Krishnan",
                "Dermatology",
                "MBBS, MD (DVL) - Madras Medical College, Chennai",
                10,
                4.92,
                220,
                800.0,
                "Gleneagles Global Health City, Perumbakkam, Chennai",
                "Available Today",
                Arrays.asList("11:00 AM", "01:00 PM", "03:30 PM", "05:30 PM"),
                "Specialist in clinical dermatology, acne protocols, laser skin rejuvenation, and hair fall management.",
                "https://images.unsplash.com/photo-1651008376811-b90baee60c1f?auto=format&fit=crop&q=80&w=400"
        ));

        doctors.add(new Doctor(
                "DOC-06",
                "Dr. Arvind Ramanathan",
                "General Medicine",
                "MBBS, MD (Internal Medicine) - Kilpauk Medical College",
                14,
                4.82,
                260,
                600.0,
                "Fortis Malar Hospital, Adyar, Chennai",
                "Available Today",
                Arrays.asList("08:00 AM", "09:30 AM", "11:00 AM", "02:00 PM", "04:00 PM"),
                "Senior family physician & diabetologist specializing in lifestyle diseases, thyroid disorders, and chronic care.",
                "https://images.unsplash.com/photo-1594824813629-7925e0a6e033?auto=format&fit=crop&q=80&w=400"
        ));

        doctors.add(new Doctor(
                "DOC-07",
                "Dr. Shalini Mukundan",
                "Dentistry",
                "BDS, MDS (Orthodontics) - Tamil Nadu Dr. M.G.R. Medical University",
                11,
                4.89,
                145,
                500.0,
                "SmileCraft Multi-Specialty Dental Clinic, T. Nagar, Chennai",
                "Mon - Sat",
                Arrays.asList("09:00 AM", "11:00 AM", "02:00 PM", "04:00 PM"),
                "Expert in clear aligners, painless root canal therapy, ultrasonic teeth scaling, and cosmetic smile designs.",
                "https://images.unsplash.com/photo-1582750433449-648ed127bb54?auto=format&fit=crop&q=80&w=400"
        ));

        doctors.add(new Doctor(
                "DOC-08",
                "Dr. R. Jayanthi",
                "Oncology",
                "MBBS, MD, DM (Medical Oncology) - Cancer Institute (WIA), Adyar",
                22,
                4.98,
                110,
                1200.0,
                "Prashanth Super Speciality Hospital, Velachery, Chennai",
                "Wed, Fri",
                Arrays.asList("10:00 AM", "01:30 PM", "03:30 PM"),
                "Pioneer in medical oncology, immunotherapy, and comprehensive breast & gastrointestinal cancer treatments.",
                "https://images.unsplash.com/photo-1576091160550-2173dba999ef?auto=format&fit=crop&q=80&w=400"
        ));
    }

    private void seedServices() {
        services.add(new HealthcareService(
                "SRV-01",
                "Apollo Comprehensive Master Health Checkup",
                "Preventative",
                "Complete hemogram, fasting glucose, lipid profile, liver/kidney function tests, ECG, chest X-Ray, and physician review at Greams Road.",
                "120 mins",
                2499.0,
                "10-12 hours overnight fasting mandatory. Report to Greams Road Apollo diagnostic center by 7:30 AM.",
                "stethoscope"
        ));

        services.add(new HealthcareService(
                "SRV-02",
                "Kauvery Cardiac 2D Echo & Treadmill Stress Test",
                "Cardiology",
                "High-precision 2D Doppler echocardiogram and automated TMT to measure myocardial perfusion and cardiac reserve.",
                "45 mins",
                1800.0,
                "Wear comfortable running shoes. Avoid caffeine or heavy morning meal 3 hours prior.",
                "heart-pulse"
        ));

        services.add(new HealthcareService(
                "SRV-03",
                "Chennai Home Telehealth Video Consultation",
                "Telehealth",
                "Consult top Chennai medical specialists from home via secure HD video in Tamil or English with digital prescription.",
                "25 mins",
                499.0,
                "Smartphone or laptop with stable internet connection. Keep previous prescriptions handy.",
                "video"
        ));

        services.add(new HealthcareService(
                "SRV-04",
                "MIOT Bone Density & Joint Arthritis Screen",
                "Orthopedics",
                "DEXA scan for bone mineral density, serum calcium/Vitamin D3 testing, and clinical joint mobility evaluation at Manapakkam.",
                "40 mins",
                1500.0,
                "Wear loose clothing without metal zippers or buttons.",
                "bone"
        ));

        services.add(new HealthcareService(
                "SRV-05",
                "Gleneagles Pediatric Wellness & Vaccination",
                "Pediatrics",
                "Full developmental pediatric milestone evaluation, height/weight velocity check, and seasonal vaccination at Perumbakkam.",
                "35 mins",
                850.0,
                "Bring child's Tamil Nadu immunization vaccination booklet.",
                "baby"
        ));

        services.add(new HealthcareService(
                "SRV-06",
                "SmileCraft Ultrasonic Teeth Scaling & Polishing",
                "Dentistry",
                "Deep plaque & tartar removal with ultrasonic scaling technology and enamel stain polishing at T. Nagar clinic.",
                "40 mins",
                650.0,
                "Avoid intake of dark tea or coffee for 2 hours after procedure.",
                "smile"
        ));
    }

    private void seedInitialAppointments() {
        addAppointmentDirect(new Appointment(
                "APT-1001",
                "K. Senthil Nathan",
                "+91 98401 23456",
                "senthil.n@example.com",
                48,
                "Male",
                "DOC-01",
                "Dr. K. Karthikeyan",
                "Cardiology",
                "2026-09-25",
                "10:30 AM",
                "In-Person",
                "Mild chest tightness during morning walks along Marina Beach; blood pressure check.",
                "Confirmed",
                "Consultation scheduled at Apollo Hospitals, Greams Road. Patient requested Tamil-speaking assistance.",
                "2026-09-20 09:15"
        ));

        addAppointmentDirect(new Appointment(
                "APT-1002",
                "Deepa Raghavan",
                "+91 98842 34567",
                "deepa.r@example.com",
                31,
                "Female",
                "DOC-05",
                "Dr. Meera Krishnan",
                "Dermatology",
                "2026-09-26",
                "01:00 PM",
                "Virtual Teleconsultation",
                "Allergic skin flare-up and rash triggered by coastal humidity in Chennai.",
                "Confirmed",
                "HD Telehealth consultation link sent via SMS. Clinical photos received.",
                "2026-09-21 14:20"
        ));

        addAppointmentDirect(new Appointment(
                "APT-1003",
                "Master A. Vignesh",
                "+91 97910 88990",
                "vignesh.parents@example.com",
                6,
                "Male",
                "DOC-03",
                "Dr. Priya Sundaram",
                "Pediatrics",
                "2026-09-24",
                "08:30 AM",
                "In-Person",
                "School wellness checkup and annual seasonal flu vaccination booster.",
                "Confirmed",
                "Kauvery Hospital Alwarpet pediatric OPD. Immunization chart to be updated.",
                "2026-09-21 16:45"
        ));

        addAppointmentDirect(new Appointment(
                "APT-1004",
                "M. Saravanan",
                "+91 98412 90123",
                "saravanan.m@example.com",
                56,
                "Male",
                "DOC-04",
                "Dr. Anand Natarajan",
                "Orthopedics",
                "2026-09-18",
                "02:30 PM",
                "In-Person",
                "Post-knee arthroscopy mobility review and physiotherapy check.",
                "Completed",
                "Conducted at MIOT Manapakkam. Full knee flexion restored; advised Chennai beach walks with supportive footwear.",
                "2026-09-15 11:30"
        ));
    }

    public List<Doctor> getAllDoctors() {
        return new ArrayList<>(doctors);
    }

    public Optional<Doctor> getDoctorById(String id) {
        return doctors.stream().filter(d -> d.getId().equalsIgnoreCase(id)).findFirst();
    }

    public List<HealthcareService> getAllServices() {
        return new ArrayList<>(services);
    }

    public List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>(appointments.values());
        list.sort((a, b) -> b.getId().compareTo(a.getId()));
        return list;
    }

    public Optional<Appointment> getAppointmentById(String id) {
        return Optional.ofNullable(appointments.get(id));
    }

    public synchronized Appointment createAppointment(String patientName, String patientPhone,
                                                     String patientEmail, int patientAge, String patientGender,
                                                     String doctorId, String appointmentDate, String timeSlot,
                                                     String type, String symptoms, String notes) {
        Doctor doctor = getDoctorById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Doctor ID: " + doctorId));

        String newId = "APT-" + idCounter.incrementAndGet();
        String now = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(LocalDateTime.now());

        Appointment appt = new Appointment(
                newId,
                patientName,
                patientPhone,
                patientEmail,
                patientAge,
                patientGender,
                doctor.getId(),
                doctor.getName(),
                doctor.getSpecialty(),
                appointmentDate,
                timeSlot,
                type,
                symptoms,
                "Confirmed",
                notes != null ? notes : ("Booked for " + doctor.getHospital() + "."),
                now
        );

        appointments.put(newId, appt);
        saveAppointmentsToFile();
        return appt;
    }

    public synchronized boolean updateAppointment(String id, String date, String timeSlot, String status, String notes) {
        Appointment appt = appointments.get(id);
        if (appt == null) return false;

        if (date != null && !date.trim().isEmpty()) appt.setAppointmentDate(date.trim());
        if (timeSlot != null && !timeSlot.trim().isEmpty()) appt.setTimeSlot(timeSlot.trim());
        if (status != null && !status.trim().isEmpty()) appt.setStatus(status.trim());
        if (notes != null) appt.setNotes(notes.trim());

        saveAppointmentsToFile();
        return true;
    }

    public synchronized boolean cancelAppointment(String id, String reason) {
        Appointment appt = appointments.get(id);
        if (appt == null) return false;
        appt.setStatus("Cancelled");
        if (reason != null && !reason.trim().isEmpty()) {
            appt.setNotes(appt.getNotes() + " | Cancellation Reason: " + reason.trim());
        }
        saveAppointmentsToFile();
        return true;
    }

    private void addAppointmentDirect(Appointment appt) {
        appointments.put(appt.getId(), appt);
    }

    private synchronized void saveAppointmentsToFile() {
        try {
            if (!dataFile.getParentFile().exists()) {
                dataFile.getParentFile().mkdirs();
            }
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(dataFile), StandardCharsets.UTF_8)) {
                writer.write("[\n");
                List<Appointment> list = new ArrayList<>(appointments.values());
                for (int i = 0; i < list.size(); i++) {
                    Appointment a = list.get(i);
                    writer.write(String.format(
                            "  {\n" +
                            "    \"id\": \"%s\",\n" +
                            "    \"patientName\": \"%s\",\n" +
                            "    \"patientPhone\": \"%s\",\n" +
                            "    \"patientEmail\": \"%s\",\n" +
                            "    \"patientAge\": %d,\n" +
                            "    \"patientGender\": \"%s\",\n" +
                            "    \"doctorId\": \"%s\",\n" +
                            "    \"doctorName\": \"%s\",\n" +
                            "    \"specialty\": \"%s\",\n" +
                            "    \"appointmentDate\": \"%s\",\n" +
                            "    \"timeSlot\": \"%s\",\n" +
                            "    \"type\": \"%s\",\n" +
                            "    \"symptoms\": \"%s\",\n" +
                            "    \"status\": \"%s\",\n" +
                            "    \"notes\": \"%s\",\n" +
                            "    \"createdAt\": \"%s\"\n" +
                            "  }%s\n",
                            escapeJson(a.getId()),
                            escapeJson(a.getPatientName()),
                            escapeJson(a.getPatientPhone()),
                            escapeJson(a.getPatientEmail()),
                            a.getPatientAge(),
                            escapeJson(a.getPatientGender()),
                            escapeJson(a.getDoctorId()),
                            escapeJson(a.getDoctorName()),
                            escapeJson(a.getSpecialty()),
                            escapeJson(a.getAppointmentDate()),
                            escapeJson(a.getTimeSlot()),
                            escapeJson(a.getType()),
                            escapeJson(a.getSymptoms()),
                            escapeJson(a.getStatus()),
                            escapeJson(a.getNotes()),
                            escapeJson(a.getCreatedAt()),
                            (i < list.size() - 1 ? "," : "")
                    ));
                }
                writer.write("]\n");
            }
        } catch (IOException e) {
            System.err.println("Could not persist appointments: " + e.getMessage());
        }
    }

    private void loadAppointmentsFromFile() {
        if (!dataFile.exists()) {
            saveAppointmentsToFile();
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
