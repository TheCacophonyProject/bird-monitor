package nz.org.cacophony.cacophonometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.test.espresso.IdlingRegistry;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements IdlingResourceForEspressoTesting{
    // Register with idling couunter
   // https://developer.android.com/training/testing/espresso/idling-resource.html
   // stackoverflow.com/questions/25470210/using-espresso-idling-resource-with-multiple-activities // this gave me idea to use an interface for app under test activities e.g MainActivity
    // https://www.youtube.com/watch?v=uCtzH0Rz5XU

    private static final String TAG = MainActivity.class.getName();
    private static final String intentAction = "nz.org.cacophony.cacophonometerlite.MainActivity";

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(intentAction);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Prefs prefs = new Prefs(this.getApplicationContext());
        prefs.setRecordingDurationSeconds();
        prefs.setNormalTimeBetweenRecordingsSeconds();
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
//        prefs.setDateTimeLastRepeatingAlarmFiredToZero();
//        prefs.setDateTimeLastUpload(0);
//        prefs.setInternetConnectionMode("normal");
//        prefs.setIsDisabled(false);
//        prefs.setIsDisableDawnDuskRecordings(false);

        if (prefs.getIsFirstTime()) {
            prefs.setDateTimeLastRepeatingAlarmFiredToZero();
            prefs.setDateTimeLastUpload(0);
            prefs.setInternetConnectionMode("normal");
            prefs.setIsDisabled(false);
            prefs.setIsDisableDawnDuskRecordings(false);

//            prefs.setOnLineMode(true);
            prefs.setIsFirstTime();

//            // Going to close this activity and open Wizard1 instead
//            Intent intent = new Intent(this, Wizard1Activity.class);
//            startActivity(intent);
//            finish();

        }

        // Now create the alarms that will cause the recordings to happen

        Util.createTheNextSingleStandardAlarm(getApplicationContext());
        DawnDuskAlarms.configureDawnAndDuskAlarms(getApplicationContext(), true);
        Util.createCreateAlarms(getApplicationContext());
        Util.setUpLocationUpdateAlarm(getApplicationContext());

    }

    @Override
    protected void onPause() {
        super.onPause();
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
    }

    @Override
    public void onResume() {
        super.onResume();
        Prefs prefs = new Prefs(this.getApplicationContext());

        if (prefs.getIsDisabled()){
            ((Button)findViewById(R.id.btnDisable)).setText("Enable Recording");
        }else{
            ((Button)findViewById(R.id.btnDisable)).setText("Disable Recording");
        }
        // listens for events broadcast from ?
        IntentFilter iff = new IntentFilter("event");
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);
    }

    @Override
    protected void onStop() {
        super.onStop();
     //   Log.e(TAG, "onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
      //  Log.e(TAG, "onRestart");
    }

    private void disableFlightMode() { // still need to think about when to test this without causing app to hang and user to get feedup
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Util.disableFlightMode(getApplicationContext());
                }
                catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "Error disabling flight mode");
                            Util.getToast(getApplicationContext(), "Error disabling flight mode", true).show();
                        }
                    });
                }
            }
        };
        thread.start();
    }

    public void launchSetupActivity(@SuppressWarnings("UnusedParameters") View v) {

        try {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    public void launchTestRecordActivity(@SuppressWarnings("UnusedParameters") View v) {
        try {
            Intent intent = new Intent(this, TestRecordActivity.class);
            startActivity(intent);
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    public void launchVitalsActivity(@SuppressWarnings("UnusedParameters") View v) {
        try {
            Intent intent = new Intent(this, VitalsActivity.class);
            startActivity(intent);
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    public void launchAdvancedActivity(@SuppressWarnings("UnusedParameters") View v) {
        try {
            Intent intent = new Intent(this, InternetConnectionActivity.class);
            startActivity(intent);
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    public void launchDisableActivity(@SuppressWarnings("UnusedParameters") View v) {
        try {
            Intent intent = new Intent(this, DisableActivity.class);
            startActivity(intent);
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }


    private final BroadcastReceiver onNotice = new BroadcastReceiver() {
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

        // broadcast notification coming from ??
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String message = intent.getStringExtra("message");
                if (message != null) {

                    if (message.equalsIgnoreCase("enable_vitals_button")) {
                        findViewById(R.id.refreshVitals).setEnabled(true);
                    } else if (message.equalsIgnoreCase("tick_logged_in_to_server")) {
                        TextView loggedInText = findViewById(R.id.loggedInText);
                        loggedInText.setText(getString(R.string.logged_in_to_server_true));
                    } else if (message.equalsIgnoreCase("untick_logged_in_to_server")) {
                        TextView loggedInText = findViewById(R.id.loggedInText);
                        loggedInText.setText(getString(R.string.logged_in_to_server_false));
                    }  else if (message.equalsIgnoreCase("recording_started")) {
                        Util.getToast(getApplicationContext(), "Recording started", false).show();
                    } else if (message.equalsIgnoreCase("recording_finished")) {
                        Util.getToast(getApplicationContext(), "Recording finished", false).show();
                    } else if (message.equalsIgnoreCase("about_to_upload_files")) {
                        Util.getToast(getApplicationContext(), "About to upload files", false).show();
                    } else if (message.equalsIgnoreCase("files_successfully_uploaded")) {
                        Util.getToast(getApplicationContext(), "Files successfully uploaded", false).show();
                    } else if (message.equalsIgnoreCase("already_uploading")) {
                        Util.getToast(getApplicationContext(), "Files are already uploading", false).show();
                    }  else if (message.equalsIgnoreCase("recording_and_uploading_finished")) {
                        Util.getToast(getApplicationContext(), "Recording and uploading finished", false).show();

                    } else if (message.equalsIgnoreCase("recorded_successfully_no_network")) {

                        Util.getToast(getApplicationContext(), "Recorded successfully, no network connection so did not upload", false).show();
                    } else if (message.equalsIgnoreCase("not_logged_in")) {

                        Util.getToast(getApplicationContext(), "Not logged in to server, could not upload files", true).show();
                    } else if (message.equalsIgnoreCase("error_do_not_have_root")) {
                        Util.getToast(getApplicationContext(), "It looks like you have incorrectly indicated in settings that this phone has been rooted", true).show();
                    }

                }

            } catch (Exception ex) {

                Log.e(TAG, ex.getLocalizedMessage());
            }
        }
    };
    /**
     * Only used by testing code
     */
    public void registerEspressoIdlingResources() {
        // https://developer.android.com/reference/android/support/test/espresso/IdlingRegistry
        //https://www.programcreek.com/java-api-examples/index.php?api=android.support.test.espresso.IdlingRegistry
        IdlingRegistry.getInstance().register(registerIdlingResource);
        IdlingRegistry.getInstance().register(recordNowIdlingResource);
        IdlingRegistry.getInstance().register(uploadingIdlingResource);
        IdlingRegistry.getInstance().register(toggleAirplaneModeIdlingResource);
    }
    /**
     * Only used by testing code
     */
    public void unRegisterEspressoIdlingResources() {
        IdlingRegistry.getInstance().unregister(registerIdlingResource);
        IdlingRegistry.getInstance().unregister(recordNowIdlingResource);
        IdlingRegistry.getInstance().unregister(uploadingIdlingResource);
        IdlingRegistry.getInstance().unregister(toggleAirplaneModeIdlingResource);

//        if (toggleAirplaneModeIdlingResource != null) {
//            IdlingRegistry.getInstance().unregister(toggleAirplaneModeIdlingResource);
//        }
    }

}
