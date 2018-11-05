package nz.org.cacophony.cacophonometer;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
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
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * The code to display the Vitals screen that the user can access from the menu, and displays to the
 * user information such as whether the app has registered with the server, the Device ID of the
 * app and the GPS loction.
 */
@SuppressWarnings("NullableProblems")
public class VitalsActivity extends AppCompatActivity implements IdlingResourceForEspressoTesting{
    // Register with idling counter
// https://developer.android.com/training/testing/espresso/idling-resource.html
// stackoverflow.com/questions/25470210/using-espresso-idling-resource-with-multiple-activities // this gave me idea to use an interface for app under test activities e.g MainActivity
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_help, menu);
        return true;
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

        //https://developer.android.com/training/appbar/setting-up#java
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    private void disableFlightMode(){
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
//                            String message = Util.disableFlightMode(getApplicationContext());
//
//                            if (message != null){
//                                ((TextView) findViewById(R.id.messageText)).setText("\n                                                                                                                " + message);
//                            }
                            Util.disableFlightMode(getApplicationContext());

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

        checkPermissions();
        refreshVitalsDisplayedText();

        // Application name text  appNameVersionText
        // http://stackoverflow.com/questions/4616095/how-to-get-the-build-version-number-of-your-android-application
        String versionName = BuildConfig.VERSION_NAME;
        TextView versionNameText = findViewById(R.id.tvAppVersion);
        String versionNameTextToDisplay = getString(R.string.version) + " " + versionName;
        versionNameText.setText(versionNameTextToDisplay);

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

        TextView permissionText = findViewById(R.id.appPermissionText);
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

        Log.w(TAG, missingPermissionMessage);

    }

    private boolean requestPermissions(){
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
        findViewById(R.id.refreshVitals).setEnabled(false);

        Util.getToast(getApplicationContext(),"About to update vitals - please wait a moment", false ).show();
        try {

            Thread server = new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    Server.updateServerConnectionStatus(getApplicationContext());
                    Looper.loop();
                }
            };
            server.start();
        }catch (Exception ex){
            Util.getToast(getApplicationContext(), "Error refreshing vitals", true).show();

            Log.e(TAG, ex.getLocalizedMessage());
            findViewById(R.id.refreshVitals).setEnabled(true);

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

    private void refreshVitalsDisplayedText(){
        Prefs prefs = new Prefs(getApplicationContext());
        // Device registered text
        TextView registered = findViewById(R.id.mainRegisteredStatus);
        if (prefs.getGroupName() != null)
            registered.setText(getString(R.string.registered_true));
        else
            registered.setText(getString(R.string.registered_false));


        // Logged In text.
        TextView loggedInText = findViewById(R.id.loggedInText);

// Check the age of the webToken
        boolean webTokenIsCurrent = Util.isWebTokenCurrent(prefs);


        if ( webTokenIsCurrent)
            loggedInText.setText(getString(R.string.logged_in_to_server_true));
        else
            loggedInText.setText(getString(R.string.logged_in_to_server_false));

        // Device Name text.
        TextView deviceNameText = findViewById(R.id.deviceNameText);

        String deviceName = prefs.getDeviceName();

        if (deviceName != null) {  // only change device name display if it isn't null

            try {
                String testServerPrefix = "";
                if (prefs.getUseTestServer()) {
                    testServerPrefix = "Test Server" + " ";
                }

                String deviceNameToDisplay = getString(R.string.device_name_colon) + " " + testServerPrefix + prefs.getDeviceName();
                deviceNameText.setText(deviceNameToDisplay);

            } catch (Exception e) {
                Log.e(TAG, "Device Name not available");
            }

        }

        // Update time of last recording
        TextView tvLastRecording = findViewById(R.id.tvLastRecording);
        String timeThatLastRecordingHappened = Util.getTimeThatLastRecordingHappened(getApplicationContext());
        tvLastRecording.setText("Last Recording: " + timeThatLastRecordingHappened);

        // Update time of next recording
        TextView tvNextRecording = findViewById(R.id.tvNextRecording);

        if (prefs.getIsDisabled()){
            tvNextRecording.setText("Next Recording: Disabled - no next recording");
        }else{
            String nextAlarm = Util.getNextAlarm(getApplicationContext());
            tvNextRecording.setText("Next Recording: " + nextAlarm);
        }



        // GPS text.
        updateGpsDisplay(prefs);

    }

    private void updateGpsDisplay(Prefs prefs){

try {
    double lat = prefs.getLatitude();
    double lon = prefs.getLongitude();

    if (lat != 0 && lon != 0) {
        //http://www.coderzheaven.com/2012/10/14/numberformat-class-android-rounding-number-android-formatting-decimal-values-android/
        NumberFormat numberFormat  = new DecimalFormat("#.000000");
        String latStr = numberFormat.format(lat);
        String lonStr = numberFormat.format(lon);
        TextView locationStatus = findViewById(R.id.gpsText);
        String latitude = getString(R.string.latitude);
        String longitude = getString(R.string.longitude);
        String locationStatusToDisplay = latitude + ": " + latStr + ", " + longitude + ": " + lonStr;
//        locationStatus.setText("Latitude: " + latStr + ", Longitude: " + lonStr);
        locationStatus.setText(locationStatusToDisplay);
         }
}catch (Exception ex){
    Log.e(TAG, ex.getLocalizedMessage());
}
    }


    private final BroadcastReceiver onNotice= new BroadcastReceiver() {
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

        // broadcast notification coming from ??
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String message = intent.getStringExtra("message");
                if (message != null) {

                    if (message.equalsIgnoreCase("enable_vitals_button")) {
                        findViewById(R.id.refreshVitals).setEnabled(true);
                    }else if (message.equalsIgnoreCase("tick_logged_in_to_server")){
                        TextView loggedInText = findViewById(R.id.loggedInText);
                        loggedInText.setText(getString(R.string.logged_in_to_server_true));
                    }else if (message.equalsIgnoreCase("untick_logged_in_to_server")){
                        TextView loggedInText = findViewById(R.id.loggedInText);
                        loggedInText.setText(getString(R.string.logged_in_to_server_false));
                    }else if (message.equalsIgnoreCase("refresh_vitals_displayed_text")){
                        refreshVitalsDisplayedText();
                    }else if (message.equalsIgnoreCase("can_not_toggle_airplane_mode")){
                        TextView messageView = findViewById(R.id.messageText);
                        if (messageView != null){
                            messageView.setText("Messages: \nTo save power the Cacophonometer is designed to automatically switch airplane mode on/off but the version of Android on this phone prevents this unless the phone has been ‘rooted’.  You can disregard this message if the phone is plugged into the mains power – See the website for more details.");
                        }
                    }else if(message.equalsIgnoreCase("refresh_gps_coordinates")){
                        Prefs prefs = new Prefs(context);
                        updateGpsDisplay(prefs);
                    }else if (message.equalsIgnoreCase("recording_finished")) {
                        refreshVitalsDisplayedText();
                    }else if (message.equalsIgnoreCase("alarms_updated")) {
                        refreshVitalsDisplayedText();
                    }

                }

            }catch (Exception ex){
                Log.e(TAG,ex.getLocalizedMessage());
            }
        }
    };

    public void back(@SuppressWarnings("UnusedParameters") View v) {
        try {
//            Intent intent = new Intent(this, MainActivity2.class);
//            startActivity(intent);
            finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.button_help:
                Util.displayHelp(this, "App Vitals");
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }
}


