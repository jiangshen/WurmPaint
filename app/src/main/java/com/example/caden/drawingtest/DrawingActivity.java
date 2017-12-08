package com.example.caden.drawingtest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class DrawingActivity extends AppCompatActivity implements View.OnTouchListener {

    /* Constants */
    int dp56;

    /* Variables */
    String currBatchName;
    int currBatchSize;
    int currImgNo;
    Map uploadsDict;
    boolean alreadyDrawn;

    // views
    private DrawModel drawModel;
    private DrawView drawView;
    private PointF mTmpPoint = new PointF();

    private float mLastX;
    private float mLastY;

    private ImageManager im;
    int brushColor;
    Button btnBrushColor;

    /* FireBase */
    private FirebaseStorage mStorage;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    Toolbar toolbar;
    ProgressBar barSend;
    FloatingActionButton fabSend;
    TextView tvUserEmail;
    TextView tvImageName;

    ConstraintLayout clDrawMain;
    ConstraintSet constraintSet = new ConstraintSet();

    private static final int PIXEL_WIDTH = 280;
    private static final int PIXEL_HEIGHT = 280;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);
        toolbar = findViewById(R.id.drawing_toolbar);
        setSupportActionBar(toolbar);

        im = new ImageManager();
        mStorage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        brushColor = Color.parseColor("#FF2646");
        ImageManager.setBrushColor(brushColor);

        FirebaseUser user = mAuth.getCurrentUser();

        //get drawing view from XML (where the finger writes the number)
        drawView = findViewById(R.id.draw);

//        TODO where i put this function is very important!! due to firebase async!!
        fireBaseRetrieveImage();

        //get the model object
        drawModel = new DrawModel(PIXEL_WIDTH, PIXEL_HEIGHT);
        btnBrushColor = findViewById(R.id.btn_mark_bad);
//        brushColor = btnBrushColor.getBackgroundTintList();
        clDrawMain = findViewById(R.id.cl_draw_main);
        tvUserEmail = findViewById(R.id.tv_user_email);
        tvUserEmail.setText(user.getEmail());
        tvImageName = findViewById(R.id.tv_img_name);

        constraintSet.clone(clDrawMain);
        dp56 = dpToPx(56);
        fabSend = findViewById(R.id.fab_send);
        barSend = findViewById(R.id.pbar_send);

        //init the view with the model object
        drawView.setModel(drawModel);
        // give it a touch listener to activate when the user taps
        drawView.setOnTouchListener(this);

        /* Readjust the height to be almost the same as screen width */
        int scrWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int scrHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
//        Log.d("ratio", scrHeight/(float)scrWidth + "");
        drawView.getLayoutParams().height =
                (int)(Resources.getSystem().getDisplayMetrics().widthPixels * 0.85);
        drawView.requestLayout();
        alreadyDrawn = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawing_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_info) {
            about_screen();
        } else if (item.getItemId() == R.id.menu_sign_out) {
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    private void about_screen() {

        View aboutDialogView =
                getLayoutInflater().inflate(R.layout.about_dialog,
                        new ConstraintLayout(this), false);

        TextView feedbackText = aboutDialogView.findViewById(R.id.tv_feedback);
        RatingBar ratingBar = aboutDialogView.findViewById(R.id.ratingBar);

        String userUID = mAuth.getCurrentUser().getUid();
        String userEmail = mAuth.getCurrentUser().getEmail();

        new AlertDialog.Builder(this)
                .setView(aboutDialogView)
                .setMessage(R.string.about_text)
                .setTitle(R.string.app_name)
                .setPositiveButton("OK", (dialog, id) -> {
                    mDatabase.child("ratings").child(userUID)
                            .child("feedback").setValue(feedbackText.getText().toString());
                    mDatabase.child("ratings").child(userUID)
                            .child("rating").setValue(ratingBar.getRating());
                    mDatabase.child("ratings").child(userUID)
                            .child("email").setValue(userEmail);
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    //OnResume() is called when the user resumes his Activity which he left a while ago,
    // //say he presses home button and then comes back to app, onResume() is called.
    protected void onResume() {
        drawView.onResume();
        super.onResume();
    }

    @Override
    //OnPause() is called when the user receives an event like a call or a text message,
    // //when onPause() is called the Activity may be partially or completely hidden.
    protected void onPause() {
        drawView.onPause();
        super.onPause();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    //this method detects which direction a user is moving
    //their finger and draws a line accordingly in that
    //direction
    public boolean onTouch(View v, MotionEvent event) {
        //get the action and store it as an int
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        //actions have predefined ints, lets match
        //to detect, if the user has touched, which direction the users finger is
        //moving, and if they've stopped moving

        //if touched
        if (action == MotionEvent.ACTION_DOWN) {
            //begin drawing line
            processTouchDown(event);
            return true;
            //draw line in every direction the user moves
        } else if (action == MotionEvent.ACTION_MOVE) {
            processTouchMove(event);
            return true;
            //if finger is lifted, stop drawing
        } else if (action == MotionEvent.ACTION_UP) {
            processTouchUp();
            return true;
        }
        return false;
    }

    //draw line down

    private void processTouchDown(MotionEvent event) {
        if (!alreadyDrawn) {
            //calculate the x, y coordinates where the user has touched
            mLastX = event.getX();
            mLastY = event.getY();
            //user them to calculate the position
            drawView.calcPos(mLastX, mLastY, mTmpPoint);
            //store them in memory to draw a line between the
            //difference in positions
            float lastConvX = mTmpPoint.x;
            float lastConvY = mTmpPoint.y;
            //and begin the line drawing
            drawModel.startLine(lastConvX, lastConvY);
        }
    }

    //the main drawing function
    //it actually stores all the drawing positions
    //into the drawModel object
    //we actually render the drawing from that object
    //in the drawRenderer class
    private void processTouchMove(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        drawView.calcPos(x, y, mTmpPoint);
        float newConvX = mTmpPoint.x;
        float newConvY = mTmpPoint.y;
        drawModel.addLineElem(newConvX, newConvY);

        mLastX = x;
        mLastY = y;
        drawView.invalidate();
    }

    private void processTouchUp() {
        if (!alreadyDrawn) {
            drawModel.endLine();
            alreadyDrawn = true;
        }
    }

    public void clear(View v) {
        drawModel.clear();
        drawView.reset();
        drawView.invalidate();
        alreadyDrawn = false;
    }

    public void sendImage(View v) {

        int dvHeight = findViewById(R.id.cv_drawview).getHeight();

        TransitionManager.beginDelayedTransition(clDrawMain);
        constraintSet.constrainHeight(R.id.fab_send, 0);
        constraintSet.constrainWidth(R.id.fab_send, 0);
        constraintSet.constrainWidth(R.id.pbar_send, dp56);
        constraintSet.constrainHeight(R.id.pbar_send, dp56);
        constraintSet.constrainHeight(R.id.cv_drawview, 0);
        constraintSet.constrainHeight(R.id.drawing_ll, dvHeight / 2);
        constraintSet.applyTo(clDrawMain);

        Bitmap bmp = drawView.getBitmapData();
        ByteArrayOutputStream baOS = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 80, baOS);

        UUID uuid = UUID.randomUUID();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd EEE", Locale.US);
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
        Date date = new Date();
        FirebaseUser u = mAuth.getCurrentUser();

        /* Upload Drawing */
        String path = "uploaded/" + currBatchName + "/" + currImgNo + "/" + uuid + ".jpg";
        StorageReference mStorageRef = mStorage.getReference(path);

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata("text", "my first upload")
                .build();
        UploadTask upTask = mStorageRef.putBytes(baOS.toByteArray(), metadata);
        upTask.addOnSuccessListener(this, taskSnapshot -> {
            TransitionManager.beginDelayedTransition(clDrawMain);
            constraintSet.constrainWidth(R.id.pbar_send, 0);
            constraintSet.constrainHeight(R.id.pbar_send, 0);
            constraintSet.constrainHeight(R.id.fab_send, dp56);
            constraintSet.constrainWidth(R.id.fab_send, dp56);
            constraintSet.constrainHeight(R.id.cv_drawview, dvHeight);
            constraintSet.constrainHeight(R.id.drawing_ll, 0);
            constraintSet.applyTo(clDrawMain);
//            Load the next one
            nextImage(v);
        });

        /* Update Database Reference */
        mDatabase.child("uploads").child(currBatchName).child(String.valueOf(currImgNo)).child(dateFormat.format(date))
            .child(timeFormat.format(date)).child("image_name").setValue(uuid.toString());
        mDatabase.child("uploads").child(currBatchName).child(String.valueOf(currImgNo)).child(dateFormat.format(date))
                .child(timeFormat.format(date)).child("user_email").setValue(u.getEmail());
        mDatabase.child("uploads").child(currBatchName).child(String.valueOf(currImgNo)).child(dateFormat.format(date))
                .child(timeFormat.format(date)).child("user_uid").setValue(u.getUid());
    }

//    public void changeColor(View v) {
//        ColorPickerDialogBuilder
//                .with(this)
//                .setTitle("Pick Color")
//                .initialColor(Color.parseColor("#FFFFFF"))
//                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
//                .lightnessSliderOnly()
//                .density(16)
//                .setOnColorSelectedListener(selectedColor -> {})
//                .setPositiveButton("ok", (dialog, selectedColor, allColors) -> {
//                    btnBrushColor.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
//                    brushColor = selectedColor;
//                    ImageManager.setBrushColor(selectedColor);
//                })
//                .setNegativeButton("cancel", (dialog, which) -> {})
//                .build()
//                .show();
//    }

    public void markAsBad(View v) {
        /* Update Database Reference */
        View aboutDialogView =
                getLayoutInflater().inflate(R.layout.image_comments_dialog,
                        new ConstraintLayout(this), false);

        TextView reasonText = aboutDialogView.findViewById(R.id.tv_mark_reason);

        String userUID = mAuth.getCurrentUser().getUid();

        new AlertDialog.Builder(this)
                .setView(aboutDialogView)
                .setMessage(R.string.reason_marking_image)
                .setTitle(R.string.app_name)
                .setPositiveButton("OK", (dialog, id) -> {
                    mDatabase.child("bad_images").child(currBatchName).child(String.valueOf(currImgNo))
                            .child(userUID).setValue(reasonText.getText().toString());
                    nextImage(v);
                    dialog.dismiss();
                })
                .show();
    }

    private void fireBaseRetrieveImage() {
        mDatabase.child("master_upload").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                initWithImage((HashMap)dataSnapshot.getValue());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @SuppressLint("DefaultLocale")
    private void initWithImage(HashMap dict) {
//        Update
        uploadsDict = dict;
        Set keys = dict.keySet();
        int keyLen = keys.size();
        String[] keyArray = (String[]) keys.toArray(new String[keyLen]);
        Random rand = new Random();
//        0 based need to go from 0 to len - 1
        int randKey = rand.nextInt(keyLen);

        currBatchName = keyArray[randKey];
        currBatchSize = longToInt((Long) dict.get(currBatchName));
        currImgNo = rand.nextInt(currBatchSize) + 1;
        tvImageName.setText(String.format("%s / %d.png", currBatchName, currImgNo));

        /* Load Drawing */
        String path = String.format("img/%s/%d.png", currBatchName, currImgNo);
        StorageReference mStorageRef = mStorage.getReference(path);

        final long ONE_KILOBYTE = 1024;
        mStorageRef.getBytes(ONE_KILOBYTE).addOnSuccessListener(bytes -> {
            im.setImage(bytes);
            clear(findViewById(R.id.cl_draw_main));
        }).addOnFailureListener(exception -> {
            // Handle any errors
        });
    }

    public void nextImage(View v) {
        Set keys = uploadsDict.keySet();
        int keyLen = keys.size();
        String[] keyArray = (String[]) keys.toArray(new String[keyLen]);
        Random rand = new Random();
        int randKey = rand.nextInt(keyLen);
        currBatchName = keyArray[randKey];
        currBatchSize = longToInt((Long) uploadsDict.get(currBatchName));
        currImgNo = rand.nextInt(currBatchSize) + 1;
        tvImageName.setText(String.format(Locale.US,"%s / %d.png", currBatchName, currImgNo));

        /* Load Drawing */
        String path = String.format(Locale.US,"img/%s/%d.png", currBatchName, currImgNo);
        StorageReference mStorageRef = mStorage.getReference(path);

        final long ONE_KILOBYTE = 1024;
        mStorageRef.getBytes(ONE_KILOBYTE).addOnSuccessListener(bytes -> {
            im.setImage(bytes);
            clear(v);
        }).addOnFailureListener(exception -> {
            // Handle any errors
        });
    }

    /**
     * Converts dp into pixel values
     * @param dp    display pixels
     * @return      pixel values
     */
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, getResources().getDisplayMetrics());
    }

    private int longToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }
}
