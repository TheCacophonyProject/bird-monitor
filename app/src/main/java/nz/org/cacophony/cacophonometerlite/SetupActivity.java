package nz.org.cacophony.cacophonometerlite;

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

//import android.util.Log;

//import static com.loopj.android.http.AsyncHttpClient.LOG_TAG;


//public class SetupActivity extends Activity {
public class SetupActivity extends AppCompatActivity implements IdlingResourceForEspressoTesting {
    private static final String TAG = SetupActivity.class.getName();

//    // https://www.youtube.com/watch?v=uCtzH0Rz5XU
//    private CountingIdlingResource idlingResource = new CountingIdlingResource("SERVER_CONNECTION");

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

            Log.w(TAG, "ActionBar ab is null");

        }


    }

    private BroadcastReceiver onNotice= new BroadcastReceiver() {
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
//                logger.error(ex.getLocalizedMessage());
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

        TextView registerStatus = (TextView) findViewById(R.id.setupRegisterStatus);
        Prefs prefs = new Prefs(getApplicationContext());



        updateGpsDisplay(getApplicationContext());

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



        boolean useShortRecordings = prefs.getUseShortRecordings();
        final CheckBox checkBoxUseUseShortRecordings = (CheckBox) findViewById(R.id.cbShortRecordings);
        if (useShortRecordings) {
            checkBoxUseUseShortRecordings.setChecked(true);
        } else
            checkBoxUseUseShortRecordings.setChecked(false);

        boolean useTestServer = prefs.getUseTestServer();
        final CheckBox checkBoxUseTestServer = (CheckBox) findViewById(R.id.cbUseTestServer);
        if (useTestServer) {
            checkBoxUseTestServer.setChecked(true);
        } else
            checkBoxUseTestServer.setChecked(false);

        boolean useFrequentRecordings = prefs.getUseFrequentRecordings();
        final CheckBox checkBoxUseFrequentRecordings = (CheckBox) findViewById(R.id.cbUseFrequentRecordings);
        if (useFrequentRecordings) {
            checkBoxUseFrequentRecordings.setChecked(true);
        } else
            checkBoxUseFrequentRecordings.setChecked(false);

        boolean useVeryFrequentRecordings = prefs.getUseVeryFrequentRecordings();
        final CheckBox checkBoxUseVeryFrequentRecordings = (CheckBox) findViewById(R.id.cbUseVeryFrequentRecordings);
        if (useVeryFrequentRecordings) {
            checkBoxUseVeryFrequentRecordings.setChecked(true);
        } else
            checkBoxUseVeryFrequentRecordings.setChecked(false);

        boolean useFrequentUploads = prefs.getUseFrequentUploads();
        final CheckBox checkBoxUseFrequentUploads = (CheckBox) findViewById(R.id.cbUseFrequentUploads);
        if (useFrequentUploads) {
            checkBoxUseFrequentUploads.setChecked(true);
        } else
            checkBoxUseFrequentUploads.setChecked(false);

        boolean ignoreLowBattery = prefs.getIgnoreLowBattery();
        final CheckBox checkBoxIgnoreLowBattery = (CheckBox) findViewById(R.id.cbIgnoreLowBattery);
        if (ignoreLowBattery) {
            checkBoxIgnoreLowBattery.setChecked(true);
        } else
            checkBoxIgnoreLowBattery.setChecked(false);

        boolean noNetwork = prefs.getOffLineMode();
        final CheckBox checkBoxNoNetwork = (CheckBox) findViewById(R.id.cbOffLineMode);
        if (noNetwork) {
            checkBoxNoNetwork.setChecked(true);
        } else {
            checkBoxNoNetwork.setChecked(false);
        }

        boolean onLineMode = prefs.getOnLineMode();
        final CheckBox checkBoxOnLineMode = (CheckBox) findViewById(R.id.cbOnLineMode);
        checkBoxOnLineMode.setChecked(onLineMode);

        boolean playWarningSound = prefs.getPlayWarningSound();
        final CheckBox checkBoxPlayWarningSound = (CheckBox) findViewById(R.id.cbPlayWarningSound);
        if (playWarningSound) {
            checkBoxPlayWarningSound.setChecked(true);
        } else {
            checkBoxPlayWarningSound.setChecked(false);
        }

        boolean alwaysUpdateGPS = prefs.getAlwaysUpdateGPS();
        final CheckBox checkBoxAlwaysUpdateGPS = (CheckBox) findViewById(R.id.cbAlwaysUpdateGPS);
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
                    idlingResource.decrement();
                    ScrollView mainScrollView = (ScrollView)findViewById(R.id.mainScrollView);
                    mainScrollView.fullScroll(ScrollView.FOCUS_UP);
                    try {
                        ((TextView) findViewById(R.id.setupGroupNameInput)).setText("");

                    }catch (Exception ex){
                        Log.e(TAG, ex.getLocalizedMessage());

                    }

                    Util.getToast(getApplicationContext(),"Success - Device has been registered with the server :-)", false ).show();
                    break;
                case REGISTER_FAIL:
                    onResume();
                    idlingResource.decrement();
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
        idlingResource.increment();

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
        if (group == null || group.length() < 1){
            Util.getToast(getApplicationContext(),"Please enter a group name of at least 4 characters (no spaces)", true ).show();
            return;
        }else if (group.length() < 4) {
            Log.i(TAG, "Invalid group name: "+group);

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
//new Thread(new Runnable(){
//    @Override
//    public void run(){
//        try{
//            Thread.sleep(5000);
//        }catch (Exception ex){
//            ex.printStackTrace();
//        }
//
//        runOnUiThread(new Runnable(){
//            @Override
//            public void run(){
//                TextView registerStatus = (TextView) findViewById(R.id.setupRegisterStatus);
//                registerStatus.setText("tim was here");
//                idlingResource.decrement();
//            }
//        });
//    }
//}).start();

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

            Prefs prefs = new Prefs(getApplicationContext());
            prefs.setGroupName(null);
            prefs.setPassword(null);
            prefs.setDeviceName(null);
           // Server.loggedIn = false;
            prefs.setToken(null);
            if (displayUnregisterdMessage){
                Util.getToast(getApplicationContext(),"Success - Device is no longer registered", false ).show();
            }

            Util.broadcastAMessage(getApplicationContext(), "refresh_vitals_displayed_text");
        }catch(Exception ex){
            Log.e(TAG, "Error Un-registering device.");

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

            return ;
        }

//        idlingResource.increment();
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
//                idlingResource.decrement();
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


    public void onCheckboxAlwaysUpdateLocationClicked(View v) {
        Prefs prefs = new Prefs(getApplicationContext());
        // Is the view now checked?
        boolean checked = ((CheckBox) v).isChecked();
            prefs.setAlwaysUpdateGPS(checked);
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

//        Util.createAlarms(getApplicationContext(), "repeating", "normal");
    }
    public void onCheckboxVeryFrequentRecordingsClicked(View v) {
        Prefs prefs = new Prefs(getApplicationContext());
        // Is the view now checked?
        boolean checked = ((CheckBox) v).isChecked();

        prefs.setUseVeryFrequentRecordings(checked);
       // Util.createAlarms(getApplicationContext(), "repeating", "normal", "SetupActivityonCheckboxVeryFrequentRecordingsClicked");
//        Util.createAlarms(getApplicationContext(), "repeating", "normal");
    }


    public void onCheckboxOffLineModeClicked(View v) {
        Prefs prefs = new Prefs(getApplicationContext());
        // Is the view now checked?
        boolean checked = ((CheckBox) v).isChecked();
        prefs.setOffLineMode(checked);
//        if (checked){
//            prefs.setOffLineMode(true);
//        }else{
//            prefs.setOffLineMode(false);
//        }
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
//        if (checked){
//            prefs.setPlayWarningSound(true);
//        }else{
//            prefs.setPlayWarningSound(false);
//        }
    }



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
                          //  Util.disableFlightModeTestSU(getApplicationContext());
                        }
                    });
                }
            }.start();



        }catch (Exception ex){

            Log.e(TAG, ex.getLocalizedMessage());
            Util.getToast(getApplicationContext(), "Error disabling flight mode", true).show();
        }
    }

    void updateGpsDisplay(Context context) {
        try {

            Prefs prefs = new Prefs(context);

            double lat = prefs.getLatitude();
            double lon = prefs.getLongitude();

            if (lat != 0 && lon != 0) {
                //http://www.coderzheaven.com/2012/10/14/numberformat-class-android-rounding-number-android-formatting-decimal-values-android/
                NumberFormat numberFormat  = new DecimalFormat("#.000000");
                String latStr = numberFormat.format(lat);
                String lonStr = numberFormat.format(lon);
                TextView locationStatus = (TextView) findViewById(R.id.setupGPSLocationStatus);
                //  locationStatus.setText("Latitude: "+lat+", Longitude: "+lon);
                locationStatus.setText("Latitude: " + latStr + ", Longitude: " + lonStr);
                //  locationStatus.setText("tim was here");
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        Util.createCreateAlarms(getApplicationContext());
        Util.createAlarms(getApplicationContext(), "repeating", "normal");
    }


    public CountingIdlingResource getIdlingResource() {
        return idlingResource;
    }
}
