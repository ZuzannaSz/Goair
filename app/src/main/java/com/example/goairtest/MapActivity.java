package com.example.goairtest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapActivity extends Activity implements LocationListener {
    private Button login, home, settings;
    private TextView loginText;
    private FirebaseAuth auth;
    private MapView map = null;
    private IMapController mapController;
    private LocationManager locationManager;
    private Location location =null;
    private boolean locationOn;
    private FirebaseFirestore db;
    private GeoPoint start;
    private GeoPoint end;
    private Marker person;
    private ArrayList<OverlayItem> overlayItemArray;
   private ArrayList<Latitude> latitudes =new ArrayList<>();
    private ArrayList<Longitude> longitudes = new ArrayList<>();
    private ArrayList<Co2> Co2s = new ArrayList<>();
    private List<Node> nodes = new ArrayList<>();
    ProgressBar waiting;
    Context ctx;
    private boolean queryFinished;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        queryFinished = true;
        super.onCreate(savedInstanceState);

        ctx = getApplicationContext();
        initFirestore();
        locationSetup();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        mapSetup();
        setup();
        authView();
        handleLogin();
        navigation();
        getNodes();
        start= new GeoPoint(41.1709, -8.5976);
        end = new GeoPoint(41.1688, -8.5924);
        waiting = findViewById(R.id.indeterminateBar);
        waiting.setVisibility(View.GONE);
        final DatabaseHandler sql = new DatabaseHandler(this);
        sql.deleteMap();
        queryFinished = false;
        db.collection("Latitude")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                if (documentSnapshots.isEmpty()) {
                    return;
                }
                else {

                    List<Latitude> list = documentSnapshots.toObjects(Latitude.class);
                    latitudes.addAll(list);
                    for(int i=0;i<latitudes.size();i++)
                    {
                        StringData data = new StringData(latitudes.get(i).getIdBiker(),"",latitudes.get(i).getLatitude(), " ",latitudes.get(i).getTimeStamp());
                        sql.addMap(data);
                    }
                 //   Log.i("DB", "Success" + latitudes);
                    db.collection("Longitude")
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot documentSnapshots) {
                                    if (documentSnapshots.isEmpty()) {
                                        return;
                                    }
                                    else {
                                        List<Longitude> list = documentSnapshots.toObjects(Longitude.class);

                                        longitudes.addAll(list);
                                        for(int i=0;i<longitudes.size(); i++)
                                        {
                                            StringData data = sql.getMapValue(longitudes.get(i).getIdBiker(),longitudes.get(i).getTimeStamp());
                                            data.setLongitude(longitudes.get(i).getLongitude());
                                            sql.updateMap(data);
                                        }
                                       // Log.i("DB", "Success" + longitudes);
                                        db.collection("Co2")
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot documentSnapshots) {
                                                        if (documentSnapshots.isEmpty()) {
                                                            queryFinished =true;
                                                            return;
                                                        }
                                                        else {

                                                            List<Co2> list = documentSnapshots.toObjects(Co2.class);
                                                            Co2s.addAll(list);

                                                            for(int i=0; i<Co2s.size();i++)
                                                            {
                                                                StringData data = sql.getMapValue(Co2s.get(i).getIdBiker(),Co2s.get(i).getTimeStamp());
                                                                data.setPollution(Co2s.get(i).getCo2());
                                                                sql.updateMap(data);
                                                            }
                                                           // Log.i("DB", "Success" + Co2s);
                                                            sql.clearMap();
                                                            //Żeby użyć tych danych trzeba poczekać aż skończy się thread i wywołać z tąd dalsze funkcje.
                                                            ArrayList<Data> recentData = new ArrayList<>();
                                                            recentData.addAll(sql.getLastMap());
                                                          /*  for(int i=0; i<recentData.size(); i++)
                                                            {
                                                                double la  = recentData.get(i).getLatitude();
                                                                double lo = recentData.get(i).getLongitude();
                                                                GeoPoint p = new GeoPoint(la,lo);
                                                                Marker m = new Marker(map);
                                                                m.setIcon(getResources().getDrawable(R.drawable.pmarker));
                                                                m.setPosition(p);
                                                                map.getOverlays().add(m);
                                                            }*/
                                                            queryFinished =true;
                                                            Toast.makeText(ctx, "Data loaded", Toast.LENGTH_SHORT).show();
                                                            showOnMap(recentData);

                                                        }
                                                    }
                                                });
                                    }
                                }
                            });
                }
            }
        });


    }
    public void showOnMap(List<Data> recentData)
    {
        List<Node> checked = new ArrayList<>();
        GeoPoint S = new GeoPoint((start.getLatitude()+end.getLatitude())/2,(start.getLongitude()+end.getLongitude())/2);
        int distance = (int)distanceCalculator(start.getLatitude(),start.getLongitude(),end.getLatitude(),end.getLongitude());
        int test;
        for(int i=0; i<nodes.size();i++)
        {
            test=(int)distanceCalculator(nodes.get(i).getLatitude(),nodes.get(i).getLongitude(),S.getLatitude(),S.getLongitude());
            if(test<distance)
            {
                checked.add(nodes.get(i));
            }
        }
        Routing r = new Routing(checked,recentData, start, end);
        r.mainRouting();
        List<Path> paths = new ArrayList<>();
        paths=r.getPaths();
        Log.i("PATH NO", "PATHS" + paths.size());
        List<GeoPoint> geoPoints = new ArrayList<>();
        int counter=0;
        for (int i=0; i<paths.get(0).getNodes().size();i++) {
            double la  = paths.get(0).getNodes().get(i).getLatitude();
            double lo = paths.get(0).getNodes().get(i).getLongitude();
            GeoPoint a = new GeoPoint(la,lo);
            geoPoints.add(a);
            Marker m = new Marker(map);
            m.setIcon(getResources().getDrawable(R.drawable.route));
            m.setTextIcon("count: "+counter);
            counter++;
            m.setPosition(a);
            map.getOverlays().add(m);
        }

//add your points here
        Polyline line = new Polyline();   //see note below!
        line.setPoints(geoPoints);
        line.setColor(ContextCompat.getColor(this.getBaseContext(),R.color.gray));
        line.setOnClickListener(new Polyline.OnClickListener() {
            @Override
            public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
                Toast.makeText(mapView.getContext(), "polyline with " + polyline.getPoints().size() + "pts was tapped", Toast.LENGTH_LONG).show();
                return false;
            }
        });
        map.getOverlayManager().add(line);
        pointMarkers();
        //Log.i("DATABASE FINAL", "FINAL" + recentData);
    }
    public void pointMarkers()
    {

        Marker m = new Marker(map);
        m.setIcon(getResources().getDrawable(R.drawable.pmarker));
        m.setPosition(start);
        map.getOverlays().add(m);
        Marker x = new Marker(map);
        x.setIcon(getResources().getDrawable(R.drawable.pmarker));
        x.setPosition(end);
        map.getOverlays().add(x);
    }
    public double distanceCalculator(double la1, double lo1, double la2, double lo2) {
        double d;
        if(la1==la2 && lo1==lo2) {
            d=0;
        }
        else {
            la1 = la1/(180/Math.PI);
            la2= la2/(180/Math.PI);
            lo1 = lo1/(180/Math.PI);
            lo2= lo2/(180/Math.PI);
            double dlong = lo2 - lo1;
            double dlat = la2 - la1;

            d = Math.pow(Math.sin(dlat / 2), 2) +
                    Math.cos(la1) * Math.cos(la2) *
                            Math.pow(Math.sin(dlong / 2), 2);

            d = 2 * Math.asin(Math.sqrt(d));
            double R = 6371;
            d = d * R*1000 *100;
        }
        return d;
    }
    public void getNodes()
    {
        String jsonFileString = Utils.getJson(getApplicationContext(), "testing.json");
        nodes = new ArrayList<>();
        Node temp = new Node();
        Edge tempe = new Edge();
        List<Edge> tempEdges = new ArrayList<>();
        double la, lo;
        assert jsonFileString != null;
       // Log.i("data", jsonFileString);
        try {
            JSONArray jsonArray = new JSONArray(jsonFileString);

            for(int k=0; k<jsonArray.length(); k++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(k);
                la  = jsonObject.getDouble("la");
                lo = jsonObject.getDouble("lo");
                temp = new Node();
                GeoPoint p = new GeoPoint(la,lo);
                Marker m = new Marker(map);
                m.setIcon(getResources().getDrawable(R.drawable.marker));
                m.setPosition(p);
                tempEdges = new ArrayList<>();
                map.getOverlays().add(m);
                JSONArray edges = jsonObject.getJSONArray("e");
                for(int j=0; j<edges.length(); j++)
                {
                    tempe= new Edge();
                    JSONObject edge =edges.getJSONObject(j);
                    if(edge!=null)
                    {
                        tempe.setDistance(edge.getInt("w"));
                        tempe.setIndex(edge.getInt("i"));
                    }
                    tempEdges.add(j,tempe);
               //     Log.i("EDGE ADDED", "edge " +tempEdges.get(0).getIndex() + " added" );
                }
             //   Log.i("Break", "++++++++++++++++++++++++++++");
             //   Log.i("EDGE ADDED TO LIST", "edge " +tempEdges.get(0).getIndex() + " added" );
             //   Log.i("EDGE ADDED TO LIST", "edge " +tempEdges.get(1).getIndex() + " added" );
                temp.setLatitude(la);
                temp.setLongitude(lo);
                temp.setEdges(tempEdges);

             //   Log.i("Break", "---------------------------------------");
                nodes.add(temp);

                //Log.i("LOCATION", Double.toString(temp.getLatitude()));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void initFirestore() {
        db = FirebaseFirestore.getInstance();
    }
    public void navigation()
    {
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(queryFinished)
                {
                     finish();
                     Intent intent = new Intent(MapActivity.this, MainActivity.class);
                      startActivity(intent);
                }
                else
                {
                    Toast.makeText(MapActivity.this, "Please wait for data to load", Toast.LENGTH_SHORT).show();
                }
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(MapActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }
    public void  mapSetup()
    {
        setContentView(R.layout.map);
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(15);
        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(map);
        map.getOverlays().add(scaleBarOverlay);
        person = new Marker(map);


        person.setIcon(getResources().getDrawable(R.drawable.location));
        person.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_BOTTOM);

        GeoPoint startPoint = new GeoPoint(41.1668043, -8.5972933);
        mapController.setCenter(startPoint);
        map.getOverlays().add(person);
    }
    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

    public void setup()
    {
        login = findViewById(R.id.loginMain);
        loginText = findViewById(R.id.loginView);
        home = findViewById(R.id.homeButton);
        settings = findViewById(R.id.settingsButton);
    }
    public void handleLogin()
    {
        login.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                FirebaseUser currentUser = auth.getCurrentUser();
                if(currentUser==null)
                {

                    Intent intent = new Intent(MapActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                else
                {
                    //popup here asking if you want to log out
                    AuthUI.getInstance().signOut(MapActivity.this)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(MapActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                                    login.setText("Login");
                                    loginText.setText("");
                                }
                            });
                }
            }
        });
    }
    public void authView() {
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser==null) {
            login.setText("Login");
        }
        else {
            login.setText("Logout");
            loginText.setText(currentUser.getEmail());
        }
    }
    private void updateLoc(){
        GeoPoint locGeoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        //mapController.setCenter(locGeoPoint);
        person.setPosition(locGeoPoint);
        map.getOverlays().add(person);
        map.invalidate();
    }
    @Override
    public void onLocationChanged(Location location) {
        this.location =location;
        updateLoc();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {    }
    @Override
    public void onProviderDisabled(String provider) {    }
    public void locationOn()
    {
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationOn = service
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!locationOn) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }    public void locationGetter()
    {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationOn();
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }
    public void locationSetup()
    {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationOn();
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationOn = service
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}
      /*  for(int i=0;i<latitudes.size(); i++)
        {
            for(int j=0; j<longitudes.size();j++)
            {
                for(int k=0; k<Co2s.size(); k++)
                {
                    if(latitudes.get(i).getIdBiker().equals(longitudes.get(j).getIdBiker()) &&
                            latitudes.get(i).getIdBiker().equals(Co2s.get(k).getIdBiker()))
                    {
                        Log.i("DATA ADDED", "data added-------------------------------------------------------------------------------------------------------\n");
                        Date dateLa = format.parse(latitudes.get(i).getTimeStamp(), new ParsePosition(0));
                        Date dateLo= format.parse(longitudes.get(j).getTimeStamp(), new ParsePosition(0));
                        Date dateC = format.parse(Co2s.get(k).getTimeStamp(), new ParsePosition(0));
                        assert dateLa != null;
                        if(dateLa.equals(dateLo)&& dateLa.equals(dateC))
                        {
                            Data data = new Data(latitudes.get(i).getIdBiker(), Double.parseDouble(latitudes.get(i).getLatitude()),
                                    Double.parseDouble(longitudes.get(j).getLongitude()),
                                   Integer.parseInt( Co2s.get(k).getCo2()), latitudes.get(i).getTimeStamp());
                            pollution.add(data);

                        }
                    }
                }
            }
        }
       ArrayList<Data> recentData = new ArrayList<>();
        recentData.add(pollution.get(0));
        boolean updated =false;
        for(int n=1;n<pollution.size();n++)
        {
            for(int m=0;m<recentData.size(); m++)
            {
                if(pollution.get(n).getLatitude()==recentData.get(m).getLatitude() &&
                pollution.get(n).getLongitude() == recentData.get(m).getLongitude())
                {
                    updated=true;
                    Date dateP = format.parse(pollution.get(n).getDate(), new ParsePosition(0));
                    Date dateN= format.parse(recentData.get(m).getDate(), new ParsePosition(0));
                    assert dateN != null;
                    if(dateN.before(dateP))
                    {
                        recentData.set(m,pollution.get(n));
                    }
                }
                if(!updated)
                {
                    recentData.add(pollution.get(n));
                }
            }
        }
        for(int t=0; t<recentData.size();t++)
        {
            Log.i("RECENT DATA", Double.toString(recentData.get(t).getLongitude()));
        }*/
                /*{
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("FIREBASE", document.getId() + " => " + document.getData());
                                Longitude temp = document.toObject(Longitude.class);
                                longitudes.add(temp);
                                Log.i("--------------", "Success" + longitudes.get(0).getLongitude());
                            }
                        } else {
                            Log.d("tag", "Error getting documents: ", task.getException());
                        }
                    }*/