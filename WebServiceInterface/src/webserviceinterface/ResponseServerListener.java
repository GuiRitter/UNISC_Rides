package webserviceinterface;

import java.io.BufferedReader;
import java.io.IOException;

public final class ResponseServerListener implements Runnable {

    private String line = null;

    private BufferedReader serverOut = null;

    public ResponseServerListener (BufferedReader serverOut) throws IOException {
        this.serverOut = serverOut;
    }

    @Override
    @SuppressWarnings({"CallToPrintStackTrace", "CallToThreadDumpStack"})
    public void run () {
        try {
            while ((line = serverOut.readLine()) != null) {
                WebServiceInterface.setResponse(line);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
