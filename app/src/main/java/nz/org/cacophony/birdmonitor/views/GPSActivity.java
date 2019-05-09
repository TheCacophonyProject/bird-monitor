package nz.org.cacophony.birdmonitor.views;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import nz.org.cacophony.birdmonitor.PermissionsHelper;
import nz.org.cacophony.birdmonitor.Prefs;
import nz.org.cacophony.birdmonitor.R;
import nz.org.cacophony.birdmonitor.Util;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class GPSActivity extends AppCompatActivity {

    private static final String TAG = GPSActivity.class.getName();
    private TextView tvMessages;
    private TextView tvSearching;
    private TextView latitudeDisplay;
    private TextView longitudeDisplay;
    Button btnGetGPSLocation;

    private PermissionsHelper permissionsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        tvMessages = findViewById(R.id.tvMessages);
        latitudeDisplay = findViewById(R.id.tvLatitude);
        longitudeDisplay = findViewById(R.id.tvLongitude);
        tvSearching = findViewById(R.id.tvSearching);
        btnGetGPSLocation = findViewById(R.id.btnGetGPSLocation);

        btnGetGPSLocation.setOnClickListener(v -> updateGPSLocationButtonPressed());

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        checkPermissions();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.button_help:
                Util.displayHelp(this, getResources().getString(R.string.activity_or_fragment_title_gps_location));
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_help, menu);
        return true;
    }

    @Override
    public void onResume() {
        try {
            super.onResume();
        } catch (Exception ex) {
            // This is very poor, but I have no idea why super.onResume give a null pointer exception
            // Need to spend time on this
            Log.e(TAG, "Error calling super.onResume");
        }

        IntentFilter iff = new IntentFilter("GPS");
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);

        IntentFilter iffRoot = new IntentFilter("ROOT");
        LocalBroadcastManager.getInstance(this).registerReceiver(onNoticeRoot, iffRoot);

        checkPermissions();

        updateGpsDisplay(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();

        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNoticeRoot);
    }

    private void updateGPSLocationButtonPressed() {
        // First check to see if Location service is available
        // https://stackoverflow.com/questions/25175522/how-to-enable-location-access-programmatically-in-android
        if (!canGetLocation()) {
            // Display dialog
            displayMessage();
            return;
        }


        latitudeDisplay.setText(getString(R.string.latitude));
        longitudeDisplay.setText(getString(R.string.longitude));

        tvSearching.setVisibility(View.VISIBLE);
        Util.updateGPSLocation(this.getApplicationContext());
    }

    private void displayMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Add the buttons
        builder.setPositiveButton("OK", (dialog, id) -> {
            Intent intent2 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent2);
        });

        builder.setMessage("Your phone\'s Location service is Off.  Press OK, to be taken to settings, and turn Location ON.  Then press your phone\'s back button to return here and press the UPDATE GPS LOCATION again.")
                .setTitle("Please turn on your phone\'s location service.");

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button btnPositive = dialog.getButton(Dialog.BUTTON_POSITIVE);
            btnPositive.setTextSize(24);
            int btnPositiveColor = ResourcesCompat.getColor(this.getResources(), R.color.dialogButtonText, null);
            btnPositive.setTextColor(btnPositiveColor);

            //https://stackoverflow.com/questions/6562924/changing-font-size-into-an-alertdialog
            TextView textView = dialog.findViewById(android.R.id.message);
            textView.setTextSize(22);
        });

        dialog.show();
    }

    private boolean canGetLocation() {

        LocationManager lm = null;
        boolean gps_enabled = false;

        if (lm == null) {
            lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        }

        // exceptions will be thrown if provider is not permitted.
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ignored) {

        }

        return gps_enabled;
    }

    private void updateGpsDisplay(Context context) {
        try {

            Prefs prefs = new Prefs(context);
            tvMessages.setText("");


            tvSearching.setVisibility(View.GONE);

            double lat = prefs.getLatitude();
            double lon = prefs.getLongitude();

            if (lat != 0 && lon != 0) {
                //http://www.coderzheaven.com/2012/10/14/numberformat-class-android-rounding-number-android-formatting-decimal-values-android/
                NumberFormat numberFormat = new DecimalFormat("#.000000");
                String latStr = numberFormat.format(lat);
                String lonStr = numberFormat.format(lon);

                latitudeDisplay.setText(getString(R.string.latitude) + ": " + latStr);
                longitudeDisplay.setText(getString(R.string.longitude) + ": " + lonStr);

            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsHelper.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    private void checkPermissions() {
        permissionsHelper = new PermissionsHelper();
        permissionsHelper.checkAndRequestPermissions(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private final BroadcastReceiver onNotice = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            try {

                String jsonStringMessage = intent.getStringExtra("jsonStringMessage");
                if (jsonStringMessage != null) {

                    JSONObject joMessage = new JSONObject(jsonStringMessage);
                    String messageType = joMessage.getString("messageType");

                    if (messageType != null) {
                        if (messageType.equalsIgnoreCase("GPS_UPDATE_SUCCESS")) {
                            updateGpsDisplay(context);
                        } else {
                            String messageToDisplay = joMessage.getString("messageToDisplay");

                            displayOKDialogMessage("Oops", messageToDisplay);

                            tvSearching.setVisibility(View.GONE);
                        }
                    }
                }

            } catch (Exception ex) {
                Log.e(TAG, ex.getLocalizedMessage(), ex);
            }
        }
    };

    public void displayOKDialogMessage(String title, String messageToDisplay) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Add the buttons
        builder.setPositiveButton("OK", (dialog, id) -> {
        });

        builder.setMessage(messageToDisplay)
                .setTitle(title);

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {

            Button btnPositive = dialog.getButton(Dialog.BUTTON_POSITIVE);
            btnPositive.setTextSize(24);
            int btnPositiveColor = ResourcesCompat.getColor(getResources(), R.color.dialogButtonText, null);
            btnPositive.setTextColor(btnPositiveColor);

            //https://stackoverflow.com/questions/6562924/changing-font-size-into-an-alertdialog
            TextView textView = dialog.findViewById(android.R.id.message);
            textView.setTextSize(22);
        });

        dialog.show();
    }


    private final BroadcastReceiver onNoticeRoot = new BroadcastReceiver() {
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

        @Override
        public void onReceive(Context context, Intent intent) {
            try {


                String jsonStringMessage = intent.getStringExtra("jsonStringMessage");
                if (jsonStringMessage != null) {
                    String messageType = intent.getStringExtra("messageType");
                    if (messageType != null) {
                        if (messageType.equalsIgnoreCase("error_do_not_have_root")) {
                            displayOKDialogMessage("Oops", "It looks like you have incorrectly indicated in settings that this phone has been rooted");
                        }
                    }
                }

            } catch (Exception ex) {
                Log.e(TAG, ex.getLocalizedMessage(), ex);
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

}
