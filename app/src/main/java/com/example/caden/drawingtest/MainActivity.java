package com.example.caden.drawingtest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

//    FireBase Storage
    FirebaseStorage mStorage;
    FirebaseAuth mAuth;

    ImageView imgV;

    SeekBar sb;

    Button btnColor;
    Button btnUpload;

    ProgressBar pBar;

    int brushColor;

    static int RESULT_LOAD_IMAGE = 1033;

    int currImgNo;
    TextView tvImgNo;
    TextView tvUserName;

    ImageManager im;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        im = new ImageManager();

        mStorage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        currImgNo = 0;
        tvImgNo = findViewById(R.id.tv_img_no);
        tvImgNo.setText(currImgNo + ".png");

        tvUserName = findViewById(R.id.tv_username);
        tvUserName.setText(user.getEmail());

        img_v_touch_handler();

        imgV = findViewById(R.id.imageView);
        imgV.setOnClickListener(view -> img_v_touch_handler());

        sb = findViewById((R.id.seekBar));

        btnColor = findViewById(R.id.btn_color);

        brushColor = btnColor.getCurrentTextColor();
        ImageManager.setBrushColor(brushColor);

        btnUpload = findViewById(R.id.btn_upload);
        pBar = findViewById(R.id.progress_bar);
    }

    public void temp(View v) {
        Intent i = new Intent(this, DrawingActivity.class);
        i.putExtra("color", brushColor);
        startActivity(i);
    }

    private void img_v_touch_handler() {
        currImgNo = (currImgNo == 18) ? 1 : currImgNo + 1;
        im.setImgFileName(String.valueOf(currImgNo));
        tvImgNo.setText(currImgNo + ".png");

        String path = "img/2017-09-23/" + currImgNo + ".png";

        StorageReference storageRef = mStorage.getReference();
        StorageReference pathReference = storageRef.child(path);

        final long ONE_KILOBYTE = 1024;
        pathReference.getBytes(ONE_KILOBYTE).addOnSuccessListener(bytes -> {
            im.setImage(bytes);
            im.setImageID(currImgNo);
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            imgV.setImageBitmap(Bitmap.createScaledBitmap(bmp, imgV.getWidth(),
                    imgV.getHeight(), false));
        }).addOnFailureListener(exception -> {
            // Handle any errors
        });
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
        }
        return false;
    }

    public void transition(View v) {
        Intent i = new Intent(this, DrawActivity.class);
//        i.putExtra("thickness", sb.getProgress());
//        i.putExtra("color", brushColor);
        startActivity(i);
    }

    public void loadImg(View v) {
        Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
        gallery.setType("image/*");
        startActivityForResult(gallery, RESULT_LOAD_IMAGE);
    }

    public byte[] sendImg() {
        Bitmap bitmap = ((BitmapDrawable) imgV.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public void on_upload(View v) {
        pBar.setVisibility(View.VISIBLE);
        btnUpload.setEnabled(false);

        String path = "uploaded/" + UUID.randomUUID() + ".png";
        StorageReference mStorageRef = mStorage.getReference(path);

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata("text", "my first upload")
                .build();
        UploadTask upTask = mStorageRef.putBytes(sendImg(), metadata);
        upTask.addOnSuccessListener(this, taskSnapshot -> {
            pBar.setVisibility(View.INVISIBLE);
            btnUpload.setEnabled(true);
        });
    }

    public void logout(View v){
        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            imgV.setImageURI(imageUri);
        }
    }

    public void changeColor(View v) {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose color")
                .initialColor(Color.parseColor("#FFFFFF"))
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener(selectedColor -> {})
                .setPositiveButton("ok", (dialog, selectedColor, allColors) -> {
                    btnColor.setTextColor(selectedColor);
                    brushColor = selectedColor;
                    ImageManager.setBrushColor(selectedColor);
                })
                .setNegativeButton("cancel", (dialog, which) -> {})
                .build()
                .show();
    }
}
