package nz.org.cacophony.cacophonometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_gps, container, false);

        setUserVisibleHint(false);

        btnGetGPSLocation = (Button) view.findViewById(R.id.btnGetGPSLocation);
        btnGetGPSLocation.setOnClickListener(new View.OnClickListener(){
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
        if (getActivity() == null){
            return;
        }
        if (visible) {

            IntentFilter iff = new IntentFilter("GPS");
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onNotice, iff);

            IntentFilter iffRoot = new IntentFilter("ROOT");
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onNoticeRoot, iffRoot);

            updateGpsDisplay(getActivity().getApplicationContext());

        }else{

            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onNotice);
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onNoticeRoot);
        }
    }

    public void updateGPSLocationButtonPressed() {

        TextView latitudeDisplay = getView().findViewById(R.id.tvLatitude);
        TextView longitudeDisplay = getView().findViewById(R.id.tvLongitude);
        latitudeDisplay.setText(getString(R.string.latitude) );
        longitudeDisplay.setText(getString(R.string.longitude) );


        TextView tvSearching = getView().findViewById(R.id.tvSearching);
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
                            Util.getToast(context, messageToDisplay, true).show();
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
                            Util.getToast(getActivity().getApplicationContext(), "It looks like you have incorrectly indicated in settings that this phone has been rooted", true).show();
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
            TextView latitudeDisplay = getView().findViewById(R.id.tvLatitude);
            TextView longitudeDisplay = getView().findViewById(R.id.tvLongitude);

            TextView tvSearching = getView().findViewById(R.id.tvSearching);
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
