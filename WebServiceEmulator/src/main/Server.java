package main;

import java.io.IOException;
import java.net.ServerSocket;
import static main.Main.port;

/**
 * Listens for new connections.
 * @author Guilherme Alan Ritter M72642
 */
final class Server implements Runnable {

    private final ServerSocket serverSocket;

    public Server() throws IOException {
        serverSocket = new ServerSocket(port);
    }

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void run () {
        while (true) {
            try {
                System.out.println("Server: listening\n");
                new Thread(new Client(serverSocket.accept())).start();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
