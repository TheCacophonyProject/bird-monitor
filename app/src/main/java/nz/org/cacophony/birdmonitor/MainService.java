package nz.org.cacophony.birdmonitor;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;

import androidx.annotation.Nullable;

import android.util.Log;

import java.util.Date;


/**
 * Recordings need to happen from a service so needed this class
 */

public class MainService extends IntentService {
    private static final String TAG = MainService.class.getName();


    public MainService() {
        super("MainService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager == null) {
            Log.e(TAG, "PowerManger is null");
            return;
        }
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "Cacophonometer:MainServiceWakelockTag");
        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
        boolean updating =false;
        try {
            Bundle bundle = intent != null ? intent.getExtras() : null;
            if (bundle != null) {
                String alarmIntentType = bundle.getString("type");
                if (alarmIntentType == null) {
                    alarmIntentType = "unknown";
                    Log.w(TAG, "alarmIntentType = unknown");
                }
                RecordAndUpload.doRecord(getApplicationContext(), alarmIntentType);
                updating = checkForUpdates(getApplicationContext());
            } else {
                Log.e(TAG, "MainService bundle is null");
            }
        }finally {
            if(updating==false) {
                Util.enableFlightMode(getApplicationContext());
            }
            wakeLock.release();
        }
    }


    private static boolean checkForUpdates(Context context){
        Prefs prefs = new Prefs(context);
        if(prefs.getAutoUpdate()) {
            long lastUpdate = prefs.getDateTimeLastUpdateCheck();
            long now = new Date().getTime();
            if ((now - lastUpdate) > Prefs.TIME_BETEWEEN_UPDATES_MS) {
                Util.disableFlightMode(context);
                prefs.setDateTimeLastUpdateCheck(now);
                prefs.setFlightModePending(prefs.getAeroplaneMode());
                return Util.updateIfAvailable(context);
            }
        }
        return false;
    }
}
