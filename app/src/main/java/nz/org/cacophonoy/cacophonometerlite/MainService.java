package nz.org.cacophonoy.cacophonometerlite;

import android.app.IntentService;


import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by User on 29-Mar-17.
 */

public class MainService extends IntentService {

    private static final String LOG_TAG = MainService.class.getName();

    public MainService(){
        super("MainService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        String typeOfRecording = null;

        Bundle bundle = intent.getExtras();
        String alarmIntentType = bundle.getString("type");

//        if (alarmIntentType == null){
//            if (alarmIntentType.equalsIgnoreCase("alarmIntentType")){
//                typeOfRecording = "alarmIntentType";
//            }
//        }

        RecordAndUpload.doRecord(getApplicationContext(),alarmIntentType);
    }


}
