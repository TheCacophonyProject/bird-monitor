package nz.org.cacophonoy.cacophonometerlite;


import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import static nz.org.cacophonoy.cacophonometerlite.Server.disableDataConnection;
import static nz.org.cacophonoy.cacophonometerlite.Server.enableDataConnection;

class UploadFiles implements Runnable { // http://stackoverflow.com/questions/15666260/urlconnection-android-4-2-doesnt-work
    private static final String LOG_TAG = UploadFiles.class.getName();

    private Context context = null;


    UploadFiles(Context context) {
        if (context == null) {
            Log.e(LOG_TAG, "Making UploadFile without a context. context is needed for this.");
        }
        this.context = context;
    }

    private boolean sendFile(File aFile) {
  //  private void sendFile(File aFile) {
        Log.i(LOG_TAG, "sendFile: Start");
        Prefs prefs = new Prefs(context);

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
        String relativeTo = fileNameParts[6];
        String relativeToOffset = fileNameParts[7];
        String airplaneMode = fileNameParts[8];
        boolean airplaneModeOn = false;
        if (airplaneMode.equalsIgnoreCase("airplaneModeOn")){
            airplaneModeOn =  true;
        }

        String localFilePath = "/data/data/com.thecacophonytrust.cacophonometer/app_cacophony/recordings/" + fileName;
        String recordingDateTime = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
        String recordingTime = hour + ":" + minute + ":" + second;
        String batteryStatus = Util.getBatteryStatus(context);
       // boolean airplaneModeOn = Util.isAirplaneModeOn(context);

        try {
            audioRecording.put("location", "Lat: " + prefs.getLatitude() + ", Lon: " + prefs.getLongitude());
            audioRecording.put("duration", prefs.getRecordingDuration());
            audioRecording.put("localFilePath", localFilePath);
            audioRecording.put("recordingDateTime", recordingDateTime);
            audioRecording.put("recordingTime", recordingTime);
            audioRecording.put("batteryCharging", batteryStatus);
            audioRecording.put("batteryLevel", prefs.getBatteryLevel());
            audioRecording.put("airplaneModeOn", airplaneModeOn);
            if (relativeTo.equalsIgnoreCase("relativeToDawn")) {
                audioRecording.put("relativeToDawn", relativeToOffset);
            }
            if (relativeTo.equalsIgnoreCase("relativeToDusk")) {
                audioRecording.put("relativeToDusk", relativeToOffset);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

//        Server.uploadAudioRecording(aFile, audioRecording, context);
        return Server.uploadAudioRecording(aFile, audioRecording, context);
    }

    @Override
    public void run() {

        File recordingsFolder = Util.getRecordingsFolder();
        File recordingFiles[] = recordingsFolder.listFiles();
        if (recordingFiles != null) {
            enableDataConnection(context);
          //  Toast.makeText(context, "Number of Files to upload is " + recordingFiles.length,  Toast.LENGTH_LONG).show();
            for (File aFile : recordingFiles) {
//                sendFile(aFile);
//                if (!aFile.delete())
//                    Log.w(LOG_TAG, "Deleting audio file failed");
               if (sendFile(aFile)){
                   if (!aFile.delete()){
                       Log.w(LOG_TAG, "Deleting audio file failed");
                   }
               }else{
                   Log.w(LOG_TAG, "Failed to upload file to server");
               }

            }
        }
        disableDataConnection(context);
    }


}
