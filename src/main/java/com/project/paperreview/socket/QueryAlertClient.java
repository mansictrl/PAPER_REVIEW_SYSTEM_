// === QueryAlertClient.java ===
// Location: src/main/java/com/project/paperreview/socket/QueryAlertClient.java

package com.project.paperreview.socket;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.net.Socket;

/**
 * CO4 — Socket Programming (Client Side)
 *
 * This connects to QueryAlertServer and sends a message
 * whenever a student's query is resolved.
 *
 * @Async: runs on a separate thread so the HTTP response
 * is not delayed waiting for the socket connection.
 *
 * In viva, explain:
 *   Socket(host, port) — creates a TCP connection to the server.
 *   PrintWriter wraps the output stream for easy text sending.
 *   println() sends the message. The server's readLine() receives it.
 *   We close the socket after sending.
 *
 *   If the server is not running, we catch the exception and log it
 *   — the application does NOT crash.
 */
@Service
public class QueryAlertClient {

    private static final int    SERVER_PORT = 9090;
    private static final String SERVER_HOST = "localhost";

    @Async
    public void sendResolvedAlert(String prn, String question) {

        String message = "QUERY RESOLVED | Student PRN: " + prn +
                         " | Question: " + question;

        System.out.println("[SOCKET CLIENT] Sending alert: " + message);

        try {
            // Step 1: Open a connection to the server
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);

            // Step 2: Get the output stream and wrap it
            // autoFlush=true means the message is sent immediately without buffering
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            // Step 3: Send the message
            writer.println(message);

            // Step 4: Close the connection
            socket.close();

            System.out.println("[SOCKET CLIENT] Alert sent successfully.");

        } catch (Exception e) {
            // If server is not running, just log — do NOT crash the app
            System.out.println("[SOCKET CLIENT] Could not reach server: " + e.getMessage());
        }
    }
}