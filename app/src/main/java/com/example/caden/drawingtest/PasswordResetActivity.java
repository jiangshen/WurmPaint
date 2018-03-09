package com.example.caden.drawingtest;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;

public class PasswordResetActivity extends AppCompatActivity {

    String passedEmail;
    EditText reenterEmailField;
    FirebaseAuth mAuth;

    View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Layout Setup */
        setContentView(R.layout.activity_password_reset);
        Toolbar toolbar = findViewById(R.id.psw_reset_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        rootView = findViewById(R.id.cl_psw_reset);
        Intent intent = getIntent();
        passedEmail = intent.getStringExtra("user-email");
        reenterEmailField = findViewById(R.id.text_reenter_email);
        reenterEmailField.setText(passedEmail);
        reenterEmailField.setOnEditorActionListener(((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) resetPassword(rootView);
            return true;
        }));

        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        backToLogin(rootView);
    }

    public void backToLogin(View v) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void resetPassword(View v) {
        String email = reenterEmailField.getText().toString();

        /* Reset Errors */
        reenterEmailField.setError(null);

        /* Validation */
        if (email.equals("")) {
            reenterEmailField.setError("Email cannot be empty");
            reenterEmailField.requestFocus();
        } else if (!Util.isEmailValid(email)) {
            reenterEmailField.setError("Email format is not valid");
            reenterEmailField.requestFocus();
        } else {
            /* Bring down the Keyboard */
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);

            mAuth.useAppLanguage();
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Snackbar.make(v, "Reset email sent", Snackbar.LENGTH_LONG)
                                    .setAction("Back to login", view -> {
                                        backToLogin(v);
                                    })
                                    .show();
                        } else {
                            Snackbar.make(v, "User may not exist, please enter correct email",
                                    Snackbar.LENGTH_LONG).show();
                        }

                    });
        }
    }
}
