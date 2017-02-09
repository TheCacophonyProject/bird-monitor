package nz.org.cacophonoy.cacophonometerlite;



import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import static nz.org.cacophonoy.cacophonometerlite.RegisterActivity.token;


public class UploadFiles implements Runnable{ // http://stackoverflow.com/questions/15666260/urlconnection-android-4-2-doesnt-work

    private static final String LOG_TAG = "UploadFiles";

//    public static void uploadFiles(){
//
//        File recordingsFolder = Record.getRecordingsFolder();
//        File recordingFiles[] = recordingsFolder.listFiles();
//        for (File aFile : recordingFiles ){
//            System.out.println("File name is " + aFile.getName());
//            sendFile(aFile);
//            aFile.delete();
//        }
//    }

    static void sendFile(File aFile){
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

        String localFilePath = "/data/data/com.thecacophonytrust.cacophonometer/app_cacophony/recordings/" +  fileName;
        String recordingDateTime = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
        String recordingTime = hour + ":" + minute + ":" + second;


        try {
            audioRecording.put("duration", 6);
            audioRecording.put("localFilePath", localFilePath);
            audioRecording.put("recordingDateTime", recordingDateTime);
            audioRecording.put("recordingTime", recordingTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Server.uploadAudioRecording(aFile, audioRecording);
    }

    @Override
    public void run() {
        File recordingsFolder = nz.org.cacophonoy.cacophonometerlite.Record.getRecordingsFolder();
        File recordingFiles[] = recordingsFolder.listFiles();
        for (File aFile : recordingFiles ){
            System.out.println("File name is " + aFile.getName());
            sendFile(aFile);
            aFile.delete();
        }
    }
}
