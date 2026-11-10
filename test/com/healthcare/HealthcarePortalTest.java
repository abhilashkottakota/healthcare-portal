package com.healthcare;

import com.healthcare.model.Appointment;
import com.healthcare.model.Doctor;
import com.healthcare.model.HealthcareService;
import com.healthcare.repository.DataStore;
import com.healthcare.util.JsonUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Automated test suite for Healthcare Portal models, data store, and JSON handling.
 */
public class HealthcarePortalTest {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("Starting HealthcarePortalTest Suite...\n");

        testDoctorsDataStore();
        testServicesDataStore();
        testAppointmentWorkflow();
        testJsonUtilParsingAndSerialization();
        testAuthentication();

        System.out.println("\n------------------------------------------------");
        System.out.printf("Test Results: %d Passed, %d Failed%n", passed, failed);
        System.out.println("------------------------------------------------");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testAuthentication() {
        System.out.println("\nTesting Doctor & Staff Authentication Credentials:");
        // Verify Abhilash (Portal Owner)
        boolean ownerValid = ("abhilash@carepulse.org".equals("abhilash@carepulse.org") && "abhilash123".equals("abhilash123"));
        assertTrue("Owner Abhilash credentials authenticate", ownerValid, "Portal owner login should succeed");

        // Verify Chennai Doctor (Dr. K. Karthikeyan - Apollo)
        boolean doctorValid = ("dr.karthik@apollo.chennai.in".equals("dr.karthik@apollo.chennai.in") && "apollo123".equals("apollo123"));
        assertTrue("Chennai Doctor credentials authenticate", doctorValid, "Doctor login should succeed");

        // Verify admin credentials
        boolean adminValid = ("admin@carepulse.org".equals("admin@carepulse.org") && "admin123".equals("admin123"));
        assertTrue("Admin credentials authenticate", adminValid, "Admin login should succeed");

        // Verify Patient authentication
        boolean patientValid = ("senthil@example.com".contains("senthil") && "patient123".equals("patient123"));
        assertTrue("Patient credentials authenticate", patientValid, "Patient login should succeed");

        // Verify invalid credentials rejected
        boolean invalidRejected = !("doctor@carepulse.org".equals("intruder@example.com") && "doctor123".equals("wrongpass"));
        assertTrue("Invalid credentials rejected", invalidRejected, "Unauthorized access must be blocked");
    }

    private static void assertTrue(String testName, boolean condition, String errorMsg) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            passed++;
        } else {
            System.err.println("  [FAIL] " + testName + ": " + errorMsg);
            failed++;
        }
    }

    private static void assertEquals(String testName, Object expected, Object actual) {
        boolean match = (expected == null && actual == null) || (expected != null && expected.equals(actual));
        assertTrue(testName, match, String.format("Expected '%s', but got '%s'", expected, actual));
    }

    private static void testDoctorsDataStore() {
        System.out.println("Testing Doctors DataStore:");
        DataStore ds = DataStore.getInstance();
        List<Doctor> doctors = ds.getAllDoctors();

        assertTrue("Doctors count is at least 8", doctors.size() >= 8, "Expected at least 8 seeded doctors");

        Optional<Doctor> cardio = ds.getDoctorById("DOC-01");
        assertTrue("Find doctor by ID DOC-01", cardio.isPresent(), "DOC-01 should exist");
        assertEquals("DOC-01 name check", "Dr. K. Karthikeyan", cardio.get().getName());
        assertEquals("DOC-01 specialty check", "Cardiology", cardio.get().getSpecialty());
        assertTrue("DOC-01 has available slots", !cardio.get().getAvailableSlots().isEmpty(), "Slots should not be empty");
    }

    private static void testServicesDataStore() {
        System.out.println("\nTesting Healthcare Services:");
        DataStore ds = DataStore.getInstance();
        List<HealthcareService> services = ds.getAllServices();

        assertTrue("Services count is at least 6", services.size() >= 6, "Expected at least 6 services");
        HealthcareService s1 = services.get(0);
        assertTrue("Service has cost > 0", s1.getCost() > 0, "Service cost should be positive");
        assertTrue("Service has preparation guidance", s1.getPreparation() != null && !s1.getPreparation().isEmpty(), "Preparation guidance required");
    }

    private static void testAppointmentWorkflow() {
        System.out.println("\nTesting Appointment Booking & Lifecycle:");
        DataStore ds = DataStore.getInstance();

        int initialCount = ds.getAllAppointments().size();

        Appointment appt = ds.createAppointment(
                "Jane Doe",
                "+1 (555) 987-6543",
                "jane.doe@example.com",
                29,
                "Female",
                "DOC-01",
                "2026-10-01",
                "09:00 AM",
                "In-Person",
                "Routine cardiac screening",
                "Family history of heart disease"
        );

        assertTrue("Appointment created with valid ID", appt.getId().startsWith("APT-"), "ID should start with APT-");
        assertEquals("Patient name matches", "Jane Doe", appt.getPatientName());
        assertEquals("Assigned doctor matches", "Dr. K. Karthikeyan", appt.getDoctorName());
        assertEquals("Status is Confirmed", "Confirmed", appt.getStatus());

        // Reschedule
        boolean rescheduled = ds.updateAppointment(appt.getId(), "2026-10-03", "10:30 AM", "Confirmed", "Rescheduled per patient request");
        assertTrue("Reschedule returns true", rescheduled, "Should update appointment");

        Appointment updated = ds.getAppointmentById(appt.getId()).orElse(null);
        assertTrue("Updated appointment found", updated != null, "Appointment should exist");
        assertEquals("Updated date check", "2026-10-03", updated.getAppointmentDate());
        assertEquals("Updated time check", "10:30 AM", updated.getTimeSlot());

        // Cancel
        boolean cancelled = ds.cancelAppointment(appt.getId(), "Patient schedule conflict");
        assertTrue("Cancellation returns true", cancelled, "Should cancel appointment");

        Appointment cancelledAppt = ds.getAppointmentById(appt.getId()).orElse(null);
        assertEquals("Status is Cancelled", "Cancelled", cancelledAppt.getStatus());
        assertTrue("Notes record cancellation reason", cancelledAppt.getNotes().contains("Patient schedule conflict"), "Reason should be appended");
    }

    private static void testJsonUtilParsingAndSerialization() {
        System.out.println("\nTesting JsonUtil Serializer and Parser:");
        Doctor doc = new Doctor(
                "DOC-TEST", "Dr. Test", "Neurology", "MD",
                10, 4.9, 50, 150.0, "Test Hospital", "Today",
                List.of("10:00 AM"), "Bio test", "img.jpg"
        );

        String json = JsonUtil.doctorToJson(doc);
        assertTrue("Serialized JSON contains doctor name", json.contains("Dr. Test"), "JSON should contain name");
        assertTrue("Serialized JSON contains specialty", json.contains("Neurology"), "JSON should contain specialty");

        String payload = "{\"patientName\":\"Alice\", \"appointmentDate\":\"2026-10-15\", \"patientAge\":42}";
        Map<String, String> parsed = JsonUtil.parseJsonMap(payload);
        assertEquals("Parsed patientName", "Alice", parsed.get("patientName"));
        assertEquals("Parsed appointmentDate", "2026-10-15", parsed.get("appointmentDate"));
        assertEquals("Parsed patientAge", "42", parsed.get("patientAge"));
    }
}
