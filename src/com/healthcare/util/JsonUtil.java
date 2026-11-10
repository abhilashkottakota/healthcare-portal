package com.healthcare.util;

import com.healthcare.model.Appointment;
import com.healthcare.model.Doctor;
import com.healthcare.model.HealthcareService;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight JSON utility for serialization and parsing without external dependencies.
 */
public class JsonUtil {

    public static String doctorToJson(Doctor d) {
        StringBuilder slotsJson = new StringBuilder("[");
        List<String> slots = d.getAvailableSlots();
        for (int i = 0; i < slots.size(); i++) {
            slotsJson.append("\"").append(escape(slots.get(i))).append("\"");
            if (i < slots.size() - 1) slotsJson.append(",");
        }
        slotsJson.append("]");

        return String.format(
                "{\"id\":\"%s\",\"name\":\"%s\",\"specialty\":\"%s\",\"qualifications\":\"%s\"," +
                "\"experienceYears\":%d,\"rating\":%.2f,\"reviewsCount\":%d,\"consultationFee\":%.2f," +
                "\"hospital\":\"%s\",\"availability\":\"%s\",\"availableSlots\":%s,\"bio\":\"%s\",\"imageUrl\":\"%s\"}",
                escape(d.getId()), escape(d.getName()), escape(d.getSpecialty()), escape(d.getQualifications()),
                d.getExperienceYears(), d.getRating(), d.getReviewsCount(), d.getConsultationFee(),
                escape(d.getHospital()), escape(d.getAvailability()), slotsJson.toString(),
                escape(d.getBio()), escape(d.getImageUrl())
        );
    }

    public static String doctorListToJson(List<Doctor> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(doctorToJson(list.get(i)));
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String serviceToJson(HealthcareService s) {
        return String.format(
                "{\"id\":\"%s\",\"name\":\"%s\",\"category\":\"%s\",\"description\":\"%s\"," +
                "\"duration\":\"%s\",\"cost\":%.2f,\"preparation\":\"%s\",\"icon\":\"%s\"}",
                escape(s.getId()), escape(s.getName()), escape(s.getCategory()), escape(s.getDescription()),
                escape(s.getDuration()), s.getCost(), escape(s.getPreparation()), escape(s.getIcon())
        );
    }

    public static String serviceListToJson(List<HealthcareService> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(serviceToJson(list.get(i)));
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String appointmentToJson(Appointment a) {
        return String.format(
                "{\"id\":\"%s\",\"patientName\":\"%s\",\"patientPhone\":\"%s\",\"patientEmail\":\"%s\"," +
                "\"patientAge\":%d,\"patientGender\":\"%s\",\"doctorId\":\"%s\",\"doctorName\":\"%s\"," +
                "\"specialty\":\"%s\",\"appointmentDate\":\"%s\",\"timeSlot\":\"%s\",\"type\":\"%s\"," +
                "\"symptoms\":\"%s\",\"status\":\"%s\",\"notes\":\"%s\",\"createdAt\":\"%s\"}",
                escape(a.getId()), escape(a.getPatientName()), escape(a.getPatientPhone()), escape(a.getPatientEmail()),
                a.getPatientAge(), escape(a.getPatientGender()), escape(a.getDoctorId()), escape(a.getDoctorName()),
                escape(a.getSpecialty()), escape(a.getAppointmentDate()), escape(a.getTimeSlot()), escape(a.getType()),
                escape(a.getSymptoms()), escape(a.getStatus()), escape(a.getNotes()), escape(a.getCreatedAt())
        );
    }

    public static String appointmentListToJson(List<Appointment> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(appointmentToJson(list.get(i)));
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String statsToJson(int totalAppointments, int upcomingCount, int completedCount,
                                     int cancelledCount, int totalDoctors, int totalServices) {
        return String.format(
                "{\"totalAppointments\":%d,\"upcomingCount\":%d,\"completedCount\":%d," +
                "\"cancelledCount\":%d,\"totalDoctors\":%d,\"totalServices\":%d}",
                totalAppointments, upcomingCount, completedCount, cancelledCount, totalDoctors, totalServices
        );
    }

    public static String errorJson(String message) {
        return "{\"success\":false,\"error\":\"" + escape(message) + "\"}";
    }

    public static String successJson(String message, String extraJson) {
        if (extraJson != null && !extraJson.trim().isEmpty()) {
            return "{\"success\":true,\"message\":\"" + escape(message) + "\",\"data\":" + extraJson + "}";
        }
        return "{\"success\":true,\"message\":\"" + escape(message) + "\"}";
    }

    /**
     * Parses a simple flat JSON object into a Key-Value Map.
     */
    public static Map<String, String> parseJsonMap(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null) return map;

        Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(?:\"([^\"]*)\"|([0-9.]+)|(true|false|null))");
        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            String key = matcher.group(1);
            String valStr = matcher.group(2);
            String valNum = matcher.group(3);
            String valBool = matcher.group(4);

            if (valStr != null) {
                map.put(key, unescape(valStr));
            } else if (valNum != null) {
                map.put(key, valNum);
            } else if (valBool != null) {
                map.put(key, valBool);
            }
        }
        return map;
    }

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static String unescape(String s) {
        if (s == null) return "";
        return s.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }
}
