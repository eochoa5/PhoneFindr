package com.example.edwin.phonefindr;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final TextView createAccount = (TextView)findViewById(R.id.createAccount);
        final Button loginButton = (Button)findViewById(R.id.loginButton);
        final EditText email = (EditText) findViewById(R.id.editTextEmail);
        final EditText pass = (EditText) findViewById(R.id.editTextPass);

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(createAccount.getText().equals("Create an Account")){
                    createAccount.setText("Already have an Account");
                    loginButton.setText("SIGN UP");
                    pass.setHint("Choose a password");
                }
                else{
                    createAccount.setText("Create an Account");
                    loginButton.setText("LOGIN");
                    email.setHint("Enter your email");
                    pass.setHint("Password");

                }
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(loginButton.getText().equals("LOGIN")){
                    //login
                }
                else{
                    //sign up
                }

                //redirect to next Activity
                Intent i = new Intent(MainActivity.this, ActivityLoggedIn.class);
                startActivity(i);
                finish();
            }
        });


    }

}
