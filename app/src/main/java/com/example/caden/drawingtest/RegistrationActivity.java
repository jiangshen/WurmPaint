package com.example.caden.drawingtest;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;

public class RegistrationActivity extends AppCompatActivity {

    EditText txt_email;
    EditText txt_psw;
    EditText txt_psw_cfm;

    FirebaseAuth mAuth;

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Toolbar toolbar = findViewById(R.id.registration_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        txt_email = findViewById(R.id.text_new_email);
        txt_psw = findViewById(R.id.text_psw);
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

    @SuppressWarnings("ConstantConditions")
    public void registerUser(View v) {
        String email = txt_email.getText().toString();
        String password = txt_psw.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        transition();
                    } else {
                        // If sign in fails, display a message to the user.
                        Snackbar sd = Snackbar.make(v,
                                String.format("\uD83D\uDE21\uD83D\uDE21\uD83D\uDE21 %s",
                                        task.getException().getMessage()), Snackbar.LENGTH_LONG);
                        sd.show();
                    }
                });

    }

    private void transition() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
}
