package nz.org.cacophonoy.cacophonometerlite;

import android.app.IntentService;
        import android.app.IntentService;
        import android.app.Service;
        import android.content.Context;
        import android.content.Intent;
        import android.os.Bundle;
        import android.os.IBinder;
        import android.support.annotation.Nullable;

/**
 * Created by User on 29-Mar-17.
 */

public class AirplaneModeService extends IntentService {

//    private Context context = null;
//    private boolean enable = true;

    public AirplaneModeService(){
        super("AirplaneModeService");

    }

//    public AirplaneModeService(Context context, boolean enable){
//        super("AirplaneModeService");
//        this.context = context;
//        this.enable = enable;
//    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Bundle bundle = intent.getExtras();
        boolean enable = bundle.getBoolean("enable");

        if (enable){
            Util.enableAirplaneMode(getApplicationContext());
        }else{
            Util.disableAirplaneMode(getApplicationContext());
        }

    }
}
