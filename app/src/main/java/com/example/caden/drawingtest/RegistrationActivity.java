package com.example.caden.drawingtest;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegistrationActivity extends AppCompatActivity {

    final String TAG = "DT.RegistrationActivity";

    EditText txt_email;
    EditText txt_psw;
    EditText txt_psw_cfm;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        txt_email = findViewById(R.id.text_new_email);
        txt_psw = findViewById(R.id.text_psw);
        txt_psw_cfm = findViewById(R.id.text_psw_confirmation);

        mAuth = FirebaseAuth.getInstance();
    }

    public void registerUser(final View v) {
        String email = txt_email.getText().toString();
        String password = txt_psw.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            transition();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Snackbar sd = Snackbar.make(v, "\uD83D\uDE21\uD83D\uDE21\uD83D\uDE21 "
                                    + task.getException().getMessage(), Snackbar.LENGTH_LONG);
                            sd.show();
                        }
                    }
                });

    }

    private void transition() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
}
