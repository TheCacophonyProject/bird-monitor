package nz.org.cacophony.cacophonometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

public class RegisterActivity extends AppCompatActivity implements IdlingResourceForEspressoTesting {

    private static final String TAG = RegisterActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    @Override
    public void onResume() {
        super.onResume();
        Prefs prefs = new Prefs(getApplicationContext());
        IntentFilter iff = new IntentFilter("event");
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);

        if ( prefs.getGroupName() != null) {
            ((TextView) findViewById(R.id.tvGroup)).setText("Group - " + prefs.getGroupName());
        }
        if ( prefs.getDeviceName() != null) {
            ((TextView) findViewById(R.id.tvDeviceName)).setText("Device Name - " + prefs.getDeviceName());
        }
    }

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
              Server.register(group, deviceName, context);;
            }
        };
        registerThread.start();
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

            Util.getToast(getApplicationContext(),"Success - Device is no longer registered", false ).show();

                ((TextView) findViewById(R.id.tvGroup)).setText("Group");
                ((TextView) findViewById(R.id.tvDeviceName)).setText("Device Name");


           // Util.broadcastAMessage(getApplicationContext(), "refresh_vitals_displayed_text");
        }catch(Exception ex){
            Log.e(TAG, "Error Un-registering device.");
        }

    }



    public void next(@SuppressWarnings("UnusedParameters") View v) {

        try {

            Intent intent = new Intent(this, RootedActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    public void back(@SuppressWarnings("UnusedParameters") View v) {

        try {

//            Intent intent = new Intent(this, MainActivity2.class);
//            startActivity(intent);
            finish();

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    private void disableFlightMode(){
        try {
            Util.disableFlightMode(getApplicationContext());


        }catch (Exception ex){
            Log.e(TAG, ex.getLocalizedMessage());
            Util.getToast(getApplicationContext(), "Error disabling flight mode", true).show();
        }
    }

    private final BroadcastReceiver onNotice = new BroadcastReceiver() {
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

        // broadcast notification coming from ??
        @Override
        public void onReceive(Context context, Intent intent) {
            Prefs prefs = new Prefs(getApplicationContext());
            try {
                String message = intent.getStringExtra("message");
                if (message != null) {

                   if (message.equalsIgnoreCase("REGISTER_SUCCESS")) {
                       String messageToDisplay = "Success - Your phone has been registered with the server :-)";
                       Util.getToast(getApplicationContext(),messageToDisplay, false ).show();
                       registerIdlingResource.decrement();
                       try {
                           ((TextView) findViewById(R.id.setupGroupNameInput)).setText("");
                           ((TextView) findViewById(R.id.setupDeviceNameInput)).setText("");
                           ((TextView) findViewById(R.id.tvGroup)).setText("Group - " + prefs.getGroupName());
                           ((TextView) findViewById(R.id.tvDeviceName)).setText("Device Name - " + prefs.getDeviceName());

                       }catch (Exception ex){
                           Log.e(TAG, ex.getLocalizedMessage());
                       }

                    }else if (message.equalsIgnoreCase("REGISTER_FAIL_UNKNOWN_REASON")) {
                       String errorMessage = "Oops, your phone did not register - not sure why";
                       Util.getToast(getApplicationContext(),errorMessage, true ).show();
                       registerIdlingResource.decrement();

                   }else if (message.startsWith("devicename:")) {
                       String errorMessage = message.substring("devicename:".length());
                        Util.getToast(getApplicationContext(),errorMessage, true ).show(); // need to improve this message
                       registerIdlingResource.decrement();
                    }else if (message.startsWith("NEITHER_200_NOR_422")) {
                       String errorMessage = message.substring(19);
                       Util.getToast(getApplicationContext(),errorMessage, true ).show(); // need to improve this message
                       registerIdlingResource.decrement();
                   }

                }

            } catch (Exception ex) {

                Log.e(TAG, ex.getLocalizedMessage());
            }
        }
    };

    @SuppressWarnings("SameReturnValue")
    public CountingIdlingResource getIdlingResource() {
        return registerIdlingResource;
    }

    @SuppressWarnings("SameReturnValue")
    public CountingIdlingResource getRecordNowIdlingResource() {
        return recordNowIdlingResource;
    }
}
