package com.example.caden.drawingtest;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    final String TAG = "DT.LoginActivity";

    EditText emailField;
    EditText passwordField;
    ProgressBar progressbar;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /* Layout */
        emailField = findViewById(R.id.text_email);
        passwordField = findViewById(R.id.text_password);
        progressbar = findViewById(R.id.pbar);

        /* FireBase */
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    public void login(final View v) {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        progressbar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressbar.setVisibility(View.INVISIBLE);
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            progressbar.setVisibility(View.INVISIBLE);
                            Snackbar sb = Snackbar.make(v, "\uD83D\uDE05\uD83D\uDE05\uD83D\uDE05 "
                                    + task.getException().getMessage(), Snackbar.LENGTH_LONG);
                            sb.show();
                            // If sign in fails, display a message to the user.
                            updateUI(null);
                        }
                    }
                });
    }

    public void register(View v){
        Intent i = new Intent(this, RegistrationActivity.class);
        startActivity(i);
    }

    public void updateUI(FirebaseUser currUser) {
        if (currUser != null) {
            Log.d(TAG, currUser.getUid());
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        } else {
//            TODO either Sign-in failed or user returned from activity
            Log.d(TAG, "current user null");
        }
    }
}
