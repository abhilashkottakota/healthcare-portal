package com.healthcare.api;

import com.healthcare.model.Appointment;
import com.healthcare.repository.DataStore;
import com.healthcare.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Handles HTTP requests for /api/stats
 */
public class StatsApiHandler implements HttpHandler {
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

        List<Appointment> list = dataStore.getAllAppointments();
        int total = list.size();
        int upcoming = 0;
        int completed = 0;
        int cancelled = 0;

        for (Appointment a : list) {
            String status = a.getStatus().toLowerCase();
            if ("confirmed".equals(status) || "pending".equals(status)) {
                upcoming++;
            } else if ("completed".equals(status)) {
                completed++;
            } else if ("cancelled".equals(status)) {
                cancelled++;
            }
        }

        int doctors = dataStore.getAllDoctors().size();
        int services = dataStore.getAllServices().size();

        String statsJson = JsonUtil.statsToJson(total, upcoming, completed, cancelled, doctors, services);
        sendResponse(exchange, 200, statsJson);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseJson) throws IOException {
        byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
