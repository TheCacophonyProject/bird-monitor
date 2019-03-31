package nz.org.cacophony.cacophonometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import static android.content.Context.POWER_SERVICE;

/**
 * This class is part of the process of updating the GPS location.  A wakelock is used to keep the
 * process awake long enough to finish.
 *
 * Because the time to obtain an new GPS location is
 * indeterminate, there was an issue of having the correct GPS location for each recording when
 * in walking mode.  A compromise was made that meant / involved just updating the GPS location
 * every 5 minutes and so a recording could have a GPS location that was up to 5 minutes old - which
 * should not be a major issue when moving a typical walking speed.
 */

public class LocationReceiver extends BroadcastReceiver {
    private static final String TAG = LocationReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        if (powerManager == null){
            Log.e(TAG, "PowerManger is null");
            return;
        }

        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,TAG + ":LocationReceiverWakelockTag");

        long timeout = 1000 * 60 * 2;
        wakeLock.acquire(timeout);
        try {
            Util.createLocationUpdateAlarm(context);

            Util.updateGPSLocation(context);
        }catch (Exception ex){
            Log.e(TAG, ex.getLocalizedMessage());

        }finally {
            wakeLock.release();
        }
    }
}
