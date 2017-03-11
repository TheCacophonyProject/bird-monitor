package nz.org.cacophonoy.cacophonometerlite;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    public static final String intentAction = "nz.org.cacophony.cacophonometerlite.MainActivity";

    /**
     * Handler for Main Activity
     */
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {
            Log.d(TAG, "Main activity received message.");
            switch (inputMessage.what) {
                case RESUME:
                    onResume();
                    break;
                default:
                    // Unknown case
                    break;
            }
        }
    };

    /**
     * Handler states
     */
    private static final int RESUME = 1;

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(intentAction);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent myIntent = new Intent(MainActivity.this, StartRecordingReceiver.class);
       // myIntent.putExtra("type","repeating");
        try {
            myIntent.putExtra("type","repeating");
            System.out.println("intent type " + myIntent.getExtras().getString("type"));
        }catch (Exception e){
            System.out.println("In MainActivity");
            System.out.println(e.getLocalizedMessage());
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, myIntent,0);

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);


        Prefs prefs = new Prefs(this.getApplicationContext());
        prefs.setRecordingDurationSeconds();
        prefs.setTimeBetweenRecordingsSeconds();
        prefs.setDawnDuskOffsetLargeSeconds();
        prefs.setDawnDuskOffsetSmallSeconds();
        prefs.setDawnDuskOffsetLargeSeconds();
        prefs.setLengthOfTwilightSeconds();

        // determine if there is a sim card - need to disable airplane mode to determine
        Util.disableAirplaneMode(this.getApplicationContext());
        boolean isSimCardDetected = Util.isSimPresent(this.getApplicationContext());
        prefs.setSimCardDetected(isSimCardDetected);


        long timeBetweenRecordingsSeconds = (long)prefs.getTimeBetweenRecordingsSeconds();
        long delay = 1000 * timeBetweenRecordingsSeconds ;
        //long delay = 1000 * 60  * 10 ; // 10 minutes
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() ,
                delay, pendingIntent);
        refreshVitals();
    } //end onCreate

    /**
     * Updated UI.
     */
    @Override
    public void onResume() {
        checkPermissions();

        Prefs prefs = new Prefs(getApplicationContext());


        // Device registered text
        TextView registered = (TextView) findViewById(R.id.mainRegisteredStatus);
        if (prefs.getGroupName() != null)
            registered.setText(R.string.registered_true);
        else
            registered.setText(R.string.registered_false);

        // Server connection text.
        TextView connectToServerText = (TextView) findViewById(R.id.connectToServerText);
        if (Server.serverConnection)
            connectToServerText.setText(R.string.connected_to_server_true);
        else
            connectToServerText.setText(R.string.connected_to_server_false);

        // Logged In text.
        TextView loggedInText = (TextView) findViewById(R.id.loggedInText);
        if (Server.loggedIn)
            loggedInText.setText(R.string.logged_in_true);
        else
            loggedInText.setText(R.string.logged_in_false);

        // Device ID text.
        TextView deviceIDText = (TextView) findViewById(R.id.deviceIDText);
        try {
            deviceIDText.setText("Device ID " + Util.getDeviceID(Server.getToken()));
        } catch (Exception e) {
            Log.i(TAG, "Device ID not available");
        }

        // Application name text  appNameVersionText
        // http://stackoverflow.com/questions/4616095/how-to-get-the-build-version-number-of-your-android-application
        String versionName = BuildConfig.VERSION_NAME;
        TextView versionNameText = (TextView) findViewById(R.id.appNameVersionText);
        versionNameText.setText("Cacophonometer Lite " + versionName);


        putIntoAirplaneMode();
        super.onResume();
    }

    /**
     * Checks if the app has the required permissions. Storage, Microphone, Location.
     */
    private void checkPermissions() {
        boolean storagePermission =
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean microphonePermission =
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        boolean locationPermission =
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        TextView permissionText = (TextView) findViewById(R.id.appPermissionText);
        if (storagePermission && microphonePermission && locationPermission) {
            permissionText.setText(R.string.required_permissions_true);
            return;
        } else {
            permissionText.setText(R.string.required_permissions_false);
        }

        List<String> missingPermissionList = new ArrayList<>();
        if (!storagePermission) missingPermissionList.add("Write External Storage");
        if (!microphonePermission) missingPermissionList.add("Recording");
        if (!locationPermission) missingPermissionList.add("Location");

        String missingPermissionMessage = "App not granted some permissions: " + StringUtils.join(missingPermissionList, ", ");
        Toast.makeText(getApplicationContext(), missingPermissionMessage, Toast.LENGTH_SHORT).show();
        Log.w(TAG, missingPermissionMessage);

    }

    /**
     * Starts SetupActivity.
     * @param v View
     */
    public void register(View v) {
        Intent intent = new Intent(this, SetupActivity.class);
        startActivity(intent);
    }

    public void testRecording(View v) {
        Log.d(TAG, "Test recording button.");

        Intent intent = new Intent(MainActivity.this, StartRecordingReceiver.class);
        sendBroadcast(intent);
    }

    /**
     * UI button to refresh vitals
     * @param v View
     */
    public void refreshButton(View v) {
        refreshVitals();
    }

    /**
     * Check the vitals again and update the UI.
     */
    public void refreshVitals() {
        Toast.makeText(getApplicationContext(), "Update app vitals", Toast.LENGTH_SHORT).show();
        Thread server = new Thread() {
            @Override
            public void run() {
                Server.updateServerConnectionStatus(getApplicationContext());
                Message message = handler.obtainMessage();
                message.what = RESUME;
                message.sendToTarget();
            }
        };
        server.start();
    }

private void putIntoAirplaneMode(){ // to save power
//    //http://stackoverflow.com/posts/5533943/edit
//    boolean isEnabled = Settings.System.getInt(
//            getContentResolver(),
//            Settings.System.AIRPLANE_MODE_ON, 2) == 1;
    boolean airplaneModeOn = Util.isAirplaneModeOn(getApplicationContext());

    if (!airplaneModeOn){
        // Airplane mode is not on so might need to turn on
        int sdkLevel = Build.VERSION.SDK_INT;
        if (sdkLevel > 19) {
            // Can not change airplane mode on devices running android sdk > 19 (that's 5.0 Lollipop)
            // If there is no sim then ask the user to enable airplane mode
//            boolean simPresent = Util.isSimPresent(getApplicationContext());
            Prefs prefs = new Prefs(getApplicationContext());
            boolean simPresent = prefs.getSimCardDetected();
            if (!simPresent){
                Toast.makeText(getApplicationContext(), "SAVE POWER - Enable Airplane/Flight mode", Toast.LENGTH_LONG).show();
            }

//            // https://sites.google.com/site/androidhowto/how-to-1/check-if-sim-card-exists-in-the-phone
//            TelephonyManager tm = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);  //gets the current TelephonyManager
//            if (tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT){
//
//            } else {
//                System.out.println("The phone has a sim card");
//            }

            return;
        }

        // Only get here if sdk is 19 or below - this means we can enable/disable airplane mode programatically
        // My testing seemed to show a bug (on android sdk 10) when checking to see if there was a sim
        Util.enableAirplaneMode(getApplicationContext());

    }
}

}
