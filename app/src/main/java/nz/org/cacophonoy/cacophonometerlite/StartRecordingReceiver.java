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

    Context context = null;

    @Override
    public void onReceive(final Context context, Intent intent) {
        // no point doing a recording if not logged in to server
        if (!Server.loggedIn){
            return;
        }
        if (intent.getExtras() != null) { // will be null if gets here due to pressing 'Start Test Recording
            try {


                System.out.println("intent type " + intent.getExtras().getString("type"));
                String alarmIntentType = intent.getExtras().getString("type");
//                if (alarmIntentType != null){ // will be null if
                    if (alarmIntentType.equalsIgnoreCase("repeating")) {
                        DawnDuskAlarms.configureDawnAlarms(context);
                        DawnDuskAlarms.configureDuskAlarms(context);
                    }
                System.out.println("intent timeUri " + intent.getDataString());

//                }

            } catch (Exception e) {
                System.out.println("b0000000000000000000000");
                System.out.println(e.getLocalizedMessage());
            }
            // First check to see if battery level is greater than 70% - Abort if it isnt
        }
        if (Util.getBatteryLevel(context) < 0.5){
            return;
        }
        Log.i(LOG_TAG, "Called receiver method");
        this.context = context;
        if (!Util.checkPermissionsForRecording(context)) {
            Toast.makeText(context, "Don't have proper permissions to record..", Toast.LENGTH_SHORT).show();
            return;
        }
        /*
        if (!Server.loggedIn) {
            Log.i(LOG_TAG, "Won't start recording as device not logged in.");
            Toast.makeText(context, "Wont record as device not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }
        */

        try{
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 30);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);

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