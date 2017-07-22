package com.example.edwin.phonefindr;

/**
 * Created by Edwin on 7/4/2017.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ServiceRestarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle data = intent.getExtras();

        if(data == null) {
            context.startService(new Intent(context, SocketIoService.class));
        }else{
            //use this same Broadcast receiver to stop sound because why not
            SocketIoService.mPlayer.stop();
            SocketIoService.mNotificationManager.cancel(0);
        }
    }
}