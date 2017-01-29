package webserviceinterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public final class ResponseAppListener implements Runnable {

    private String line = null;

    private BufferedReader inputStream = null;

    private ServerSocket serverSocket = null;

    private Socket socket = null;

    public ResponseAppListener () throws IOException {
        serverSocket = new ServerSocket(WebServiceInterface.serverPort);
    }

    @Override
    @SuppressWarnings({"CallToPrintStackTrace", "CallToThreadDumpStack"})
    public void run () {
        while (true) {
            try {
                socket = serverSocket.accept();
                inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                line = inputStream.readLine();
                WebServiceInterface.setResponse(line);
                inputStream.close();
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
                System.exit(0);
	    }
        }
    }
}
