package com.example.caden.drawingtest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;

public class RegistrationActivity extends AppCompatActivity {

    EditText txt_email;
    EditText txt_psw;
    EditText txt_psw_cfm;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Toolbar toolbar = findViewById(R.id.registration_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        txt_email = findViewById(R.id.text_new_email);
        txt_psw = findViewById(R.id.text_psw);
        txt_psw.setOnEditorActionListener(((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                registerUser(findViewById(R.id.cl_register_main));
            }
            return true;
        }));
        txt_psw_cfm = findViewById(R.id.text_psw_confirmation);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_standard, menu);
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

    public void registerUser(View v) {
        String email = txt_email.getText().toString();
        String password = txt_psw.getText().toString();

        /* Email & Password Validation */
        if (email.equals("")) {
            txt_email.setError("Email cannot be empty");
            txt_email.requestFocus();
        } else if (password.equals("")) {
            txt_psw.setError("Password cannot be empty");
            txt_psw.requestFocus();
        } else if (!Util.isPasswordValid(password)) {
            txt_psw.setError("Password cannot be less than 8 characters");
            txt_psw.requestFocus();
        } else if (!Util.isEmailValid(email)) {
            txt_email.setError("Email format is not valid");
            txt_email.requestFocus();
        } else {
            /* Bring down the Keyboard */
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            transition();
                        } else {
                            String msg = task.getException() == null ?
                                    "Something went wrong, please try again!" :
                                    task.getException().getMessage();
                            Snackbar sd = Snackbar.make(v,
                                    String.format("\uD83D\uDE21\uD83D\uDE21\uD83D\uDE21 %s", msg),
                                    Snackbar.LENGTH_LONG);
                            sd.show();
                        }
                    });
        }
    }

    private void transition() {
        Intent i = new Intent(this, DrawingActivity.class);
        startActivity(i);
    }
}
