package csc435.app;



import java.util.concurrent.atomic.AtomicLong;

import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;



public class IndexStore {

    private final Map<String, Map<String, Integer>> globalIndex;

    private final AtomicLong totalIndexingTime = new AtomicLong(0);



    // Constructor

    public IndexStore() {

        globalIndex = new ConcurrentHashMap<>();

    }

    

    public synchronized void insertIndex(Map<String, Integer> localIndex, String fileName) {

        long startTime = System.currentTimeMillis();



    	System.out.println("[IndexStore] Attempting to index file: " + fileName);

    	if (localIndex == null || localIndex.isEmpty()) {

        	System.out.println("[IndexStore] Local index for \"" + fileName + "\" is empty, skipping.");

        	return;

    	}



    	localIndex.forEach((word, occurrence) -> {

        globalIndex.compute(word, (key, currentMap) -> {

            if (currentMap == null) {

                // If the word is not already in the index, add it with the current document's occurrences.

                currentMap = new ConcurrentHashMap<>();

                currentMap.put(fileName, occurrence);

            } else {

                // If the word is already in the index, update the occurrences only if the new count is higher.

                currentMap.merge(fileName, occurrence, Integer::max);

            }

            return currentMap;

        });

    });



    // System.out.println("[IndexStore] Indexing completed for file: " + fileName);

    long indexingTime = System.currentTimeMillis() - startTime;

    totalIndexingTime.addAndGet(indexingTime);

    System.out.println("[IndexStore] Indexing completed for file: " + fileName + " in " + indexingTime + " ms");

    

}



    // Method to lookup index data for a particular term

    public Map<String, Integer> lookupIndex(String term) {

    	long startTime = System.currentTimeMillis();

    	System.out.println("[IndexStore] Looking up index for term: \"" + term + "\"");

    	Map<String, Integer> searchResults = globalIndex.getOrDefault(term, new ConcurrentHashMap<>());

    	long endTime = System.currentTimeMillis();



    		if (searchResults.isEmpty()) {

        	// System.out.println("[IndexStore] No search results found for '" + term + "'");

        	System.out.println("[IndexStore] No search results found for '" + term + "' in " + (endTime - startTime) + " ms");



    		} else {

        		// System.out.println("[IndexStore] Found search results for '" + term + "': " + searchResults);

        		System.out.println("[IndexStore] Found search results for '" + term + "': " + searchResults + " in " + (endTime - startTime) + " ms");

    		}

    		return searchResults;

	}





    public void printIndex() {

        globalIndex.forEach((term, files) -> {

            System.out.println("Term: " + term + ", Files: " + files);

        });

    }

}

