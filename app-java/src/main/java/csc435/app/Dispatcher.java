package csc435.app;



import java.io.IOException;

import java.net.ServerSocket;

import java.net.Socket;



public class Dispatcher implements Runnable {

    private ServerSideEngine engine;

    private ServerSocket serverSocket;

    public Dispatcher(ServerSideEngine engine, int port) throws IOException {

        this.engine = engine;

        this.serverSocket = new ServerSocket(engine.getPort());

    }



    @Override

    public void run() {

        System.out.println("Server is listening on port " + serverSocket.getLocalPort());

        try {

            while (!Thread.currentThread().isInterrupted()) {

                try {

                    Socket clientSocket = serverSocket.accept();

                    System.out.println("[Dispatcher] New client connected: " + clientSocket.getInetAddress().getHostAddress());

                    engine.spawnWorker(clientSocket);

                } catch (IOException e) {

                    if (serverSocket.isClosed()) {

                        System.out.println("Server socket is closed. Exiting listener thread.");

                        break;

                    }

                    System.err.println("Error accepting client connection: " + e.getMessage());

                }

            }

        } finally {

            if (serverSocket != null && !serverSocket.isClosed()) {

                try {

                    serverSocket.close();

                } catch (IOException e) {

                    System.err.println("Error closing server socket: " + e.getMessage());

                }

            }

        }

    }

    

    public void closeServerSocket() throws IOException {

    	if (serverSocket != null && !serverSocket.isClosed()) {

        	serverSocket.close();

    	}

    }





}

