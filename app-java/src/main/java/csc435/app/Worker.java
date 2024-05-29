package csc435.app;



import java.util.concurrent.atomic.AtomicLong;

import java.util.HashMap;

import java.util.ArrayList; // This is the new import statement

import java.util.List;

import java.util.stream.Collectors;

import java.io.ObjectInputStream;

import java.io.ObjectOutputStream;

import java.net.Socket;

import java.util.Map;

import java.util.*;



public class Worker implements Runnable {

    private Socket clientSocket;

    private IndexStore store;

    private boolean isIndexing = false;

    private String currentFileName = ""; 

    private final AtomicLong totalSearchingTime = new AtomicLong(0);





    // Constructor with Socket and IndexStore

    public Worker(IndexStore store, Socket clientSocket) {

        this.clientSocket = clientSocket;

        this.store = store;

    }

    

    @Override

    public void run() {

    	try (ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());

         	ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream())) {



        Object request; 

        

        while ((request = inputStream.readObject()) != null) {

            if ("START_INDEXING".equals(request)) {

                isIndexing = true;

            } else if ("END_INDEXING".equals(request)) {

                isIndexing = false;

            } else if (isIndexing && request instanceof String) {

                currentFileName = (String) request;

            } else if (isIndexing && request instanceof Map) {

                @SuppressWarnings("unchecked")

                Map<String, Integer> indexData = (Map<String, Integer>) request;

                if (!currentFileName.isEmpty()) {

                    store.insertIndex(indexData, currentFileName);

                }

            } else if (!isIndexing && request instanceof String) {

                String query = (String) request;   

                long startTime = System.currentTimeMillis();

                             

                List<String> searchResultsList = search(query);

                

                

                // long endTime = System.currentTimeMillis();

                // System.out.println("[Worker] Search completed for: '" + query + "' in " + (endTime - startTime) + " ms");

                

                long searchTime = System.currentTimeMillis() - startTime;

            	totalSearchingTime.addAndGet(searchTime);

            	System.out.println("[Worker] Search completed for: '" + query + "' in " + searchTime + " ms");





                outputStream.writeObject(searchResultsList);

                outputStream.flush();

            } else if (request instanceof String && "quit".equals(request)) {

                        System.out.println("[Worker] Quit command received. Disconnecting client.");

    			// System.out.println("Client requested disconnection.");

    			break; // Exit the loop to end this worker thread

	    }



        }

    } catch (Exception e) {

        // System.err.println("[Worker] Error handling client connection: " + e.getMessage());



    } finally {

        closeClientSocket();

        System.out.println("[Worker] Connection closed successfully.");



    }

}





	public long getTotalSearchingTime() {

        	return totalSearchingTime.get();

    	}





// Within your Worker class

private List<String> search(String query) {

    String[] terms = query.toLowerCase().split("\\s+and\\s+");

    Map<String, Integer> combinedResults = new HashMap<>();



    for (String term : terms) {

        term = term.trim();

        Map<String, Integer> termResults = store.lookupIndex(term);



        if (termResults.isEmpty()) {

            return new ArrayList<>();

        }

        termResults.forEach((doc, count) -> combinedResults.merge(doc, count, Integer::sum));

    }        

     return combinedResults.entrySet().stream()

        .filter(entry -> Arrays.stream(terms).allMatch(term -> store.lookupIndex(term).containsKey(entry.getKey())))

        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())

        .limit(10)

        .map(entry -> entry.getKey() + " contains all terms with a total of " + entry.getValue() + " occurrences")

        .collect(Collectors.toList());

        

}







    // Helper method to close the client socket

    private void closeClientSocket() {

        try {

            if (clientSocket != null && !clientSocket.isClosed()) {

                clientSocket.close();

            }

        } catch (Exception e) {

            System.err.println("Worker: Error closing client socket: " + e.getMessage());

        }

    }

    // Helper method to receive the file name from the client

    private String receiveFileNameFromClient(ObjectInputStream inputStream) throws Exception {

        Object response = inputStream.readObject();

        if (response instanceof String) {

            return (String) response;

        } else {

            throw new IllegalArgumentException("Expected a file name from the client.");

        }

    }

}

