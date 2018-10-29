package nz.org.cacophony.cacophonometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;


public class GPSActivity extends AppCompatActivity {
    private static final String TAG = GPSActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateGpsDisplay(getApplicationContext());

        IntentFilter iff = new IntentFilter("event");
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
    }

    public void updateGPSLocationButton(@SuppressWarnings("UnusedParameters") View v) {
        TextView tvSearching = findViewById(R.id.tvSearching);
        tvSearching.setVisibility(View.VISIBLE);
        Util.updateGPSLocation(getApplicationContext());
    }

    public void next(@SuppressWarnings("UnusedParameters") View v) {
        try {
            Intent intent = new Intent(this, WalkingActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    public void back(@SuppressWarnings("UnusedParameters") View v) {

        try {
            Intent intent = new Intent(this, RootedActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    private final BroadcastReceiver onNotice = new BroadcastReceiver() {
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String message = intent.getStringExtra("message");
                if (message != null) {
                    if (message.equalsIgnoreCase("refresh_gps_coordinates")) {
                        updateGpsDisplay(context);
                    } else if (message.equalsIgnoreCase("turn_on_gps_and_try_again")) {
                        Util.getToast(context, "Sorry, GPS is not enabled.  Please enable location/gps in the phone settings and try again.", true).show();
                    } else if (message.equalsIgnoreCase("error_do_not_have_root")) {
                        Util.getToast(getApplicationContext(), "It looks like you have incorrectly indicated in settings that this phone has been rooted", true).show();
                    }
                }

            } catch (Exception ex) {
                Log.e(TAG, ex.getLocalizedMessage());
            }
        }
    };

    private void updateGpsDisplay(Context context) {
        try {

            Prefs prefs = new Prefs(context);
            TextView latitudeDisplay = findViewById(R.id.tvLatitude);
            TextView longitudeDisplay = findViewById(R.id.tvLongitude);

            TextView tvSearching = findViewById(R.id.tvSearching);
            tvSearching.setVisibility(View.INVISIBLE);

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
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }
}
