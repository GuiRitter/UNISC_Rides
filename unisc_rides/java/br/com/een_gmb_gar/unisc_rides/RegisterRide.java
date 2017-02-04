package br.com.een_gmb_gar.unisc_rides;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class RegisterRide extends Activity {
    private Spinner sp_shifts, sp_locations;
    private String selected_day, selected_shift;
    private ListView your_rides;
    private ArrayAdapter<CharSequence> adapter;
    private int usr_id;
    private Integer register_id;
    private ArrayList<MySeatData> actualOffers;
    private ArrayList<String> arrlist_sp_locations;

    private ArrayList<Origin> origin_list;
    private int selectedOrigin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("RegisterRide, onCreate", "enter");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_ride);

        register_id = null;

        // get user ID
        Bundle bundle = getIntent().getExtras();
        this.usr_id = (int) bundle.get("user_id");

        this.sp_locations = (Spinner) findViewById(R.id.sp_locations);
        sp_locations.setOnItemSelectedListener(new on_origin_selected());

        // insert day shifts in the spinner
        sp_shifts = (Spinner) findViewById(R.id.sp_shifts);
        this.adapter = ArrayAdapter.createFromResource(this, R.array.day_shift, android.R.layout.simple_spinner_dropdown_item);
        this.adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_shifts.setAdapter(this.adapter);
        sp_shifts.setOnItemSelectedListener(new on_shift_selected());

        ((EditText) findViewById(R.id.rng_distance)).setText("0.2");

        your_rides = (ListView) findViewById(R.id.listView_your_rides);
        your_rides.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                              @Override
                                              public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                                                  Offer selected_offer = actualOffers.get(position).offer;
                                                  register_id = selected_offer.id;
                                                  if(selected_offer.shift == Offer.Shift.Morning)
                                                        sp_shifts.setSelection(0);
                                                  else if(selected_offer.shift == Offer.Shift.Afternoon)
                                                        sp_shifts.setSelection(1);
                                                  else
                                                        sp_shifts.setSelection(2);

                                                  ((CheckBox) findViewById(R.id.select_monday)).setChecked((selected_offer.weekDays.contains(Offer.WeekDay.MONDAY)));
                                                  ((CheckBox) findViewById(R.id.select_friday)).setChecked((selected_offer.weekDays.contains(Offer.WeekDay.FRIDAY)));
                                                  ((CheckBox) findViewById(R.id.select_saturday)).setChecked((selected_offer.weekDays.contains(Offer.WeekDay.SATURDAY)));
                                                  ((CheckBox) findViewById(R.id.select_thursday)).setChecked((selected_offer.weekDays.contains(Offer.WeekDay.THURSDAY)));
                                                  ((CheckBox) findViewById(R.id.select_tuesday)).setChecked((selected_offer.weekDays.contains(Offer.WeekDay.TUESDAY)));
                                                  ((CheckBox) findViewById(R.id.select_wednesday)).setChecked((selected_offer.weekDays.contains(Offer.WeekDay.WEDNESDAY)));

                                                  // is promiscuous?
                                                  ((CheckBox) findViewById(R.id.is_promiscuous)).setChecked((selected_offer.promiscuous));

                                                  EditText seat = (EditText) findViewById(R.id.av_seats);
                                                  seat.setText(((int)selected_offer.availableSeats)+"");
                                              }
                                          });
        your_rides.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id ){
                final int RegPosition = position;
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterRide.this);
                builder.setMessage("Are you sure to delete this ride?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Offer offer = new Offer();

                                offer.id = actualOffers.get(RegPosition).offer.id;
                                Client.socket.send(Client.Cmds.CMD_DELETE, Client.Tables.TB_OFFER, new Object[] {offer});

                                if (Client.socket.getRc()) {
                                    Toast.makeText(RegisterRide.this, "Your offer was deleted!", Toast.LENGTH_LONG).show();

                                    loadRides();
                                }
                                else {
                                    Toast.makeText(RegisterRide.this, "Error trying to delete your offer.", Toast.LENGTH_LONG).show();
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
        loadRides();
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
            sp_locations = (Spinner) findViewById(R.id.sp_locations);
            strRouletteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sp_locations.setAdapter(strRouletteAdapter);

            sp_locations.setOnItemSelectedListener(new on_origin_selected());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRides();
        load_locations();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        actualOffers = new ArrayList<>();
//        loadRides();
    }

    private void loadRides(){

        ArrayList<Offer> newOffers = new ArrayList<>();
        register_id = null;
        Offer offer = new Offer();
        offer.person = usr_id;

        Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_OFFER, new Object[]{offer, newOffers});

        if(Client.socket.getRc()){
            // overwrite the list view with the read data
            this.actualOffers.clear();

            Person person = new Person();
            person.id = usr_id;

            ArrayList<Person> listPerson = new ArrayList<>();

            Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_PERSON, new Object[]{person, listPerson});

            if(Client.socket.getRc() && !listPerson.isEmpty()){

                for(Offer of: newOffers){
                    MySeatData msd = new MySeatData();
                    msd.offer = of;
                    msd.person = listPerson.get(0);
                    this.actualOffers.add(msd);
                }
            }

        }
        final DataAdapter stable_adapter = new DataAdapter(this, actualOffers);
        your_rides.setAdapter(stable_adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // insert items in the activity bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.commit_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ArrayList<Offer.WeekDay> week_days_list = new ArrayList<>();

        if (((CheckBox) findViewById(R.id.select_monday)).isChecked()) {
            week_days_list.add(Offer.WeekDay.MONDAY);
        }
        if (((CheckBox) findViewById(R.id.select_tuesday)).isChecked()) {
            week_days_list.add(Offer.WeekDay.TUESDAY);
        }
        if (((CheckBox) findViewById(R.id.select_wednesday)).isChecked()) {
            week_days_list.add(Offer.WeekDay.WEDNESDAY);
        }
        if (((CheckBox) findViewById(R.id.select_thursday)).isChecked()) {
            week_days_list.add(Offer.WeekDay.THURSDAY);
        }
        if (((CheckBox) findViewById(R.id.select_friday)).isChecked()) {
            week_days_list.add(Offer.WeekDay.FRIDAY);
        }
        if (((CheckBox) findViewById(R.id.select_saturday)).isChecked()) {
            week_days_list.add(Offer.WeekDay.SATURDAY);
        }

        Spinner shift;
        Offer.Shift shift_code;
        boolean promiscuous;
        int seats = 1;
        double range = 0.2;

        if (week_days_list.isEmpty()) {
            Toast.makeText(this, "Select at least one day.", Toast.LENGTH_LONG);
            return false;
        }
        shift = (Spinner) findViewById(R.id.sp_shifts);
        switch (shift.getSelectedItemPosition()) {
            case 0: // morning
                shift_code = Offer.Shift.Morning;
                break;
            case 1: // afternoon
                shift_code = Offer.Shift.Afternoon;
                break;
            case 2: // night
                shift_code = Offer.Shift.Night;
                break;
            default:
                Log.d("getDayShift", "unexpected shift code");
                return false;
        }
        promiscuous = ((CheckBox) findViewById(R.id.is_promiscuous)).isChecked();
        try {
            seats = Integer.parseInt(((EditText) findViewById(R.id.av_seats)).getText().toString());
        } catch (Exception e) {
            Log.d("getSeatsCount", "no seats value");
            Toast.makeText(this, "Insert a number of available seats!", Toast.LENGTH_LONG).show();
            return false;
        }
        try {
            range = Double.parseDouble(((EditText) findViewById(R.id.rng_distance)).getText().toString());
        } catch (Exception e) {
            Log.d("getRangeCount", "no range value");
            Toast.makeText(this, "Insert a range to match!", Toast.LENGTH_LONG).show();
            return false;
        }

        if (seats < 1 || range < 0.0) {
            Toast.makeText(this, "Invalid number of seats available!", Toast.LENGTH_LONG).show();
            Log.d("getSeatsCount", "invalid seats value");
            return false;
        }
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_insert_ride:
                Log.d("SelectedOrigin",selectedOrigin+"");
                Offer offer = new Offer(-1, usr_id, origin_list.get(this.selectedOrigin).id, range, (byte) seats, (byte) seats, week_days_list, shift_code, null, promiscuous, true);

                Client.socket.send(Client.Cmds.CMD_CREATE, Client.Tables.TB_OFFER, new Object[] {offer});

                if (Client.socket.getRc()) {
                    Toast.makeText(this, "Your offer was added!", Toast.LENGTH_LONG).show();
                    loadRides();
                }
                else {
                    Toast.makeText(this, "Error trying to add your offer.", Toast.LENGTH_LONG).show();
                }
                register_id = null;
                return true;

            case R.id.action_update_ride:

                if(register_id == null){
                    Toast.makeText(this, "Select a ride!", Toast.LENGTH_LONG).show();
                    return true;
                }
                Offer offer1 = new Offer();
                offer1.id = register_id;
                Offer offer2 = new Offer(-1, usr_id,origin_list.get(selectedOrigin).id, range, (byte) seats, (byte) seats, week_days_list, shift_code, null, promiscuous, true);

                Client.socket.send(Client.Cmds.CMD_UPDATE, Client.Tables.TB_OFFER, new Object[] {offer1, offer2});

                if (Client.socket.getRc()) {
                    Toast.makeText(this, "Your offer was updated!", Toast.LENGTH_LONG).show();

                    loadRides();
                }
                else {
                    Toast.makeText(this, "Error trying to add your offer.", Toast.LENGTH_LONG).show();
                }

                register_id = null;
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class on_shift_selected implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            selected_shift = adapterView.getItemAtPosition(i).toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            selected_shift = null;
        }
    }

    private void load_locations() {
        arrlist_sp_locations = new ArrayList<>();

        Origin origin = new Origin();
        origin.person = this.usr_id;

        this.origin_list = new ArrayList<>();
        Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_ORIGIN, new Object[]{origin, this.origin_list});

        if (Client.socket.getRc()) {

            for(Origin o : this.origin_list ){
                arrlist_sp_locations.add(o.nickname);
            }
        }

        ArrayAdapter<String> adp = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrlist_sp_locations);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.sp_locations.setAdapter(adp);
    }
    private class on_origin_selected implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int idx, long l) {
            RegisterRide.this.selectedOrigin = idx;
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            RegisterRide.this.selectedOrigin = 0;
        }
    }
}