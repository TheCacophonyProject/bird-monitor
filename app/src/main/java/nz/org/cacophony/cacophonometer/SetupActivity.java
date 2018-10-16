package nz.org.cacophony.cacophonometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * This class creates the Settings screen that is displayed to the user via the menu on the main
 * screen.
 * It is used to configure the application.
 */
@SuppressWarnings("unused")
public class SetupActivity extends AppCompatActivity implements IdlingResourceForEspressoTesting {
    // Register with idling counter
// https://developer.android.com/training/testing/espresso/idling-resource.html
// stackoverflow.com/questions/25470210/using-espresso-idling-resource-with-multiple-activities // this gave me idea to use an interface for app under test activities e.g MainActivity
    // https://www.youtube.com/watch?v=uCtzH0Rz5XU

    private static final String TAG = SetupActivity.class.getName();

    // Handler status indicators
    private static final int REGISTER_SUCCESS = 1;
    private static final int REGISTER_FAIL = 2;
    private static final int RESUME = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        if (ab != null){
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayUseLogoEnabled(true);
            ab.setLogo(R.mipmap.ic_launcher);
        }else{
            Log.w(TAG, "ActionBar ab is null");
        }

    }

    private final BroadcastReceiver onNotice= new BroadcastReceiver() {
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String message = intent.getStringExtra("message");
                if (message != null) {
                    if (message.equalsIgnoreCase("refresh_gps_coordinates")) {
                        updateGpsDisplay(context);
                    }else if(message.equalsIgnoreCase("turn_on_gps_and_try_again")){
                        Util.getToast(context, "Sorry, GPS is not enabled.  Please enable location/gps in the phone settings and try again.", true).show();
                    }else if (message.equalsIgnoreCase("error_do_not_have_root")){
                        Util.getToast(getApplicationContext(),"It looks like you have incorrectly indicated in settings that this phone has been rooted", true ).show();
                    }
                }


            }catch (Exception ex){
                Log.e(TAG,ex.getLocalizedMessage());
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

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



    private void openVitals() {
        Intent intent = new Intent(this, VitalsActivity.class);
        startActivity(intent);
    }

    private void openHelp() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume 1");
        try{
            super.onResume();
        }catch (Exception ex){
            Log.e(TAG, "Error calling super.onResume");
        }

        TextView registerStatus = findViewById(R.id.setupRegisterStatus);
        Prefs prefs = new Prefs(getApplicationContext());



        updateGpsDisplay(getApplicationContext());

        String group = prefs.getGroupName();
        String deviceName = prefs.getDeviceName();
        if (group != null) {
          // String str = "Registered in group: " + group;
            String str = deviceName + " is registered in group " + group;
            registerStatus.setText(str);
        } else
            registerStatus.setText(R.string.not_registered);


        boolean hasRootAccess = prefs.getHasRootAccess();
        final CheckBox checkBoxRootAccess = findViewById(R.id.cbHasRootAccess);
        if (hasRootAccess) {
            checkBoxRootAccess.setChecked(true);
        } else
            checkBoxRootAccess.setChecked(false);



        boolean useShortRecordings = prefs.getUseShortRecordings();
        final CheckBox checkBoxUseUseShortRecordings = findViewById(R.id.cbShortRecordings);
        if (useShortRecordings) {
            checkBoxUseUseShortRecordings.setChecked(true);
        } else
            checkBoxUseUseShortRecordings.setChecked(false);

        boolean useTestServer = prefs.getUseTestServer();
        final CheckBox checkBoxUseTestServer = findViewById(R.id.cbUseTestServer);
        if (useTestServer) {
            checkBoxUseTestServer.setChecked(true);
        } else
            checkBoxUseTestServer.setChecked(false);

        boolean useFrequentRecordings = prefs.getUseFrequentRecordings();
        final CheckBox checkBoxUseFrequentRecordings = findViewById(R.id.cbUseFrequentRecordings);
        if (useFrequentRecordings) {
            checkBoxUseFrequentRecordings.setChecked(true);
        } else
            checkBoxUseFrequentRecordings.setChecked(false);

        boolean useVeryFrequentRecordings = prefs.getUseVeryFrequentRecordings();
        final CheckBox checkBoxUseVeryFrequentRecordings = findViewById(R.id.cbUseVeryFrequentRecordings);
        if (useVeryFrequentRecordings) {
            checkBoxUseVeryFrequentRecordings.setChecked(true);
        } else
            checkBoxUseVeryFrequentRecordings.setChecked(false);

        boolean useFrequentUploads = prefs.getUseFrequentUploads();
        final CheckBox checkBoxUseFrequentUploads = findViewById(R.id.cbUseFrequentUploads);
        if (useFrequentUploads) {
            checkBoxUseFrequentUploads.setChecked(true);
        } else
            checkBoxUseFrequentUploads.setChecked(false);

        boolean ignoreLowBattery = prefs.getIgnoreLowBattery();
        final CheckBox checkBoxIgnoreLowBattery = findViewById(R.id.cbIgnoreLowBattery);
        if (ignoreLowBattery) {
            checkBoxIgnoreLowBattery.setChecked(true);
        } else
            checkBoxIgnoreLowBattery.setChecked(false);

        boolean noNetwork = prefs.getOffLineMode();
        final CheckBox checkBoxNoNetwork = findViewById(R.id.cbOffLineMode);
        if (noNetwork) {
            checkBoxNoNetwork.setChecked(true);
        } else {
            checkBoxNoNetwork.setChecked(false);
        }

        boolean onLineMode = prefs.getOnLineMode();
        final CheckBox checkBoxOnLineMode = findViewById(R.id.cbOnLineMode);
        checkBoxOnLineMode.setChecked(onLineMode);

        boolean playWarningSound = prefs.getPlayWarningSound();
        final CheckBox checkBoxPlayWarningSound = findViewById(R.id.cbPlayWarningSound);
        if (playWarningSound) {
            checkBoxPlayWarningSound.setChecked(true);
        } else {
            checkBoxPlayWarningSound.setChecked(false);
        }

        boolean alwaysUpdateGPS = prefs.getPeriodicallyUpdateGPS();
        final CheckBox checkBoxAlwaysUpdateGPS = findViewById(R.id.cbPeriodicallyUpdateGPS);
        checkBoxAlwaysUpdateGPS.setChecked(alwaysUpdateGPS);

        IntentFilter iff = new IntentFilter("event");
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {

            switch (inputMessage.what) {
                case REGISTER_SUCCESS:
                    onResume();
                    registerIdlingResource.decrement();
                    ScrollView mainScrollView = findViewById(R.id.mainScrollView);
                    mainScrollView.fullScroll(ScrollView.FOCUS_UP);
                    try {
                        ((TextView) findViewById(R.id.setupGroupNameInput)).setText("");
                        ((TextView) findViewById(R.id.setupDeviceNameInput)).setText("");

                    }catch (Exception ex){
                        Log.e(TAG, ex.getLocalizedMessage());
                    }

                    Util.getToast(getApplicationContext(),"Success - Device has been registered with the server :-)", false ).show();
                    break;
                case REGISTER_FAIL:
                    onResume();
                    registerIdlingResource.decrement();
//                    Context context = SetupActivity.this;
                    String errorMessage = "Failed to register";
                    if(Server.getErrorMessage() != null){
                        errorMessage = Server.getErrorMessage();
                        // Just use second part, i.e. after colon
                        String message[] = errorMessage.split(":");
                        errorMessage = message[1].trim();

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
        registerIdlingResource.increment();

        Prefs prefs = new Prefs(getApplicationContext());

        if (prefs.getOffLineMode()){
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
        if (group.length() < 1){
            Util.getToast(getApplicationContext(),"Please enter a group name of at least 4 characters (no spaces)", true ).show();
            return;
        }else if (group.length() < 4) {
            Log.i(TAG, "Invalid group name: "+group);

            Util.getToast(getApplicationContext(),group + " is not a valid group name. Please use at least 4 characters (no spaces)", true ).show();
            return;
        }

        // Check that the device name is valid, at least 4 characters.
        String deviceName = ((EditText) findViewById(R.id.setupDeviceNameInput)).getText().toString();
        if (deviceName.length() < 1){
            Util.getToast(getApplicationContext(),"Please enter a device name of at least 4 characters (no spaces)", true ).show();
            return;
        }else if (deviceName.length() < 4) {
            Log.i(TAG, "Invalid device name: "+deviceName);

            Util.getToast(getApplicationContext(),deviceName + " is not a valid device name. Please use at least 4 characters (no spaces)", true ).show();
            return;
        }

        Util.getToast(getApplicationContext(),"Attempting to register with server - please wait", false ).show();

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null){
            Log.e(TAG, "imm is null");
            return;
        }

        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        String groupName = prefs.getGroupName();
        if (groupName != null && groupName.equals(group)) {
            // Try to login with username and password.
            Util.getToast(getApplicationContext(),"Already registered with that group", true ).show();
            return;
        }

        register(group, deviceName, getApplicationContext());

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
        unregister();

    }

    private void unregister(){
        try {

            Prefs prefs = new Prefs(getApplicationContext());
            prefs.setGroupName(null);
            prefs.setPassword(null);
            prefs.setDeviceName(null);
            prefs.setToken(null);
           // if (true){
                Util.getToast(getApplicationContext(),"Success - Device is no longer registered", false ).show();
           // }

            Util.broadcastAMessage(getApplicationContext(), "refresh_vitals_displayed_text");
        }catch(Exception ex){
            Log.e(TAG, "Error Un-registering device.");
        }
        onResume();
        ScrollView mainScrollView = findViewById(R.id.mainScrollView);
        mainScrollView.fullScroll(ScrollView.FOCUS_UP);
    }

    /**
     * Will register the device in the given group saving the JSON Web Token, devicename, and password.
     * @param group name of group to join.
     */
    private void register(final String group, final String deviceName, final Context context) {
        // Check that the group name is valid, at least 4 characters.
        if (group == null || group.length() < 4) {

            Log.e(TAG, "Invalid group name - this should have already been picked up");
            return;
        }

        disableFlightMode();

        // Now wait for network connection as setFlightMode takes a while
        if (!Util.waitForNetworkConnection(getApplicationContext(), true)){
            Log.e(TAG, "Failed to disable airplane mode");
            return ;
        }

        Thread registerThread = new Thread() {
            @Override
            public void run() {
                Message message = handler.obtainMessage();
                if (Server.register(group, deviceName, context)) {
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
        Util.updateGPSLocation(getApplicationContext());
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

    public void onCheckboxPeriodicallyUpdateLocationClicked(View v) {
        Prefs prefs = new Prefs(getApplicationContext());
        // Is the view now checked?
        boolean checked = ((CheckBox) v).isChecked();
            prefs.setPeriodicallyUpdateGPS(checked);
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

    public void onCheckboxUseTestServerClicked(View v) {
        Prefs prefs = new Prefs(getApplicationContext());
        // Is the view now checked?
        boolean checked = ((CheckBox) v).isChecked();
        if (checked){
            prefs.setUseTestServer(true);
        }else{
            prefs.setUseTestServer(false);
        }
    }

    public void onCheckboxUseFrequentUploadsClicked(View v) {
        Prefs prefs = new Prefs(getApplicationContext());
        // Is the view now checked?
        boolean checked = ((CheckBox) v).isChecked();
        if (checked){
            prefs.setUseFrequentUploads(true);
        }else{
            prefs.setUseFrequentUploads(false);
        }
    }

    public void onCheckboxIgnoreLowBatteryClicked(View v) {
        Prefs prefs = new Prefs(getApplicationContext());
        // Is the view now checked?
        boolean checked = ((CheckBox) v).isChecked();
        if (checked){
            prefs.setIgnoreLowBattery(true);
        }else{
            prefs.setIgnoreLowBattery(false);
        }
    }

    public void onCheckboxFrequentRecordingsClicked(View v) {
        Prefs prefs = new Prefs(getApplicationContext());
        // Is the view now checked?
        boolean checked = ((CheckBox) v).isChecked();
        prefs.setUseFrequentRecordings(checked);
    }
    public void onCheckboxVeryFrequentRecordingsClicked(View v) {
        Prefs prefs = new Prefs(getApplicationContext());
        // Is the view now checked?
        boolean checked = ((CheckBox) v).isChecked();

        prefs.setUseVeryFrequentRecordings(checked);
    }


    public void onCheckboxOffLineModeClicked(View v) {
        Prefs prefs = new Prefs(getApplicationContext());
        // Is the view now checked?
        boolean checked = ((CheckBox) v).isChecked();
        prefs.setOffLineMode(checked);

    }

    public void onCheckboxOnLineModeClicked(View v) {
        Prefs prefs = new Prefs(getApplicationContext());
        // Is the view now checked?
        boolean checked = ((CheckBox) v).isChecked();
        prefs.setOnLineMode(checked);
    }

    public void onCheckboxWarningSoundClicked(View v) {
        Prefs prefs = new Prefs(getApplicationContext());
        // Is the view now checked?
        boolean checked = ((CheckBox) v).isChecked();
        prefs.setPlayWarningSound(checked);
    }

    private void disableFlightMode(){
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
            Log.e(TAG, ex.getLocalizedMessage());
            Util.getToast(getApplicationContext(), "Error disabling flight mode", true).show();
        }
    }

    private void updateGpsDisplay(Context context) {
        try {

            Prefs prefs = new Prefs(context);

            double lat = prefs.getLatitude();
            double lon = prefs.getLongitude();

            if (lat != 0 && lon != 0) {
                //http://www.coderzheaven.com/2012/10/14/numberformat-class-android-rounding-number-android-formatting-decimal-values-android/
                NumberFormat numberFormat  = new DecimalFormat("#.000000");
                String latStr = numberFormat.format(lat);
                String lonStr = numberFormat.format(lon);
                TextView locationStatus = findViewById(R.id.setupGPSLocationStatus);

                String latitude = getString(R.string.latitude);
                String longitude = getString(R.string.longitude);
                String locationStatusToDisplay = latitude + ": " + latStr + ", " + longitude + ": " + lonStr;
//                locationStatus.setText("Latitude: " + latStr + ", Longitude: " + lonStr);
                locationStatus.setText(locationStatusToDisplay);
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        Util.createCreateAlarms(getApplicationContext());
        Util.createTheNextSingleStandardAlarm(getApplicationContext());
        Util.setUpLocationUpdateAlarm(getApplicationContext());
    }


    @SuppressWarnings("SameReturnValue")
    public CountingIdlingResource getIdlingResource() {
        return registerIdlingResource;
    }

    @SuppressWarnings("SameReturnValue")
    public CountingIdlingResource getRecordNowIdlingResource() {
        return recordNowIdlingResource;
    }


}
