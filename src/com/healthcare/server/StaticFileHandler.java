package com.healthcare.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;

/**
 * Serves static web assets (HTML, CSS, JS, Images) from the web/ folder.
 */
public class StaticFileHandler implements HttpHandler {
    private final File webDir;

    public StaticFileHandler(File webDir) {
        this.webDir = webDir;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod()) && !"HEAD".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        URI uri = exchange.getRequestURI();
        String path = uri.getPath();

        if ("/".equals(path) || path.isEmpty()) {
            path = "/index.html";
        } else if ("/patient".equals(path)) {
            path = "/patient.html";
        } else if ("/appointments".equals(path)) {
            path = "/appointments.html";
        } else if ("/admin".equals(path)) {
            path = "/admin.html";
        }

        File targetFile = new File(webDir, path.replace('/', File.separatorChar));

        // Prevent directory traversal
        if (!targetFile.getCanonicalPath().startsWith(webDir.getCanonicalPath()) || !targetFile.exists() || targetFile.isDirectory()) {
            byte[] notFound = "<h1>404 Not Found</h1>".getBytes();
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(404, notFound.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(notFound);
            }
            return;
        }

        String mimeType = getMimeType(targetFile.getName());
        exchange.getResponseHeaders().set("Content-Type", mimeType);
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");

        long length = targetFile.length();
        exchange.sendResponseHeaders(200, length);

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            try (FileInputStream fis = new FileInputStream(targetFile); OutputStream os = exchange.getResponseBody()) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
            }
        }
    }

    private String getMimeType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".html")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }
}
