package nz.org.cacophonoy.cacophonometerlite;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class Record implements Runnable {
    private static final String LOG_TAG = Record.class.getName();

    private static final long RECORD_TIME = 6 * 1000; // six seconds


    private Context context = null;
    private Handler handler = null;

    // Params to add: duration,
    Record(Context context, Handler handler) {
        this.context = context;
        this.handler =handler;
    }

    @Override
    public void run() {
        if (context == null || handler == null) {
            Log.e(LOG_TAG, "Context or Handler were null.");
            return;
        }
        if (!Util.checkPermissionsForRecording(context)) {
            Log.e(LOG_TAG, "App does not have permission to record.");
            Message message = handler.obtainMessage();
            message.what = StartRecordingReceiver.NO_PERMISSIONS_TO_RECORD;
            message.sendToTarget();
            return;
        }
        makeRecording(handler);
        UploadFiles uf = new UploadFiles(context);
        uf.run();
    }


    private static boolean makeRecording(Handler handler){
        Log.i(LOG_TAG, "Make a recording");

        // Get recording file.
        Date date = new Date(System.currentTimeMillis());
        DateFormat fileFormat = new SimpleDateFormat("yyyy MM dd HH mm ss", Locale.UK);
        String fileName = fileFormat.format(date)+".3gp";
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
            Message message = handler.obtainMessage();
            message.what = StartRecordingReceiver.RECORDING_FAILED;
            message.sendToTarget();
            Log.e(LOG_TAG, "Setup recording failed.");
            return false;
        }

        // Send message that recording started.
        Message message = handler.obtainMessage();
        message.what = StartRecordingReceiver.RECORDING_STARTED;
        message.sendToTarget();

        // Start recording.
        mRecorder.start();

        // Sleep for duration of recording.
        try {
            Thread.sleep(RECORD_TIME);
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Failed sleeping in recording thread.");
            e.printStackTrace();
            return false;
        }

        // Stop recording.
        mRecorder.stop();
        mRecorder.release();

        // Send message that recording finished.
        message = handler.obtainMessage();
        message.what = StartRecordingReceiver.RECORDING_FINISHED;
        message.sendToTarget();

        // Give time for file to be saved.
        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}

