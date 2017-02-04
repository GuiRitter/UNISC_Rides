package br.com.een_gmb_gar.unisc_rides;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by eenagel on 10/25/15.
 */
public class Notify {
    private static Context context = null;
    private static int user_id = 0;
    private static int taker_user_id = 0;
    private static int sender_user_id = 0;
    private static int offer = 0;

    public static void setup(Context cntx, int usr_id) {
        Notify.context = cntx;
        Notify.user_id = usr_id;
    }

    public static int getUserID () {
        return Notify.user_id;
    }

    public static int getTakerUserID () {
        return Notify.taker_user_id;
    }

    public static int getOffer () {
        return Notify.offer;
    }

    public static void allocation(int taker_usr_id, int offer) {
        long when = System.currentTimeMillis();
        if (Notify.context == null || Notify.user_id == 0) {
            Log.e("Notify", "uninitialized data");
        }

        Notify.taker_user_id = taker_usr_id;
        Notify.offer = offer;

        Intent intent = new Intent(Notify.context, NotifySeatTakenActivity.class);
//        intent.putExtra("user_id", Notify.user_id);
//        intent.putExtra("taker_usr_id", taker_usr_id);
//        intent.putExtra("offer", offer);

        NotificationManager nm = (NotificationManager) Notify.context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pending = PendingIntent.getActivity(Notify.context, 0, intent, 0);

        Notification notification;
        notification = new Notification.Builder(Notify.context)
                .setContentTitle("Allocation")
                .setContentText("New allocation was occurred")
                .setSmallIcon(R.drawable.green_ball)
                .setContentIntent(pending).setWhen(when).setAutoCancel(true)
                .build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND;

        // do it
        nm.notify(0, notification);
    }

    public static void newMessage(int sender_usr_id) {
        Log.d("Notify", "received message. I'm " + user_id);
        Log.d("Notify", "received message. I'm " + Notify.user_id);
        Log.d("Notify", "received from " + sender_usr_id);
        long when = System.currentTimeMillis();
        if (Notify.context == null || Notify.user_id == 0) {
            Log.e("Notify", "uninitialized data");
        }

        Notify.sender_user_id = sender_usr_id;

        Intent intent = new Intent(Notify.context, ChatActivity.class);
        intent.putExtra("user_id", Notify.user_id);

        Person person = new Person();
        person.id = sender_usr_id;
        ArrayList<Person>  listPersons = new ArrayList<>();

        Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_PERSON, new Object[]{person, listPersons});

        Log.d("Notify", "rc " + Client.socket.getRc());
        if(Client.socket.getRc() && !listPersons.isEmpty()){
            intent.putExtra("peer", listPersons.get(0).getKeyValuePairs());
            UserMain.transfer_person = listPersons.get(0);

            NotificationManager nm = (NotificationManager) Notify.context.getSystemService(Context.NOTIFICATION_SERVICE);
            PendingIntent pending = PendingIntent.getActivity(Notify.context, 0, intent, 0);

            Notification notification;
            notification = new Notification.Builder(Notify.context)
                    .setContentTitle("Message")
                    .setContentText("New message received")
                    .setSmallIcon(android.R.drawable.ic_dialog_email)
                    .setContentIntent(pending).setWhen(when).setAutoCancel(true)
                    .build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.defaults |= Notification.DEFAULT_SOUND;

            // do it
            nm.notify(0, notification);
        }else{
            Log.e("Error","No person found");
        }

//        intent.putExtra("offer", offer);


    }
}
