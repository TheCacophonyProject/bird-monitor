package nz.org.cacophony.cacophonometer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;


public class MainActivity2 extends AppCompatActivity {
    private static final String TAG = MainActivity2.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Prefs prefs = new Prefs(this.getApplicationContext());
        prefs.setRecordingDurationSeconds();
        prefs.setNormalTimeBetweenRecordingsSeconds();
        prefs.setTimeBetweenFrequentRecordingsSeconds();
        prefs.setTimeBetweenVeryFrequentRecordingsSeconds();
        prefs.setTimeBetweenGPSLocationUpdatesSeconds();
        prefs.setDawnDuskOffsetMinutes();
        prefs.setDawnDuskIncrementMinutes();
        prefs.setLengthOfTwilightSeconds();
        prefs.setTimeBetweenUploadsSeconds();
        prefs.setTimeBetweenFrequentUploadsSeconds();
        prefs.setBatteryLevelCutoffRepeatingRecordings();
        prefs.setBatteryLevelCutoffDawnDuskRecordings();
        prefs.setDateTimeLastRepeatingAlarmFiredToZero();
        prefs.setDateTimeLastUpload(0);

        // Now create the alarms that will cause the recordings to happen

        Util.createTheNextSingleStandardAlarm(getApplicationContext());
        DawnDuskAlarms.configureDawnAndDuskAlarms(getApplicationContext(), true);
        Util.createCreateAlarms(getApplicationContext());
        Util.setUpLocationUpdateAlarm(getApplicationContext());

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, "onRestart");
    }

    public void launchSetupActivity(@SuppressWarnings("UnusedParameters") View v) {

        try {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            //finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    public void launchTestRecordActivity(@SuppressWarnings("UnusedParameters") View v) {
        try {
            Intent intent = new Intent(this, TestRecordActivity.class);
            startActivity(intent);
           // finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    public void launchVitalsActivity(@SuppressWarnings("UnusedParameters") View v) {
        try {
            Intent intent = new Intent(this, VitalsActivity.class);
            startActivity(intent);
           // finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }




}
