package nz.org.cacophony.birdmonitor;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;

import static android.content.Context.POWER_SERVICE;
import static nz.org.cacophony.birdmonitor.Util.getBatteryLevelByIntent;
import static nz.org.cacophony.birdmonitor.views.ManageRecordingsFragment.MANAGE_RECORDINGS_ACTION;
import static nz.org.cacophony.birdmonitor.views.ManageRecordingsFragment.MessageType.NO_PERMISSION_TO_RECORD;
import static nz.org.cacophony.birdmonitor.views.ManageRecordingsFragment.MessageType.RECORDING_DISABLED;

/**
 * This class receives the intents that indicate that a recording is required to be made.  Before a
 * recording is initiated a number of 'house keeping' tasks/checks are made including creating the
 * next alarms and checking the phone has enough power to proceed.
 * <p>
 * Depending on how a recording request is made (ie from Android alarm or the 'Record Now' button)
 * either a service or thread is used to proceed to making the actual recording - this was imposed
 * by the Android OS.
 */

public class StartRecordingReceiver extends BroadcastReceiver {

    private static final String TAG = StartRecordingReceiver.class.getName();

    @Override
    public void onReceive(final Context context, Intent intent) {
        Prefs prefs = new Prefs(context);
        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        if (powerManager == null) {
            Log.e(TAG, "PowerManger is null");
            Crashlytics.logException(new Throwable("PowerManger is null"));
            return;
        }
        PowerManager.WakeLock wakeLock = null;

        try {
            Util.createTheNextSingleStandardAlarm(context, intent.getStringExtra(Prefs.OFFSET));
            // need to determine the source of the intent ie Main UI or boot receiver
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                Log.e(TAG, "bundle is null");
                Crashlytics.logException(new Throwable("Bundle is null"));
                return;
            }
            final String alarmIntentType = bundle.getString("type");
            if (alarmIntentType == null) {
                Log.e(TAG, "Intent does not have a type");
                Crashlytics.logException(new Throwable("No Intent Type"));
                return;
            } else if (alarmIntentType == Prefs.FAIL_SAFE_ALARM) {
                return;
            }

            // A wake lock stops the Android OS trying to save power by stopping a process.
            // A 10 minute wait lock has been working well, but with the addition of the
            // Bird Count feature with a 10 and 15 minute duration, I've increased the duration of
            // the wake locks.
            //
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "Cacophonometer:StartRecordingReceiverWakelockTag");

            long wakeLockDuration = 10 * 60 * 1000L; /*10 minutes*/
            boolean recordButtonWasPressed = false;

            if (alarmIntentType.equalsIgnoreCase(Prefs.RECORD_NOW_ALARM)) {
                recordButtonWasPressed = true;
            } else if (alarmIntentType.equalsIgnoreCase(Prefs.BIRD_COUNT_5_ALARM)) {
                recordButtonWasPressed = true;
            } else if (alarmIntentType.equalsIgnoreCase(Prefs.BIRD_COUNT_10_ALARM)) {
                recordButtonWasPressed = true;
                wakeLockDuration = 12 * 60 * 1000L; // 10 minutes for recording plus margin of error/uploading
            } else if (alarmIntentType.equalsIgnoreCase(Prefs.BIRD_COUNT_15_ALARM)) {
                recordButtonWasPressed = true;
                wakeLockDuration = 17 * 60 * 1000L; // 15 minutes for recording plus margin of error/uploading
            } else if (alarmIntentType.equalsIgnoreCase(Prefs.REPEATING_ALARM) && prefs.getUseSunAlarms()) {
                wakeLockDuration += prefs.getRecLength() * 60 * 1000;
            }

            wakeLock.acquire(wakeLockDuration);


            if (prefs.getAutomaticRecordingsDisabled() && !recordButtonWasPressed) {
                String messageToDisplay = "Recording is currently disabled on this phone";
                MessageHelper.broadcastMessage(messageToDisplay, RECORDING_DISABLED, MANAGE_RECORDINGS_ACTION, context);
                return;  // Don't do anything else if Turn Off has been enabled. (Very Important that next alarm has been created)
            }

            if (!Util.checkPermissionsForRecording(context)) {
                Log.e(TAG, "Don't have proper permissions to record");
                Crashlytics.logException(new Throwable("Inccorect Permissions"));

                // Need to enable record button
                String messageToDisplay = "No permission to record";
                MessageHelper.broadcastMessage(messageToDisplay, NO_PERMISSION_TO_RECORD, MANAGE_RECORDINGS_ACTION, context);
                return;
            }

            // First check to see if battery level is sufficient to continue.

            double batteryLevel = Util.getBatteryLevelUsingSystemFile();
            if (batteryLevel != -1) { // looks like getting battery level using system file worked
                String batteryStatus = Util.getBatteryStatus(context);
                prefs.setBatteryLevel(batteryLevel); // had to put it into prefs as I could not ready battery level from UploadFiles class (looper error)
                if (batteryStatus.equalsIgnoreCase("FULL")) {
                    // The current battery level must be the maximum it can be!
                    prefs.setMaximumBatteryLevel(batteryLevel);
                }

                double batteryRatioLevel = batteryLevel / prefs.getMaximumBatteryLevel();
                double batteryPercent = batteryRatioLevel * 100;
                if (!enoughBatteryToContinue(batteryPercent, alarmIntentType, prefs)) {
                    Log.w(TAG, "Battery level too low to do a recording");
                    return;
                }

            } else { // will need to get battery level using intent method
                double batteryPercentLevel = getBatteryLevelByIntent(context);

                if (!enoughBatteryToContinue(batteryPercentLevel, alarmIntentType, prefs)) {
                    Log.w(TAG, "Battery level too low to do a recording");
                    return;
                }
            }

            // need to determine the source of the intent ie Main UI or boot receiver
            if (Util.isUIRecording(alarmIntentType)) {
                try {
                    // Start recording in new thread.

                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            MainThread mainThread = new MainThread(context, alarmIntentType);
                            mainThread.run();
                        }
                    };
                    thread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else { // intent came from boot receiver or app (not test record, or bird count )
                Intent mainServiceIntent = new Intent(context, MainService.class);
                mainServiceIntent.putExtra("type", alarmIntentType);
                context.startService(mainServiceIntent);
            }

        } catch (Exception ex) {
            Crashlytics.logException(ex);
            Log.e(TAG, ex.getLocalizedMessage(), ex);

        } finally {
            // https://stackoverflow.com/questions/12140844/java-lang-runtimeexception-wakelock-under-locked-c2dm-lib
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
        }

    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean enoughBatteryToContinue(double batteryPercent, String alarmType, Prefs prefs) {
        // The battery level required to continue depends on the type of alarm

        if (alarmType.equalsIgnoreCase(Prefs.RECORD_NOW_ALARM)) {
            // record now button was pressed
            return true;
        }

        if (prefs.getIgnoreLowBattery()) {
            return true;
        }

        if (alarmType.equalsIgnoreCase(prefs.REPEATING_ALARM)) {

            return batteryPercent > prefs.getBatteryLevelCutoffRepeatingRecordings();
        } else { // must be a dawn or dusk alarm

            return batteryPercent > prefs.getBatteryLevelCutoffDawnDuskRecordings();
        }
    }

}