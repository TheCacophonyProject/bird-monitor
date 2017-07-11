package nz.org.cacophony.cacophonometerlite;


import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
//import android.util.Log;
//import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class GPSLocationListener implements LocationListener {
    private static final String LOG_TAG = Server.class.getName();

    private Context context = null;
    private static Logger logger = null;
    private Handler handler = null;



    GPSLocationListener(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
        logger = Util.getAndConfigureLogger(context, LOG_TAG);
    }

    public void onLocationChanged(Location location) {

        Util.getToast(context,"New Location saved", false ).show();
        double lat = location.getLatitude();
        double lon = location.getLongitude();
       // Log.i(LOG_TAG, "Latitude: "+lat+", Longitude: "+lon);
        logger.info("Latitude: "+lat+", Longitude: "+lon);
        Prefs prefs = new Prefs(context);
        prefs.setLatitude(lat);
        prefs.setLongitude(lon);

        // Tell SetupActivity to resume.
        Message message = handler.obtainMessage();
        message.what = SetupActivity.RESUME;
        message.sendToTarget();
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {}

    public void onProviderEnabled(String provider) {}

    public void onProviderDisabled(String provider) {}

}
