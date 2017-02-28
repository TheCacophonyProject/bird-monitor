package nz.org.cacophonoy.cacophonometerlite;



import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import static android.R.attr.delay;
import static android.content.Context.ALARM_SERVICE;


public class BootReceiver extends BroadcastReceiver {
    static final String LOG_TAG = "BootReceiver";

    @Override
    public void onReceive(final Context context, Intent intent)
    {
        Log.d(LOG_TAG, "onReceive: Running boot code...");
        Toast.makeText(context, "Cacophony BootReceiver started.", Toast.LENGTH_SHORT).show();
        if (!Util.checkPermissionsForRecording(context)) {
            Toast.makeText(context, "App doesn't have proper permissions to record.", Toast.LENGTH_SHORT).show();
            return;
        }
        Thread t = new Thread() {
            @Override
            public void run() {
                Server.updateServerConnectionStatus(context);
            }
        };
        t.start();

        Intent myIntent = new Intent(context, StartRecordingReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent,0);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(ALARM_SERVICE);
//        long delay = 60 * 1000 * 5; // 5 minutes
       // long delay = 60 * 1000 ; // 1 minutes
        long delay = 1000 * 60  * 10 ; // 10 minutes
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() ,
                delay, pendingIntent);
    }
}
