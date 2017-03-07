package nz.org.cacophonoy.cacophonometerlite;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by User on 07-Mar-17.
 */

public class DawnAlarms {
    private static final String LOG_TAG = DawnAlarms.class.getName();
    public static void configureDawnAlarms(Context context){
        Calendar sunRiseCalendar = Util.getSunrise(context);
        if (sunRiseCalendar == null){
            Log.e(LOG_TAG, "Missing sunRiseCalendar");
            return;
        }
        PendingIntent pendingIntent = null;
        Uri timeUri = null; // // this will hopefully allow matching of intents so when adding a new one with new time it will replace this one
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(context, StartRecordingReceiver.class);

        sunRiseCalendar.add(Calendar.MINUTE, -40); // 40 minutes before dawn
        timeUri = Uri.parse("dawnMinus40Minutes");
        myIntent.setData(timeUri);
        pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent,0);
        alarmManager.set(AlarmManager.RTC_WAKEUP,sunRiseCalendar.getTimeInMillis(),pendingIntent);

        sunRiseCalendar.add(Calendar.MINUTE, +30); // now 10 minutes before dawn
        timeUri = Uri.parse("dawnMinus10Minutes");
        myIntent.setData(timeUri);
        pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent,0);
        alarmManager.set(AlarmManager.RTC_WAKEUP,sunRiseCalendar.getTimeInMillis(),pendingIntent);

        sunRiseCalendar.add(Calendar.MINUTE, +10); // now  dawn
        timeUri = Uri.parse("atDawn");
        myIntent.setData(timeUri);
        pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent,0);
        alarmManager.set(AlarmManager.RTC_WAKEUP,sunRiseCalendar.getTimeInMillis(),pendingIntent);

        sunRiseCalendar.add(Calendar.MINUTE, +10); // now 10 minutes after dawn
        timeUri = Uri.parse("dawnPlus10Minutes");
        myIntent.setData(timeUri);
        pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent,0);
        alarmManager.set(AlarmManager.RTC_WAKEUP,sunRiseCalendar.getTimeInMillis(),pendingIntent);

        sunRiseCalendar.add(Calendar.MINUTE, +30); // now 40 minutes after dawn
        timeUri = Uri.parse("dawnPlus10Minutes");
        myIntent.setData(timeUri);
        pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent,0);
        alarmManager.set(AlarmManager.RTC_WAKEUP,sunRiseCalendar.getTimeInMillis(),pendingIntent);


    }
}
