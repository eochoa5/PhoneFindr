package com.example.edwin.phonefindr;


import android.*;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import java.net.URISyntaxException;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;


public class ActivityLoggedIn extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private String serviceString = Context.LOCATION_SERVICE;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Criteria criteria;

    private Socket socket;
    {
        try{
           socket = IO.socket("https://fonefinder.herokuapp.com");
            //socket = IO.socket("http://192.168.0.3:8080");
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }


    public class  myLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) { locationManager.removeUpdates(this); }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onProviderDisabled(String provider) {}
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d("permissions", "requestpermissionsresult");
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("permissions","permission granted"); }
                else { Log.d("permissions","permission denied"); }
                return;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        firebaseAuth = FirebaseAuth.getInstance();
        locationManager = (LocationManager)getSystemService(serviceString);
        //Setting the criteria for locationListener
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);

        int permissionCheck = ContextCompat.checkSelfPermission(ActivityLoggedIn.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);

        if(permissionCheck != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(ActivityLoggedIn.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if(firebaseAuth.getCurrentUser() == null){
            Intent i = new Intent(ActivityLoggedIn.this, MainActivity.class);
            startActivity(i);
            finish();
        }

        String email = firebaseAuth.getCurrentUser().getEmail();
        String user = email.split("@")[0];

        TextView username = (TextView)findViewById(R.id.username);
        username.setText(user);
        TextView welcomeMessage = (TextView) findViewById(R.id.welcomeMessage);
        String instructions = "This app will help you find your phone. Go to https://fonefinder.herokuapp.com and " +
                "sign in and you will be able to make your phone ring and see its location in the map. ";

        String content = "Welcome "+user + "\n\n" + instructions ;

        SpannableStringBuilder str = new SpannableStringBuilder(content);
        int startPos = content.indexOf("https://fonefinder.herokuapp.com");
        str.setSpan(new ForegroundColorSpan(Color.RED),
                startPos, startPos+32, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        welcomeMessage.setText(str);

        socket.connect();
        socket.emit("new user", email);
        socket.on("ring request", makeRing);
        socket.on("location request", sendLocation);
    }

    private Emitter.Listener makeRing = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    MediaPlayer mPlayer = MediaPlayer.create(ActivityLoggedIn.this, R.raw.sound);

                    AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
                    audioManager.setMode(AudioManager.MODE_IN_CALL);
                    audioManager.setSpeakerphoneOn(true);
                    mPlayer.setLooping(true);

                    mPlayer.start();
                }
            });
        }
    };

    private Emitter.Listener sendLocation = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String lat = null;
                    String lon = null;
                    //CHECK PERMISSIONS
                    if (Build.VERSION.SDK_INT >= 23) {

                        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                || checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                            Log.d("permissions", "permissions check out");
                            locationListener = new myLocationListener();
                            locationManager.requestSingleUpdate(criteria, locationListener, null);
                            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            lat = ((Double)location.getLatitude()).toString();
                            lon = ((Double)location.getLongitude()).toString();

                            //JSON
                            JSONObject toLatLon = new JSONObject();
                            try{
                                toLatLon.put("to",args[0].toString());
                                toLatLon.put("lat",lat);
                                toLatLon.put("lon",lon);
                                socket.emit("send location", toLatLon);
                            }catch(JSONException e){
                                e.printStackTrace();
                            }
                        }
                        else{Log.d("permissions","permissions failed");}
                    }



                }
            });
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
       public boolean onOptionsItemSelected(MenuItem item) {
             int id = item.getItemId();
             if (id == R.id.action_logout) {

                 new AlertDialog.Builder(this)
                         .setTitle("Logout confirmation")
                         .setMessage("Are you sure you want to Logout?")
                         .setNegativeButton(android.R.string.cancel, null)
                         .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                             @Override public void onClick(DialogInterface dialog, int which) {

                                 firebaseAuth.signOut();
                                 Intent i = new Intent(ActivityLoggedIn.this, MainActivity.class);
                                 startActivity(i);
                                 finish();
                                 Toast.makeText(ActivityLoggedIn.this, "You have been logged out", Toast.LENGTH_SHORT).show();
                             }
                         })
                         .create()
                         .show();

             }

            return super.onOptionsItemSelected(item);
            }

    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }


}
