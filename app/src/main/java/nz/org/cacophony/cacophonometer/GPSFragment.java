package nz.org.cacophony.cacophonometer;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class GPSFragment extends Fragment {

    private static final String TAG = "GPSFragment";


    private Button btnGetGPSLocation;
    private TextView tvMessages;
    private TextView tvSearching;
    private TextView latitudeDisplay;
    private TextView longitudeDisplay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gps, container, false);

        setUserVisibleHint(false);
        tvMessages = view.findViewById(R.id.tvMessages);
        tvSearching = view.findViewById(R.id.tvSearching);
        latitudeDisplay = view.findViewById(R.id.tvLatitude);
        longitudeDisplay = view.findViewById(R.id.tvLongitude);

        btnGetGPSLocation = view.findViewById(R.id.btnGetGPSLocation);
        btnGetGPSLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateGPSLocationButtonPressed();
            }
        });

        return view;
    }

    @Override
    public void setUserVisibleHint(final boolean visible) {
        super.setUserVisibleHint(visible);
        if (getActivity() == null) {
            return;
        }
        if (visible) {

            IntentFilter iff = new IntentFilter("GPS");
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onNotice, iff);

            IntentFilter iffRoot = new IntentFilter("ROOT");
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onNoticeRoot, iffRoot);

            updateGpsDisplay(getActivity().getApplicationContext());

        } else {

            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onNotice);
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onNoticeRoot);
        }
    }

    public void updateGPSLocationButtonPressed() {

        // First check to see if Location service is available
        // https://stackoverflow.com/questions/25175522/how-to-enable-location-access-programmatically-in-android
        if (!canGetLocation()){
            // Display dialog
            displayMessage();
            return;
        }

        TextView latitudeDisplay = getView().findViewById(R.id.tvLatitude);
        TextView longitudeDisplay = getView().findViewById(R.id.tvLongitude);
        latitudeDisplay.setText(getString(R.string.latitude));
        longitudeDisplay.setText(getString(R.string.longitude));


        // TextView tvSearching = getView().findViewById(R.id.tvSearching);
        tvSearching.setVisibility(View.VISIBLE);
        Util.updateGPSLocation(getActivity().getApplicationContext());
    }

    private final BroadcastReceiver onNotice = new BroadcastReceiver() {
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                if (getView() == null) {
                    return;
                }

                String jsonStringMessage = intent.getStringExtra("jsonStringMessage");
                if (jsonStringMessage != null) {

                    JSONObject joMessage = new JSONObject(jsonStringMessage);
                    String messageType = joMessage.getString("messageType");

                    if (messageType != null) {
                        if (messageType.equalsIgnoreCase("GPS_UPDATE_SUCCESS")) {
                            updateGpsDisplay(context);
                        } else {
                            String messageToDisplay = joMessage.getString("messageToDisplay");

                              ((SetupWizardActivity) getActivity()).displayOKDialogMessage("Oops", messageToDisplay);

                            tvSearching.setVisibility(View.GONE);
                        }
                    }
                }

            } catch (Exception ex) {
                Log.e(TAG, ex.getLocalizedMessage());
            }
        }
    };


    private final BroadcastReceiver onNoticeRoot = new BroadcastReceiver() {
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (getView() == null) {
                    return;
                }

                String jsonStringMessage = intent.getStringExtra("jsonStringMessage");
                if (jsonStringMessage != null) {

                    JSONObject joMessage = new JSONObject(jsonStringMessage);
                    String messageType = intent.getStringExtra("messageType");
                    String messageToDisplay = joMessage.getString("messageToDisplay");

                    if (messageType != null) {
                        if (messageType.equalsIgnoreCase("error_do_not_have_root")) {
                            ((SetupWizardActivity) getActivity()).displayOKDialogMessage("Oops", "It looks like you have incorrectly indicated in settings that this phone has been rooted");
                        }
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
            tvMessages.setText("");
            latitudeDisplay = getView().findViewById(R.id.tvLatitude);
            longitudeDisplay = getView().findViewById(R.id.tvLongitude);

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
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    public boolean canGetLocation() {

        LocationManager lm = null;
        boolean gps_enabled = false;

        if (lm == null)

            lm = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);

        // exceptions will be thrown if provider is not permitted.
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {

        }



        return gps_enabled;
    }

    private void displayMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Add the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent2 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent2);
            }
        });

        builder.setMessage("Your phone\'s Location service is Off.  Press OK, to be taken to settings, and turn Location ON.  Then press your phone\'s back button to return here and press the UPDATE GPS LOCATION again.")
                .setTitle("Please turn on your phone\'s location service.");

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btnPositive = dialog.getButton(Dialog.BUTTON_POSITIVE);
                btnPositive.setTextSize(24);
                int btnPositiveColor = ResourcesCompat.getColor(getActivity().getResources(), R.color.dialogButtonText, null);
                btnPositive.setTextColor(btnPositiveColor);

                //https://stackoverflow.com/questions/6562924/changing-font-size-into-an-alertdialog
                TextView textView = (TextView) dialog.findViewById(android.R.id.message);
                textView.setTextSize(22);
            }
        });

        dialog.show();
    }

}
