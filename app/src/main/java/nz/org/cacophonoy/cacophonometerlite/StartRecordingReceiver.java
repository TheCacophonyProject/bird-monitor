package nz.org.cacophonoy.cacophonometerlite;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import static nz.org.cacophonoy.cacophonometerlite.Util.getBatteryLevelByIntent;

public class StartRecordingReceiver extends BroadcastReceiver {
    public static final int RECORDING_STARTED = 1;
    public static final int RECORDING_FAILED = 2;
    public static final int RECORDING_FINISHED = 3;
    public static final int NO_PERMISSIONS_TO_RECORD = 4;
    private static final String LOG_TAG = StartRecordingReceiver.class.getName();
    public static String intentTimeUriMessage = null;

    Context context = null;
    // Handler to pass to recorder.
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {
            Log.d(LOG_TAG, "StartRecordingReceiver handler.");
            if (context == null) {
                Log.e(LOG_TAG, "Context was null for handler.");
            }
            switch (inputMessage.what) {
                case RECORDING_STARTED:
                    Toast.makeText(context, "Recording started.", Toast.LENGTH_SHORT).show();
                    break;
                case RECORDING_FAILED:
                    Toast.makeText(context, "Recording failed.", Toast.LENGTH_SHORT).show();
                    break;
                case RECORDING_FINISHED:
                    Toast.makeText(context, "Recording finished", Toast.LENGTH_SHORT).show();
                    break;
                case NO_PERMISSIONS_TO_RECORD:
                    Toast.makeText(context, "Did not have proper permissions to record.", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Log.w(LOG_TAG, "Unknown handler what.");
                    break;
            }
        }
    };

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i(LOG_TAG, "Called receiver method");


        this.context = context;
        if (!Util.checkPermissionsForRecording(context)) {
            Toast.makeText(context, "Don't have proper permissions to record..", Toast.LENGTH_SHORT).show();
            return;
        }
        Prefs prefs = new Prefs(context);

        if (intent.getExtras() != null) { // will be null if gets here due to pressing 'Start Test Recording
            try {
                String alarmIntentType = intent.getExtras().getString("type");
                if (alarmIntentType != null) { // will be null if
                    if (alarmIntentType.equalsIgnoreCase("repeating")) {
                        DawnDuskAlarms.configureDawnAlarms(context);
                        DawnDuskAlarms.configureDuskAlarms(context);
                    } else if (alarmIntentType.equalsIgnoreCase("dawn") || alarmIntentType.equalsIgnoreCase("dusk")) {
                        intentTimeUriMessage = intent.getDataString();
                    }
                }

            } catch (Exception e) {
                Log.e(LOG_TAG, "Error setting up dawn and dusk alarms");
            }
        }

        // First check to see if battery level is greater than 50% - Abort if it isnt

        double batteryLevel = Util.getBatteryLevelUsingSystemFile(context);
        if (batteryLevel != -1){ // looks like getting battery level using system file worked
            String batteryStatus = Util.getBatteryStatus(context);
            prefs.setBatteryLevel(batteryLevel); // had to put it into prefs as I could not ready battery level from UploadFiles class (looper error)
            if (batteryStatus.equalsIgnoreCase("FULL")) {
                // The current battery level must be the maximum it can be!
                prefs.setMaximumBatteryLevel(batteryLevel);
            }


            double batteryRatioLevel = batteryLevel / prefs.getMaximumBatteryLevel();
            if (batteryRatioLevel < 0.5) {
                return;
            }
        }else { // will need to get battery level using intent method
            double batteryPercentLevel = getBatteryLevelByIntent(context);
            if (batteryPercentLevel < 50) {
                return;
            }

        }



        try {
            // Start recording in new thread.
            Thread thread = new Thread() {
                @Override
                public void run() {
                    Log.i(LOG_TAG, "Thread thread = new Thread() {");
                    Record record = new Record(context, handler);
                    record.run();
                }
            };
            thread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}