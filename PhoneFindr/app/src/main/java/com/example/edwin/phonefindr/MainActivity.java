package com.example.edwin.phonefindr;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private EditText email;
    private EditText pass;
    private ProgressDialog pdialog;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final TextView createAccount = (TextView)findViewById(R.id.createAccount);
        final Button loginButton = (Button)findViewById(R.id.loginButton);
        email = (EditText) findViewById(R.id.editTextEmail);
        pass = (EditText) findViewById(R.id.editTextPass);

        pdialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() != null){
            Intent i = new Intent(MainActivity.this, ActivityLoggedIn.class);
            startActivity(i);
            finish();
        }

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
                if(loginButton.getText().equals("LOGIN"))
                    login();
                else
                    signUp();
            }
        });


    }

    private void signUp(){
        if(pass.getText().length()>5 && email.getText().length()>0){
            pdialog.setMessage("Signing up... please wait");
            pdialog.show();

            firebaseAuth.createUserWithEmailAndPassword(email.getText().toString().trim(), pass.getText().toString().trim())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(MainActivity.this, "Account created. You have been logged in", Toast.LENGTH_SHORT ).show();
                                pdialog.dismiss();
                                Intent i = new Intent(MainActivity.this, ActivityLoggedIn.class);
                                startActivity(i);
                                finish();
                            }
                            else{
                                Toast.makeText(MainActivity.this, "An error occurred please try again", Toast.LENGTH_SHORT ).show();
                                pdialog.dismiss();
                            }
                        }
                    });

        }
        else{
            Toast.makeText(MainActivity.this, "Password must contain more than 5 characters", Toast.LENGTH_LONG ).show();
        }

    }

    private void login(){
        if(pass.getText().length()>0 && email.getText().length()>0){
            pdialog.setMessage("Signing in... please wait");
            pdialog.show();

            firebaseAuth.signInWithEmailAndPassword(email.getText().toString().trim(), pass.getText().toString().trim())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(MainActivity.this, "Logged in...", Toast.LENGTH_SHORT ).show();
                                pdialog.dismiss();
                                Intent i = new Intent(MainActivity.this, ActivityLoggedIn.class);
                                startActivity(i);
                                finish();
                            }
                            else{
                                Toast.makeText(MainActivity.this, "Incorrect email or password", Toast.LENGTH_SHORT ).show();
                                pdialog.dismiss();
                            }

                        }
                    });
        }
        else{
            Toast.makeText(MainActivity.this, "Please enter a valid email and password", Toast.LENGTH_LONG ).show();
        }

    }

}
