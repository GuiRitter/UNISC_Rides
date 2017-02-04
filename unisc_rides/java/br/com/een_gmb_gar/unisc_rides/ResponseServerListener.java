package br.com.een_gmb_gar.unisc_rides;

import java.io.IOException;
import static br.com.een_gmb_gar.unisc_rides.WebServiceInterface.serverOut;

public final class ResponseServerListener implements Runnable {

    private String line = null;

    public ResponseServerListener () throws IOException {
    }

    @Override
    @SuppressWarnings({"CallToPrintStackTrace", "CallToThreadDumpStack"})
    public void run () {
        try {
            while (serverOut == null){}

            while ((line = serverOut.readLine()) != null) {
                WebServiceInterface.setResponse(line);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}