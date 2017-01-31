package nz.org.cacophonoy.cacophonometerlite;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by tim on 6/12/2016.
 */

public class Record {

    // private static final String LOG_TAG = "Audio";
    static long recordTime = 6 * 1000; // six seconds

    public static void makeRecording(){
        startRecording();
        try {
            Thread.sleep(recordTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stopRecording();
        try {
            Thread.sleep(5 * 1000); // give time for file to be saved
//            UploadFiles.uploadFiles();
            UploadFiles uploadFiles = new UploadFiles();
            Thread thread = new Thread(uploadFiles);
            thread.start();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    private static final String LOG_TAG = "Record";
    private static MediaRecorder mRecorder = null;
    private static File recordingFolder = null;
    private static File homeFile = null;
    private static final String DEFAULT_RECORDINGS_FOLDER = "recordings";

    private static void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//    String fileName = getFileName();
//        String recordingsFolder = getRecordingsFolder().getAbsolutePath();
//        mFileName = getRecordingsFolder().getAbsolutePath() + "/" +fileName;
////        mRecorder.setOutputFile(mFileName);
//        String fullFilePath = recordingsFolder +

        Date date = new Date(System.currentTimeMillis());
        //DateFormat fileFormat = new SimpleDateFormat("yyyyMMdd HHmmss", Locale.UK);
        DateFormat fileFormat = new SimpleDateFormat("yyyy MM dd HH mm ss", Locale.UK);
        String fileName = fileFormat.format(date)+".3gp";
        File file = new File(getRecordingsFolder(), fileName);
        String filePath = file.getAbsolutePath();
        mRecorder.setOutputFile(filePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private static void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    public static File getRecordingsFolder(){
        if (recordingFolder == null){
            recordingFolder = new File(getHomeFile(), DEFAULT_RECORDINGS_FOLDER);
            if (!recordingFolder.exists() && !recordingFolder.isDirectory() && !recordingFolder.mkdirs()){
                System.out.println("error with recording file");
                //TODO try to fix problem and if cant output error message then exit, maybe send error to server.
            }
        }
        return recordingFolder;
    }

    public static File getHomeFile() {
        // 15/8/16 Tim Hunt - Going to change file storage location to always use internal phone storage rather than rely on sdcard
        // This is because if sdcard is 'dodgy' can get inconsistent results.
        // Need context to get internal storage location, so need to pass this around the code.
        // If I could be sure the homeFile was set before any of the other directories are needed then I
        // wouldn't need to pass context around to the other methods :-(

        if (homeFile == null){
            homeFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/cacophony");
            //https://developer.android.com/reference/android/content/Context.html#getDir(java.lang.String, int)
            // homeFile = context.getDir("cacophony", Context.MODE_PRIVATE); // getDir creates the folder if it doesn't exist, but needs contect

            if (!homeFile.exists() && !homeFile.isDirectory() && !homeFile.mkdirs()){
                System.out.println("error with home file");
                //TODO, exit program safely from here and display error.
            }

        }
        return homeFile;
    }
}

