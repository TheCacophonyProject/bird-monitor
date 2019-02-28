package nz.org.cacophony.cacophonometer;


import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.luckycatlabs.SunriseSunsetCalculator;
import com.luckycatlabs.dto.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ch.qos.logback.classic.android.BasicLogcatConfigurator;

import static android.content.Context.ALARM_SERVICE;
import static nz.org.cacophony.cacophonometer.IdlingResourceForEspressoTesting.rootedIdlingResource;


/**
 *
 *
 * A rather large set of helper methods that are placed here to simplify other classes/methods.
 *
 * @author Tim Hunt
 */
class Util {
    private static final String TAG = Util.class.getName();

    private static final String DEFAULT_RECORDINGS_FOLDER = "recordings";

    static {
        BasicLogcatConfigurator.configureDefaultContext();
    }

    // For airplane mode
    private final static String COMMAND_FLIGHT_MODE_1 = "settings put global airplane_mode_on";
    private final static String COMMAND_FLIGHT_MODE_2 = "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state";

    /**
     * Make sure user has given permission to record.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static boolean checkPermissionsForRecording(Context context) {
        boolean permissionForRecording = false;
        try{
            if (context == null) {
                Log.e(TAG, "Context was null when checking permissions");

            }else{
                boolean storagePermission =
                        ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

                boolean microphonePermission =
                        ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
                boolean locationPermission =
                        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                permissionForRecording = (storagePermission && microphonePermission && locationPermission);
            }

        }catch (Exception ex){
            Log.e(TAG, "Error with checkPermissionsForRecording");
        }
      return permissionForRecording;

    }

    /**
     * Retrieve the location of where data is stored on the phone. Will automatically use the sdcard
     * if it is present.  This means for phones that do not have a internet connection, you just
     * have to insert an sdcard and all recordings will be on the card, making it possible to
     * retrieve the recordings by swapping out the card..
     *
     * @param context
     * @return Returns the location of where to store data on the phone
     */
    private static File getAppDataFolder(Context context) {

        // 15/8/16 Tim Hunt - Going to change file storage location to always use internal phone storage rather than rely on sdcard
        // This is because if sdcard is 'dodgy' can get inconsistent results.
        // Need context to get internal storage location, so need to pass this around the code.
        // If I could be sure the homeFile was set before any of the other directories are needed then I
        // wouldn't need to pass context around to the other methods :-(

        File appDataFolder = null;
        String sDCardAvailable = isRemovableSDCardAvailable( context);
        if (sDCardAvailable == null){
            Log.i(TAG, "No sd card detected");
        }else{
            Log.i(TAG, "sd card IS detected");
        }

        String canCreateFile = null;
        if (sDCardAvailable != null){
            canCreateFile = canCreateFile(sDCardAvailable);
        }

        //https://developer.android.com/reference/android/content/Context.html#getDir(java.lang.String, int)

        try {
            String appName = context.getResources().getString(R.string.activity_or_fragment_title_main);

            if (canCreateFile != null){
                // Use the sdcard
                appDataFolder = new File(canCreateFile + "/" + appName);
                return appDataFolder;
            }

            String externalStorageState = Environment.getExternalStorageState();
            if (externalStorageState.equalsIgnoreCase("mounted")){
                appDataFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + appName);
            }else{
                appDataFolder = context.getFilesDir();
            }

            if (appDataFolder != null) {
                if (!appDataFolder.exists()){
                    boolean appDataFolderCreated =   appDataFolder.mkdirs();
                    if (!appDataFolderCreated){
                        return null;
                    }
                    // check it got created
                    if (!appDataFolder.exists()){
                        return null;
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
        return appDataFolder;
    }

    /**
     * Returns the folder for storing recordings on the phone.
     * @param context
     * @return File object representing recordings folder.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
     public static File getRecordingsFolder(Context context) {

        File localFolderFile;
        try {
            File appDataFolder = getAppDataFolder(context);
            if (appDataFolder == null){

                return null;
            }

            localFolderFile = new File(appDataFolder, Util.DEFAULT_RECORDINGS_FOLDER);

                    if (!localFolderFile.exists()){
                        localFolderFile.mkdirs();

                        // now check it exists
                         if (!localFolderFile.exists()){
                            localFolderFile = null;
                        }
                    }

                    // now check it is there


            if (localFolderFile == null){
                Log.e(TAG, "There is a problem writing to the memory - please fix");
                getToast(context, "There is a problem writing to the memory - please fix", true).show();
            }

            return localFolderFile;
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
            getToast(context, "There is a problem writing to the memory - please fix", true).show();
            return null;
        }
    }


    /**
     * Returns the device id of this phone.  The device id has been allocated by the server when
     * the phone registers with the server, and is stored locally in a 'webtoken' string in the shared
     * preferences on this phone.
     * @param webToken
     * @return
     * @throws Exception
     */
    static String getDeviceID(String webToken) throws Exception {
        if (webToken == null){
            return "";
        }

        String webTokenBody = Util.decoded(webToken);
        JSONObject jObject = new JSONObject(webTokenBody);
        return jObject.getString("id");
    }

    /**
     * Extracts the webtoken from the actual token
     * @param JWTEncoded
     * @return
     */
    private static String decoded(String JWTEncoded) {
        // http://stackoverflow.com/questions/37695877/how-can-i-decode-jwt-token-in-android#38751017
        String webTokenBody = null;
        try {
            String[] split = JWTEncoded.split("\\.");
            webTokenBody = getJson(split[1]);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Error decoding JWT");
        }
        return webTokenBody;
    }

    private static String getJson(String strEncoded) throws UnsupportedEncodingException {
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }

    static double getBatteryLevel(Context context) {
        double batteryLevel;
        batteryLevel = getBatteryLevelUsingSystemFile();
        if (batteryLevel == -1) {
            batteryLevel = getBatteryLevelByIntent(context);

        }
        return batteryLevel;
    }

    static double getBatteryLevelUsingSystemFile() {

        // https://longtrieuquang.wordpress.com/2013/04/08/android-battery-information-from-file-system/
        // found the file volt that stores battery voltage
        String batteryLevelStr = null;
        double batteryLevel = -1;
        String voltFilePathName = "/sys/class/power_supply/battery/volt";
        File voltFile = new File(voltFilePathName);
        if (voltFile.exists()) {
            try {
                batteryLevelStr = getStringFromFile(voltFilePathName);
            } catch (Exception ex) {
                ex.printStackTrace();

                Log.e(TAG, ex.getLocalizedMessage());
            }
        }

        if (batteryLevelStr != null) {
            try {
                batteryLevel = Double.parseDouble(batteryLevelStr);
            } catch (Exception ex) {
                Log.e(TAG, "converting double");
            }
        }
        return batteryLevel;
    }

    static double getBatteryLevelByIntent(Context context) {

        //Will use the method of checking battery level that may only give an update if phone charging status changes
        // this will return a percentage
        try {
            double batteryLevel;
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.getApplicationContext().registerReceiver(null, ifilter);
            batteryLevel = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
            return batteryLevel;
        } catch (Exception ex) {
            Log.e(TAG, "Error with getBatteryLevelByIntent");
            return -1;
        }
    }

    private static String getStringFromFile(String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);

        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    private static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        line = reader.readLine();
        reader.close();
        return line;
    }

    static String getBatteryStatus(Context context) {
        // https://developer.android.com/training/monitoring-device-state/battery-monitoring.html
        // http://stackoverflow.com/questions/24934260/intentreceiver-components-are-not-allowed-to-register-to-receive-intents-when

        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.getApplicationContext().registerReceiver(null, ifilter);
            // Are we charging / charged?
            int status = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1) : -1;

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
        } catch (Exception ex) {
            Log.e(TAG, "getBatteryStatus");
            return "Error";
        }

    }

    /**
     * Gets the state of Airplane Mode.
     * http://stackoverflow.com/questions/4319212/how-can-one-detect-airplane-mode-on-android
     *
     * @return true if enabled.
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    static boolean isAirplaneModeOn(Context context) {
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
    private static Calendar getSunrise(Context context, Calendar todayOrTomorrow) {

        Location location = getLocation(context);
        SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, "Pacific/Auckland");
        return calculator.getOfficialSunriseCalendarForDate(todayOrTomorrow);
    }

    static Calendar getDawn(Context context, Calendar todayOrTomorrow) {
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
    private static Calendar getSunset(Context context, Calendar todayOrTomorrow) {

        Location location = getLocation(context);
        SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, "Pacific/Auckland");
        return calculator.getOfficialSunsetCalendarForDate(todayOrTomorrow);
    }

    /**
     * Returns the dusk time for the current location
     * @param context
     * @param todayOrTomorrow Calendar that represents either today or tomorrow
     * @return
     */
    static Calendar getDusk(Context context, Calendar todayOrTomorrow) {
        Prefs prefs = new Prefs(context);
        Calendar sunSet = getSunset(context, todayOrTomorrow);
        Calendar dusk = (Calendar) sunSet.clone();
        int lengthOfTwilight = (int) prefs.getLengthOfTwilightSeconds();
        dusk.add(Calendar.SECOND, lengthOfTwilight);
        return dusk;
    }

    private static Location getLocation(Context context) {
        Prefs prefs = new Prefs(context);
        String lat;
        String lon;

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


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static boolean waitForNetworkConnection(Context context, boolean networkConnectionRequired) {
        int numberOfLoops = 0;

        while (isNetworkConnected(context) != networkConnectionRequired) {

            try {
                Thread.sleep(1000); // give time for airplane mode to turn off
            } catch (InterruptedException ex) {
                Log.e(TAG,ex.getLocalizedMessage() );
            }

            numberOfLoops += 1;
            if (numberOfLoops > 60) {
                Log.e(TAG, "Number of loops > 60");
                break;
            }
        }
        //noinspection RedundantIfStatement
        if (numberOfLoops > 60) {
            return false;
        }
        return true;
    }


    static boolean isNetworkConnected(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null){
            Log.e(TAG, "cm is null");
            return false; // false may not be correct
        }

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        @SuppressWarnings("UnnecessaryLocalVariable") boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
//        if (!rootedIdlingResource.isIdleNow()){
//            rootedIdlingResource.decrement();
//        }
        return isConnected;
    }

    static void disableFlightMode(final Context context) {

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                        // API 17 onwards.
                        // Must be a rooted device
                        Prefs prefs = new Prefs(context);
                        if (!prefs.getHasRootAccess()) {  // don't try to disable flight mode if phone has been rooted.
                            return ;
                        }

                        if (prefs.getInternetConnectionMode().equalsIgnoreCase("offline")) {  // Don't try to turn on aerial if set to be offline
                            return ;
                        }

                        // Set Airplane / Flight mode using su commands.
                        String command = COMMAND_FLIGHT_MODE_1 + " " + "0";
                        executeCommandTim(context, command);
                        command = COMMAND_FLIGHT_MODE_2 + " " + "false";
                        executeCommandTim(context, command);

                    } else {
                        // API 16 and earlier.

                        //noinspection deprecation
                        Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);
                        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                        intent.putExtra("state", false);
                        context.sendBroadcast(intent);
                    }

                }catch (Exception ex){
                    Log.e(TAG, ex.getLocalizedMessage());
                }

            }
        };
       // rootedIdlingResource.increment(); // and decrement in isNetworkConnected method
        thread.start();

    }


    static void enableFlightMode(final Context context) {
        Prefs prefs = new Prefs(context);

        boolean onlineMode = prefs.getOnLineMode();


        if (onlineMode){
            return; // don't try to enable airplane mode
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) { // Jelly bean is 4.1

            // API 17 onwards.
            // Must be a rooted device

            if (!prefs.getHasRootAccess()) {
                Log.e(TAG, "Do NOT have required ROOT access");
                return;
            }
        }

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) { // Jelly bean is 4.1

                        // Set Airplane / Flight mode using su commands.
                        String command = COMMAND_FLIGHT_MODE_1 + " " + "1";
                        executeCommandTim(context, command);
                        command = COMMAND_FLIGHT_MODE_2 + " " + "true";
                        executeCommandTim(context, command);
                   //     rootedIdlingResource.decrement();

                    } else {

                        //noinspection deprecation
                        Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 1);
                        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                        intent.putExtra("state", true);
                        context.sendBroadcast(intent);
                    }

                }

                catch (Exception e) {
                            Log.e(TAG, "Error disabling flight mode");

                }

            }
        };

        thread.start();
    }



private static void executeCommandTim(Context context, String command){
    try {
        ExecuteAsRootBaseTim executeAsRootBaseTim = new ExecuteAsRootBaseTim();
        executeAsRootBaseTim.addCommand(command);
        executeAsRootBaseTim.execute(context);
    }catch (Exception ex){
        Log.e(TAG, ex.getLocalizedMessage());
        JSONObject jsonObjectMessageToBroadcast = new JSONObject();
        try {
            jsonObjectMessageToBroadcast.put("messageType", "error_do_not_have_root");
            jsonObjectMessageToBroadcast.put("messageToDisplay", "error_do_not_have_root");
            Util.broadcastAMessage(context, "ROOT", jsonObjectMessageToBroadcast);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}


    static String getSimStateAsString(int simState) {
        String simStateStr;
        switch (simState) {
            case 0:
                simStateStr = "SIM_STATE_UNKNOWN";
                break;
            case 1:
                simStateStr = "SIM_STATE_ABSENT";
                break;
            case 2:
                simStateStr = "SIM_STATE_PIN_REQUIRED";
                break;
            case 3:
                simStateStr = "SIM_STATE_PUK_REQUIRED";
                break;
            case 4:
                simStateStr = "SIM_STATE_NETWORK_LOCKED";
                break;
            case 5:
                simStateStr = "SIM_STATE_READY";
                break;
            case 6:
                simStateStr = "SIM_STATE_NOT_READY";
                break;
            case 7:
                simStateStr = "SIM_STATE_PERM_DISABLED";
                break;
            case 8:
                simStateStr = "SIM_STATE_CARD_IO_ERROR";
                break;
            case 9:
                simStateStr = "SIM_STATE_CARD_RESTRICTED";
                break;
            case 10:
                simStateStr = "October";
                break;
            case 11:
                simStateStr = "November";
                break;
            case 12:
                simStateStr = "December";
                break;
            default:
                simStateStr = "noMatch";
                break;
        }
        return simStateStr;
    }


    static Toast getToast(Context context, String message, boolean standOut){

        @SuppressLint("ShowToast") Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        if (standOut){
            toast.getView().setBackgroundColor(context.getResources().getColor(R.color.alert));
        }else{
            toast.getView().setBackgroundColor(context.getResources().getColor(R.color.green));
        }

        return toast;
    }

        static void broadcastAMessage(Context context, String action, JSONObject jsonStringMessage){
        // https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

        Intent intent = new Intent(action);
        intent.putExtra("jsonStringMessage", jsonStringMessage.toString());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

//https://stackoverflow.com/questions/5694933/find-an-external-sd-card-location/29107397#29107397
   private static String isRemovableSDCardAvailable(Context context) {
        final String FLAG = "mnt";
        final String SECONDARY_STORAGE = System.getenv("SECONDARY_STORAGE");
        final String EXTERNAL_STORAGE_DOCOMO = System.getenv("EXTERNAL_STORAGE_DOCOMO");
        final String EXTERNAL_SDCARD_STORAGE = System.getenv("EXTERNAL_SDCARD_STORAGE");
        final String EXTERNAL_SD_STORAGE = System.getenv("EXTERNAL_SD_STORAGE");
        final String EXTERNAL_STORAGE = System.getenv("EXTERNAL_STORAGE");

        @SuppressLint("UseSparseArrays") Map<Integer, String> listEnvironmentVariableStoreSDCardRootDirectory = new HashMap<>();
        listEnvironmentVariableStoreSDCardRootDirectory.put(0, SECONDARY_STORAGE);
        listEnvironmentVariableStoreSDCardRootDirectory.put(1, EXTERNAL_STORAGE_DOCOMO);
        listEnvironmentVariableStoreSDCardRootDirectory.put(2, EXTERNAL_SDCARD_STORAGE);
        listEnvironmentVariableStoreSDCardRootDirectory.put(3, EXTERNAL_SD_STORAGE);
        listEnvironmentVariableStoreSDCardRootDirectory.put(4, EXTERNAL_STORAGE);

        File externalStorageList[] = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            externalStorageList = context.getExternalFilesDirs(null);
        }
        String directory;
        int size = listEnvironmentVariableStoreSDCardRootDirectory.size();
        for (int i = 0; i < size; i++) {
            if (externalStorageList != null && externalStorageList.length > 1 && externalStorageList[1] != null)
                directory = externalStorageList[1].getAbsolutePath();
            else
                directory = listEnvironmentVariableStoreSDCardRootDirectory.get(i);

            directory = canCreateFile(directory);
            if (directory != null && directory.length() != 0) {
                if (i == size - 1) {
                    if (directory.contains(FLAG)) {
                        return directory;
                    } else {
                        return null;
                    }
                }

                return directory;
            }
        }
        return null;
    }


    private static String canCreateFile(String directory) {
        // https://stackoverflow.com/questions/5694933/find-an-external-sd-card-location
        final String FILE_DIR = directory + File.separator + "hoang.txt";
        File tempFile = null;
        try {
            tempFile = new File(FILE_DIR);
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(new byte[1024]);
            fos.flush();
            fos.close();

        } catch (Exception e) {
            return null;
        } finally {
            if (tempFile != null && tempFile.exists() && tempFile.isFile()) {
                //noinspection UnusedAssignment
                tempFile = null;
            }
        }
        return directory;
    }

    static boolean isWebTokenCurrent(Prefs prefs){
       if (prefs.getToken() == null){
           return false;
       }
       long currentTimeMilliSeconds = new Date().getTime();
       long tokenLastRefreshedMilliSeconds = prefs.getTokenLastRefreshed();
       long tokenTimeOutSeconds = Prefs.getDeviceTokenTimeoutSeconds();
       long tokenTimeOutMilliSeconds = tokenTimeOutSeconds * 1000;
       if ((currentTimeMilliSeconds-tokenLastRefreshedMilliSeconds) > tokenTimeOutMilliSeconds){
           prefs.setDeviceToken(null);
           Log.d(TAG, "Web token out of date and so set to null");
           return false;
       }else{
           return true;
       }
    }

    public static void createCreateAlarms(Context context){ // Because each alarm now creates the next one, need to have this fail safe to get them going again (it doesn't rely on a previous alarm)
        Intent myIntent = new Intent(context, StartRecordingReceiver.class);
        try {
            myIntent.putExtra("type","repeating");
            Uri timeUri; // // this will hopefully allow matching of intents so when adding a new one with new time it will replace this one
            timeUri = Uri.parse("createCreateAlarms"); // cf dawn dusk offsets created in DawnDuskAlarms
            myIntent.setData(timeUri);

        }catch (Exception e){
            Log.e(TAG, e.getLocalizedMessage());
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent,0);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(ALARM_SERVICE);
        if (alarmManager == null){
            Log.e(TAG, "alarmManager is null");
            return;
        }

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,AlarmManager.INTERVAL_FIFTEEN_MINUTES, AlarmManager.INTERVAL_DAY,pendingIntent);

    }

    /**
     * Creates Android OS alarms that when fired by the OS, create and send intents to the
     * StartRecordingReceiver class which in turn initiate a recording.
     *
     * This method creates the 'normal' alarms which fire on a regular interval through out the day
     * and night.  There are also dawn/dusk alarms for extra recordings either side of dawn and
     * dusk.
     *
     * There is also a method called createCreateAlarms that kick starts this method.
     *
     * @param context
     *     *
     */
    public static void createTheNextSingleStandardAlarm(Context context){ // Standard repeating as apposed to Dawn or Dusk
Prefs prefs = new Prefs(context);
        Intent myIntent = new Intent(context, StartRecordingReceiver.class);
    myIntent.putExtra("callingCode", "tim"); // for debugging

        try {
            myIntent.putExtra("type","repeating");
            Uri timeUri; // // this will hopefully allow matching of intents so when adding a new one with new time it will replace this one
            timeUri = Uri.parse("normal"); // cf dawn dusk offsets created in DawnDuskAlarms
            myIntent.setData(timeUri);

        }catch (Exception e){
            Log.e(TAG, e.getLocalizedMessage());
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent,0);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(ALARM_SERVICE);
    if (alarmManager == null){
        Log.e(TAG, "alarmManager is null");
        return;
    }
        long timeBetweenRecordingsSeconds  = (long)prefs.getAdjustedTimeBetweenRecordingsSeconds();
        long delay = 1000 * timeBetweenRecordingsSeconds ;

    long currentElapsedRealTime = SystemClock.elapsedRealtime();
    long startWindowTime = currentElapsedRealTime + delay;


    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) { // KitKat is 19
        // https://developer.android.com/reference/android/app/AlarmManager.html

        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, startWindowTime, pendingIntent);

    }else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){ //m is Marshmallow 23
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, startWindowTime, pendingIntent);
    }else  {// Marshmallow will go into Doze mode, so use setExactAndAllowWhileIdle to allow wakeup https://developer.android.com/reference/android/app/AlarmManager#setExactAndAllowWhileIdle(int,%20long,%20android.app.PendingIntent)
        alarmManager.setExactAndAllowWhileIdle (AlarmManager.ELAPSED_REALTIME_WAKEUP, startWindowTime, pendingIntent);

    }
        setTheNextSingleStandardAlarmUsingDelay(context, delay);
    }

    public static void setUpLocationUpdateAlarm(Context context){
    Prefs prefs = new Prefs(context);
        if (prefs.getPeriodicallyUpdateGPS()){
                    createLocationUpdateAlarm(context);
                }else{
                    deleteLocationUpdateAlarm(context);
                }
    }

    public static void createLocationUpdateAlarm(Context context){

        // When in walking mode, also set up alarm for periodically updating location
        Intent locationUpdateIntent = new Intent(context, LocationReceiver.class);

        PendingIntent pendingLocationUpdateIntent = PendingIntent.getBroadcast(context, 0, locationUpdateIntent,0);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(ALARM_SERVICE);
        if (alarmManager == null){
            Log.e(TAG, "alarmManager is null");
            return;
        }

        Prefs prefs = new Prefs(context);
        long timeBetweenVeryFrequentRecordingsSeconds = (long)prefs.getTimeBetweenVeryFrequentRecordingsSeconds();

        long delayBetweenLocationUpdates = 1000 * timeBetweenVeryFrequentRecordingsSeconds;
        long currentElapsedRealTime = SystemClock.elapsedRealtime();
        long startWindowTimeForLocationUpdate = currentElapsedRealTime + delayBetweenLocationUpdates;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) { // KitKat is 19
            // https://developer.android.com/reference/android/app/AlarmManager.html
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, startWindowTimeForLocationUpdate, pendingLocationUpdateIntent);
        }else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){ //m is Marshmallow 23
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, startWindowTimeForLocationUpdate, pendingLocationUpdateIntent);
        }else {// Marshmallow will go into Doze mode, so use setExactAndAllowWhileIdle to allow wakeup https://developer.android.com/reference/android/app/AlarmManager#setExactAndAllowWhileIdle(int,%20long,%20android.app.PendingIntent)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, startWindowTimeForLocationUpdate, pendingLocationUpdateIntent);
        }
    }

    private static void deleteLocationUpdateAlarm(Context context){
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(ALARM_SERVICE);
        if (alarmManager == null){
            Log.e(TAG, "alarmManager is null");
            return;
        }
        Intent locationUpdateIntent = new Intent(context, LocationReceiver.class);
        PendingIntent pendingLocationUpdateIntent = PendingIntent.getBroadcast(context, 0, locationUpdateIntent,0);

        alarmManager.cancel(pendingLocationUpdateIntent);
    }

    public static void updateGPSLocation(Context context){
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null){
            Log.e(TAG, "locationManager is null");
            return;
        }

        //https://stackoverflow.com/questions/36123431/gps-service-check-to-check-if-the-gps-is-enabled-or-disabled-on-device
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            String messageToDisplay = "";
            JSONObject jsonObjectMessageToBroadcast = new JSONObject();
            try {
                jsonObjectMessageToBroadcast.put("messageType", "GPS_UPDATE_FAILED");
                jsonObjectMessageToBroadcast.put("messageToDisplay", "Sorry, GPS is not enabled.  Please enable location/gps in the phone settings and try again.");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Util.broadcastAMessage(context, "GPS", jsonObjectMessageToBroadcast);
            return;
        }

        GPSLocationListener gpsLocationListener = new GPSLocationListener(context);
        try {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, gpsLocationListener, context.getMainLooper());

        } catch (SecurityException e) {
            Log.e(TAG, "Unable to get GPS location. Don't have required permissions.");

        }
    }

    static long[] getDawnDuskAlarmList(Context context) {
        Prefs prefs = new Prefs(context);
        String alarmsString = prefs.getAlarmString();
        if (alarmsString == null){
            return null;
        }
        String[] tempArray;

        /* delimiter */
        String delimiter = ",";

        /* given string will be split by the argument delimiter provided. */
        tempArray = alarmsString.split(delimiter);
        Arrays.sort(tempArray);

        long[] alarmTimes = new long [tempArray.length];
        for (int i = 0; i < tempArray.length; i++) {
            alarmTimes[i] =  Long.parseLong(tempArray[i]);
        }

        return alarmTimes;
    }

    static String getNextAlarm(Context context){
        Prefs prefs = new Prefs(context);
        long nextAlarm = prefs.getNextSingleStandardAlarm();
        long[] dawnDuskAlarms = getDawnDuskAlarmList(context);
        if (dawnDuskAlarms != null){
            Date now = new Date();
            for (long dawnDuskAlarm: dawnDuskAlarms) {
                if (dawnDuskAlarm < nextAlarm && dawnDuskAlarm > now.getTime()){
                    nextAlarm = dawnDuskAlarm;
                }
            }
        }
        return convertUnixTimeToString(nextAlarm);
    }

    static void addDawnDuskAlarm(Context context, long alarmInUnixTime){
        Prefs prefs = new Prefs(context);
        // First Ignore it if this time has already passed
        Date now = new Date();
        if (alarmInUnixTime < now.getTime()){
            return;
        }

        String alarmInUnixTimeStr = Long.toString(alarmInUnixTime);
        String currentAlarms = prefs.getDawnDuskAlarms();
        if (currentAlarms == null){
            currentAlarms = alarmInUnixTimeStr;
        }else {
            currentAlarms = currentAlarms + "," + alarmInUnixTimeStr;
        }
        prefs.saveDawnDuskAlarms(currentAlarms);
    }

    static void setTheNextSingleStandardAlarmUsingDelay(Context context, long delayInMillisecs){
        Prefs prefs = new Prefs(context);
        // need to covert this delay into unix time
        Date date = new Date();
        long currentUnixTime = date.getTime();
        long nextHourlyAlarmInUnixTime = currentUnixTime + delayInMillisecs;
        prefs.setTheNextSingleStandardAlarmUsingUnixTime(nextHourlyAlarmInUnixTime);
    }

    static String getTimeThatLastRecordingHappened(Context context){
        Prefs prefs = new Prefs(context);
        long  lastRecordingTime = prefs.getTimeThatLastRecordingHappened();
        return convertUnixTimeToString(lastRecordingTime);
    }

    static void setTimeThatLastRecordingHappened(Context context, long timeLastRecordingHappened){
        Prefs prefs = new Prefs(context);
        prefs.setTimeThatLastRecordingHappened(timeLastRecordingHappened);

    }

    static String convertUnixTimeToString(long unixTimeToConvert){
        if (unixTimeToConvert < 1){
            return "";
        }
        Date date = new Date(unixTimeToConvert);
        Locale nzLocale = new Locale("nz");
        DateFormat fileFormat = new SimpleDateFormat("EEE, d MMM yyyy 'at' HH:mm:ss", nzLocale);
        return fileFormat.format(date);
    }

    static void setUseVeryFrequentRecordings(Context context, boolean useVeryFrequentRecordings){
        Prefs prefs = new Prefs(context);
        prefs.setUseVeryFrequentRecordings(useVeryFrequentRecordings);
        createTheNextSingleStandardAlarm(context);
    }

    static void setPeriodicallyUpdateGPS(Context context, boolean periodicallyUpdateGPS){
        Prefs prefs = new Prefs(context);
        prefs.setPeriodicallyUpdateGPS(periodicallyUpdateGPS);
        createLocationUpdateAlarm(context);
    }

    static void setWalkingMode(Context context, boolean walkingMode){
        Prefs prefs = new Prefs(context);
        if (walkingMode){
            prefs.setInternetConnectionMode("offline");
            prefs.setUseFrequentUploads(!walkingMode); // don't upload as it will be in airplane mode
        }else{
            prefs.setInternetConnectionMode("normal");
            prefs.setIsDisabled(!walkingMode); // I thought it best to disable recording when user exits walking mode, but don't enable just because they turn on walking mode
        }

        prefs.setUseFrequentRecordings(walkingMode);
        prefs.setIgnoreLowBattery(walkingMode);
        prefs.setPlayWarningSound(walkingMode);
        prefs.setPeriodicallyUpdateGPS(walkingMode);
        prefs.setIsDisableDawnDuskRecordings(walkingMode);

        // need to reset alarms as their frequency may have changed.
        Util.createTheNextSingleStandardAlarm(context);
        Util.setUpLocationUpdateAlarm(context);
    }

    static void setUseFrequentRecordings(Context context, boolean useFrequentRecordings){
        Prefs prefs = new Prefs(context);
        prefs.setUseFrequentRecordings(useFrequentRecordings);
        createTheNextSingleStandardAlarm(context);
    }

    static void setUseFrequentUploads(Context context, boolean useFrequentUploads){
        Prefs prefs = new Prefs(context);
        prefs.setUseFrequentUploads(useFrequentUploads);

    }

    static void uploadFilesUsingUploadButton(final Context context){

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    boolean uploadedSuccessfully =  RecordAndUpload.uploadFiles(context);

                    JSONObject jsonObjectMessageToBroadcast = new JSONObject();
                    if (uploadedSuccessfully){

                        jsonObjectMessageToBroadcast.put("messageType", "SUCCESSFULLY_UPLOADED_RECORDINGS");
                        jsonObjectMessageToBroadcast.put("messageToDisplay", "Recordings have been uploaded to the server.");

                    }else{
                        jsonObjectMessageToBroadcast.put("messageType", "FAILED_RECORDINGS_NOT_UPLOADED");
                        jsonObjectMessageToBroadcast.put("messageToDisplay", "There was a problem. The recordings were NOT uploaded.");
                    }
                    Util.broadcastAMessage(context, "MANAGE_RECORDINGS", jsonObjectMessageToBroadcast);
                }
                catch (Exception ex) {
                    Log.e(TAG, ex.getLocalizedMessage());
                }
            }
        };
        thread.start();
    }

    public static void displayHelp(final Context context, String activityOrFragmentName){

        String dialogMessage = "";


        if (activityOrFragmentName.equalsIgnoreCase(context.getResources().getString(R.string.app_icon_name))){
            dialogMessage = context.getString(R.string.help_text_welcome);
        }else if (activityOrFragmentName.equalsIgnoreCase(context.getResources().getString(R.string.activity_or_fragment_title_welcome))){
            dialogMessage = context.getString(R.string.help_text_welcome);
        }else if (activityOrFragmentName.equalsIgnoreCase(context.getResources().getString(R.string.activity_or_fragment_title_create_account))){
            dialogMessage = context.getString(R.string.help_text_create_account);
        }else if (activityOrFragmentName.equalsIgnoreCase(context.getResources().getString(R.string.activity_or_fragment_title_sign_in))){
            dialogMessage = context.getString(R.string.help_text_sign_in);
        }else if (activityOrFragmentName.equalsIgnoreCase(context.getResources().getString(R.string.activity_or_fragment_title_create_or_choose_group))){
            dialogMessage = context.getString(R.string.help_text_create_or_choose_group);
        }else if (activityOrFragmentName.equalsIgnoreCase(context.getResources().getString(R.string.activity_or_fragment_title_register_phone))){
            dialogMessage = context.getString(R.string.help_text_register_phone);
        }else if (activityOrFragmentName.equalsIgnoreCase(context.getResources().getString(R.string.activity_or_fragment_title_gps_location))){
            dialogMessage = context.getString(R.string.help_text_gps_location);
        }else if (activityOrFragmentName.equalsIgnoreCase(context.getResources().getString(R.string.activity_or_fragment_title_test_record))){
            dialogMessage = context.getString(R.string.help_text_test_record);
        }else if (activityOrFragmentName.equalsIgnoreCase(context.getResources().getString(R.string.activity_or_fragment_title_vitals))){
            dialogMessage = context.getString(R.string.help_text_vitals);
        }else if (activityOrFragmentName.equalsIgnoreCase(context.getResources().getString(R.string.activity_or_fragment_title_walking))){
            dialogMessage = context.getString(R.string.help_text_walking);
        }else if (activityOrFragmentName.equalsIgnoreCase(context.getResources().getString(R.string.activity_or_fragment_title_activity_ignore_low_battery))){
            dialogMessage = context.getString(R.string.help_text_battery);
        }else if (activityOrFragmentName.equalsIgnoreCase(context.getResources().getString(R.string.activity_or_fragment_title_warning_sound))){
            dialogMessage = context.getString(R.string.help_text_warning_sound);
        }else if (activityOrFragmentName.equalsIgnoreCase(context.getResources().getString(R.string.activity_or_fragment_title_manage_recordings))){
            dialogMessage = context.getString(R.string.help_text_manage_recordings);
        }else if (activityOrFragmentName.equalsIgnoreCase(context.getResources().getString(R.string.activity_or_fragment_title_internet_connection))){
            dialogMessage = context.getString(R.string.help_text_internet_connection);
        }else if (activityOrFragmentName.equalsIgnoreCase(context.getResources().getString(R.string.activity_or_fragment_title_activity_frequency))){
            dialogMessage = context.getString(R.string.help_text_frequency);
        }else if (activityOrFragmentName.equalsIgnoreCase(context.getResources().getString(R.string.activity_or_fragment_title_rooted))){
            dialogMessage = context.getString(R.string.help_text_rooted);
        }else if (activityOrFragmentName.equalsIgnoreCase(context.getResources().getString(R.string.activity_or_fragment_title_settings_for_testing))){
            dialogMessage = context.getString(R.string.help_text_settings_for_testing);
        }else if (activityOrFragmentName.equalsIgnoreCase(context.getResources().getString(R.string.activity_or_fragment_title_turn_off_or_on))){
            dialogMessage = context.getString(R.string.help_text_turn_off_or_on);
        }else if (activityOrFragmentName.equalsIgnoreCase(context.getResources().getString(R.string.activity_or_fragment_title_settings_for_audio_source))){
            dialogMessage = context.getString(R.string.help_text_settings_for_audio_source);

        }else {
            dialogMessage = "Still to fix in Util.displayHelp";
        }

        // Make any urls 'clickable'
        //https://stackoverflow.com/questions/9204303/android-is-it-possible-to-add-a-clickable-link-into-a-string-resource
        final SpannableString s = new SpannableString(dialogMessage);

        Linkify.addLinks(s, Linkify.ALL);


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Add the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                return;
            }
        });



        builder.setMessage(s)
                .setTitle(activityOrFragmentName);

        // https://stackoverflow.com/questions/15909672/how-to-set-font-size-for-text-of-dialog-buttons
        final AlertDialog  dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btnPositive = dialog.getButton(Dialog.BUTTON_POSITIVE);
                btnPositive.setTextSize(24);
                int oKButtonColor = ResourcesCompat.getColor(context.getResources(), R.color.dialogButtonText, null);
                btnPositive.setTextColor(oKButtonColor);

                //https://stackoverflow.com/questions/6562924/changing-font-size-into-an-alertdialog
                //https://stackoverflow.com/questions/13520193/android-linkify-how-to-set-custom-link-color
                TextView textView = (TextView) dialog.findViewById(android.R.id.message);
                int linkColorInt = ResourcesCompat.getColor(context.getResources(), R.color.linkToServerInHelp, null);
                textView.setLinkTextColor(linkColorInt);
                textView.setTextSize(22);

            }
        });

        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        dialog.show();

        // Make the textview clickable. Must be called after show(). Need for URL to work
        ((TextView)dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    static void deleteAllRecordingsOnPhoneUsingDeleteButton(final Context context){

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    File recordingsFolder = Util.getRecordingsFolder(context);

                    for (File file : recordingsFolder.listFiles()){
                        file.delete();
                    }

                    JSONObject jsonObjectMessageToBroadcast = new JSONObject();
                    if (getNumberOfRecordings(context) == 0){
                        jsonObjectMessageToBroadcast.put("messageType", "SUCCESSFULLY_DELETED_RECORDINGS");
                        jsonObjectMessageToBroadcast.put("messageToDisplay", "All recordings on the phone have been deleted.");
                    }else{
                        jsonObjectMessageToBroadcast.put("messageType", "FAILED_RECORDINGS_NOT_DELETED");
                        jsonObjectMessageToBroadcast.put("messageToDisplay", "There was a problem. The recordings were NOT deleted.");
                    }
                    Util.broadcastAMessage(context, "MANAGE_RECORDINGS", jsonObjectMessageToBroadcast);
                }
                catch (Exception ex) {
                    Log.e(TAG, ex.getLocalizedMessage());
                }
            }
        };
        thread.start();
    }

    static int getNumberOfRecordings(Context context){
        File recordingsFolder = Util.getRecordingsFolder(context);
        File recordingFiles[] = recordingsFolder.listFiles();
        return recordingFiles.length;
    }

    //https://stackoverflow.com/questions/1819142/how-should-i-validate-an-e-mail-address
     public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static void setGroups(Context context, ArrayList<String> groupsArrayList){
        try {
            JSONArray groupsArrayJSON = new JSONArray();
            for (String group : groupsArrayList) {
                groupsArrayJSON.put(group);
            }

            JSONObject jsonGroups = new JSONObject();
            jsonGroups.put("groups", groupsArrayJSON);

            String groupsAsJsonString = jsonGroups.toString();
            Prefs prefs = new Prefs(context);
            prefs.setGroups(groupsAsJsonString);

        }catch (Exception ex){
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    public static void addGroup(Context context, String groupName){
        ArrayList<String> localGroups = getGroupsStoredOnPhone(context);
        if (!localGroups.contains(groupName)) {
            localGroups.add(groupName);
        }
        setGroups(context, localGroups);
    }

    public static  ArrayList<String> getGroupsStoredOnPhone(Context context){
        Prefs prefs = new Prefs(context);
        ArrayList<String> groups  = new ArrayList<String>();
        String groupsString = prefs.getGroups();
        if (groupsString != null) {
            try {
                JSONObject jsonGroups = new JSONObject(groupsString);
                JSONArray groupsArrayJSON = jsonGroups.getJSONArray("groups");
                if (groupsArrayJSON != null) {
                    for (int i = 0; i < groupsArrayJSON.length(); i++) {
                        groups.add(groupsArrayJSON.getString(i));
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return groups;
    }

    static void getGroupsFromServer(final Context context){

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {

                    ArrayList<String> groupsFromServer = Server.getGroups(context);
                    setGroups( context, groupsFromServer);

                }
                catch (Exception ex) {
                    Log.e(TAG, ex.getLocalizedMessage());
                }
            }
        };
        thread.start();
    }

    static void addGroupToServer(final Context context, final String groupName){
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                   Server.addGroupToServer(context, groupName);
                }
                catch (Exception ex) {
                    Log.e(TAG, ex.getLocalizedMessage());
                }
            }
        };
        thread.start();
    }

   static void setUseTestServer(final Context context, boolean useTestServer) {
        Prefs prefs = new Prefs(context);
        prefs.setUseTestServer(useTestServer);
        // Need to un register phone and remove groups, account
        unregisterPhone(context);
        signOutUser(context);
        prefs.setGroups(null);
    }

    static void unregisterPhone(final Context context) {
        try {
            Prefs prefs = new Prefs(context);

            prefs.setGroupName(null);
            prefs.setDevicePassword(null);
            prefs.setDeviceName(null);
            prefs.setDeviceToken(null);

        } catch (Exception ex) {
            Log.e(TAG, "Error Un-registering device.");
        }

    }

    static void signOutUser(final Context context) {
        try {
            Prefs prefs = new Prefs(context);
            prefs.setUserSignedIn(false);
        } catch (Exception ex) {
            Log.e(TAG, "Error Un-registering user.");
        }
    }

    static boolean isPhoneRegistered(final Context context){
        Prefs prefs = new Prefs(context);
        String groupNameFromPrefs = prefs.getGroupName();
        String deviceNameFromPrefs = prefs.getDeviceName();
        if (groupNameFromPrefs != null && deviceNameFromPrefs != null){
            return true;
        }else {
            return false;
        }
    }
}
