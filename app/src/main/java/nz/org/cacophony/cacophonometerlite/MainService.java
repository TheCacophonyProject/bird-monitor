package nz.org.cacophony.cacophonometerlite;

import android.app.IntentService;


import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
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

      try {
//          logger = Util.getAndConfigureLogger(getApplicationContext(),LOG_TAG);
          Bundle bundle = intent != null ? intent.getExtras() : null;
          if (bundle != null){
              String alarmIntentType = bundle.getString("type");
              if (alarmIntentType == null){
                  alarmIntentType = "unknown";
//                  Log.w(LOG_TAG, "alarmIntentType = unknown");
//                  Util.writeLocalLogEntryUsingLogback(getApplicationContext(), LOG_TAG, "alarmIntentType = unknown");
//                  logger.warn("alarmIntentType = unknown");
                  Log.w(TAG, "alarmIntentType = unknown");
              }
              if (alarmIntentType.equalsIgnoreCase("walkModeRepeating")){
                  Util.getToast(getApplicationContext(),"Prepare to start recording", false ).show();
                  ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                  toneGen1.startTone(ToneGenerator.TONE_CDMA_NETWORK_BUSY,10000);
                  try{
                      Thread.sleep(10000);
                  }catch (Exception ex){
                      Log.e(TAG, ex.getLocalizedMessage());
                  }
              }
//                  RecordAndUpload.doRecord(getApplicationContext(),alarmIntentType, null);
              RecordAndUpload.doRecord(getApplicationContext(),alarmIntentType);

          }else{
              Log.e(TAG, "MainService error");
//              Util.writeLocalLogEntryUsingLogback(getApplicationContext(), LOG_TAG, "MainService error");
//              logger.warn("MainService error");
          }

      }catch (Exception ex){
//          Util.writeLocalLogEntryUsingLogback(getApplicationContext(), LOG_TAG, ex.getLocalizedMessage());
//          logger.error(ex.getLocalizedMessage());
          Log.e(TAG,ex.getLocalizedMessage() );
      }finally {
          Util.enableFlightMode(getApplicationContext());
      }

    }


}
