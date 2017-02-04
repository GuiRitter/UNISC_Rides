package br.com.een_gmb_gar.unisc_rides;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;

public class NotifySeatTakenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify_seat_taken);
        setTitle("Seat Taker Info");

//        Bundle bundle = getIntent().getExtras();
//        Log.d("NotifyActivity", "bundle usr_id = " + bundle.get("user_id"));
//        int usr_id = (int) bundle.get("user_id");
//        int taker_usr_id = (int) bundle.get("taker_usr_id");
//        int offer_id = (int) bundle.get("offer");
        int usr_id = Notify.getUserID();
        int taker_usr_id = Notify.getTakerUserID();
        int offer_id = Notify.getOffer();
        UserMain.setUserID(usr_id);

        Person person = new Person();
        person.id = taker_usr_id;

        Offer offer = new Offer();
        offer.id = offer_id;

        ArrayList<Person> personList = new ArrayList<>();
        ArrayList<Offer> offerList = new ArrayList<>();

        Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_PERSON, new Object[]{person, personList});
        Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_OFFER, new Object[]{offer, offerList});

        //TODO checks
        TextView text = (TextView) findViewById(R.id.notify_seat_taken_person_info);
        person = personList.get(0);
        offer = offerList.get(0);
        text.setText("Name:\n" + person.name + "\n\nPhone:\n" + person.phone + "\n\nEmail:\n" + person.email + "\n\nWeek days:\n" + offer.weekDays + "\n\nShift:\n" + offer.shift);
    }
}
