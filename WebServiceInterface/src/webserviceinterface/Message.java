package webserviceinterface;

import static webserviceinterface.WebServiceInterface.S_F;
import static webserviceinterface.WebServiceInterface.u;
import static webserviceinterface.WebServiceInterface.w;

public final class Message {

    final String COLUMN_RECEIVER = "receiver";
    final String COLUMN_SENDER   = "sender";
    final String COLUMN_DATE     = "date_";
    final String COLUMN_TIME     = "time_";
    final String COLUMN_MESSAGE  = "message";

    java.sql.Date date = null;

    String message = null;

    int receiver = -1;

    int sender = -1;

    java.sql.Time time = null;

    public Message () {
    }

    public Message (int sender, int receiver,
     java.sql.Date date, java.sql.Time time, String message) {
        this.sender   = sender;
        this.receiver = receiver;
        this.date     = date;
        this.time     = time;
        this.message  = message;
    }

    public Message (Message message) {
        this.sender   = message.sender;
        this.receiver = message.receiver;
        this.date     = message.date;
        this.time     = message.time;
        this.message  = message.message;
    }

    public Message (String keyValuePairs) throws Exception {
        String fields[] = keyValuePairs.split(S_F);
        for (int i = 0; i < fields.length; i += 2) {
            switch (fields[i]) {
            case COLUMN_SENDER:
                sender = Integer.parseInt(fields[i + 1]);
            case COLUMN_RECEIVER:
                receiver = Integer.parseInt(fields[i + 1]);
            break; case COLUMN_DATE:
//                date = new java.sql.Date(SQLdateFormat.parse(u(fields[i +1])).getTime());
                date = java.sql.Date.valueOf(u(fields[i + 1]));
            break; case COLUMN_TIME:
                time = java.sql.Time.valueOf(u(fields[i + 1]));
            break; case COLUMN_MESSAGE:
                message = u(fields[i + 1]);
            break; default:
                throw new Exception();
            }
        }
    }

    final String getKeyValuePairs () {
        String returnString =
         (isSenderNull()   ? "" : (COLUMN_SENDER   + S_F + sender             + S_F)) +
         (isReceiverNull() ? "" : (COLUMN_RECEIVER + S_F + receiver           + S_F)) +
         (isDateNull()     ? "" : (COLUMN_DATE     + S_F + w(date.toString()) + S_F)) +
         (isTimeNull()     ? "" : (COLUMN_TIME     + S_F + w(time.toString()) + S_F)) +
         (isMessageNull()  ? "" : (COLUMN_MESSAGE  + S_F + w(message)         + S_F));
        return (returnString.length() < 1) ? null
         : (returnString.substring(0, returnString.length() - 1));
    }

    final boolean isDateNull () {
        return date == null;
    }

    final boolean isMessageNull () {
        return message == null;
    }

    final boolean isNull (boolean considerDate, boolean considerTime) {
        return isSenderNull() && isReceiverNull()
         && (considerDate ? isDateNull() : true)
         && (considerTime ? isTimeNull() : true) && isMessageNull();
    }

    final boolean isReceiverNull () {
        return receiver < 0;
    }

    final boolean isSenderNull () {
        return sender < 0;
    }

    final boolean isTimeNull () {
        return time == null;
    }

    @Override
    public String toString () {
        return "Message\n" +
         "Sender: "   + sender   + "\n" +
         "Receiver: " + receiver + "\n" +
         "Date: "     + date     + "\n" +
         "Time: "     + time     + "\n" +
         "Message: "  + message  + "\n";
    }
}
