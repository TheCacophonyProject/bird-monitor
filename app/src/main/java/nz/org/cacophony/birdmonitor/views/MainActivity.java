package nz.org.cacophony.birdmonitor.views;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.crashlytics.android.Crashlytics;

import nz.org.cacophony.birdmonitor.Prefs;
import nz.org.cacophony.birdmonitor.R;
import nz.org.cacophony.birdmonitor.Util;


public class MainActivity extends AppCompatActivity {
    // Register with idling counter
    // https://developer.android.com/training/testing/espresso/idling-resource.html
    // stackoverflow.com/questions/25470210/using-espresso-idling-resource-with-multiple-activities // this gave me idea to use an interface for app under test activities e.g MainActivity
    // https://www.youtube.com/watch?v=uCtzH0Rz5XU

    private static final String TAG = MainActivity.class.getName();

    private static long advancedButtonDownTime = 0;
    private static long advancedButtonUpTime = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        //https://developer.android.com/training/appbar/setting-up#java
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // https://stackoverflow.com/questions/3488664/how-to-set-different-label-for-launcher-rather-than-activity-title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }

        final Prefs prefs = new Prefs(this.getApplicationContext());
        prefs.setRecordingDurationSeconds();
        prefs.setTimeBetweenFrequentRecordingsSeconds();
        prefs.setTimeBetweenVeryFrequentRecordingsSeconds();
        prefs.setTimeBetweenGPSLocationUpdatesSeconds();
        prefs.setDawnDuskOffsetMinutes();
        prefs.setDawnDuskIncrementMinutes();
        prefs.setLengthOfTwilightSeconds();
        prefs.setTimeBetweenUploadsSeconds();
        prefs.setTimeBetweenFrequentUploadsSeconds();
        prefs.setBatteryLevelCutoffRepeatingRecordings();
        prefs.setBatteryLevelCutoffDawnDuskRecordings();

        boolean isFirstTime = prefs.getIsFirstTime();

        if (isFirstTime) {
            prefs.setDateTimeLastRepeatingAlarmFiredToZero();
            prefs.setDateTimeLastUpload(0);
            prefs.setInternetConnectionMode("normal");
            prefs.setAudioSource("MIC");
            prefs.setAutomaticRecordingsDisabled(false);
            prefs.setIsDisableDawnDuskRecordings(false);
            prefs.setVeryAdvancedSettingsEnabled(false);
            prefs.setRecLength(1);
            prefs.setIsFirstTimeFalse();
            prefs.setAutoUpdateAllowed();
        }

        final Button advancedButton = findViewById(R.id.btnAdvanced);

        if (prefs.getVeryAdvancedSettingsEnabled()) {
            advancedButton.setText(getResources().getString(R.string.very_advanced));
        }

        advancedButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    advancedButtonDownTime = System.currentTimeMillis();
                    break;

                case MotionEvent.ACTION_UP:
                    advancedButtonUpTime = System.currentTimeMillis();
                    long timeBetweenDownAndUp = advancedButtonUpTime - advancedButtonDownTime;

                    if (timeBetweenDownAndUp > 5000) {
                        if (prefs.getVeryAdvancedSettingsEnabled()) {
                            advancedButton.setText(getResources().getString(R.string.advanced));
                            prefs.setVeryAdvancedSettingsEnabled(false);

                        } else {
                            advancedButton.setText(getResources().getString(R.string.very_advanced));
                            prefs.setVeryAdvancedSettingsEnabled(true);

                        }

                        // Add or Remove Settings for Test Server fragment


                    } else {
                        startActivity(new Intent(MainActivity.this, AdvancedWizardActivity.class));
                    }
                    break;
            }

            return true;
        });

        // Now create the alarms that will cause the recordings to happen
        Util.createTheNextSingleStandardAlarm(getApplicationContext(), null);
        Util.createFailSafeAlarm(getApplicationContext());

        Crashlytics.setUserEmail(prefs.getEmailAddress());
        Crashlytics.setUserName(prefs.getUsername());
        Crashlytics.setUserIdentifier(String.format("%s-%s-%d", prefs.getGroupName(), prefs.getDeviceName(), prefs.getDeviceId()));
        // Open the Setup wizard if the app does not yet have device name
        if (prefs.getDeviceName() == null) {
            startActivity(new Intent(MainActivity.this, SetupWizardActivity.class));
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        Prefs prefs = new Prefs(this.getApplicationContext());


        if (prefs.getAutomaticRecordingsDisabled()) {
            ((Button) findViewById(R.id.btnDisable)).setText(getResources().getString(R.string.enable_recording));
            findViewById(R.id.btnDisable).setBackgroundColor(getResources().getColor(R.color.recordingDisabledButton));
        } else {
            ((Button) findViewById(R.id.btnDisable)).setText(getResources().getString(R.string.disable_recording));
            findViewById(R.id.btnDisable).setBackgroundColor(getResources().getColor(R.color.colorAccent));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_help, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.button_help:
                Util.displayHelp(this, getResources().getString(R.string.app_icon_name));
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    public void launchSetupActivity(@SuppressWarnings("UnusedParameters") View v) {

        try {
            startActivity(new Intent(MainActivity.this, SetupWizardActivity.class));
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage(), ex);
        }
    }


    public void launchBirdCountActivity(@SuppressWarnings("UnusedParameters") View v) {
        try {
            Intent intent = new Intent(this, BirdCountActivity.class);
            startActivity(intent);
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage(), ex);
        }
    }

    public void launchGPSActivity(@SuppressWarnings("UnusedParameters") View v) {
        try {
            Intent intent = new Intent(this, GPSActivity.class);
            startActivity(intent);
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage(), ex);
        }
    }

    public void launchVitalsActivity(@SuppressWarnings("UnusedParameters") View v) {
        try {
            Intent intent = new Intent(this, VitalsActivity.class);
            startActivity(intent);
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage(), ex);
        }
    }


    public void launchDisableActivity(@SuppressWarnings("UnusedParameters") View v) {
        try {
            Intent intent = new Intent(this, DisableAutomaticRecordingActivity.class);
            startActivity(intent);
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage(), ex);
        }
    }

}
