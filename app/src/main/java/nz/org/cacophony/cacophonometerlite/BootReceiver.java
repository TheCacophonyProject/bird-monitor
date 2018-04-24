package nz.org.cacophony.cacophonometerlite;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.PowerManager;
import android.util.Log;

import static android.content.Context.POWER_SERVICE;

//import android.util.Log;
//import org.slf4j.Logger;
//import static com.loopj.android.http.AsyncHttpClient.LOG_TAG;
//import static com.loopj.android.http.AsyncHttpClient.LOG_TAG;


public class BootReceiver extends BroadcastReceiver {
    // BootReceiver runs when phone is restarted. It does the job that the MainActivity would have done of setting up the repeating alarm
    // This means the recordings will be made without having to reopen the application
    // Note: If you need to change any settings, then you will need to open the application which will save those settings
    // in the apps shared preferences (file on phone) that are accessed via the Prefs class.
   // private static final String TAG = "BootReceiver";
    private static final String TAG = BootReceiver.class.getName();

    @Override
    public void onReceive(final Context context, Intent intent)
    {
        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGen1.startTone(ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_NORMAL, 1000);


        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);

        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "BootReceiverWakelockTag");
        long timeout = 1000 * 60 * 2;  // give the boot stuff two minutes to run - but the enable flight mode does not seem to be working (however long I wait).
        wakeLock.acquire(timeout); // finally never seems to run which is why I used a timeout on the wakelock creaation
try {

    Util.createAlarms(context, "repeating", "normal");

    DawnDuskAlarms.configureDawnAndDuskAlarms(context, true);
    Util.createCreateAlarms(context);
    Util.setUpLocationUpdateAlarm(context);

    // make a sound to show that alarms have been created.
    //toneGen1.startTone(ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_NORMAL, 1000);

    Util.enableFlightMode(context);

}catch (Exception ex){
    toneGen1.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 5000);
    try {
        Thread.sleep(3000);
    } catch (Exception e) {
        Log.e(TAG, ex.getLocalizedMessage());
    }
}finally{
    // finally never seems to run which is why I used a timeout on the wakelock creation

    // This tone is for testing - to see if it ever runs - need to get back to this sometime
    toneGen1.startTone(ToneGenerator.TONE_CDMA_NETWORK_BUSY, 4000);
    try {
        Thread.sleep(4000);
    } catch (Exception ex) {
        Log.e(TAG, ex.getLocalizedMessage());
    }
    wakeLock.release();
}


    }

}
