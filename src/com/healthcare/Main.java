package com.healthcare;

import com.healthcare.server.HealthcareServer;

import java.awt.Desktop;
import java.net.URI;

/**
 * Main application entrypoint for Healthcare Portal.
 */
public class Main {
    public static void main(String[] args) {
        int port = 8080;
        boolean openBrowser = true;

        for (String arg : args) {
            if (arg.startsWith("--port=")) {
                try {
                    port = Integer.parseInt(arg.substring(7));
                } catch (NumberFormatException ignored) {}
            } else if ("--no-browser".equalsIgnoreCase(arg)) {
                openBrowser = false;
            }
        }

        try {
            HealthcareServer server = new HealthcareServer(port);
            server.start();

            if (openBrowser) {
                try {
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI("http://localhost:" + port));
                    }
                } catch (Exception e) {
                    System.out.println("Tip: Open your browser and navigate to http://localhost:" + port);
                }
            }

            // Keep main thread alive
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("Fatal error starting Healthcare Portal: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
