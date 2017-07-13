package com.example.edwin.phonefindr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Edwin on 7/4/2017.
 */
public class BootReceiver extends BroadcastReceiver {
    private FirebaseAuth firebaseAuth;
    public void onReceive(Context context, Intent intent) {
        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() != null) {

            SocketIoService mSocketIoService = new SocketIoService(context);
            Intent mServiceIntent = new Intent(context, mSocketIoService.getClass());

            context.startService(mServiceIntent);
        }

    }

}
