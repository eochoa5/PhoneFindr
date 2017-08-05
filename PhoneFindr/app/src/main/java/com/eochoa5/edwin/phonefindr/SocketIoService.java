package com.eochoa5.edwin.phonefindr;

/**
 * Created by Edwin on 7/4/2017.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.eochoa5.edwin.phonefindr.utils.GPSTracker;
import com.eochoa5.edwin.phonefindr.utils.SleepReceiver;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;


public class SocketIoService extends Service {
    private Context ctx;
    private FirebaseAuth firebaseAuth;
    private Socket socket;
    private GPSTracker gps;
    private IO.Options options;
    private PowerManager pm;
    private PowerManager.WakeLock wl;
    public static  PowerManager.WakeLock stayAwake;
    public static MediaPlayer mPlayer;
    public static NotificationManager mNotificationManager;
    private AudioManager audioManager;
    private String myPhoneName = "Unknown device";
    public static boolean okToRestart = true;
    private SleepReceiver sleepReceiver;

    public SocketIoService(Context applicationContext) {
        super();
        this.ctx = applicationContext;
    }

    public SocketIoService() {
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        okToRestart = true;

        firebaseAuth = FirebaseAuth.getInstance();
        String email = firebaseAuth.getCurrentUser().getEmail();

        myPhoneName = Build.MANUFACTURER  + " " + Build.MODEL;
        options = new IO.Options();
        options.query = "email="+email+
                "&phone="+"true"+
                "&phoneName="+myPhoneName;
        try{
            socket = IO.socket("https://fonefinder.herokuapp.com", options);


        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }


        socket.connect();
        socket.on("ring request", makeRing);
        socket.on("location request", sendLocation);

        pm = (PowerManager)getApplicationContext().getSystemService(
                Context.POWER_SERVICE);
        //ignore deprecation
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "MyWakeLock");

        stayAwake = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock2");
        //stayAwake.acquire();

        //check when screen turns off to acquire partial lock and release when on
        sleepReceiver = new SleepReceiver();
        IntentFilter lockFilter = new IntentFilter();
        lockFilter.addAction(Intent.ACTION_SCREEN_ON);
        lockFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(sleepReceiver, lockFilter);
        //

        mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.siren1);
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        socket.disconnect();
        unregisterReceiver(sleepReceiver);

        //stayAwake.release();

        if (firebaseAuth.getCurrentUser()!=null && okToRestart) {
            Intent broadcastIntent = new Intent("com.eochoa5.edwin.phonefindr.ServiceRestarter");
            sendBroadcast(broadcastIntent);
        }
    }

    private Emitter.Listener makeRing = new Emitter.Listener(){
        @Override
        public void call(final Object... args){

            wl.acquire();

            String name = args[0].toString();

            if(mPlayer.isPlaying()){mPlayer.stop();}

            switch(name) {
                case "iphone" :
                    mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.iphone2);
                    break;
                case "iphone2" :
                    mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.iphone3);
                    break;
                case "iphone3" :
                    mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.iphone);
                    break;
                case "arnold" :
                    mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.arnold);
                    break;
                case "rick" :
                    mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.rickroll);
                    break;
                case "siren1" :
                    mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.siren1);
                    break;
                case "police1" :
                    mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.police);
                    break;
                case "siren2" :
                    mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.siren2);
                    break;
                case "police2" :
                    mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.police2);
                    break;
                case "siren3" :
                    mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.siren3);
                    break;
                default :
                    mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.iphone2);
            }


            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.setSpeakerphoneOn(true);
            mPlayer.setLooping(true);

            mPlayer.start();

            //notification code
            Intent notificationIntent = new Intent("com.eochoa5.edwin.phonefindr.ServiceRestarter");
            notificationIntent.putExtra("stopRinging", true);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.ic_notification)
                            .setContentTitle("PhoneFindr")
                            .addAction(R.drawable.ic_stop,"Stop",pendingIntent)
                            .setPriority(Notification.PRIORITY_MAX);
            mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.notify(0, mBuilder.build());
            //end of notification code


            wl.release();

        }
    };

    private Emitter.Listener sendLocation = new Emitter.Listener(){
        @Override
        public void call(final Object... args){

            wl.acquire();
            if (Looper.myLooper()==null) {
                Looper.prepare();
            }
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
            }finally{
                gps.stopUsingGPS();
                wl.release();
            }





        }
    };


    @Override
    public boolean onUnbind(Intent intent) {

        return super.onUnbind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}