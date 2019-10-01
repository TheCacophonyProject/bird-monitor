package nz.org.cacophony.birdmonitor;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import static nz.org.cacophony.birdmonitor.views.ManageRecordingsFragment.MANAGE_RECORDINGS_ACTION;
import static nz.org.cacophony.birdmonitor.views.ManageRecordingsFragment.MessageType.NO_PERMISSION_TO_RECORD;
import static nz.org.cacophony.birdmonitor.views.ManageRecordingsFragment.MessageType.RECORD_AND_UPLOAD_FAILED;

//import android.util.Log;

/**
 * Recordings made from the record now button needed to run in a thread
 */

class MainThread implements Runnable {

    private static final String TAG = MainThread.class.getName();


    private final Context context;

    private final String alarmIntentType;


    MainThread(Context context, String alarmIntentType) {
        this.context = context;
        this.alarmIntentType = alarmIntentType;

    }

    @Override
    public void run() {
        Looper.prepare();
        if (context == null) {
            Log.w(TAG, "Context was null.");
            return;
        }
        if (!Util.checkPermissionsForRecording(context)) {
            Log.e(TAG, "App does not have permission to record.");
            String messageToDisplay = "You do not have permission to record.";
            MessageHelper.broadcastMessage(messageToDisplay, NO_PERMISSION_TO_RECORD, MANAGE_RECORDINGS_ACTION, context);
            return;
        }

        try {
            RecordAndUpload.doRecord(context, alarmIntentType, null);
        } catch (Exception e) {
            Crashlytics.logException(e);
            Log.e(TAG, "Unknown error when recording", e);
            String messageToDisplay = "Unknown error: " + e.getLocalizedMessage();
            MessageHelper.broadcastMessage(messageToDisplay, RECORD_AND_UPLOAD_FAILED, MANAGE_RECORDINGS_ACTION, context);
            return;
        }

        Looper.loop();
    }
}
