package com.example.edwin.phonefindr;

/**
 * Created by Edwin on 7/4/2017.
 */
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;


import com.example.edwin.phonefindr.utils.GPSTracker;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;


/**
 * Created by fabio on 30/01/2016.
 */
public class SocketIoService extends Service {
    private Context ctx;
    private FirebaseAuth firebaseAuth;
    private Socket socket;
    private GPSTracker gps;
    private IO.Options options;

    public SocketIoService(Context applicationContext) {
        super();
        this.ctx = applicationContext;
    }

    public SocketIoService() {
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);


        firebaseAuth = FirebaseAuth.getInstance();
        String email = firebaseAuth.getCurrentUser().getEmail();

        options = new IO.Options();
        options.query = "email="+email;
        try{
            socket = IO.socket("https://fonefinder.herokuapp.com", options);
            //socket = IO.socket("http://192.168.1.172:8080", options);
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }

        socket.connect();
        socket.on("ring request", makeRing);
        socket.on("location request", sendLocation);

        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.disconnect();
        Intent broadcastIntent = new Intent("com.example.edwin.phonefindr.ServiceRestarter");
        sendBroadcast(broadcastIntent);
    }

    private Emitter.Listener makeRing = new Emitter.Listener(){
        @Override
        public void call(final Object... args){

        MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sound);

        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        audioManager.setSpeakerphoneOn(true);
        mPlayer.setLooping(true);

        mPlayer.start();



        }
    };

    private Emitter.Listener sendLocation = new Emitter.Listener(){
        @Override
        public void call(final Object... args){

            gps = new GPSTracker(getApplicationContext());

            String lat = gps.getLatitude()+"";
            String lon = gps.getLongitude()+"";

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
    };



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}