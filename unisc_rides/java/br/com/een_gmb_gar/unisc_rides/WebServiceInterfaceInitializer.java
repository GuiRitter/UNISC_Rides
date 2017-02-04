package br.com.een_gmb_gar.unisc_rides;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static br.com.een_gmb_gar.unisc_rides.WebServiceInterface.server;
import static br.com.een_gmb_gar.unisc_rides.WebServiceInterface.serverIP;
import static br.com.een_gmb_gar.unisc_rides.WebServiceInterface.serverIn;
import static br.com.een_gmb_gar.unisc_rides.WebServiceInterface.serverOut;
import static br.com.een_gmb_gar.unisc_rides.WebServiceInterface.serverPort;

/**
 * Created by eenagel on 11/28/15.
 */
public class WebServiceInterfaceInitializer implements Runnable {

    @Override
    public void run() {
        try {
            server = new Socket(serverIP, serverPort);
            serverIn = new PrintWriter(server.getOutputStream(), true);
            serverOut = new BufferedReader(new InputStreamReader(server.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
