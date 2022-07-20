package nl.holdyourcode.issues.gradle.XXX.repository;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

public class RepositoryServer {

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(args[0]);
        String urlArtifact = args[1];
        Path archivePath = Path.of(args[2]);

        byte[] artifact = Files.readAllBytes(archivePath);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext(urlArtifact, exchange -> handleArtifact(exchange, archivePath.getFileName().toString(), artifact));
        server.setExecutor(null);
        server.start();

        System.out.println("Repository server started on port "+ port);
    }

    public static void handleArtifact(HttpExchange exchange, String artifactPath, byte[] artifact) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.equals("file="+artifactPath)) {
            System.out.println("Invalid query ("+exchange.getRequestURI()+")");
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        if (exchange.getRequestMethod().equals("HEAD")) {
            exchange.sendResponseHeaders(200, -1);
        } else {
            System.out.println("Sending artifact ("+exchange.getRequestMethod()+" "+exchange.getRequestURI()+")");
            exchange.sendResponseHeaders(200, artifact.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(artifact);
            }
        }
    }

}
