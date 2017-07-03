package com.example.edwin.phonefindr;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
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

import com.google.firebase.auth.FirebaseAuth;
import java.net.URISyntaxException;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;


public class ActivityLoggedIn extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;

    private Socket socket;
    {
        try{

           socket = IO.socket("https://fonefinder.herokuapp.com");
            //socket = IO.socket("http://192.168.1.172:3000");

        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
                    JSONObject toLatLon = new JSONObject();
                    try{
                        toLatLon.put("to",args[0].toString());
                        toLatLon.put("lat",0);
                        toLatLon.put("lon",0);
                        socket.emit("send location", toLatLon);
                    }catch(JSONException e){
                        e.printStackTrace();
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
