package com.example.edwin.phonefindr.utils;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.edwin.phonefindr.SocketIoService;

public class SoundStopper extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //stop ringing
        Bundle data = getIntent().getExtras();

        if(data != null && data.containsKey("stopRinging")){

            SocketIoService.mPlayer.stop();
            SocketIoService.mNotificationManager.cancel(0);
            finish();
        }
    }
}
