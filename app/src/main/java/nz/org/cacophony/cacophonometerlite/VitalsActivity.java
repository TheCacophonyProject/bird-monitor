package nz.org.cacophony.cacophonometerlite;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;



public class VitalsActivity extends AppCompatActivity implements IdlingResourceForEspressoTesting{
    // Register with idling couunter
// https://developer.android.com/training/testing/espresso/idling-resource.html
// stackoverflow.com/questions/25470210/using-espresso-idling-resource-with-multiple-activities // this gave me idea to use an inteface for app under test activities e.g MainActivity
    // https://www.youtube.com/watch?v=uCtzH0Rz5XU

    private static final String TAG = VitalsActivity.class.getName();
    private static final String intentAction = "nz.org.cacophony.cacophonometerlite.VitalsActivity";

    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 0;
    private static final int PERMISSION_RECORD_AUDIO = 1;
    private static final int PERMISSION_LOCATION = 2;

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(intentAction);
    }

    @Override
    protected void onPause() {
        super.onPause();

        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vitals);


        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayUseLogoEnabled(true);
            ab.setLogo(R.mipmap.ic_launcher);
        } else {
            Log.w(TAG, "ActionBar ab is null");

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_vitals, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_settings:
                openSettings();
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

    private void openSettings() {
        try{
        //    disableFlightMode();
            Intent intent = new Intent(this, SetupActivity.class);
            startActivity(intent);
        }catch (Exception ex){
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    public void disableFlightMode(){
        try {
            //https://stackoverflow.com/questions/3875184/cant-create-handler-inside-thread-that-has-not-called-looper-prepare
            new Thread()
            {
                public void run()
                {
                    VitalsActivity.this.runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            String message = Util.disableFlightMode(getApplicationContext());

                            if (message != null){
                                ((TextView) findViewById(R.id.messageText)).setText("\n                                                                                                                " + message);
                            }

                        }
                    });
                }
            }.start();

        }catch (Exception ex){
            Log.e(TAG, ex.getLocalizedMessage());
            Util.getToast(getApplicationContext(), "Error disabling flight mode", true).show();
        }
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

     //   disableFlightMode();
        checkPermissions();
        refreshVitalsDisplayedText();

        // Application name text  appNameVersionText
        // http://stackoverflow.com/questions/4616095/how-to-get-the-build-version-number-of-your-android-application
        String versionName = BuildConfig.VERSION_NAME;
        TextView versionNameText = (TextView) findViewById(R.id.appNameVersionText);
        versionNameText.setText(getString(R.string.version) + " " + versionName);

        // listens for events broadcast from ?
        IntentFilter iff = new IntentFilter("event");
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);

        disableFlightMode();
    }

    /**
     * Checks if the app has the required permissions. Storage, Microphone, Location.
     */
    private void checkPermissions() {
        if (!requestPermissions()) {
            return;  // will need to press button again.
        }
        boolean storagePermission =
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean microphonePermission =
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        boolean locationPermission =
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        TextView permissionText = (TextView) findViewById(R.id.appPermissionText);
        if (storagePermission && microphonePermission && locationPermission) {
            permissionText.setText(getString(R.string.required_permissions_true));

            return;
        } else {
            permissionText.setText(getString(R.string.required_permissions_false));
        }

        List<String> missingPermissionList = new ArrayList<>();
        if (!storagePermission) missingPermissionList.add("Write External Storage");
        if (!microphonePermission) missingPermissionList.add("Recording");
        if (!locationPermission) missingPermissionList.add("Location");

        String missingPermissionMessage = "App not granted some permissions: " + StringUtils.join(missingPermissionList, ", ");

        Util.getToast(getApplicationContext(),missingPermissionMessage, false ).show();

//        logger.warn(missingPermissionMessage );
        Log.w(TAG, missingPermissionMessage);

    }

    boolean requestPermissions(){
        // If Android OS >= 6 then need to ask user for permission to Write External Storage, Recording, Location
//        https://developer.android.com/training/permissions/requesting.html

        boolean allPermissionsAlreadyGranted = true;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            allPermissionsAlreadyGranted = false;

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_WRITE_EXTERNAL_STORAGE);

        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            allPermissionsAlreadyGranted = false;

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_RECORD_AUDIO);

        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            allPermissionsAlreadyGranted = false;

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_LOCATION);

        }



        return allPermissionsAlreadyGranted;
    }

    /**
     * UI button to refresh vitals
     * @param v View
     */
    public void refreshButton(@SuppressWarnings("UnusedParameters") View v) {
        refreshVitals();
    }

    /**
     * Check the vitals again and update the UI.
     */
    private void refreshVitals() {
        ((Button) findViewById(R.id.refreshVitals)).setEnabled(false);
        // ((Button) findViewById(R.id.testRecording)).setEnabled(false);



        Util.getToast(getApplicationContext(),"About to update vitals - please wait a moment", false ).show();
        try {

            Thread server = new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    Server.updateServerConnectionStatus(getApplicationContext());
//                    Message message = handler.obtainMessage();
//                    message.what = RESUME_AND_DISPLAY_REFRESH_MESSAGE;
//                    message.sendToTarget();
                    Looper.loop();
                }
            };
            server.start();
        }catch (Exception ex){
            Util.getToast(getApplicationContext(), "Error refreshing vitals", true).show();

            Log.e(TAG, ex.getLocalizedMessage());
            ((Button) findViewById(R.id.refreshVitals)).setEnabled(true);
            //  ((Button) findViewById(R.id.testRecording)).setEnabled(true);

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == PERMISSION_WRITE_EXTERNAL_STORAGE) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start recording

                Util.getToast(this.getApplicationContext(), "WRITE_EXTERNAL_STORAGE permission granted", false).show();
            } else {
                Util.getToast(this.getApplicationContext(), "Do not have WRITE_EXTERNAL_STORAGE permission, You can NOT save recordings", true).show();
            }
        }

        if (requestCode == PERMISSION_RECORD_AUDIO) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start recording

                Util.getToast(this.getApplicationContext(), "RECORD_AUDIO permission granted", false).show();
            } else {
                Util.getToast(this.getApplicationContext(), "Do not have RECORD_AUDIO permission, You can NOT record", true).show();
            }
        }

        // END_INCLUDE(onRequestPermissionsResult)
    }

    void refreshVitalsDisplayedText(){
        Prefs prefs = new Prefs(getApplicationContext());
        // Device registered text
        TextView registered = (TextView) findViewById(R.id.mainRegisteredStatus);
        if (prefs.getGroupName() != null)
            registered.setText(getString(R.string.registered_true));
        else
            registered.setText(getString(R.string.registered_false));


        // Logged In text.
        TextView loggedInText = (TextView) findViewById(R.id.loggedInText);

// Check the age of the webToken
        boolean webTokenIsCurrent = Util.isWebTokenCurrent(prefs);

//        if (Server.loggedIn && webTokenIsCurrent)
        if ( webTokenIsCurrent)
            loggedInText.setText(getString(R.string.logged_in_to_server_true));
        else
            loggedInText.setText(getString(R.string.logged_in_to_server_false));

        // Device ID text.
        TextView deviceIDText = (TextView) findViewById(R.id.deviceIDText);
        try {
            String textServerPrefix = "";
            if (prefs.getUseTestServer()){
                textServerPrefix = "Test Server" + " ";
            }

            deviceIDText.setText(getString(R.string.device_id) + " " + textServerPrefix + Util.getDeviceID(getApplicationContext(),prefs.getToken()));
        } catch (Exception e) {

//            logger.error("Device ID not available");
            Log.e(TAG, "Device ID not available");
        }

        // GPS text.
        updateGpsDisplay(prefs);

    }

    void updateGpsDisplay(Prefs prefs){

try {
    double lat = prefs.getLatitude();
    double lon = prefs.getLongitude();

    if (lat != 0 && lon != 0) {
        //http://www.coderzheaven.com/2012/10/14/numberformat-class-android-rounding-number-android-formatting-decimal-values-android/
        NumberFormat numberFormat  = new DecimalFormat("#.000000");
        String latStr = numberFormat.format(lat);
        String lonStr = numberFormat.format(lon);
        TextView locationStatus = (TextView) findViewById(R.id.gpsText);
        //  locationStatus.setText("Latitude: "+lat+", Longitude: "+lon);
        locationStatus.setText("Latitude: " + latStr + ", Longitude: " + lonStr);
        //  locationStatus.setText("tim was here");
    }
}catch (Exception ex){
    Log.e(TAG, ex.getLocalizedMessage());
}
    }

//    private void updateGPSDisplay(Prefs prefs){
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
                    }else if (message.equalsIgnoreCase("refresh_vitals_displayed_text")){
                        refreshVitalsDisplayedText();
                    }else if (message.equalsIgnoreCase("can_not_toggle_airplane_mode")){
                        TextView messageView = (TextView)findViewById(R.id.messageText);
                        if (messageView != null){
                            messageView.setText("Messages: \nTo save power the Cacophonometer is designed to automatically switch airplane mode on/off but the version of Android on this phone prevents this unless the phone has been ‘rooted’.  You can disregard this message if the phone is plugged into the mains power – See the website for more details.");
                        }
                    }else if(message.equalsIgnoreCase("refresh_gps_coordinates")){
                        Prefs prefs = new Prefs(context);
                        updateGpsDisplay(prefs);
                    }

//                    else if (message.equalsIgnoreCase("recordNowButton_finished")) {
//                        ((Button) findViewById(R.id.recordNowButton)).setEnabled(true);
//                    }else if (message.equalsIgnoreCase("recording_started")){
//                        Util.getToast(getApplicationContext(),"Recording started", false ).show();
//                    }else if (message.equalsIgnoreCase("recording_finished")){
//                        Util.getToast(getApplicationContext(),"Recording finished", false ).show();
//                    }else if (message.equalsIgnoreCase("about_to_upload_files")){
//                        Util.getToast(getApplicationContext(),"About to upload files", false ).show();
//                    }else if (message.equalsIgnoreCase("files_successfully_uploaded")){
//                        Util.getToast(getApplicationContext(),"Files successfully uploaded", false ).show();
//                    }else if (message.equalsIgnoreCase("already_uploading")){
//                        Util.getToast(getApplicationContext(),"Files are already uploading", false ).show();
//                    }else if (message.equalsIgnoreCase("no_permission_to_record")){
//                        Util.getToast(getApplicationContext(),"No permission to record", false ).show();
//                    }else if (message.equalsIgnoreCase("recording_failed")){
//                        Util.getToast(getApplicationContext(),"Failed to make recording", false ).show();
//                    }else if (message.equalsIgnoreCase("recording_and_uploading_finished")){
//                        Util.getToast(getApplicationContext(),"Recording and uploading finished", false ).show();
//                    }else if (message.equalsIgnoreCase("recording_finished_but_uploading_failed")){
//                        Util.getToast(getApplicationContext(),"Recording finished but uploading failed", false ).show();
//                    }else if (message.equalsIgnoreCase("recorded_successfully_no_network")){
//                        Util.getToast(getApplicationContext(),"Recorded successfully, no network connection so did not upload", false ).show();
//                    }else if (message.equalsIgnoreCase("recording_failed")){
//                        Util.getToast(getApplicationContext(),"Recording failed", true ).show();
//                    }else if (message.equalsIgnoreCase("not_logged_in")){
//                        Util.getToast(getApplicationContext(),"Not logged in to server, could not upload files", true ).show();
//                    }
                }

            }catch (Exception ex){
//                logger.error(ex.getLocalizedMessage());
                Log.e(TAG,ex.getLocalizedMessage());
            }
        }
    };
    public CountingIdlingResource getIdlingResource() {
        return idlingResource;
    }
}


