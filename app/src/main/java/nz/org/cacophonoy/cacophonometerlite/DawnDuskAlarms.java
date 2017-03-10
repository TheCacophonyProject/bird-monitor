package nz.org.cacophonoy.cacophonometerlite;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.GregorianCalendar;

import java.util.Calendar;
import java.util.TimeZone;


import static android.content.Context.ALARM_SERVICE;

/**
 * Created by User on 07-Mar-17.
 */

public class DawnDuskAlarms {
    // according to http://www.gaisma.com/en/location/auckland.html it seems that dawn/dusk times
    // vary between 26 and 29 minutes before/after sunrise/sunset, so will add/subtract 27 minutes
    private static final String LOG_TAG = DawnDuskAlarms.class.getName();



    public static void configureDawnAlarms(Context context) {
        Prefs prefs = new Prefs(context);
        int dawnDuskOffsetSmallSeconds = (int) prefs.getDawnDuskOffsetSmallSeconds();
        int dawnDuskOffsetLargeSeconds = (int) prefs.getDawnDuskOffsetLargeSeconds();
        int differenceBetweenSmallAndLarge = dawnDuskOffsetLargeSeconds - dawnDuskOffsetSmallSeconds; // used later to set alarms relative to each other
        int lengthOfTwilight = (int)prefs.getLengthOfTwilightSeconds();

        Calendar nowToday =  new GregorianCalendar(TimeZone.getTimeZone("Pacific/Auckland"));

        Calendar nowTomorrow = new GregorianCalendar(TimeZone.getTimeZone("Pacific/Auckland"));
        nowTomorrow.add(Calendar.DAY_OF_YEAR, 1);
      //  System.out.println("nowTomorrow " + nowTomorrow.getTime());

        PendingIntent pendingIntent = null;
        Uri timeUri = null; // // this will hopefully allow matching of intents so when adding a new one with new time it will replace this one
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(context, StartRecordingReceiver.class);
        myIntent.putExtra("type", "dawn");

        Calendar sunRiseTodayCalendar = Util.getSunrise(context, nowToday);
        Calendar dawnTodayCalendar = (Calendar) sunRiseTodayCalendar.clone();
        dawnTodayCalendar.add(Calendar.SECOND, -lengthOfTwilight); // going to use dawn rather than sunrise

        Calendar sunRiseTomorrowCalendar = Util.getSunrise(context, nowTomorrow);
       Calendar dawnTomorrowCalendar = (Calendar) sunRiseTomorrowCalendar.clone();
        dawnTomorrowCalendar.add(Calendar.SECOND, -lengthOfTwilight); // going to use dawn rather than sunrise

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


    public static void configureDuskAlarms(Context context) {
        Prefs prefs = new Prefs(context);
        int dawnDuskOffsetSmallSeconds = (int) prefs.getDawnDuskOffsetSmallSeconds();
        int dawnDuskOffsetLargeSeconds = (int) prefs.getDawnDuskOffsetLargeSeconds();
        int differenceBetweenSmallAndLarge = dawnDuskOffsetLargeSeconds - dawnDuskOffsetSmallSeconds; // used later to set alarms relative to each other
        int lengthOfTwilight = (int)prefs.getLengthOfTwilightSeconds();

        Calendar nowToday =  new GregorianCalendar(TimeZone.getTimeZone("Pacific/Auckland"));

        Calendar nowTomorrow = new GregorianCalendar(TimeZone.getTimeZone("Pacific/Auckland"));
        nowTomorrow.add(Calendar.DAY_OF_YEAR, 1);
        //  System.out.println("nowTomorrow " + nowTomorrow.getTime());

        PendingIntent pendingIntent = null;
        Uri timeUri = null; // // this will hopefully allow matching of intents so when adding a new one with new time it will replace this one
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(context, StartRecordingReceiver.class);
        myIntent.putExtra("type", "dawn");

        Calendar sunSetTodayCalendar = Util.getSunrise(context, nowToday);
        Calendar duskTodayCalendar = (Calendar) sunSetTodayCalendar.clone();
        duskTodayCalendar.add(Calendar.SECOND, lengthOfTwilight); // going to use dusk rather than sunset

        Calendar sunSetTomorrowCalendar = Util.getSunrise(context, nowTomorrow);
        Calendar duskTomorrowCalendar = (Calendar) sunSetTomorrowCalendar.clone();
        duskTomorrowCalendar.add(Calendar.SECOND, lengthOfTwilight); // going to use dusk rather than sunset

        duskTodayCalendar.add(Calendar.SECOND, -dawnDuskOffsetLargeSeconds); // 40 minutes before dusk
        duskTomorrowCalendar.add(Calendar.SECOND, -dawnDuskOffsetLargeSeconds);
        timeUri = Uri.parse("dawnMinus40Minutes");
        myIntent.setData(timeUri);
        pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);
        if (nowToday.getTimeInMillis() < duskTodayCalendar.getTimeInMillis()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, duskTodayCalendar.getTimeInMillis(), pendingIntent);
           // System.out.println("today dawnMinus40Minutes " + duskTodayCalendar.getTime());
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, duskTomorrowCalendar.getTimeInMillis(), pendingIntent);
           // System.out.println("tomorrow dawnMinus40Minutes " + duskTomorrowCalendar.getTime());
        }

        duskTodayCalendar.add(Calendar.SECOND, differenceBetweenSmallAndLarge); // now 10 minutes before dusk
        duskTomorrowCalendar.add(Calendar.SECOND, differenceBetweenSmallAndLarge);
        timeUri = Uri.parse("dawnMinusSmallOffset");
        myIntent.setData(timeUri);
        pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);
        if (nowToday.getTimeInMillis() < duskTodayCalendar.getTimeInMillis()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, duskTodayCalendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, duskTomorrowCalendar.getTimeInMillis(), pendingIntent);
        }

        duskTodayCalendar.add(Calendar.SECOND, dawnDuskOffsetSmallSeconds); // now at dusk
        duskTomorrowCalendar.add(Calendar.SECOND, dawnDuskOffsetSmallSeconds);
        timeUri = Uri.parse("atDawn");
        myIntent.setData(timeUri);
        pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);
        if (nowToday.getTimeInMillis() < duskTodayCalendar.getTimeInMillis()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, duskTodayCalendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, duskTomorrowCalendar.getTimeInMillis(), pendingIntent);
        }

        duskTodayCalendar.add(Calendar.SECOND, dawnDuskOffsetSmallSeconds); // now 10 minutes after dusk
        duskTomorrowCalendar.add(Calendar.SECOND, dawnDuskOffsetSmallSeconds);
        timeUri = Uri.parse("dawnPlusSmallOffset");
        myIntent.setData(timeUri);
        pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);
        if (nowToday.getTimeInMillis() < duskTodayCalendar.getTimeInMillis()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, duskTodayCalendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, duskTomorrowCalendar.getTimeInMillis(), pendingIntent);
        }

        duskTodayCalendar.add(Calendar.SECOND, differenceBetweenSmallAndLarge); // now 40 minutes after dusk
        duskTomorrowCalendar.add(Calendar.SECOND, differenceBetweenSmallAndLarge);
        timeUri = Uri.parse("dawnPlusLargeOffset");
        myIntent.setData(timeUri);
        pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);
        if (nowToday.getTimeInMillis() < duskTodayCalendar.getTimeInMillis()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, duskTodayCalendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, duskTomorrowCalendar.getTimeInMillis(), pendingIntent);
        }

    }

}
