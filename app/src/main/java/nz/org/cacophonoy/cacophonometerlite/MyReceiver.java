package nz.org.cacophonoy.cacophonometerlite;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

public class MyReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
		/*Intent service1 = new Intent(context, MyAlarmService.class);
	     context.startService(service1);*/
        Log.i("App", "called receiver method");
        try{
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 30);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);


            CharSequence text = "About to record";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            Record.makeRecording();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}