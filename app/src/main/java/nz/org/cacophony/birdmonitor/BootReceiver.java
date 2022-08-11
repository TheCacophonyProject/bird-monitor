package nz.org.cacophony.birdmonitor;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import static android.content.Context.POWER_SERVICE;


/**
 * BootReceiver runs when phone is restarted. It does the job that the MainActivity would have done of setting up the repeating alarm.
 * This means the recordings will be made without having to reopen the application.
 * If the user needs to change any settings, then they will need to open the application which will save those settings
 * in the apps shared preferences (file on phone) that are accessed via the Prefs class.
 */
public class BootReceiver extends BroadcastReceiver {


    private static final String TAG = BootReceiver.class.getName();

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction() == null) {
            Log.e(TAG, "intent.getAction() is null");
            return;
        }
        if (!intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) && !intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
            return;
        }

        try {
            final Thread thread = new Thread() {
                @Override
                public void run() {

                    PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
                    if (powerManager == null) {
                        Log.e(TAG, "powerManager is null");
                        return;
                    }

                    final PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                            "Cacophonometer:BootReceiverWakelockTag");
                    long timeout = 1000 * 60 * 2;  // give the boot stuff two minutes to run - but the enable flight mode does not seem to be working (however long I wait).
                    wakeLock.acquire(timeout); // finally never seems to run which is why I used a timeout on the wakelock creation
                    try {
                        Util.createTheNextSingleStandardAlarm(context, null, intent.getAction());
                        Util.enableFlightMode(context);
                        if (intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
                            Prefs prefs = new Prefs(context);
                            if (prefs.getRelaunchOnUpdate()) {
                                prefs.setRelaunchOnUpdate(false);
                                Util.relaunch(context);
                            }
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "Error setting boot alarms");
                    } finally {
                        wakeLock.release();
                    }
                }
            };
            thread.start();

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage(), ex);
        }


    }

}
