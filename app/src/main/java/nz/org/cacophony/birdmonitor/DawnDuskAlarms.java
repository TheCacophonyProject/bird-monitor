package nz.org.cacophony.birdmonitor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static android.content.Context.ALARM_SERVICE;

//import ch.qos.logback.classic.Logger;

/**
 * This class creates alarms (that start recordings) that occur every 10 minutes either side of
 * dawn and dusk.  It uses SunriseSunsetCalculator class from com.luckycatlabs to calculate the
 * time of sunrise and sunset and then applies a rough offset to give dawn and dusk.
 *
 * According to http://www.gaisma.com/en/location/auckland.html it seems that dawn/dusk times
 * vary between 26 and 29 minutes before/after sunrise/sunset, so will add/subtract 27 minutes.
 */

class DawnDuskAlarms {


    private static final String TAG = DawnDuskAlarms.class.getName();

    static void configureDawnAndDuskAlarms(Context context, boolean ignoreTimeConstraints){
        Prefs prefs = new Prefs(context);
        long dateTimeLastCalculatedDawnDusk = prefs.getDateTimeLastCalculatedDawnDusk();
        long timeIntervalBetweenDawnDuskTimeCalculation = 1000 * 60 * 60 * 12; // 12 hours - seemed like a good period to wait. Must happen within 24 hours (and shift in sun) since they were last set
        long now = new Date().getTime();
        if (((now - dateTimeLastCalculatedDawnDusk) > timeIntervalBetweenDawnDuskTimeCalculation) || ignoreTimeConstraints) {
            if ((DawnDuskAlarms.isOutsideDawnDuskRecordings(context, prefs)) || ignoreTimeConstraints) {
                prefs.deleteDawnDuskAlarmList();
                DawnDuskAlarms.configureDawnAlarmsUsingLoop(context);
                DawnDuskAlarms.configureDuskAlarmsUsingLoop(context);
                prefs.setDateTimeLastCalculatedDawnDusk(now);
            }
        }


    }

    private static void configureDawnAlarmsUsingLoop(Context context) {

        Prefs prefs = new Prefs(context);
        Calendar nowToday =  new GregorianCalendar(TimeZone.getTimeZone("Pacific/Auckland"));
        Calendar nowTomorrow = new GregorianCalendar(TimeZone.getTimeZone("Pacific/Auckland"));
        nowTomorrow.add(Calendar.DAY_OF_YEAR, 1);

        PendingIntent pendingIntent;
        Uri timeUri; // // this will hopefully allow matching of intents so when adding a new one with new time it will replace this one
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (alarmManager == null){
            Log.e(TAG, "alarmManager is null");
            return;
        }
        Intent myIntent = new Intent(context, StartRecordingReceiver.class);
        myIntent.putExtra("type", "dawn");
        myIntent.putExtra("callingCode", "configureDawnAlarmsUsingLoop"); // for debugging

        int dawnDuskOffsetMinutes = (int)prefs.getDawnDuskOffsetMinutes();
        int dawnDuskIncrementMinutes = (int)prefs.getDawnDuskIncrementMinutes();

        int currentOffsetSeconds = dawnDuskOffsetMinutes * 60 * -1;

        while (currentOffsetSeconds <= (dawnDuskOffsetMinutes * 60) ){ // we are going to keep adding alarms until currentOffsetSeconds reaches dawn + minutesBeforeAfterDawnToDoExtraRecordings
            Calendar dawnTodayCalendar = Util.getDawn(context, nowToday);
            Calendar dawnTomorrowCalendar =  Util.getDawn(context, nowTomorrow);

            dawnTodayCalendar.add(Calendar.SECOND, +currentOffsetSeconds);
            dawnTomorrowCalendar.add(Calendar.SECOND, +currentOffsetSeconds);

            String currentOffsetAsString = Integer.toString(currentOffsetSeconds); // need this to label alarm so it can be overwritten when dawn changes, otherwise more alarms will be added each time calculation is done
            timeUri = Uri.parse("dawn" + currentOffsetAsString);
            myIntent.setData(timeUri);
            pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);
            if (nowToday.getTimeInMillis() < dawnTodayCalendar.getTimeInMillis()) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, dawnTodayCalendar.getTimeInMillis(), pendingIntent);
                }else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){ //m is Marshmallow 23
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, dawnTodayCalendar.getTimeInMillis() , pendingIntent );
                }else {// Marshmallow will go into Doze mode, so use setExactAndAllowWhileIdle to allow wakeup https://developer.android.com/reference/android/app/AlarmManager#setExactAndAllowWhileIdle(int,%20long,%20android.app.PendingIntent)
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dawnTodayCalendar.getTimeInMillis() , pendingIntent );
                }
                Util.addDawnDuskAlarm(context, dawnTodayCalendar.getTimeInMillis());
            } else {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, dawnTomorrowCalendar.getTimeInMillis(), pendingIntent);
                }else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){ //m is Marshmallow 23
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, dawnTomorrowCalendar.getTimeInMillis(), pendingIntent);
                }else {// Marshmallow will go into Doze mode, so use setExactAndAllowWhileIdle to allow wakeup https://developer.android.com/reference/android/app/AlarmManager#setExactAndAllowWhileIdle(int,%20long,%20android.app.PendingIntent)
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dawnTomorrowCalendar.getTimeInMillis(), pendingIntent);
                }
                Util.addDawnDuskAlarm(context, dawnTomorrowCalendar.getTimeInMillis());
            }
            currentOffsetSeconds +=  (dawnDuskIncrementMinutes * 60);
        }
    }


    private static void configureDuskAlarmsUsingLoop(Context context) {
        Prefs prefs = new Prefs(context);
        Calendar nowToday =  new GregorianCalendar(TimeZone.getTimeZone("Pacific/Auckland"));
        Calendar nowTomorrow = new GregorianCalendar(TimeZone.getTimeZone("Pacific/Auckland"));
        nowTomorrow.add(Calendar.DAY_OF_YEAR, 1);

        PendingIntent pendingIntent;
        Uri timeUri; // // this will hopefully allow matching of intents so when adding a new one with new time it will replace this one
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (alarmManager == null){
            Log.e(TAG, "alarmManager == null");
            return;
        }
        Intent myIntent = new Intent(context, StartRecordingReceiver.class);
        myIntent.putExtra("type", "dusk");
        myIntent.putExtra("callingCode", "configureDuskAlarmsUsingLoop"); // for debugging

        int dawnDuskOffsetMinutes = (int)prefs.getDawnDuskOffsetMinutes();
        int dawnDuskIncrementMinutes = (int)prefs.getDawnDuskIncrementMinutes();

        int currentOffsetSeconds = dawnDuskOffsetMinutes * 60 * -1;

        while (currentOffsetSeconds <= (dawnDuskOffsetMinutes * 60) ){ // we are going to keep adding alarms until currentOffsetSeconds reaches dawn + minutesBeforeAfterDawnToDoExtraRecordings
            Calendar duskTodayCalendar = Util.getDusk(context, nowToday);
            Calendar duskTomorrowCalendar =  Util.getDusk(context, nowTomorrow);

            duskTodayCalendar.add(Calendar.SECOND, +currentOffsetSeconds);
            duskTomorrowCalendar.add(Calendar.SECOND, +currentOffsetSeconds);

            String currentOffsetAsString = Integer.toString(currentOffsetSeconds); // need this to label alarm so it can be overwritten when dawn changes, otherwise more alarms will be added each time calculation is done
            timeUri = Uri.parse("dusk" + currentOffsetAsString);
            myIntent.setData(timeUri);
            pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);
            if (nowToday.getTimeInMillis() < duskTodayCalendar.getTimeInMillis()) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, duskTodayCalendar.getTimeInMillis(), pendingIntent);
                }else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){ //m is Marshmallow 23
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, duskTodayCalendar.getTimeInMillis(), pendingIntent);
                }else {// Marshmallow will go into Doze mode, so use setExactAndAllowWhileIdle to allow wakeup https://developer.android.com/reference/android/app/AlarmManager#setExactAndAllowWhileIdle(int,%20long,%20android.app.PendingIntent)
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, duskTodayCalendar.getTimeInMillis(), pendingIntent);
                }
                Util.addDawnDuskAlarm(context, duskTodayCalendar.getTimeInMillis());
            } else {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, duskTomorrowCalendar.getTimeInMillis(), pendingIntent);
                }else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){ //m is Marshmallow 23
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, duskTomorrowCalendar.getTimeInMillis(), pendingIntent);
                }else {// Marshmallow will go into Doze mode, so use setExactAndAllowWhileIdle to allow wakeup https://developer.android.com/reference/android/app/AlarmManager#setExactAndAllowWhileIdle(int,%20long,%20android.app.PendingIntent)
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, duskTomorrowCalendar.getTimeInMillis(), pendingIntent);
                }
                Util.addDawnDuskAlarm(context, duskTomorrowCalendar.getTimeInMillis());
            }
            Log.d(TAG, "Dusk alarm set with offset of " + currentOffsetSeconds + " seconds");
            currentOffsetSeconds +=  (dawnDuskIncrementMinutes * 60);
        }
    }


    @SuppressWarnings("RedundantIfStatement")
    private static boolean isOutsideDawnDuskRecordings(Context context, Prefs prefs){
        // Trying to fix bug of not all 13 dawn or dusk recordings happening - maybe it is because
        // the recalculation of alarms interferes.  So don't do the recalculation within the time range of dawn dusk readings (plus a margin)

        int dawnDuskOffsetMinutes = (int)prefs.getDawnDuskOffsetMinutes();
        dawnDuskOffsetMinutes += 60; // extra margin
        Calendar nowToday =  new GregorianCalendar(TimeZone.getTimeZone("Pacific/Auckland"));

        Calendar dawnRecordingsStart = Util.getDawn(context, nowToday);
        dawnRecordingsStart.add(Calendar.MINUTE, -dawnDuskOffsetMinutes);

        Calendar dawnRecordingsFinish = Util.getDawn(context, nowToday);
        dawnRecordingsFinish.add(Calendar.MINUTE, dawnDuskOffsetMinutes);

        if ((dawnRecordingsStart.getTimeInMillis() < nowToday.getTimeInMillis() && (nowToday.getTimeInMillis() < dawnRecordingsFinish.getTimeInMillis()))){
            return false;
        }

        Calendar duskRecordingsStart = Util.getDawn(context, nowToday);
        duskRecordingsStart.add(Calendar.MINUTE, -dawnDuskOffsetMinutes);

        Calendar duskRecordingsFinish = Util.getDawn(context, nowToday);
        duskRecordingsFinish.add(Calendar.MINUTE, dawnDuskOffsetMinutes);

        //noinspection RedundantIfStatement
        if ((duskRecordingsStart.getTimeInMillis() < nowToday.getTimeInMillis() && (nowToday.getTimeInMillis() < duskRecordingsFinish.getTimeInMillis()))){
            return false;
        }

        // If it gets here then current time is outside of the dawn or dusk readings time period (typically from 1 hour before to 1 hour after, dawn or dusk)
        return true;
    }


}
