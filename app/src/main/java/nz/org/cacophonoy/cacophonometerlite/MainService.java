package nz.org.cacophonoy.cacophonometerlite;

import android.app.IntentService;


import android.app.Service;
import android.content.Intent;
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

        RecordAndUpload.doRecord(getApplicationContext(),null);
    }


}
