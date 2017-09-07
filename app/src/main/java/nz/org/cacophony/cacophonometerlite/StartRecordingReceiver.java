package nz.org.cacophony.cacophonometerlite;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
//import android.util.Log;

import org.slf4j.Logger;

import static com.loopj.android.http.AsyncHttpClient.LOG_TAG;
import static nz.org.cacophony.cacophonometerlite.Util.getBatteryLevelByIntent;

public class StartRecordingReceiver extends BroadcastReceiver{
    public static final int RECORDING_STARTED = 1;
    public static final int RECORDING_FAILED = 2;
    public static final int RECORDING_FINISHED = 3;
    public static final int NO_PERMISSIONS_TO_RECORD = 4;
    public static final int UPLOADING_FINISHED = 5;
    private static final String TAG = StartRecordingReceiver.class.getName();
//    private static Logger logger = null;

    private Context context = null;
    // Handler to pass to recorder.
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {

            if (context == null) {
                Log.e(TAG, "Context was null for handler.");
//                logger.error("Context was null for handler.");

            }
            switch (inputMessage.what) {
                case RECORDING_STARTED:

                    Util.getToast(context,"Recording started", false ).show();
                    break;
                case RECORDING_FAILED:

                    Util.getToast(context,"Recording failed", true ).show();
                    enableButtons(context);
                    break;
                case RECORDING_FINISHED:

                    Util.getToast(context,"Recording has finished. Now uploading it to server - please wait", false ).show();
                    break;
                case UPLOADING_FINISHED:

                    Util.getToast(context,"Recording has been uploaded to the server - all done", false ).show();
                    enableButtons(context);
                    break;
                case NO_PERMISSIONS_TO_RECORD:

                    Util.getToast(context,"Did not have proper permissions to record", true ).show();
                    enableButtons(context);
                    break;
                default:
                    Log.w(TAG, "Unknown handler what.");
//                    Util.writeLocalLogEntryUsingLogback(context, LOG_TAG, "Unknown handler what.");
//                    logger.error("Unknown handler what.");
                    enableButtons(context);
                    break;
            }
        }
    };

    @Override
    public void onReceive(final Context context, Intent intent) {

        this.context = context;
//        logger = Util.getAndConfigureLogger(context, LOG_TAG);
//        logger.info("StartRecordingReceiver onReceive" );
        if (!Util.checkPermissionsForRecording(context)) {

            Util.getToast(context,"Do not have proper permissions to record", true ).show();

//            Util.writeLocalLogEntryUsingLogback(context, LOG_TAG, "Don't have proper permissions to record");
            Log.e(TAG, "Don't have proper permissions to record");
//            logger.error("Don't have proper permissions to record");
            return;
        }
        Prefs prefs = new Prefs(context);

        // need to determine the source of the intent ie Main UI or boot receiver
        Bundle bundle = intent.getExtras();
        String alarmIntentType = bundle.getString("type");


        if (alarmIntentType == null){
            Log.e(TAG, "Intent does not have a type");
//            Util.writeLocalLogEntryUsingLogback(context, LOG_TAG, "Intent does not have a type");
//            logger.error("Intent does not have a type");
            alarmIntentType = "unknown"; // shouldn't get here
        }



        // First check to see if battery level is sufficient to continue.


        double batteryLevel = Util.getBatteryLevelUsingSystemFile(context);
        if (batteryLevel != -1){ // looks like getting battery level using system file worked
            String batteryStatus = Util.getBatteryStatus(context);
            prefs.setBatteryLevel(batteryLevel); // had to put it into prefs as I could not ready battery level from UploadFiles class (looper error)
            if (batteryStatus.equalsIgnoreCase("FULL")) {
                // The current battery level must be the maximum it can be!
                prefs.setMaximumBatteryLevel(batteryLevel);
            }


            double batteryRatioLevel = batteryLevel / prefs.getMaximumBatteryLevel();
            double batteryPercent = batteryRatioLevel * 100;
            if (!enoughBatteryToContinue( batteryPercent, alarmIntentType)){
                Log.w(TAG, "Battery level too low to do a recording");
//                Util.writeLocalLogEntryUsingLogback(context, LOG_TAG, "Battery level too low to do a recording");
//                logger.warn("Battery level too low to do a recording");
                return;
            }


        }else { // will need to get battery level using intent method
            double batteryPercentLevel = getBatteryLevelByIntent(context);

            if (!enoughBatteryToContinue( batteryPercentLevel, alarmIntentType)){
                Log.w(TAG, "Battery level too low to do a recording");
//                Util.writeLocalLogEntryUsingLogback(context, LOG_TAG, "Battery level too low to do a recording");
//                logger.warn("Battery level too low to do a recording");
                return;
            }
        }


        // need to determine the source of the intent ie Main UI or boot receiver

        if (alarmIntentType.equalsIgnoreCase("testButton")){
            try {
                // Start recording in new thread.

                Thread thread = new Thread() {
                    @Override
                    public void run() {
//                        Looper.prepare();
                        MainThread mainThread = new MainThread(context, handler);
                        mainThread.run();
                    }
                };
                thread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }else{ // intent came from boot receiver or app (not test record)

            Intent mainServiceIntent = new Intent(context, MainService.class);
            try {
                mainServiceIntent.putExtra("type",alarmIntentType);

            }catch (Exception e){
                Log.e(TAG, "Error setting up intent");
//                Util.writeLocalLogEntryUsingLogback(context, LOG_TAG, "Error setting up intent");
//                logger.warn("Error setting up intent");
            }
            context.startService(mainServiceIntent);


        }



    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean enoughBatteryToContinue(double batteryPercent, String alarmType){
        // The battery level required to continue depends on the type of alarm

        if (alarmType.equalsIgnoreCase("testButton")){
            // Test button was pressed
            return true;
        }

        if (alarmType.equalsIgnoreCase("repeating")){
          //  return batteryPercent > 75;
            return batteryPercent > 85;
        }else { // must be a dawn or dusk alarm
            //return batteryPercent > 50;
            return batteryPercent > 75;
        }

    }

     static void enableButtons(Context context){
         Util.sendMainActivityAMessage(context, "enable_vitals_button");
         Util.sendMainActivityAMessage(context, "enable_test_recording_button");
         Util.sendMainActivityAMessage(context, "enable_setup_button");

          }


}