package csc435.app;



import java.io.*;

import java.net.Socket;

import java.util.HashMap;

import java.util.Map;

import java.util.List;

import java.util.stream.Collectors; 



public class ClientSideEngine {

    private Socket socket;

    private ObjectOutputStream outputStream;

    private ObjectInputStream inputStream;

    private boolean isConnected = false;



    // Constructor

    public ClientSideEngine() {

        this.socket = null;

        this.outputStream = null;

        this.inputStream = null;

        this.isConnected = false;

    }



    public void openConnection(String serverAddress, int serverPort) throws IOException {

        this.socket = new Socket(serverAddress, serverPort);

        this.outputStream = new ObjectOutputStream(socket.getOutputStream());

        this.inputStream = new ObjectInputStream(socket.getInputStream());

        this.isConnected = true;

    }



    public void indexFiles(String directoryPath) throws IOException {

    if (!isConnected) {

        throw new IllegalStateException("Must be connected to a server to index files.");

    }

    

    

     // Start time measurement

    long startTime = System.nanoTime();

    

    System.out.println("Indexing files in directory: " + directoryPath);

    outputStream.writeObject("START_INDEXING");



    File dir = new File(directoryPath);

    File[] files = dir.listFiles();

    if (files != null) {

        for (File file : files) {

            if (file.isFile()) {

                System.out.println("Indexing file: " + file.getName());

                Map<String, Integer> wordCount = countWords(file);

                outputStream.writeObject(file.getName());

                outputStream.writeObject(wordCount);

                outputStream.flush();

            } else if (file.isDirectory()) {

                indexFiles(file.getAbsolutePath());

            }

        }

    }

    outputStream.writeObject("END_INDEXING");

     // End time measurement

    long endTime = System.nanoTime();

    long duration = (endTime - startTime) / 1_000_000; // Convert to milliseconds

    

    System.out.println("Indexing completed for directory: " + directoryPath);

    System.out.println("Indexing took " + duration + " ms");



}



    private Map<String, Integer> countWords(File file) throws IOException {

        Map<String, Integer> wordCount = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] words = line.toLowerCase().split("\\W+");

                for (String word : words) {

                    wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);

                }

            }

        }

        return wordCount;

    }



    public void searchFiles(String query) throws IOException {

        if (!isConnected) {

            throw new IllegalStateException("Must be connected to a server to search files.");

        }

	System.out.println("[ClientSideEngine] Sending search query: " + query);

        outputStream.writeObject(query);

        outputStream.flush();

        try {

            System.out.println("[ClientSideEngine] Waiting for response from server...");

            Object response = inputStream.readObject();

            System.out.println("[ClientSideEngine] Received response from server.");            

            if (response instanceof List) {

        	List<?> resultList = (List<?>) response;

                if (resultList.isEmpty()) {

                       System.out.println("No results found for query: " + query);

                     } else {

                         System.out.println("[ClientSideEngine] Search results for query \"" + query + "\": ");

                         resultList.forEach(System.out::println);

                       }

           }else {

            	System.err.println("Received unexpected response type from server: " + response.getClass());

                throw new IOException("Invalid response from server.");

            }

        } catch (Exception e) {

                      // This will catch any exception, including ClassNotFoundException, SocketTimeoutException, etc.

                      System.err.println("An error occurred while waiting for or processing the response from server:");

                      e.printStackTrace(); // Print the full stack trace to understand what went wrong

                      throw new IOException("An error occurred while communicating with the server.", e);

         }

    }



    public void closeConnection() throws IOException {

        if (socket != null) {

            socket.close();

        }

        if (outputStream != null) {

            outputStream.close();

        }

        if (inputStream != null) {

            inputStream.close();

        }

        this.isConnected = false;

    }

}

