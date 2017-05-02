package nz.org.cacophonoy.cacophonometerlite;


import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;

import static android.R.attr.enabled;
import static android.R.attr.level;
import static android.content.ContentValues.TAG;
import static android.media.CamcorderProfile.get;
import static java.lang.Float.parseFloat;

class Util {
    private static final String LOG_TAG = Util.class.getName();

    private static File homeFile = null;
    private static File recordingFolder = null;
    private static final String DEFAULT_RECORDINGS_FOLDER = "recordings";

    // For airplane mode
    private final static String COMMAND_FLIGHT_MODE_1 = "settings put global airplane_mode_on";
    private final static String COMMAND_FLIGHT_MODE_2 = "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state";

    private final static String COMMAND_FLIGHT_MODE_3 = "settings put global airplane_mode_on";

    static boolean checkPermissionsForRecording(Context context) {
        Log.d(LOG_TAG, "Checking permissions needed for recording.");
        if (context == null) {
            Log.e(LOG_TAG, "Context was null when checking permissions");
            return false;
        }
        boolean storagePermission =
                ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean microphonePermission =
                ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        boolean locationPermission =
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        return (storagePermission && microphonePermission && locationPermission);
    }

    static File getHomeFile() {
        // 15/8/16 Tim Hunt - Going to change file storage location to always use internal phone storage rather than rely on sdcard
        // This is because if sdcard is 'dodgy' can get inconsistent results.
        // Need context to get internal storage location, so need to pass this around the code.
        // If I could be sure the homeFile was set before any of the other directories are needed then I
        // wouldn't need to pass context around to the other methods :-(

        if (homeFile == null) {
            homeFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/cacophony");
            //https://developer.android.com/reference/android/content/Context.html#getDir(java.lang.String, int)
            // homeFile = context.getDir("cacophony", Context.MODE_PRIVATE); // getDir creates the folder if it doesn't exist, but needs contect

            if (!homeFile.exists() && !homeFile.isDirectory() && !homeFile.mkdirs()) {
                Log.e(LOG_TAG, "HomeFile location problem");

                //TODO, exit program safely from here and display error.
            }

        }
        return homeFile;
    }

    static File getRecordingsFolder() {
        if (recordingFolder == null) {
            recordingFolder = new File(getHomeFile(), DEFAULT_RECORDINGS_FOLDER);
            if (!recordingFolder.exists() && !recordingFolder.isDirectory() && !recordingFolder.mkdirs()) {
                Log.e(LOG_TAG, "Recording location problem");
                //TODO try to fix problem and if cant output error message then exit, maybe send error to server.
            }
        }
        return recordingFolder;
    }

    public static String getDeviceID(String webToken) throws Exception {
        String webTokenBody = Util.decoded(webToken);
        JSONObject jObject = new JSONObject(webTokenBody);
        return jObject.getString("id");
    }

    public static String decoded(String JWTEncoded) throws Exception {
        // http://stackoverflow.com/questions/37695877/how-can-i-decode-jwt-token-in-android#38751017
        String webTokenBody = null;
        try {
            String[] split = JWTEncoded.split("\\.");
            Log.d("JWT_DECODED", "Header: " + getJson(split[0]));

            // Log.d("JWT_DECODED", "Body: " + getJson(split[1]));
            webTokenBody = getJson(split[1]);
        } catch (UnsupportedEncodingException e) {
            //Error
        }
        return webTokenBody;
    }

    private static String getJson(String strEncoded) throws UnsupportedEncodingException {
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }
    public static double getBatteryLevel(Context context){
        double batteryLevel = -1;
        batteryLevel = getBatteryLevelUsingSystemFile(context);
        if (batteryLevel == -1){
            batteryLevel = getBatteryLevelByIntent(context);

        }
        return batteryLevel;
    }
    public static double getBatteryLevelUsingSystemFile(Context context) {
//        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
// //       IntentFilter ifilter = new IntentFilter(Intent.ACTION_TIME_TICK);
//        Intent batteryStatus = context.getApplicationContext().registerReceiver(null, ifilter);
//        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
//        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
//
//        float batteryPct = level / (float) scale;
        // https://longtrieuquang.wordpress.com/2013/04/08/android-battery-information-from-file-system/
        // found the file volt that stores battery voltage
        String batteryLevelStr = null;
        double batteryLevel = -1;
        File voltFile = new File("/sys/class/power_supply/battery/volt");
        if (voltFile.exists()) {
            try {
                batteryLevelStr = getStringFromFile(context, "/sys/class/power_supply/battery/volt");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (batteryLevelStr != null) {
            try {
                batteryLevel = Double.parseDouble(batteryLevelStr);
            } catch (Exception ex) {

            }

        }

        return batteryLevel;
    }

    public static double getBatteryLevelByIntent(Context context){

        //Will use the method of checking battery level that may only give an update if phone charging status changes
        // this will return a percentage
        double batteryLevel = -1;
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.getApplicationContext().registerReceiver(null, ifilter);
        batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        return batteryLevel;
    }

    public static String getStringFromFile(Context context, String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(context, fin);

        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    public static String convertStreamToString(Context context, InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;

//        while ((line = reader.readLine()) != null) {
//            sb.append(line).append("\n");
//            Toast.makeText(context, "Battery voltage is " + line, Toast.LENGTH_LONG).show();
//
//        }

        line = reader.readLine();
        // Toast.makeText(context, "Battery voltage is " + line, Toast.LENGTH_LONG).show();


        reader.close();
        return line;
    }

    public static String getBatteryStatus(Context context) {
        // https://developer.android.com/training/monitoring-device-state/battery-monitoring.html
        // http://stackoverflow.com/questions/24934260/intentreceiver-components-are-not-allowed-to-register-to-receive-intents-when
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.getApplicationContext().registerReceiver(null, ifilter);
        // Are we charging / charged?
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        String batteryStatusToReturn;
        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                batteryStatusToReturn = "CHARGING";
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                batteryStatusToReturn = "DISCHARGING";
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                batteryStatusToReturn = "NOT_CHARGING";
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                batteryStatusToReturn = "FULL";
                break;
            case BatteryManager.BATTERY_STATUS_UNKNOWN:
                batteryStatusToReturn = "UNKNOWN";
                break;
            default:
                batteryStatusToReturn = Integer.toString(status);
        }
        return batteryStatusToReturn;


    }

    /**
     * Gets the state of Airplane Mode.
     * http://stackoverflow.com/questions/4319212/how-can-one-detect-airplane-mode-on-android
     *
     * @param context
     * @return true if enabled.
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isAirplaneModeOn(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }





    /**
     * Returns the sunrise time for the current device location
     *
     * @param context - for getting the location
     * @return Calendar time of the sunrise
     */
    public static Calendar getSunrise(Context context, Calendar todayOrTomorrow) {

        Location location = getLocation(context);
        SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, "Pacific/Auckland");
        Calendar officialSunrise = calculator.getOfficialSunriseCalendarForDate(todayOrTomorrow);
        //Log.d("DEBUG: ", "Sunrise time is: " + officialSunrise);
        return officialSunrise;
    }

    public static Calendar getDawn(Context context, Calendar todayOrTomorrow) {
        Prefs prefs = new Prefs(context);
        Calendar sunRise = getSunrise(context, todayOrTomorrow);
        Calendar dawn = (Calendar) sunRise.clone();
        int lengthOfTwilight = (int) prefs.getLengthOfTwilightSeconds();
        dawn.add(Calendar.SECOND, -lengthOfTwilight);
        return dawn;
    }

    /**
     * Returns the sunset time for the current device location
     *
     * @param context - for getting the location
     * @return Calendar time of the sunset
     */
    public static Calendar getSunset(Context context, Calendar todayOrTomorrow) {

        Location location = getLocation(context);
        SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, "Pacific/Auckland");
        Calendar officialSunset = calculator.getOfficialSunsetCalendarForDate(todayOrTomorrow);

        // Log.d("DEBUG: ", "Sunset time is: " + officialSunset);
        return officialSunset;
    }

    public static Calendar getDusk(Context context, Calendar todayOrTomorrow) {
        Prefs prefs = new Prefs(context);
        Calendar sunSet = getSunset(context, todayOrTomorrow);
        Calendar dusk = (Calendar) sunSet.clone();
        int lengthOfTwilight = (int) prefs.getLengthOfTwilightSeconds();
        dusk.add(Calendar.SECOND, lengthOfTwilight);
        return dusk;
    }

    private static Location getLocation(Context context) {
        Prefs prefs = new Prefs(context);
        String lat = null;
        String lon = null;

        if (prefs.getLatitude() == 0.0 && prefs.getLongitude() == 0.0) {
            // gps not yet set, so to avoid errors/too complex code to check, just use coordinates for Hamilton NZ
            lat = Double.toString(-37.805294);
            lon = Double.toString(175.306775);

        } else {
            lat = Double.toString(prefs.getLatitude());
            lon = Double.toString(prefs.getLongitude());
        }


        return new Location(lat, lon);
    }

//    public static boolean enableAirplaneMode(Context context) {
//
//        // This will not work (and causes a crash) for Android 4.2 and above
//        if (Build.VERSION.SDK_INT > 16){  // The last version that allows airplane mode switching is Android 4.1 (API 16)
//            // but can still enable if rooted
////            Prefs prefs = new Prefs(context);
////            if (prefs.getHasRootAccess()){
////               return enableAirplaneModeRooted(context);
////            }
//            return false;
//        }
//
//        boolean isEnabled = Settings.System.getInt(
//                context.getContentResolver(),
//                Settings.System.AIRPLANE_MODE_ON, 0) == 1;
//
//        if (!isEnabled){
//
//            Settings.System.putInt(
//                    context.getContentResolver(),
//                    Settings.System.AIRPLANE_MODE_ON, isEnabled ? 0 : 1);
//
//// Post an intent to reload
//            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
//            intent.putExtra("state", !isEnabled);
//            context.sendBroadcast(intent);
//        }
//
//        while (isNetworkConnected(context)) {
//            try {
//                Thread.sleep(500); // give time for airplane mode to turn on
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        return true;
//    }

//    public static boolean disableAirplaneMode(Context context) {
//
//        // This will not work (and causes a crash) for Android 4.2 and above
//        if (Build.VERSION.SDK_INT > 16){  // The last version that allows airplane mode switching is Android 4.1 (API 16)
//            Prefs prefs = new Prefs(context);
////            if (prefs.getHasRootAccess()){
////                return disableAirplaneModeRooted(context);
////            }
//            return false;
//        }
//
//
//        boolean isEnabled = Settings.System.getInt(
//                context.getContentResolver(),
//                Settings.System.AIRPLANE_MODE_ON, 0) == 1;
//
//        if (isEnabled){
//
//            Settings.System.putInt(
//                    context.getContentResolver(),
//                    Settings.System.AIRPLANE_MODE_ON, isEnabled ? 0 : 1);
//
//// Post an intent to reload
//            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
//            intent.putExtra("state", !isEnabled);
//            context.sendBroadcast(intent);
//        }
//
//        int numberOfLoops = 0;
//        while (!isNetworkConnected(context)) {
//
//            try {
//                Thread.sleep(500); // give time for airplane mode to turn off
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            numberOfLoops+=1;
//            if (numberOfLoops > 20){
//                break;
//            }
//        }
//        if (numberOfLoops > 20){
//            return false;
//        }
//        return true;
//    }

//    public static boolean isSimPresent(Context context) {
//        // https://sites.google.com/site/androidhowto/how-to-1/check-if-sim-card-exists-in-the-phone
//        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        int simState = tm.getSimState();
//
//        // int state 5
//        return simState == TelephonyManager.SIM_STATE_READY;
//    }

    public static boolean waitForNetworkConnection(Context context, boolean networkConnectionRequired){
        int numberOfLoops = 0;
//        while (!isNetworkConnected(context)) {
        while (isNetworkConnected(context) != networkConnectionRequired ) {

            try {
                Thread.sleep(500); // give time for airplane mode to turn off
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            numberOfLoops+=1;
            if (numberOfLoops > 20){
                Log.e(LOG_TAG, "Number of loops > 20");
                break;
            }
        }
        if (numberOfLoops > 20){
            return false;
        }
        return true;
    }


    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

//    public static boolean canReachServer(){
//
//    }

//public static boolean enableAirplaneModeRooted(Context context){
//    // http://stackoverflow.com/questions/25674655/how-to-turn-on-off-airplane-mode-even-on-new-android-versions-and-even-with-ro
//    int enabled = isFlightModeEnabled(context) ? 0 : 1;
//    // Set Airplane / Flight mode using su commands.
//    String command = COMMAND_FLIGHT_MODE_1 + " " + enabled;
//    executeCommandWithoutWait(context, "-c", command);
//    command = COMMAND_FLIGHT_MODE_2 + " " + enabled;
//    executeCommandWithoutWait(context, "-c", command);
//
//
//
//    return true;
//}

    public static boolean setFlightMode(Context context, boolean enable) { // if enable is true, then this means turn on flight mode ie turn off network and save power

       boolean isCurrentlyInFlightMode = isFlightModeEnabled(context);

        // will I continue - depends on if enable is true or false and if already in flightmode or not
        // so write logic so it exits method if need be

        if (isCurrentlyInFlightMode){
            Log.d(LOG_TAG, "Currently in flight mode");
            if (enable){ // ie want to turn on flight mode, but it is already on
                Log.d(LOG_TAG, "And have been asked to enable flight mode so nothing to do");
                return true;
            }
        }else {
            Log.d(LOG_TAG, "Not currently in flight mode");
            if (!enable){ // ie want to turn on flight mode, but it is already on
                Log.d(LOG_TAG, "And have been asked to disable flight mode so nothing to do");
                return true;
            }
        }






//        if (isFlightModeEnabled(context) == enable){ // if enable is true and it is already in flight mode then method will return without doing anything and vice versa
//            return true;
//        }


        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            // API 17 onwards.
            Log.d(LOG_TAG, "Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN");
            // Must be a rooted device
            Prefs prefs = new Prefs(context);
            if (!prefs.getHasRootAccess()){
                return false;
            }

            int enabled = isFlightModeEnabled(context) ? 0 : 1;

            // Set Airplane / Flight mode using su commands.
            String command = COMMAND_FLIGHT_MODE_1 + " " + enabled;
            executeCommandWithoutWait(context, "-c", command);
            command = COMMAND_FLIGHT_MODE_2 + " " + enabled;
            executeCommandWithoutWait(context, "-c", command);

        } else {
            // API 16 and earlier.
            Log.d(LOG_TAG, "API 16 and earlier.");
            boolean enabled = isFlightModeEnabled(context);
            Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, enabled ? 0 : 1);
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.putExtra("state", !enabled);
            context.sendBroadcast(intent);
        }
        return true;
    }
    public static void disableFlightMode(Context context) { // if enable is true, then this means turn on flight mode ie turn off network and save power

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            // API 17 onwards.
            Log.d(LOG_TAG, "Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN");
            // Must be a rooted device
            Prefs prefs = new Prefs(context);
            if (!prefs.getHasRootAccess()){
                Log.e(LOG_TAG, "Do NOT have required ROOT access");
                Toast.makeText(context, "Root access required to change airplane mode", Toast.LENGTH_LONG).show();
                return ;
            }


            // Set Airplane / Flight mode using su commands.
            String command = COMMAND_FLIGHT_MODE_1 + " " + "0";
            executeCommandWithoutWait(context, "-c", command);
            command = COMMAND_FLIGHT_MODE_2 + " " + "false";
            executeCommandWithoutWait(context, "-c", command);

        } else {
            // API 16 and earlier.
            Log.d(LOG_TAG, "API 16 and earlier.");
          //  boolean enabled = isFlightModeEnabled(context);
            Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.putExtra("state", false);
            context.sendBroadcast(intent);
        }
        return ;
    }

    public static void enableFlightMode(Context context) { // if enable is true, then this means turn on flight mode ie turn off network and save power

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            // API 17 onwards.
            Log.d(LOG_TAG, "Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN");
            // Must be a rooted device
            Prefs prefs = new Prefs(context);
            if (!prefs.getHasRootAccess()){
                Log.e(LOG_TAG, "Do NOT have required ROOT access");
                Toast.makeText(context, "Root access required to change airplane mode", Toast.LENGTH_LONG).show();
                return ;
            }


            // Set Airplane / Flight mode using su commands.
            String command = COMMAND_FLIGHT_MODE_1 + " " + "1";
            executeCommandWithoutWait(context, "-c", command);
            command = COMMAND_FLIGHT_MODE_2 + " " + "true";
            executeCommandWithoutWait(context, "-c", command);

        } else {
            // API 16 and earlier.
            Log.d(LOG_TAG, "API 16 and earlier.");
        //    boolean enabled = isFlightModeEnabled(context);
            Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 1);
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.putExtra("state", true);
            context.sendBroadcast(intent);
        }
        return ;
    }
//    public static boolean disableAirplaneModeRooted(Context context){
//        // http://stackoverflow.com/questions/25674655/how-to-turn-on-off-airplane-mode-even-on-new-android-versions-and-even-with-ro
//        int enabled = isFlightModeEnabled(context) ? 0 : 1;
//        // Set Airplane / Flight mode using su commands.
//        String command = COMMAND_FLIGHT_MODE_1 + " " + enabled;
//        executeCommandWithoutWait(context, "-c", command);
//        command = COMMAND_FLIGHT_MODE_2 + " " + enabled;
//        executeCommandWithoutWait(context, "-c", command);
//
//        return true;
//    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    private static boolean isFlightModeEnabled(Context context) {
        boolean mode = false;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            // API 17 onwards
            Log.d(LOG_TAG, "Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN");
//            mode = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
            mode = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            // API 16 and earlier.
            Log.d(LOG_TAG, "API 16 and earlier.");
//            mode = Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
            mode = Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        }
        Log.d(LOG_TAG, "Airplane mode is enabled " + mode);
        return mode;
    }

    private static void  executeCommandWithoutWait(Context context, String option, String command) {
        // http://muzso.hu/2014/04/02/how-to-programmatically-enable-and-disable-airplane-flight-mode-on-android-4.2
        boolean success = false;
        String su = "su";
        for (int i=0; i < 3; i++) {
            // "su" command executed successfully.
            if (success) {
                // Stop executing alternative su commands below.
                break;
            }
            if (i == 1) {
                su = "/system/xbin/su";
            } else if (i == 2) {
                su = "/system/bin/su";
            }
            try {
                // execute command
                Runtime.getRuntime().exec(new String[]{su, option, command});
            } catch (IOException e) {
                Log.e(LOG_TAG, "su command has failed due to: " + e.fillInStackTrace());
            }
        }
    }

}
