package nz.org.cacophony.cacophonometerlite;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.makeText;


public class MainActivity extends AppCompatActivity {
   // private static final String TAG = "MainActivity";
    private static final String LOG_TAG = MainActivity.class.getName();

    private static final String intentAction = "nz.org.cacophony.cacophonometerlite.MainActivity";

//    private final String COMMAND_FLIGHT_MODE_1 = "settings put global airplane_mode_on";
//    private final String COMMAND_FLIGHT_MODE_2 = "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state";

    /**
     * Handler for Main Activity
     */
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {
            Log.d(LOG_TAG, "Main activity received message.");
            switch (inputMessage.what) {
                case RESUME:
                    onResume();
                    break;
                case RESUME_AND_DISPLAY_REFRESH_MESSAGE:
                    Util.getToast(getApplicationContext(),"Vitals have been updated", false ).show();
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
    private static final int RESUME_AND_DISPLAY_REFRESH_MESSAGE = 2;

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

        this.setTitle(R.string.main_activity_name);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        Intent myIntent = new Intent(MainActivity.this, StartRecordingReceiver.class);

        try {
            myIntent.putExtra("type","repeating");
            Uri timeUri; // // this will hopefully allow matching of intents so when adding a new one with new time it will replace this one
            timeUri = Uri.parse("normal"); // cf dawn dusk offsets created in DawnDuskAlarms
            myIntent.setData(timeUri);

        }catch (Exception e){
            Log.e(LOG_TAG, "Error with intent setup");

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
        prefs.setTimeBetweenUploadsSeconds();


        long timeBetweenRecordingsSeconds = (long)prefs.getTimeBetweenRecordingsSeconds();
      //  long timeBetweenRecordingsSeconds = (long)3600;
        long delay = 1000 * timeBetweenRecordingsSeconds ;

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() ,
                delay, pendingIntent);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        if (ab != null){


        ab.setDisplayUseLogoEnabled(true);
        ab.setLogo(R.mipmap.ic_launcher);
        }else{
            Log.w(LOG_TAG, "ActionBar ab is null");
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
            registered.setText(getString(R.string.registered_true));
        else
            registered.setText(getString(R.string.registered_false));

        // Server connection text.
        TextView connectToServerText = (TextView) findViewById(R.id.connectToServerText);
        if (Server.serverConnection)
            connectToServerText.setText(getString(R.string.connected_to_server_true));
        else
            connectToServerText.setText(getString(R.string.connected_to_server_false));

        // Logged In text.
        TextView loggedInText = (TextView) findViewById(R.id.loggedInText);
        if (Server.loggedIn)
            loggedInText.setText(getString(R.string.logged_in_true));
        else
            loggedInText.setText(getString(R.string.logged_in_false));

        // Device ID text.
        TextView deviceIDText = (TextView) findViewById(R.id.deviceIDText);
        try {
            deviceIDText.setText(getString(R.string.device_id) + " " + Util.getDeviceID(Server.getToken()));
        } catch (Exception e) {
            Log.i(LOG_TAG, "Device ID not available");
        }

        // Application name text  appNameVersionText
        // http://stackoverflow.com/questions/4616095/how-to-get-the-build-version-number-of-your-android-application
        String versionName = BuildConfig.VERSION_NAME;
        TextView versionNameText = (TextView) findViewById(R.id.appNameVersionText);
        versionNameText.setText(getString(R.string.version) + " " + versionName);


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
      //  makeText(getApplicationContext(), missingPermissionMessage, Toast.LENGTH_SHORT).show();
        Util.getToast(getApplicationContext(),missingPermissionMessage, false ).show();
        Log.w(LOG_TAG, missingPermissionMessage);

    }


public void setup(@SuppressWarnings("UnusedParameters") View v) {
    if (!Util.isNetworkConnected(getApplicationContext())){
        Util.getToast(getApplicationContext(),"There is no network connection - please connect and try again", true ).show();
        return;
    }

        Intent intent = new Intent(this, SetupActivity.class);
        startActivity(intent);
    }

    public void testRecording(@SuppressWarnings("UnusedParameters") View v) {
        // test that it has registered
        if (Server.loggedIn != true){
//            Toast toast = Toast.makeText(getApplicationContext(), "NOT logged in - Press REFRESH and if App Vitals show all OK, try again", Toast.LENGTH_LONG);
//            toast.getView().setBackgroundColor(getResources().getColor(R.color.colorAccent));
//            toast.show();
            Util.getToast(getApplicationContext(),"NOT logged in - Press REFRESH and if App Vitals show all OK, try again", true ).show();

            return;
        }

        Log.d(LOG_TAG, "Test recording button.");
//        Log.i(TAG, "Test recording button.");
//        Log.e(TAG, "Test recording button.");

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //https://stackoverflow.com/questions/36123431/gps-service-check-to-check-if-the-gps-is-enabled-or-disabled-on-device
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
           // makeText(getApplicationContext(), "Turn OFF GPS before testing", Toast.LENGTH_LONG).show();
            Util.getToast(getApplicationContext(),"Turn OFF GPS before testing", true ).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            return;
        }



        Intent myIntent = new Intent(MainActivity.this, StartRecordingReceiver.class);
        try {
            myIntent.putExtra("type","testButton");

        }catch (Exception e){
Log.e(LOG_TAG, "Error setting up intent");
        }
        Util.getToast(getApplicationContext(),"Getting ready to record - please wait", false ).show();
        sendBroadcast(myIntent);
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
//        makeText(getApplicationContext(), "Update app vitals", Toast.LENGTH_SHORT).show();
        Util.getToast(getApplicationContext(),"About to update App vitals - please wait a moment", false ).show();
        Thread server = new Thread() {
            @Override
            public void run() {
                Server.updateServerConnectionStatus(getApplicationContext());
                Message message = handler.obtainMessage();
                message.what = RESUME_AND_DISPLAY_REFRESH_MESSAGE;
                message.sendToTarget();
            }
        };
        server.start();
    }



    public  void disableFlightMode(@SuppressWarnings("UnusedParameters") View v){

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Util.disableFlightMode(getApplicationContext());
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error disabling flight mode");
                }
            }
        };
        thread.start();
    }

    public  void enableFlightMode(@SuppressWarnings("UnusedParameters") View v) {

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Util.enableFlightMode(getApplicationContext());
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error enabling flight mode");
                }
            }
        };
        thread.start();
    }

//    public  void isFlightModeOn(View v) {
//        boolean mode = false;
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
//            // API 17 onwards
//            mode = Settings.Global.getInt(getApplicationContext().getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
//        } else {
//            // API 16 and earlier.
//            mode = Settings.System.getInt(getApplicationContext().getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
//
//        }
//
//        if (mode){
//            Toast.makeText(getApplicationContext(), "Airplane mode is ON", Toast.LENGTH_LONG).show();
//        }else{
//            Toast.makeText(getApplicationContext(), "Airplane mode is OFF", Toast.LENGTH_LONG).show();
//        }
//
//
//    }

}
