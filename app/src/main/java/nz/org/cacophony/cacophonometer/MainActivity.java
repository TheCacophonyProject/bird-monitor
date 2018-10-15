package nz.org.cacophony.cacophonometer;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.test.espresso.IdlingRegistry;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.TextView;


/**
 * This MainActivity class creates the opening screen that is shown to the user.  It allows the user
 * to set the mode of operation,  and also access the menu to go to the settings and vitals
 * screens for further configuration.
 * <p>
 * There is also a 'Record Now' button that allows the user to make an immediate recording - useful
 * for testing, as well as when you happen to hear an interesting bird.  Apart from the Record Now
 * feature, this MainActivity does not take part in the normal operation of periodic recordings -
 * which happen when Alarms/Intents are fired.  Alarms are created by the BootReciever class, and the
 * StartRecordingReciever class listens/responds to the alarm Intents.  The Record Now button also just
 * sends an Intent to the StartRecordingRecciever class.
 *
 * MainActivity implements the interface IdlingResourceForEspressoTesting provides the ability for
 * Espresso testing of this code to wait for background operations
 * to complete - e.g. Can check if a recording happens.
 *
 *
 * @author Tim Hunt
 */
public class MainActivity extends AppCompatActivity implements IdlingResourceForEspressoTesting, ActivityCompat.OnRequestPermissionsResultCallback {

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
    protected void onPause() {
        super.onPause();

//        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD); // helps with testing: Now don't have to use airdroid to open screen, as long as screen has no pin etc

        this.setTitle(R.string.main_activity_name);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

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
        prefs.setDateTimeLastRepeatingAlarmFiredToZero();
        prefs.setDateTimeLastUpload(0);




        if (prefs.getIsFirstTime()) {
            // Set Keep Online to be the default
            prefs.setOnLineMode(true);
            prefs.setIsFirstTime();

//            // Going to close this activity and open Wizard1 instead
//            Intent intent = new Intent(this, Wizard1Activity.class);
//            startActivity(intent);
//            finish();

        } else {
            disableFlightMode(); // force app to ask for root permission as early as possible
        }

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayUseLogoEnabled(true);
            ab.setLogo(R.mipmap.ic_launcher);
        } else {
            Log.w(TAG, "ActionBar ab is null");
        }

        // Now create the alarms that will cause the recordings to happen
        Util.createAlarms(getApplicationContext());

        DawnDuskAlarms.configureDawnAndDuskAlarms(getApplicationContext(), true);
        Util.createCreateAlarms(getApplicationContext());
        Util.setUpLocationUpdateAlarm(getApplicationContext());


    } //end onCreate


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_settings:
                openSettings();
                return true;

            case R.id.action_vitals:
                openVitals();
                return true;

            case R.id.action_help:
                openHelp();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void openHelp() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    private void openVitals() {
        Intent intent = new Intent(this, VitalsActivity.class);
        startActivity(intent);
    }

    /**
     * Updated UI.
     */
    @Override
    public void onResume() {
        try {
            super.onResume();
        } catch (Exception ex) {
            // This is very poor, but I have no idea why super.onResume give a null pointer exception
            // Need to spend time on this
            Log.e(TAG, "Error calling super.onResume");
        }
        if (!RecordAndUpload.isRecording) {
            findViewById(R.id.recordNowButton).setEnabled(true);
        }
        Prefs prefs = new Prefs(getApplicationContext());
        String mode = prefs.getMode();
        switch (mode) {
            case "off":
                final RadioButton offModeRadioButton = findViewById(R.id.offMode);
                offModeRadioButton.setChecked(true);
                break;
            case "normal":
                final RadioButton normalModeRadioButton = findViewById(R.id.normalMode);
                normalModeRadioButton.setChecked(true);
                break;
            case "normalOnline":
                final RadioButton normalModeOnlineRadioButton = findViewById(R.id.normalModeOnline);
                normalModeOnlineRadioButton.setChecked(true);
                break;
            case "walking":
                final RadioButton walkingModeRadioButton = findViewById(R.id.walkingMode);
                walkingModeRadioButton.setChecked(true);
                break;
        }

        // listens for events broadcast from ?
        IntentFilter iff = new IntentFilter("event");
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);
    }


    private void openSettings() {
        try {

            Intent intent = new Intent(this, SetupActivity.class);
            startActivity(intent);
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }


    private void disableFlightMode() {
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



//    private void disableFlightMode() {
//        try {
//            //https://stackoverflow.com/questions/3875184/cant-create-handler-inside-thread-that-has-not-called-looper-prepare
//            new Thread() {
//                public void run() {
//                    MainActivity.this.runOnUiThread(new Runnable() {
//                        public void run() {
//                            Util.disableFlightMode(getApplicationContext());
//                        }
//                    });
//                }
//            }.start();
//
//        } catch (Exception ex) {
//            Log.e(TAG, ex.getLocalizedMessage());
//            Util.getToast(getApplicationContext(), "Error disabling flight mode", true).show();
//        }
//    }


    public void recordNowButtonClicked(@SuppressWarnings("UnusedParameters") View v) {

        recordNowIdlingResource.increment();

        Util.getToast(getApplicationContext(), "Prepare to start recording", false).show();

        findViewById(R.id.recordNowButton).setEnabled(false);

        Intent myIntent = new Intent(MainActivity.this, StartRecordingReceiver.class);
        myIntent.putExtra("callingCode", "recordNowButtonClicked"); // for debugging
        try {
            myIntent.putExtra("type", "recordNowButton");
            sendBroadcast(myIntent);

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }

        Util.createCreateAlarms(getApplicationContext());
    }

    public void onModeRadioButtonClicked(@SuppressWarnings("UnusedParameters") View v) {
        Prefs prefs = new Prefs(getApplicationContext());
        boolean checked = ((RadioButton) v).isChecked();
        // Check which radio button was clicked
        switch (v.getId()) {
            case R.id.offMode:
                if (checked) {
                    prefs.setMode("off");
                }
                break;
            case R.id.normalMode:
                if (checked) {
                    prefs.setMode("normal");
                }
                break;
            case R.id.normalModeOnline:
                if (checked) {
                    prefs.setMode("normalOnline");
                }
                break;
            case R.id.walkingMode:
                if (checked) {
                    prefs.setMode("walking");
                }
                break;
        }
        // need to reset alarms as their frequency may have changed.
        Util.createAlarms(getApplicationContext());
        Util.setUpLocationUpdateAlarm(getApplicationContext());
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
                    } else if (message.equalsIgnoreCase("recordNowButton_finished")) {
                        findViewById(R.id.recordNowButton).setEnabled(true);
                        recordNowIdlingResource.decrement();

                    } else if (message.equalsIgnoreCase("recording_started")) {
                        Util.getToast(getApplicationContext(), "Recording started", false).show();
                    } else if (message.equalsIgnoreCase("recording_finished")) {
                        Util.getToast(getApplicationContext(), "Recording finished", false).show();
                    } else if (message.equalsIgnoreCase("about_to_upload_files")) {
                        Util.getToast(getApplicationContext(), "About to upload files", false).show();
                    } else if (message.equalsIgnoreCase("files_successfully_uploaded")) {
                        Util.getToast(getApplicationContext(), "Files successfully uploaded", false).show();
                    } else if (message.equalsIgnoreCase("already_uploading")) {
                        Util.getToast(getApplicationContext(), "Files are already uploading", false).show();
                    } else if (message.equalsIgnoreCase("no_permission_to_record")) {
                        Util.getToast(getApplicationContext(), "Can not record.  Please go to Android settings and enable all required permissions for this app", true).show();
                        findViewById(R.id.recordNowButton).setEnabled(true);
                        recordNowIdlingResource.decrement();
                    } else if (message.equalsIgnoreCase("recording_and_uploading_finished")) {
                        Util.getToast(getApplicationContext(), "Recording and uploading finished", false).show();

                    } else if (message.equalsIgnoreCase("recording_finished_but_uploading_failed")) {
                        Util.getToast(context, "Recording finished but uploading failed", true).show();
                        findViewById(R.id.recordNowButton).setEnabled(true);
                        recordNowIdlingResource.decrement();
                    } else if (message.equalsIgnoreCase("recorded_successfully_no_network")) {

                        Util.getToast(getApplicationContext(), "Recorded successfully, no network connection so did not upload", false).show();
                    } else if (message.equalsIgnoreCase("recording_failed")) {
                        Util.getToast(getApplicationContext(), "Recording failed", true).show();
                        findViewById(R.id.recordNowButton).setEnabled(true);
                    } else if (message.equalsIgnoreCase("not_logged_in")) {

                        Util.getToast(getApplicationContext(), "Not logged in to server, could not upload files", true).show();
                    } else if (message.equalsIgnoreCase("is_already_recording")) {                  //      uploadingIdlingResource.decrement();
                        // Will need enable Record Now button
                        findViewById(R.id.recordNowButton).setEnabled(true);
                        recordNowIdlingResource.decrement();

                        Util.getToast(getApplicationContext(), "Could not do a recording as another recording is already in progress", true).show();
                        findViewById(R.id.recordNowButton).setEnabled(true);
                        recordNowIdlingResource.decrement();
                    } else if (message.equalsIgnoreCase("error_do_not_have_root")) {
                        Util.getToast(getApplicationContext(), "It looks like you have incorrectly indicated in settings that this phone has been rooted", true).show();
                    }else if (message.equalsIgnoreCase("update_record_now_button")){
                        findViewById(R.id.recordNowButton).setEnabled(!RecordAndUpload.isRecording);
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
