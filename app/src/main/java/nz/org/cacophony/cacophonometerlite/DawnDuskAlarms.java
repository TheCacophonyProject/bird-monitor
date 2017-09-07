package nz.org.cacophony.cacophonometerlite;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.TimeZone;
import static android.content.Context.ALARM_SERVICE;
//import ch.qos.logback.classic.Logger;
import org.slf4j.Logger;

/**
 * Created by User on 07-Mar-17.
 * This class is used to calculate the actual times of dawn and dusk for each day at any specified location
 * It is used by the code that sets the extra alarms/recordings around dawn and dusk
 */

class DawnDuskAlarms {

    // Need to have recordings that automatically run around dawn and dusk
    // Use code from https://github.com/mikereedell/sunrisesunsetlib-java to get sunrise and sunset for either today or tomorrow, and then offset by the length of twilight - average of 29 mins for NZ
    // To make app robust (hopefully) these alarms are reset every time a periodic alarm runs.

// --Commented out by Inspection START (12-Jun-17 1:56 PM):
//    // according to http://www.gaisma.com/en/location/auckland.html it seems that dawn/dusk times
//    // vary between 26 and 29 minutes before/after sunrise/sunset, so will add/subtract 27 minutes
//    private static final String LOG_TAG = DawnDuskAlarms.class.getName();
// --Commented out by Inspection STOP (12-Jun-17 1:56 PM)

//    private static final String LOG_TAG = DawnDuskAlarms.class.getName();
    private static final String TAG = DawnDuskAlarms.class.getName();

//    static Logger logger;
//
//          static  Logger getLogger (Context context){
//                if (logger == null){
//                    logger = Util.getAndConfigureLogger(context, LOG_TAG);
//                }
//                return logger;
//            }


    static void configureDawnAlarms(Context context) {
//        getLogger(context).info("About to configure Dawn Alarms");
        Prefs prefs = new Prefs(context);
        int dawnDuskOffsetSmallSeconds = (int) prefs.getDawnDuskOffsetSmallSeconds();
        int dawnDuskOffsetLargeSeconds = (int) prefs.getDawnDuskOffsetLargeSeconds();
        int differenceBetweenSmallAndLarge = dawnDuskOffsetLargeSeconds - dawnDuskOffsetSmallSeconds; // used later to set alarms relative to each other
      //  int lengthOfTwilight = (int)prefs.getLengthOfTwilightSeconds();

        Calendar nowToday =  new GregorianCalendar(TimeZone.getTimeZone("Pacific/Auckland"));

        Calendar nowTomorrow = new GregorianCalendar(TimeZone.getTimeZone("Pacific/Auckland"));
        nowTomorrow.add(Calendar.DAY_OF_YEAR, 1);
      //  System.out.println("nowTomorrow " + nowTomorrow.getTime());

        PendingIntent pendingIntent;
        Uri timeUri; // // this will hopefully allow matching of intents so when adding a new one with new time it will replace this one
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(context, StartRecordingReceiver.class);
        myIntent.putExtra("type", "dawn");

        Calendar dawnTodayCalendar = Util.getDawn(context, nowToday);
        Calendar dawnTomorrowCalendar =  Util.getDawn(context, nowTomorrow);

        dawnTodayCalendar.add(Calendar.SECOND, -dawnDuskOffsetLargeSeconds);
        dawnTomorrowCalendar.add(Calendar.SECOND, -dawnDuskOffsetLargeSeconds);
        timeUri = Uri.parse("dawnMinusLargeOffset");
        myIntent.setData(timeUri);
        pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);
        if (nowToday.getTimeInMillis() < dawnTodayCalendar.getTimeInMillis()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, dawnTodayCalendar.getTimeInMillis(), pendingIntent);
          //  System.out.println("today dawnMinus40Minutes " + dawnTodayCalendar.getTime());
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, dawnTomorrowCalendar.getTimeInMillis(), pendingIntent);
           // System.out.println("tomorrow dawnMinus40Minutes " + dawnTomorrowCalendar.getTime());
        }

        dawnTodayCalendar.add(Calendar.SECOND, differenceBetweenSmallAndLarge); // now 10 minutes before dawn
        dawnTomorrowCalendar.add(Calendar.SECOND, differenceBetweenSmallAndLarge);
        timeUri = Uri.parse("dawnMinusSmallOffset");
        myIntent.setData(timeUri);
        pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);
        if (nowToday.getTimeInMillis() < dawnTodayCalendar.getTimeInMillis()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, dawnTodayCalendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, dawnTomorrowCalendar.getTimeInMillis(), pendingIntent);
        }

        dawnTodayCalendar.add(Calendar.SECOND, dawnDuskOffsetSmallSeconds); // now at dawn
        dawnTomorrowCalendar.add(Calendar.SECOND, dawnDuskOffsetSmallSeconds);
        timeUri = Uri.parse("atDawn");
        myIntent.setData(timeUri);
        pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);
        if (nowToday.getTimeInMillis() < dawnTodayCalendar.getTimeInMillis()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, dawnTodayCalendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, dawnTomorrowCalendar.getTimeInMillis(), pendingIntent);
        }

        dawnTodayCalendar.add(Calendar.SECOND, dawnDuskOffsetSmallSeconds); // now 10 minutes after dawn
        dawnTomorrowCalendar.add(Calendar.SECOND, dawnDuskOffsetSmallSeconds);
        timeUri = Uri.parse("dawnPlusSmallOffset");
        myIntent.setData(timeUri);
        pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);
        if (nowToday.getTimeInMillis() < dawnTodayCalendar.getTimeInMillis()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, dawnTodayCalendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, dawnTomorrowCalendar.getTimeInMillis(), pendingIntent);
        }

        dawnTodayCalendar.add(Calendar.SECOND, differenceBetweenSmallAndLarge); // now 40 minutes after dawn
        dawnTomorrowCalendar.add(Calendar.SECOND, differenceBetweenSmallAndLarge);
        timeUri = Uri.parse("dawnPlusLargeOffset");
        myIntent.setData(timeUri);
        pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);
        if (nowToday.getTimeInMillis() < dawnTodayCalendar.getTimeInMillis()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, dawnTodayCalendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, dawnTomorrowCalendar.getTimeInMillis(), pendingIntent);
        }
    }


    static void configureDuskAlarms(Context context) {
//        getLogger(context).info("About to configure Dusk Alarms");
        Prefs prefs = new Prefs(context);
        int dawnDuskOffsetSmallSeconds = (int) prefs.getDawnDuskOffsetSmallSeconds();
        int dawnDuskOffsetLargeSeconds = (int) prefs.getDawnDuskOffsetLargeSeconds();
        int differenceBetweenSmallAndLarge = dawnDuskOffsetLargeSeconds - dawnDuskOffsetSmallSeconds; // used later to set alarms relative to each other

        Calendar nowToday =  new GregorianCalendar(TimeZone.getTimeZone("Pacific/Auckland"));

        Calendar nowTomorrow = new GregorianCalendar(TimeZone.getTimeZone("Pacific/Auckland"));
        nowTomorrow.add(Calendar.DAY_OF_YEAR, 1);

        PendingIntent pendingIntent;
        Uri timeUri; // // this will hopefully allow matching of intents so when adding a new one with new time it will replace this one
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(context, StartRecordingReceiver.class);
        myIntent.putExtra("type", "dusk");

        Calendar duskTodayCalendar = Util.getDusk(context, nowToday);
        Calendar duskTomorrowCalendar = Util.getDusk(context, nowTomorrow);

        duskTodayCalendar.add(Calendar.SECOND, -dawnDuskOffsetLargeSeconds); // 40 minutes before dusk
        duskTomorrowCalendar.add(Calendar.SECOND, -dawnDuskOffsetLargeSeconds);
        timeUri = Uri.parse("duskMinusLargeOffset");
        myIntent.setData(timeUri);
        pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);
        if (nowToday.getTimeInMillis() < duskTodayCalendar.getTimeInMillis()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, duskTodayCalendar.getTimeInMillis(), pendingIntent);

        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, duskTomorrowCalendar.getTimeInMillis(), pendingIntent);

        }

        duskTodayCalendar.add(Calendar.SECOND, differenceBetweenSmallAndLarge); // now 10 minutes before dusk
        duskTomorrowCalendar.add(Calendar.SECOND, differenceBetweenSmallAndLarge);
        timeUri = Uri.parse("duskMinusSmallOffset");
        myIntent.setData(timeUri);
        pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);
        if (nowToday.getTimeInMillis() < duskTodayCalendar.getTimeInMillis()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, duskTodayCalendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, duskTomorrowCalendar.getTimeInMillis(), pendingIntent);
        }

        duskTodayCalendar.add(Calendar.SECOND, dawnDuskOffsetSmallSeconds); // now at dusk
        duskTomorrowCalendar.add(Calendar.SECOND, dawnDuskOffsetSmallSeconds);
        timeUri = Uri.parse("atDusk");
        myIntent.setData(timeUri);
        pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);
        if (nowToday.getTimeInMillis() < duskTodayCalendar.getTimeInMillis()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, duskTodayCalendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, duskTomorrowCalendar.getTimeInMillis(), pendingIntent);
        }

        duskTodayCalendar.add(Calendar.SECOND, dawnDuskOffsetSmallSeconds); // now 10 minutes after dusk
        duskTomorrowCalendar.add(Calendar.SECOND, dawnDuskOffsetSmallSeconds);
        timeUri = Uri.parse("duskPlusSmallOffset");
        myIntent.setData(timeUri);
        pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);
        if (nowToday.getTimeInMillis() < duskTodayCalendar.getTimeInMillis()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, duskTodayCalendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, duskTomorrowCalendar.getTimeInMillis(), pendingIntent);
        }

        duskTodayCalendar.add(Calendar.SECOND, differenceBetweenSmallAndLarge); // now 40 minutes after dusk
        duskTomorrowCalendar.add(Calendar.SECOND, differenceBetweenSmallAndLarge);
        timeUri = Uri.parse("duskPlusLargeOffset");
        myIntent.setData(timeUri);
        pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);
        if (nowToday.getTimeInMillis() < duskTodayCalendar.getTimeInMillis()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, duskTodayCalendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, duskTomorrowCalendar.getTimeInMillis(), pendingIntent);
        }
    }

}
