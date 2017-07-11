package nz.org.cacophony.cacophonometerlite;

import android.app.IntentService;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
//import android.util.Log;

import org.slf4j.Logger;

/**
 * Created by User on 29-Mar-17.
 * Recordings need to happen from a service so needed this class
 */

public class MainService extends IntentService {
    private static final String LOG_TAG = MainService.class.getName();
    private static Logger logger = null;

    // --Commented out by Inspection (12-Jun-17 1:56 PM):private static final String LOG_TAG = MainService.class.getName();

    public MainService(){
        super("MainService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

      try {
          logger = Util.getAndConfigureLogger(getApplicationContext(),LOG_TAG);
          Bundle bundle = intent != null ? intent.getExtras() : null;
          if (bundle != null){
              String alarmIntentType = bundle.getString("type");
              if (alarmIntentType == null){
                  alarmIntentType = "unknown";
//                  Log.w(LOG_TAG, "alarmIntentType = unknown");
//                  Util.writeLocalLogEntryUsingLogback(getApplicationContext(), LOG_TAG, "alarmIntentType = unknown");
                  logger.warn("alarmIntentType = unknown");
              }
                  RecordAndUpload.doRecord(getApplicationContext(),alarmIntentType, null);

          }else{
//              Log.e(LOG_TAG, "MainService error");
//              Util.writeLocalLogEntryUsingLogback(getApplicationContext(), LOG_TAG, "MainService error");
              logger.warn("MainService error");
          }

      }catch (Exception ex){
//          Util.writeLocalLogEntryUsingLogback(getApplicationContext(), LOG_TAG, ex.getLocalizedMessage());
          logger.error(ex.getLocalizedMessage());
      }

    }


}
