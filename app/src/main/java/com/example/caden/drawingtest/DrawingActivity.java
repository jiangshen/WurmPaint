package com.example.caden.drawingtest;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
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

public class DrawingActivity extends AppCompatActivity
        implements View.OnTouchListener, NavigationView.OnNavigationItemSelectedListener {

    /* Constants */
    int dp56;
    private static final int PIXEL_WIDTH = 280;
    private static final int PIXEL_HEIGHT = 280;
    private static final int RC_ACHIEVEMENT_UI = 9003;
    private static final int RC_LEADERBOARD_UI = 9004;
    private static final int RC_SETTING_UI = 9005;

    /* Variables */
    String currBatchName;
    int currBatchSize;
    int currImgNo;
    Map uploadsDict;
    boolean alreadyDrawn;

    /* Views */
    private DrawModel drawModel;
    private DrawView drawView;
    private PointF mTmpPoint = new PointF();

    private float mLastX;
    private float mLastY;

    Button btnMarkBad;

    /* FireBase */
    private FirebaseStorage mStorage;
    private DatabaseReference mDatabase;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseUser mUser;

    /* Play Games */
    GoogleSignInAccount gAcct;
    AchievementsClient mAchClient;
    LeaderboardsClient mLeadClient;

    Toolbar toolbar;
    ProgressBar barSend;
    FloatingActionButton fabSend;
    TextView navUserEmail;
    TextView navUserName;
    TextView tvImageName;
    TextView tvUserScore;

    String userUID;
    String userEmail;
    int userScore;

    ConstraintLayout clDrawMain;
    ConstraintSet constraintSet = new ConstraintSet();
    DrawerLayout drawer;
    NavigationView navView;
    View navHeaderLayout;

    boolean isGoogleSignIn = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        /* Setting up toolbar */
        toolbar = findViewById(R.id.drawing_toolbar);
        setSupportActionBar(toolbar);

        /* Setting up navigation drawer */
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mStorage = FirebaseStorage.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        userUID = mUser.getUid();
        userEmail = mUser.getEmail();

        for (UserInfo u : mUser.getProviderData()) {
            if (u.getProviderId().equals("google.com")) {
                isGoogleSignIn = true;
            }
        }

        // get drawing view from XML (where the finger writes the number)
        drawView = findViewById(R.id.draw);

        /* Get Wurm image from FireBase */
        fireBaseRetrieveImage();
        fireBaseRetrieveUserScore();

        tvUserScore = findViewById(R.id.tv_user_score);
        tvUserScore.setText(String.valueOf(userScore));


        /* Get the Draw Model Object */
        drawModel = new DrawModel(PIXEL_WIDTH, PIXEL_HEIGHT);
        btnMarkBad = findViewById(R.id.btn_mark_bad);
        clDrawMain = findViewById(R.id.cl_draw_main);
        navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);
        navHeaderLayout = navView.getHeaderView(0);
        navUserEmail = navHeaderLayout.findViewById(R.id.nav_drawer_email);
        navUserEmail.setText(userEmail);
        tvImageName = findViewById(R.id.tv_img_name);

        navUserName = navHeaderLayout.findViewById(R.id.nav_drawer_name);
        String userName = mUser.getDisplayName();
        if (userName == null || userName.equals("")) {
            /* New users gets the default name */
            userName = getString(R.string.default_user_name);
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(userName)
                    .build();
            mUser.updateProfile(profileUpdates);
        }
        navUserName.setText(userName);

        if (!isGoogleSignIn) {
            Menu menu = navView.getMenu();
            menu.findItem(R.id.nav_achievements).setEnabled(false);
            menu.findItem(R.id.nav_leaderboard).setEnabled(false);
            menu.findItem(R.id.nav_gplay_setting).setEnabled(false);
        }

//        FIXME get user profile photo
//        ImageView navUserImgView = navHeaderLayout.findViewById(R.id.nav_imgview);
//        navUserImgView.setImageBitmap(getImageBitmap(mUser.getPhotoUrl().toString()));
//
//        Uri userPhotoUrl = mUser.getPhotoUrl();
//        if (userPhotoUrl != null){
//            try {
//                Bitmap userBMP = MediaStore.Images.Media.
//                        getBitmap(this.getContentResolver(), userPhotoUrl);
//                navUserImgView.setImageBitmap(userBMP);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        constraintSet.clone(clDrawMain);
        dp56 = dpToPx(56);
        fabSend = findViewById(R.id.fab_send);
        barSend = findViewById(R.id.pbar_send);

        // init the view with the model object
        drawView.setModel(drawModel);
        // give it a touch listener to activate when the user taps
        drawView.setOnTouchListener(this);

        /* Re-adjust the height to be almost the same as screen width */
//        int scrWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
//        int scrHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
//        Log.d("ratio", scrHeight/(float)scrWidth + "");
        drawView.getLayoutParams().height =
                (int)(Resources.getSystem().getDisplayMetrics().widthPixels * 0.85);
        drawView.requestLayout();
        alreadyDrawn = false;

        /* Google Play Game */
        gAcct = GoogleSignIn.getLastSignedInAccount(this);
        if (gAcct != null) {
            Games.getGamesClient(this, gAcct).setViewForPopups(clDrawMain);
        }
        if (isGoogleSignIn) {
            mAchClient = Games.getAchievementsClient(this, gAcct);
            mLeadClient = Games.getLeaderboardsClient(this, gAcct);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_achievements && isGoogleSignIn) {
            showAchievements();
        } else if (id == R.id.nav_leaderboard && isGoogleSignIn) {
            showLeaderBoard();
        } else if (id == R.id.nav_gplay_setting && isGoogleSignIn) {
            showGPlaySettings();
        } else if (id == R.id.nav_logout) {
            logOut();
        } else if (id == R.id.nav_settings) {
            showSettings();
        } else if (id == R.id.nav_send_feedback) {
            feedbackScreen();
        } else if (id == R.id.nav_about) {
            showAbout();
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void showAchievements() {
        mAchClient.getAchievementsIntent()
                .addOnSuccessListener((intent -> startActivityForResult(intent, RC_ACHIEVEMENT_UI)));
    }

    private void showLeaderBoard() {
        mLeadClient.getLeaderboardIntent(getString(R.string.leaderboard_wurm_scores_id))
                .addOnSuccessListener((intent -> startActivityForResult(intent, RC_LEADERBOARD_UI)));
    }

    private void showGPlaySettings() {
        Games.getGamesClient(this, gAcct).getSettingsIntent()
                .addOnSuccessListener((intent -> startActivityForResult(intent, RC_SETTING_UI)));
    }

    private void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void feedbackScreen() {
        View aboutDialogView =
                getLayoutInflater().inflate(R.layout.dialog_feedback,
                        new ConstraintLayout(this), false);

        TextView feedbackText = aboutDialogView.findViewById(R.id.tv_feedback);
        RatingBar ratingBar = aboutDialogView.findViewById(R.id.ratingBar);

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
    // OnResume() is called when the user resumes his Activity which he left a while ago,
    // say he presses home button and then comes back to app, onResume() is called.
    protected void onResume() {
        drawView.onResume();
        if (GoogleSignIn.getLastSignedInAccount(this) == null) {
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
        }
        mUser.reload().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                navUserName.setText(mUser.getDisplayName());
                navUserEmail.setText(mUser.getEmail());
            }
        });
        super.onResume();
    }

    @Override
    // OnPause() is called when the user receives an event like a call or a text message,
    // when onPause() is called the Activity may be partially or completely hidden.
    protected void onPause() {
        drawView.onPause();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            logOut();
        }
    }

    private void logOut() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("YES", (dialog, id) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent i = new Intent(this, LoginActivity.class);
                    startActivity(i);
                })
                .setNegativeButton("NO", (dialog, id) -> dialog.cancel())
                .show();
    }

    private void showAbout() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_DOWN) {
            processTouchDown(event);
            return true;
        } else if (action == MotionEvent.ACTION_MOVE) {
            processTouchMove(event);
            return true;
        } else if (action == MotionEvent.ACTION_UP) {
            processTouchUp();
            return true;
        }
        return false;
    }

    private void processTouchDown(MotionEvent event) {
        if (!alreadyDrawn) {
            mLastX = event.getX();
            mLastY = event.getY();
            drawView.calcPos(mLastX, mLastY, mTmpPoint);
            float lastConvX = mTmpPoint.x;
            float lastConvY = mTmpPoint.y;
            drawModel.startLine(lastConvX, lastConvY);
        }
    }

    /**
     * The main drawing function
     * it actually stores all the drawing positions
     * into the drawModel object
     * we actually render the drawing from that object
     * in the drawRenderer class
     * @param event motion event
     */
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
        } else {
            Snackbar.make(drawer, "Please clear the screen before drawing again", Snackbar.LENGTH_SHORT)
                    .setAction("Dismiss", view -> {
                    })
                    .show();
        }
    }

    public void clear(View v) {
        drawModel.clear();
        drawView.reset();
        drawView.invalidate();
        alreadyDrawn = false;
    }

    public void sendImage(View v) {

        if (!alreadyDrawn) {
            Snackbar.make(drawer, "Draw something before sending", Snackbar.LENGTH_SHORT)
                    .setAction("Dismiss", view -> {
                    })
                    .show();
            return;
        }

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

        /* Upload Drawing */
        String path = "uploaded/" + currBatchName + "/" + currImgNo + "_" + uuid + ".jpg";
        StorageReference mStorageRef = mStorage.getReference(path);

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata("User Email", mUser.getEmail())
                .setCustomMetadata("User UUID", mUser.getUid())
                .setCustomMetadata("Batch Name", currBatchName)
                .setCustomMetadata("Image Number", String.valueOf(currImgNo))
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
            /* Load the next drawing */
            nextImage(v);
        });

        /* Update User Score internally, with FireBase and with Google Play Games */
        userScore++;
        if (mAchClient != null) {
            mAchClient.increment(getString(R.string.achievement_newbie_id), 1);

        }

        tvUserScore.setText(String.valueOf(userScore));
        mDatabase.child("user_scores").child(mUser.getUid()).setValue(userScore);
        mFirebaseAnalytics.setUserProperty("user_score", String.valueOf(userScore));

        /* Update Database Reference */
        mDatabase.child("uploads").child(currBatchName).child(String.valueOf(currImgNo)).child(dateFormat.format(date))
            .child(timeFormat.format(date)).child("image_name").setValue(uuid.toString());
        mDatabase.child("uploads").child(currBatchName).child(String.valueOf(currImgNo)).child(dateFormat.format(date))
                .child(timeFormat.format(date)).child("user_email").setValue(userEmail);
        mDatabase.child("uploads").child(currBatchName).child(String.valueOf(currImgNo)).child(dateFormat.format(date))
                .child(timeFormat.format(date)).child("user_uid").setValue(userUID);

        /* Upload Database Line Data */
        mDatabase.child("uploads").child(currBatchName).child(String.valueOf(currImgNo)).child(dateFormat.format(date))
                .child(timeFormat.format(date)).child("lines").setValue(SharedData.lineData);
    }

    public void markAsBad(View v) {
        /* Update Database Reference */
        View aboutDialogView =
                getLayoutInflater().inflate(R.layout.dialog_img_comments,
                        new ConstraintLayout(this), false);
        TextView reasonText = aboutDialogView.findViewById(R.id.tv_mark_reason);
        reasonText.setEnabled(false);
        RadioGroup rG = aboutDialogView.findViewById(R.id.radioGroup);
        RadioButton rbOther = aboutDialogView.findViewById(R.id.rb_other);
        rG.setOnCheckedChangeListener(((radioGroup, i) -> {
            if (i == rbOther.getId()) {
                reasonText.setEnabled(true);
            } else {
                reasonText.setEnabled(false);
            }
        }));

        new AlertDialog.Builder(this)
                .setView(aboutDialogView)
                .setMessage(R.string.reason_marking_image)
                .setTitle(R.string.app_name)
                .setPositiveButton("OK", (dialog, id) -> {
                    String textToSend = "";
                    int selId = rG.getCheckedRadioButtonId();
                    if (selId == rbOther.getId()) {
                        if (reasonText.getText() != null) {
                            textToSend = reasonText.getText().toString();
                        }
                    } else {
                        RadioButton selRB = aboutDialogView.findViewById(selId);
                        textToSend = selRB.getText().toString();
                    }
                    mDatabase.child("bad_images").child(currBatchName).child(String.valueOf(currImgNo))
                            .child(userUID).setValue(textToSend);
                    nextImage(v);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel())
                .show();
    }

    private void fireBaseRetrieveImage() {
        mDatabase.child("master_upload").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                initWithImage((HashMap) dataSnapshot.getValue());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void fireBaseRetrieveUserScore() {
        mDatabase.child("user_scores").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                readUserScore((HashMap)dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void readUserScore(HashMap dict) {
        userScore = 0;
        if (dict == null) {
            /* New user, populate FireBase with default score */
            mDatabase.child("user_scores").child(mUser.getUid()).setValue(userScore);
        } else {
            if (dict.containsKey(mUser.getUid())) {
                Long score = (Long)dict.get(mUser.getUid());
                userScore = score.intValue();
                mFirebaseAnalytics.setUserProperty("user_score", String.valueOf(userScore));
                tvUserScore.setText(String.valueOf(userScore));
            } else {
                /* First time again */
                mDatabase.child("user_scores").child(mUser.getUid()).setValue(userScore);
            }
        }
    }

    private void initWithImage(HashMap dict) {
        if (dict != null) {
            uploadsDict = dict;
            Set keys = dict.keySet();
            int keyLen = keys.size();
            String[] keyArray = (String[]) keys.toArray(new String[keyLen]);
            Random rand = new Random();
            int randKey = rand.nextInt(keyLen);

            currBatchName = keyArray[randKey];
            currBatchSize = longToInt((Long) dict.get(currBatchName));
            currImgNo = rand.nextInt(currBatchSize) + 1;
            tvImageName.setText(String.format(Locale.getDefault(), "%s / %d.png", currBatchName, currImgNo));

            /* Load Drawing */
            String path = String.format(Locale.getDefault(), "img/%s/%d.png", currBatchName, currImgNo);
            StorageReference mStorageRef = mStorage.getReference(path);

            final long FIVE_HUNDRED_KILOBYTE = 1024 * 500;
            mStorageRef.getBytes(FIVE_HUNDRED_KILOBYTE).addOnSuccessListener(bytes -> {
                SharedData.imgData = bytes;
                clear(findViewById(R.id.cl_draw_main));
            }).addOnFailureListener(exception -> {
            });
        }

    }

    public void nextImage(View v) {
        if (uploadsDict != null) {
            Set keys = uploadsDict.keySet();
            int keyLen = keys.size();
            String[] keyArray = (String[]) keys.toArray(new String[keyLen]);
            Random rand = new Random();
            int randKey = rand.nextInt(keyLen);
            currBatchName = keyArray[randKey];
            currBatchSize = longToInt((Long) uploadsDict.get(currBatchName));
            currImgNo = rand.nextInt(currBatchSize) + 1;
            tvImageName.setText(String.format(Locale.getDefault(),"%s / %d.png", currBatchName, currImgNo));

            /* Load Drawing */
            String path = String.format(Locale.getDefault(),"img/%s/%d.png", currBatchName, currImgNo);
            StorageReference mStorageRef = mStorage.getReference(path);

            final long FIVE_HUNDRED_KILOBYTE = 1024 * 500;
            mStorageRef.getBytes(FIVE_HUNDRED_KILOBYTE).addOnSuccessListener(bytes -> {
                SharedData.imgData = bytes;
                clear(v);
            }).addOnFailureListener(exception -> {
            });
        }

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
