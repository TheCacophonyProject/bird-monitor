package nz.org.cacophonoy.cacophonometerlite;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Created by User on 29-Mar-17.
 */

public class MainThread implements Runnable {

    private static final String LOG_TAG = MainThread.class.getName();
    private static long recordTimeSeconds = 0; //  set it later

    private Context context = null;
    private Handler handler = null;

    // Params to add: duration,
    MainThread(Context context, Handler handler) {
        this.context = context;
        this.handler =handler;

    }
    @Override
    public void run() {

        //        if (context == null || handler == null) {
        if (context == null ) {
            Log.e(LOG_TAG, "Context or Handler were null.");
            return;
        }
        if (!Util.checkPermissionsForRecording(context)) {
            Log.e(LOG_TAG, "App does not have permission to record.");
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

        try {
            RecordAndUpload.doRecord(context, "testButton");
        }catch (Exception e){
            message = handler.obtainMessage();
            message.what = StartRecordingReceiver.RECORDING_FAILED;
            message.sendToTarget();
            return;
        }
        message = handler.obtainMessage();
        message.what = StartRecordingReceiver.RECORDING_FINISHED;
        message.sendToTarget();

    }
}
