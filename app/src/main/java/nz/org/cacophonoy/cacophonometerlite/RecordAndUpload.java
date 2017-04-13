package nz.org.cacophonoy.cacophonometerlite;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
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

import static nz.org.cacophonoy.cacophonometerlite.Server.getToken;
import static nz.org.cacophonoy.cacophonometerlite.Server.login;

/**
 * Created by User on 29-Mar-17.
 */

public class RecordAndUpload {
    private static final String LOG_TAG = RecordAndUpload.class.getName();
    private static long recordTimeSeconds = 0; //  set it later

    public RecordAndUpload(){

    }



    public static void doRecord(Context context, String typeOfRecording){

        Prefs prefs = new Prefs(context);

        recordTimeSeconds =  (long)prefs.getRecordingDuration();


        if (typeOfRecording != null){
            if (typeOfRecording.equalsIgnoreCase("testButton")  ){
                recordTimeSeconds = 5;  // short test
            }
        }
        makeRecording(context);

        // only upload recordings if it has been more than a day since last upload
        long dateTimeLastUpload = prefs.getDateTimeLastUpload();
        long now = new Date().getTime();

       long aDay = 1000 * 60 * 60 * 24;
     //   long aDay = 1; // for testing

        if (typeOfRecording != null){
            if (typeOfRecording.equalsIgnoreCase("testButton") ){
                uploadFiles(context);
            }
        }
        if ((now - dateTimeLastUpload) > aDay){
            if (uploadFiles(context)){
                prefs.setDateTimeLastUpload(now);
            }

        }

        // Update dawn/dusk times if it has been more than 23.5 hours since last time
        long dateTimeLastCalculatedDawnDusk = prefs.getDateTimeLastCalculatedDawnDusk();
        long twentyThreeAndAHalfHours = 1000 * 60 * 6 * 235;

        if ((now - dateTimeLastCalculatedDawnDusk) > twentyThreeAndAHalfHours){
            DawnDuskAlarms.configureDawnAlarms(context);
            DawnDuskAlarms.configureDuskAlarms(context);
            prefs.setDateTimeLastCalculatedDawnDusk(now);
        }





    }

    private static boolean makeRecording(Context context){
      //  Log.d(LOG_TAG, "Make a recording");


        // Get recording file.
        Date date = new Date(System.currentTimeMillis());
        // Calculate dawn and dusk offset in seconds will be sent to server to allow queries on this data
        Calendar nowToday =  new GregorianCalendar(TimeZone.getTimeZone("Pacific/Auckland"));

        Calendar dawn = Util.getDawn(context, nowToday);
        System.out.println("dawn " + dawn);
//        long relativeToDawn  = dawn.getTimeInMillis() - nowToday.getTimeInMillis();
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
        fileName += ".3gp";

        File file = new File(Util.getRecordingsFolder(), fileName);
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

            return false;
        }

        // Send message that recording started.
      //  Log.d(LOG_TAG, "RECORDING_STARTED");

        // Start recording.
        try {
            mRecorder.start();
        }catch (Exception e){
            Log.e(LOG_TAG, "mRecorder.start " + e.getLocalizedMessage());
            return false;
        }

        // Sleep for duration of recording.
        try {
            Thread.sleep(recordTimeSeconds * 1000);
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Failed sleeping in recording thread.");
            e.printStackTrace();
            return false;
        }

        // Stop recording.
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null; // attempting to fix error mediarecorder went away with unhandled events

        // Send message that recording finished.
     //   Log.d(LOG_TAG, "RECORDING_FINISHED");


        // Give time for file to be saved.
        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Failed sleeping in recording thread.");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static boolean uploadFiles(Context context){
        try {
            File recordingsFolder = Util.getRecordingsFolder();
            File recordingFiles[] = recordingsFolder.listFiles();
            if (recordingFiles != null) {

                Log.d(LOG_TAG, "about to disable airplane mode");

                // Switching airplane mode does not work (and causes a crash) for Android 4.2 and above
                if (Build.VERSION.SDK_INT <= 16){  // The last version that allows airplane mode switching is Android 4.1 (API 16)
                    Util.disableAirplaneMode(context);
                }


                // Check here to see if can connect to server and abort (for all files) if can't
                // Check that there is a JWT (JSON Web Token)
                if (getToken() == null) {
                    if (!Server.login(context)) {
                        Log.w(LOG_TAG, "sendFile: no JWT. Aborting upload");

                        if (Build.VERSION.SDK_INT <= 16){  // The last version that allows airplane mode switching is Android 4.1 (API 16)
                            Util.enableAirplaneMode(context);
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
                        if (Build.VERSION.SDK_INT <= 16){  // The last version that allows airplane mode switching is Android 4.1 (API 16)
                            Util.enableAirplaneMode(context);
                            }
                        return false;
                    }

                }

                if (Build.VERSION.SDK_INT <= 16){  // The last version that allows airplane mode switching is Android 4.1 (API 16)
                    Util.enableAirplaneMode(context);
                }

            }
        }catch (Exception e){
            if (Build.VERSION.SDK_INT <= 16){  // The last version that allows airplane mode switching is Android 4.1 (API 16)
                Util.enableAirplaneMode(context); // just to make sure airplane mode is enabled
            }

            Log.e(LOG_TAG, "Error with upload");
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

        if (fileNameParts.length != 13) {
            aFile.delete();
            Log.i(LOG_TAG, "deleted file: " + fileName);
            return false;
        }
        Log.d(LOG_TAG, "3");
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
        boolean airplaneModeOn = false;
        if (airplaneMode.equalsIgnoreCase("airplaneModeOn")) {
            airplaneModeOn = true;
        }


        String localFilePath = "/data/data/com.thecacophonytrust.cacophonometer/app_cacophony/recordings/" + fileName;
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


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Server.uploadAudioRecording(aFile, audioRecording, context);
    }
}
