package nz.org.cacophonoy.cacophonometerlite;



import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import static android.R.attr.delay;
import static android.content.Context.ALARM_SERVICE;
import static android.os.Build.VERSION_CODES.ECLAIR;


public class BootReceiver extends BroadcastReceiver {
    // BootReceiver runs when phone is restarted. It does the job that the MainActivity would have done of setting up the repeating alarm
    // This means the recordings will be made without having to reopen the application
    // Note: If you need to change any settings, then you will need to open the application which will save those settings
    // in the apps shared preferences (file on phone) that are accessed via the Prefs class.
    static final String LOG_TAG = "BootReceiver";

    @Override
    public void onReceive(final Context context, Intent intent)
    {
        //   Server.disableDataConnection(context);

//        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME);
//        toneG.startTone(ToneGenerator.TONE_DTMF_0, 1000);

        Intent myIntent = new Intent(context, StartRecordingReceiver.class);
        try {
            myIntent.putExtra("type","repeating");

        }catch (Exception e){
            // Sound alarm if problem
            ToneGenerator toneG2 = new ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME);
            if (Build.VERSION.SDK_INT >= ECLAIR) {
                toneG2.startTone(ToneGenerator.TONE_DTMF_0, 10000);
            }
            Log.i(LOG_TAG, e.getLocalizedMessage());
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent,0);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(ALARM_SERVICE);
        Prefs prefs = new Prefs(context);


        long timeBetweenRecordingsSeconds = (long)prefs.getTimeBetweenRecordingsSeconds();
        long delay = 1000 * timeBetweenRecordingsSeconds ;
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() ,
                delay, pendingIntent);

        // Do a test record when first boots
//        Intent runOnceOnBootIntent = new Intent(context, StartRecordingReceiver.class);
//        try {
//            Log.e(LOG_TAG, "About to broadcast runOnceOnBootIntent");
//            runOnceOnBootIntent.putExtra("type","runOnceOnBoot");
//            context.sendBroadcast(runOnceOnBootIntent);
//
//        }catch (Exception e){
//
//        }


    }

}
