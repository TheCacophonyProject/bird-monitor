package nz.org.cacophony.cacophonometerlite;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

//import android.util.Log;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
//import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


//import org.slf4j.Logger;


import static android.widget.Toast.makeText;



public class MainActivity extends AppCompatActivity {

private static final String TAG = MainActivity.class.getName();
   private static final String intentAction = "nz.org.cacophony.cacophonometerlite.MainActivity";

//    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 0;
//    private static final int PERMISSION_RECORD_AUDIO = 1;
//    private static final int PERMISSION_LOCATION = 2;



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
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.setTitle(R.string.main_activity_name);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        Prefs prefs = new Prefs(this.getApplicationContext());
        prefs.setRecordingDurationSeconds();
        prefs.setNormalTimeBetweenRecordingsSeconds();
        prefs.setTimeBetweenFrequentRecordingsSeconds();
        prefs.setTimeBetweenVeryFrequentRecordingsSeconds();
        prefs.setDawnDuskOffsetMinutes();
        prefs.setDawnDuskIncrementMinutes();
        prefs.setLengthOfTwilightSeconds();
        prefs.setTimeBetweenUploadsSeconds();
        prefs.setTimeBetweenFrequentUploadsSeconds();
        prefs.setBatteryLevelCutoffRepeatingRecordings();
        prefs.setBatteryLevelCutoffDawnDuskRecordings();
        prefs.setDateTimeLastRepeatingAlarmFired(0);
        prefs.setDateTimeLastUpload(0);

        disableFlightMode(); // force app to ask for root permission as early as possible

     //   Util.createAlarms(getApplicationContext());
        Util.createAlarms(getApplicationContext(), "repeating", "normal");
        DawnDuskAlarms.configureDawnAndDuskAlarms(getApplicationContext(), false);


        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        if (ab != null){


        ab.setDisplayUseLogoEnabled(true);
        ab.setLogo(R.mipmap.ic_launcher);
        }else{
            Log.w(TAG, "ActionBar ab is null");
        }



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
        }   catch (Exception ex){
            // This is very poor, but I have no idea why super.onResume give a null pointer exception
            // Need to spend time on this
            Log.e(TAG, "Error calling super.onResume");
        }
        if (!RecordAndUpload.isRecording){
            ((Button) findViewById(R.id.recordNowButton)).setEnabled(true);
        }
        Prefs prefs = new Prefs(getApplicationContext());
        String mode = prefs.getMode();
        switch(mode) {
            case "off":
                final RadioButton  offModeRadioButton  = (RadioButton ) findViewById(R.id.offMode);
                offModeRadioButton.setChecked(true);
                break;
            case "normal":
                final RadioButton  normalModeRadioButton  = (RadioButton ) findViewById(R.id.normalMode);
                normalModeRadioButton.setChecked(true);
                break;
            case "normalOnline":
                final RadioButton  normalModeOnlineRadioButton  = (RadioButton ) findViewById(R.id.normalModeOnline);
                normalModeOnlineRadioButton.setChecked(true);
                break;
            case "walking":
                final RadioButton  walkingModeRadioButton  = (RadioButton ) findViewById(R.id.walkingMode);
                walkingModeRadioButton.setChecked(true);
                break;
        }



//        disableFlightMode();
//        checkPermissions();

       // Prefs prefs = new Prefs(getApplicationContext());

//    refreshVitalsDisplayedText();


//        // Application name text  appNameVersionText
//        // http://stackoverflow.com/questions/4616095/how-to-get-the-build-version-number-of-your-android-application
//        String versionName = BuildConfig.VERSION_NAME;
//        TextView versionNameText = (TextView) findViewById(R.id.appNameVersionText);
//        versionNameText.setText(getString(R.string.version) + " " + versionName);

      //  super.onResume();

        // listens for events broadcast from ?
        IntentFilter iff = new IntentFilter("event");
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);

      //  disableFlightMode();
    }

//    void refreshVitalsDisplayedText(){
//        Prefs prefs = new Prefs(getApplicationContext());
//        // Device registered text
//        TextView registered = (TextView) findViewById(R.id.mainRegisteredStatus);
//        if (prefs.getGroupName() != null)
//            registered.setText(getString(R.string.registered_true));
//        else
//            registered.setText(getString(R.string.registered_false));
//
//
//        // Logged In text.
//        TextView loggedInText = (TextView) findViewById(R.id.loggedInText);
//
//// Check the age of the webToken
//        boolean webTokenIsCurrent = Util.isWebTokenCurrent(prefs);
//
////        if (Server.loggedIn && webTokenIsCurrent)
//        if ( webTokenIsCurrent)
//            loggedInText.setText(getString(R.string.logged_in_to_server_true));
//        else
//            loggedInText.setText(getString(R.string.logged_in_to_server_false));
//
//        // Device ID text.
//        TextView deviceIDText = (TextView) findViewById(R.id.deviceIDText);
//        try {
//            String textServerPrefix = "";
//            if (prefs.getUseTestServer()){
//                textServerPrefix = "Test Server" + " ";
//            }
//
//            deviceIDText.setText(getString(R.string.device_id) + " " + textServerPrefix + Util.getDeviceID(getApplicationContext(),prefs.getToken()));
//        } catch (Exception e) {
//
////            logger.error("Device ID not available");
//            Log.e(TAG, "Device ID not available");
//        }
//
//        // GPS text.
//
//        try {
//            double lat = prefs.getLatitude();
//            double lon = prefs.getLongitude();
//
//            if (lat != 0 && lon != 0) {
//                TextView gpsText = (TextView) findViewById(R.id.gpsText);
//                gpsText.setText(getString(R.string.gps_text) + " " + "Latitude: "+lat+", Longitude: "+lon);
//            }
//
//        } catch (Exception e) {
//
////            logger.error("Device ID not available");
//            Log.e(TAG, "Device ID not available");
//        }
//    }

//    /**
//     * Checks if the app has the required permissions. Storage, Microphone, Location.
//     */
//    private void checkPermissions() {
//        if (!requestPermissions()) {
//            return;  // will need to press button again.
//        }
//        boolean storagePermission =
//                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
//        boolean microphonePermission =
//                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
//        boolean locationPermission =
//                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
//
//        TextView permissionText = (TextView) findViewById(R.id.appPermissionText);
//        if (storagePermission && microphonePermission && locationPermission) {
//            permissionText.setText(getString(R.string.required_permissions_true));
//
//            return;
//        } else {
//            permissionText.setText(getString(R.string.required_permissions_false));
//        }
//
//        List<String> missingPermissionList = new ArrayList<>();
//        if (!storagePermission) missingPermissionList.add("Write External Storage");
//        if (!microphonePermission) missingPermissionList.add("Recording");
//        if (!locationPermission) missingPermissionList.add("Location");
//
//        String missingPermissionMessage = "App not granted some permissions: " + StringUtils.join(missingPermissionList, ", ");
//
//        Util.getToast(getApplicationContext(),missingPermissionMessage, false ).show();
//
////        logger.warn(missingPermissionMessage );
//Log.w(TAG, missingPermissionMessage);
//
//    }


//public void setupButtonClick(@SuppressWarnings("UnusedParameters") View v) {
//    openSettings();
//}

    private void openSettings() {
        try{
//           disableFlightMode();
            Intent intent = new Intent(this, SetupActivity.class);
            startActivity(intent);
        }catch (Exception ex){
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

//    /**
//     * UI button to refresh vitals
//     * @param v View
//     */
//    public void refreshButton(@SuppressWarnings("UnusedParameters") View v) {
//        refreshVitals();
//    }

//    /**
//     * Check the vitals again and update the UI.
//     */
//    private void refreshVitals() {
//        ((Button) findViewById(R.id.refreshVitals)).setEnabled(false);
//       // ((Button) findViewById(R.id.testRecording)).setEnabled(false);
//
//
//
//        Util.getToast(getApplicationContext(),"About to update vitals - please wait a moment", false ).show();
//        try {
//
//            Thread server = new Thread() {
//                @Override
//                public void run() {
//                    Looper.prepare();
//                    Server.updateServerConnectionStatus(getApplicationContext());
////                    Message message = handler.obtainMessage();
////                    message.what = RESUME_AND_DISPLAY_REFRESH_MESSAGE;
////                    message.sendToTarget();
//                    Looper.loop();
//                }
//            };
//            server.start();
//        }catch (Exception ex){
//            Util.getToast(getApplicationContext(), "Error refreshing vitals", true).show();
//
//            Log.e(TAG, ex.getLocalizedMessage());
//            ((Button) findViewById(R.id.refreshVitals)).setEnabled(true);
//          //  ((Button) findViewById(R.id.testRecording)).setEnabled(true);
//
//        }
//    }



//    public  void disableFlightModeButtonClick(@SuppressWarnings("UnusedParameters") View v){
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
//            Prefs prefs = new Prefs(getApplicationContext());
//            if (!prefs.getHasRootAccess()){
//                Util.getToast(getApplicationContext(), "Root access required to change airplane mode", true).show();
//                return;
//            }
//
//        }
//        disableFlightMode();
//    }

     public void disableFlightMode(){
        try {
            //https://stackoverflow.com/questions/3875184/cant-create-handler-inside-thread-that-has-not-called-looper-prepare
        new Thread()
        {
            public void run()
            {
                MainActivity.this.runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        Util.disableFlightMode(getApplicationContext());
//                      String message = Util.disableFlightMode(getApplicationContext());

//                        if (message != null){
//                            ((TextView) findViewById(R.id.messageText)).setText("\n                                                                                                                " + message);
//                        }

                    }
                });
            }
        }.start();

        }catch (Exception ex){
            Log.e(TAG, ex.getLocalizedMessage());
            Util.getToast(getApplicationContext(), "Error disabling flight mode", true).show();
        }
    }

//    boolean requestPermissions(){
//        // If Android OS >= 6 then need to ask user for permission to Write External Storage, Recording, Location
////        https://developer.android.com/training/permissions/requesting.html
//
//        boolean allPermissionsAlreadyGranted = true;
//
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            allPermissionsAlreadyGranted = false;
//
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                        PERMISSION_WRITE_EXTERNAL_STORAGE);
//
//        }
//
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.RECORD_AUDIO)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            allPermissionsAlreadyGranted = false;
//
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.RECORD_AUDIO},
//                    PERMISSION_RECORD_AUDIO);
//
//        }
//
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            allPermissionsAlreadyGranted = false;
//
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    PERMISSION_LOCATION);
//
//        }
//
//
//
//        return allPermissionsAlreadyGranted;
//    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                                           int[] grantResults) {
//        // BEGIN_INCLUDE(onRequestPermissionsResult)
//        if (requestCode == PERMISSION_WRITE_EXTERNAL_STORAGE) {
//            // Request for camera permission.
//            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission has been granted. Start recording
//
//                Util.getToast(this.getApplicationContext(), "WRITE_EXTERNAL_STORAGE permission granted", false).show();
//            } else {
//                Util.getToast(this.getApplicationContext(), "Do not have WRITE_EXTERNAL_STORAGE permission, You can NOT save recordings", true).show();
//            }
//        }
//
//        if (requestCode == PERMISSION_RECORD_AUDIO) {
//            // Request for camera permission.
//            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission has been granted. Start recording
//
//                Util.getToast(this.getApplicationContext(), "RECORD_AUDIO permission granted", false).show();
//            } else {
//                Util.getToast(this.getApplicationContext(), "Do not have RECORD_AUDIO permission, You can NOT record", true).show();
//            }
//        }
//
//
//
//
//
//        // END_INCLUDE(onRequestPermissionsResult)
//    }

    public void recordNowButtonClicked(@SuppressWarnings("UnusedParameters") View v) {

     //   disableFlightMode();
//        Util.updateGPSLocation(getApplicationContext());
        Util.getToast(getApplicationContext(),"Prepare to start recording", false ).show();

        ((Button) findViewById(R.id.recordNowButton)).setEnabled(false);

        Intent myIntent = new Intent(MainActivity.this, StartRecordingReceiver.class);
        try {
            myIntent.putExtra("type", "recordNowButton");
            sendBroadcast(myIntent);

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
  //      DawnDuskAlarms.configureDawnAndDuskAlarms(getApplicationContext(), true);// use for testing, should remove
    }
    public void onModeRadioButtonClicked(@SuppressWarnings("UnusedParameters") View v) {
        Prefs prefs = new Prefs(getApplicationContext());
        boolean checked = ((RadioButton) v).isChecked();
        // Check which radio button was clicked
        switch(v.getId()) {
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
        Util.createAlarms(getApplicationContext(), "repeating", "normal");

    }


    private BroadcastReceiver onNotice= new BroadcastReceiver() {
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

        // broadcast notification coming from ??
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String message = intent.getStringExtra("message");
                if (message != null) {
//                    if (message.equalsIgnoreCase("enable_test_recording_button")) {
//                        ((Button) findViewById(R.id.testRecording)).setEnabled(true);
//                    }else if (message.equalsIgnoreCase("enable_vitals_button")) {
                    if (message.equalsIgnoreCase("enable_vitals_button")) {
                        ((Button) findViewById(R.id.refreshVitals)).setEnabled(true);
//                    }else if (message.equalsIgnoreCase("enable_disable_flight_mode_button")) {
//                        ((Button) findViewById(R.id.disableFlightMode)).setEnabled(true);
                    }else if (message.equalsIgnoreCase("tick_logged_in_to_server")){
                        TextView loggedInText = (TextView) findViewById(R.id.loggedInText);
                        loggedInText.setText(getString(R.string.logged_in_to_server_true));
                    }else if (message.equalsIgnoreCase("untick_logged_in_to_server")){
                        TextView loggedInText = (TextView) findViewById(R.id.loggedInText);
                        loggedInText.setText(getString(R.string.logged_in_to_server_false));
                    }else if (message.equalsIgnoreCase("recordNowButton_finished")) {
                        ((Button) findViewById(R.id.recordNowButton)).setEnabled(true);
                    }else if (message.equalsIgnoreCase("recording_started")){
                        Util.getToast(getApplicationContext(),"Recording started", false ).show();
                    }else if (message.equalsIgnoreCase("recording_finished")){
                        Util.getToast(getApplicationContext(),"Recording finished", false ).show();
                    }else if (message.equalsIgnoreCase("about_to_upload_files")){
                        Util.getToast(getApplicationContext(),"About to upload files", false ).show();
                    }else if (message.equalsIgnoreCase("files_successfully_uploaded")){
                        Util.getToast(getApplicationContext(),"Files successfully uploaded", false ).show();
                    }else if (message.equalsIgnoreCase("already_uploading")){
                        Util.getToast(getApplicationContext(),"Files are already uploading", false ).show();
                    }else if (message.equalsIgnoreCase("no_permission_to_record")){
                        Util.getToast(getApplicationContext(),"No permission to record", false ).show();
                    }else if (message.equalsIgnoreCase("recording_failed")){
                        Util.getToast(getApplicationContext(),"Failed to make recording", false ).show();
                    }else if (message.equalsIgnoreCase("recording_and_uploading_finished")){
                        Util.getToast(getApplicationContext(),"Recording and uploading finished", false ).show();
                    }else if (message.equalsIgnoreCase("recording_finished_but_uploading_failed")){
                        Util.getToast(getApplicationContext(),"Recording finished but uploading failed", false ).show();
                    }else if (message.equalsIgnoreCase("recorded_successfully_no_network")){
                        Util.getToast(getApplicationContext(),"Recorded successfully, no network connection so did not upload", false ).show();
                    }else if (message.equalsIgnoreCase("recording_failed")){
                        Util.getToast(getApplicationContext(),"Recording failed", true ).show();
                    }else if (message.equalsIgnoreCase("not_logged_in")){
                        Util.getToast(getApplicationContext(),"Not logged in to server, could not upload files", true ).show();
                    }else if (message.equalsIgnoreCase("is_already_recording")){
                        // Will need enable Record Now button
                        ((Button) findViewById(R.id.recordNowButton)).setEnabled(true);
                        Util.getToast(getApplicationContext(),"Could not do a recording as another recording is already in progress", true ).show();
                    }else if (message.equalsIgnoreCase("error_do_not_have_root")){
                        Util.getToast(getApplicationContext(),"It looks like you have incorrectly indicated in settings that this phone has been rooted", true ).show();
                    }
                }

            }catch (Exception ex){
//                logger.error(ex.getLocalizedMessage());
                Log.e(TAG,ex.getLocalizedMessage());
            }
        }
    };


}
