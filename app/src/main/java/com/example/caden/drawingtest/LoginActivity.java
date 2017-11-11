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

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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
//    GoogleSignInClient mGoogleSignInClient;

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

        /* Google Login */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

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

    public void google_login(final View v) {
//        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        startActivityForResult(signInIntent, RC_SIGN_IN);
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
