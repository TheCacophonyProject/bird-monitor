package nz.org.cacophony.cacophonometer;

import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
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


/**
 * This is where the action is - however the code starts, it gets here to do a recording and then
 * sends recording to server.
 */

class RecordAndUpload implements IdlingResourceForEspressoTesting{
    private static final String TAG = RecordAndUpload.class.getName();
    public static boolean isRecording = false;
    private RecordAndUpload(){

    }

static String doRecord(Context context, String typeOfRecording) {
    JSONObject jsonObjectMessageToBroadcast = new JSONObject();

    Log.d(TAG, "typeOfRecording is " + typeOfRecording);
    String returnValue;

    if (typeOfRecording == null) {
        Log.e(TAG, "typeOfRecording is null");

        returnValue = "error";
        return returnValue;
    }

    Prefs prefs = new Prefs(context);

    long   recordTimeSeconds = (long) prefs.getRecordingDuration();



            if (prefs.getUseShortRecordings()) {
                recordTimeSeconds = 1;
            }

    if (typeOfRecording.equalsIgnoreCase("dawn") || typeOfRecording.equalsIgnoreCase("dusk")) {
        recordTimeSeconds += 2; // help to recognise dawn/dusk recordings
        Log.d(TAG, "typeOfRecording is dawn or dusk");
    } else if (typeOfRecording.equalsIgnoreCase("recordNowButton")) {
        recordTimeSeconds += 1; // help to recognise recordNowButton recordings
        prefs.setDateTimeLastRepeatingAlarmFiredToZero(); // Helped when testing, but probably don't need when app is running normally
    }

if (isRecording){
     jsonObjectMessageToBroadcast = new JSONObject();
    try {
        jsonObjectMessageToBroadcast.put("messageType", "ALREADY_RECORDING");
        jsonObjectMessageToBroadcast.put("messageToDisplay", "Can not record, as a recording is already in progress.");
    } catch (JSONException e) {
        e.printStackTrace();
    }
    Util.broadcastAMessage(context, "MANAGE_RECORDINGS", jsonObjectMessageToBroadcast);
   return "isRecording";
}else{

    makeRecording(context, recordTimeSeconds, prefs.getPlayWarningSound());

    returnValue = "recorded successfully";
}



// Checked that it has a webToken before trying to upload
    if (prefs.getToken() == null) {
        returnValue =  "not logged in";
        jsonObjectMessageToBroadcast = new JSONObject();
        try {
            jsonObjectMessageToBroadcast.put("messageType", "UPLOADING_FAILED_NOT_REGISTERED");
            jsonObjectMessageToBroadcast.put("messageToDisplay", "The Phone is NOT registered - could not upload the files.");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Util.broadcastAMessage(context, "MANAGE_RECORDINGS", jsonObjectMessageToBroadcast);
    }else {
        // only upload recordings if sufficient time has passed since last upload
        long dateTimeLastUpload = prefs.getDateTimeLastUpload();
        long now = new Date().getTime();
        long timeIntervalBetweenUploads = 1000 * (long) prefs.getTimeBetweenUploadsSeconds();
        //noinspection UnusedAssignment
        boolean uploadedFilesSuccessfully = false;


 if ((now - dateTimeLastUpload) > timeIntervalBetweenUploads || typeOfRecording.equalsIgnoreCase("recordNowButton")) { // don't upload if not enough time has passed

         if (!prefs.getInternetConnectionMode().equalsIgnoreCase("offline")) { // don't upload if in offline mode
        // uploadingIdlingResource.increment();
                uploadedFilesSuccessfully = uploadFiles(context);
       //  uploadingIdlingResource.decrement();
             if (uploadedFilesSuccessfully) {
                 prefs.setDateTimeLastUpload(now);
             }else{
                 returnValue = "recorded BUT did not upload";
                 Log.e(TAG, "Files failed to upload");
                 String messageToDisplay = "";
                 jsonObjectMessageToBroadcast = new JSONObject();
                 try {
                     jsonObjectMessageToBroadcast.put("messageType", "FAILED_RECORDINGS_NOT_UPLOADED");
                     jsonObjectMessageToBroadcast.put("messageToDisplay", "Files failed to upload to server.");
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
                 Util.broadcastAMessage(context, "MANAGE_RECORDINGS", jsonObjectMessageToBroadcast);
             }
//                if (uploadedFilesSuccessfully) {
//                    returnValue = "recorded and uploaded successfully";
//                    prefs.setDateTimeLastUpload(now);
//
//                     jsonObjectMessageToBroadcast = new JSONObject();
//                    try {
//                        jsonObjectMessageToBroadcast.put("messageType", "SUCCESSFULLY_UPLOADED_RECORDINGS");
//                        jsonObjectMessageToBroadcast.put("messageToDisplay", "Files have been uploaded");
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    Util.broadcastAMessage(context, "MANAGE_RECORDINGS", jsonObjectMessageToBroadcast);
//                } else {
//                    returnValue = "recorded BUT did not upload";
//                    Log.e(TAG, "Files failed to upload");
//                    String messageToDisplay = "";
//                     jsonObjectMessageToBroadcast = new JSONObject();
//                    try {
//                        jsonObjectMessageToBroadcast.put("messageType", "FAILED_RECORDINGS_NOT_UPLOADED");
//                        jsonObjectMessageToBroadcast.put("messageToDisplay", "Files failed to upload to server");
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    Util.broadcastAMessage(context, "MANAGE_RECORDINGS", jsonObjectMessageToBroadcast);
//                }
            }
        }

    }


            return returnValue;
    }

    private static void makeRecording(Context context, long recordTimeSeconds, boolean playWarningBeeps){
       isRecording = true;
        String messageToDisplay = "";
        JSONObject jsonObjectMessageToBroadcast = new JSONObject();
        try {
            jsonObjectMessageToBroadcast.put("messageType", "GETTING_READY_TO_RECORD");
            jsonObjectMessageToBroadcast.put("messageToDisplay", "Getting ready to record.");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Util.broadcastAMessage(context, "MANAGE_RECORDINGS", jsonObjectMessageToBroadcast);
        //Util.broadcastAMessage(context, "update_record_now_button");
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

//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) { // HONEYCOMB / Android version 3 / API 11
//        fileName += ".m4a";
//    }else{
//        fileName += ".3gp";
//    }
    fileName += ".m4a";

    File file = new File(Util.getRecordingsFolder(context), fileName);
    String filePath = file.getAbsolutePath();

    // Setup audio recording settings.
    MediaRecorder mRecorder = new MediaRecorder();

    // Try to prepare recording.
    try {
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFile(filePath);

            // Sampling configuration
            mRecorder.setAudioChannels(1);
            mRecorder.setAudioSamplingRate(16000);

            // Encoding configuration
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // MPEG_4 added in API 1
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC); // AAC added in API 10
            mRecorder.setAudioEncodingBitRate(256000);

        mRecorder.prepare();

    } catch (Exception ex) {

        Log.e(TAG, "Setup recording failed. Could be due to lack of sdcard. Could be due to phone connected to pc as usb storage");
        Log.e(TAG, ex.getLocalizedMessage());

        return;
    }

       if (playWarningBeeps) {

        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
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
         messageToDisplay = "";
         jsonObjectMessageToBroadcast = new JSONObject();
        try {
            jsonObjectMessageToBroadcast.put("messageType", "RECORDING_STARTED");
            jsonObjectMessageToBroadcast.put("messageToDisplay", "Recording has started");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Util.broadcastAMessage(context, "MANAGE_RECORDINGS", jsonObjectMessageToBroadcast);
        //Util.broadcastAMessage(context, "recording_started");
    } catch (Exception e) {

        Log.e(TAG, "mRecorder.start " + e.getLocalizedMessage());
        return;
    }


    // Sleep for duration of recording.
    try {

        Thread.sleep(recordTimeSeconds * 1000);

    } catch (InterruptedException e) {
        Log.e(TAG, "Failed sleeping in recording thread.");
        return;
    }

    // Stop recording.
    mRecorder.stop();

    //https://stackoverflow.com/questions/9609479/android-mediaplayer-went-away-with-unhandled-events
    mRecorder.reset();
    mRecorder.release();


    if (playWarningBeeps) {
        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 70);
        toneGen1.startTone(ToneGenerator.TONE_CDMA_NETWORK_BUSY, 1000);
    }

    // Give time for file to be saved. (and play beeps)
    try {
        Thread.sleep(1000);
    } catch (InterruptedException ex) {
//            logger.error("Failed sleeping in recording thread." +  ex.getLocalizedMessage());
        Log.e(TAG, "Failed sleeping in recording thread." + ex.getLocalizedMessage());
    }

    Util.setTimeThatLastRecordingHappened(context, new Date().getTime());
}catch (Exception ex){
    Log.e(TAG, ex.getLocalizedMessage());
}finally {
    isRecording = false;
     jsonObjectMessageToBroadcast = new JSONObject();
    try {
        jsonObjectMessageToBroadcast.put("messageType", "RECORDING_FINISHED");
        jsonObjectMessageToBroadcast.put("messageToDisplay", "Recording has finished");
    } catch (JSONException e) {
        e.printStackTrace();
    }
    Util.broadcastAMessage(context, "MANAGE_RECORDINGS", jsonObjectMessageToBroadcast);
}

    }

    public static boolean uploadFiles(Context context){
        String messageToDisplay = "";
        JSONObject jsonObjectMessageToBroadcast = new JSONObject();
        try {
            jsonObjectMessageToBroadcast.put("messageType", "UPLOADING_RECORDINGS");
            jsonObjectMessageToBroadcast.put("messageToDisplay", "Uploading recordings");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Util.broadcastAMessage(context, "MANAGE_RECORDINGS", jsonObjectMessageToBroadcast);

       // Util.broadcastAMessage(context, "about_to_upload_files");
        boolean returnValue = true;
        try {
            File recordingsFolder = Util.getRecordingsFolder(context);
            if (recordingsFolder == null){

                Log.e(TAG,"Error getting recordings folder." );
                return false;
            }
            File recordingFiles[] = recordingsFolder.listFiles();
            if (recordingFiles != null) {
                Util.disableFlightMode(context);

                // Now wait for network connection as setFlightMode takes a while
                if (!Util.waitForNetworkConnection(context, true)){

                    Log.e(TAG, "Failed to disable airplane mode");
                    return false;
                }


                // Check here to see if can connect to server and abort (for all files) if can't
                // Check that there is a JWT (JSON Web Token)

                Prefs prefs = new Prefs(context);

                // check to see if webToken needs updating
                boolean tokenIsCurrent = Util.isWebTokenCurrent(prefs);


                if ((prefs.getToken() == null) || !tokenIsCurrent) {

                    if (!Server.login(context)) {
                        Log.w(TAG, "sendFile: no JWT. Aborting upload");
                        return false; // Can't upload without JWT, login/register device to get JWT.
                    }
                }


                for (File aFile : recordingFiles) {

                    if (sendFile(context, aFile)) {
                        // deleting files can cause app to crash when phone connected to pc, so put in try catch
                        boolean fileSuccessfullyDeleted = false;
                        try {
                            fileSuccessfullyDeleted = aFile.delete();

                        }catch (Exception ex){

                            Log.e(TAG, ex.getLocalizedMessage());

                        }
                        if (!fileSuccessfullyDeleted) {
                            // for some reason file did not delete so exit for loop

                            Log.e(TAG, "Failed to delete file");
                            returnValue = false;
                            break;
                        }
                    } else {
                       // returnValue = false;

                        Log.e(TAG, "Failed to upload file to server");
                        return false;
                    }

                }
            }
            if (returnValue){
                 messageToDisplay = "";
                 jsonObjectMessageToBroadcast = new JSONObject();
                try {
                    jsonObjectMessageToBroadcast.put("messageType", "UPLOADING_FINISHED");
                    jsonObjectMessageToBroadcast.put("messageToDisplay", "Files have been successfully uploaded to the server.");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Util.broadcastAMessage(context, "MANAGE_RECORDINGS", jsonObjectMessageToBroadcast);
              //  Util.broadcastAMessage(context, "files_successfully_uploaded");
            }
            return returnValue;
        }catch (Exception ex){


            Log.e(TAG, ex.getLocalizedMessage());
            return false;
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

        if (fileNameParts.length != 18) {
          if (aFile.delete()){
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

        if (airplaneMode.equalsIgnoreCase("apModeOn")) {
            airplaneModeOn = true;
        }

        String localFilePath = Util.getRecordingsFolder(context) + "/" + fileName;
        if (! new File(localFilePath).exists()){

            Log.e(TAG,localFilePath + " does not exist" );
            return false;
        }
        //https://stackoverflow.com/questions/11399491/java-timezone-offset
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = GregorianCalendar.getInstance(tz);
        int offsetInMillis = tz.getOffset(cal.getTimeInMillis());
//        String offset = String.format("%02d:%02d", Math.abs(offsetInMillis / 3600000), Math.abs((offsetInMillis / 60000) % 60));
        String offset = String.format(Locale.ENGLISH,"%02d:%02d", Math.abs(offsetInMillis / 3600000), Math.abs((offsetInMillis / 60000) % 60)); // added in Locale.ENGLISH to stop Lint warning
        offset = (offsetInMillis >= 0 ? "+" : "-") + offset;

        String recordingDateTime = year + "-" + month + "-" + day + "T" + hour + ":" + minute + ":" + second+offset;
      //  String recordingTime = hour + ":" + minute + ":" + second; // 7/12/12 Agreed with Cameron to stop sending this

        try {

            JSONArray location = new JSONArray();
            location.put(Double.parseDouble(latStr));
            location.put(Double.parseDouble(lonStr));

            audioRecording.put("location", location);
            audioRecording.put("duration", recordTimeSeconds);
            audioRecording.put("localFilePath", localFilePath);
            audioRecording.put("recordingDateTime", recordingDateTime);
            audioRecording.put("batteryCharging", batteryStatus);
            audioRecording.put("batteryLevel", batteryLevel);
            audioRecording.put("airplaneModeOn", airplaneModeOn);
            audioRecording.put("type", "audio");

            if (relativeTo.equalsIgnoreCase("rToDawn")) {
                audioRecording.put("relativeToDawn", relativeToOffset);
            }
            if (relativeTo.equalsIgnoreCase("rToDusk")) {
                audioRecording.put("relativeToDusk", relativeToOffset);
            }

            String versionName = BuildConfig.VERSION_NAME;
            audioRecording.put("version", versionName);

            JSONObject additionalMetadata = new JSONObject();
            additionalMetadata.put("Android API Level", Build.VERSION.SDK_INT);
            additionalMetadata.put("App has root access", prefs.getHasRootAccess());
            additionalMetadata.put("Phone manufacturer", Build.MANUFACTURER);
            additionalMetadata.put("Phone model", Build.MODEL);

            TelephonyManager mTelephonyManager = (TelephonyManager) context
                    .getSystemService(Service.TELEPHONY_SERVICE);
            if (mTelephonyManager == null){
                Log.e(TAG, "mTelephonyManager is null");
                return false;
            }

            additionalMetadata.put("SIM state",Util.getSimStateAsString( mTelephonyManager.getSimState()));
            if (mTelephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY) {
                additionalMetadata.put("SimOperatorName", mTelephonyManager.getSimOperatorName());
            }

            String simImei = "Unknown";
            try {
                if (android.os.Build.VERSION.SDK_INT >= 26) {
                    simImei=mTelephonyManager.getImei();
                }
                else
                {
                    simImei=mTelephonyManager.getDeviceId();
                }
            }catch (SecurityException ex){
                Log.e(TAG, ex.getLocalizedMessage());
            }
            additionalMetadata.put("SIM IMEI", simImei);

            audioRecording.put("additionalMetadata", additionalMetadata);

        } catch (JSONException ex) {
            Log.e(TAG,ex.getLocalizedMessage() );
        }
        return Server.uploadAudioRecording(aFile, audioRecording, context);
    }
}
