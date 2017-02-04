package br.com.een_gmb_gar.unisc_rides;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

final class WebServiceInterface {

    private static Allocation allocation = null;

    private static final ArrayList<Allocation> allocationList = new ArrayList<>();

    static final StringBuilder builder = new StringBuilder();

    private static int i = 0;

    private static String lines[] = null;

    private static Login login = null;

    private static final ArrayList<Login> loginList = new ArrayList<>();

    private static Match match = null;

    private static final ArrayList<Match> matchList = new ArrayList<>();

    private static Message message = null;

    private static final ArrayList<Message> messageList = new ArrayList<>();

    static final String MESSAGE_CREATE = "POST";
    static final String MESSAGE_READ   = "GET";
    static final String MESSAGE_UPDATE = "PUT";
    static final String MESSAGE_DELETE = "DELETE";

    static final String MESSAGE_ACK                 = "ack";
    static final String MESSAGE_NACK                = "nack";
    static final String MESSAGE_NEW_ALLOCATION      = "newAllocation";
    static final String MESSAGE_NEW_MESSAGE         = "newMessage";
    static final String MESSAGE_UNKNOWN_PACKET_TYPE = "unknown_packet_type";
    static final String MESSAGE_WRONG_FIELD_AMOUNT  = "wrong_field_amount";
    static final String MESSAGE_WRONG_ROW_AMOUNT    = "wrong_row_amount";

    private static MessageInterface messageInterface;

    private static Offer offer = null;

    private static final ArrayList<Offer> offerList = new ArrayList<>();

    private static Origin origin = null;

    private static final ArrayList<Origin> originList = new ArrayList<>();

    static PrintStream outStream = null;

    private static Person person = null;

    private static final ArrayList<Person> personList = new ArrayList<>();

    private static String response = null;

    private static RouteBox routeBox = null;

    private static final ArrayList<RouteBox> routeBoxList = new ArrayList<>();

    /**
     * Field separator. Horizontal tab character.
     */
    static final String S_F = "\t";

    /**
     * Message separator. Form feed character.
     */
    static final String S_M = "\f";

    /**
     * Row separator. Vertical tab character.
     */
    static final String S_R = "\u000b";

    static final String SELECT_ALL = 0 + S_F + 0;

    private static final Semaphore semaphore = new Semaphore(0, true);

    static Socket server = null;

    static final String serverIP = "192.168.0.13";

    static PrintWriter serverIn = null;

    static BufferedReader serverOut = null;

    static final int serverPort = 22113;

    static final SimpleDateFormat SQLdateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private static Socket socket = null;

    private static boolean success = true;

    private static boolean success_ = true;

    private static Context cntx2toast;

    enum Table {
        Allocation,
        Login,
        Match_,
        Message,
        Offer,
        Origin,
        Person,
        RouteBox
    }

    static final void initialize (MessageInterface msgIf) throws IOException {
        Thread responder = new Thread(new WebServiceInterfaceInitializer());
        responder.setPriority(Thread.MAX_PRIORITY);
        responder.start();

        responder = new Thread(new ResponseAppListener());
        responder.setPriority(Thread.MAX_PRIORITY);
        responder.start();
        WebServiceInterface.messageInterface = msgIf;

		responder = new Thread(new ResponseServerListener());
        responder.setPriority(Thread.MAX_PRIORITY);
        responder.start();

    }

    private static boolean createUpdateDelete (String message, String tableName,
     String keyValuePairs0, String keyValuePairs1) {
        if (keyValuePairs0 == null) {
            return false;
        }
        builder.setLength(0);
        builder.append(message).append(S_F).append(tableName).append(S_R)
        .append(keyValuePairs0);
        if (keyValuePairs1 != null) {
            builder.append(S_R).append(keyValuePairs1);
        }
        sendToServer();
        return lines[0].equals(MESSAGE_ACK);
    }

    static final boolean createAllocation (Allocation newAllocation) {
        if (newAllocation.isNull()) return false;
        personList.clear();
        if (!readPerson(null, personList)) return false;
        success = false;
        for (Person p : personList) {
            if (newAllocation.person == p.id) {
                success = true;
                break;
            }
        }
        personList.clear();
        if (!success) return false;
        offerList.clear();
        if (!readOffer(null, offerList)) return false;
        success = false;
        for (Offer o : offerList) {
            if (newAllocation.offer == o.id) {
                success = true;
                break;
            }
        }
        offerList.clear();
        if (!success) return false;
        allocationList.clear();
        if (!readAllocation(null, allocationList)) return false;
        success = true;
        for (Allocation a : allocationList) {
            if ((newAllocation.person == a.person)
             && (newAllocation.offer == a.offer)) {
                success = false;
                break;
            }
        }
        allocationList.clear();
        if (!success) return false;
        return createUpdateDelete(MESSAGE_CREATE, Table.Allocation.name(),
         newAllocation.getKeyValuePairs(), null);
    }

    static final boolean createLogin (Login newLogin) {
        if (newLogin.isNull()) return false;
        personList.clear();
        if (!readPerson(null, personList)) return false;
        success = false;
        for (Person p : personList) {
            if (newLogin.person == p.id) {
                success = true;
                break;
            }
        }
        personList.clear();
        if (!success) return false;
        loginList.clear();
        if (!readLogin(null, loginList)) return false;
        success = true;
        for (Login l : loginList) {
            if (newLogin.person == l.person) {
                success = false;
                break;
            }
        }
        loginList.clear();
        if (!success) return false;
        return createUpdateDelete(MESSAGE_CREATE, Table.Login.name(),
         newLogin.getKeyValuePairs(), null);
    }

    static final boolean createMessage (Message newMessage) {
        if (newMessage.isNull(false, false)) return false;
        newMessage.date = new Date(System.currentTimeMillis());
        newMessage.time = new Time(System.currentTimeMillis());
        personList.clear();
        if (!readPerson(null, personList)) return false;
        success = false;
        success_ = false;
        for (Person p : personList) {
            if (newMessage.sender == p.id) {
                success = true;
            }
            if (newMessage.receiver == p.id) {
                success_ = true;
            }
            if (success && success_) break;
        }
        personList.clear();
        if (!success) return false;
        if (!success_) return false;
        return createUpdateDelete(MESSAGE_CREATE, Table.Message.name(),
         newMessage.getKeyValuePairs(), null);
    }

    static final boolean createOffer (Offer newOffer) {
        if (newOffer.isNull(false, false)) return false;
        newOffer.insertionDate = new Date(System.currentTimeMillis());
        personList.clear();
        if (!readPerson(null, personList)) return false;
        success = false;
        for (Person p : personList) {
            if (newOffer.person == p.id) {
                success = true;
                break;
            }
        }
        personList.clear();
        if (!success) return false;
        return createUpdateDelete(MESSAGE_CREATE, Table.Offer.name(),
         newOffer.getKeyValuePairs(), null);
    }

    static final boolean createOrigin (Origin newOrigin) {
        if (newOrigin.isPersonNull()) return false;
//        if (newOrigin.isNeighborhoodNull()) return false;
//        if (newOrigin.isCityNull()) return false;
        personList.clear();
        if (!readPerson(null, personList)) return false;
        success = false;
        for (Person p : personList) {
            if (newOrigin.person == p.id) {
                success = true;
                break;
            }
        }
        personList.clear();
        if (!success) return false;
        newOrigin.id = -1;
        return createUpdateDelete(MESSAGE_CREATE, Table.Origin.name(),
         newOrigin.getKeyValuePairs(), null);
    }

    static final boolean createPerson (Person newPerson) {
        if (newPerson.isNameNull()) return false;
        if (newPerson.isIPNull()) return false;
        newPerson.id = -1; // auto
        return createUpdateDelete(MESSAGE_CREATE, Table.Person.name(),
         newPerson.getKeyValuePairs(), null);
    }

    static final boolean deleteAllocation (Allocation oldAllocation) {
        if (oldAllocation == null) return false;
        if (oldAllocation.isNull()) return false;
        return createUpdateDelete(MESSAGE_DELETE, Table.Allocation.name(),
         oldAllocation.getKeyValuePairs(), null);
    }

    static final boolean deleteLogin (Login oldLogin) {
        if (oldLogin == null) return false;
        if (oldLogin.isNull()) return false;
        return createUpdateDelete(MESSAGE_DELETE, Table.Login.name(),
         oldLogin.getKeyValuePairs(), null);
    }

    static final boolean deleteMatch (Match oldMatch) {
        if (oldMatch == null) return false;
        if (oldMatch.isNull()) return false;
        return createUpdateDelete(MESSAGE_DELETE, Table.Match_.name(),
         oldMatch.getKeyValuePairs(), null);
    }

    static final boolean deleteMessage (Message oldMessage) {
        if (oldMessage == null) return false;
        if (oldMessage.isNull(true, true)) return false;
        return createUpdateDelete(MESSAGE_DELETE, Table.Message.name(),
         oldMessage.getKeyValuePairs(), null);
    }

    static final boolean deleteOffer (Offer oldOffer) {
        if (oldOffer == null) return false;
        if (oldOffer.isNull(true, true)) return false;
        offerList.clear();
        if (oldOffer.isIdNull()) {
            if (!readOffer(oldOffer, offerList)) return false;
        } else {
            offerList.add(oldOffer);
        }
        for (Offer o : offerList) {
            allocation = new Allocation();
            allocation.offer = o.id;
            if (!deleteAllocation(allocation)) return false;
            match = new Match();
            match.offer = o.id;
            if (!deleteMatch(match)) return false;
            routeBox = new RouteBox();
            routeBox.offer = o.id;
            if (!deleteRouteBox(routeBox)) return false;
            if (!createUpdateDelete(MESSAGE_DELETE, Table.Offer.name(),
             o.getKeyValuePairs(), null)) return false;
        }
        offerList.clear();
        return true;
    }

    static final boolean deleteOrigin (Origin oldOrigin) {
        if (oldOrigin == null) return false;
        if (oldOrigin.isNull(true)) return false;
        originList.clear();
        if (oldOrigin.isIdNull()) {
            if (!readOrigin(oldOrigin, originList)) return false;
        } else {
            originList.add(oldOrigin);
        }
        for (Origin o : originList) {
            allocation = new Allocation();
            allocation.origin = o.id;
            if (!deleteAllocation(allocation)) return false;
            match = new Match();
            match.origin = o.id;
            if (!deleteMatch(match)) return false;
            offer = new Offer();
            offer.origin = o.id;
            if (!deleteOffer(offer)) return false;
            if (!createUpdateDelete(MESSAGE_DELETE, Table.Origin.name(),
             o.getKeyValuePairs(), null)) return false;
        }
        originList.clear();
        return true;
    }

    static final boolean deletePerson (Person oldPerson) {
        if (oldPerson == null) return false;
        if (oldPerson.isNull(true)) return false;
        personList.clear();
        if (oldPerson.isIdNull()) {
            if (!readPerson(oldPerson, personList)) return false;
        } else {
            personList.add(oldPerson);
        }
        for (Person p : personList) {
            login = new Login();
            login.person = p.id;
            if (!deleteLogin(login)) return false;
            origin = new Origin();
            origin.person = p.id;
            if (!deleteOrigin(origin)) return false;
            allocation = new Allocation();
            allocation.person = p.id;
            if (!deleteAllocation(allocation)) return false;
            message = new Message();
            message.sender = p.id;
            if (!deleteMessage(message)) return false;
            message.sender = 0;
            message.receiver = p.id;
            if (!deleteMessage(message)) return false;
            if (!createUpdateDelete(MESSAGE_DELETE, Table.Person.name(),
             p.getKeyValuePairs(), null)) return false;
        }
        personList.clear();
        return true;
    }

    static final boolean deleteRouteBox (RouteBox oldRouteBox) {
        if (oldRouteBox == null) return false;
        if (oldRouteBox.isNull()) return false;
        return createUpdateDelete(MESSAGE_DELETE, Table.RouteBox.name(),
         oldRouteBox.getKeyValuePairs(), null);
    }

    private static boolean read (
     Table table,
     ArrayList<Allocation> allocations,
     ArrayList<Login> logins,
     ArrayList<Match> matches,
     ArrayList<Message> messages,
     ArrayList<Offer> offers,
     ArrayList<Origin> origins,
     ArrayList<Person> persons,
     ArrayList<RouteBox> routeBoxes,
     Allocation allocationSelect,
     Login loginSelect,
     Match matchSelect,
     Message messageSelect,
     Offer offerSelect,
     Origin originSelect,
     Person personSelect,
     RouteBox routeBoxSelect) {
        switch (table) {
        case Allocation:
            if (allocations == null) allocations = new ArrayList<>();
        break; case Login:
            if (logins == null) logins = new ArrayList<>();
        break; case Match_:
            if (matches == null) matches = new ArrayList<>();
        break; case Message:
            if (messages == null) messages = new ArrayList<>();
        break; case Offer:
            if (offers == null) offers = new ArrayList<>();
        break; case Origin:
            if (origins == null) origins = new ArrayList<>();
        break; case Person:
            if (persons == null) persons = new ArrayList<>();
        break; case RouteBox:
            if (routeBoxes == null) routeBoxes = new ArrayList<>();
        }
        success = true;
        builder.setLength(0);
        builder.append(MESSAGE_READ).append(S_F)
         .append(table.name()).append(S_R);
        switch (table) {
        case Allocation:
            if (allocationSelect == null) builder.append(SELECT_ALL);
            else if (allocationSelect.isNull()) builder.append(SELECT_ALL);
            else builder.append(allocationSelect.getKeyValuePairs());
        break; case Login:
            if (loginSelect == null) builder.append(SELECT_ALL);
            else if (loginSelect.isNull()) builder.append(SELECT_ALL);
            else builder.append(loginSelect.getKeyValuePairs());
        break; case Match_:
            if (matchSelect == null) builder.append(SELECT_ALL);
            else if (matchSelect.isNull()) builder.append(SELECT_ALL);
            else builder.append(matchSelect.getKeyValuePairs());
        break; case Message:
            if (messageSelect == null) builder.append(SELECT_ALL);
            else if (messageSelect.isNull(true, true)) builder.append(SELECT_ALL);
            else builder.append(messageSelect.getKeyValuePairs());
        break; case Offer:
            if (offerSelect == null) builder.append(SELECT_ALL);
            else if (offerSelect.isNull(true, true)) builder.append(SELECT_ALL);
            else builder.append(offerSelect.getKeyValuePairs());
        break; case Origin:
            if (originSelect == null) builder.append(SELECT_ALL);
            else if (originSelect.isNull(true)) builder.append(SELECT_ALL);
            else builder.append(originSelect.getKeyValuePairs());
        break; case Person:
            if (personSelect == null) builder.append(SELECT_ALL);
            else if (personSelect.isNull(true)) builder.append(SELECT_ALL);
            else builder.append(personSelect.getKeyValuePairs());
        break; case RouteBox:
            if (routeBoxSelect == null) builder.append(SELECT_ALL);
            else if (routeBoxSelect.isNull()) builder.append(SELECT_ALL);
            else builder.append(routeBoxSelect.getKeyValuePairs());
        }
        sendToServer();
        Log.d("WebService", lines[0]);
        if (lines[0].equals(MESSAGE_ACK)
         || lines[0].equals(MESSAGE_NACK)
         || lines[0].equals(MESSAGE_UNKNOWN_PACKET_TYPE)
         || lines[0].equals(MESSAGE_WRONG_FIELD_AMOUNT)
         || lines[0].equals(MESSAGE_WRONG_ROW_AMOUNT)) {
            return false;
        }
        for (i = 1; i < lines.length; i++) {
            try {
                switch (table) {
                case Allocation:
                    allocations.add(new Allocation(lines[i]));
                break; case Login:
                    logins.add(new Login(lines[i]));
                break; case Match_:
                    matches.add(new Match(lines[i]));
                break; case Message:
                    messages.add(new Message(lines[i]));
                break; case Offer:
                    offers.add(new Offer(lines[i]));
                break; case Origin:
                    origins.add(new Origin(lines[i]));
                break; case Person:
                    persons.add(new Person(lines[i]));
                break; case RouteBox:
                    routeBoxes.add(new RouteBox(lines[i]));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                success = false;
            }
        }
        return success;
    }

    static final boolean readAllocation (Allocation selectObject, ArrayList<Allocation> list) {
        return read(Table.Allocation,
         list,         null, null, null, null, null, null, null,
         selectObject, null, null, null, null, null, null, null);
    }

    static final boolean readLogin (Login selectObject, ArrayList<Login> list) {
        return read(Table.Login,
         null, list,         null, null, null, null, null, null,
         null, selectObject, null, null, null, null, null, null);
    }

    static final boolean readMatch (Match selectObject, ArrayList<Match> list) {
        return read(Table.Match_,
         null, null, list,         null, null, null, null, null,
         null, null, selectObject, null, null, null, null, null);
    }

    static final boolean readMessage (Message selectObject, ArrayList<Message> list) {
        return read(Table.Message,
         null, null, null, list,         null, null, null, null,
         null, null, null, selectObject, null, null, null, null);
    }

    static final boolean readOffer (Offer selectObject, ArrayList<Offer> list) {
        return read(Table.Offer,
         null, null, null, null, list,         null, null, null,
         null, null, null, null, selectObject, null, null, null);
    }

    static final boolean readOrigin (Origin selectObject, ArrayList<Origin> list) {
        return read(Table.Origin,
         null, null, null, null, null, list,         null, null,
         null, null, null, null, null, selectObject, null, null);
    }

    static final boolean readPerson (Person selectObject, ArrayList<Person> list) {
        return read(Table.Person,
         null, null, null, null, null, null, list,         null,
         null, null, null, null, null, null, selectObject, null);
    }

    static final boolean readRouteBox (RouteBox selectObject, ArrayList<RouteBox> list) {
        return read(Table.RouteBox,
         null, null, null, null, null, null, null, list,
         null, null, null, null, null, null, null, selectObject);
    }

    static final boolean sendNewAllocationToApp (int person, int offer) {
        offerList.clear();
        WebServiceInterface.offer = new Offer();
        WebServiceInterface.offer.id = offer;
        if (!readOffer(WebServiceInterface.offer, offerList)) return false;
        personList.clear();
        WebServiceInterface.person = new Person();
        WebServiceInterface.person.id = offerList.get(0).person;
        offerList.clear();
        if (!readPerson(WebServiceInterface.person, personList)) return false;
        builder.setLength(0);
        builder.append(MESSAGE_NEW_ALLOCATION).append(S_F).append(person).append(S_F).append(offer);
        return sendToApp(personList.get(0).IP, builder.toString());
    }

    static final boolean sendNewMessageToApp (int sender, int receiver) {
        personList.clear();
        person = new Person();
        person.id = receiver;
        if (!readPerson(person, personList)) return false;
        return sendNewMessageToApp(sender, personList.get(0).IP);
    }

    static final boolean sendNewMessageToApp (int sender, String receiverIP) {
        builder.setLength(0);
        builder.append(MESSAGE_NEW_MESSAGE).append(S_F).append(sender);
        return sendToApp(receiverIP, builder.toString());
    }

    private static boolean sendToApp (String IP, String message) {
        try {
            Log.d("Roullete ip send to app", IP);
            socket = new Socket(IP, serverPort);
            outStream = new PrintStream(socket.getOutputStream());
        } catch (IOException ex) {
            Log.e("Interface", "send to app exception");
            ex.printStackTrace();
            return false;
        }
        outStream.println(message);
        return true;
    }

    private static void sendToServer () {
        serverIn.println(builder.toString());
        semaphore.acquireUninterruptibly();
        lines = response.split(S_R);
        response = "";
    }

    static final void setResponse (String response) {
        if (response.startsWith(MESSAGE_NEW_ALLOCATION)) {
            lines = response.split(S_F);
            messageInterface.receiveNewAllocation(
             Integer.parseInt(lines[1]), Integer.parseInt(lines[2]));
        } else if (response.startsWith(MESSAGE_NEW_MESSAGE)) {
            lines = response.split(S_F);
            messageInterface.receiveNewMessage(Integer.parseInt(lines[1]));
        } else {
            WebServiceInterface.response = response;
            semaphore.release();
        }
    }

    /**
     * Unwrap strings from SQL. Removes the leading and trailing "'".
     * @param s
     * @return
     */
    public static final String u (String s) {
        return s.substring(1, s.length() - 1);
    }

    static final boolean updateAllocation (Allocation selectAllocation, Allocation updatedAllocation) {
        if (updatedAllocation.isNull()) return false;
        if (!updatedAllocation.isOfferNull()) {
            offerList.clear();
            if (!readOffer(null, offerList)) return false;
            success = false;
            for (Offer o : offerList) {
                if (updatedAllocation.offer == o.id) {
                    success = true;
                    break;
                }
            }
            offerList.clear();
            if (!success) return false;
        }
        if (!updatedAllocation.isPersonNull()) {
            personList.clear();
            if (!readPerson(null, personList)) return false;
            success = false;
            for (Person p : personList) {
                if (updatedAllocation.person == p.id) {
                    success = true;
                    break;
                }
            }
            personList.clear();
            if (!success) return false;
        }
        return createUpdateDelete(
         MESSAGE_UPDATE, Table.Allocation.name(), updatedAllocation.getKeyValuePairs(),
         ((selectAllocation == null) || (selectAllocation.isNull()))
          ? SELECT_ALL : selectAllocation.getKeyValuePairs());
    }

    static final boolean updateLogin (Login selectLogin, Login updatedLogin) {
        if (updatedLogin.isNull()) return false;
        if (!updatedLogin.isPersonNull()) {
            personList.clear();
            if (!readPerson(null, personList)) return false;
            success = false;
            for (Person p : personList) {
                if (updatedLogin.person == p.id) {
                    success = true;
                    break;
                }
            }
            personList.clear();
            if (!success) return false;
        }
        return createUpdateDelete(
         MESSAGE_UPDATE, Table.Login.name(), updatedLogin.getKeyValuePairs(),
         ((selectLogin == null) || (selectLogin.isNull()))
          ? SELECT_ALL : selectLogin.getKeyValuePairs());
    }

    static final boolean updateOffer (Offer selectOffer, Offer updatedOffer) {
        if (updatedOffer.isNull(false, true)) return false;
        updatedOffer.insertionDate = new Date(System.currentTimeMillis());
        if (!updatedOffer.isPersonNull()) {
            personList.clear();
            if (!readPerson(null, personList)) return false;
            success = false;
            for (Person p : personList) {
                if (updatedOffer.person == p.id) {
                    success = true;
                    break;
                }
            }
            personList.clear();
            if (!success) return false;
        }
        Log.d("Interface", "exit update offer");
        return createUpdateDelete(
         MESSAGE_UPDATE, Table.Offer.name(), updatedOffer.getKeyValuePairs(),
         ((selectOffer == null) || (selectOffer.isNull(true, true)))
          ? SELECT_ALL : selectOffer.getKeyValuePairs());
    }

    static final boolean updateOrigin (Origin selectOrigin, Origin updatedOrigin) {
        if (updatedOrigin.isNull(false)) return false;
        if (!updatedOrigin.isPersonNull()) {
            personList.clear();
            if (!readPerson(null, personList)) return false;
            success = false;
            for (Person p : personList) {
                if (updatedOrigin.person == p.id) {
                    success = true;
                    break;
                }
            }
            personList.clear();
            if (!success) return false;
        }
        return createUpdateDelete(
         MESSAGE_UPDATE, Table.Origin.name(), updatedOrigin.getKeyValuePairs(),
         ((selectOrigin == null) || (selectOrigin.isNull(true)))
          ? SELECT_ALL : selectOrigin.getKeyValuePairs());
    }

    static final boolean updatePerson (Person selectPerson, Person updatedPerson) {
        if (updatedPerson.isNull(false)) return false;
        return createUpdateDelete(
         MESSAGE_UPDATE, Table.Person.name(), updatedPerson.getKeyValuePairs(),
         ((selectPerson == null) || (selectPerson.isNull(true)))
          ? SELECT_ALL : selectPerson.getKeyValuePairs());
    }

    /**
     * Wrap strings for SQL. Appends leading and trailing "'".
     * @param s
     * @return
     */
    public static final String w (String s) {
        return "'" + s + "'";
    }
}
