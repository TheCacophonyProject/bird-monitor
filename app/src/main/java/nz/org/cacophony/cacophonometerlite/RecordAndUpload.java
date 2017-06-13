package nz.org.cacophony.cacophonometerlite;
import android.app.Service;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import static nz.org.cacophony.cacophonometerlite.Server.getToken;

/**
 * Created by User on 29-Mar-17.
 * This is where the action is - however the code starts, it gets here to do a recording and then send recording to server
 */

class RecordAndUpload {
    private static final String LOG_TAG = RecordAndUpload.class.getName();
   // private static long recordTimeSeconds = 0; //  set it later

    private RecordAndUpload(){

    }



    static void doRecord(Context context, String typeOfRecording){
        if (typeOfRecording == null){
            Log.e(LOG_TAG, "typeOfRecording is null");
            return;
        }

        Prefs prefs = new Prefs(context);

       // recordTimeSeconds =  (long)prefs.getRecordingDuration();
        long recordTimeSeconds =  (long)prefs.getRecordingDuration();

        if (prefs.getUseShortRecordings()){
            recordTimeSeconds = 1;
        }

     //   if (typeOfRecording != null){
            if (typeOfRecording.equalsIgnoreCase("testButton")  ){
                recordTimeSeconds = 5;  // short test
            }else if (typeOfRecording.equalsIgnoreCase("dawn") || typeOfRecording.equalsIgnoreCase("dusk")){
                recordTimeSeconds +=  2; // help to recognise dawn/dusk recordings
            }
     //   }
        makeRecording(context, recordTimeSeconds);

        // only upload recordings if it has been more than a day since last upload
        long dateTimeLastUpload = prefs.getDateTimeLastUpload();
        long now = new Date().getTime();

//       long timeIntervalBetweenUploads = 1000 * 60 * 60 * 24;
      //  long timeIntervalBetweenUploads = 1000 * 60 * 60 * 12;
        long timeIntervalBetweenUploads = 1000 * 60 * 60 * 6;
//        long timeIntervalBetweenUploads = 1000 * 60 * 60 * 2; // 2 hours for testing
//        long timeIntervalBetweenUploads = 1000 * 60 * 60; // 1 hour for testing
      //  long timeIntervalBetweenUploads = 1000 * 60 ; // 1 minute for testing
//        long timeIntervalBetweenUploads = 1000  ; // 1 second for testing

      //  if (typeOfRecording != null){
            if (typeOfRecording.equalsIgnoreCase("testButton") ){
                // Always upload when test button pressed
                uploadFiles(context);
                prefs.setDateTimeLastUpload(0); // this is to allow the recording to upload the next time the periodic recording happens

                // Always set up dawn/dusk alarms when test button pressed
                DawnDuskAlarms.configureDawnAlarms(context);
                DawnDuskAlarms.configureDuskAlarms(context);
                prefs.setDateTimeLastCalculatedDawnDusk(0);
            }else if ((now - dateTimeLastUpload) > timeIntervalBetweenUploads){
            if (uploadFiles(context)){
                prefs.setDateTimeLastUpload(now);
            }

        }

     //   if (typeOfRecording != null){
            if (typeOfRecording.equalsIgnoreCase("repeating")  ){
                // Update dawn/dusk times if it has been more than 23.5 hours since last time. It will do this if the current alarm is a repeating alarm or a dawn/dusk alarm
                long dateTimeLastCalculatedDawnDusk = prefs.getDateTimeLastCalculatedDawnDusk();
                 long timeIntervalBetweenDawnDuskTimeCalculation = 1000 * 60 * 6 * 235;
//                long timeIntervalBetweenDawnDuskTimeCalculation = 1000 * 60 * 60 * 2; // 2 hours for testing
//                long timeIntervalBetweenDawnDuskTimeCalculation = 1000 * 60 * 60 ; // 1 hour for testing

                if ((now - dateTimeLastCalculatedDawnDusk) > timeIntervalBetweenDawnDuskTimeCalculation){
                    DawnDuskAlarms.configureDawnAlarms(context);
                    DawnDuskAlarms.configureDuskAlarms(context);
                    prefs.setDateTimeLastCalculatedDawnDusk(now);
                }

            }
       // }






    }

    private static void makeRecording(Context context,  long recordTimeSeconds){


        // Get recording file.
        Date date = new Date(System.currentTimeMillis());
        // Calculate dawn and dusk offset in seconds will be sent to server to allow queries on this data
        Calendar nowToday =  new GregorianCalendar(TimeZone.getTimeZone("Pacific/Auckland"));

        Calendar dawn = Util.getDawn(context, nowToday);

        long relativeToDawn  =  nowToday.getTimeInMillis() - dawn.getTimeInMillis();
        relativeToDawn  = relativeToDawn /1000; // now in seconds

        Calendar dusk = Util.getDusk(context, nowToday);
//        long relativeToDusk  = dusk.getTimeInMillis() - nowToday.getTimeInMillis();
        long relativeToDusk  = nowToday.getTimeInMillis() - dusk.getTimeInMillis();
        relativeToDusk  = relativeToDusk /1000; // now in seconds

        DateFormat fileFormat = new SimpleDateFormat("yyyy MM dd HH mm ss", Locale.UK);
        String fileName = fileFormat.format(date);

        if (Math.abs(relativeToDawn) < Math.abs(relativeToDusk)){
            fileName += " relativeToDawn " + relativeToDawn;
        }else{
            fileName += " relativeToDusk " + relativeToDusk;
        }

        if (Util.isAirplaneModeOn(context)){
            fileName += " airplaneModeOn";
        }else{
            fileName += " airplaneModeOff";
        }

        String batteryStatus = Util.getBatteryStatus(context);
        fileName += " " + batteryStatus;
        double batteryLevel = Util.getBatteryLevel(context);
        fileName += " " + batteryLevel;
        fileName += " " + recordTimeSeconds;
        fileName += ".3gp";

        File file = new File(Util.getRecordingsFolder(context), fileName);
        String filePath = file.getAbsolutePath();

        // Setup audio recording settings.
        MediaRecorder mRecorder = new MediaRecorder();

        // Try to prepare recording.
        try {
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setOutputFile(filePath);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.prepare();

        } catch (Exception e) {

            Log.e(LOG_TAG, "Setup recording failed.");
            Log.e(LOG_TAG, "Could be due to lack of sdcard");
            Log.e(LOG_TAG, "Could be due to phone connected to pc as usb storage");
            Log.e(LOG_TAG, e.getLocalizedMessage());



            return ;
        }



        // Start recording.
        try {
            mRecorder.start();
        }catch (Exception e){
            Log.e(LOG_TAG, "mRecorder.start " + e.getLocalizedMessage());
            return ;
        }

        // Sleep for duration of recording.
        try {


            Thread.sleep(recordTimeSeconds * 1000);

        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Failed sleeping in recording thread.");
            e.printStackTrace();
            return ;
        }

        // Stop recording.
        mRecorder.stop();
//        mRecorder.release();
        //https://stackoverflow.com/questions/9609479/android-mediaplayer-went-away-with-unhandled-events
        mRecorder.reset();
        mRecorder.release();
        //noinspection UnusedAssignment
        mRecorder = null; // attempting to fix error media recorder went away with unhandled events

       Log.d(LOG_TAG, "RECORDING_FINISHED");


        // Give time for file to be saved.
        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Failed sleeping in recording thread.");
            e.printStackTrace();
        }
    }

    private static boolean uploadFiles(Context context){
        try {
            File recordingsFolder = Util.getRecordingsFolder(context);
            if (recordingsFolder == null){
                Log.d(LOG_TAG, "Error getting recordings folder");
                return false;
            }
            File recordingFiles[] = recordingsFolder.listFiles();
            if (recordingFiles != null) {

                Log.d(LOG_TAG, "about to disable airplane mode");



                Util.disableFlightMode(context);

                // Now wait for network connection as setFlightMode takes a while
                if (!Util.waitForNetworkConnection(context, true)){
                            Log.e(LOG_TAG, "Failed to disable airplane mode");
                    return false;
                }


                // Check here to see if can connect to server and abort (for all files) if can't
                // Check that there is a JWT (JSON Web Token)
                if (getToken() == null) {
                    if (!Server.login(context)) {
                        Log.w(LOG_TAG, "sendFile: no JWT. Aborting upload");


                        Util.enableFlightMode(context);

                        // Now wait for network connection to close as  setFlightMode takes a while
                        if (!Util.waitForNetworkConnection(context, false)){
                            Log.e(LOG_TAG, "Failed to disable airplane mode");
                            return false;
                        }


                        return false; // Can't upload without JWT, login/register device to get JWT.
                    }
                }

                Log.d(LOG_TAG, "finished disabling airplane mode");
                for (File aFile : recordingFiles) {

                    if (sendFile(context, aFile)) {
                        if (!aFile.delete()) {
                            Log.w(LOG_TAG, "Deleting audio file failed");
                        }
                    } else {
                        Log.w(LOG_TAG, "Failed to upload file to server");

                        Util.enableFlightMode(context);

                        // Now wait for network connection to close as  setFlightMode takes a while
                        if (!Util.waitForNetworkConnection(context, false)){
                            Log.e(LOG_TAG, "Failed to disable airplane mode");
                            return false;
                        }
                        return false;
                    }

                }


                Util.enableFlightMode(context);

                // Now wait for network connection to close as  setFlightMode takes a while
                if (!Util.waitForNetworkConnection(context, false)){
                    Log.e(LOG_TAG, "Failed to disable airplane mode");
                    return false;
                }

            }
        }catch (Exception e){


            Util.enableFlightMode(context);


            // Now wait for network connection to close as  setFlightMode takes a while
            if (!Util.waitForNetworkConnection(context, false)){
                Log.e(LOG_TAG, "Failed to disable airplane mode");
                return false;
            }

            Log.e(LOG_TAG, "Error with upload");
            return false;
        }
        // The airplane was not showing on the phone (even though it seems to be in flight mode, so try the next code to wait for network connection to die
        // Now wait for network connection as setFlightMode takes a while
        if (!Util.waitForNetworkConnection(context, false)){
            Log.e(LOG_TAG, "Failed to disable airplane mode");
            return false;
        }

        return true;
    }

    private static boolean sendFile(Context context, File aFile) {

        Log.i(LOG_TAG, "sendFile: Start");
        Prefs prefs = new Prefs(context);

        // Get metadata and put into JSON.
        JSONObject audioRecording = new JSONObject();

        String fileName = aFile.getName();
        Log.i(LOG_TAG, "fileName: " + fileName);
        // http://stackoverflow.com/questions/3481828/how-to-split-a-string-in-java
        //http://stackoverflow.com/questions/3387622/split-string-on-dot-as-delimiter
        String[] fileNameParts = fileName.split("[. ]");
        // this code breaks if old files exist, so delete them and move on

        if (fileNameParts.length != 14) {
          if (aFile.delete()){
              Log.i(LOG_TAG, "deleted file: " + fileName);
              return false;
          }

        }
      //  Log.d(LOG_TAG, "3");
        String year = fileNameParts[0];
        String month = fileNameParts[1];
        String day = fileNameParts[2];
        String hour = fileNameParts[3];
        String minute = fileNameParts[4];
        String second = fileNameParts[5];
        String relativeTo = fileNameParts[6];
        String relativeToOffset = fileNameParts[7];
        String airplaneMode = fileNameParts[8];
        String batteryStatus = fileNameParts[9];
        String batteryLevel = fileNameParts[10];
        batteryLevel += ".";
        batteryLevel += fileNameParts[11];
        String recordTimeSeconds = fileNameParts[12];
        boolean airplaneModeOn = false;
        if (airplaneMode.equalsIgnoreCase("airplaneModeOn")) {
            airplaneModeOn = true;
        }



        String localFilePath = Util.getRecordingsFolder(context) + "/" + fileName;
        if (! new File(localFilePath).exists()){
            Log.e(LOG_TAG, localFilePath + " does not exist");
            return false;
        }
        String recordingDateTime = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
        String recordingTime = hour + ":" + minute + ":" + second;

        try {

            JSONArray location = new JSONArray();
            location.put(prefs.getLatitude());
            location.put(prefs.getLongitude());
            audioRecording.put("location", location);
//            audioRecording.put("duration", prefs.getRecordingDuration());
            audioRecording.put("duration", recordTimeSeconds);
            audioRecording.put("localFilePath", localFilePath);
            audioRecording.put("recordingDateTime", recordingDateTime);
            audioRecording.put("recordingTime", recordingTime);
            audioRecording.put("batteryCharging", batteryStatus);

            audioRecording.put("batteryLevel", batteryLevel);
            audioRecording.put("airplaneModeOn", airplaneModeOn);

            if (relativeTo.equalsIgnoreCase("relativeToDawn")) {
                audioRecording.put("relativeToDawn", relativeToOffset);
            }
            if (relativeTo.equalsIgnoreCase("relativeToDusk")) {
                audioRecording.put("relativeToDusk", relativeToOffset);
            }
            String versionName = BuildConfig.VERSION_NAME;
            audioRecording.put("version", versionName);


            JSONObject additionalMetadata = new JSONObject();
            additionalMetadata.put("Android API Level", Build.VERSION.SDK_INT);
            additionalMetadata.put("Phone has been rooted", prefs.getHasRootAccess());
            additionalMetadata.put("Phone manufacturer", Build.MANUFACTURER);
            additionalMetadata.put("Phone model", Build.MODEL);

            // see if can send logcat
            String logCat = Util.getLogCat();
            Util.clearLog();
            additionalMetadata.put("Logcat ", logCat);


            TelephonyManager mTelephonyManager = (TelephonyManager) context
                    .getSystemService(Service.TELEPHONY_SERVICE);

            additionalMetadata.put("SIM state",Util.getSimStateAsString( mTelephonyManager.getSimState()));
            if (mTelephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY) {
                additionalMetadata.put("SimOperatorName", mTelephonyManager.getSimOperatorName());
              //  additionalMetadata.put("Line1Number", mTelephonyManager.getLine1Number());
            }

            audioRecording.put("additionalMetadata", additionalMetadata);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Server.uploadAudioRecording(aFile, audioRecording, context);
    }
}
