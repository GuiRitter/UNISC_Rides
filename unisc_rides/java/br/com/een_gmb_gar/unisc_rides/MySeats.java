package br.com.een_gmb_gar.unisc_rides;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MySeats extends Activity {
    private int usr_id;
    private ListView my_seats;
    private ArrayList<MySeatData> actualOffers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_seats);

        // get user ID
        Bundle bundle = getIntent().getExtras();
        this.usr_id = (int) bundle.get("user_id");

        // get list view reference
        my_seats = (ListView) findViewById(R.id.listView_my_steats);

        my_seats.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int RegPosition = position;
                AlertDialog.Builder builder = new AlertDialog.Builder(MySeats.this);
                builder.setMessage("Are you sure to delete this ride?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // TODO
                                Allocation allocation = new Allocation();

                                allocation.offer = actualOffers.get(RegPosition).offer.id;
                                allocation.person = usr_id;

                                Client.socket.send(Client.Cmds.CMD_DELETE, Client.Tables.TB_ALLOCATION, new Object[]{allocation});

                                Log.d("ERROU", "id " + Client.socket.getRc());
                                if (Client.socket.getRc()) {

                                    Offer offer = new Offer();//actualOffers.get(RegPosition);
                                    Offer offer2 = new Offer();

                                    offer2.id = actualOffers.get(RegPosition).offer.id;
                                    offer.remainingSeats = (byte) (actualOffers.get(RegPosition).offer.remainingSeats + 1);

                                    Client.socket.send(Client.Cmds.CMD_UPDATE, Client.Tables.TB_OFFER, new Object[]{offer2, offer});

                                    if (Client.socket.getRc()) {
                                        Toast.makeText(MySeats.this, "Your seat was released!", Toast.LENGTH_LONG).show();
                                    }else{
                                        Toast.makeText(MySeats.this, "Error trying to release your offer.", Toast.LENGTH_LONG).show();
                                    }

                                    load_my_seats();
                                } else {
                                    Toast.makeText(MySeats.this, "Error trying to release your offer.", Toast.LENGTH_LONG).show();
                                }

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();


                return true;
            }
        });


        actualOffers = new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        load_my_seats();
    }

    private void load_my_seats() {
        Allocation alloc = new Allocation(this.usr_id, -1,-1 , null);
        ArrayList <Allocation> alloc_list = new ArrayList<>();

        Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_ALLOCATION, new Object[] {alloc, alloc_list});
        if(!Client.socket.getRc()){
            Toast.makeText(this, "Error reading your allocations from database.", Toast.LENGTH_LONG).show();
            return;
        }

        ArrayList <Offer> offer_list = new ArrayList<>();

        Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_OFFER, new Object[] {null, offer_list});

        if(!Client.socket.getRc()){
            Toast.makeText(this, "Error reading all offers from database.", Toast.LENGTH_LONG).show();
            return;
        }

        actualOffers.clear();
        for(int x=0; x < alloc_list.size();x++){
            for(int y=0; y < offer_list.size(); y++){
                if(offer_list.get(y).id == alloc_list.get(x).offer){

                    Person person = new Person();
                    person.id = offer_list.get(y).person;

                    ArrayList<Person> listPerson = new ArrayList<>();

                    Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_PERSON, new Object[]{person, listPerson});

                    if(Client.socket.getRc() && !listPerson.isEmpty()){
                        MySeatData sd = new MySeatData();
                        sd.offer = offer_list.get(y);
                        sd.person = listPerson.get(0);

                        actualOffers.add(sd);

                    }
                }
            }
        }

        // update the list of seats
        final DataAdapter adapter = new DataAdapter(this, actualOffers);
        my_seats.setAdapter(adapter);
    }
}
