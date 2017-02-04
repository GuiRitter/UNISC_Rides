package br.com.een_gmb_gar.unisc_rides;

import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 * Created by nuts on 10/5/15.
 */
public class Client implements Runnable {

    enum Tables {
        TB_LOGIN,
        TB_ALLOCATION,
        TB_MATCH,
        TB_MSG,
        TB_OFFER,
        TB_ORIGIN,
        TB_PERSON,
        TB_ROUTE_BOX
        }
    enum Cmds {
        CMD_CREATE,
        CMD_UPDATE,
        CMD_READ,
        CMD_DELETE,
        CMD_MESSAGE_NEW_ALLOCATION,
        CMD_MSG_NEW_MSG
    }
    private Semaphore sema_run;
    private Semaphore sema_exit;

    private Tables table;
    private Cmds cmd;
    private Object parameter[];

    private static Thread thread;

    // force thread start
    private CountDownLatch latch;

    static String IP;

    protected static Client socket = new Client();

    /**
     * Return code from web service.
     */
    private boolean rc;

    Client() {
        latch = new CountDownLatch(1);
        sema_run = new Semaphore(0);
        sema_exit = new Semaphore(0);

        try{
            WebServiceInterface.initialize(new MessageInterface() {
                @Override
                public void receiveNewAllocation(int person, int offer) {
                    Log.d("Client", "before received allocation");
                    Notify.allocation(person, offer);
                    Log.d("Client", "after received allocation");
                }

                @Override
                public void receiveNewMessage(int sender) {
                    Notify.newMessage(sender);
                }
            });
        }catch (Exception e){
            // TODO
        }
    }

    static
    {
        thread = new Thread(socket);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    private boolean lockParent(){
        try {
            latch.await(); // waits until released by the child thread.
            return true;
        } catch (InterruptedException e) {
            Log.e("latch", "await");
            return false;
        }
    }

    public boolean getRc() {
        return this.rc;
    }

    public void send(Cmds cmd, Tables table, Object param[]) {
        this.cmd = cmd;
        this.table = table;
        this.parameter = param;
        sema_run.release();
        lockParent();
        sema_exit.acquireUninterruptibly();
    }

    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.getAddress().length == 4) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("get IP", ex.toString());
        }
        return null;
    }

    static void initialize () {
        // gambi to get the IP address on App initialization
    }

    public void run(){
        IP = getLocalIpAddress();
        Log.d("get IP", "IP addr = " + IP);

        while (true) {
            sema_run.acquireUninterruptibly();
            try{
                switch (this.cmd) {
                    case CMD_CREATE:
                        switch (this.table) {
                            case TB_ALLOCATION:
                                rc = WebServiceInterface.createAllocation((Allocation) parameter[0]);
                                break;
                            case TB_LOGIN:
                                rc = WebServiceInterface.createLogin((Login) parameter[0]);
                                break;
                            case TB_MSG:
                                rc = WebServiceInterface.createMessage((Message) parameter[0]);
                                break;
                            case TB_OFFER:
                                rc = WebServiceInterface.createOffer((Offer) parameter[0]);
                                break;
                            case TB_ORIGIN:
                                rc = WebServiceInterface.createOrigin((Origin) parameter[0]);
                                break;
                            case TB_PERSON:
                                rc = WebServiceInterface.createPerson((Person) parameter[0]);
                                break;
                        }
                        break;
                    case CMD_UPDATE:
                        switch (this.table) {
                            case TB_ALLOCATION:
                                rc = WebServiceInterface.updateAllocation((Allocation) parameter[0], (Allocation) parameter[1]);
                                break;
                            case TB_LOGIN:
                                rc = WebServiceInterface.updateLogin((Login) parameter[0], (Login) parameter[1]);
                                break;
                            case TB_OFFER:
                                Log.d("Client", "before update offer");
                                Log.d("Client", "param[0] is null? " + (parameter[0] == null) + "param[1] is null? " + (parameter[1] == null));
                                Log.d("Client", "param[0] is Offer? " + (parameter[0].getClass() == Offer.class) + " param[1] is Offer? " + (parameter[1].getClass() == Offer.class));
                                rc = WebServiceInterface.updateOffer((Offer) parameter[0], (Offer) parameter[1]);
                                Log.d("Client", "after update offer, rc = " + rc);
                                break;
                            case TB_ORIGIN:
                                rc = WebServiceInterface.updateOrigin((Origin) parameter[0], (Origin) parameter[1]);
                                break;
                            case TB_PERSON:
                                rc = WebServiceInterface.updatePerson((Person) parameter[0], (Person) parameter[1]);
                                break;
                        }
                        break;
                    case CMD_READ:
                        switch (this.table) {
                            case TB_ALLOCATION:
                                rc = WebServiceInterface.readAllocation((Allocation) parameter[0], (ArrayList<Allocation>) parameter[1]);
                                break;
                            case TB_LOGIN:
                                rc = WebServiceInterface.readLogin((Login) parameter[0], (ArrayList<Login>) parameter[1]);
                                break;
                            case TB_MATCH:
                                rc = WebServiceInterface.readMatch((Match) parameter[0], (ArrayList<Match>) parameter[1]);
                                break;
                            case TB_MSG:
                                rc = WebServiceInterface.readMessage((Message) parameter[0], (ArrayList<Message>) parameter[1]);
                                break;
                            case TB_OFFER:
                                rc = WebServiceInterface.readOffer((Offer) parameter[0], (ArrayList<Offer>) parameter[1]);
                                break;
                            case TB_ORIGIN:
                                rc = WebServiceInterface.readOrigin((Origin) parameter[0], (ArrayList<Origin>) parameter[1]);
                                break;
                            case TB_PERSON:
                                rc = WebServiceInterface.readPerson((Person) parameter[0], (ArrayList<Person>) parameter[1]);
                                break;
                            case TB_ROUTE_BOX:
                                rc = WebServiceInterface.readRouteBox((RouteBox) parameter[0], (ArrayList<RouteBox>) parameter[1]);
                                break;
                        }
                        break;
                    case CMD_DELETE:
                        switch (this.table) {
                            case TB_ALLOCATION:
                                rc = WebServiceInterface.deleteAllocation((Allocation) parameter[0]);
                                break;
                            case TB_LOGIN:
                                rc = WebServiceInterface.deleteLogin(     (Login)      parameter[0]);
                                break;
                            case TB_MATCH:
                                rc = WebServiceInterface.deleteMatch((Match) parameter[0]);
                                break;
                            case TB_MSG:
                                rc = WebServiceInterface.deleteMessage((Message) parameter[0]);
                                break;
                            case TB_OFFER:
                                rc = WebServiceInterface.deleteOffer((Offer) parameter[0]);
                                break;
                            case TB_ORIGIN:
                                rc = WebServiceInterface.deleteOrigin((Origin) parameter[0]);
                                break;
                            case TB_PERSON:
                                rc = WebServiceInterface.deletePerson((Person) parameter[0]);
                                break;
                            case TB_ROUTE_BOX:
                                rc = WebServiceInterface.deleteRouteBox((RouteBox) parameter[0]);
                                break;
                        }
                        break;
                    case CMD_MESSAGE_NEW_ALLOCATION:
                        rc = WebServiceInterface.sendNewAllocationToApp((int) parameter[0], (int) parameter[1]);
                        break;
                    case CMD_MSG_NEW_MSG:
                        // my ID, peer IP
                        rc = WebServiceInterface.sendNewMessageToApp((int) parameter[0], (String) parameter[1]);
                        break;
                }
            }catch(Exception e){
                //Release the mutex if it was acquired before the error
                if(sema_run.availablePermits() > 0){
                    sema_run.drainPermits();
                }
            }
            sema_exit.release();
            // release the parent thread
            latch.countDown();
        }
    }
}
