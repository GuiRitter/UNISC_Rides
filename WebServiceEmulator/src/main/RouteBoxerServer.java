package main;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * Communicates with the HTML/Javascript server
 * providing RouteBoxer functionality.
 * @author Guilherme Alan Ritter M72642
 */
@SuppressWarnings("CallToPrintStackTrace")
public final class RouteBoxerServer implements Runnable {

    @Override
    public void run () {
        System.out.println("RouteBoxerServer: running\n");
        Server server = new Server(22114);
        WebSocketHandler wsHandler = new WebSocketHandler() {
            @Override
            public void configure (WebSocketServletFactory factory) {
                factory.register(Handler.class);
            }
        };
        server.setHandler(wsHandler);
        try {
            server.start();
            server.join();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
}
