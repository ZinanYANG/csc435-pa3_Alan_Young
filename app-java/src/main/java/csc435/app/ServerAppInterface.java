package csc435.app;



import java.util.Scanner;



public class ServerAppInterface {

    private ServerSideEngine engine;



    // Constructor

    public ServerAppInterface(ServerSideEngine engine) {

        this.engine = engine;

    }



    // Method to read commands from the console

    public void readCommands() {

        try (Scanner sc = new Scanner(System.in)) {

            String command;

            while (true) {

                System.out.print("> ");

                command = sc.nextLine().trim();



                if ("quit".equalsIgnoreCase(command)) {

                    engine.shutdown();

                    System.out.println("Server is shutting down.");

                    break;

                } else if (command.startsWith("list")) {

                    engine.list();

                } else {

                    System.out.println("Unrecognized command!");

                }

            }

        } catch (Exception e) {

            System.err.println("An error occurred while reading commands: " + e.getMessage());

        }

    }

}

