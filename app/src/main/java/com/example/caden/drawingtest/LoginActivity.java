package com.example.caden.drawingtest;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    final int RC_SIGN_IN = 1778;

    View root;
    EditText emailField;
    EditText passwordField;
    ProgressBar progressbar;

    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);

        /* Layout Setup */
        root = findViewById(R.id.cl_login);
        emailField = findViewById(R.id.text_email);
        passwordField = findViewById(R.id.text_password);
        progressbar = findViewById(R.id.pbar);
        findViewById(R.id.sign_in_button).setOnClickListener(this);

        /* FireBase */
        mAuth = FirebaseAuth.getInstance();

        /* Google Login */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUI();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.standard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_info) {
            about_screen();
        }
        return super.onOptionsItemSelected(item);
    }

    private void about_screen() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.about_text)
                .setTitle(R.string.app_name)
                .setPositiveButton("OK", (dialog, id) -> dialog.cancel())
                .show();
    }

    public void register(View v){
        Intent i = new Intent(this, RegistrationActivity.class);
        startActivity(i);
    }

    public void updateUI() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            progressbar.setVisibility(View.VISIBLE);
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                fireBaseAuthWithGoogle(account);
            } catch (ApiException e) {
                progressbar.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void fireBaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                progressbar.setVisibility(View.INVISIBLE);
                if (task.isSuccessful()) {
                    updateUI();
                } else {
                    Snackbar sb = Snackbar.make(root, "\uD83D\uDE05\uD83D\uDE05\uD83D\uDE05 "
                            + task.getException().getMessage(), Snackbar.LENGTH_LONG);
                    sb.show();
                }
            });
    }

    public void login(View v) {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        progressbar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                progressbar.setVisibility(View.INVISIBLE);
                if (task.isSuccessful()) {
                    updateUI();
                } else {
                    Snackbar sb = Snackbar.make(v, "\uD83D\uDE05\uD83D\uDE05\uD83D\uDE05 "
                            + task.getException().getMessage(), Snackbar.LENGTH_LONG);
                    sb.show();
                }
            });
    }
}