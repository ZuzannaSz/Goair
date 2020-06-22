package com.example.goairtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;

import android.view.Window;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


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
    private Button login, map, settings;
    private TextView loginText;
    private FirebaseAuth auth;
    Location location = null;
    private String provider = null;
    private  TextView text;
    private int pollutionTest = 400;
    private int pollutionMax = 499;
    int pollution;
    int THcounter;
    private ImageView logo;
    private String updated;
    private boolean clicked =false;
    Context context;
    Context cont;
    private FragmentRefreshListener fragmentRefreshListener;
private FirebaseFirestore mFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this.getBaseContext();
        cont=this;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user!=null;
        logo = findViewById(R.id.logo);
        setup();
        db = new DatabaseHandler(this);
        ba = BluetoothAdapter.getDefaultAdapter();
        if(getFragmentRefreshListener()!= null){
            getFragmentRefreshListener().onRefresh();
        }
        THcounter=0;
        pollution=450;
        bltOn();
        initFirestore();
        locationRequest();
        locationGetter();
        authView();
        handleLogin();
        testButton();
        navigation();
        showData();
        if(isNetworkAvailable(cont))
        {
            addAllToFirebase();
        }
        Button legend = findViewById(R.id.homeInfo);
        legend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(cont);
                View promptsView = li.inflate(R.layout.dialog_legend, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(cont);
                alertDialogBuilder.setView(promptsView);
                alertDialogBuilder.setCancelable(false)
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });




        Button help = findViewById(R.id.home_help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(cont);
                View promptsView = li.inflate(R.layout.dialog_help, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(cont);
                alertDialogBuilder.setView(promptsView);
                alertDialogBuilder.setCancelable(false)
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });
    }
    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
    public void showData()
    {
        if (db.checkDataPopulated()) {
            Data d = db.getLast();
            if(d.getPollution()<500) {
                logo.setColorFilter(logo.getContext().getResources().getColor(R.color.green), PorterDuff.Mode.SRC_ATOP);
            }
            else if(d.getPollution()>=500 && d.getPollution()<700) {
                logo.setColorFilter(logo.getContext().getResources().getColor(R.color.yellow), PorterDuff.Mode.SRC_ATOP);
            }
            else {
                logo.setColorFilter(logo.getContext().getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
            }
            TextView text = findViewById(R.id.pollutionView);
            text.setText(Integer.toString(d.getPollution()));
            TextView temp = findViewById(R.id.temperature);
            temp.setText(Integer.toString(db.getLastTH().getTemperature()));
            TextView hum = findViewById(R.id.humidity);
            hum.setText(Integer.toString(db.getLastTH().getHumidity()));
        } else {
            TextView text = findViewById(R.id.pollutionView);
            text.setText("---");
            TextView temp = findViewById(R.id.temperature);
            temp.setText("---");
            TextView hum = findViewById(R.id.humidity);
            hum.setText("---");
        }
    }
    public void navigation()
    {
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
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
                    //Random r = new Random();
                  //  int pollution = r.nextInt(2000-400) + 400;
                    Data data2 = new Data(location.getLatitude(), location.getLongitude(),140,pollution, getDate(),"false");
                    TempHum th = new TempHum(25, 40,getDate(),"false");
                    db.addData(data2);
                    pollution+=100;
                    db.addTH(th);
                    clicked = true;
                    data2 = db.getLast();
                    th = db.getLastTH();
                    addDataToFirebase(data2);
                    addTHToFirebase(th);
                    if(getFragmentRefreshListener()!= null){
                        getFragmentRefreshListener().onRefresh();
                    }
                    showData();
                }
                else{
                    Toast.makeText(MainActivity.this, "Waiting for location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void addAllToFirebase() {
        List<Data> dataList = new ArrayList<>();
        List<TempHum> thList = new ArrayList<>();
        dataList=db.getAllData();
        thList = db.getAllTH();
        for(int i=0;i<dataList.size();i++) {
            if(dataList.get(i).getUpdate().equals("false")) {
                addDataToFirebase(dataList.get(i));
                dataList.get(i).setUpdate("true");
                db.updateData(dataList.get(i));
            }

        }
        for(int j=0; j<thList.size();j++) {
            if(thList.get(j).getUpdate().equals("false")) {
                addTHToFirebase(thList.get(j));
                thList.get(j).setUpdate("true");
                db.updateTH(thList.get(j));
            }

        }
    }
    public void addDataToFirebase(Data data) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null) {
            addToFirebase("Latitude", String.valueOf(data.getLatitude()), String.valueOf(data.getID()), user.getUid(),String.valueOf(data.getDate()));
            addToFirebase("Longitude", String.valueOf(data.getLongitude()), String.valueOf(data.getID()), user.getUid(),String.valueOf(data.getDate()));
            addToFirebase("Altitude", String.valueOf(data.getAltitude()), String.valueOf(data.getID()), user.getUid(),String.valueOf(data.getDate()));
            addToFirebase("Co2", String.valueOf(data.getPollution()), String.valueOf(data.getID()), user.getUid(),String.valueOf(data.getDate()));

        }
    }
    public void addTHToFirebase(TempHum tempHum) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            addToFirebase("Temperature", String.valueOf(tempHum.getTemperature()), String.valueOf(tempHum.getID()), user.getUid(), String.valueOf(tempHum.getDate()));
            addToFirebase("Humidity", String.valueOf(tempHum.getHumidity()), String.valueOf(tempHum.getID()), user.getUid(), String.valueOf(tempHum.getDate()));
        }
    }
    public void addToFirebase(String dataType, String data, String key, String user, String time ) {
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
    public void handleLogin() {
        login.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                FirebaseUser currentUser = auth.getCurrentUser();
                if(currentUser==null) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                else {
                    AuthUI.getInstance().signOut(MainActivity.this).addOnCompleteListener(new OnCompleteListener<Void>() {
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
    }
    public void locationGetter() {
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
                                    if(location!=null)
                                    {
                                        saveData(string);
                                        THcounter++;
                                    }

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
        String date = getDate();
        Data data = new Data(location.getLatitude(), location.getLongitude(),location.getAltitude(), Integer.parseInt(rawData[0]),date,"false");
        db.addData(data);
        if(getFragmentRefreshListener()!= null){
            getFragmentRefreshListener().onRefresh();
        }
        if(THcounter>24)
        {
            TempHum tempHum = new TempHum(Integer.parseInt(rawData[1]), Integer.parseInt(rawData[2]),date,"false");
            THcounter=0;
        }

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
            }
        }
        else {
            try{
                bltDisconnect();
            }
            catch (IOException e) {
                ((ToggleButton) view).setChecked(true);
                e.printStackTrace();
            }
        }
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
    public void setup() {
        map = findViewById(R.id.mapButton);
        settings = findViewById(R.id.settingsButton);
        text = findViewById(R.id.localisation);
        blt = (ToggleButton) findViewById(R.id.button_blt);
        login = findViewById(R.id.loginMain);
        loginText = findViewById(R.id.loginView);
    }
    public String getDate() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
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
/*
    public void navigationSetup() {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_map, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }*/
//Button Red = findViewById(R.id.red);
// Button Green = findViewById(R.id.green);
//Button Yellow = findViewById(R.id.yellow);
       /* Red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pollutionMax =2000;
                pollutionTest = 700;
            }
        });
        Green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pollutionTest=400;
                pollutionMax=499;
            }
        });
        Yellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pollutionMax= 699;
                pollutionTest = 500;
            }
        });*/