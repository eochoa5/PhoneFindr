package com.example.edwin.phonefindr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Edwin on 7/4/2017.
 */
public class BootReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {

        SocketIoService mSocketIoService = new SocketIoService(context);
        Intent mServiceIntent = new Intent(context, mSocketIoService.getClass());

        context.startService(mServiceIntent);

    }

}
