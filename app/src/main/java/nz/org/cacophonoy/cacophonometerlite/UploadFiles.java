package nz.org.cacophonoy.cacophonometerlite;


import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

class UploadFiles implements Runnable { // http://stackoverflow.com/questions/15666260/urlconnection-android-4-2-doesnt-work
    private static final String LOG_TAG = UploadFiles.class.getName();

    private Context context = null;


    UploadFiles(Context context) {
        if (context == null) {
            Log.e(LOG_TAG, "Making UploadFile without a context. context is needed for this.");
        }
        this.context = context;
    }


    private void sendFile(File aFile) {
        System.out.println("Going to upload file " + aFile.getName());
        Log.i(LOG_TAG, "sendFile: Start");

        // Get metadata and put into JSON.
        JSONObject audioRecording = new JSONObject();

        String fileName = aFile.getName();
        // http://stackoverflow.com/questions/3481828/how-to-split-a-string-in-java
        //http://stackoverflow.com/questions/3387622/split-string-on-dot-as-delimiter
        String[] fileNameParts = fileName.split("[. ]");

        String year = fileNameParts[0];
        String month = fileNameParts[1];
        String day = fileNameParts[2];
        String hour = fileNameParts[3];
        String minute = fileNameParts[4];
        String second = fileNameParts[5];

        String localFilePath = "/data/data/com.thecacophonytrust.cacophonometer/app_cacophony/recordings/" + fileName;
        String recordingDateTime = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
        String recordingTime = hour + ":" + minute + ":" + second;

        String  batteryStatus = Util.getBatteryStatus(context);
        float batteryLevel = Util.getBatteryLevel(context);

        boolean airplaneModeOn = Util.isAirplaneModeOn(context);


        Prefs prefs = new Prefs(context);

        try {
            audioRecording.put("location", "Lat: " + prefs.getLatitude() + ", Lon: " + prefs.getLongitude());
            audioRecording.put("duration", prefs.getRecordingDuration());
            audioRecording.put("localFilePath", localFilePath);
            audioRecording.put("recordingDateTime", recordingDateTime);
            audioRecording.put("recordingTime", recordingTime);

            audioRecording.put("batteryCharging", batteryStatus);
            audioRecording.put("batteryLevel", batteryLevel);
            audioRecording.put("airplaneModeOn", airplaneModeOn);
//            audioRecording.put("relativeToDawn", );
//            audioRecording.put("relativeToDusk", );

            // hack for now to see battery status
        //    audioRecording.put("location", "Battery status: " + batteryStatus + ", Battery Level: " + batteryLevel + ", airplaneModeOn: " + airplaneModeOn);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        Server.uploadAudioRecording(aFile, audioRecording, context);
    }

    @Override
    public void run() {

            File recordingsFolder = Util.getRecordingsFolder();
            File recordingFiles[] = recordingsFolder.listFiles();
            if (recordingFiles != null) {
                for (File aFile : recordingFiles) {
                    System.out.println("File name is " + aFile.getName());
                    sendFile(aFile);
                    if (!aFile.delete())
                        Log.w(LOG_TAG, "Deleting audio file failed");
                }
            }
                   }




}
