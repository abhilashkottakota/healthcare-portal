package com.healthcare.server;

import com.healthcare.api.AppointmentApiHandler;
import com.healthcare.api.AuthApiHandler;
import com.healthcare.api.DoctorApiHandler;
import com.healthcare.api.ServiceApiHandler;
import com.healthcare.api.StatsApiHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Embedded HTTP server routing REST API requests and web UI assets.
 */
public class HealthcareServer {
    private final int port;
    private HttpServer server;

    public HealthcareServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // REST API Contexts
        server.createContext("/api/auth", new AuthApiHandler());
        server.createContext("/api/doctors", new DoctorApiHandler());
        server.createContext("/api/services", new ServiceApiHandler());
        server.createContext("/api/appointments", new AppointmentApiHandler());
        server.createContext("/api/stats", new StatsApiHandler());

        // Web Assets Context
        File webDir = new File("web");
        server.createContext("/", new StaticFileHandler(webDir));

        server.setExecutor(Executors.newFixedThreadPool(12));
        server.start();

        System.out.println("==================================================================");
        System.out.println("  HEALTHCARE PORTAL & APPOINTMENT BOOKING SYSTEM - SERVER RUNNING ");
        System.out.println("==================================================================");
        System.out.println("  Local URL:        http://localhost:" + port);
        System.out.println("  Patient Portal:   http://localhost:" + port + "/index.html");
        System.out.println("  Appointments:     http://localhost:" + port + "/appointments.html");
        System.out.println("  Staff Admin:      http://localhost:" + port + "/admin.html");
        System.out.println("  REST Endpoints:   http://localhost:" + port + "/api/doctors");
        System.out.println("==================================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    public int getPort() {
        return port;
    }
}
