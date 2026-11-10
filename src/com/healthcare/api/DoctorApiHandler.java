package com.healthcare.api;

import com.healthcare.model.Doctor;
import com.healthcare.repository.DataStore;
import com.healthcare.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles HTTP requests for /api/doctors
 */
public class DoctorApiHandler implements HttpHandler {
    private final DataStore dataStore = DataStore.getInstance();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, JsonUtil.errorJson("Method Not Allowed"));
            return;
        }

        URI requestUri = exchange.getRequestURI();
        String path = requestUri.getPath();
        Map<String, String> queryParams = parseQueryParams(requestUri.getQuery());

        // Check if requesting single doctor: /api/doctors/{id}
        String[] parts = path.split("/");
        if (parts.length > 3 && !parts[3].isEmpty()) {
            String doctorId = parts[3];
            Optional<Doctor> doctorOpt = dataStore.getDoctorById(doctorId);
            if (doctorOpt.isPresent()) {
                sendResponse(exchange, 200, JsonUtil.doctorToJson(doctorOpt.get()));
            } else {
                sendResponse(exchange, 404, JsonUtil.errorJson("Doctor not found with ID: " + doctorId));
            }
            return;
        }

        // Filter doctors
        List<Doctor> doctors = dataStore.getAllDoctors();

        String search = queryParams.get("search");
        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();
            doctors = doctors.stream().filter(d ->
                    d.getName().toLowerCase().contains(q) ||
                    d.getSpecialty().toLowerCase().contains(q) ||
                    d.getHospital().toLowerCase().contains(q) ||
                    d.getBio().toLowerCase().contains(q)
            ).collect(Collectors.toList());
        }

        String specialty = queryParams.get("specialty");
        if (specialty != null && !specialty.trim().isEmpty() && !"All".equalsIgnoreCase(specialty)) {
            String s = specialty.trim().toLowerCase();
            doctors = doctors.stream().filter(d ->
                    d.getSpecialty().toLowerCase().equals(s)
            ).collect(Collectors.toList());
        }

        String maxFeeStr = queryParams.get("maxFee");
        if (maxFeeStr != null) {
            try {
                double maxFee = Double.parseDouble(maxFeeStr);
                doctors = doctors.stream().filter(d -> d.getConsultationFee() <= maxFee).collect(Collectors.toList());
            } catch (NumberFormatException ignored) {}
        }

        String minRatingStr = queryParams.get("minRating");
        if (minRatingStr != null) {
            try {
                double minRating = Double.parseDouble(minRatingStr);
                doctors = doctors.stream().filter(d -> d.getRating() >= minRating).collect(Collectors.toList());
            } catch (NumberFormatException ignored) {}
        }

        sendResponse(exchange, 200, JsonUtil.doctorListToJson(doctors));
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
