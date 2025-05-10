package server;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private ChatServer server; // Your server instance
    private static final DatabaseManager dbManager = new DatabaseManager();

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("Welcome! Type 'login' to log in or 'register' to create a new account:");
            String option = in.readLine();
            boolean authenticated = false;

            while (!authenticated) {
                if ("register".equalsIgnoreCase(option)) {
                    out.println("Enter a username:");
                    String newUser = in.readLine();
                    out.println("Enter a password:");
                    String newPass = in.readLine();

                    if (dbManager.registerUser(newUser, newPass)) {
                        out.println("Registration successful! You can now log in.");
                        option = "login";
                    } else {
                        out.println("Username already exists. Try again.");
                        option = in.readLine();
                    }

                } else if ("login".equalsIgnoreCase(option)) {
                    out.println("Username:");
                    String user = in.readLine();
                    out.println("Password:");
                    String pass = in.readLine();

                    if (dbManager.authenticateUser(user, pass)) {
                        out.println("Login successful. Welcome, " + user + "!");
                        username = user;
                        authenticated = true;
                    } else {
                        out.println("Invalid credentials. Try again:");
                        option = in.readLine();
                    }
                } else {
                    out.println("Invalid option. Type 'login' or 'register':");
                    option = in.readLine();
                }
            }

            out.println("You can now start chatting. Type 'exit' to leave.");

            String message;
            while ((message = in.readLine()) != null) {
                if (message.equalsIgnoreCase("exit")) break;

                System.out.println("[" + username + "]: " + message);
                int senderId = dbManager.getUserId(username);
                dbManager.saveMessage(senderId, null, message); // save public message

                // Use the instance method, not static
                server.broadcast("[" + username + "]: " + message, this);
            }

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            try {
                socket.close();
                server.removeClient(this); // Use instance method
                System.out.println(username + " disconnected.");
            } catch (IOException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}
