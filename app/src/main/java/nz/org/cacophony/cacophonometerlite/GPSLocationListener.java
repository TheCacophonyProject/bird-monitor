package nz.org.cacophony.cacophonometerlite;


import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;



class GPSLocationListener implements LocationListener {
    private static final String TAG = GPSLocationListener.class.getName();

    private Context context = null;


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

           // Trying to find reason for crashing - seems to be connected with getting GPS - so will try commenting out the next
           // few lines that are telling the SetupActivity to resume
//           Message message = handler.obtainMessage();
//           message.what = SetupActivity.RESUME;
//           message.sendToTarget();

           // send a broadcast for SetupActivity to update gps location text
         //  Util.broadcastAMessage(context, "refresh_vitals_displayed_text");
           Util.broadcastAMessage(context, "refresh_gps_coordinates");

           Log.d(TAG, "onLocationChanged 2");
       }catch (Exception ex){
//           logger.error(ex.getLocalizedMessage());
           Log.e(TAG, ex.getLocalizedMessage());
       }
//        logger.debug("onLocationChanged method finished");
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {}

    public void onProviderEnabled(String provider) {}

    public void onProviderDisabled(String provider) {}

}
