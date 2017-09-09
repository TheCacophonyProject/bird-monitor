package nz.org.cacophony.cacophonometerlite;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
//import android.util.Log;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import org.slf4j.Logger;

import static com.loopj.android.http.AsyncHttpClient.LOG_TAG;


//public class SetupActivity extends Activity {
public class SetupActivity extends AppCompatActivity {
    private static final String TAG = SetupActivity.class.getName();

    // Handler status indicators
    private static final int REGISTER_SUCCESS = 1;
    private static final int REGISTER_FAIL = 2;
    static final int RESUME = 3;
//    private static Logger logger = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

//        logger = Util.getAndConfigureLogger(getApplicationContext(), LOG_TAG);
//        logger.info("SetupActivity onCreate" );

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        if (ab != null){
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayUseLogoEnabled(true);
            ab.setLogo(R.mipmap.ic_launcher);
        }else{
            Log.w(LOG_TAG, "ActionBar ab is null");
//            Util.writeLocalLogEntryUsingLogback(getApplicationContext(), LOG_TAG, "ActionBar ab is null");
//            logger.warn("ActionBar ab is null");
        }


    }

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

    @Override
    public void onResume() {
        try{
            super.onResume();
        }catch (Exception ex){
            Log.e(TAG, "Error calling super.onResume");
        }

        TextView registerStatus = (TextView) findViewById(R.id.setupRegisterStatus);
        Prefs prefs = new Prefs(getApplicationContext());

        double lat = prefs.getLatitude();
        double lon = prefs.getLongitude();

        if (lat != 0 && lon != 0) {
            TextView locationStatus = (TextView) findViewById(R.id.setupGPSLocationStatus);
            locationStatus.setText("Latitude: "+lat+", Longitude: "+lon);
        }

        String group = prefs.getGroupName();
        if (group != null) {
            String str = "Registered in group: " + group;
            registerStatus.setText(str);
        } else
            registerStatus.setText(R.string.not_registered);


        boolean hasRootAccess = prefs.getHasRootAccess();
        final CheckBox checkBoxRootAccess = (CheckBox) findViewById(R.id.cbHasRootAccess);
        if (hasRootAccess) {
            checkBoxRootAccess.setChecked(true);
        } else
            checkBoxRootAccess.setChecked(false);

//        boolean useTestServer = prefs.getUseTestServer();
//        final CheckBox checkBoxUseTestServer = (CheckBox) findViewById(R.id.cbUseTestServer);
//        if (useTestServer) {
//            checkBoxUseTestServer.setChecked(true);
//        } else
//            checkBoxUseTestServer.setChecked(false);

        boolean useShortRecordings = prefs.getUseShortRecordings();
        final CheckBox checkBoxUseUseShortRecordings = (CheckBox) findViewById(R.id.cbShortRecordings);
        if (useShortRecordings) {
            checkBoxUseUseShortRecordings.setChecked(true);
        } else
            checkBoxUseUseShortRecordings.setChecked(false);

        boolean noNetwork = prefs.getNoNetwork();
        final CheckBox checkBoxNoNetwork = (CheckBox) findViewById(R.id.cbNoNetwork);
        if (noNetwork) {
            checkBoxNoNetwork.setChecked(true);
        } else
            checkBoxNoNetwork.setChecked(false);

//        super.onResume();

//        boolean useFullLogging = prefs.getUseFullLogging();
//        final CheckBox checkBoxUseFullLogging = (CheckBox) findViewById(R.id.cbUseFullLogging);
//        if (useFullLogging) {
//            checkBoxUseFullLogging.setChecked(true);
//        } else
//            checkBoxUseFullLogging.setChecked(false);
//        super.onResume();
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {

            switch (inputMessage.what) {
                case REGISTER_SUCCESS:
                    onResume();
                    ScrollView mainScrollView = (ScrollView)findViewById(R.id.mainScrollView);
                    mainScrollView.fullScroll(ScrollView.FOCUS_UP);
                    try {
                        ((TextView) findViewById(R.id.setupGroupNameInput)).setText("");
                    }catch (Exception ex){
                        Log.e(TAG, ex.getLocalizedMessage());
//                        logger.error(ex.getLocalizedMessage());
                    }

                    Util.getToast(getApplicationContext(),"Success - Device has been registered with the server :-)", false ).show();
                    break;
                case REGISTER_FAIL:
                    onResume();
                    Context context = SetupActivity.this;
                    String errorMessage = "Failed to register";
                    if(Server.getErrorMessage() != null){
                        errorMessage = Server.getErrorMessage();
                    }

                    Util.getToast(getApplicationContext(),errorMessage, true ).show();
                case RESUME:
                    onResume();
                default:
                    // Unknown case
                    break;
            }
        }
    };

    public void registerButton(View v) {

        Prefs prefs = new Prefs(getApplicationContext());

        if (prefs.getNoNetwork()){
            Util.getToast(getApplicationContext(),"The No Network Connection checkbox is checked - so this device can not be registered", true ).show();
            return;
        }

        if (!Util.isNetworkConnected(getApplicationContext())){
            Util.getToast(getApplicationContext(),"The phone is not currently connected to the internet - please fix and try again", true ).show();
            return;
        }

        if (prefs.getGroupName() != null){
            Util.getToast(getApplicationContext(),"Already registered - press UNREGISTER first (if you really want to change group)", true ).show();
            return;
        }
        // Check that the group name is valid, at least 4 characters.
        String group = ((EditText) findViewById(R.id.setupGroupNameInput)).getText().toString();
        if (group == null || group.length() < 1){
            Util.getToast(getApplicationContext(),"Please enter a group name of at least 4 characters (no spaces)", true ).show();
            return;
        }else if (group.length() < 4) {
            Log.i(TAG, "Invalid group name: "+group);
//            Util.writeLocalLogEntryUsingLogback(getApplicationContext(), LOG_TAG, "Invalid group name: "+group);
//            logger.info("Invalid group name: "+group);

            Util.getToast(getApplicationContext(),group + " is not a valid group name. Please use at least 4 characters (no spaces)", true ).show();
            return;
        }



        Util.getToast(getApplicationContext(),"Attempting to register with server - please wait", false ).show();

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        String groupName = prefs.getGroupName();
        if (groupName != null && groupName.equals(group)) {
            // Try to login with username and password.

            Util.getToast(getApplicationContext(),"Already registered with that group", true ).show();
            return;
        }
        register(group, getApplicationContext());
    }

    /**
     * Un-registered a device deleting the password, devicename, and JWT.
     * @param v View
     */
    public void unRegisterButton(@SuppressWarnings("UnusedParameters") View v) {
        Prefs prefs = new Prefs(getApplicationContext());
        if (prefs.getGroupName() == null){
            Util.getToast(getApplicationContext(),"Not currently registered - so can not unregister :-(", true ).show();
            return;
        }
        unregister(true);

    }

    private void unregister(boolean displayUnregisterdMessage){
        try {
        //    Log.d(LOG_TAG, "Un-register device.");
            Prefs prefs = new Prefs(getApplicationContext());
            prefs.setGroupName(null);
            prefs.setPassword(null);
            prefs.setDeviceName(null);
            Server.loggedIn = false;
            if (displayUnregisterdMessage){
                Util.getToast(getApplicationContext(),"Success - Device is no longer registered", false ).show();
            }

        }catch(Exception ex){
            Log.e(TAG, "Error Un-registering device.");
//            Util.writeLocalLogEntryUsingLogback(getApplicationContext(), LOG_TAG, "Error Un-registering device.");
//            logger.error("Error Un-registering device.");
        }
        onResume();
        ScrollView mainScrollView = (ScrollView)findViewById(R.id.mainScrollView);
        mainScrollView.fullScroll(ScrollView.FOCUS_UP);
    }

    /**
     * Will register the device in the given group saving the JSON Web Token, devicename, and password.
     * @param group name of group to join.
     */
    private void register(final String group, final Context context) {
        // Check that the group name is valid, at least 4 characters.
        if (group == null || group.length() < 4) {

            Log.e(TAG, "Invalid group name - this should have already been picked up");
//            Util.writeLocalLogEntryUsingLogback(getApplicationContext(), LOG_TAG, "Invalid group name - this should have already been picked up");
//            logger.error("Invalid group name - this should have already been picked up");
            return;
        }

        disableFlightMode();

        // Now wait for network connection as setFlightMode takes a while
        if (!Util.waitForNetworkConnection(getApplicationContext(), true)){
            Log.e(TAG, "Failed to disable airplane mode");
//            Util.writeLocalLogEntryUsingLogback(getApplicationContext(), LOG_TAG, "Failed to disable airplane mode");
//            logger.error("Failed to disable airplane mode");
            return ;
        }


        Thread registerThread = new Thread() {
            @Override
            public void run() {
                Message message = handler.obtainMessage();
                if (Server.register(group, context)) {
                    message.what = REGISTER_SUCCESS;

                } else {
                    message.what = REGISTER_FAIL;
                }
                message.sendToTarget();
            }
        };
        registerThread.start();
    }



    public void updateGPSLocationButton(@SuppressWarnings("UnusedParameters") View v) {


//        Log.i(LOG_TAG, "Update location button");
     //   logger.debug("Update location button");
        Util.getToast(getApplicationContext(),"Getting new Location...", false ).show();
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //https://stackoverflow.com/questions/36123431/gps-service-check-to-check-if-the-gps-is-enabled-or-disabled-on-device
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        GPSLocationListener gpsLocationListener = new GPSLocationListener(getApplicationContext(), handler);
        try {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, gpsLocationListener, getApplicationContext().getMainLooper());
        } catch (SecurityException e) {
            Log.e(TAG, "Unable to get GPS location. Don't have required permissions.");
//            Util.writeLocalLogEntryUsingLogback(getApplicationContext(), LOG_TAG, "Unable to get GPS location. Don't have required permissions.");
         //   logger.error("Unable to get GPS location. Don't have required permissions.");
        }

    }

    public void disableGPSButton(@SuppressWarnings("UnusedParameters") View v) {

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //https://stackoverflow.com/questions/36123431/gps-service-check-to-check-if-the-gps-is-enabled-or-disabled-on-device
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }else{

            Util.getToast(getApplicationContext(),"GPS is already off", true ).show();
        }



    }


    public void onCheckboxRootedClicked(View v) {
        Prefs prefs = new Prefs(getApplicationContext());
        // Is the view now checked?
        boolean checked = ((CheckBox) v).isChecked();
        if (checked){
            prefs.setHasRootAccess(true);
        }else{
            prefs.setHasRootAccess(false);
        }
    }



    public void onCheckboxShortRecordingsClicked(View v) {
        Prefs prefs = new Prefs(getApplicationContext());
        // Is the view now checked?
        boolean checked = ((CheckBox) v).isChecked();
        if (checked){
            prefs.setUseShortRecordings(true);
        }else{
            prefs.setUseShortRecordings(false);
        }
    }

    public void onCheckboxNoNetworkClicked(View v) {
        Prefs prefs = new Prefs(getApplicationContext());
        // Is the view now checked?
        boolean checked = ((CheckBox) v).isChecked();
        if (checked){
            prefs.setNoNetwork(true);
        }else{
            prefs.setNoNetwork(false);
        }
    }

//    public void onCheckboxUseFullLogginClicked(View v) {
//        Prefs prefs = new Prefs(getApplicationContext());
//        // Is the view now checked?
//        boolean checked = ((CheckBox) v).isChecked();
//        if (checked){
//            prefs.setUseFullLogging(true);
//        }else{
//            prefs.setUseFullLogging(false);
//        }
//        Util.setLogbackConfigured(false);
//    }

//    public void onCheckboxUseTestServerClicked(View v) {
//        Prefs prefs = new Prefs(getApplicationContext());
//        // Is the view now checked?
//        boolean checked = ((CheckBox) v).isChecked();
//        if (checked){
//            prefs.setUseTestServer(true);
//
//        }else{
//            prefs.setUseTestServer(false);
//        }
//        unregister(false);// false means don't display unregistered message
//    }

    public void disableFlightMode(){
        try {
            //https://stackoverflow.com/questions/3875184/cant-create-handler-inside-thread-that-has-not-called-looper-prepare
            new Thread()
            {
                public void run()
                {
                    SetupActivity.this.runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            Util.disableFlightMode(getApplicationContext());
                        }
                    });
                }
            }.start();



        }catch (Exception ex){
//            logger.error(ex.getLocalizedMessage());
            Log.e(TAG, ex.getLocalizedMessage());
            Util.getToast(getApplicationContext(), "Error disabling flight mode", true).show();
        }
    }

}
