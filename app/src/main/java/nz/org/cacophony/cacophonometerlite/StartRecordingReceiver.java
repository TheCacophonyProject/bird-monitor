package nz.org.cacophony.cacophonometerlite;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
//import android.util.Log;

import static nz.org.cacophony.cacophonometerlite.Util.getBatteryLevelByIntent;

public class StartRecordingReceiver extends BroadcastReceiver{
//    public static final int RECORDING_STARTED = 1;
//    public static final int RECORDING_FAILED = 2;
//    public static final int RECORDING_FINISHED = 3;
//    public static final int NO_PERMISSIONS_TO_RECORD = 4;
//    public static final int RECORDING_AND_UPLOADING_FINISHED = 5;
//    public static final int RECORDING_FINISHED_BUT_UPLOAD_FAILED = 6;
//    public static final int  RECORDING_FINISHED_NO_NETWORK = 7;
//    public static final int  RECORDING_FINISHED_WALK_MODE = 8;

    private static final String TAG = StartRecordingReceiver.class.getName();
//    private static Logger logger = null;

    private Context context = null;


    @Override
    public void onReceive(final Context context, Intent intent) {
        // First thing to do is to create the next repeating alarm as to avoid batching of alarms I now to to just creat the next one (can't create repeating)

//        Bundle b = intent.getExtras();
//        String j =(String) b.get("callingCode");
//        if (j!= null){
//            Log.e(TAG,j );
//        }else{
//            Log.e(TAG,intent.getDataString() );
//        }

//        Util.createAlarms(context, "repeating", "normal", "StartRecordingReceiverOnReceive");
        Util.createAlarms(context, "repeating", "normal");
        DawnDuskAlarms.configureDawnAndDuskAlarms(context, false);

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
       final String alarmIntentType = bundle.getString("type");


        if (alarmIntentType == null){
            Log.e(TAG, "Intent does not have a type");
//            Util.writeLocalLogEntryUsingLogback(context, LOG_TAG, "Intent does not have a type");
//            logger.error("Intent does not have a type");
        //    alarmIntentType = "unknown"; // shouldn't get here
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
            if (!enoughBatteryToContinue( batteryPercent, alarmIntentType, prefs)){
                Log.w(TAG, "Battery level too low to do a recording");
//                Util.writeLocalLogEntryUsingLogback(context, LOG_TAG, "Battery level too low to do a recording");
//                logger.warn("Battery level too low to do a recording");
                return;
            }


        }else { // will need to get battery level using intent method
            double batteryPercentLevel = getBatteryLevelByIntent(context);

            if (!enoughBatteryToContinue( batteryPercentLevel, alarmIntentType, prefs)){
                Log.w(TAG, "Battery level too low to do a recording");
//                Util.writeLocalLogEntryUsingLogback(context, LOG_TAG, "Battery level too low to do a recording");
//                logger.warn("Battery level too low to do a recording");
                return;
            }
        }



        String mode = prefs.getMode();
        switch(mode) {
            case "off":
                if (prefs.getAlwaysUpdateGPS()){
                    Util.updateGPSLocation(context);
                }

                break;
            case "normal":
                // Don't update location
                break;
            case "normalOnline":
                // Don't update location
                break;

            case "walking":
                // Not going to do dawn/dusk alarms if in walking mode
                if (alarmIntentType.equalsIgnoreCase("dawn") || alarmIntentType.equalsIgnoreCase("dusk")){
                    return; // exit onReceive method
                }

                Util.updateGPSLocation(context);

                break;
        }


        // need to determine the source of the intent ie Main UI or boot receiver

       // if (alarmIntentType.equalsIgnoreCase("testButton") || alarmIntentType.equalsIgnoreCase("recordNowButton")){
            if ( alarmIntentType.equalsIgnoreCase("recordNowButton")){
            try {
                // Start recording in new thread.

                Thread thread = new Thread() {
                    @Override
                    public void run() {
//                        Looper.prepare();
//                        MainThread mainThread = new MainThread(context, handler, alarmIntentType);
                        MainThread mainThread = new MainThread(context, alarmIntentType);
                        mainThread.run();
                    }
                };
                thread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }


        }else{ // intent came from boot receiver or app (not test record, or walk )

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
    private static boolean enoughBatteryToContinue(double batteryPercent, String alarmType, Prefs prefs){
        // The battery level required to continue depends on the type of alarm

        if (alarmType.equalsIgnoreCase("recordNowButton") ){
            // record now button was pressed
            return true;
        }


        String mode = prefs.getMode();
        switch(mode) { // mode determined earlier
            case "off":
                // has no affect on decision
                break;
            case "normal":
                // has no affect on decision
                break;
            case "normalOnline":
                // has no affect on decision
                break;

            case "walking":
                return true;  // ignore battery level when in walking mode

        }

        if (alarmType.equalsIgnoreCase("repeating")){

//            return batteryPercent > 85;
            return batteryPercent > prefs.getBatteryLevelCutoffRepeatingRecordings();
        }else { // must be a dawn or dusk alarm
           // return batteryPercent > 70;
            return batteryPercent > prefs.getBatteryLevelCutoffDawnDuskRecordings();
        }

    }

//     static void enableButtons(Context context){
//         Util.broadcastAMessage(context, "enable_vitals_button");
//         Util.broadcastAMessage(context, "enable_test_recording_button");
//         Util.broadcastAMessage(context, "enable_setup_button");
//
//          }




}