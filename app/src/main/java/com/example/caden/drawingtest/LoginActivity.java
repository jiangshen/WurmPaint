package com.example.caden.drawingtest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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

        /* Layout Setup */
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
        root = findViewById(R.id.cl_login);
        emailField = findViewById(R.id.text_email);
        passwordField = findViewById(R.id.text_password);
        passwordField.setOnEditorActionListener(((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                login(root);
            }
            return true;
        }));
        progressbar = findViewById(R.id.pbar);
        findViewById(R.id.sign_in_button).setOnClickListener(this);

        /* FireBase Auth */
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
                .setMessage("Created by Caden 2017")
                .setTitle(R.string.app_name)
                .setPositiveButton("OK", (dialog, id) -> dialog.cancel())
                .show();
    }

    public void register(View v){
        Intent i = new Intent(this, RegistrationActivity.class);
        startActivity(i);
    }

    public void updateUI() {
        Intent i = new Intent(this, DrawingActivity.class);
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

    @SuppressWarnings("ConstantConditions")
    private void fireBaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                progressbar.setVisibility(View.INVISIBLE);
                if (task.isSuccessful()) {
                    updateUI();
                } else {
                    Snackbar sb = Snackbar.make(root,
                            String.format("\uD83D\uDE05\uD83D\uDE05\uD83D\uDE05 %s",
                                    task.getException().getMessage()), Snackbar.LENGTH_LONG);
                    sb.show();
                }
            });
    }

    @SuppressWarnings("ConstantConditions")
    public void login(View v) {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        /* Reset Errors */
        emailField.setError(null);
        passwordField.setError(null);

        /* Email & Password Validation */
        if (email.equals("")) {
            emailField.setError("Email cannot be empty");
            emailField.requestFocus();
        } else if (password.equals("")) {
            passwordField.setError("Password cannot be empty");
            passwordField.requestFocus();
        } else if (!Util.isPasswordValid(password)) {
            passwordField.setError("Password cannot be less than 8 characters");
            passwordField.requestFocus();
        } else if (!Util.isEmailValid(email)) {
            emailField.setError("Email format is not valid");
            emailField.requestFocus();
        } else {
            /* Bring down the Keyboard */
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(root.getWindowToken(), 0);

            progressbar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        progressbar.setVisibility(View.INVISIBLE);
                        if (task.isSuccessful()) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user.isEmailVerified()) {
                                updateUI();
                            } else {
                                new AlertDialog.Builder(this)
                                        .setMessage("User is not verified, do you want to verify now?")
                                        .setTitle(R.string.app_name)
                                        .setPositiveButton("Yes", (dialog, id) -> {
                                            sendVerificationEmail(user);
                                        })
                                        .setNegativeButton("No", (dialog, id) -> {
                                            FirebaseAuth.getInstance().signOut();
                                            emailField.requestFocus();
                                        })
                                        .show();
                            }

                        } else {
                            Snackbar.make(v, "Forgot your password?", Snackbar.LENGTH_LONG)
                                    .setAction("Reset", view -> {

                                    })
                                    .show();
//                            emailField.setError(String.format("%s \uD83D\uDE05",
//                                    task.getException().getMessage()));
                            emailField.requestFocus();
                        }
                    });
        }
    }

    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Snackbar.make(root, "Email sent!", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(root, "Failed to send email, please try again",
                        Snackbar.LENGTH_LONG).show();

            }
        });
        FirebaseAuth.getInstance().signOut();
    }
}