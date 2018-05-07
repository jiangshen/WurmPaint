package com.caden.drawing.wurmpaint;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView tvChangeLog = findViewById(R.id.tv_change_log);
        tvChangeLog.setText(Html.fromHtml(getString(R.string.change_log_text)));
        tvChangeLog.setMovementMethod(LinkMovementMethod.getInstance());

        TextView tvAboutApp = findViewById(R.id.tv_about_app);
        tvAboutApp.setText(Html.fromHtml(getString(R.string.about_app_text)));
        tvAboutApp.setMovementMethod(LinkMovementMethod.getInstance());

        TextView tvPrivacyPolicy = findViewById(R.id.tv_privacy_policy);
        tvPrivacyPolicy.setText(Html.fromHtml(getString(R.string.privacy_policy_text)));
        tvPrivacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());

        TextView tvLicense = findViewById(R.id.tv_license);
        tvLicense.setText(Html.fromHtml(getString(R.string.license_text)));
        tvLicense.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
