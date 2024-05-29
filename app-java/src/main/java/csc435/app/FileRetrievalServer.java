package csc435.app;



import java.io.IOException;



public class FileRetrievalServer {

    public static void main(String[] args) {

        try {

            IndexStore store = new IndexStore();

            ServerSideEngine engine = new ServerSideEngine(store);

            ServerAppInterface appInterface = new ServerAppInterface(engine);



            engine.initialize(); 

            appInterface.readCommands();

        } catch (IOException e) {

            e.printStackTrace(); 

        }

    }

}

