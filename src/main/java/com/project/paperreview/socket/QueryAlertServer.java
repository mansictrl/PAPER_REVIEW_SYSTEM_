// === socket/QueryAlertServer.java ===
package com.project.paperreview.socket;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * CO4 — Socket Programming (Server Side)
 *
 * IMPROVED: Each incoming connection is handled on its own Thread.
 * This means multiple clients can send alerts simultaneously
 * without one blocking another.
 *
 * Thread-per-client is the basic multithreading pattern at SY level.
 * For viva: explain that ServerSocket.accept() waits for a connection,
 * then we hand it off to a new Thread and immediately loop back to wait
 * for the next connection.
 */
@Component
public class QueryAlertServer implements ApplicationRunner {

    private static final int PORT = 9090;

    @Override
    public void run(ApplicationArguments args) {
        Thread serverThread = new Thread(this::startListening);
        serverThread.setName("query-alert-server");
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private void startListening() {
        System.out.println("[SOCKET SERVER] Starting on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SOCKET SERVER] Ready. Listening for alerts (thread-per-client).");

            while (true) {
                // Wait for next client connection
                Socket clientSocket = serverSocket.accept();

                // Hand off to a new thread — don't block the main loop
                Thread clientThread = new Thread(() -> handleClient(clientSocket));
                clientThread.setDaemon(true);
                clientThread.setName("socket-client-" + clientSocket.getPort());
                clientThread.start();
            }

        } catch (Exception e) {
            System.out.println("[SOCKET SERVER] Error: " + e.getMessage());
        }
    }

    /**
     * Handles one client connection on its own thread.
     * Reads the alert message and prints it.
     */
    private void handleClient(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream())
            );

            String message = reader.readLine();

            System.out.println("════════════════════════════════════════════");
            System.out.println("[SOCKET ALERT] " + message);
            System.out.println("[SOCKET ALERT] Handled by: " + Thread.currentThread().getName());
            System.out.println("════════════════════════════════════════════");

            clientSocket.close();

        } catch (Exception e) {
            System.out.println("[SOCKET SERVER] Client error: " + e.getMessage());
        }
    }
}