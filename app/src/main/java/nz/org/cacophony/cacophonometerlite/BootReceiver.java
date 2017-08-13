package nz.org.cacophony.cacophonometerlite;



import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.net.Uri;

import android.os.SystemClock;
//import android.util.Log;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.android.BasicLogcatConfigurator;

import static android.content.Context.ALARM_SERVICE;



public class BootReceiver extends BroadcastReceiver {
    // BootReceiver runs when phone is restarted. It does the job that the MainActivity would have done of setting up the repeating alarm
    // This means the recordings will be made without having to reopen the application
    // Note: If you need to change any settings, then you will need to open the application which will save those settings
    // in the apps shared preferences (file on phone) that are accessed via the Prefs class.
    private static final String LOG_TAG = "BootReceiver";
    private static Logger logger = null;
//    static {
//        BasicLogcatConfigurator.configureDefaultContext();
//    }

    @Override
    public void onReceive(final Context context, Intent intent)
    {
//        Util.configureLogbackDirectly(context);
//        Logger logger = LoggerFactory.getLogger(LOG_TAG);
//        for (int i = 0; i < 10; i++) {
//            logger.info("BootReceiver onCreate");
//        }
        logger = Util.getAndConfigureLogger(context, LOG_TAG);
        logger.info("BootReceiver onReceive" );
        String intentAction = intent.getAction();

        Intent myIntent = new Intent(context, StartRecordingReceiver.class);

        try {
            myIntent.putExtra("type","repeating");
            Uri timeUri; // // this will hopefully allow matching of intents so when adding a new one with new time it will replace this one
            timeUri = Uri.parse("normal"); // cf dawn dusk offsets created in DawnDuskAlarms
            myIntent.setData(timeUri);

        }catch (Exception ex){

//            Log.e(LOG_TAG, ex.getLocalizedMessage());
//            Util.writeLocalLogEntryUsingLogback(context, LOG_TAG, ex.getLocalizedMessage());
            logger.error(ex.getLocalizedMessage());
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent,0);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(ALARM_SERVICE);
        Prefs prefs = new Prefs(context);


        long timeBetweenRecordingsSeconds = (long)prefs.getTimeBetweenRecordingsSeconds();
        long delay = 1000 * timeBetweenRecordingsSeconds ;
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() ,
                delay, pendingIntent);

    }

}
