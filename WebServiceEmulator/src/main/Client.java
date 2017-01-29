package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Represents and manages the connection to one client.
 * @author Guilherme Alan Ritter M72642
 */
@SuppressWarnings("CallToPrintStackTrace")
final class Client implements Runnable {

    private final BufferedReader in;

    private String line = null;

    private final PrintWriter out;

    private ServerRequest request = null;

    private final Socket socket;

    public Client(Socket socket) throws IOException {
        this.socket = socket;
        System.out.println("Client: "
         + socket.getInetAddress() + " connected.\n");
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run () {
        try {
            while ((line = in.readLine()) != null) {
                System.out.println("Client: received:");
                System.out.println(Main.showControl(line));
                System.out.println("from");
                System.out.println(socket.getInetAddress());
                System.out.println();
                request = new ServerRequest(line, out);
                Main.request(request);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("Client: " + socket.getInetAddress() + " closed.\n");
    }
}
