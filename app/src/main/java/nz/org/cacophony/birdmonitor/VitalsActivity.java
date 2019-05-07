package nz.org.cacophony.birdmonitor;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * The code to display the Vitals screen that the user can access from the menu, and displays to the
 * user information such as whether the app has registered with the server, the Device ID of the
 * app and the GPS loction.
 */
@SuppressWarnings("NullableProblems")
public class VitalsActivity extends AppCompatActivity implements IdlingResourceForEspressoTesting {
    // Register with idling counter
// https://developer.android.com/training/testing/espresso/idling-resource.html
// stackoverflow.com/questions/25470210/using-espresso-idling-resource-with-multiple-activities // this gave me idea to use an interface for app under test activities e.g MainActivity
    // https://www.youtube.com/watch?v=uCtzH0Rz5XU

    private static final String TAG = VitalsActivity.class.getName();

    private TextView tvMessages;

    private PermissionsHelper permissionsHelper;

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter iff = new IntentFilter("MANAGE_RECORDINGS");
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);
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

        tvMessages = findViewById(R.id.tvMessages);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        checkPermissions();
    }

    private void disableFlightMode() {
        try {
            //https://stackoverflow.com/questions/3875184/cant-create-handler-inside-thread-that-has-not-called-looper-prepare
            new Thread(() -> runOnUiThread(() -> Util.disableFlightMode(getApplicationContext()))).start();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage(), ex);
            tvMessages.setText("Error disabling flight mode");
        }
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

        displayPermissions();
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


    private void refreshVitalsDisplayedText() {
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


        if (webTokenIsCurrent)
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

        if (prefs.getIsDisabled()) {
            tvNextRecording.setText("Next Recording: Disabled - no next recording");
        } else {
            String nextAlarm = Util.getNextAlarm(getApplicationContext());
            tvNextRecording.setText("Next Recording: " + nextAlarm);
        }

        // GPS text.
        updateGpsDisplay(prefs);
    }

    private void updateGpsDisplay(Prefs prefs) {

        try {
            double lat = prefs.getLatitude();
            double lon = prefs.getLongitude();

            if (lat != 0 && lon != 0) {
                //http://www.coderzheaven.com/2012/10/14/numberformat-class-android-rounding-number-android-formatting-decimal-values-android/
                NumberFormat numberFormat = new DecimalFormat("#.000000");
                String latStr = numberFormat.format(lat);
                String lonStr = numberFormat.format(lon);
                TextView locationStatus = findViewById(R.id.gpsText);
                String latitude = getString(R.string.latitude);
                String longitude = getString(R.string.longitude);
                String locationStatusToDisplay = latitude + ": " + latStr + ", " + longitude + ": " + lonStr;
                locationStatus.setText(locationStatusToDisplay);
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage(), ex);
        }
    }


    private final BroadcastReceiver onNotice = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                String jsonStringMessage = intent.getStringExtra("jsonStringMessage");
                if (jsonStringMessage != null) {
                    JSONObject joMessage = new JSONObject(jsonStringMessage);
                    String messageType = joMessage.optString("messageType");

                    if (messageType.equalsIgnoreCase("RECORDING_FINISHED")) {
                        refreshVitalsDisplayedText();
                    }
                }

            } catch (Exception ex) {
                Log.e(TAG, ex.getLocalizedMessage(), ex);
                tvMessages.setText("Could not record");
            }

        }
    };

    public void finished(@SuppressWarnings("UnusedParameters") View v) {
        try {
            finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.button_help:
                Util.displayHelp(this, getResources().getString(R.string.activity_or_fragment_title_vitals));
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsHelper.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    private void checkPermissions() {
        permissionsHelper = new PermissionsHelper();
        permissionsHelper.checkAndRequestPermissions(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE);
    }

    /**
     * Checks if the app has the required permissions. Storage, Microphone, Location.
     */
    private void displayPermissions() {

        boolean storagePermission =
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean recordPermission =
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        boolean locationPermission =
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        TextView permissionText = findViewById(R.id.appPermissionText);
        if (storagePermission && recordPermission && locationPermission) {
            permissionText.setText(getString(R.string.required_permissions_true));

            return;
        } else {
            permissionText.setText(getString(R.string.required_permissions_false));
        }

        List<String> missingPermissionList = new ArrayList<>();
        if (!storagePermission) missingPermissionList.add("Write External Storage");
        if (!recordPermission) missingPermissionList.add("Recording");
        if (!locationPermission) missingPermissionList.add("Location");

        String missingPermissionMessage = "App not granted some permissions: " + StringUtils.join(missingPermissionList, ", ");
        tvMessages.setText(missingPermissionMessage);
        Log.w(TAG, missingPermissionMessage);
    }
}


