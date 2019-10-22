package nz.org.cacophony.birdmonitor;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;

import com.crashlytics.android.Crashlytics;

import java.util.Date;


/**
 * Recordings need to happen from a service so needed this class
 */

public class MainService extends IntentService {
    private static final String TAG = MainService.class.getName();

    public MainService() {
        super("MainService");
    }

    private static boolean checkForUpdates(Context context) {
        Prefs prefs = new Prefs(context);
        if (prefs.getAutoUpdate()) {
            long lastUpdate = prefs.getDateTimeLastUpdateCheck();
            long now = new Date().getTime();
            if ((now - lastUpdate) > Prefs.TIME_BETEWEEN_UPDATES_MS) {
                Util.disableFlightMode(context);
                prefs.setDateTimeLastUpdateCheck(now);
                prefs.setFlightModePending(prefs.getAeroplaneMode());
                if (Util.waitForNetworkConnection(context,true)) {
                    return UpdateUtil.updateIfAvailable(context);
                }
            }
        }
        return false;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Prefs prefs = new Prefs(getApplicationContext());
        Crashlytics.setUserEmail(prefs.getEmailAddress());
        Crashlytics.setUserName(prefs.getUsername());
        Crashlytics.setUserIdentifier(String.format("%s-%s-%d", prefs.getGroupName(), prefs.getDeviceName(), prefs.getDeviceId()));

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager == null) {
            Log.e(TAG, "PowerManger is null");
            return;
        }


        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "Cacophonometer:MainServiceWakelockTag");

        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
        boolean updating = false;
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
                updating = checkForUpdates(getApplicationContext());
            } else {
                Log.e(TAG, "MainService bundle is null");
            }
        } finally {
            if (updating == false) {
                Util.enableFlightMode(getApplicationContext());
            }
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }
}
