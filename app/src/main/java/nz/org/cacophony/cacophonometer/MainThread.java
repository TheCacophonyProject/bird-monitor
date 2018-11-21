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
                jsonObjectMessageToBroadcast.put("messageToType", "no_permission_to_record");
                jsonObjectMessageToBroadcast.put("messageToDisplay", "no_permission_to_record");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Util.broadcastAMessage(context, jsonObjectMessageToBroadcast);

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
                jsonObjectMessageToBroadcast.put("messageToType", "recording_failed");
                jsonObjectMessageToBroadcast.put("messageToDisplay", "recording_failed");
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            Util.broadcastAMessage(context, jsonObjectMessageToBroadcast);

          //  Util.broadcastAMessage(context, "recording_failed");

            return;
        }

        if (recordAndUploadedSuccessfully.equalsIgnoreCase("recorded successfully")){
//            String messageToDisplay = "";
//            JSONObject jsonObjectMessageToBroadcast = new JSONObject();
//            try {
//                jsonObjectMessageToBroadcast.put("messageToType", "recording_finished");
//                jsonObjectMessageToBroadcast.put("messageToDisplay", "recording_finished");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            Util.broadcastAMessage(context,TAG + ".run", jsonObjectMessageToBroadcast);

           // Util.broadcastAMessage(context, "recording_finished");

        }else if (recordAndUploadedSuccessfully.equalsIgnoreCase("recorded and uploaded successfully")){
//            String messageToDisplay = "";
//            JSONObject jsonObjectMessageToBroadcast = new JSONObject();
//            try {
//                jsonObjectMessageToBroadcast.put("messageToType", "recording_and_uploading_finished");
//                jsonObjectMessageToBroadcast.put("messageToDisplay", "recording_and_uploading_finished");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            Util.broadcastAMessage(context,TAG + ".run", jsonObjectMessageToBroadcast);

           // Util.broadcastAMessage(context, "recording_and_uploading_finished");

        }else if (recordAndUploadedSuccessfully.equalsIgnoreCase("recorded BUT did not upload")){
//            String messageToDisplay = "";
//            JSONObject jsonObjectMessageToBroadcast = new JSONObject();
//            try {
//                jsonObjectMessageToBroadcast.put("messageToType", "recording_finished_but_uploading_failed");
//                jsonObjectMessageToBroadcast.put("messageToDisplay", "recording_finished_but_uploading_failed");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            Util.broadcastAMessage(context,TAG + ".run", jsonObjectMessageToBroadcast);

           // Util.broadcastAMessage(context, "recording_finished_but_uploading_failed");

        }else if (recordAndUploadedSuccessfully.equalsIgnoreCase("recorded successfully no network")){

          //  Util.broadcastAMessage(context, "recorded_successfully_no_network");
        }else if (recordAndUploadedSuccessfully.equalsIgnoreCase("not logged in")){
          //  Util.broadcastAMessage(context, "not_logged_in");
        }else if (recordAndUploadedSuccessfully.equalsIgnoreCase("isRecording")){
          //  Util.broadcastAMessage(context, "is_already_recording");
        }
        else{

         //   Util.broadcastAMessage(context, "recording_failed");
        }


        Looper.loop();
    }
}
