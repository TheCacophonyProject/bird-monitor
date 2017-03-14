package nz.org.cacophonoy.cacophonometerlite;



import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import static android.R.attr.delay;
import static android.content.Context.ALARM_SERVICE;


public class BootReceiver extends BroadcastReceiver {
    static final String LOG_TAG = "BootReceiver";

    @Override
    public void onReceive(final Context context, Intent intent)
    {
//        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 30);
//        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
//        Log.d(LOG_TAG, "onReceive: Running boot code...");
//        Toast.makeText(context, "Cacophony BootReceiver started.", Toast.LENGTH_SHORT).show();
//        if (!Util.checkPermissionsForRecording(context)) {
//            Toast.makeText(context, "App doesn't have proper permissions to record.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        Thread t = new Thread() {
//            @Override
//            public void run() {
//                Server.updateServerConnectionStatus(context);
//            }
//        };
//        t.start();

        Intent myIntent = new Intent(context, StartRecordingReceiver.class);
        try {
            myIntent.putExtra("type","repeating");
          // System.out.println("intent type " + intent.getExtras().getString("type"));
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME);
            toneG.startTone(ToneGenerator.TONE_CDMA_ANSWER, 1000);
        }catch (Exception e){
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME);
            toneG.startTone(ToneGenerator.TONE_DTMF_0, 1000);
           // System.out.println("intent type " + intent.getExtras().getString("type"));
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent,0);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(ALARM_SERVICE);
//        long delay = 60 * 1000 * 5; // 5 minutes
       // long delay = 60 * 1000 ; // 1 minutes
        Prefs prefs = new Prefs(context);
//        prefs.setRecordingDurationSeconds();
//        prefs.setTimeBetweenRecordingsSeconds();
//        prefs.setDawnDuskOffsetLargeSeconds();
//        prefs.setDawnDuskOffsetSmallSeconds();
//        prefs.setDawnDuskOffsetLargeSeconds();
//        prefs.setLengthOfTwilightSeconds();
//
//        // determine if there is a sim card - need to disable airplane mode to determine
//        Util.disableAirplaneMode(context);
//        boolean isSimCardDetected = Util.isSimPresent(context);
//        prefs.setSimCardDetected(isSimCardDetected);

        long timeBetweenRecordingsSeconds = (long)prefs.getTimeBetweenRecordingsSeconds();
        long delay = 1000 * timeBetweenRecordingsSeconds ;
//        long delay = 1000 *60;
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() ,
                delay, pendingIntent);
    }

}
