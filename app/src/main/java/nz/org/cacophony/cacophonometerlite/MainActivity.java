package nz.org.cacophony.cacophonometerlite;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
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
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
//import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


//import org.slf4j.Logger;


import static android.widget.Toast.makeText;
//import static com.loopj.android.http.AsyncHttpClient.LOG_TAG;
//import static nz.org.cacophony.cacophonometerlite.DawnDuskAlarms.logger;


public class MainActivity extends AppCompatActivity {
//    private static final String LOG_TAG = MainActivity.class.getName();
private static final String TAG = MainActivity.class.getName();
    private static final String intentAction = "nz.org.cacophony.cacophonometerlite.MainActivity";
  //  private static Logger logger = null;
 // private static Log logger = null;


    /**
     * Handler for Main Activity
     */
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {
           // Log.d(LOG_TAG, "Main activity received message.");
            Log.d(TAG, "Main activity received message.");
         //   logger.info("Main activity received message." );
            switch (inputMessage.what) {
                case RESUME:
                    onResume();
                    break;
                case RESUME_AND_DISPLAY_REFRESH_MESSAGE:
                    Util.getToast(getApplicationContext(),"Vitals have been updated", false ).show();
                   // ((Button) findViewById(R.id.refreshVitals)).setEnabled(true);
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

    private BroadcastReceiver onNotice= new BroadcastReceiver() {
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String message = intent.getStringExtra("message");
                if (message != null) {
                    if (message.equalsIgnoreCase("enable_test_recording_button")) {
                        ((Button) findViewById(R.id.testRecording)).setEnabled(true);
                    }else if (message.equalsIgnoreCase("enable_vitals_button")) {
                        ((Button) findViewById(R.id.refreshVitals)).setEnabled(true);
                    }else if (message.equalsIgnoreCase("enable_disable_flight_mode_button")) {
                        ((Button) findViewById(R.id.disableFlightMode)).setEnabled(true);
                    }else if (message.equalsIgnoreCase("enable_setup_button")) {
                        ((Button) findViewById(R.id.setUpDeviceButton)).setEnabled(true);
                    }

                }




            }catch (Exception ex){
//                logger.error(ex.getLocalizedMessage());
                Log.e(TAG,ex.getLocalizedMessage());
            }
        }
    };



    @Override
    protected void onPause() {
        super.onPause();

        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
    }



    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);


//        logger = Util.getAndConfigureLogger(getApplicationContext(), LOG_TAG);
//        if (logger == null){
//            Log.e(LOG_TAG, "logger is null");
////https://stackoverflow.com/questions/6330200/how-to-quit-android-application-programmatically
//            this.finish();
//            System.exit(0);
//        }
//        logger.info("MainActivity onCreate" );

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


//            logger.error("Error with intent setupButtonClick");
            Log.e(TAG, "Error with intent setupButtonClick");

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


//            logger.warn("ActionBar ab is null");
            Log.w(TAG, "ActionBar ab is null");
        }

      //  refreshVitals();
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
        try {
            super.onResume();
        }   catch (Exception ex){
            // This is very poor, but I have no idea why super.onResume give a null pointer exception
            // Need to spend time on this
            Log.e(TAG, "Error calling super.onResume");
        }
//        logger.info("MainActivity onResume" );
        Log.i(TAG, "MainActivity onResume" );
        disableFlightMode();
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
            loggedInText.setText(getString(R.string.logged_in_to_server_true));
        else
            loggedInText.setText(getString(R.string.logged_in_to_server_false));

        // Device ID text.
        TextView deviceIDText = (TextView) findViewById(R.id.deviceIDText);
        try {
            deviceIDText.setText(getString(R.string.device_id) + " " + Util.getDeviceID(getApplicationContext(),Server.getToken()));
        } catch (Exception e) {

//            logger.error("Device ID not available");
            Log.e(TAG, "Device ID not available");
        }

        // GPS text.

        try {
            double lat = prefs.getLatitude();
            double lon = prefs.getLongitude();

            if (lat != 0 && lon != 0) {
                TextView gpsText = (TextView) findViewById(R.id.gpsText);
                gpsText.setText(getString(R.string.gps_text) + " " + "Latitude: "+lat+", Longitude: "+lon);
            }

        } catch (Exception e) {

//            logger.error("Device ID not available");
            Log.e(TAG, "Device ID not available");
        }

        // Application name text  appNameVersionText
        // http://stackoverflow.com/questions/4616095/how-to-get-the-build-version-number-of-your-android-application
        String versionName = BuildConfig.VERSION_NAME;
        TextView versionNameText = (TextView) findViewById(R.id.appNameVersionText);
        versionNameText.setText(getString(R.string.version) + " " + versionName);

      //  super.onResume();

        IntentFilter iff = new IntentFilter("event");
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);
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

        Util.getToast(getApplicationContext(),missingPermissionMessage, false ).show();

//        logger.warn(missingPermissionMessage );
Log.w(TAG, missingPermissionMessage);

    }


public void setupButtonClick(@SuppressWarnings("UnusedParameters") View v) {
    try{
        disableFlightMode();
//    if (!Util.isNetworkConnected(getApplicationContext())){
//        Util.getToast(getApplicationContext(),"There is no network connection - I'll disable flight mode to see if that fixes it.", true ).show();
//        Util.getToast(getApplicationContext(),"You will need to press the SETUP button again once there is a network connection", true ).show();
//
//        // disableFlightModeButtonClick(null);
//        disableFlightMode();
//        return;
//    }

        Intent intent = new Intent(this, SetupActivity.class);
        startActivity(intent);
    }catch (Exception ex){
//        logger.error(ex.getLocalizedMessage());
        Log.e(TAG, ex.getLocalizedMessage());
    }
}

    public void testRecordingButtonClick(@SuppressWarnings("UnusedParameters") View v) {
        try {
            Prefs prefs = new Prefs(getApplicationContext());

            // test for network connection
            if (!prefs.getOffLineMode()){
                if (!Util.isNetworkConnected(getApplicationContext())){
                    Util.getToast(getApplicationContext(), "There is no network connection - please fix and try again", true).show();
                    return;
                }
            }


            if (Server.loggedIn != true) {

                if (!prefs.getOffLineMode()){
                    Util.getToast(getApplicationContext(), "Not logged in - press REFRESH to connect", true).show();
                    return;
                }

            }


            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


            //https://stackoverflow.com/questions/36123431/gps-service-check-to-check-if-the-gps-is-enabled-or-disabled-on-device
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                // makeText(getApplicationContext(), "Turn OFF GPS before testing", Toast.LENGTH_LONG).show();
                Util.getToast(getApplicationContext(), "Turn OFF GPS before testing", true).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                return;
            }


            Intent myIntent = new Intent(MainActivity.this, StartRecordingReceiver.class);
            try {
                myIntent.putExtra("type", "testButton");

            } catch (Exception ex) {

//                logger.error(ex.getLocalizedMessage());
                Log.e(TAG, ex.getLocalizedMessage());
            }
        //    Util.getToast(getApplicationContext(), "Getting ready to record - please wait", false).show();
            ((Button)v).setEnabled(false);
            ((Button) findViewById(R.id.refreshVitals)).setEnabled(false);
            ((Button) findViewById(R.id.setUpDeviceButton)).setEnabled(false);

            sendBroadcast(myIntent);
        }catch (Exception ex){
//            logger.error(ex.getLocalizedMessage());
            Log.e(TAG, ex.getLocalizedMessage());
            ((Button) findViewById(R.id.refreshVitals)).setEnabled(false);
            ((Button) findViewById(R.id.testRecording)).setEnabled(false);
            ((Button) findViewById(R.id.setUpDeviceButton)).setEnabled(false);
        }
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
        ((Button) findViewById(R.id.testRecording)).setEnabled(false);
        ((Button) findViewById(R.id.setUpDeviceButton)).setEnabled(false);


        Util.getToast(getApplicationContext(),"About to update vitals - please wait a moment", false ).show();
        try {

            Thread server = new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    Server.updateServerConnectionStatus(getApplicationContext());
                    Message message = handler.obtainMessage();
                    message.what = RESUME_AND_DISPLAY_REFRESH_MESSAGE;
                    message.sendToTarget();
                    Looper.loop();
                }
            };
            server.start();
        }catch (Exception ex){
            Util.getToast(getApplicationContext(), "Error refreshing vitals", true).show();
//            logger.error(ex.getLocalizedMessage());
            Log.e(TAG, ex.getLocalizedMessage());
            ((Button) findViewById(R.id.refreshVitals)).setEnabled(true);
            ((Button) findViewById(R.id.testRecording)).setEnabled(true);
            ((Button) findViewById(R.id.setUpDeviceButton)).setEnabled(true);
        }
    }



    public  void disableFlightModeButtonClick(@SuppressWarnings("UnusedParameters") View v){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            Prefs prefs = new Prefs(getApplicationContext());
            if (!prefs.getHasRootAccess()){
                Util.getToast(getApplicationContext(), "Root access required to change airplane mode", true).show();
                return;
            }

        }
        disableFlightMode();
    }



    public void disableFlightMode(){
        try {

           // Util.getToast(getApplicationContext(), "About to disable flight mode - it will take up to a minute to get a network connection", false).show();
            //https://stackoverflow.com/questions/3875184/cant-create-handler-inside-thread-that-has-not-called-looper-prepare
        new Thread()
        {
            public void run()
            {
                MainActivity.this.runOnUiThread(new Runnable()
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
           // logger.error(ex.getLocalizedMessage());
            Log.e(TAG, ex.getLocalizedMessage());
            Util.getToast(getApplicationContext(), "Error disabling flight mode", true).show();
        }
    }


}
