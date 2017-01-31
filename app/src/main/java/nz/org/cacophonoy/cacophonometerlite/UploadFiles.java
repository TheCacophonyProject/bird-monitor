package nz.org.cacophonoy.cacophonometerlite;



import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;




public class UploadFiles implements Runnable{ // http://stackoverflow.com/questions/15666260/urlconnection-android-4-2-doesnt-work

    private static final String LOG_TAG = "UploadFiles";

    private static final String TWO_HYPHENS = "--";
    private static final String LINE_END = "\r\n";
    private static URL url = null;
    private static final String DEFAULT_URL = "http://52.64.67.145:8888";
    public static final String API_URL = "/api/v1/audiorecordings";

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

        String response;
        int responseCode;
//        Logger.d(LOG_TAG, "Starting post");
//        Logger.d(LOG_TAG, "URL: " + url);
        String boundary = Long.toHexString(System.currentTimeMillis());
        try {

            String urlString = DEFAULT_URL + API_URL;
            url = new URL(urlString);
            URLConnection urlConn = url.openConnection();
            urlConn.setDoInput (true);
            urlConn.setDoOutput (true);
            urlConn.setUseCaches (false);

            urlConn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream request = new DataOutputStream (urlConn.getOutputStream ());
            String key = "data";

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
//                audioRecording.put("localFilePath", "/data/data/com.thecacophonytrust.cacophonometer/app_cacophony/recordings/20161129 144200.3gp");
                audioRecording.put("localFilePath", localFilePath);
//                audioRecording.put("recordingDateTime", "2016-12-01 21:44:00");
                audioRecording.put("recordingDateTime", recordingDateTime);
//                audioRecording.put("recordingTime", "21:44:00");
                audioRecording.put("recordingTime", recordingTime);
                audioRecording.put("group", "tim"); // this didn't work (ie Group did not appear on website)
            } catch (JSONException e) {
                e.printStackTrace();
            }



            //   String value = data.toString();
            String value = audioRecording.toString();

            //  Logger.d(LOG_TAG, value);
            request.writeBytes(TWO_HYPHENS + boundary + LINE_END);
            request.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + LINE_END);
            request.writeBytes("Content-Type: text/plain; charset=UTF-8" + LINE_END);
            request.writeBytes(LINE_END);
            request.writeBytes(value + LINE_END);

            request.writeBytes(TWO_HYPHENS + boundary + LINE_END);
            request.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""+aFile.getName()+"\"" + LINE_END); //TODO change file name etc...
            request.writeBytes(LINE_END);
            FileInputStream fileInputStream = new FileInputStream(aFile);
            if (!aFile.exists()){
                Log.e(LOG_TAG, "File to upload does NOT exist");
            }
            int bytesRead, bytesAvailable, bufferSize;
            int maxBufferSize = 1024 * 1024;
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer;
            buffer = new byte[bufferSize];
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                request.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            request.writeBytes(LINE_END + TWO_HYPHENS + boundary + TWO_HYPHENS + LINE_END);
            request.flush();
            request.close();
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), "UTF-8"));
            responseCode = ((HttpURLConnection) urlConn).getResponseCode();
            //   Logger.d(LOG_TAG, "Response code: " + responseCode);
            Log.e(LOG_TAG, "Response code: " + responseCode);
            response = "";
            String line;
            try {
                while ((line = inputStream.readLine()) != null){
                    response += line;
                }
            } catch (IOException e) {
                //   Logger.d(LOG_TAG, e.toString());
                //    Logger.exception(LOG_TAG, e);
                //    UploadManager.errorWithAudioUpload(recordingKey, "Error with getting response from server");
                Log.e(LOG_TAG, "Error with getting response from server");
                return;
            }
        } catch (IOException e) {
//            Logger.e(LOG_TAG, "Error with uploading data.");
//            Logger.exception(LOG_TAG, e);
//            UploadManager.errorWithAudioUpload(recordingKey, "Error with uploading data");
            Log.e(LOG_TAG, "Error with getting response from server");
            return;
        }
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
