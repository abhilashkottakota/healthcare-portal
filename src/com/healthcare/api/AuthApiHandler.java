package com.healthcare.api;

import com.healthcare.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles Authentication, persistent Registration, and Login verification for Patients and Doctors.
 */
public class AuthApiHandler implements HttpHandler {

    public static class UserAccount {
        public String email;
        public String password;
        public String name;
        public String phone;
        public String role; // "Patient" or "Doctor" or "Admin"
        public String hospital;
        public String specialty;

        public UserAccount(String email, String password, String name, String phone, String role, String hospital, String specialty) {
            this.email = email.toLowerCase().trim();
            this.password = password.trim();
            this.name = name;
            this.phone = phone != null ? phone : "";
            this.role = role;
            this.hospital = hospital != null ? hospital : "";
            this.specialty = specialty != null ? specialty : "";
        }
    }

    private final Map<String, UserAccount> users = new ConcurrentHashMap<>();
    private final File usersFile = new File("data/users.json");

    public AuthApiHandler() {
        seedDefaultAccounts();
        loadUsersFromFile();
    }

    private void seedDefaultAccounts() {
        // Portal Owner & Director
        users.put("abhilash@carepulse.org", new UserAccount("abhilash@carepulse.org", "abhilash123", "Abhilash (Portal Owner & Director)", "+91 98400 00001", "Admin", "CarePulse Chennai Central Network", "Hospital Administration"));
        users.put("abhilash", new UserAccount("abhilash@carepulse.org", "abhilash123", "Abhilash (Portal Owner & Director)", "+91 98400 00001", "Admin", "CarePulse Chennai Central Network", "Hospital Administration"));

        // Chennai Doctors
        users.put("dr.karthik@apollo.chennai.in", new UserAccount("dr.karthik@apollo.chennai.in", "apollo123", "Dr. K. Karthikeyan", "+91 98401 11223", "Doctor", "Apollo Hospitals, Greams Road, Chennai", "Cardiology"));
        users.put("dr.priya@kauvery.chennai.in", new UserAccount("dr.priya@kauvery.chennai.in", "kauvery123", "Dr. Priya Sundaram", "+91 98402 33445", "Doctor", "Kauvery Hospital, Alwarpet, Chennai", "Pediatrics"));
        users.put("dr.anand@miot.chennai.in", new UserAccount("dr.anand@miot.chennai.in", "miot123", "Dr. Anand Natarajan", "+91 98403 55667", "Doctor", "MIOT International, Manapakkam, Chennai", "Orthopedics"));
        users.put("doctor@carepulse.org", new UserAccount("doctor@carepulse.org", "doctor123", "Dr. Meera Krishnan", "+91 98404 77889", "Doctor", "Gleneagles Global Health City, Chennai", "Dermatology"));

        // Admin
        users.put("admin@carepulse.org", new UserAccount("admin@carepulse.org", "admin123", "Chennai Central Hospital Operations", "+91 44 2829 0200", "Admin", "All Partner Facilities", "Operations"));

        // Patients
        users.put("senthil@example.com", new UserAccount("senthil@example.com", "patient123", "K. Senthil Nathan", "+91 98401 23456", "Patient", "", ""));
        users.put("deepa.r@example.com", new UserAccount("deepa.r@example.com", "patient123", "Deepa Raghavan", "+91 98842 34567", "Patient", "", ""));
        users.put("patient@carepulse.org", new UserAccount("patient@carepulse.org", "patient123", "Patient Member (Chennai)", "+91 97910 88990", "Patient", "", ""));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, JsonUtil.errorJson("Method Not Allowed"));
            return;
        }

        String path = exchange.getRequestURI().getPath();

        if (path.endsWith("/login")) {
            handleLogin(exchange);
        } else if (path.endsWith("/register")) {
            handleRegister(exchange);
        } else if (path.endsWith("/logout")) {
            sendResponse(exchange, 200, JsonUtil.successJson("Logged out successfully", null));
        } else {
            sendResponse(exchange, 404, JsonUtil.errorJson("Auth endpoint not found"));
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Map<String, String> map = JsonUtil.parseJsonMap(body);

        String email = map.get("email");
        if (email == null) email = map.get("username");
        String password = map.get("password");
        String requestedRole = map.get("roleType"); // "patient" or "doctor"

        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            sendResponse(exchange, 400, "{\"success\":false,\"error\":\"Email and password are required.\"}");
            return;
        }

        email = email.trim().toLowerCase();
        password = password.trim();

        UserAccount user = users.get(email);

        // Check if account does NOT exist -> tell client to ask for sign up
        if (user == null) {
            String roleName = "patient".equalsIgnoreCase(requestedRole) ? "Patient" : "Doctor / Staff";
            String msg = "No " + roleName + " account found with email '" + email + "'. Please sign up below to create a new account.";
            sendResponse(exchange, 404, String.format(
                    "{\"success\":false,\"notRegistered\":true,\"error\":\"%s\",\"email\":\"%s\"}",
                    JsonUtil.escape(msg), JsonUtil.escape(email)
            ));
            return;
        }

        // Verify password
        if (!user.password.equals(password)) {
            sendResponse(exchange, 401, "{\"success\":false,\"notRegistered\":false,\"error\":\"Incorrect password. Please verify your password or reset it.\"}");
            return;
        }

        // Generate session token & response
        String token = "TOKEN-" + UUID.randomUUID().toString();
        String redirect = "Patient".equalsIgnoreCase(user.role) ? "patient.html" : "admin.html";

        String userJson = String.format(
                "{\"token\":\"%s\",\"email\":\"%s\",\"name\":\"%s\",\"phone\":\"%s\",\"role\":\"%s\",\"hospital\":\"%s\",\"specialty\":\"%s\",\"redirectUrl\":\"%s\"}",
                token, JsonUtil.escape(user.email), JsonUtil.escape(user.name), JsonUtil.escape(user.phone),
                user.role, JsonUtil.escape(user.hospital), JsonUtil.escape(user.specialty), redirect
        );

        sendResponse(exchange, 200, JsonUtil.successJson("Login successful", userJson));
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Map<String, String> map = JsonUtil.parseJsonMap(body);

        String email = map.get("email");
        String password = map.get("password");
        String name = map.get("name");
        String phone = map.get("phone");
        String roleType = map.get("roleType"); // "patient" or "doctor"
        String hospital = map.get("hospital");
        String specialty = map.get("specialty");

        if (email == null || password == null || name == null ||
            email.trim().isEmpty() || password.trim().isEmpty() || name.trim().isEmpty()) {
            sendResponse(exchange, 400, "{\"success\":false,\"error\":\"Full Name, Email, and Password are required to sign up.\"}");
            return;
        }

        email = email.trim().toLowerCase();
        password = password.trim();
        name = name.trim();

        // Check if account already exists
        if (users.containsKey(email)) {
            sendResponse(exchange, 409, String.format(
                    "{\"success\":false,\"alreadyRegistered\":true,\"error\":\"An account with email '%s' already exists. Please sign in instead.\"}",
                    JsonUtil.escape(email)
            ));
            return;
        }

        // Determine Role
        String role = "doctor".equalsIgnoreCase(roleType) ? "Doctor" : "Patient";
        if (hospital == null || hospital.trim().isEmpty()) {
            hospital = "doctor".equalsIgnoreCase(roleType) ? "Apollo Hospitals, Chennai" : "";
        }
        if (specialty == null || specialty.trim().isEmpty()) {
            specialty = "doctor".equalsIgnoreCase(roleType) ? "General Medicine" : "";
        }

        UserAccount newAccount = new UserAccount(email, password, name, phone, role, hospital, specialty);
        users.put(email, newAccount);
        saveUsersToFile();

        String token = "TOKEN-" + UUID.randomUUID().toString();
        String redirect = "Patient".equalsIgnoreCase(role) ? "patient.html" : "admin.html";

        String userJson = String.format(
                "{\"token\":\"%s\",\"email\":\"%s\",\"name\":\"%s\",\"phone\":\"%s\",\"role\":\"%s\",\"hospital\":\"%s\",\"specialty\":\"%s\",\"redirectUrl\":\"%s\"}",
                token, JsonUtil.escape(newAccount.email), JsonUtil.escape(newAccount.name), JsonUtil.escape(newAccount.phone),
                newAccount.role, JsonUtil.escape(newAccount.hospital), JsonUtil.escape(newAccount.specialty), redirect
        );

        sendResponse(exchange, 201, JsonUtil.successJson("Account registered successfully!", userJson));
    }

    private synchronized void saveUsersToFile() {
        try {
            if (!usersFile.getParentFile().exists()) {
                usersFile.getParentFile().mkdirs();
            }
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(usersFile), StandardCharsets.UTF_8)) {
                writer.write("[\n");
                List<UserAccount> list = new ArrayList<>(users.values());
                for (int i = 0; i < list.size(); i++) {
                    UserAccount u = list.get(i);
                    writer.write(String.format(
                            "  {\n" +
                            "    \"email\": \"%s\",\n" +
                            "    \"password\": \"%s\",\n" +
                            "    \"name\": \"%s\",\n" +
                            "    \"phone\": \"%s\",\n" +
                            "    \"role\": \"%s\",\n" +
                            "    \"hospital\": \"%s\",\n" +
                            "    \"specialty\": \"%s\"\n" +
                            "  }%s\n",
                            JsonUtil.escape(u.email), JsonUtil.escape(u.password), JsonUtil.escape(u.name),
                            JsonUtil.escape(u.phone), u.role, JsonUtil.escape(u.hospital), JsonUtil.escape(u.specialty),
                            (i < list.size() - 1 ? "," : "")
                    ));
                }
                writer.write("]\n");
            }
        } catch (IOException e) {
            System.err.println("Could not save users to file: " + e.getMessage());
        }
    }

    private void loadUsersFromFile() {
        if (!usersFile.exists()) {
            saveUsersToFile();
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
}
