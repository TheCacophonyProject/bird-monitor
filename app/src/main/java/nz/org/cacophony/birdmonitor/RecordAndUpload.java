package nz.org.cacophony.birdmonitor;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import io.fabric.sdk.android.services.common.Crash;

import static nz.org.cacophony.birdmonitor.IdlingResourceForEspressoTesting.recordIdlingResource;
import static nz.org.cacophony.birdmonitor.views.ManageRecordingsFragment.MANAGE_RECORDINGS_ACTION;
import static nz.org.cacophony.birdmonitor.views.ManageRecordingsFragment.MessageType.*;


/**
 * This is where the action is - however the code starts, it gets here to do a recording and then
 * sends recording to server.
 */

public class RecordAndUpload {
    private static final String TAG = RecordAndUpload.class.getName();
    public static boolean isRecording = false;
    private static boolean cancelUploadingRecordings = false;

    private RecordAndUpload() {

    }

    static void doRecord(Context context, String typeOfRecording, String offset) {
        if (typeOfRecording == null) {
            Log.e(TAG, "typeOfRecording is null");
            return;
        }

        Prefs prefs = new Prefs(context);

        long recordTimeSeconds = Util.getRecordingDuration(context, typeOfRecording);


        if (isRecording) {
            String messageToDisplay = "Can not record, as a recording is already in progress.";
            MessageHelper.broadcastMessage(messageToDisplay, ALREADY_RECORDING, MANAGE_RECORDINGS_ACTION, context);
            return;
        } else {
            makeRecording(context, recordTimeSeconds, prefs.getPlayWarningSound(), typeOfRecording, offset);
        }


        // Checked that it has a webToken before trying to upload
        if (prefs.getToken() == null) {
            String messageToDisplay = "The Phone is NOT registered - could not upload the files.";
            MessageHelper.broadcastMessage(messageToDisplay, UPLOADING_FAILED_NOT_REGISTERED, MANAGE_RECORDINGS_ACTION, context);
        } else {
            // only upload recordings if sufficient time has passed since last upload
            long dateTimeLastUpload = prefs.getDateTimeLastUpload();
            long now = new Date().getTime();
            long timeIntervalBetweenUploads = 1000 * (long) prefs.getTimeBetweenUploadsSeconds();

            if ((now - dateTimeLastUpload) > timeIntervalBetweenUploads || Util.isUIRecording(typeOfRecording)) { // don't upload if not enough time has passed

                if (!prefs.getInternetConnectionMode().equalsIgnoreCase("offline")) { // don't upload if in offline mode
                    boolean uploadedFilesSuccessfully = uploadFiles(context);
                    if (uploadedFilesSuccessfully) {
                        prefs.setDateTimeLastUpload(now);
                    } else {

                        Log.e(TAG, "Files failed to upload");
                        String messageToDisplay = "Files failed to upload to server.";
                        MessageHelper.broadcastMessage(messageToDisplay, FAILED_RECORDINGS_NOT_UPLOADED, MANAGE_RECORDINGS_ACTION, context);
                    }

                }
            }
        }
    }

    private static void makeRecording(Context context, long recordTimeSeconds, boolean playWarningBeeps, String typeOfRecording, String offset) {
        recordIdlingResource.increment();
        isRecording = true;
        String messageToDisplay = "Getting ready to record.";
        MessageHelper.broadcastMessage(messageToDisplay, GETTING_READY_TO_RECORD, MANAGE_RECORDINGS_ACTION, context);
        String timeOfRecordingForBirdCountMessage = "";
        String locationForBirdCountMessage = "";
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

            DateFormat timeOnlyFormat = new SimpleDateFormat("hh:mm a", Locale.UK);
            timeOfRecordingForBirdCountMessage = timeOnlyFormat.format(date);

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

            NumberFormat numberFormat = new DecimalFormat("#.000000");
            double lat = prefs.getLatitude();
            double lon = prefs.getLongitude();
            String latStr = numberFormat.format(lat);
            String lonStr = numberFormat.format(lon);
            fileName += " " + latStr;
            fileName += " " + lonStr;

            if(offset!=null) {
                fileName += " " + offset + " " + prefs.getLong(offset);
            }
            locationForBirdCountMessage = latStr + " " + lonStr;

            if (Util.isBirdCountRecording(typeOfRecording)) {
                // Save this filename in Prefs so that User can add notes - which will be stored in a file with the same name but .json extension
                prefs.setLatestBirdCountRecordingFileNameNoExtension(fileName);
            }


            fileName += Util.getRecordingFileExtension();

            File file = new File(Util.getRecordingsFolder(context), fileName);
            String filePath = file.getAbsolutePath();


            // Setup audio recording settings.
            MediaRecorder mRecorder = new MediaRecorder();

            // Try to prepare recording.
            try {

                // Automatic gain control setting
                String audioSource = prefs.getAudioSource();

                switch (audioSource) {
                    case "CAMCORDER":
                        mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                        break;

                    case "DEFAULT":
                        mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
                        break;

                    case "MIC":
                        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        break;

                    case "UNPROCESSED":
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            mRecorder.setAudioSource(MediaRecorder.AudioSource.UNPROCESSED);
                        } else {
                            mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
                        }
                        break;

                    case "VOICE_COMMUNICATION":
                        mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
                        break;

                    case "VOICE_RECOGNITION":
                        mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
                        break;
                    default:
                        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                }


                mRecorder.setOutputFile(filePath);

                // Sampling configuration
                mRecorder.setAudioChannels(1);
                mRecorder.setAudioSamplingRate(16000);

                // Encoding configuration
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // MPEG_4 added in API 1
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC); // AAC added in API 10
                mRecorder.setAudioEncodingBitRate(256000);

                mRecorder.prepare();

            } catch (IllegalStateException | IOException ex) {
                Crashlytics.log(Log.ERROR, TAG, "Setup recording failed. Could be due to lack of sdcard. Could be due to phone connected to pc as usb storage");
                Log.e(TAG, ex.getLocalizedMessage(), ex);
                Crashlytics.logException(ex);
                return;
            }

            if (playWarningBeeps) {
                ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                toneGen1.startTone(ToneGenerator.TONE_CDMA_NETWORK_BUSY, 2000);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Log.e(TAG, ex.getLocalizedMessage(), ex);
                }
            }

            // Start recording.
            try {
                mRecorder.start();
                messageToDisplay = "Recording has started";
                MessageHelper.broadcastMessage(messageToDisplay, RECORDING_STARTED, MANAGE_RECORDINGS_ACTION, context);
            } catch (IllegalStateException e) {
                Crashlytics.logException(e);
                Log.e(TAG, "mRecorder.start " + e.getLocalizedMessage());
                return;
            }

            // The duration of the recording is controlled by sleeping this thread.

            // With the addition of the Bird Count feature, the ability to cancel the recording was
            // added.  It was looking too difficult to communicate with this thread from the GUI
            // so This was done, by continually checking if a flag in prefs had be raised.

            // Just in case this checking affects the actual time of a recording, I only used this
            // checking for the Bird Count recordings.

            if (Util.isBirdCountRecording(typeOfRecording)) {
                // Sleep for duration of recording modified to check if that no request to stop has be given (say from Bird Count)
                try {
                    long remainingRecordingTime = recordTimeSeconds * 1000;
                    while (remainingRecordingTime > 0 && !prefs.getCancelRecording()) {
                        Thread.sleep(1000);
                        remainingRecordingTime -= 1000;
                    }
                    if (prefs.getCancelRecording()) {
                        cancelRecording(mRecorder, context, file, prefs);
                        return;
                    }

                } catch (InterruptedException e) {
                    Log.e(TAG, "Failed sleeping in recording thread.");
                    return;
                }
            } else { // Is a non Bird Count recording
                // Sleep for duration of recording.
                try {
                    Thread.sleep(recordTimeSeconds * 1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Failed sleeping in recording thread.");
                    return;
                }
            }

            if (playWarningBeeps) {
                ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 70);
                toneGen1.startTone(ToneGenerator.TONE_CDMA_NETWORK_BUSY, 1000);
            }

            endRecording(mRecorder, context);
        } finally {
            if (isRecording) {
                if (Util.isBirdCountRecording(typeOfRecording)) {
                    messageToDisplay = "Recording successful at " + timeOfRecordingForBirdCountMessage + " and GPS " + locationForBirdCountMessage + " . Use the 'Advanced - Recordings' screen to upload the recordings when you have an internet connection.";
                } else {
                    messageToDisplay = "Recording has finished";
                }
                MessageHelper.broadcastMessage(messageToDisplay, RECORDING_FINISHED, MANAGE_RECORDINGS_ACTION, context);
            }
            recordIdlingResource.decrement();
            isRecording = false;
        }
    }

    private static void cancelRecording(MediaRecorder mRecorder, Context context, File file, Prefs prefs) {
        isRecording = false;
        prefs.setCancelRecording(false);
        endRecording(mRecorder, context);
        if (!file.delete()) {
            Log.w(TAG, "Failed to delete cancelled recording: " + file.getAbsolutePath());
        }
        String messageToDisplay = "Recording has been cancelled.";
        MessageHelper.broadcastMessage(messageToDisplay, RECORDING_FINISHED, MANAGE_RECORDINGS_ACTION, context);
    }

    private static void endRecording(MediaRecorder mRecorder, Context context) {
        // Stop recording.
        mRecorder.stop();

        //https://stackoverflow.com/questions/9609479/android-mediaplayer-went-away-with-unhandled-events
        mRecorder.reset();
        mRecorder.release();

        // Give time for file to be saved. (and play beeps)
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Log.e(TAG, "Failed sleeping in recording thread.", ex);
        }

        Util.setTimeThatLastRecordingHappened(context, new Date().getTime());
    }

    public static boolean uploadFiles(Context context) {
        String messageToDisplay = "Preparing to upload.";
        MessageHelper.broadcastMessage(messageToDisplay, PREPARING_TO_UPLOAD, MANAGE_RECORDINGS_ACTION, context);
        boolean returnValue = true;
        File recordingsFolder = Util.getRecordingsFolder(context);
        if (recordingsFolder == null) {
            Log.e(TAG, "Error getting recordings folder.");
            return false;
        }

        File recordingFiles[] = recordingsFolder.listFiles();
        if (recordingFiles != null) {
            Util.disableFlightMode(context);

            // Now wait for network connection as setFlightMode takes a while
            if (!Util.waitForNetworkConnection(context, true)) {
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
                if (isCancelUploadingRecordings()) {
                    break;
                }

                if (sendFile(context, aFile)) {

                    // deleting files can cause app to crash when phone connected to pc, so put in try catch
                    boolean fileSuccessfullyDeleted = false;
                    try {

                        String recordingFileNameWithOutPath = aFile.getName();

                        fileSuccessfullyDeleted = aFile.delete();

                        if (fileSuccessfullyDeleted) {
                            // Send a broadcast to inform GUI that the number of files on phone has changed
                            MessageHelper.broadcastMessage("", RECORDING_DELETED, MANAGE_RECORDINGS_ACTION, context);

                            // Delete the recording notes file if it exists.
                            String recordingFileExtension = Util.getRecordingFileExtension();
                            String recordingFileNameWithOutPathOrExtension = recordingFileNameWithOutPath.split(recordingFileExtension)[0];
                            String notesFileName = recordingFileNameWithOutPathOrExtension + ".json";
                            String notesFilePathName = Util.getRecordingNotesFolder(context) + "/" + notesFileName;

                            File notesFile = new File(notesFilePathName);

                            if (notesFile.exists()) {
                                notesFile.delete();

                                // If this file was the latest bird count file, then need to set the latest bird count file to null
                                String fileNameOfLatestBirdCountFile = prefs.getLatestBirdCountRecordingFileNameNoExtension() + ".json";
                                if (notesFileName.equals(fileNameOfLatestBirdCountFile)) {
                                    prefs.setLatestBirdCountRecordingFileNameNoExtension(null);
                                }
                            }
                        }


                    } catch (Exception ex) {
                        Log.e(TAG, ex.getLocalizedMessage(), ex);
                        Crashlytics.logException(ex);
                    }
                    if (!fileSuccessfullyDeleted) {
                        // for some reason file did not delete so exit for loop

                        Log.e(TAG, "Failed to delete file");
                        returnValue = false;
                        break;
                    }
                } else {
                    // Did not upload, but reason may have been that the user pressed cancel
                    if (!isCancelUploadingRecordings()) {
                        Log.e(TAG, "Failed to upload file to server");
                    }
                    return false;
                }
            }
        }
        if (returnValue) {
            messageToDisplay = "Recordings have been successfully uploaded to the server.";
            MessageHelper.broadcastMessage(messageToDisplay, UPLOADING_FINISHED, MANAGE_RECORDINGS_ACTION, context);
        }
        return returnValue;

    }

    @SuppressLint("HardwareIds")
    private static boolean sendFile(Context context, File aFile) {
        Prefs prefs = new Prefs(context);

        // Get metadata and put into JSON.
        JSONObject audioRecording = new JSONObject();

        String fileName = aFile.getName();


        // http://stackoverflow.com/questions/3481828/how-to-split-a-string-in-java
        //http://stackoverflow.com/questions/3387622/split-string-on-dot-as-delimiter
        String[] fileNameParts = fileName.split("[. ]");
        // this code breaks if old files exist, so delete them and move on

        if (fileNameParts.length < 18) {
            if (aFile.delete()) {
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
        if (!new File(localFilePath).exists()) {

            Log.e(TAG, localFilePath + " does not exist");
            return false;
        }
        //https://stackoverflow.com/questions/11399491/java-timezone-offset
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = GregorianCalendar.getInstance(tz);
        int offsetInMillis = tz.getOffset(cal.getTimeInMillis());
        String offset = String.format(Locale.ENGLISH, "%02d:%02d", Math.abs(offsetInMillis / 3600000), Math.abs((offsetInMillis / 60000) % 60)); // added in Locale.ENGLISH to stop Lint warning
        offset = (offsetInMillis >= 0 ? "+" : "-") + offset;

        String recordingDateTime = year + "-" + month + "-" + day + "T" + hour + ":" + minute + ":" + second + offset;

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
            if(fileNameParts.length==20){
                additionalMetadata.put(fileNameParts[17], fileNameParts[18]);
            }
            // Add the recording notes if they exist.
            String recordingFileExtension = Util.getRecordingFileExtension();
            String recordingFileWithOutExtension = fileName.split(recordingFileExtension)[0];
            String notesFileName = recordingFileWithOutExtension + ".json";
            String notesFilePath = Util.getRecordingNotesFolder(context) + "/" + notesFileName;

            File notesFile = new File(notesFilePath);

            if (notesFile.exists()) {
                JSONObject jsonNotes = Util.getNotesFromNoteFile(notesFile);
                additionalMetadata.put("user-entered", jsonNotes);
            }

            TelephonyManager mTelephonyManager = (TelephonyManager) context
                    .getSystemService(Service.TELEPHONY_SERVICE);
            if (mTelephonyManager == null) {
                Log.e(TAG, "mTelephonyManager is null");
                return false;
            }

            additionalMetadata.put("SIM state", Util.getSimStateAsString(mTelephonyManager.getSimState()));
            if (mTelephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY) {
                additionalMetadata.put("SimOperatorName", mTelephonyManager.getSimOperatorName());
            }

            String simImei = "Unknown";
            try {
                if (android.os.Build.VERSION.SDK_INT >= 26) {
                    simImei = mTelephonyManager.getImei();
                } else {
                    simImei = mTelephonyManager.getDeviceId();
                }
            } catch (SecurityException ex) {
                Log.e(TAG, ex.getLocalizedMessage(), ex);
            }
            additionalMetadata.put("SIM IMEI", simImei);

            audioRecording.put("additionalMetadata", additionalMetadata);

        } catch (JSONException ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
        return Server.uploadAudioRecording(aFile, audioRecording, context);
    }

    public static boolean isCancelUploadingRecordings() {
        return cancelUploadingRecordings;
    }

    public static void setCancelUploadingRecordings(boolean cancelUploadingRecordings2) {
        cancelUploadingRecordings = cancelUploadingRecordings2;
    }
}
