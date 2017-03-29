package nz.org.cacophonoy.cacophonometerlite;

import android.app.IntentService;



import android.app.IntentService;
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by User on 29-Mar-17.
 */

public class MainService extends IntentService {

    private static final String LOG_TAG = MainService.class.getName();

//    private Context context = null;
//    private boolean enable = true;

    public MainService(){
        super("AirplaneModeService");

    }

//    public AirplaneModeService(Context context, boolean enable){
//        super("AirplaneModeService");
//        this.context = context;
//        this.enable = enable;
//    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Record2.doRecord(getApplicationContext());
//        try {
//            // Start recording in new thread.
//            Thread thread = new Thread() {
//                @Override
//                public void run() {
//                    Looper.prepare();
//                    Log.d(LOG_TAG, "onHandleIntent");
//                   Record2.doRecord(getApplicationContext());
//                }
//            };
//            thread.start();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
