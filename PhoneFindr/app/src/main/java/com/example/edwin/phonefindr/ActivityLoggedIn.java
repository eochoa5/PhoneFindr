package com.example.edwin.phonefindr;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.edwin.phonefindr.utils.GPSTracker;
import com.google.firebase.auth.FirebaseAuth;


public class ActivityLoggedIn extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private GPSTracker gps;
    Intent mServiceIntent;
    private SocketIoService mSocketIoService;
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSocketIoService = new SocketIoService(this);
        mServiceIntent = new Intent(this, mSocketIoService.getClass());
        if (!isMyServiceRunning(mSocketIoService.getClass())) {
            startService(mServiceIntent);
        }

        firebaseAuth = FirebaseAuth.getInstance();

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
        //int startPos = content.indexOf("https://192.168.100.232");
        str.setSpan(new ForegroundColorSpan(Color.RED),
                startPos, startPos+32, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        welcomeMessage.setText(str);

        int permissionCheck = ContextCompat.checkSelfPermission(ActivityLoggedIn.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);

                if(permissionCheck != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(ActivityLoggedIn.this,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
               }

        gps = new GPSTracker(this);

        if(!gps.canGetLocation()) {
            gps.showSettingsAlert();
        }



    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {

                return true;
            }
        }

        return false;
    }


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
                                 // stop service
                                 if (isMyServiceRunning(mSocketIoService.getClass())) {
                                     stopService(mServiceIntent);
                                 }
                                 //
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

    }

}
