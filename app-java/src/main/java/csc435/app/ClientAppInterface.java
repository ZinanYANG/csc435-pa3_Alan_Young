package csc435.app;
import java.util.Scanner;
public class ClientAppInterface {
    private ClientSideEngine engine;
    // Constructor
    public ClientAppInterface(ClientSideEngine engine) {
        this.engine = engine;
    }
    // Method to read and process user commands
    public void readCommands() {
        Scanner sc = new Scanner(System.in);
        String command;
        while (true) {
            System.out.print("> ");
            command = sc.nextLine().trim();
            if ("quit".equalsIgnoreCase(command)) {
                try {
                    engine.closeConnection();
                } catch (Exception e) {
                    System.out.println("Error closing connection: " + e.getMessage());
                }
                System.out.println("Disconnecting...");
                break;
            } else if (command.startsWith("connect ")) {
                String[] parts = command.split("\\s+", 3);
                if (parts.length >= 3) {
                    try {
                        String serverAddress = parts[1];
                        int port = Integer.parseInt(parts[2]);
                        engine.openConnection(serverAddress, port);
                        System.out.println("Connected to server at " + serverAddress + ":" + port);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid port number.");
                    } catch (Exception e) {
                        System.out.println("Error connecting to server: " + e.getMessage());
                    }
                } else {
                    System.out.println("Usage: connect <server_address> <port>");
                }
            } else if (command.startsWith("index ")) {
                String directoryPath = command.substring(6);
                try {
                    engine.indexFiles(directoryPath);
                    System.out.println("Indexing completed for directory: " + directoryPath);
                } catch (Exception e) {
                    System.out.println("Error indexing files: " + e.getMessage());
                }
            } else if (command.startsWith("search ")) {
                String query = command.substring(7);
                try {
                    engine.searchFiles(query);
                } catch (Exception e) {
                    System.out.println("Error searching files: " + e.getMessage());
                }
            } else {
                System.out.println("Unrecognized command!");
            }
        }
        sc.close();
    }
}

