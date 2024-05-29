package csc435.app;



import java.io.IOException;

import java.net.Socket;

import java.util.Collections;

import java.util.HashSet;

import java.util.Set;

import java.util.concurrent.ExecutorService;

import java.util.concurrent.Executors;



public class ServerSideEngine {

    private IndexStore store;

    private ExecutorService executorService; 

    private Thread dispatcherThread; 

    private Dispatcher dispatcher; 

    private Set<String> clientAddresses;

    private final int port = 12345;



    // Constructor

    public ServerSideEngine(IndexStore store) {

        this.store = store;

        this.executorService = Executors.newCachedThreadPool();

        this.clientAddresses = Collections.synchronizedSet(new HashSet<>());

    }



    // Initialize the server by creating and starting the dispatcher thread

    public void initialize() throws IOException {

        dispatcher = new Dispatcher(this, port);

        dispatcherThread = new Thread(dispatcher);

        dispatcherThread.start();

    }



    // Create and start a new worker thread for each client connection

    public void spawnWorker(Socket clientSocket) {

        Worker worker = new Worker(store, clientSocket);

        executorService.submit(worker);

        // Track the client connection

        String clientAddress = clientSocket.getInetAddress().getHostAddress();

        int clientPort = clientSocket.getPort(); 

        String clientIdentifier = clientAddress + " " + clientPort;

        clientAddresses.add(clientIdentifier);

        System.out.println("Connected clients: " + clientAddresses.size());

    

    }



    // Gracefully shutdown the server by stopping the dispatcher and worker threads

    public void shutdown() {

    	try {

        	dispatcher.closeServerSocket(); 

        	executorService.shutdownNow();

        	if (dispatcherThread != null && !dispatcherThread.isInterrupted()) {

            		dispatcherThread.interrupt();

        	}

    	} catch (Exception e) {

        	System.err.println("Error during server shutdown: " + e.getMessage());

    		}

    	

    	

    	

    	System.out.println("Server shutdown complete.");

    	

    }

    





    // List information about connected clients

    public void list() {

      // Print information about connected clients

      System.out.println("Connected clients:");

      for (String clientInfo : clientAddresses) {

          System.out.println(clientInfo);

        }

    }



    // Remove a client address when a client disconnects

    public void removeClientAddress(String clientAddress) {

        clientAddresses.remove(clientAddress);

    }



    // Get the connected clients information

    public Set<String> getConnectedClients() {

        return Collections.unmodifiableSet(clientAddresses);

    }

    

    public int getPort() {

    	return this.port; 

    }	



}

