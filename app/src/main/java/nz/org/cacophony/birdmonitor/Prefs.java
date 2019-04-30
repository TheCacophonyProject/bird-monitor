package nz.org.cacophony.birdmonitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * This class helps static classes that don't have an application Context to get and save Shared Preferences (Server.java..)
 * Expanded to keep all settings in one place
 */

class Prefs {

    private static final String TAG = Prefs.class.getName();
    private final Context context;
    private static final String PREFS_NAME = "CacophonyPrefs";
    private static final String PRODUCTION_CACOPHONY_PROJECT_WEBSITE_BROWSE_RECORDINGS = "https://browse.cacophony.org.nz/";
    private static final String TEST_CACOPHONY_PROJECT_WEBSITE_BROWSE_RECORDINGS = "https://browse-test.cacophony.org.nz/";
    private static final String PRODUCTION_SERVER_HOST = "api.cacophony.org.nz";
    private static final String TEST_SERVER_HOST = "api-test.cacophony.org.nz";       // Test Server URL
    private static final String SCHEME = "https";
    private static final String DEVICE_PASSWORD_KEY = "PASSWORD";
    private static final String USERNAME_PASSWORD_KEY = "USERNAME_PASSWORD";
    private static final String DEVICE_NAME_KEY = "DEVICE_NAME";
    private static final String USERNAME_KEY = "USERNAME";
    private static final String GROUP_NAME_KEY = "GROUP_NAME";
    private static final String DEVICE_TOKEN_KEY = "TOKEN";
    private static final String USER_TOKEN_KEY = "USER_TOKEN";
    private static final long DEVICE_TOKEN_TIMEOUT_SECONDS = 60 * 60 * 24 * 7; // 1 week
    private static final String DEVICE_TOKEN_LAST_REFRESHED_KEY = "TOKEN_LAST_REFRESHED";
    private static final String USER_TOKEN_LAST_REFRESHED_KEY = "USERNAME_TOKEN_LAST_REFRESHED";
    private static final String EMAIL_ADDRESS_KEY = "EMAIL_ADDRESS";
    private static final String USERNAME_OR_EMAIL_ADDRESS_KEY = "USERNAME_OR_EMAIL_ADDRESS";

    private static final String LATITUDE_KEY = "LATITUDE";
    private static final String LONGITUDE_KEY = "LONGITUDE";
    private static final String DEVICE_ID = "UNKNOWN";
    private static final String RECORDING_DURATION_SECONDS_KEY = "RECORDING_DURATION_SECONDS";
    private static final double RECORDING_DURATION_SECONDS = 60;

    private static final String NORMAL_TIME_BETWEEN_RECORDINGS_SECONDS_KEY = "TIME_BETWEEN_RECORDINGS";
    private static final double NORMAL_TIME_BETWEEN_RECORDINGS_SECONDS = 3600;  //3600 is one hour!

    private static final String TIME_BETWEEN_FREQUENT_RECORDINGS_SECONDS_KEY = "TIME_BETWEEN_FREQUENT_RECORDINGS_SECONDS";
    private static final double TIME_BETWEEN_FREQUENT_RECORDINGS_SECONDS = 900;  //900 is 15 minutes

    private static final String TIME_BETWEEN_VERY_FREQUENT_RECORDINGS_SECONDS_KEY = "TIME_BETWEEN_VERY_FREQUENT_RECORDINGS_SECONDS";
    private static final double TIME_BETWEEN_VERY_FREQUENT_RECORDINGS_SECONDS = 120;  //120 is two minutes, use for testing

    private static final String TIME_BETWEEN_GPS_LOCATION_UPDATES_SECONDS_KEY = "TIME_BETWEEN_GPS_LOCATION_UPDATES_SECONDS";
    private static final double TIME_BETWEEN_GPS_LOCATION_UPDATES_SECONDS = 300; // 300 is 5 minutes


    private static final String BATTERY_LEVEL_CUTOFF_REPEATING_RECORDINGS_KEY = "BATTERY_LEVEL_CUTOFF_REPEATING_RECORDINGS";
    private static final double BATTERY_LEVEL_CUTOFF_REPEATING_RECORDINGS = 70;

    private static final String IGNORE_BATTERY_LEVEL_CUTOFF_REPEATING_RECORDINGS_KEY = "IGNORE_BATTERY_LEVEL_CUTOFF_REPEATING_RECORDINGS";

    private static final String BATTERY_LEVEL_CUTOFF_DAWN_DUSK_RECORDINGS_KEY = "BATTERY_LEVEL_CUTOFF_DAWN_DUSK_RECORDINGS";
    private static final double BATTERY_LEVEL_CUTOFF_DAWN_DUSK_RECORDINGS = 50;

    private static final String IGNORE_BATTERY_LEVEL_CUTOFF_DAWN_DUSK_RECORDINGS_KEY = "IGNORE_BATTERY_LEVEL_CUTOFF_DAWN_DUSK_RECORDINGS";

    private static final String TIME_BETWEEN_UPLOADS_SECONDS_KEY = "TIME_BETWEEN_UPLOADS";
    private static final double TIME_BETWEEN_UPLOADS_SECONDS = 21600;  //21600 is six hours!

    private static final String TIME_BETWEEN_FREQUENT_UPLOADS_SECONDS_KEY = "TIME_BETWEEN_FREQUENT_UPLOADS_SECONDS";
    private static final double TIME_BETWEEN_FREQUENT_UPLOADS_SECONDS = 1;  // So happens with every recording

    private static final String DAWN_DUSK_OFFSET_MINUTES_KEY = "DAWN_DUSK_OFFSET_MINUTES";
    private static final double DAWN_DUSK_OFFSET_MINUTES = 60;

    private static final String DAWN_DUSK_INCREMENT_MINUTES_KEY = "DAWN_DUSK_INCREMENT_MINUTES";
    private static final double DAWN_DUSK_INCREMENT_MINUTES = 10;

    private static final String LENGTH_OF_TWILIGHT_KEY = "LENGTH_OF_TWILIGHT"; // Twilight is the time between dawn and sunrise, or sunset and dusk
    private static final double LENGTH_OF_TWILIGHT_SECONDS = 29 * 60; // 29 minutes http://www.gaisma.com/en/location/nelson.html

    private static final String HAS_ROOT_ACCESS_KEY = "HAS_ROOT_ACCESS";
    private static final String CANCEL_RECORDING_ACCESS_KEY = "CANCEL_RECORDING_ACCESS";

    private static final String USE_VERY_FREQUENT_RECORDINGS_KEY = "USE_VERY_FREQUENT_RECORDINGS";
    private static final String USE_FREQUENT_RECORDINGS_KEY = "USE_FREQUENT_RECORDINGS";

    private static final String USE_SHORT_RECORDINGS_KEY = "USE_SHORT_RECORDINGS";
    private static final String USE_FREQUENT_UPLOADS_KEY = "USE_FREQUENT_UPLOADS";
    private static final String IGNORE_LOW_BATTERY_KEY = "IGNORE_LOW_BATTERY";

    private static final String USE_TEST_SERVER_KEY = "USE_TEST_SERVER";
    private static final String ONLINE_MODE_KEY = "ONLINE_MODE";
    private static final String PLAY_WARNING_SOUND_KEY = "PLAY_WARNING_SOUND";

    private static final String BATTERY_LEVEL_KEY = "BATTERY_LEVEL";
    private static final String MAXIMUM_BATTERY_LEVEL_KEY = "MAXIMUM_BATTERY_LEVEL";
    private static final String DATE_TIME_LAST_UPLOAD_KEY = "DATE_TIME_LAST_UPLOAD";
    private static final String DATE_TIME_LAST_CALCULATED_DAWN_DUSK_KEY = "DATE_TIME_LAST_CALCULATED_DAWN_DUSK";
    private static final String DATE_TIME_LAST_REPEATING_ALARM_FIRED_KEY = "DATE_TIME_LAST_REPEATING_ALARM_FIRED";
    private static final String LAST_RECORDING_ID_RETURNED_FROM_SERVER = "LAST_RECORDING_ID_RETURNED_FROM_SERVER";
    private static final String PERIODICALLY_UPDATE_GPS_KEY = "ALWAYS_UPDATE_GPS";
    private static final String FIRST_TIME_KEY = "FIRST_TIME";
    private static final String NEXT_ALARM_KEY = "NEXT_ALARM";
    private static final String DAWN_DUSK_ALARMS_KEY = "DAWN_DUSK_ALARMS";
    private static final String LAST_RECORDING_TIME_KEY = "LAST_RECORDING_TIME";
    private static final String INTERNET_CONNECTION_MODE_KEY = "INTERNET_CONNECTION_MODE";
    private static final String AUDIO_SOURCE_KEY = "AUDIO_SOURCE";
    private static final String BIRD_COUNT_DURATION_KEY = "BIRD_COUNT_DURATION";
    private static final String DISABLED_KEY = "DISABLED";
    private static final String DISABLED_DAWN_DUSK_RECORDINGS_KEY = "DISABLED_DAWN_DUSK_RECORDINGS";
    private static final String SETTINGS_FOR_TEST_SERVER_ENABLED_KEY = "SETTINGS_FOR_TEST_SERVER_ENABLED";
    private static final String GROUPS_KEY = "GROUPS";
    private static final String USER_SIGNED_IN_KEY = "USER_SIGNED_IN";
    private static final String LAST_DEVICE_NAME_USED_FOR_TESTING_KEY = "LAST_PASSWORD_USED_FOR_TESTING";
    private static final String LATEST_RECORDING_FILE_NAME_KEY = "LATEST_RECORDING_FILE_NAME";


    public Prefs(Context context) {
        this.context = context;
    }

    public static long getDeviceTokenTimeoutSeconds() {
        return DEVICE_TOKEN_TIMEOUT_SECONDS;
    }


    private String getString(String key) {
        if (context == null) {
            Log.e(TAG, "Context was null when trying to get preferences.");
            return null;
        } else {
            SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return preferences.getString(key, null);
        }
    }

    private void setString(String key, String val) {
        if (context == null) {
            Log.e(TAG, "Context was null when trying to get preferences.");
            return;
        }
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(key, val).apply();
    }

    private double getDouble(String key) {
        if (context == null) {
            Log.e(TAG, "Context was null when trying to get preferences.");
            return 0;
        }
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return Double.longBitsToDouble(preferences.getLong(key, 0));
    }

    private long getLong(String key) {
        if (context == null) {
            Log.e(TAG, "Context was null when trying to get preferences.");
            return 0;
        }
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getLong(key, 0);
    }

    private void setDouble(String key, double val) {
        if (context == null) {
            Log.e(TAG, "Context was null when trying to get preferences.");
            return;
        }
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putLong(key, Double.doubleToRawLongBits(val)).apply();
    }

    private void setLong(String key, long val) {
        if (context == null) {
            Log.e(TAG, "Context was null when trying to get preferences.");
            return;
        }
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putLong(key, val).apply();
    }

    private boolean getBoolean(String key) {
        if (context == null) {
            Log.e(TAG, "Context was null when trying to get preferences.");
            return false;
        }
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(key, false);
    }

    private boolean getBooleanDefaultTrue() {  // used to determine first time app runs after install
        if (context == null) {
            Log.e(TAG, "Context was null when trying to get preferences.");
            return false;
        }
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(Prefs.FIRST_TIME_KEY, true);
    }


    private void setBoolean(String key, boolean val) {
        if (context == null) {
            Log.e(TAG, "Context was null when trying to get preferences.");
            return;
        }
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(key, val).apply();
    }

    String getBrowseRecordingsServerUrl() {
        if (getBoolean(USE_TEST_SERVER_KEY)) {
            return TEST_CACOPHONY_PROJECT_WEBSITE_BROWSE_RECORDINGS;
        } else {
            return PRODUCTION_CACOPHONY_PROJECT_WEBSITE_BROWSE_RECORDINGS;
        }
    }

    String getServerUrl() {
        if (getBoolean(USE_TEST_SERVER_KEY)) {
            return SCHEME + "://" + TEST_SERVER_HOST;
        } else {
            return SCHEME + "://" + PRODUCTION_SERVER_HOST;
        }
    }

    String getServerScheme() {
        return SCHEME;
    }

    String getServerHost() {
        if (getBoolean(USE_TEST_SERVER_KEY)) {
            return TEST_SERVER_HOST;
        } else {
            return PRODUCTION_SERVER_HOST;
        }
    }

    String getDevicePassword() {
        return getString(DEVICE_PASSWORD_KEY);
    }


    void setDevicePassword(String devicePassword) {
        setString(DEVICE_PASSWORD_KEY, devicePassword);
    }

    public String getDeviceName() {
        return getString(DEVICE_NAME_KEY);
    }

    String getUsernamePassword() {
        return getString(USERNAME_PASSWORD_KEY);
    }

    void setUsernamePassword(String usernamePassword) {
        setString(USERNAME_PASSWORD_KEY, usernamePassword);
    }

    void setUsername(String username) {
        setString(USERNAME_KEY, username);
    }

    String getUsername() {
        return getString(USERNAME_KEY);
    }

    String getEmailAddress() {
        return getString(EMAIL_ADDRESS_KEY);
    }

    void setUserNameOrEmailAddress(String userNameOrEmailAddress) {
        setString(USERNAME_OR_EMAIL_ADDRESS_KEY, userNameOrEmailAddress);
    }


    String getUserNameOrEmailAddress() {
        return getString(USERNAME_OR_EMAIL_ADDRESS_KEY);
    }

    void setEmailAddress(String emailAddress) {
        setString(EMAIL_ADDRESS_KEY, emailAddress);
    }

    void setDeviceName(String name) {
        setString(DEVICE_NAME_KEY, name);
    }

    void setDeviceToken(String deviceToken) {
        setString(DEVICE_TOKEN_KEY, deviceToken);
    }

    void setUserToken(String userToken) {
        setString(USER_TOKEN_KEY, userToken);
    }

    String getUserToken() {
        return getString(USER_TOKEN_KEY);
    }


    void setTheNextSingleStandardAlarmUsingUnixTime(long nextHourlyAlarmInUnixTime) {
        setLong(NEXT_ALARM_KEY, nextHourlyAlarmInUnixTime);
    }

    String getDawnDuskAlarms() {
        return getString(DAWN_DUSK_ALARMS_KEY);
    }

    void saveDawnDuskAlarms(String dawnDuskAlarms) {
        setString(DAWN_DUSK_ALARMS_KEY, dawnDuskAlarms);
    }

    void deleteDawnDuskAlarmList() {
        setString(DAWN_DUSK_ALARMS_KEY, null);
    }

    String getAlarmString() {
        return getString(DAWN_DUSK_ALARMS_KEY);
    }

    long getNextSingleStandardAlarm() {
        return getLong(NEXT_ALARM_KEY);
    }

    String getToken() {
        return getString(DEVICE_TOKEN_KEY);
    }

    long getTokenLastRefreshed() {
        return getLong(DEVICE_TOKEN_LAST_REFRESHED_KEY);
    }

    void setTokenLastRefreshed(long timeTokenLastRefreshed) {
        setLong(DEVICE_TOKEN_LAST_REFRESHED_KEY, timeTokenLastRefreshed);
    }

    void setUserTokenLastRefreshed(long timeUserTokenLastRefreshed) {
        setLong(USER_TOKEN_LAST_REFRESHED_KEY, timeUserTokenLastRefreshed);
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

    double getAdjustedTimeBetweenRecordingsSeconds() {
        if (getBoolean(USE_VERY_FREQUENT_RECORDINGS_KEY)) {
            return getDouble(TIME_BETWEEN_VERY_FREQUENT_RECORDINGS_SECONDS_KEY);
        } else if (getBoolean(USE_FREQUENT_RECORDINGS_KEY)) {
            return getDouble(TIME_BETWEEN_FREQUENT_RECORDINGS_SECONDS_KEY);
        } else {
            return getDouble(NORMAL_TIME_BETWEEN_RECORDINGS_SECONDS_KEY);
        }
    }

    void setNormalTimeBetweenRecordingsSeconds() {
        setDouble(NORMAL_TIME_BETWEEN_RECORDINGS_SECONDS_KEY, NORMAL_TIME_BETWEEN_RECORDINGS_SECONDS);
    }

    void setTimeBetweenVeryFrequentRecordingsSeconds() {
        setDouble(TIME_BETWEEN_VERY_FREQUENT_RECORDINGS_SECONDS_KEY, TIME_BETWEEN_VERY_FREQUENT_RECORDINGS_SECONDS);
    }

    double getTimeBetweenVeryFrequentRecordingsSeconds() {
        return getDouble(TIME_BETWEEN_VERY_FREQUENT_RECORDINGS_SECONDS_KEY);
    }

    void setTimeBetweenGPSLocationUpdatesSeconds() {
        setDouble(TIME_BETWEEN_GPS_LOCATION_UPDATES_SECONDS_KEY, TIME_BETWEEN_GPS_LOCATION_UPDATES_SECONDS);
    }

    void setTimeBetweenFrequentRecordingsSeconds() {
        setDouble(TIME_BETWEEN_FREQUENT_RECORDINGS_SECONDS_KEY, TIME_BETWEEN_FREQUENT_RECORDINGS_SECONDS);
    }

    double getTimeBetweenUploadsSeconds() {
        if (getBoolean(USE_FREQUENT_UPLOADS_KEY)) {
            return getDouble(TIME_BETWEEN_FREQUENT_UPLOADS_SECONDS_KEY);
        } else {
            return getDouble(TIME_BETWEEN_UPLOADS_SECONDS_KEY);
        }
    }

    void setTimeBetweenUploadsSeconds() {
        setDouble(TIME_BETWEEN_UPLOADS_SECONDS_KEY, TIME_BETWEEN_UPLOADS_SECONDS);
    }

    void setTimeBetweenFrequentUploadsSeconds() {
        setDouble(TIME_BETWEEN_FREQUENT_UPLOADS_SECONDS_KEY, TIME_BETWEEN_FREQUENT_UPLOADS_SECONDS);
    }

    double getBatteryLevelCutoffRepeatingRecordings() {
        if (getBoolean(IGNORE_LOW_BATTERY_KEY)) {
            return getDouble(IGNORE_BATTERY_LEVEL_CUTOFF_REPEATING_RECORDINGS_KEY);
        } else {
            return getDouble(BATTERY_LEVEL_CUTOFF_REPEATING_RECORDINGS_KEY);
        }
    }

    double getBatteryLevelCutoffDawnDuskRecordings() {
        if (getBoolean(IGNORE_LOW_BATTERY_KEY)) {
            return getDouble(IGNORE_BATTERY_LEVEL_CUTOFF_DAWN_DUSK_RECORDINGS_KEY);
        } else {
            return getDouble(BATTERY_LEVEL_CUTOFF_DAWN_DUSK_RECORDINGS_KEY);
        }
    }

    void setBatteryLevelCutoffRepeatingRecordings() {
        setDouble(BATTERY_LEVEL_CUTOFF_REPEATING_RECORDINGS_KEY, BATTERY_LEVEL_CUTOFF_REPEATING_RECORDINGS);
    }

    void setBatteryLevelCutoffDawnDuskRecordings() {
        setDouble(BATTERY_LEVEL_CUTOFF_DAWN_DUSK_RECORDINGS_KEY, BATTERY_LEVEL_CUTOFF_DAWN_DUSK_RECORDINGS);
    }

    double getDawnDuskOffsetMinutes() {
        return getDouble(DAWN_DUSK_OFFSET_MINUTES_KEY);
    }

    void setDawnDuskOffsetMinutes() {
        setDouble(DAWN_DUSK_OFFSET_MINUTES_KEY, DAWN_DUSK_OFFSET_MINUTES);
    }

    double getDawnDuskIncrementMinutes() {
        return getDouble(DAWN_DUSK_INCREMENT_MINUTES_KEY);
    }

    void setDawnDuskIncrementMinutes() {
        setDouble(DAWN_DUSK_INCREMENT_MINUTES_KEY, DAWN_DUSK_INCREMENT_MINUTES);
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

    void setHasRootAccess(boolean hasRootAccess) {
        setBoolean(HAS_ROOT_ACCESS_KEY, hasRootAccess);
    }

    boolean getUseShortRecordings() {
        return getBoolean(USE_SHORT_RECORDINGS_KEY);
    }

    boolean getPeriodicallyUpdateGPS() {
        return getBoolean(PERIODICALLY_UPDATE_GPS_KEY);
    }

    boolean getUseTestServer() {
        return getBoolean(USE_TEST_SERVER_KEY);
    }

    boolean getUseVeryFrequentRecordings() {
        return getBoolean(USE_VERY_FREQUENT_RECORDINGS_KEY);
    }

    boolean getUseFrequentRecordings() {
        return getBoolean(USE_FREQUENT_RECORDINGS_KEY);
    }

    boolean getUseFrequentUploads() {
        return getBoolean(USE_FREQUENT_UPLOADS_KEY);
    }

    boolean getIgnoreLowBattery() {
        return getBoolean(IGNORE_LOW_BATTERY_KEY);
    }

    boolean getOnLineMode() {
        return getBoolean(ONLINE_MODE_KEY);
    }

    boolean getPlayWarningSound() {
        return getBoolean(PLAY_WARNING_SOUND_KEY);
    }

    void setUseShortRecordings(boolean useShortRecordings) {
        setBoolean(USE_SHORT_RECORDINGS_KEY, useShortRecordings);
    }

    void setPeriodicallyUpdateGPS(boolean PeriodicallyUpdateGPS) {
        setBoolean(PERIODICALLY_UPDATE_GPS_KEY, PeriodicallyUpdateGPS);
    }

    void setUseTestServer(boolean useTestServer) {
        setBoolean(USE_TEST_SERVER_KEY, useTestServer);
    }

    void setUseVeryFrequentRecordings(boolean useVeryFrequentRecordings) {
        setBoolean(USE_VERY_FREQUENT_RECORDINGS_KEY, useVeryFrequentRecordings);
    }

    void setUseFrequentRecordings(boolean useFrequentRecordings) {
        setBoolean(USE_FREQUENT_RECORDINGS_KEY, useFrequentRecordings);
    }

    void setUseFrequentUploads(boolean useFrequentUploads) {
        setBoolean(USE_FREQUENT_UPLOADS_KEY, useFrequentUploads);
    }

    void setIgnoreLowBattery(boolean ignoreLowBattery) {
        setBoolean(IGNORE_LOW_BATTERY_KEY, ignoreLowBattery);
    }

    void setPlayWarningSound(boolean playWarningSound) {
        setBoolean(PLAY_WARNING_SOUND_KEY, playWarningSound);
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

    void setDateTimeLastRepeatingAlarmFiredToZero() {
        setLong(DATE_TIME_LAST_REPEATING_ALARM_FIRED_KEY, (long) 0);
    }

    void setLastRecordIdReturnedFromServer(long lastRecordingIdReturnedFromServer) {
        setLong(LAST_RECORDING_ID_RETURNED_FROM_SERVER, lastRecordingIdReturnedFromServer);
    }

    long getLastRecordIdReturnedFromServer() {
        return getLong(LAST_RECORDING_ID_RETURNED_FROM_SERVER);
    }

    boolean getIsFirstTime() {
        return getBooleanDefaultTrue();
    }

    void setIsFirstTimeFalse() {
        setBoolean(FIRST_TIME_KEY, false);
    }

    long getTimeThatLastRecordingHappened() {
        return getLong(LAST_RECORDING_TIME_KEY);
    }

    void setTimeThatLastRecordingHappened(long lastRecordingTime) {
        setLong(LAST_RECORDING_TIME_KEY, lastRecordingTime);
    }

    String getInternetConnectionMode() {
        return getString(INTERNET_CONNECTION_MODE_KEY);
    }

    void setInternetConnectionMode(String internetConnectionMode) {
        setString(INTERNET_CONNECTION_MODE_KEY, internetConnectionMode);
    }

    String getAudioSource() {
        String audioSource = getString(AUDIO_SOURCE_KEY);
        if (audioSource == null) {
            audioSource = "MIC";
        }
        return audioSource;
    }

    void setAudioSource(String audioSource) {
        setString(AUDIO_SOURCE_KEY, audioSource);
    }

    String getBirdCountDuration() {
        String birdCountDuration = getString(BIRD_COUNT_DURATION_KEY);
        if (birdCountDuration == null) {
            birdCountDuration = "fiveMinute";
        }
        return birdCountDuration;
    }

    void setBirdCountDuration(String birdCountDuration) {
        setString(BIRD_COUNT_DURATION_KEY, birdCountDuration);
    }

    void setIsDisabled(boolean isDisabled) {
        setBoolean(DISABLED_KEY, isDisabled);
    }

    boolean getIsDisabled() {
        return getBoolean(DISABLED_KEY);
    }

    void setIsDisableDawnDuskRecordings(boolean isDisabled) {
        setBoolean(DISABLED_DAWN_DUSK_RECORDINGS_KEY, isDisabled);
    }

    boolean getIsDisableDawnDuskRecordings() {
        return getBoolean(DISABLED_DAWN_DUSK_RECORDINGS_KEY);
    }

    void setSettingsForTestServerEnabled(boolean isEnabled) {
        setBoolean(SETTINGS_FOR_TEST_SERVER_ENABLED_KEY, isEnabled);
    }

    boolean getSettingsForTestServerEnabled() {
        return getBoolean(SETTINGS_FOR_TEST_SERVER_ENABLED_KEY);
    }

    void setGroups(String groups) {
        setString(GROUPS_KEY, groups);
    }

    String getGroups() {
        return getString(GROUPS_KEY);
    }

    void setUserSignedIn(boolean userSignedIn) {
        setBoolean(USER_SIGNED_IN_KEY, userSignedIn);
    }

    boolean getUserSignedIn() {
        return getBoolean(USER_SIGNED_IN_KEY);
    }

    void setLastDeviceNameUsedForTesting(String lastDeviceNameUsedForTesting) {
        setString(LAST_DEVICE_NAME_USED_FOR_TESTING_KEY, lastDeviceNameUsedForTesting);
    }

    boolean getCancelRecording() {
        return getBoolean(CANCEL_RECORDING_ACCESS_KEY);
    }

    void setCancelRecording(boolean cancelRecording) {
        setBoolean(CANCEL_RECORDING_ACCESS_KEY, cancelRecording);
    }

    void setLatestBirdCountRecordingFileName(String latestBirdCountRecordingFileName){
        setString(LATEST_RECORDING_FILE_NAME_KEY, latestBirdCountRecordingFileName);
    }

    String getLatestRecordingFileName(){
        return getString(LATEST_RECORDING_FILE_NAME_KEY);
    }
}
