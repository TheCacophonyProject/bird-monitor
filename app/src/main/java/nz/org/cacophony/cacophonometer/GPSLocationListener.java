package nz.org.cacophony.cacophonometer;


import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * The app has the ability to save the current GPS location.  When the app asks the Android OS
 * for a new location, this class listens for the broadcast from the OS and responds to it by
 * saving the new location and displaying a message to the use.
 */
class GPSLocationListener implements LocationListener {
    private static final String TAG = GPSLocationListener.class.getName();

    private final Context context;


    GPSLocationListener(Context context) {
        this.context = context;

    }



    public void onLocationChanged(Location location) {

       try {
           Log.d(TAG, "onLocationChanged 1");
           double lat = location.getLatitude();
           lat = Math.round(lat*1000000.0)/1000000.0;
           double lon = location.getLongitude();
           lon = Math.round(lon*1000000.0)/1000000.0;

           Prefs prefs = new Prefs(context);
           prefs.setLatitude(lat);
           prefs.setLongitude(lon);

           // Tell SetupActivity to resume.
           Util.getToast(context, "New Location saved", false).show();

           // send a broadcast for SetupActivity to update gps location text
           String messageToDisplay = "";
           JSONObject jsonObjectMessageToBroadcast = new JSONObject();
           try {
               jsonObjectMessageToBroadcast.put("messageToType", "refresh_gps_coordinates");
               jsonObjectMessageToBroadcast.put("messageToDisplay", "refresh_gps_coordinates");
           } catch (JSONException e) {
               e.printStackTrace();
           }
           Util.broadcastAMessage(context, jsonObjectMessageToBroadcast);

          // Util.broadcastAMessage(context, "refresh_gps_coordinates");

           Log.d(TAG, "onLocationChanged 2");
       }catch (Exception ex){

           Log.e(TAG, ex.getLocalizedMessage());
       }

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {}

    public void onProviderEnabled(String provider) {}

    public void onProviderDisabled(String provider) {}

}
