package com.healthcare.api;

import com.healthcare.model.Appointment;
import com.healthcare.repository.DataStore;
import com.healthcare.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles HTTP requests for /api/appointments (CRUD, rescheduling, status, notes)
 */
public class AppointmentApiHandler implements HttpHandler {
    private final DataStore dataStore = DataStore.getInstance();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        String method = exchange.getRequestMethod().toUpperCase();

        if ("OPTIONS".equals(method)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        URI uri = exchange.getRequestURI();
        String path = uri.getPath();
        String[] parts = path.split("/");
        String subId = (parts.length > 3 && !parts[3].isEmpty()) ? parts[3] : null;

        switch (method) {
            case "GET":
                handleGet(exchange, subId, uri.getQuery());
                break;
            case "POST":
                handlePost(exchange);
                break;
            case "PUT":
                handlePut(exchange, subId);
                break;
            case "DELETE":
                handleDelete(exchange, subId, uri.getQuery());
                break;
            default:
                sendResponse(exchange, 405, JsonUtil.errorJson("Method Not Allowed"));
        }
    }

    private void handleGet(HttpExchange exchange, String id, String query) throws IOException {
        if (id != null) {
            Optional<Appointment> opt = dataStore.getAppointmentById(id);
            if (opt.isPresent()) {
                sendResponse(exchange, 200, JsonUtil.appointmentToJson(opt.get()));
            } else {
                sendResponse(exchange, 404, JsonUtil.errorJson("Appointment not found: " + id));
            }
            return;
        }

        List<Appointment> list = dataStore.getAllAppointments();
        Map<String, String> queryParams = parseQueryParams(query);

        String status = queryParams.get("status");
        if (status != null && !status.trim().isEmpty() && !"All".equalsIgnoreCase(status)) {
            list = list.stream()
                    .filter(a -> a.getStatus().equalsIgnoreCase(status.trim()))
                    .collect(Collectors.toList());
        }

        String search = queryParams.get("search");
        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();
            list = list.stream().filter(a ->
                    a.getPatientName().toLowerCase().contains(q) ||
                    a.getDoctorName().toLowerCase().contains(q) ||
                    a.getId().toLowerCase().contains(q) ||
                    a.getPatientPhone().toLowerCase().contains(q) ||
                    a.getSpecialty().toLowerCase().contains(q)
            ).collect(Collectors.toList());
        }

        sendResponse(exchange, 200, JsonUtil.appointmentListToJson(list));
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Map<String, String> map = JsonUtil.parseJsonMap(body);

        String patientName = map.get("patientName");
        String patientPhone = map.get("patientPhone");
        String patientEmail = map.get("patientEmail");
        String patientAgeStr = map.get("patientAge");
        String patientGender = map.get("patientGender");
        String doctorId = map.get("doctorId");
        String appointmentDate = map.get("appointmentDate");
        String timeSlot = map.get("timeSlot");
        String type = map.get("type");
        String symptoms = map.get("symptoms");
        String notes = map.get("notes");

        if (patientName == null || patientName.trim().isEmpty() ||
            patientPhone == null || patientPhone.trim().isEmpty() ||
            doctorId == null || doctorId.trim().isEmpty() ||
            appointmentDate == null || appointmentDate.trim().isEmpty() ||
            timeSlot == null || timeSlot.trim().isEmpty()) {
            sendResponse(exchange, 400, JsonUtil.errorJson("Missing required fields (patientName, patientPhone, doctorId, appointmentDate, timeSlot)."));
            return;
        }

        int age = 30;
        if (patientAgeStr != null) {
            try {
                age = Integer.parseInt(patientAgeStr);
            } catch (NumberFormatException ignored) {}
        }

        try {
            Appointment appt = dataStore.createAppointment(
                    patientName.trim(),
                    patientPhone.trim(),
                    patientEmail != null ? patientEmail.trim() : "N/A",
                    age,
                    patientGender != null ? patientGender.trim() : "Other",
                    doctorId.trim(),
                    appointmentDate.trim(),
                    timeSlot.trim(),
                    type != null ? type.trim() : "In-Person",
                    symptoms != null ? symptoms.trim() : "General Consultation",
                    notes
            );
            sendResponse(exchange, 201, JsonUtil.successJson("Appointment booked successfully!", JsonUtil.appointmentToJson(appt)));
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, JsonUtil.errorJson(e.getMessage()));
        }
    }

    private void handlePut(HttpExchange exchange, String id) throws IOException {
        if (id == null) {
            sendResponse(exchange, 400, JsonUtil.errorJson("Appointment ID is required in URL path."));
            return;
        }

        String body = readRequestBody(exchange);
        Map<String, String> map = JsonUtil.parseJsonMap(body);

        String date = map.get("appointmentDate");
        String timeSlot = map.get("timeSlot");
        String status = map.get("status");
        String notes = map.get("notes");

        boolean updated = dataStore.updateAppointment(id, date, timeSlot, status, notes);
        if (updated) {
            Optional<Appointment> appt = dataStore.getAppointmentById(id);
            sendResponse(exchange, 200, JsonUtil.successJson("Appointment updated successfully.", appt.map(JsonUtil::appointmentToJson).orElse(null)));
        } else {
            sendResponse(exchange, 404, JsonUtil.errorJson("Appointment not found: " + id));
        }
    }

    private void handleDelete(HttpExchange exchange, String id, String query) throws IOException {
        if (id == null) {
            sendResponse(exchange, 400, JsonUtil.errorJson("Appointment ID is required in URL path."));
            return;
        }

        Map<String, String> queryParams = parseQueryParams(query);
        String reason = queryParams.get("reason");

        boolean cancelled = dataStore.cancelAppointment(id, reason);
        if (cancelled) {
            sendResponse(exchange, 200, JsonUtil.successJson("Appointment cancelled successfully.", null));
        } else {
            sendResponse(exchange, 404, JsonUtil.errorJson("Appointment not found: " + id));
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseJson) throws IOException {
        byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1) {
                params.put(pair[0], java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
            } else if (pair.length == 1) {
                params.put(pair[0], "");
            }
        }
        return params;
    }
}
