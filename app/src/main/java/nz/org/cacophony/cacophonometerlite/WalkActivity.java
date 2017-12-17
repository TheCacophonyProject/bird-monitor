package nz.org.cacophony.cacophonometerlite;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;

import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;


public class WalkActivity extends AppCompatActivity {
    private static final String TAG = WalkActivity.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button // need to also configure AndroidManifest to say what the parent activity is
        if (ab != null){
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayUseLogoEnabled(true);
            ab.setLogo(R.mipmap.ic_launcher);
        }else{
            Log.w(TAG, "ActionBar ab is null");

        }


    }

    @Override
    public void onResume() {
        try {
            super.onResume();
        }   catch (Exception ex){
            // This is very poor, but I have no idea why super.onResume give a null pointer exception
            // Need to spend time on this
            Log.e(TAG, "Error calling super.onResume");
        }

        // listens for events broadcast from ?
        IntentFilter iff = new IntentFilter("event");
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);

        // Disable periodic recording when first start
        Prefs prefs = new Prefs(this.getApplicationContext());
        if (prefs.isWalkingPeriodicRecordingsEnabled()){
            final RadioButton enableRecordingsRadioButton = (RadioButton) findViewById(R.id.periodicRecordingsEnabled);
            enableRecordingsRadioButton.setChecked(true);
        }else{
            final RadioButton disableRecordingsRadioButton = (RadioButton) findViewById(R.id.periodicRecordingsDisabled);
            disableRecordingsRadioButton.setChecked(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
    }
    private BroadcastReceiver onNotice= new BroadcastReceiver() {
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

        // broadcast notification coming from ??
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String message = intent.getStringExtra("message");
                if (message != null) {
                    if (message.equalsIgnoreCase("walk_record_started")) {

                        Util.getToast(getApplicationContext(),"Recording started", false ).show();
                    }else if (message.equalsIgnoreCase("walk_record_finished")) {
                        ((Button) findViewById(R.id.recordNowButton)).setEnabled(true);
                        Util.getToast(getApplicationContext(),"Recording finished", false ).show();
                    }


                }




            }catch (Exception ex){
//                logger.error(ex.getLocalizedMessage());
                Log.e(TAG,ex.getLocalizedMessage());
            }
        }
    };

    public void recordNowButtonClicked(@SuppressWarnings("UnusedParameters") View v) {

        Util.getToast(getApplicationContext(),"Prepare to start recording", false ).show();
        ((Button) findViewById(R.id.recordNowButton)).setEnabled(false);

        Intent myIntent = new Intent(WalkActivity.this, StartRecordingReceiver.class);
        try {
            myIntent.putExtra("type", "walkRecordNowButton");
            sendBroadcast(myIntent);

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }


    public void onCheckboxContinuousUploadClicked(@SuppressWarnings("UnusedParameters") View v) {

        Util.getToast(getApplicationContext(),"onCheckboxContinuousUploadClicked", true ).show();
    }

    public void startPeriodicRecordings() {

        Util.getToast(getApplicationContext(),"startPeriodicRecordingsButtonClicked", true ).show();
        Intent myIntent = new Intent(WalkActivity.this, StartRecordingReceiver.class);
        try{
            myIntent.putExtra("type","walkModeRepeating");
            Uri timeUri;
            timeUri = Uri.parse("walkModeNormal"); // not sure if need this
            myIntent.setData(timeUri);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(WalkActivity.this, 0, myIntent,0);
            AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
            long timeBetweenRecordingsSeconds  = 2 * 60; // testing
            long delay = 1000 * timeBetweenRecordingsSeconds ;
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() ,
                    delay, pendingIntent);

        }catch (Exception ex){
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    public void stopPeriodicRecordings() {
//https://stackoverflow.com/questions/28922521/how-to-cancel-alarm-from-alarmmanager
        Intent myIntent = new Intent(WalkActivity.this, StartRecordingReceiver.class);
        try{
            myIntent.putExtra("type","walkModeRepeating");
            Uri timeUri;
            timeUri = Uri.parse("walkModeNormal"); // not sure if need this
            myIntent.setData(timeUri);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(WalkActivity.this, 0, myIntent,0);
            AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

            alarmManager.cancel(pendingIntent);

        }catch (Exception ex){
            Log.e(TAG, ex.getLocalizedMessage());
        }


    }

    public void uploadRecordingsButtonClicked(@SuppressWarnings("UnusedParameters") View v) {

        Util.getToast(getApplicationContext(),"uploadRecordingsButtonClicked", true ).show();
    }

    public void onPeriodicRecordingsRadioButtonClicked(View view) {
        Prefs prefs = new Prefs(this.getApplicationContext());
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.periodicRecordingsDisabled:
                if (checked)
                    prefs.setWalkingPeriodicRecordingsEnabled(false);
                    Util.getToast(getApplicationContext(),"periodicRecordingsDisabled", true ).show();
                stopPeriodicRecordings();
                    break;
            case R.id.periodicRecordingsEnabled:
                if (checked)
                    prefs.setWalkingPeriodicRecordingsEnabled(true);
                    Util.getToast(getApplicationContext(),"periodicRecordingsEnabled", true ).show();
                startPeriodicRecordings();
                    break;
        }
    }
}
