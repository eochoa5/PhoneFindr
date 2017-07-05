package com.example.edwin.phonefindr;

/**
 * Created by Edwin on 7/4/2017.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServiceRestarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, SocketIoService.class));;
    }
}