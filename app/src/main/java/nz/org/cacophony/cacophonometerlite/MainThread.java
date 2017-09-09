package nz.org.cacophony.cacophonometerlite;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
//import android.util.Log;

import org.slf4j.Logger;

import static android.R.id.message;
import static com.loopj.android.http.AsyncHttpClient.LOG_TAG;
import static nz.org.cacophony.cacophonometerlite.RecordAndUpload.doRecord;

/**
 * Created by User on 29-Mar-17.
 * Recordings made from the test button needed to run in a thread
 */

class MainThread implements Runnable {

    private static final String TAG = MainThread.class.getName();
    // --Commented out by Inspection (12-Jun-17 1:56 PM):private static long recordTimeSeconds = 0; //  set it later

    private Context context = null;
    private Handler handler = null;
//    private static Logger logger = null;

    // Params to add: duration,
    MainThread(Context context, Handler handler) {
        this.context = context;
        this.handler =handler;

    }
    @Override
    public void run() {
        Looper.prepare();
        //        if (context == null || handler == null) {
        if (context == null ) {
            Log.w(TAG, "Context or Handler were null.");
//            logger.warn("Context or Handler were null.");

            return;
        }
        if (!Util.checkPermissionsForRecording(context)) {
            Log.e(TAG, "App does not have permission to record.");
//            Util.writeLocalLogEntryUsingLogback(context, LOG_TAG, "App does not have permission to record.");
//            logger.error("App does not have permission to record.");
            if (handler != null) {
                Message message = handler.obtainMessage();
                message.what = StartRecordingReceiver.NO_PERMISSIONS_TO_RECORD;
                message.sendToTarget();
            }
            return;
        }

        Message message = handler.obtainMessage();
        message.what = StartRecordingReceiver.RECORDING_STARTED;
        message.sendToTarget();

        String recordAndUploadedSuccessfully = null;
        try {

            recordAndUploadedSuccessfully =  RecordAndUpload.doRecord(context, "testButton",handler);
        }catch (Exception e){
            message = handler.obtainMessage();
            message.what = StartRecordingReceiver.RECORDING_FAILED;
            message.sendToTarget();
            return;
        }
        message = handler.obtainMessage();
        if (recordAndUploadedSuccessfully.equalsIgnoreCase("recorded successfully")){
            message.what = StartRecordingReceiver.RECORDING_FINISHED;

        }else if (recordAndUploadedSuccessfully.equalsIgnoreCase("recorded and uploaded successfully")){
            message.what = StartRecordingReceiver.RECORDING_AND_UPLOADING_FINISHED;

        }else if (recordAndUploadedSuccessfully.equalsIgnoreCase("recorded BUT did not upload")){
            message.what = StartRecordingReceiver.RECORDING_FINISHED_BUT_UPLOAD_FAILED;

        }else if (recordAndUploadedSuccessfully.equalsIgnoreCase("recorded successfully no network")){
            message.what = StartRecordingReceiver.RECORDING_FINISHED_NO_NETWORK;

        }
        else{
            message.what = StartRecordingReceiver.RECORDING_FAILED;
        }

        message.sendToTarget();
        Looper.loop();
    }
}
