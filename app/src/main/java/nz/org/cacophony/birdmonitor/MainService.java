package nz.org.cacophony.birdmonitor;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;


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

        try {
            Bundle bundle = intent != null ? intent.getExtras() : null;
            if (bundle != null) {
                String alarmIntentType = bundle.getString(Prefs.INTENT_TYPE);
                if (alarmIntentType == null) {
                    alarmIntentType = "unknown";
                    Log.w(TAG, "alarmIntentType = unknown");
                }
                String relativeTo = bundle.getString(Prefs.RELATIVE);
                long recordTimeSeconds = Util.getRecordingDuration(getApplicationContext(), alarmIntentType, relativeTo);
                wakeLock.acquire(recordTimeSeconds * 1000L /*10 minutes*/);

                RecordAndUpload.doRecord(getApplicationContext(), alarmIntentType, relativeTo);
            } else {
                Log.e(TAG, "MainService bundle is null");
            }
        } finally {
            Util.enableFlightMode(getApplicationContext());
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }
}
