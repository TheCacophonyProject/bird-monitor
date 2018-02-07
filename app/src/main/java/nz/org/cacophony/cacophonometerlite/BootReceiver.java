package nz.org.cacophony.cacophonometerlite;



import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.net.Uri;

import android.os.SystemClock;
import android.util.Log;
//import android.util.Log;


//import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.android.BasicLogcatConfigurator;

import static android.content.Context.ALARM_SERVICE;
//import static com.loopj.android.http.AsyncHttpClient.LOG_TAG;
//import static com.loopj.android.http.AsyncHttpClient.LOG_TAG;


public class BootReceiver extends BroadcastReceiver {
    // BootReceiver runs when phone is restarted. It does the job that the MainActivity would have done of setting up the repeating alarm
    // This means the recordings will be made without having to reopen the application
    // Note: If you need to change any settings, then you will need to open the application which will save those settings
    // in the apps shared preferences (file on phone) that are accessed via the Prefs class.
   // private static final String TAG = "BootReceiver";
    private static final String TAG = BootReceiver.class.getName();
  //  private static Logger logger = null;
//    static {
//        BasicLogcatConfigurator.configureDefaultContext();
//    }

    @Override
    public void onReceive(final Context context, Intent intent)
    {


        Util.enableFlightMode(context);

     //   Util.createAlarms(context, "repeating", "normal", "BootReceiver");
        Util.createAlarms(context, "repeating", "normal");
       DawnDuskAlarms.configureDawnAndDuskAlarms(context, true);



    }

}
