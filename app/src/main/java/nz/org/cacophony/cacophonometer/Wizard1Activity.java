package nz.org.cacophony.cacophonometer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;


public class Wizard1Activity extends AppCompatActivity implements IdlingResourceForEspressoTesting {
    private static final String TAG = Wizard1Activity.class.getName();

    // Handler status indicators
    private static final int REGISTER_SUCCESS = 1;
    private static final int REGISTER_FAIL = 2;
    private static final int RESUME = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wizard1);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        if (ab != null){
          //  ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayUseLogoEnabled(true);
            ab.setLogo(R.mipmap.ic_launcher);
        }else{
            Log.w(TAG, "ActionBar ab is null");
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

            case R.id.action_settings:
                openSettings();
                return true;

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

    private void openSettings() {
        try {

            Intent intent = new Intent(this, SetupActivity.class);
            startActivity(intent);
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    private void openHelp() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    private void openVitals() {
        Intent intent = new Intent(this, VitalsActivity.class);
        startActivity(intent);
    }


    public void registerButton(View v) {
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

    private void register(final String group, final String deviceName, final Context context) {
        // Check that the group name is valid, at least 4 characters.
        if (group == null || group.length() < 4) {

            Log.e(TAG, "Invalid group name - this should have already been picked up");
            return;
        }

     //   disableFlightMode();

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

    private void disableFlightMode(){
        try {
            //https://stackoverflow.com/questions/3875184/cant-create-handler-inside-thread-that-has-not-called-looper-prepare
            new Thread()
            {
                public void run()
                {
                    Wizard1Activity.this.runOnUiThread(new Runnable()
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
                  //  registerIdlingResource.decrement();
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

}
