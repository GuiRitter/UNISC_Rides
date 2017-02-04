package br.com.een_gmb_gar.unisc_rides;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback
{

    private GoogleMap map;
    private int usr_id;
    private MarkerOptions options;
    private Location location;
    Polyline line;
    Context context;
    private ArrayList<Origin> searchedOrigins;
    private Origin selectedOrigin;
    int totalDistance;


    /////////////////////////////////////////

    private LatLngBounds latlngBounds;
    private Polyline newPolyline;
    PolylineOptions rectLine;

    /////////////////////////////////////////

    LatLng startLatLng = new LatLng(-29.6976663, -52.4386775);
    LatLng endLatLng = new LatLng(-29.709996666, -52.43004418);

    private void initilizeMap() {
        if (map == null) {
            map = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();

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
        setContentView(R.layout.activity_map);

        context = MapActivity.this;

        this.location = new Location(new LatLng(-29.6976663, -52.4386775).toString());

        Bundle bundle = getIntent().getExtras();
        usr_id = (int) bundle.get("user_id");

        try {
            // Loading map
            initilizeMap();

        } catch (Exception e) {
            e.printStackTrace();
        }

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                ArrayList<LatLng> points = new ArrayList<LatLng>();
                rectLine = new PolylineOptions().width(5).color(Color.RED);

                map.clear();

                endLatLng   = new LatLng(latLng.latitude, latLng.longitude) ;

                points.add(startLatLng);
                points.add(endLatLng);

                totalDistance = 0;

                makeTheStuff(points);

            }
        });




        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        boolean passo = false;




        if ( manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {

            passo = true;
            map.setMyLocationEnabled(true);

        }

        drawMarker(passo);

        zoom();

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
        inflater.inflate(R.menu.commit_map, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        Intent intent;

        if(id == R.id.action_insert_map){
            //TODO, enviar o ponto gps


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add point");

            final EditText input = new EditText(this);

            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            builder.setView(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Origin origin = new Origin(-1, usr_id, endLatLng.latitude, endLatLng.longitude,input.getText().toString(), null, -1, null, null);

                    Client.socket.send(Client.Cmds.CMD_CREATE, Client.Tables.TB_ORIGIN, new Object[]{origin});

                    if(Client.socket.getRc()){
                        Toast.makeText(MapActivity.this, " Origin successfully registered.", Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(MapActivity.this, " Error trying to register this origin.", Toast.LENGTH_LONG).show();
                    }


                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();

        }else if(id == R.id.action_remove_map) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Remove point");

            final TextView textView = new TextView(this);

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    MapActivity.this,
                    android.R.layout.select_dialog_singlechoice);

            Origin origin = new Origin();
            origin.person = usr_id;
            ArrayList<Origin> listOrigin = new ArrayList<>();

            Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_ORIGIN, new Object[]{origin, listOrigin});

            if(Client.socket.getRc()){
                this.searchedOrigins = listOrigin;

                for(Origin ori : this.searchedOrigins){
                    arrayAdapter.add(ori.nickname);
                }
                builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);
                        AlertDialog.Builder builderInner = new AlertDialog.Builder(
                                MapActivity.this);
                        builderInner.setMessage(strName);
                        builderInner.setTitle("Are you sure that you want to remove this?");

                        MapActivity.this.selectedOrigin =  MapActivity.this.searchedOrigins.get(which);

                        builderInner.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Origin org = new Origin(MapActivity.this.selectedOrigin);
                                org.latitude = null;
                                org.longitude = null;



                                Client.socket.send(Client.Cmds.CMD_DELETE, Client.Tables.TB_ORIGIN, new Object[]{org});

                                if (Client.socket.getRc()) {
                                    Toast.makeText(MapActivity.this, "Successful removal.", Toast.LENGTH_SHORT).show();
                                }

                                dialog.dismiss();
                            }
                        });
                        builderInner.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builderInner.show();
                    }
                });

                builder.show();
            }
        }

        return super.onOptionsItemSelected(item);
    }


    private class connectAsyncTask extends AsyncTask<Void, Void, String> {
        private ProgressDialog progressDialog;
        String url;

        connectAsyncTask(String urlPass) {
            url = urlPass;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Fetching route, Please wait...");
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {

            JSONParser jParser = new JSONParser();
            String json = jParser.getJSONFromUrl(url);
            return json;

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.hide();
            if (result != null) {
                drawPath(result);
            }
        }
    }

    public String makeURL(double sourcelat, double sourcelog, double destlat,
                          double destlog) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("http://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString.append(Double.toString(sourcelog));
        urlString.append("&destination=");// to
        urlString.append(Double.toString(destlat));
        urlString.append(",");
        urlString.append(Double.toString(destlog));
        urlString.append("&sensor=false&mode=driving&alternatives=true&units=metric");
        return urlString.toString();
    }

    public class JSONParser {

        InputStream is = null;
        JSONObject jObj = null;
        String json = "";

        // constructor
        public JSONParser() {
        }

        public String getJSONFromUrl(String url) {

            // Making HTTP request
            try {
                // defaultHttpClient
                DefaultHttpClient httpClient = new DefaultHttpClient();

                HttpPost httpPost = new HttpPost(url);

                HttpResponse httpResponse = httpClient.execute(httpPost);

                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();

                try {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(is, "iso-8859-1"), 8);


                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }

                    json = sb.toString();

                    is.close();
                } catch (Exception e) {
                    Log.e("Buffer Error", "Error converting result " + e.toString());
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return json;

        }
    }

    private int getNodeIndex(NodeList nl, String nodename) {
        for(int i = 0 ; i < nl.getLength() ; i++) {
            if(nl.item(i).getNodeName().equals(nodename))
                return i;
        }
        return -1;
    }

    public void drawPath(String result) {
        if (line != null) {
            map.clear();
        }
        map.addMarker(new MarkerOptions().position(endLatLng)).setIcon((BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        map.addMarker(new MarkerOptions().position(startLatLng)).setIcon((BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        try {
            // Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes
                    .getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);



            PolylineOptions options = new PolylineOptions().width(5).color(Color.RED).geodesic(true);
            for (int z = 0; z < list.size(); z++) {
                LatLng point = list.get(z);
                options.add(point);
            }
            line = map.addPolyline(options);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    public String getDurationText (Document doc) {
        NodeList nl1 = doc.getElementsByTagName("duration");
        Node node1 = nl1.item(0);
        NodeList nl2 = node1.getChildNodes();
        Node node2 = nl2.item(getNodeIndex(nl2, "text"));
        Log.i("DurationText", node2.getTextContent());
        return node2.getTextContent();
    }

    public int getDurationValue (Document doc) {
        NodeList nl1 = doc.getElementsByTagName("duration");
        Node node1 = nl1.item(0);
        NodeList nl2 = node1.getChildNodes();
        Node node2 = nl2.item(getNodeIndex(nl2, "value"));
        Log.i("DurationValue", node2.getTextContent());
        return Integer.parseInt(node2.getTextContent());
    }

    public String getDistanceText (Document doc) {
        NodeList nl1 = doc.getElementsByTagName("distance");
        Node node1 = nl1.item(0);
        NodeList nl2 = node1.getChildNodes();
        Node node2 = nl2.item(getNodeIndex(nl2, "text"));
        Log.i("DistanceText", node2.getTextContent());
        return node2.getTextContent();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

    }


    //////////////////////////////

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

        totalDistance+=asyncTask.distance;

    }

    ///////////////////////////////
}
