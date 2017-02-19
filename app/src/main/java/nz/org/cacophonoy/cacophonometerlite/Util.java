package nz.org.cacophonoy.cacophonometerlite;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.File;

class Util {
    private static final String LOG_TAG = Util.class.getName();

    private static File homeFile = null;
    private static File recordingFolder = null;
    private static final String DEFAULT_RECORDINGS_FOLDER = "recordings";

    static boolean checkPermissionsForRecording(Context context) {
        Log.d(LOG_TAG, "Checking permissions needed for recording.");
        if (context == null) {
            Log.e(LOG_TAG, "Context was null when checking permissions");
            return false;
        }
        boolean storagePermission =
                ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean microphonePermission =
                ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        boolean locationPermission =
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        return (storagePermission && microphonePermission && locationPermission);
    }

    static File getHomeFile() {
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

    static File getRecordingsFolder(){
        if (recordingFolder == null){
            recordingFolder = new File(getHomeFile(), DEFAULT_RECORDINGS_FOLDER);
            if (!recordingFolder.exists() && !recordingFolder.isDirectory() && !recordingFolder.mkdirs()){
                System.out.println("error with recording file");
                //TODO try to fix problem and if cant output error message then exit, maybe send error to server.
            }
        }
        return recordingFolder;
    }
}
