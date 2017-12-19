package nz.org.cacophony.cacophonometerlite;


import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
//import android.util.Log;
//import android.widget.Toast;

import org.slf4j.Logger;

import static android.content.ContentValues.TAG;
//import org.slf4j.LoggerFactory;

class GPSLocationListener implements LocationListener {
    private static final String TAG = GPSLocationListener.class.getName();

    private Context context = null;
//    private static Logger logger = null;
 //   private Handler handler = null;

    GPSLocationListener(Context context) {
        this.context = context;

//        logger = Util.getAndConfigureLogger(context, LOG_TAG);
//        logger.debug("End of GPSLocationListener");
    }

//    GPSLocationListener(Context context, Handler handler) {
//        this.context = context;
//        this.handler = handler;
////        logger = Util.getAndConfigureLogger(context, LOG_TAG);
////        logger.debug("End of GPSLocationListener");
//    }

    public void onLocationChanged(Location location) {
//        logger.debug("onLocationChanged method entered");
       try {
           Log.d(TAG, "onLocationChanged 1");
           double lat = location.getLatitude();
           double lon = location.getLongitude();
           // Log.i(LOG_TAG, "Latitude: "+lat+", Longitude: "+lon);
//           logger.info("Latitude: " + lat + ", Longitude: " + lon);
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
           Util.broadcastAMessage(context, "refresh_vitals_displayed_text");

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
