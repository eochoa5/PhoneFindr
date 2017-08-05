package com.eochoa5.edwin.phonefindr.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.eochoa5.edwin.phonefindr.SocketIoService;

/**
 * Created by Edwin on 7/24/2017.
 */
public class ShutdownReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SocketIoService.okToRestart = false;
        context.stopService(new Intent(context, SocketIoService.class));
    }
}
