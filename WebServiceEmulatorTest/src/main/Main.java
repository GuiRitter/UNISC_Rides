package main;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public final class Main {

    private static byte[] data = null;

    private static int dataLength = 0;

    private static DataOutputStream outStream = null;

    private static Socket socket = null;

    public static final void main (String args[]) throws UnknownHostException, IOException {
        new Thread(new ResponseListener()).start();
        data = ("POST\nPerson\nname_\n\"Adão\"\nphone\n98765432\n"
         + "email\n\"a@t.com\"\nip\n\"128.0.0.1\"").getBytes();
        dataLength = data.length;
        socket = new Socket("10.163.1.1", 22113);
        outStream = new DataOutputStream(socket.getOutputStream());
        outStream.writeInt(dataLength);
        outStream.write(data);
//        outStream.writeByte(77);
        outStream.close();
        socket.close();
    }
}
