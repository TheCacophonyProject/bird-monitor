package nz.org.cacophonoy.cacophonometerlite;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import static android.R.id.message;
import static nz.org.cacophonoy.cacophonometerlite.StartRecordingReceiver.intentTimeUriMessage;
import static nz.org.cacophonoy.cacophonometerlite.Util.isNetworkConnected;

class Record implements Runnable {
    private static final String LOG_TAG = Record.class.getName();

  //  private static final long RECORD_TIME = 6 * 1000; // six seconds
  //  private static final long RECORD_TIME = 100; // 0.1 second
    private static long recordTimeSeconds = 0; //  set it later



    private Context context = null;
    private Handler handler = null;


    // Params to add: duration,
    Record(Context context, Handler handler) {
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
        Prefs prefs = new Prefs(context);
        recordTimeSeconds =  (long)prefs.getRecordingDuration();
        makeRecording(handler, context);
        if (Util.isAirplaneModeOn(context)){
            Log.d(LOG_TAG, "Airplane Mode is On");

            Util.disableAirplaneMode(context);
            Log.d(LOG_TAG, "Have just disabled Airplane Mode");
            Log.d(LOG_TAG, "Is there a network connection? " + isNetworkConnected(context));


//            while (!isNetworkConnected(context)) {
//                Log.d(LOG_TAG, "Pausing for a Network connection ");
//                try {
//                    Thread.sleep(500); // give time for airplane mode to turn on
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                Log.d(LOG_TAG, "Network connection? " + isNetworkConnected(context));
//            }
        }
         UploadFiles uf = new UploadFiles(context);
        uf.run();
    }


    private static boolean makeRecording(Handler handler, Context context){
        Log.i(LOG_TAG, "Make a recording");


        // Get recording file.
        Date date = new Date(System.currentTimeMillis());
        // Calculate dawn and dusk offset in seconds will be sent to server to allow queries on this data
        Calendar nowToday =  new GregorianCalendar(TimeZone.getTimeZone("Pacific/Auckland"));

        Calendar dawn = Util.getDawn(context, nowToday);
        System.out.println("dawn " + dawn);
//        long relativeToDawn  = dawn.getTimeInMillis() - nowToday.getTimeInMillis();
        long relativeToDawn  =  nowToday.getTimeInMillis() - dawn.getTimeInMillis();
        relativeToDawn  = relativeToDawn /1000; // now in seconds

        Calendar dusk = Util.getDusk(context, nowToday);
//        long relativeToDusk  = dusk.getTimeInMillis() - nowToday.getTimeInMillis();
        long relativeToDusk  = nowToday.getTimeInMillis() - dusk.getTimeInMillis();
        relativeToDusk  = relativeToDusk /1000; // now in seconds

        DateFormat fileFormat = new SimpleDateFormat("yyyy MM dd HH mm ss", Locale.UK);
        String fileName = fileFormat.format(date);

        if (Math.abs(relativeToDawn) < Math.abs(relativeToDusk)){
            fileName += " relativeToDawn " + relativeToDawn;
        }else{
            fileName += " relativeToDusk " + relativeToDusk;
        }

        if (Util.isAirplaneModeOn(context)){
            fileName += " airplaneModeOn";
        }else{
            fileName += " airplaneModeOff";
        }

        String batteryStatus = Util.getBatteryStatus(context);
        fileName += " " + batteryStatus;
        double batteryLevel = Util.getBatteryLevel(context);
        fileName += " " + batteryLevel;
        fileName += ".3gp";

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
            if (handler != null) {
                Message message = handler.obtainMessage();
                message.what = StartRecordingReceiver.RECORDING_FAILED;
                message.sendToTarget();
                Log.e(LOG_TAG, "Setup recording failed.");
                Log.e(LOG_TAG, "Could be due to lack of sdcard");
            }
            return false;
        }

        // Send message that recording started.
        if (handler != null) {
            Message message = handler.obtainMessage();
            message.what = StartRecordingReceiver.RECORDING_STARTED;
            message.sendToTarget();
        }
        // Start recording.
        try {
            mRecorder.start();
        }catch (Exception e){
            System.out.println("mRecorder.start " + e.getLocalizedMessage());
            return false;
        }

        // Sleep for duration of recording.
        try {
            Thread.sleep(recordTimeSeconds * 1000);
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Failed sleeping in recording thread.");
            e.printStackTrace();
            return false;
        }

        // Stop recording.
        mRecorder.stop();
        mRecorder.release();

        // Send message that recording finished.
        if (handler != null) {
          Message  message = handler.obtainMessage();
            message.what = StartRecordingReceiver.RECORDING_FINISHED;
            message.sendToTarget();
        }
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

