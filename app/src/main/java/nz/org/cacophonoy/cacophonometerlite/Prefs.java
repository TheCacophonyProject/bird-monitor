package nz.org.cacophonoy.cacophonometerlite;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * This class helps static classes that don't have an application Context to get and save Shared Preferences (Server.java..)
 */

class Prefs {

    private static final String LOG_TAG = "Prefs.java";
    private Context context = null;


    private static final String PREFS_NAME = "CacophonyPrefs";
    //private static final String DEFAULT_SERVER_URL = "http://52.64.67.145:8888";       // Server URL
    private static final String DEFAULT_SERVER_URL = "http://138.68.237.249:8888/";       // Server URL
    //private static final String DEFAULT_SERVER_URL = "http://192.168.1.9:8888";       // Server URL
    private static final String SERVER_URL_KEY = "SERVER_URL";
    private static final String PASSWORD_KEY = "PASSWORD";
    private static final String DEVICE_NAME_KEY = "DEVICE_NAME";
    private static final String GROUP_NAME_KEY = "GROUP_NAME";
    private static final String LATITUDE_KEY = "LATITUDE";
    private static final String LONGITUDE_KEY = "LONGITUDE";
    private static final String DEVICE_ID = "UNKNOWN";
    private static final String RECORDING_DURATION_SECONDS_KEY = "RECORDING_DURATION_SECONDS";
    //private static final double RECORDING_DURATION_SECONDS = 120;
    private static final double RECORDING_DURATION_SECONDS = 1;
    private static final String TIME_BETWEEN_RECORDINGS_SECONDS_KEY = "TIME_BETWEEN_RECORDINGS";
    //private static final double TIME_BETWEEN_RECORDINGS_SECONDS = 3600;  //3600 is one hour!
    private static final double TIME_BETWEEN_RECORDINGS_SECONDS = 600;  //600 is ten minutes
   // private static final double TIME_BETWEEN_RECORDINGS_SECONDS = 60;  //600 is ten minutes

    private static final String DAWN_DUSK_OFFSET_LARGE_KEY = "DAWN_DUSK_OFFSET_LARGE";
    private static final double DAWN_DUSK_OFFSET_LARGE_SECONDS = 40 * 60; // 40 minutes

    private static final String DAWN_DUSK_OFFSET_SMALL_KEY = "DAWN_DUSK_OFFSET_SMALL";
    private static final double DAWN_DUSK_OFFSET_SMALL_SECONDS = 10 * 60; // 10 minutes

    private static final String LENGTH_OF_TWILIGHT_KEY = "LENGTH_OF_TWILIGHT"; // Twilight is the time between dawn and sunrise, or sunset and dusk
    private static final double LENGTH_OF_TWILIGHT_SECONDS = 29 * 60; // 27 minutes http://www.gaisma.com/en/location/nelson.html


    Prefs(Context context) {
        this.context = context;
    }

    private String getString(String key) {
        if (context == null) {
            Log.e(LOG_TAG, "Context was null when trying to get preferences.");
            return null;
        } else {
            SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return preferences.getString(key, null);
        }
    }

    private void setString(String key, String val) {
        if (context == null) {
            Log.e(LOG_TAG, "Context was null when trying to get preferences.");
            return;
        }
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(key, val).apply();
    }

    private double getDouble(String key) {
        if (context == null) {
            Log.e(LOG_TAG, "Context was null when trying to get preferences.");
            return 0;
        }
            SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return Double.longBitsToDouble(preferences.getLong(key, 0));
    }

    private void setDouble(String key, double val) {
        if (context == null) {
            Log.e(LOG_TAG, "Context was null when trying to get preferences.");
            return;
        }
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putLong(key, Double.doubleToRawLongBits(val)).apply();
    }

    String getServerUrl() {
        String url = getString(SERVER_URL_KEY);
        if (url == null)
            return DEFAULT_SERVER_URL;
        else
            return url;
    }

    void setServerUrl(String url) {
        setString(SERVER_URL_KEY, url);
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
}
