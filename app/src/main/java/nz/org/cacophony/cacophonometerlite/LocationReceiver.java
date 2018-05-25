package nz.org.cacophony.cacophonometerlite;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import static android.content.Context.POWER_SERVICE;

/**
 * Created by Tim Hunt on 23-Apr-18.
 */

public class LocationReceiver extends BroadcastReceiver {
    private static final String TAG = LocationReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);

        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "LocationReceiverWakelockTag");
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
