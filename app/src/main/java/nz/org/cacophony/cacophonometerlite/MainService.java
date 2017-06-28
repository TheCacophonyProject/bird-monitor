package nz.org.cacophony.cacophonometerlite;

import android.app.IntentService;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by User on 29-Mar-17.
 * Recordings need to happen from a service so needed this class
 */

public class MainService extends IntentService {
    private static final String LOG_TAG = MainService.class.getName();

    // --Commented out by Inspection (12-Jun-17 1:56 PM):private static final String LOG_TAG = MainService.class.getName();

    public MainService(){
        super("MainService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

      try {
          Bundle bundle = intent != null ? intent.getExtras() : null;
          if (bundle != null){
              String alarmIntentType = bundle.getString("type");
              if (alarmIntentType == null){
                  alarmIntentType = "unknown";
                  Log.w(LOG_TAG, "alarmIntentType = unknown");
              }
                  RecordAndUpload.doRecord(getApplicationContext(),alarmIntentType, null);

          }else{
              Log.e(LOG_TAG, "MainService error");
          }

      }catch (Exception ex){
          Log.e(LOG_TAG, "MainService error");
      }

    }


}
