package nz.org.cacophony.cacophonometer;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

//import android.util.Log;

/**
 * Recordings made from the record now button needed to run in a thread
 */

class MainThread implements Runnable {

    private static final String TAG = MainThread.class.getName();


    private final Context context;

    private final String alarmIntentType;


    MainThread(Context context, String alarmIntentType) {
        this.context = context;
        this.alarmIntentType = alarmIntentType;

    }
    @Override
    public void run() {
        Log.d(TAG, "MainThread 1");
        Looper.prepare();
        //        if (context == null || handler == null) {
        if (context == null ) {
            Log.w(TAG, "Context or Handler were null.");


            return;
        }
        if (!Util.checkPermissionsForRecording(context)) {
            Log.e(TAG, "App does not have permission to record.");

            String messageToDisplay = "";
            JSONObject jsonObjectMessageToBroadcast = new JSONObject();
            try {
                jsonObjectMessageToBroadcast.put("messageType", "NO_PERMISSION_TO_RECORD");
                jsonObjectMessageToBroadcast.put("messageToDisplay", "You do not have permission to record.");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Util.broadcastAMessage(context, "MANAGE_RECORDINGS", jsonObjectMessageToBroadcast);

           // Util.broadcastAMessage(context, "no_permission_to_record");

            return;
        }


        String recordAndUploadedSuccessfully;
        try {

            recordAndUploadedSuccessfully =  RecordAndUpload.doRecord(context, alarmIntentType );

        }catch (Exception e){
            String messageToDisplay = "";
            JSONObject jsonObjectMessageToBroadcast = new JSONObject();
            try {
                jsonObjectMessageToBroadcast.put("messageToType", "RECORD_AND_UPLOAD_FAILED");
                jsonObjectMessageToBroadcast.put("messageToDisplay", e.getLocalizedMessage());
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            Util.broadcastAMessage(context, "MANAGE_RECORDINGS", jsonObjectMessageToBroadcast);

          //  Util.broadcastAMessage(context, "recording_failed");

            return;
        }



        Looper.loop();
    }
}
