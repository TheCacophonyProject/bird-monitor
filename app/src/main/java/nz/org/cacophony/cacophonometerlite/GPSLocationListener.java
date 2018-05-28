package nz.org.cacophony.cacophonometerlite;


import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;



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

           Util.broadcastAMessage(context, "refresh_gps_coordinates");

           Log.d(TAG, "onLocationChanged 2");
       }catch (Exception ex){

           Log.e(TAG, ex.getLocalizedMessage());
       }

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {}

    public void onProviderEnabled(String provider) {}

    public void onProviderDisabled(String provider) {}

}
