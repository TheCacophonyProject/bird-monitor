package nz.org.cacophonoy.cacophonometerlite;



import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by User on 24-Nov-16.
 */

public class BootReceiver extends BroadcastReceiver {
    private PendingIntent pendingIntent;
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent myIntent = new Intent(context, MyReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent,0);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(ALARM_SERVICE);

//        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                SystemClock.elapsedRealtime() ,
//                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
        long delay = 60 * 1000 * 5; // 5 minutes
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() ,
                delay, pendingIntent);
    }
}
