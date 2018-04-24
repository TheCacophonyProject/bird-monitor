package nz.org.cacophony.cacophonometerlite;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

//import android.util.Log;
//import ch.qos.logback.classic.Logger;

//import static nz.org.cacophony.cacophonometerlite.Server.getToken;

/**
 * Created by User on 29-Mar-17.
 * This is where the action is - however the code starts, it gets here to do a recording and then send recording to server
 */

class RecordAndUpload implements IdlingResourceForEspressoTesting{
    private static final String TAG = RecordAndUpload.class.getName();
//    private static Logger logger = null;
    public static boolean isRecording = false;



    private RecordAndUpload(){

    }



//    static String doRecord(Context context, String typeOfRecording, Handler handler){
static String doRecord(Context context, String typeOfRecording) {


    Log.d(TAG, "typeOfRecording is " + typeOfRecording);
    String returnValue = null;
//        if (logger == null){
//            logger = Util.getAndConfigureLogger(context, LOG_TAG);
//            logger.info("Starting doRecord method");
//        }


    if (typeOfRecording == null) {
        Log.e(TAG, "typeOfRecording is null");
//            Util.writeLocalLogEntryUsingLogback(context, LOG_TAG, "typeOfRecording is null");
//            logger.error("typeOfRecording is null");
        returnValue = "error";
        return returnValue;
    }

    Prefs prefs = new Prefs(context);

    long   recordTimeSeconds = (long) prefs.getRecordingDuration();
    boolean playWarningBeeps = false;

    String mode = prefs.getMode();
    switch(mode) {
        case "off":
            if (prefs.getUseShortRecordings()) {
                recordTimeSeconds = 1;
            }
            if (prefs.getPlayWarningSound()){
                playWarningBeeps = true;
            }

            break;
        case "normal":
            playWarningBeeps = false;
            break;
        case "normalOnline":
            playWarningBeeps = false;
            break;
        case "walking":
            playWarningBeeps = true;
            break;
    }


    if (typeOfRecording.equalsIgnoreCase("dawn") || typeOfRecording.equalsIgnoreCase("dusk")) {
        recordTimeSeconds += 2; // help to recognise dawn/dusk recordings
        Log.d(TAG, "typeOfRecording is dawn or dusk");
//    } else if (typeOfRecording.equalsIgnoreCase("repeating")) {
//
//        // Did this to try to fix bug of unknown origin that sometimes gives recordings every minute around dawn
//        long dateTimeLastRepeatingAlarmFired = prefs.getDateTimeLastRepeatingAlarmFired();
//        long now = new Date().getTime();
//        long minFractionOfRepeatTimeToWait = (long) (prefs.getTimeBetweenRecordingsSeconds() * 1000 * 0.9); // Do not proceed if last repeating alarm occurred within 90% of time interval for repeating alarms
//        if ((now - dateTimeLastRepeatingAlarmFired) < minFractionOfRepeatTimeToWait) {
//            Log.w(TAG, "A repeating alarm happened to soon and so was aborted");
////                    logger.warn("A repeating alarm happened to soon and so was aborted");
//            return "repeating alarm happened to soon";
//        } else {
//            prefs.setDateTimeLastRepeatingAlarmFired(now); // needed by above code next time repeating alarm fires.
//        }

    } else if (typeOfRecording.equalsIgnoreCase("recordNowButton")) {
        recordTimeSeconds += 1; // help to recognise recordNowButton recordings
      //  long now = new Date().getTime();
        prefs.setDateTimeLastRepeatingAlarmFired(0); // Helped when testing, but probably don't need when app is running normally

    }

if (isRecording){
   return "isRecording";
}else{
    makeRecording(context, recordTimeSeconds, playWarningBeeps);
    returnValue = "recorded successfully";
}





    if (typeOfRecording.equalsIgnoreCase("recordNowButton")) {
        Util.broadcastAMessage(context, "recordNowButton_finished");
    }



// Checked that it has a webToken before trying to upload
    if (prefs.getToken() == null) {
        returnValue =  "not logged in";
    }else {
        // only upload recordings if sufficient time has passed since last upload
        long dateTimeLastUpload = prefs.getDateTimeLastUpload();
        long now = new Date().getTime();
        long timeIntervalBetweenUploads = 1000 * (long) prefs.getTimeBetweenUploadsSeconds();
        boolean uploadedFilesSuccessfully = false;

      boolean offlineMode = prefs.getOffLineMode();
        switch(mode) { // mode determined earlier
            case "off":
                // don't change offline mode
                break;
            case "normal":
                offlineMode = false;
                break;
            case "walking":
                offlineMode = true;
                break;
        }

 if ((now - dateTimeLastUpload) > timeIntervalBetweenUploads) { // don't upload if not enough time has passed

//            if (!prefs.getOffLineMode()) { // don't upload if in offline mode
     if (!offlineMode) { // don't upload if in offline mode
         uploadingIdlingResource.increment();
                uploadedFilesSuccessfully = uploadFiles(context);
         uploadingIdlingResource.decrement();
                if (uploadedFilesSuccessfully) {
                    returnValue = "recorded and uploaded successfully";
                    prefs.setDateTimeLastUpload(now);
                } else {
                    returnValue = "recorded BUT did not upload";
                    Log.e(TAG, "Files failed to upload");
                }
            }


        }
    }
    //    boolean repeatingRecording = false;
//    if (typeOfRecording.equalsIgnoreCase("repeating")) {
//        DawnDuskAlarms.configureDawnAndDuskAlarms(context, false);
//    }

            return returnValue;
    }

    public static boolean makeRecording(Context context,  long recordTimeSeconds, boolean playWarningBeeps){
       isRecording = true;
try {


    Prefs prefs = new Prefs(context);
    // Get recording file.
    Date date = new Date(System.currentTimeMillis());
    // Calculate dawn and dusk offset in seconds will be sent to server to allow queries on this data
    Calendar nowToday = new GregorianCalendar(TimeZone.getTimeZone("Pacific/Auckland"));

    Calendar dawn = Util.getDawn(context, nowToday);

    long relativeToDawn = nowToday.getTimeInMillis() - dawn.getTimeInMillis();
    relativeToDawn = relativeToDawn / 1000; // now in seconds

    Calendar dusk = Util.getDusk(context, nowToday);

    long relativeToDusk = nowToday.getTimeInMillis() - dusk.getTimeInMillis();
    relativeToDusk = relativeToDusk / 1000; // now in seconds

    DateFormat fileFormat = new SimpleDateFormat("yyyy MM dd HH mm ss", Locale.UK);
    String fileName = fileFormat.format(date);

    if (Math.abs(relativeToDawn) < Math.abs(relativeToDusk)) {
        fileName += " rToDawn " + relativeToDawn;
    } else {
        fileName += " rToDusk " + relativeToDusk;
    }

    if (Util.isAirplaneModeOn(context)) {
        fileName += " apModeOn";
    } else {
        fileName += " apModeOff";
    }

//    if (Math.abs(relativeToDawn) < Math.abs(relativeToDusk)) {
//        fileName += " relativeToDawn " + relativeToDawn;
//    } else {
//        fileName += " relativeToDusk " + relativeToDusk;
//    }
//
//    if (Util.isAirplaneModeOn(context)) {
//        fileName += " airplaneModeOn";
//    } else {
//        fileName += " airplaneModeOff";
//    }

    String batteryStatus = Util.getBatteryStatus(context);
    fileName += " " + batteryStatus;
    double batteryLevel = Util.getBatteryLevel(context);
    fileName += " " + batteryLevel;
    fileName += " " + recordTimeSeconds;

    NumberFormat numberFormat  = new DecimalFormat("#.000000");
    double lat = prefs.getLatitude();
    double lon = prefs.getLongitude();
    String latStr = numberFormat.format(lat);
    String lonStr = numberFormat.format(lon);
    fileName += " " + latStr;
    fileName += " " + lonStr;
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

    } catch (Exception ex) {


        Log.e(TAG, "Setup recording failed. Could be due to lack of sdcard. Could be due to phone connected to pc as usb storage");
        Log.e(TAG, ex.getLocalizedMessage());

        return false;
    }

    //Prefs prefs = new Prefs(context);
    if (playWarningBeeps) {

        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 70);
        toneGen1.startTone(ToneGenerator.TONE_CDMA_NETWORK_BUSY, 2000);
        try {
            Thread.sleep(2000);
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    // Start recording.
    try {
        mRecorder.start();
        Util.broadcastAMessage(context, "recording_started");
    } catch (Exception e) {

//            logger.error("mRecorder.start " + e.getLocalizedMessage());
        Log.e(TAG, "mRecorder.start " + e.getLocalizedMessage());
        return false;
    }


    // Sleep for duration of recording.
    try {

        Thread.sleep(recordTimeSeconds * 1000);

    } catch (InterruptedException e) {

//            logger.error("Failed sleeping in recording thread.");
        Log.e(TAG, "Failed sleeping in recording thread.");
        return false;
    }

    // Stop recording.
    mRecorder.stop();
//        mRecorder.release();
    //https://stackoverflow.com/questions/9609479/android-mediaplayer-went-away-with-unhandled-events
    mRecorder.reset();
    mRecorder.release();

    mRecorder = null; // attempting to fix error media recorder went away with unhandled events


    if (playWarningBeeps) {
        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 70);
        toneGen1.startTone(ToneGenerator.TONE_CDMA_NETWORK_BUSY, 1000);
    }

    // Give time for file to be saved. (and play beeps)
    try {
        Thread.sleep(1 * 1000);
    } catch (InterruptedException ex) {
//            logger.error("Failed sleeping in recording thread." +  ex.getLocalizedMessage());
        Log.e(TAG, "Failed sleeping in recording thread." + ex.getLocalizedMessage());
    }
}catch (Exception ex){
    Log.e(TAG, ex.getLocalizedMessage());
}finally {
    isRecording = false;
}

        return true;
    }

    private static boolean uploadFiles(Context context){

        Util.broadcastAMessage(context, "about_to_upload_files");
        boolean returnValue = true;
        try {
            File recordingsFolder = Util.getRecordingsFolder(context);
            if (recordingsFolder == null){

//                logger.error("Error getting recordings folder");
                Log.e(TAG,"Error getting recordings folder" );
                return false;
            }
            File recordingFiles[] = recordingsFolder.listFiles();
            if (recordingFiles != null) {
                Util.disableFlightMode(context);

                // Now wait for network connection as setFlightMode takes a while
                if (!Util.waitForNetworkConnection(context, true)){
//                    logger.error("Failed to disable airplane mode");
                    Log.e(TAG, "Failed to disable airplane mode");
                    return false;
                }


                // Check here to see if can connect to server and abort (for all files) if can't
                // Check that there is a JWT (JSON Web Token)
//                if (getToken() == null) {
                Prefs prefs = new Prefs(context);

                // check to see if webToken needs updating
                boolean tokenIsCurrent = Util.isWebTokenCurrent(prefs);


                if ((prefs.getToken() == null) || !tokenIsCurrent) {

                    if (!Server.login(context)) {
//                        logger.warn("sendFile: no JWT. Aborting upload");
                        Log.w(TAG, "sendFile: no JWT. Aborting upload");


                        return false; // Can't upload without JWT, login/register device to get JWT.
                    }
                }

                int numberOfFilesUploaded = 0;  // put a limit on the number of file uploads as if there are too many I think it may be timing out
                for (File aFile : recordingFiles) {
//                    if (numberOfFilesUploaded > 9){
                    if (numberOfFilesUploaded > 19){ // increased as 10 may not be enough for longer intervals between uploads
                        break;
                    }



                    if (sendFile(context, aFile)) {
                        // deleting files can cause app to crash when phone connected to pc, so put in try catch
                        boolean fileSuccessfullyDeleted = false;
                        try {
                            fileSuccessfullyDeleted = aFile.delete();

                        }catch (Exception ex){
//                            logger.error(ex.getLocalizedMessage());
                            Log.e(TAG, ex.getLocalizedMessage());

                        }
                        if (!fileSuccessfullyDeleted) {
                            // for some reason file did not delete so exit for loop
//                            logger.error("Failed to delete file");
                            Log.e(TAG, "Failed to delete file");
                            returnValue = false;
                            break;
                        }
                    } else {
                        returnValue = false;
//                        logger.error("Failed to upload file to server");
                        Log.e(TAG, "Failed to upload file to server");
                        return false;
                    }
                    numberOfFilesUploaded++;
                }
            }
            if (returnValue){
                Util.broadcastAMessage(context, "files_successfully_uploaded");
            }
            return returnValue;
        }catch (Exception ex){

//            logger.error(ex.getLocalizedMessage());
            Log.e(TAG, ex.getLocalizedMessage());
            return false;
        }finally {
//            Util.enableFlightMode(context);
//            // Now wait for network connection to close as  setFlightMode takes a while
//            if (!Util.waitForNetworkConnection(context, false)){
//
//                logger.error("Failed to disable airplane mode");
//
//            }
        }

    }

    private static boolean sendFile(Context context, File aFile) {
        Prefs prefs = new Prefs(context);

        // Get metadata and put into JSON.
        JSONObject audioRecording = new JSONObject();

        String fileName = aFile.getName();

        // http://stackoverflow.com/questions/3481828/how-to-split-a-string-in-java
        //http://stackoverflow.com/questions/3387622/split-string-on-dot-as-delimiter
        String[] fileNameParts = fileName.split("[. ]");
        // this code breaks if old files exist, so delete them and move on

     //   if (fileNameParts.length != 14) {
        if (fileNameParts.length != 18) {
          if (aFile.delete()){
//              Log.i(LOG_TAG, "deleted file: " + fileName);
//              logger.error("deleted file: " + fileName);
              return false;
          }

        }

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
        String latStr = fileNameParts[13] + "." + fileNameParts[14];
        String lonStr = fileNameParts[15] + "." + fileNameParts[16];
        boolean airplaneModeOn = false;
//        if (airplaneMode.equalsIgnoreCase("airplaneModeOn")) {
        if (airplaneMode.equalsIgnoreCase("apModeOn")) {
            airplaneModeOn = true;
        }



        String localFilePath = Util.getRecordingsFolder(context) + "/" + fileName;
        if (! new File(localFilePath).exists()){

//            logger.error(localFilePath + " does not exist");
            Log.e(TAG,localFilePath + " does not exist" );
            return false;
        }
        //https://stackoverflow.com/questions/11399491/java-timezone-offset
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = GregorianCalendar.getInstance(tz);
        int offsetInMillis = tz.getOffset(cal.getTimeInMillis());
        String offset = String.format("%02d:%02d", Math.abs(offsetInMillis / 3600000), Math.abs((offsetInMillis / 60000) % 60));
        offset = (offsetInMillis >= 0 ? "+" : "-") + offset;

        String recordingDateTime = year + "-" + month + "-" + day + "T" + hour + ":" + minute + ":" + second+offset;
      //  String recordingTime = hour + ":" + minute + ":" + second; // 7/12/12 Agreed with Cameron to stop sending this

        try {

            JSONArray location = new JSONArray();
//            location.put(prefs.getLatitude());
//            location.put(prefs.getLongitude());
            location.put(Double.parseDouble(latStr));
            location.put(Double.parseDouble(lonStr));



            audioRecording.put("location", location);
            audioRecording.put("duration", recordTimeSeconds);
            audioRecording.put("localFilePath", localFilePath);
            audioRecording.put("recordingDateTime", recordingDateTime);
       //     audioRecording.put("recordingTime", recordingTime);
            audioRecording.put("batteryCharging", batteryStatus);

            audioRecording.put("batteryLevel", batteryLevel);
            audioRecording.put("airplaneModeOn", airplaneModeOn);

            if (relativeTo.equalsIgnoreCase("rToDawn")) {
                audioRecording.put("relativeToDawn", relativeToOffset);
            }
            if (relativeTo.equalsIgnoreCase("rToDusk")) {
                audioRecording.put("relativeToDusk", relativeToOffset);
            }

//            if (relativeTo.equalsIgnoreCase("relativeToDawn")) {
//                audioRecording.put("relativeToDawn", relativeToOffset);
//            }
//            if (relativeTo.equalsIgnoreCase("relativeToDusk")) {
//                audioRecording.put("relativeToDusk", relativeToOffset);
//            }
            String versionName = BuildConfig.VERSION_NAME;
            audioRecording.put("version", versionName);


            JSONObject additionalMetadata = new JSONObject();



            additionalMetadata.put("Android API Level", Build.VERSION.SDK_INT);
            additionalMetadata.put("App has root access", prefs.getHasRootAccess());
            additionalMetadata.put("Phone manufacturer", Build.MANUFACTURER);
            additionalMetadata.put("Phone model", Build.MODEL);

            TelephonyManager mTelephonyManager = (TelephonyManager) context
                    .getSystemService(Service.TELEPHONY_SERVICE);

            additionalMetadata.put("SIM state",Util.getSimStateAsString( mTelephonyManager.getSimState()));
            if (mTelephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY) {
                additionalMetadata.put("SimOperatorName", mTelephonyManager.getSimOperatorName());
            }

            audioRecording.put("additionalMetadata", additionalMetadata);

        } catch (JSONException ex) {
         //   ex.printStackTrace();
//            logger.error(ex.getLocalizedMessage());
            Log.e(TAG,ex.getLocalizedMessage() );
        }
        return Server.uploadAudioRecording(aFile, audioRecording, context);
    }
}
