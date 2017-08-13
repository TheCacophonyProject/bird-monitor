package nz.org.cacophony.cacophonometerlite;

import android.content.Context;
import android.content.SharedPreferences;
//import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.android.BasicLogcatConfigurator;

/**
 * This class helps static classes that don't have an application Context to get and save Shared Preferences (Server.java..)
 */

class Prefs {
    private static final boolean isLocalLog = true;

    private static final String LOG_TAG = "Prefs.java";
    private static Context context = null;


    private static final String PREFS_NAME = "CacophonyPrefs";
    private static final String TEST_SERVER_URL = "http://138.68.237.249:8888/";       // Server URL
    private static final String PRODUCTION_SERVER_URL = "http://103.16.20.22";       // Server URL

    private static final String SERVER_URL_KEY = "SERVER_URL";
    private static final String PASSWORD_KEY = "PASSWORD";
    private static final String DEVICE_NAME_KEY = "DEVICE_NAME";
    private static final String GROUP_NAME_KEY = "GROUP_NAME";
    private static final String LATITUDE_KEY = "LATITUDE";
    private static final String LONGITUDE_KEY = "LONGITUDE";
    private static final String DEVICE_ID = "UNKNOWN";
    private static final String RECORDING_DURATION_SECONDS_KEY = "RECORDING_DURATION_SECONDS";
//    private static final double RECORDING_DURATION_SECONDS = 120;
 //  private static final double RECORDING_DURATION_SECONDS = 60;
     private static final double RECORDING_DURATION_SECONDS = 1;
    private static final String TIME_BETWEEN_RECORDINGS_SECONDS_KEY = "TIME_BETWEEN_RECORDINGS";
  //  private static final double TIME_BETWEEN_RECORDINGS_SECONDS = 3600;  //3600 is one hour!
//    private static final double TIME_BETWEEN_RECORDINGS_SECONDS = 1800;  //half hour
  //  private static final double TIME_BETWEEN_RECORDINGS_SECONDS = 86400;  //86400 is one day!
  //  private static final double TIME_BETWEEN_RECORDINGS_SECONDS = 600;  //600 is ten minutes
    //  private static final double TIME_BETWEEN_RECORDINGS_SECONDS = 300;  //300 is five minutes
  //  private static final double TIME_BETWEEN_RECORDINGS_SECONDS = 60;  //60 is one minute
      private static final double TIME_BETWEEN_RECORDINGS_SECONDS = 120;  //120 is two minute

    private static final String TIME_BETWEEN_UPLOADS_SECONDS_KEY = "TIME_BETWEEN_UPLOADS";
    private static final double TIME_BETWEEN_UPLOADS_SECONDS = 21600;  //21600 is six hours!
//    private static final double TIME_BETWEEN_UPLOADS_SECONDS = 1;  //for testing

    private static final String DAWN_DUSK_OFFSET_LARGE_KEY = "DAWN_DUSK_OFFSET_LARGE";
    private static final double DAWN_DUSK_OFFSET_LARGE_SECONDS = 40 * 60; // 40 minutes

    private static final String DAWN_DUSK_OFFSET_SMALL_KEY = "DAWN_DUSK_OFFSET_SMALL";
    private static final double DAWN_DUSK_OFFSET_SMALL_SECONDS = 10 * 60; // 10 minutes

    private static final String LENGTH_OF_TWILIGHT_KEY = "LENGTH_OF_TWILIGHT"; // Twilight is the time between dawn and sunrise, or sunset and dusk
    private static final double LENGTH_OF_TWILIGHT_SECONDS = 29 * 60; // 29 minutes http://www.gaisma.com/en/location/nelson.html

    private static final String  HAS_ROOT_ACCESS_KEY = "HAS_ROOT_ACCESS";
   // private static final String  IS_CURRENTLY_RECORDING_KEY = "IS_CURRENTLY_RECORDING";
    private static final String  USE_SHORT_RECORDINGS_KEY = "USE_SHORT_RECORDINGS";
    private static final String  USE_FULL_LOGGING_KEY = "USE_FULL_LOGGING";
    private static final String  USE_TEST_SERVER_KEY = "USE_TEST_SERVER";
    private static final String BATTERY_LEVEL_KEY = "BATTERY_LEVEL";
    private static final String MAXIMUM_BATTERY_LEVEL_KEY = "MAXIMUM_BATTERY_LEVEL";
    private static final String DATE_TIME_LAST_UPLOAD_KEY = "DATE_TIME_LAST_UPLOAD";
    private static final String DATE_TIME_LAST_CALCULATED_DAWN_DUSK_KEY = "DATE_TIME_LAST_CALCULATED_DAWN_DUSK";
    private static final String DATE_TIME_LAST_REPEATING_ALARM_FIRED_KEY = "DATE_TIME_LAST_REPEATING_ALARM_FIRED";
    private static Logger logger = null;

    Prefs(Context context) {
        this.context = context;
        if (logger == null){
            logger = Util.getAndConfigureLogger(context,LOG_TAG );
        }
    }

    Prefs(Context context, Logger logger) { // needed this constructor to fix loop of Prefs calling Util calling Prefs
        this.context = context;
        this.logger = logger;

    }



    public static boolean isLocalLog() {
        return isLocalLog;
    }

    private String getString(String key) {
        if (context == null) {
            //Log.e(LOG_TAG, "Context was null when trying to get preferences.");
            logger.error("Context was null when trying to get preferences.");
            return null;
        } else {
            SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return preferences.getString(key, null);
        }
    }

    private void setString(String key, String val) {
        if (context == null) {
          //  Log.e(LOG_TAG, "Context was null when trying to get preferences.");
            logger.error("Context was null when trying to get preferences.");
            return;
        }
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(key, val).apply();
    }

    private double getDouble(String key) {
        if (context == null) {
          //  Log.e(LOG_TAG, "Context was null when trying to get preferences.");
            logger.error("Context was null when trying to get preferences.");
            return 0;
        }
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return Double.longBitsToDouble(preferences.getLong(key, 0));
    }

    private long getLong(String key) {
        if (context == null) {
           // Log.e(LOG_TAG, "Context was null when trying to get preferences.");
            logger.error("Context was null when trying to get preferences.");
            return 0;
        }
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getLong(key, 0);
    }

    private void setDouble(String key, double val) {
        if (context == null) {
           // Log.e(LOG_TAG, "Context was null when trying to get preferences.");
            logger.error("Context was null when trying to get preferences.");
            return;
        }
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putLong(key, Double.doubleToRawLongBits(val)).apply();
    }

    private void setLong(String key, long val) {
        if (context == null) {
//            Log.e(LOG_TAG, "Context was null when trying to get preferences.");
            logger.error("Context was null when trying to get preferences.");
            return;
        }
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putLong(key, val).apply();
    }

    private boolean getBoolean(String key) {
        if (context == null) {
          //  Log.e(LOG_TAG, "Context was null when trying to get preferences.");
            logger.error("Context was null when trying to get preferences.");
            return false;
        }
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(key, false);

    }

    private void setBoolean(String key, boolean val) {
        if (context == null) {
         //   Log.e(LOG_TAG, "Context was null when trying to get preferences.");
            logger.error("Context was null when trying to get preferences.");
            return;
        }
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(key, val).apply();

    }

    String getServerUrl(boolean useTestServer) {
        String url = getString(SERVER_URL_KEY);
        if (url == null) {
           // return DEFAULT_SERVER_URL;
            if (useTestServer){
                return TEST_SERVER_URL;
            }else{
                return PRODUCTION_SERVER_URL;
            }
        }
        else {
            return url;
        }
    }


    String getPassword() {
        return getString(PASSWORD_KEY);
    }

    void setPassword(String password) {
        setString(PASSWORD_KEY, password);
    }

    String getDeviceName() {
        return getString(DEVICE_NAME_KEY);
    }

    void setDeviceName(String name) {
        setString(DEVICE_NAME_KEY, name);
    }

    String getGroupName() {
        return getString(GROUP_NAME_KEY);
    }

    void setGroupName(String name) {
        setString(GROUP_NAME_KEY, name);
    }

    double getLatitude() {
        return getDouble(LATITUDE_KEY);
    }

    void setLatitude(double val) {
        setDouble(LATITUDE_KEY, val);
    }

    double getLongitude() {
        return getDouble(LONGITUDE_KEY);
    }

    void setLongitude(double val) {
        setDouble(LONGITUDE_KEY, val);
    }

    void setDeviceId(String deviceID) {
        setString(DEVICE_ID, deviceID);
    }

    double getRecordingDuration() {
        return getDouble(RECORDING_DURATION_SECONDS_KEY);
    }

    void setRecordingDurationSeconds() {
        setDouble(RECORDING_DURATION_SECONDS_KEY, RECORDING_DURATION_SECONDS);
    }

    double getTimeBetweenRecordingsSeconds() {
        return getDouble(TIME_BETWEEN_RECORDINGS_SECONDS_KEY);
    }

    void setTimeBetweenRecordingsSeconds() {
        setDouble(TIME_BETWEEN_RECORDINGS_SECONDS_KEY, TIME_BETWEEN_RECORDINGS_SECONDS);
    }

    double getTimeBetweenUploadsSeconds() {
        return getDouble(TIME_BETWEEN_UPLOADS_SECONDS_KEY);
    }

    void setTimeBetweenUploadsSeconds() {
        setDouble(TIME_BETWEEN_UPLOADS_SECONDS_KEY, TIME_BETWEEN_UPLOADS_SECONDS);
    }

    double getDawnDuskOffsetLargeSeconds() {
        return getDouble(DAWN_DUSK_OFFSET_LARGE_KEY);
    }

    void setDawnDuskOffsetLargeSeconds() {
        setDouble(DAWN_DUSK_OFFSET_LARGE_KEY, DAWN_DUSK_OFFSET_LARGE_SECONDS);
    }

    double getDawnDuskOffsetSmallSeconds() {
        return getDouble(DAWN_DUSK_OFFSET_SMALL_KEY);
    }

    void setDawnDuskOffsetSmallSeconds() {
        setDouble(DAWN_DUSK_OFFSET_SMALL_KEY, DAWN_DUSK_OFFSET_SMALL_SECONDS);
    }

    double getLengthOfTwilightSeconds() {
        return getDouble(LENGTH_OF_TWILIGHT_KEY);
    }

    void setLengthOfTwilightSeconds() {
        setDouble(LENGTH_OF_TWILIGHT_KEY, LENGTH_OF_TWILIGHT_SECONDS);
    }

    boolean getHasRootAccess() {
        return getBoolean(HAS_ROOT_ACCESS_KEY);
    }

    boolean getUseTestServer() {
        return getBoolean(USE_TEST_SERVER_KEY);
    }

    void setHasRootAccess(boolean hasRootAccess) {
        setBoolean(HAS_ROOT_ACCESS_KEY, hasRootAccess);
    }

    boolean getUseShortRecordings() {
        return getBoolean(USE_SHORT_RECORDINGS_KEY);
    }

    void setUseShortRecordings(boolean useShortRecordings) {
        setBoolean(USE_SHORT_RECORDINGS_KEY, useShortRecordings);
    }

    boolean getUseFullLogging() {
        return getBoolean(USE_FULL_LOGGING_KEY);
    }

    void setUseFullLogging(boolean useFullLogging) {
        setBoolean(USE_FULL_LOGGING_KEY, useFullLogging);
    }

    void setUseTestServer(boolean useTestServer) {
        setBoolean(USE_TEST_SERVER_KEY, useTestServer);
    }

    void setBatteryLevel(double batteryLevel) {
        setDouble(BATTERY_LEVEL_KEY, batteryLevel);
    }

    double getMaximumBatteryLevel() {
        return getDouble(MAXIMUM_BATTERY_LEVEL_KEY);
    }

    void setMaximumBatteryLevel(double batteryLevel) {
        setDouble(MAXIMUM_BATTERY_LEVEL_KEY, batteryLevel);
    }

    void setDateTimeLastUpload(long dateTimeLastUpload) {
        setLong(DATE_TIME_LAST_UPLOAD_KEY, dateTimeLastUpload);
    }

    long getDateTimeLastUpload() {
        return getLong(DATE_TIME_LAST_UPLOAD_KEY);
    }

    long getDateTimeLastCalculatedDawnDusk() {
        return getLong(DATE_TIME_LAST_CALCULATED_DAWN_DUSK_KEY);
    }

    void setDateTimeLastCalculatedDawnDusk(long dateTimeLastCalculatedDawnDusk) {
        setLong(DATE_TIME_LAST_CALCULATED_DAWN_DUSK_KEY, dateTimeLastCalculatedDawnDusk);
    }

    long getDateTimeLastRepeatingAlarmFired() {
        return getLong(DATE_TIME_LAST_REPEATING_ALARM_FIRED_KEY);
    }

    void setDateTimeLastRepeatingAlarmFired(long dateTimeLastRepeatingAlarmFired) {
        setLong(DATE_TIME_LAST_REPEATING_ALARM_FIRED_KEY, dateTimeLastRepeatingAlarmFired);
    }
}
