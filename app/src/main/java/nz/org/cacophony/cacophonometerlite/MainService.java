package nz.org.cacophony.cacophonometerlite;

import android.app.IntentService;


import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
//import android.util.Log;

import org.slf4j.Logger;

import static com.loopj.android.http.AsyncHttpClient.LOG_TAG;

/**
 * Created by User on 29-Mar-17.
 * Recordings need to happen from a service so needed this class
 */

public class MainService extends IntentService {
    private static final String TAG = MainService.class.getName();
//    private static Logger logger = null;

    // --Commented out by Inspection (12-Jun-17 1:56 PM):private static final String LOG_TAG = MainService.class.getName();

    public MainService(){
        super("MainService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
//        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
//        toneGen1.startTone(ToneGenerator.TONE_CDMA_NETWORK_BUSY, 2000);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MainServiceWakelockTag");
        wakeLock.acquire();

      try {


//          logger = Util.getAndConfigureLogger(getApplicationContext(),LOG_TAG);
          Bundle bundle = intent != null ? intent.getExtras() : null;
          if (bundle != null){
              String alarmIntentType = bundle.getString("type");
              if (alarmIntentType == null){
                  alarmIntentType = "unknown";

                  Log.w(TAG, "alarmIntentType = unknown");
              }






              RecordAndUpload.doRecord(getApplicationContext(),alarmIntentType);

          }else{
              Log.e(TAG, "MainService error");

          }

      }catch (Exception ex){

          Log.e(TAG,ex.getLocalizedMessage() );
      }finally {
          Util.enableFlightMode(getApplicationContext());
//           toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
//          toneGen1.startTone(ToneGenerator.TONE_CDMA_NETWORK_BUSY, 5000);
          wakeLock.release();
      }

    }


}
