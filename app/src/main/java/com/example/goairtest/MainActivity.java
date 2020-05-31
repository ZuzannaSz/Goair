package com.example.goairtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.Set;

public class MainActivity extends AppCompatActivity implements LocationListener {
    int REQUEST_PERMISSION_ACCESS_FINE_LOCATION= 101;
    Button blt, b;
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private BluetoothAdapter ba;
    private OutputStream outputStream;
    private InputStream inputStream;
    public  DatabaseHandler db;
    private BluetoothDevice device = null;
    private Location lastLocation;
    private boolean locationEnabled = false;
    private boolean locationOn = true;
    private boolean exists = false;
    private BluetoothSocket socket =null;
    private boolean stopThread = false;
    private boolean connected = false;
    private LocationManager locationManager;
    private Button login;
    private TextView loginText;
    private FirebaseAuth auth;
    Location location = null;
    private String provider = null;
    private  TextView text;
    private int polutionTest = 700;
    private String updated;
    private FragmentRefreshListener fragmentRefreshListener;
private FirebaseFirestore mFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user!=null;
     /*   Uri uri = user.getPhotoUrl();
       ImageView img = findViewById(R.id.profile);
        if(uri!=null)
        {
            Glide.with(this).load(uri).error(R.mipmap.ic_launcher)
                    .placeholder(R.drawable.person)
                    .into(img);
            //Picasso.with(getContext()).load(uri.toString()).into(img);
        }*/
        setup();
        db = new DatabaseHandler(this);
        ba = BluetoothAdapter.getDefaultAdapter();
        if(getFragmentRefreshListener()!= null){
            getFragmentRefreshListener().onRefresh();
        }

        bltOn();
        initFirestore();
        locationRequest();
        navigationSetup();
        locationGetter();
        authView();
        handleLogin();
       testButton();
    }
    private void initFirestore() {
        mFirestore = FirebaseFirestore.getInstance();
    }
    public void testButton() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        b = findViewById(R.id.test);
        b .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(location!=null)
                {
                    Data data2 = new Data(location.getLatitude(),location.getLongitude(),location.getAltitude(),polutionTest, getDate(),"false");
                    TempHum th = new TempHum(25, 40,getDate(),"false");

                    polutionTest+=20;
                    db.addData(data2);
                    db.addTH(th);
                    data2 = db.getLast();
                    th = db.getLastTH();
                    addDataToFirebase(data2);
                    addTHToFirebase(th);
                    if(getFragmentRefreshListener()!= null){
                        getFragmentRefreshListener().onRefresh();
                    }
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Waiting for location", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
    public void addDataToFirebase(Data data)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null)
        {
            addToFirebase("Latitude", String.valueOf(data.getLatitude()), String.valueOf(data.getID()), user.getUid(),String.valueOf(data.getDate()));
            addToFirebase("Longitude", String.valueOf(data.getLatitude()), String.valueOf(data.getID()), user.getUid(),String.valueOf(data.getDate()));
            addToFirebase("Altitude", String.valueOf(data.getLatitude()), String.valueOf(data.getID()), user.getUid(),String.valueOf(data.getDate()));
            addToFirebase("Co2", String.valueOf(data.getLatitude()), String.valueOf(data.getID()), user.getUid(),String.valueOf(data.getDate()));

        }

    }
    public void addTHToFirebase(TempHum tempHum) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            addToFirebase("Temperature", String.valueOf(tempHum.getTemperature()), String.valueOf(tempHum.getID()), user.getUid(), String.valueOf(tempHum.getDate()));
            addToFirebase("Humidity", String.valueOf(tempHum.getHumidity()), String.valueOf(tempHum.getID()), user.getUid(), String.valueOf(tempHum.getDate()));
        }
    }
    public void addToFirebase(String dataType, String data, String key, String user, String time )
    {
        Map<String, Object> doc = new HashMap<>();
        doc.put(dataType, data);
        doc.put("IdBiker", user);
        doc.put("Key", key);
        doc.put("TimeStamp", time);

        mFirestore.collection(dataType).document()
                .set(doc)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("firebase", "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("firebase", "Error writing document", e);
                    }
                });
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
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                else
                {
                    //popup here asking if you want to log out
                    AuthUI.getInstance().signOut(MainActivity.this)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                                    login.setText("Login");
                                    loginText.setText("");
                                }
                            });
                }
            }
        });
    }
    public FragmentRefreshListener getFragmentRefreshListener() {
        return fragmentRefreshListener;
    }
    @Override
    public void onLocationChanged(Location location) {
        this.location =location;
        //text.setText("location coordinates: " + location.getLatitude() + " " + location.getLongitude());
    }

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
            locationEnabled =true;
            locationOn();
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }
    void receiveData(){
        final Handler handler = new Handler();
        final Thread thread  = new Thread(new Runnable() {
            public void run() {
               while(!stopThread) {
                    try {
                        int byteCount = inputStream.available();
                        if (byteCount > 0) {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string = new String(rawBytes, StandardCharsets.UTF_8);
                            handler.post(new Runnable() {
                                public void run() {
                                    saveData(string);
                                }
                            });
                        }
                    }
                    catch (IOException e) {
                        stopThread=true;
                    }
                }
            }
        });
        thread.start();
    }
    public void saveData(String string)
    {
        String[] rawData = string.split(" ");
        //get current localisation
        //Data data = new Data(location.getLatitude(), location.getLongitude(),location.getAltitude(), Integer.parseInt(rawData[0]));

       // int id = db.getId(data);
       /* if(id !=-1)
        {
         //   int i = db.updateData(id,location.getLatitude(),location.getLongitude(), Integer.parseInt(rawData[0]));
            Log.d("exist", "data exists and got updated " + id);
            if(getFragmentRefreshListener()!= null){
                getFragmentRefreshListener().onRefresh();
            }
        }
        else
        {
            db.addData(data);
            if(getFragmentRefreshListener()!= null){
                getFragmentRefreshListener().onRefresh();
            }
            Log.d("exist", "data doesn't exist");
        }*/
    }
    public void bltConnect() throws IOException {
        Set<BluetoothDevice> devices = ba.getBondedDevices();
        if(devices.isEmpty()) {
           Toast.makeText(getApplicationContext(), "No devices are paired", Toast.LENGTH_LONG).show();
            connected =false;
        }
        else {
            for(BluetoothDevice i : devices) {
                if(i.getName().equals("GOAirLight")) {
                    device = i;
                    exists = true;
                    break;
                }
            }
        }
        if(exists) {
                socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
                socket.connect();
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            connected = true;
            receiveData();
        }
        else {
            Toast.makeText(getApplicationContext(), "Please pair the device", Toast.LENGTH_LONG).show();
            connected=false;
        }
    }
    public void locationRequest(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_ACCESS_FINE_LOCATION);
            }
        }
    }
    public void bltOn(){
        if (!ba.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on",Toast.LENGTH_LONG).show();
        }
    }
    public void toggleHandle(View view) {
        boolean on = ((ToggleButton) view).isChecked();
        if (on) {
            try{
                bltConnect();
            }
            catch (IOException e) {
                ((ToggleButton) view).setChecked(false);
                e.printStackTrace();
            }
            if(!connected) {
                ((ToggleButton) view).setChecked(false);
            } } else {
            try{
                bltDisconnect();
            }
            catch (IOException e) {
                ((ToggleButton) view).setChecked(true);
                e.printStackTrace();
            } }
    }
    void bltDisconnect() throws IOException {
        if(connected) {
            stopThread= true;
            outputStream.close();
            inputStream.close();
            socket.close();
            connected =false;
        }
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
    public void navigationSetup() {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_map, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }
    public void setup()
    {
        text = findViewById(R.id.localisation);
        blt = (ToggleButton) findViewById(R.id.button_blt);
        login = findViewById(R.id.loginMain);
        loginText = findViewById(R.id.loginView);
    }
    public String getDate() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String strDate = dateFormat.format(date);
        return strDate;
    }
    public interface FragmentRefreshListener{ void onRefresh();    }
    public void onProviderEnabled(String string) {    }
    public void  onProviderDisabled(String string) {    }
    public void setFragmentRefreshListener(FragmentRefreshListener fragmentRefreshListener) {
        this.fragmentRefreshListener = fragmentRefreshListener; }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {    }
}
/*
    List<Data> data = db.getAllData();

        for (Data cn : data) {
            String log = "Id: " + cn.getID() + " ,latitude: " + cn.getLatitude() + " ,longitude: " +
                    cn.getLongitude() + " ,pollution: " + cn.getPollution();
            Log.d("Name: ", log);
        }
        Data d = db.getLast();
        String logs= "Id: " + d.getID() + " ,latitude: " + d.getLatitude() + " ,longitude: " +
                d.getLongitude() + " ,pollution: " + d.getPollution();
        Log.d("name", logs);
                   // Criteria criteria = new Criteria();
            //provider = locationManager.getBestProvider(criteria, false);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
          //  location = locationManager.getLastKnownLocation(provider);
        //    Log.d("location", "tried to access location-----------------------------------");
        }
        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
        } else {
            text.setText("Location not available");
        }*/
