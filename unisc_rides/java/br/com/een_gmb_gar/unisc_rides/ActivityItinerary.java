package br.com.een_gmb_gar.unisc_rides;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActivityItinerary extends FragmentActivity implements OnMapReadyCallback {

    private MapFragment mMapFragment;
    private GoogleMap map;
    private int usr_id;
    private Marker marker;
    private MarkerOptions options;
    private Location location;
    Polyline line;
    Context context;
    private LatLngBounds latlngBounds;
    private Polyline newPolyline;
    private boolean isTravelingToParis = false;
    PolylineOptions rectLine;
    private ArrayList<Offer> actualOffers;
    private ArrayList<PointData> actualOrigins;

    private int index = 0;

    LatLng startLatLng = new LatLng(-29.6976663, -52.4386775);
    LatLng endLatLng = new LatLng(-29.709996666, -52.43004418);

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    private void initilizeMap() {
        if (map == null) {
            map = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.mapItinerary)).getMap();

            // check if map is created successfully or not
            if (map == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_itinerary);

        context = ActivityItinerary.this;

        this.location = new Location(new LatLng(-29.6976663, -52.4386775).toString());

        Bundle bundle = getIntent().getExtras();
        usr_id = (int) bundle.get("user_id");

    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            // Loading map
            initilizeMap();

        } catch (Exception e) {
            e.printStackTrace();
        }

        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            map.setMyLocationEnabled(true);

        }

        pickTheStuff();
        if(!this.actualOffers.isEmpty())
            pickUpAllocation(this.actualOffers.get(index));

        zoom();
    }

    void pickTheStuff(){

        Offer offer = new Offer();
        offer.person = usr_id;

        ArrayList<Offer> listOffers = new ArrayList<>();

        Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_OFFER, new Object[]{offer, listOffers});


        if(Client.socket.getRc()){

            this.actualOffers = new ArrayList<>();
            for(Offer of: listOffers){
                if(of.availableSeats!=of.remainingSeats)
                    this.actualOffers.add(of);
            }


            if(listOffers.isEmpty()){
                Toast.makeText(this, "You don't have an itinerary!", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(this, UserMain.class);
                intent.putExtra("user_id", usr_id);
                startActivity(intent);
            }

        }

        for(Offer of : this.actualOffers){
            Log.d("listOffers", "id "+of.id);
        }

    }

    void pickUpAllocation(Offer offer){

        map.clear();

        Allocation aloc = new Allocation();
        ArrayList<Allocation> listAllocation = new ArrayList<>();
        actualOrigins = new ArrayList<>();
        rectLine = new PolylineOptions().width(5).color(Color.RED);
        aloc.offer = offer.id;
        aloc.status = Allocation.Status.APPROVED;

        Toast.makeText(this, " Shift: "+offer.shift+" Day: " +offer.weekDays, Toast.LENGTH_LONG).show();

        Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_ALLOCATION, new Object[]{aloc, listAllocation});

        if(Client.socket.getRc()){

            ArrayList<Origin> listOrigin = new ArrayList<>();

            Origin origin = new Origin();

            origin.id = offer.origin;

            Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_ORIGIN, new Object[]{origin, listOrigin});

            if(!Client.socket.getRc()){
                Toast.makeText(this, "Error getting your origin.", Toast.LENGTH_SHORT).show();
            }else{
                PointData pd = new PointData();
                pd.origin = listOrigin.get(listOrigin.size()-1);

                ArrayList<Person> listPerson = new ArrayList<>();
                Person person = new Person();
                person.id = usr_id;

                Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_PERSON, new Object[]{person, listPerson});

                if(Client.socket.getRc() && !listPerson.isEmpty()){
                    pd.person = listPerson.get(0);
                    this.actualOrigins.add(pd);
                    Log.d("addingPoint", "rider point added.");
                }else{
                    Toast.makeText(this, "Error adding the actual user point.", Toast.LENGTH_SHORT).show();
                }

                for(Allocation alo : listAllocation){
                    origin.id = alo.origin;

                    Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_ORIGIN, new Object[]{origin, listOrigin});

                    if(!Client.socket.getRc()){
                        Toast.makeText(this, "Error getting one of the origins.", Toast.LENGTH_SHORT).show();
                    }else{
                        pd = new PointData();
                        pd.origin = listOrigin.get(listOrigin.size()-1);

                        person = new Person();
                        listPerson = new ArrayList<>();
                        person.id = alo.person;

                        Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_PERSON, new Object[]{person, listPerson});
                        if(Client.socket.getRc() && !listPerson.isEmpty()){
                            pd.person = listPerson.get(0);
                            this.actualOrigins.add(pd);
                            Log.d("addingPoint", "user point added.");
                        }
                    }
                }
            }

            PointData pd = new PointData();
            Person person = new Person();
            person.name = "UNISC";
            pd.person = person;

            Origin origin1= new Origin();
            origin1.latitude = -29.6976663;
            origin1.longitude= -52.4386775;
            pd.origin = origin1;

            this.actualOrigins.add(pd);
            getNewOrderedPoints();

        }else{
            Toast.makeText(this, "Error getting your offers.", Toast.LENGTH_SHORT).show();
        }

    }


    void getNewOrderedPoints() {

        PointData rider = this.actualOrigins.get(0);
        PointData unisc = this.actualOrigins.get(this.actualOrigins.size() - 1);

        this.actualOrigins.remove(0);
        this.actualOrigins.remove(this.actualOrigins.size() - 1);

        //Add the guy who will give the ride first
        map.addMarker(new MarkerOptions().position(new LatLng(rider.origin.latitude.doubleValue(), rider.origin.longitude.doubleValue()))
                .title(rider.person.name))
                .setIcon((BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        endLatLng = new LatLng(rider.origin.latitude.doubleValue(), rider.origin.longitude.doubleValue());


        Double coefficient = null;

        while (this.actualOrigins.size() != 0) {

            startLatLng = endLatLng;
            int index = 0;
            // for(PointData pd : this.actualOrigins){
            for (int x = 0; x < this.actualOrigins.size(); x++) {
                Double newCoef = (Math.abs(this.actualOrigins.get(x).origin.latitude - endLatLng.latitude)) + (Math.abs(this.actualOrigins.get(x).origin.longitude - endLatLng.longitude));

                if (coefficient == null || newCoef < coefficient) {
                    coefficient = newCoef;
                    index = x;
                }
            }

            endLatLng = new LatLng(this.actualOrigins.get(index).origin.latitude.doubleValue(), this.actualOrigins.get(index).origin.longitude.doubleValue());
            map.addMarker(new MarkerOptions().position(endLatLng)
                    .title(this.actualOrigins.get(index).person.name))
                    .setIcon((BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            this.actualOrigins.remove(index);

            findDirections(startLatLng.latitude, startLatLng.longitude,
                    endLatLng.latitude, endLatLng.longitude, GMapV2Direction.MODE_DRIVING);

        }

        startLatLng = endLatLng;

        //Add the guy who will give the ride first
        map.addMarker(new MarkerOptions().position(new LatLng(unisc.origin.latitude.doubleValue(), unisc.origin.longitude.doubleValue()))
                .title(unisc.person.name))
                .setIcon((BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        endLatLng = new LatLng(unisc.origin.latitude.doubleValue(), unisc.origin.longitude.doubleValue());

        findDirections(startLatLng.latitude, startLatLng.longitude,
                endLatLng.latitude, endLatLng.longitude, GMapV2Direction.MODE_DRIVING);
    }

    void makeTheStuff(ArrayList<LatLng> points){

        for(int x = 0; x< points.size(); x++){

            map.addMarker(new MarkerOptions().position(points.get(x))).setIcon((BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

            if(x != 0)
                findDirections(points.get(x-1).latitude, points.get(x-1).longitude,
                        points.get(x).latitude, points.get(x).longitude, GMapV2Direction.MODE_DRIVING );

        }

    }

    private void zoom(){
        CameraPosition cameraPosition = new CameraPosition.Builder().target(
                new LatLng(-29.6976663, -52.4386775)).zoom(13).build();

        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void drawMarker(boolean passou){
        // Remove any existing markers on the map
        map.clear();

        LatLng newPosition = new LatLng(-29.6976663, -52.4386775);

        options = new MarkerOptions()
                .position(newPosition)
                .snippet("Lat:" + newPosition.latitude + "Lng:" + newPosition.longitude)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
//                    .draggable(true)
                .title("ME");

        map.addMarker(options);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.itinerary_map, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        Intent intent;

        if(id == R.id.map_backward){
            if(index > 0){
                index--;
                pickUpAllocation(this.actualOffers.get(index));
            }else{
                Toast.makeText(this, "No more data.",Toast.LENGTH_LONG);
            }


        }else if(id == R.id.map_forward){
            if(this.index+1 < this.actualOffers.size()){
                index++;
                pickUpAllocation(this.actualOffers.get(index));
            }else{
                Toast.makeText(this, "No more data.", Toast.LENGTH_LONG);
            }

        }

        return super.onOptionsItemSelected(item);
    }

    public void findDirections(double fromPositionDoubleLat, double fromPositionDoubleLong, double toPositionDoubleLat, double toPositionDoubleLong, String mode)
    {

        Map<String, String> map = new HashMap<String, String>();
        map.put(GetDirectionsAsyncTask.USER_CURRENT_LAT, String.valueOf(fromPositionDoubleLat));
        map.put(GetDirectionsAsyncTask.USER_CURRENT_LONG, String.valueOf(fromPositionDoubleLong));
        map.put(GetDirectionsAsyncTask.DESTINATION_LAT, String.valueOf(toPositionDoubleLat));
        map.put(GetDirectionsAsyncTask.DESTINATION_LONG, String.valueOf(toPositionDoubleLong));
        map.put(GetDirectionsAsyncTask.DIRECTIONS_MODE, mode);

        GetDirectionsAsyncTask asyncTask = new GetDirectionsAsyncTask(this);
        asyncTask.execute(map);

    }

    public void handleGetDirectionsResult(ArrayList<LatLng> directionPoints) {

        for(int i = 0 ; i < directionPoints.size() ; i++)
        {
            rectLine.add(directionPoints.get(i));
        }
        if (newPolyline != null)
        {
            newPolyline.remove();
        }
        newPolyline = map.addPolyline(rectLine);

        latlngBounds = createLatLngBoundsObject(startLatLng, endLatLng);

    }

    private LatLngBounds createLatLngBoundsObject(LatLng firstLocation, LatLng secondLocation)
    {
        if (firstLocation != null && secondLocation != null)
        {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(firstLocation).include(secondLocation);

            return builder.build();
        }
        return null;
    }

}
