package com.caden.drawing.wurmpaint;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;

public class WurmMeterActivity extends AppCompatActivity {

    DatabaseReference mDatabase;
    ImageView ivInfo;
    TextView tvInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wurm_meter);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(0);
        }

        TextView tvScore = findViewById(R.id.tv_score);
        tvScore.setText(String.valueOf(SharedData.userScore));
        TextView tvHours = findViewById(R.id.tv_hours);
        float hr = (float) SharedData.userScore / 15;
        tvHours.setText(String.format(Locale.getDefault(), "%.1f", hr));
        ivInfo = findViewById(R.id.iv_info);
        tvInfo = findViewById(R.id.tv_info);

        if (SharedData.isGoogleSignIn) {
            findViewById(R.id.iv_tip).setVisibility(View.INVISIBLE);
            findViewById(R.id.tv_tip).setVisibility(View.INVISIBLE);
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("user_scores").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                updateTotal((HashMap) dataSnapshot.getValue());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void updateTotal(HashMap dict) {
        int total = 0;
        if (dict != null) for (Object i : dict.values()) total += Util.longToInt((Long) i);
        float contribution = (float) SharedData.userScore / total;
        tvInfo.setText(String.format(Locale.getDefault(),
                "%d Wurms annotated so far, you have contributed %.2f%% to the Wurm Community!",
                total, contribution * 100));
        if (ivInfo.getVisibility() == View.INVISIBLE) ivInfo.setVisibility(View.VISIBLE);
    }
}
