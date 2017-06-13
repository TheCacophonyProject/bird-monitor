package nz.org.cacophony.cacophonometerlite;


import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

class GPSLocationListener implements LocationListener {
    private static final String LOG_TAG = Server.class.getName();

    private Context context = null;
    private Handler handler = null;

    GPSLocationListener(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    public void onLocationChanged(Location location) {
//        Toast.makeText(context, "New Location saved.", Toast.LENGTH_SHORT).show();
        Util.getToast(context,"New Location saved", false ).show();
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        Log.i(LOG_TAG, "Latitude: "+lat+", Longitude: "+lon);
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
