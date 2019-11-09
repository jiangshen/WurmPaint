package com.caden.drawing.wurmpaint;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private RVHistoryAdapter rvHistoryAdapter;
    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private String uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(0);
        }

        rvHistoryAdapter = new RVHistoryAdapter();
        rvHistory = findViewById(R.id.rv_history);
        rvHistory.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvHistory.setLayoutManager(layoutManager);
        rvHistory.setAdapter(rvHistoryAdapter);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) uid = user.getUid();
        getHistory();

    }

    private void getHistory() {
        if (SharedData.userTotal > 0) {
            mDatabase.child("user_history").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    GenericTypeIndicator<List<Wurm>> gtInd = new GenericTypeIndicator<List<Wurm>>(){};
                    List<Wurm> wurms = dataSnapshot.getValue(gtInd);
                    if (wurms != null) {
                        SharedData.totalWurmsDrawn = wurms.size();
                        for (int i = wurms.size() - 1; i >= 0; i--) {
                            updateHistoryItem(wurms.get(i), i);
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    private void updateHistoryItem(Wurm w, int i) {
        rvHistoryAdapter.addWurm(w);
        String path = String.format(Locale.getDefault(), "img/%s/%d.png", w.getWurmBatchName(), w.getWurmImgNo());
        StorageReference mStorageRef = mStorage.getReference(path);
        mStorageRef.getBytes(1024 * 500).addOnSuccessListener(bytes ->
                rvHistoryAdapter.updateImage(i, BitmapFactory.decodeByteArray(bytes, 0, bytes.length)));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
