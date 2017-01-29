package main;

import java.io.IOException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@SuppressWarnings("CallToPrintStackTrace")
@WebSocket
public class Handler {

    private Session session;

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.println("RouteBoxerHandler: Close: statusCode=" + statusCode + ", reason=" + reason + "\n");
    }

    @OnWebSocketError
    public void onError(Throwable t) {
        System.out.println("RouteBoxerHandler: Error: " + t.getMessage()
         + " " + System.currentTimeMillis() + "\n");
    }

    @OnWebSocketConnect
    @SuppressWarnings("CallToPrintStackTrace")
    public void onConnect(Session session) {
        System.out.println("RouteBoxerHandler: Connect: "
         + session.getRemoteAddress().getAddress()
         + " " + System.currentTimeMillis() + "\n");
        this.session = session;
        RouteBoxer.instance.setHandler(this);
        new Thread(new KeepAliver(this)).start();
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        System.out.println("RouteBoxerHandler: Message: " + message + "\n");
        RouteBoxer.instance.response = message;
        RouteBoxer.instance.flowSemaphore.release();
    }

    void send (String message) {
        System.out.println("RouteBoxerHandler: received request: " + message + "\n");
        try {
            session.getRemote().sendString(message);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}