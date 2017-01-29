package main;

import java.io.PrintWriter;

/**
 *
 * @author Guilherme Alan Ritter M72642
 */
public final class ServerRequest {

    public final PrintWriter out;

    public final String data;

    public ServerRequest (String request, PrintWriter out) {
        this.out  = out    ;
        this.data = request;
    }
}
