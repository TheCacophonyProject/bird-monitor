package nz.org.cacophonoy.cacophonometerlite;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class StartRecordingReceiver extends BroadcastReceiver
{
    private static final String LOG_TAG = StartRecordingReceiver.class.getName();

    public static final int RECORDING_STARTED = 1;
    public static final int RECORDING_FAILED = 2;
    public static final int RECORDING_FINISHED = 3;
    public static final int NO_PERMISSIONS_TO_RECORD = 4;
    public static  String intentTimeUriMessage = null;

    Context context = null;

    @Override
    public void onReceive(final Context context, Intent intent) {
//        // no point doing a recording if not logged in to server
//        if (!Server.loggedIn){
//            return;
//        }

                Prefs prefs = new Prefs(context);

        // determine if there is a sim card - need to disable airplane mode to determine
        Util.disableAirplaneMode(context);
        boolean isSimCardDetected = Util.isSimPresent(context);
        prefs.setSimCardDetected(isSimCardDetected);





        if (intent.getExtras() != null) { // will be null if gets here due to pressing 'Start Test Recording
            try {
//
//
//                System.out.println("intent type " + intent.getExtras().getString("type"));
                String alarmIntentType = intent.getExtras().getString("type");
                if (alarmIntentType != null){ // will be null if
                    if (alarmIntentType.equalsIgnoreCase("repeating")) {
                        DawnDuskAlarms.configureDawnAlarms(context);
                        DawnDuskAlarms.configureDuskAlarms(context);
                    }else if (alarmIntentType.equalsIgnoreCase("dawn") || alarmIntentType.equalsIgnoreCase("dusk")){
                        intentTimeUriMessage = intent.getDataString();
                      //  System.out.println("intent timeUri " + intentTimeUriMessage);
                    }


                }
//
            } catch (Exception e) {
//                System.out.println("b0000000000000000000000");
//                System.out.println(e.getLocalizedMessage());
            }
//            // First check to see if battery level is greater than 70% - Abort if it isnt
        }


//        Prefs prefs = new Prefs(context);
        double batteryLevel = Util.getBatteryLevel(context);
        String  batteryStatus = Util.getBatteryStatus(context);
        prefs.setBatteryLevel(batteryLevel); // had to put it into prefs as I could not ready battery level from UploadFiles class (looper error)
        if (batteryStatus.equalsIgnoreCase("FULL")){
            // The current battery level must be the maximum it can be!
            prefs.setMaximumBatteryLevel(batteryLevel);
        }
         double batteryPercentLevel = batteryLevel/prefs.getMaximumBatteryLevel();
        if (batteryPercentLevel < 0.5){
            return;
        }

        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME);
        toneG.startTone(ToneGenerator.TONE_CDMA_DIAL_TONE_LITE, 5000);
        Log.i(LOG_TAG, "Called receiver method");
        this.context = context;
        if (!Util.checkPermissionsForRecording(context)) {
            Toast.makeText(context, "Don't have proper permissions to record..", Toast.LENGTH_SHORT).show();
            return;
        }
        /*
        if (!Server.loggedIn) {
            Log.i(LOG_TAG, "Won't start recording as device not logged in.");
//            Toast.makeText(context, "Wont record as device not logged in.", Toast.LENGTH_SHORT).show();
            ToneGenerator toneG2 = new ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME);
            toneG2.startTone(ToneGenerator.TONE_DTMF_8, 1000);
            return;
        }
//        */
//
        try{
//            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 30);
//            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);

            // Start recording in new thread.
            Thread thread = new Thread() {
                @Override
                public void run() {
                    Record record = new Record(context, handler);
                    record.run();
                }
            };
            thread.start();

        }catch(Exception e){
            e.printStackTrace();
        }

    }

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
}