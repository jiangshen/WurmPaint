package com.example.caden.drawingtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SettingsActivity extends AppCompatPreferenceActivity {

    private static FirebaseUser user;
    private static String userName;
    private static SharedPreferences sharedPrefs;

    private static Preference.OnPreferenceChangeListener sBindPrefSummaryToValListener = (pref, val) -> {
        String strVal = val.toString();
        if (pref instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) pref;
            int index = listPreference.findIndexOfValue(strVal);
            sharedPrefs.edit().putInt("wheel_type", index).apply();
            pref.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
        } else if (pref instanceof EditTextPreference) {
            if (pref.getKey().equals("user_name")) {
                userName = strVal;
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(userName)
                        .build();
                user.updateProfile(profileUpdates);
            }
            pref.setSummary(strVal);
        } else {
            pref.setSummary(strVal);
        }
        return true;
    };


    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPrefSummaryToValListener);
        sBindPrefSummaryToValListener.onPreferenceChange(preference, PreferenceManager
                .getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = FirebaseAuth.getInstance().getCurrentUser();
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new WurmPrefFragment()).commit();
        setupActionBar();

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("user_name", user.getDisplayName());
        editor.putString("user_email", user.getEmail());
        editor.apply();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || WurmPrefFragment.class.getName().equals(fragmentName);
    }


    public static class WurmPrefFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_wurm);
            setHasOptionsMenu(true);
            for (UserInfo u : user.getProviderData()) {
                if (u.getProviderId().equals("google.com")) {
                    findPreference("user_email").setEnabled(false);
                }
            }
            bindPreferenceSummaryToValue(findPreference("user_name"));
            bindPreferenceSummaryToValue(findPreference("user_email"));
            bindPreferenceSummaryToValue(findPreference("draw_color_type"));

            SwitchPreference colorPref = (SwitchPreference) findPreference("draw_color");
            if (SharedData.userScore < 125) {
                colorPref.setEnabled(false);
            } else {
                colorPref.setEnabled(true);
                /* Set true to it only one time */
                if(sharedPrefs.getBoolean("color_enabled_first_time", false)) {
                    colorPref.setChecked(true);
                    sharedPrefs.edit()
                            .putBoolean("color_enabled_first_time", true)
                            .putBoolean("draw_in_color", true).apply();
                }
                /* Set Listener for further changes */
                colorPref.setOnPreferenceChangeListener(((p, o) -> {
                    sharedPrefs.edit().putBoolean("draw_in_color", (boolean) o).apply();
                    return true;
                }));
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
