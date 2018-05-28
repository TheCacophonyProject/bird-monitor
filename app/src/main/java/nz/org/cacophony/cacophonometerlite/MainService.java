package nz.org.cacophony.cacophonometerlite;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;


/**
 * Created by User on 29-Mar-17.
 * Recordings need to happen from a service so needed this class
 */

public class MainService extends IntentService {
    private static final String TAG = MainService.class.getName();



    public MainService(){
        super("MainService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager == null){
            Log.e(TAG, "PowerManger is null");
            return;
        }
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MainServiceWakelockTag");
        wakeLock.acquire(10*60*1000L /*10 minutes*/);

      try {

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
          wakeLock.release();
      }
    }
}
