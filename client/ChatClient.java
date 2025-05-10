package client;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        final boolean[] isDisconnected = {false}; // Shared flag for disconnection

        try (
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Connected to chat server. Type 'exit' to quit.");

            // Thread to read messages from server
            Thread receiveThread = new Thread(() -> {
                String serverMsg;
                try {
                    while ((serverMsg = in.readLine()) != null) {
                        System.out.println(serverMsg);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                    isDisconnected[0] = true; // Notify main thread
                }
            });
            receiveThread.start();

            // Main thread sends user input to server
            while (true) {
                if (isDisconnected[0]) {
                    break;
                }
                try {
                    String userInput = scanner.nextLine();
                    if (userInput.equalsIgnoreCase("exit")) {
                        receiveThread.interrupt();
                        break;
                    }
                    out.println(userInput);
                } catch (Exception e) {
                    System.out.println("Error reading input: " + e.getMessage());
                    break;
                }
            }

            System.out.println("You have left the chat.");
        } catch (IOException e) {
            System.out.println("Unable to connect to server: " + e.getMessage());
        }
    }
}
