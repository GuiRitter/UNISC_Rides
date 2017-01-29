package main;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public final class ResponseListener implements Runnable {

    private DataInputStream inputStream = null;

    private ServerSocket serverSocket = null;

    private Socket socket = null;

    public ResponseListener () throws IOException {
        serverSocket = new ServerSocket(22116);
    }

    @Override
    @SuppressWarnings("CallToThreadDumpStack")
    public void run () {
        try {
            socket = serverSocket.accept();
            inputStream = new DataInputStream(socket.getInputStream());
            System.out.println(inputStream.readInt());
            System.out.println(inputStream.readByte());
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }
}
