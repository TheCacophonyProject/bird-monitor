package nz.org.cacophonoy.cacophonometerlite;


import android.content.Context;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by User on 28-Mar-17.
 */

public class AirplaneMode implements Runnable {
    private static final String LOG_TAG = AirplaneMode.class.getName();

    private Context context = null;
    private boolean enable = true;



    AirplaneMode(Context context, boolean enable){
        this.context = context;
        this.enable = enable;

    }

    @Override
    public void run() {
        if (context == null ) {
            Log.e(LOG_TAG, "Context or Handler were null.");
            return;
        }

        if (enable){
            boolean airplaneModeEnabled = Util.enableAirplaneMode(context);
            if(airplaneModeEnabled){
                Log.i(LOG_TAG, "airplaneMode Enabled");
            }else{
                Log.e(LOG_TAG, "airplaneMode NOT Enabled");
            }
        }else{
            boolean airplaneModeDisabled = Util.disableAirplaneMode(context);
            if(airplaneModeDisabled){
                Log.i(LOG_TAG, "airplaneMode Disabled");
            }else{
                Log.e(LOG_TAG, "airplaneMode NOT Disabled");
            }
        }



    }
}
