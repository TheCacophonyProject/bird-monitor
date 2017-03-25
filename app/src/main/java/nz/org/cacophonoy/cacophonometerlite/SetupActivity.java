package nz.org.cacophonoy.cacophonometerlite;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//public class SetupActivity extends Activity {
public class SetupActivity extends AppCompatActivity {
private static final String LOG_TAG = SetupActivity.class.getName();

    // Handler status indicators
    static final int REGISTER_SUCCESS = 1;
    static final int REGISTER_FAIL = 2;
    static final int RESUME = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        EditText serverUrlEditText = (EditText) findViewById(R.id.setupServerUrlInput);
        Prefs prefs = new Prefs(getApplicationContext());
        serverUrlEditText.setText(prefs.getServerUrl());
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.action_settings:
//                // User chose the "Settings" item, show the app settings UI...
//                return true;

            case R.id.action_help:
                openHelp();
                // Toast.makeText(getApplicationContext(), "Settings updated.", Toast.LENGTH_LONG).show();
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
        TextView registerStatus = (TextView) findViewById(R.id.setupRegisterStatus);
        Prefs prefs = new Prefs(getApplicationContext());

        double lat = prefs.getLatitude();
        double lon = prefs.getLongitude();

        if (lat != 0 && lon != 0) {
            TextView locationStatus = (TextView) findViewById(R.id.setupLocationStatus);
            locationStatus.setText("Latitude: "+lat+", Longitude: "+lon);
        }

        String group = prefs.getGroupName();
        if (group != null) {
            String str = "Registered in group: " + group;
            registerStatus.setText(str);
        } else
            registerStatus.setText(R.string.not_registered);

        boolean simPresent = prefs.getSimCardDetected();
        final CheckBox checkBox = (CheckBox) findViewById(R.id.cbSimPresent);
        if (simPresent) {
            checkBox.setChecked(true);
        } else
            checkBox.setChecked(false);



        super.onResume();
    }

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {
            Log.d(LOG_TAG, "Received message.");
            switch (inputMessage.what) {
                case REGISTER_SUCCESS:
                    onResume();
                    Toast.makeText(getApplicationContext(), "Registered device.", Toast.LENGTH_SHORT).show();
                    break;
                case REGISTER_FAIL:
                    onResume();
                    Toast.makeText(getApplicationContext(), "Failed to register.", Toast.LENGTH_SHORT).show();
                case RESUME:
                    onResume();
                default:
                    // Unknown case
                    break;
            }
        }
    };

    public void registerButton(View v) {
        Server.enableDataConnection(getApplicationContext());
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        //Get Group from text field.
        String group = ((EditText) findViewById(R.id.setupGroupNameInput)).getText().toString();
        Prefs prefs = new Prefs(getApplicationContext());
        String groupName = prefs.getGroupName();
        if (groupName != null && groupName.equals(group)) {
            // Try to login with username and password.
            Toast.makeText(getApplicationContext(), "Already registered with that group.", Toast.LENGTH_SHORT).show();
            return;
        }
        register(group, getApplicationContext());
    }

    /**
     * Un-registered a device deleting the password, devicename, and JWT.
     * @param v View
     */
    public void unRegisterButton(View v) {
        Log.d(LOG_TAG, "Un-register device.");
        Prefs prefs = new Prefs(getApplicationContext());
        prefs.setGroupName(null);
        prefs.setPassword(null);
        prefs.setDeviceName(null);
        Server.loggedIn = false;
        onResume();
    }

    /**
     * Will register the device in the given group saving the JSON Web Token, devicename, and password.
     * @param group name of goup to join.
     */
    public void register(final String group, final Context context) {
        // Check that the group name is valid, at least 4 characters.
        if (group == null || group.length() < 4) {
            Log.i("Register", "Invalid group name: "+group);
            Toast.makeText(context, "Invalid Group name: "+group, Toast.LENGTH_SHORT).show();
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

    public void updateServerUrlButton(View v) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        String newUrl =  ((EditText) findViewById(R.id.setupServerUrlInput)).getText().toString();
        if (URLUtil.isValidUrl(newUrl)) {
            Prefs prefs = new Prefs(getApplicationContext());
            prefs.setServerUrl(newUrl);
            Toast.makeText(getApplicationContext(), "Updated Server URL", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Invalid URL", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateLocationButton(View v) {
        Log.i(LOG_TAG, "Update location button");
        Toast.makeText(getApplicationContext(), "Getting new Location...", Toast.LENGTH_SHORT).show();

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        GPSLocationListener gpsLocationListener = new GPSLocationListener(getApplicationContext(), handler);
        try {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, gpsLocationListener, getApplicationContext().getMainLooper());
        } catch (SecurityException e) {
            Log.e(LOG_TAG, "Unable to get GPS location. Don't have required permissions.");
        }

    }

    public void onCheckboxSimPresentClicked(View v) {
        Prefs prefs = new Prefs(getApplicationContext());
        // Is the view now checked?
        boolean checked = ((CheckBox) v).isChecked();
        if (checked){
            prefs.setSimCardDetected(true);
        }else{
            prefs.setSimCardDetected(false);
        }

    }

}
