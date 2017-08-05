package com.eochoa5.edwin.phonefindr.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.eochoa5.edwin.phonefindr.SocketIoService;

/**
 * Created by Edwin on 8/2/2017.
 */
public class SleepReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null)
        {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                if(!SocketIoService.mPlayer.isPlaying()) {
                    SocketIoService.stayAwake.release();
                }
                System.out.println("is held: " + SocketIoService.stayAwake.isHeld());
            }
            else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                SocketIoService.stayAwake.acquire();

                System.out.println("is held: " + SocketIoService.stayAwake.isHeld());


            }

        }
    }
}