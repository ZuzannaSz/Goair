package com.example.goairtest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;
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
    Location location = null;
    private String provider = null;
    private  TextView text;
    private int polutionTest = 666;
    private FragmentRefreshListener fragmentRefreshListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new DatabaseHandler(this);

        text = (TextView) findViewById(R.id.localisation);

        ba = BluetoothAdapter.getDefaultAdapter();
        bltOn();
        locationRequest();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationEnabled =true;
            locationOn();
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
       blt = (ToggleButton) findViewById(R.id.button_blt);
       navigationSetup();
        if(getFragmentRefreshListener()!= null){
            getFragmentRefreshListener().onRefresh();
        }
        b = (Button) findViewById(R.id.test);
        b .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Data data2 = new Data(location.getLatitude(),location.getLongitude(),location.getAltitude(),polutionTest, getDate());
                TempHum th = new TempHum(25, 40,getDate());
                polutionTest+=20;
                db.addData(data2);
                db.addTH(th);
                if(getFragmentRefreshListener()!= null){
                    getFragmentRefreshListener().onRefresh();
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
        text.setText(String.valueOf("location coordinates: "+ location.getLatitude() + " " + location.getLongitude()));

    }
    public void setFragmentRefreshListener(FragmentRefreshListener fragmentRefreshListener) {
        this.fragmentRefreshListener = fragmentRefreshListener;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

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
    public interface FragmentRefreshListener{
        void onRefresh();
    }
    public void bltOn(){
        if (!ba.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on",Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();

        }
    }
    public String getDate()
    {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String strDate = dateFormat.format(date);
        return strDate;
    }
    void receiveData(){

        final Handler handler = new Handler();
        final Thread thread  = new Thread(new Runnable() {
            public void run() {
               while(!stopThread)
               {
                    try {
                        int byteCount = inputStream.available();
                        if (byteCount > 0) {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string = new String(rawBytes, "UTF-8");
                            handler.post(new Runnable() {
                                public void run() {
                                    saveData(string);
                                }
                            });
                        }
                    }
                    catch (IOException e)
                    {
                        stopThread=true;
                    }
                };
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
    public void bltConnect() throws IOException
    {
        Set<BluetoothDevice> devices = ba.getBondedDevices();
        if(devices.isEmpty())
        {
           Toast.makeText(getApplicationContext(), "No devices are paired", Toast.LENGTH_LONG).show();
            connected =false;
        }
        else
        {
            for(BluetoothDevice i : devices)
            {
                if(i.getName().equals("GOAirLight"))
                {
                    device = i;
                    exists = true;
                    break;
                }
            }
        }
        if(exists)
        {
                socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
                socket.connect();
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            connected = true;
            receiveData();

        }
        else
        {
            Toast.makeText(getApplicationContext(), "Please pair the device", Toast.LENGTH_LONG).show();
            connected=false;
        }

    }
    public void navigationSetup()
    {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_map, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }
  public void toggleHandle(View view)
    {
        boolean on = ((ToggleButton) view).isChecked();

        if (on) {

            try{
                bltConnect();
            }
            catch (IOException e)
            {
                ((ToggleButton) view).setChecked(false);
                e.printStackTrace();
            }
            if(!connected)
            {
                ((ToggleButton) view).setChecked(false);
            }

        } else {
            try{
                bltDisconnect();
            }
            catch (IOException e)
            {
                ((ToggleButton) view).setChecked(true);
                e.printStackTrace();

            }

        }
    }
    void bltDisconnect() throws IOException {
        if(connected)
        {
            stopThread= true;
            outputStream.close();
            inputStream.close();
            socket.close();
            connected =false;
        }

    }

    public void onProviderEnabled(String string)
    {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();
    }
    public void  onProviderDisabled(String string)
    {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
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
