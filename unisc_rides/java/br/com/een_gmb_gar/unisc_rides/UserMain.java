package br.com.een_gmb_gar.unisc_rides;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class UserMain extends Activity {
    private Spinner sp_days, sp_shifts, sp_origins;
    private String selected_day, selected_shift;
    private ListView avaible_rides;
    static int usr_id;
//    private Person me;
    private ArrayList<Offer> actualOffers;
    private ArrayList<Match> actualMatches;
    private ArrayList<Person> actualPersons;
    private ArrayList<MatchData> actualMatchData;

    private ArrayList<MatchData> filteredOffers;
    private int selectedDay;
    private int selectedShift;

    private ArrayList<Origin> origin_list;
    private int selectedOrigin;
    static Person transfer_person; // gambiarra

    public static void setUserID (int usr_id) {
        UserMain.usr_id = usr_id;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        actualMatchData = new ArrayList<>();
        actualOffers = new ArrayList<>();
        actualPersons = new ArrayList<>();
        filteredOffers = new ArrayList<>();

        // get user ID
        Bundle bundle = getIntent().getExtras();
        usr_id = (int) bundle.get("user_id");

        Notify.setup(UserMain.this, usr_id);

        // update my IP address into the webservice for future notifications
        Person person_id = new Person(), person_ip = new Person();
        person_id.id = usr_id;
        person_ip.IP = Client.IP;
        Client.socket.send(Client.Cmds.CMD_UPDATE, Client.Tables.TB_PERSON, new Object[]{person_id, person_ip});

        // insert weeks in the spinner
        sp_days = (Spinner) findViewById(R.id.sp_filter_day);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.week_days, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_days.setAdapter(adapter);
        sp_days.setOnItemSelectedListener(new on_day_selected());

        // insert day shifts in the spinner
        sp_shifts = (Spinner) findViewById(R.id.sp_filter_shift);
        adapter = ArrayAdapter.createFromResource(this, R.array.day_shift, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_shifts.setAdapter(adapter);
        sp_shifts.setOnItemSelectedListener(new on_shift_selected());

        // TODO, handle here the actions on selecting a element of listview
        avaible_rides = (ListView) findViewById(R.id.listView_avaible_rides);
        avaible_rides.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserMain.this);
                builder.setMessage("Are you sure to request this ride?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                MatchData matchdata = actualMatchData.get(position);
                                Allocation alloc = new Allocation(usr_id, matchdata.origin.id, matchdata.offer.id, (matchdata.offer.promiscuous ? Allocation.Status.APPROVED : Allocation.Status.PENDING));

                                Client.socket.send(Client.Cmds.CMD_CREATE, Client.Tables.TB_ALLOCATION, new Object[]{alloc});

                                if (Client.socket.getRc()) {
                                    Offer offer = new Offer();
                                    offer.remainingSeats = (byte) (matchdata.offer.remainingSeats - 1);

                                    Offer offer_id = new Offer();
                                    offer_id.id = matchdata.offer.id;

                                    Log.d("UserMain", "before update offer");
                                    Client.socket.send(Client.Cmds.CMD_UPDATE, Client.Tables.TB_OFFER, new Object[]{offer_id, offer});
                                    Log.d("UserMain", "after update offer, rc = " + Client.socket.getRc());

                                    if (Client.socket.getRc()) {
                                        if (matchdata.offer.promiscuous) {
                                            Toast.makeText(UserMain.this, "The offer was taken.", Toast.LENGTH_LONG).show();
                                            Client.socket.send(Client.Cmds.CMD_MESSAGE_NEW_ALLOCATION, null, new Object[]{alloc.person, alloc.offer});
                                            loadMatches();
                                        } else {
                                            Toast.makeText(UserMain.this, "The request has been sent to approval.", Toast.LENGTH_LONG).show();
                                        }
                                    } else if (Client.socket.getRc()) {
                                        Toast.makeText(UserMain.this, "You already have this seat!", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(UserMain.this, "Error taking the offer.", Toast.LENGTH_LONG).show();
                                    }
                                }else{
                                    Toast.makeText(UserMain.this, "Error trying to consume a seat.", Toast.LENGTH_LONG).show();
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
            }
        });

        this.avaible_rides.setOnScrollListener(new AbsListView.OnScrollListener() {
            boolean up_scroll = false;
            int cnt_up = 0;

            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                if (i == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (up_scroll) {
                        cnt_up++;
                        if (cnt_up == 2) {
                            loadMatches();
                            cnt_up = 1;
                        }
                    } else {
                        cnt_up = 0;
                    }
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCnt, int totalItemCnt) {
                up_scroll = firstVisibleItem == 0;
            }
        });

    }

    private void load_origins(){
        Origin origin = new Origin();
        origin.person = this.usr_id;

        this.origin_list = new ArrayList<>();
        Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_ORIGIN, new Object[]{origin, this.origin_list});

        if (Client.socket.getRc()) {
            ArrayList<String> strRoulette = new ArrayList<>();

            for (Origin o: this.origin_list) {
                strRoulette.add(o.nickname);

            }
            ArrayAdapter<String> strRouletteAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, strRoulette);
            sp_origins = (Spinner) findViewById(R.id.actUsrMain_sp_origin);
            strRouletteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sp_origins.setAdapter(strRouletteAdapter);

            sp_origins.setOnItemSelectedListener(new on_origin_selected());

            if(origin_list.size() == 0){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("No origins found. We are redirecting you to register a new origin.");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(UserMain.this, MapActivity.class);
                        intent.putExtra("user_id", usr_id);
                        startActivity(intent);

                    }
                });
                AlertDialog alert = builder.create();
                alert.show();

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        load_origins();
        loadPersons();
        loadMatches();
    }

    @Override
    protected void onPause() {
        super.onPause();
        actualOffers = new ArrayList<>();
        loadMatches();
    }

    private void loadPersons(){

        this.actualPersons = new ArrayList<>();

        Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_PERSON, new Object[] {null, actualPersons});
    }

    private void loadMatches() {
        ArrayList<Match> matches = new ArrayList<>();
        if ( !(origin_list == null) && !origin_list.isEmpty() ){
            //Match with selected origin
            Match match = new Match(-1, origin_list.get(selectedOrigin).id, true);
            //Read the match list to this origin
            Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_MATCH, new Object[] {match, matches});
            //If it get a message back
            if(Client.socket.getRc()){
                //Store the matches searched
                this.actualMatches = matches;
                actualOffers = new ArrayList<>();

                //diz o gui que brilha sem definhaaar
                //Annalise all the matches
                for(Match m : matches){

                    Offer offer = new Offer();
                    ArrayList<Offer> listOffer = new ArrayList<>();
                    offer.id = m.offer;
                    //you shall not pass on the loop
                    boolean pass = false;
                    //Search if the desired offer is on the list
                    for(Offer off : this.actualOffers){
                        if(off.id == m.offer) {
                            pass = true;
                            break;
                        }
                    }
                    //If we don't have that offer
                    if(!pass) {
                        Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_OFFER, new Object[]{offer, listOffer});

                        if (Client.socket.getRc() && !listOffer.isEmpty()) {
                            //Add the searched offer
                            this.actualOffers.add(listOffer.get(0));
                        }
                        else {
                            Toast.makeText(this, "Error getting data.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
            else {
                //As Faustão says, erro
                Toast.makeText(this, "Error getting actual data.", Toast.LENGTH_LONG).show();
            }

        }

        actualMatchData = new ArrayList<>();

        //Crawl the matches list again...
        for (Match m : matches){
            //Data component to go on the list adapter
            MatchData md = new MatchData();
            md.match = m;
            //Search for the match's offer
            for(Offer off : this.actualOffers){
                if(m.offer == off.id) {
                    md.offer = off;
                    break;
                }
            }
            //Get the selected origin
            md.origin = origin_list.get(selectedOrigin);
            //Find the person of this match
            for(Person person : this.actualPersons){
                if(md.offer.person == person.id) {
                    md.person = person;
                    break;
                }
            }

            Allocation aloc = new Allocation();
            ArrayList<Allocation> listAlloc = new ArrayList<>();
            aloc.person = usr_id;
            aloc.offer = md.offer.id;

            Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_ALLOCATION, new Object[]{aloc, listAlloc});

            if(Client.socket.getRc() && listAlloc.isEmpty()){
                if(md.person.id != usr_id && isDay(md.offer) && isShifty(md.offer) && md.offer.enabled && md.offer.remainingSeats > 0 ) {
                    //Add the match data, so the adapter can use it
                    this.actualMatchData.add(md);
                }
            }

        }
        //Set the adapter with the data found
        final OfferDataAdapter stable_adapter = new OfferDataAdapter(this, actualMatchData);
        stable_adapter.setActivity(this);
        avaible_rides.setAdapter(stable_adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // insert items in the activity bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_add_ride:
                intent = new Intent(this, RegisterRide.class);
                intent.putExtra("user_id", this.usr_id);
                startActivity(intent);
                return true;
            case R.id.action_show_my_seats:
                intent = new Intent(this, MySeats.class);
                intent.putExtra("user_id", this.usr_id);
                startActivity(intent);
                return true;
            case R.id.action_chat:
                intent = new Intent(this, MyChats.class);
                intent.putExtra("user_id", this.usr_id);
                startActivity(intent);
                return true;
            case R.id.action_add_rote:
                intent = new Intent(this, MapActivity.class);
                intent.putExtra("user_id", this.usr_id);
                startActivity(intent);
                return true;
            case R.id.action_see_itinerary:
                intent = new Intent(this, ActivityItinerary.class);
                intent.putExtra("user_id", this.usr_id);
                startActivity(intent);


            /*
            case R.id.action_settings:
                openSettings();
                return true;
            */
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class on_day_selected implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int idx, long l) {
            UserMain.this.selectedDay = idx;
            loadMatches();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            selected_day = null;
        }
    }

    private class on_shift_selected implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int idx, long l) {
            UserMain.this.selectedShift = idx;
            loadMatches();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            selected_shift = null;
        }
    }

    private class on_origin_selected implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int idx, long l) {
            UserMain.this.selectedOrigin = idx;
            loadMatches();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            selected_day = null;
        }
    }


    private boolean isDay(Offer offer){
        switch (selectedDay){
            case 0:
                return offer.weekDays.contains(Offer.WeekDay.MONDAY);
            case 1:
                return offer.weekDays.contains(Offer.WeekDay.TUESDAY);
            case 2:
                return offer.weekDays.contains(Offer.WeekDay.WEDNESDAY);
            case 3:
                return offer.weekDays.contains(Offer.WeekDay.THURSDAY);
            case 4:
                return offer.weekDays.contains(Offer.WeekDay.FRIDAY);
            case 5:
                return offer.weekDays.contains(Offer.WeekDay.SATURDAY);
            default:
                return true;
        }
    }

    private boolean isShifty(Offer offer){
        switch (selectedShift){
            case 0:
                return offer.shift == Offer.Shift.Morning;
            case 1:
                return offer.shift == Offer.Shift.Afternoon;
            case 2:
                return offer.shift == Offer.Shift.Night;
            default:
                return true;
        }
    }
}