package com.example.edwin.phonefindr;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class ActivityLoggedIn extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;

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

    }

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


}
