package nz.org.cacophony.cacophonometer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;

public class WalkingActivity extends AppCompatActivity implements IdlingResourceForEspressoTesting{
    private static final String TAG = WalkingActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walking);

        final Switch switchWalking = findViewById(R.id.swWalking2);
        switchWalking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (switchWalking.isShown()) { // Listener was firing each time activity loaded - https://stackoverflow.com/questions/17372750/android-setoncheckedchangelistener-calls-again-when-old-view-comes-back
                    Util.setWalkingMode(getApplicationContext(), isChecked);
                    findViewById(R.id.btnUploadFiles).setEnabled(!isChecked);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Prefs prefs = new Prefs(getApplicationContext());

        IntentFilter iff = new IntentFilter("event");
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);

        boolean walkingMode = true; // if any of the following are false, then change walking mode to false

        if (!prefs.getInternetConnectionMode().equalsIgnoreCase("offline")) {
            walkingMode = false;
        }else if(!prefs.getUseFrequentRecordings()){
            walkingMode = false;
        }else if (!prefs.getIgnoreLowBattery()){
            walkingMode = false;
        }else if (!prefs.getPlayWarningSound()){
            walkingMode = false;
        }else if (!prefs.getPeriodicallyUpdateGPS()){
            walkingMode = false;
        }else if (!prefs.getIsDisableDawnDuskRecordings()){
            walkingMode = false;
        }else if (prefs.getUseFrequentUploads()){
            walkingMode = false;

        }

        final Switch switchWalking = findViewById(R.id.swWalking2);
        switchWalking.setChecked(walkingMode);

        // Now enable or disable Upload files button
        findViewById(R.id.btnUploadFiles).setEnabled(!walkingMode);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
    }


    public void uploadFiles(@SuppressWarnings("UnusedParameters") View v){
        if (!Util.isNetworkConnected(getApplicationContext())){
            Util.getToast(getApplicationContext(),"The phone is not currently connected to the internet - please fix and try again", true ).show();
            return;
        }

        Prefs prefs = new Prefs(getApplicationContext());
        if (prefs.getGroupName() == null){
            Util.getToast(getApplicationContext(),"You need to register this phone before you can upload", true ).show();
            return;
        }

        File recordingsFolder = Util.getRecordingsFolder(getApplicationContext());
        File recordingFiles[] = recordingsFolder.listFiles();
        int numberOfFilesToUpload = recordingFiles.length;

        if (numberOfFilesToUpload > 0){
            Util.getToast(getApplicationContext(), "About to upload " + numberOfFilesToUpload + " recordings.", false).show();
            Util.uploadFilesUsingUploadButton(getApplicationContext());
        }else{
            Util.getToast(getApplicationContext(), "There are no recordings on the phone to upload.", true).show();
        }




    }


    public void next(@SuppressWarnings("UnusedParameters") View v) {

        try {
            finish();

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    public void back(@SuppressWarnings("UnusedParameters") View v) {

        try {
            Intent intent = new Intent(this, GPSActivity.class);
            startActivity(intent);

            finish();

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    private final BroadcastReceiver onNotice = new BroadcastReceiver() {
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

        // broadcast notification coming from ??
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Prefs prefs = new Prefs(context);
                String message = intent.getStringExtra("message");
                TextView tvMessages = findViewById(R.id.tvMessages);
                if (message != null) {

                    if (message.equalsIgnoreCase("files_successfully_uploaded")) {
                        Util.getToast(getApplicationContext(), "Files have been uploaded to the server", false).show();
                    } else if (message.equalsIgnoreCase("files_not_uploaded")) {
                        Util.getToast(getApplicationContext(), "Error: Unable to upload files", true).show();
                    }
                }

            } catch (Exception ex) {

                Log.e(TAG, ex.getLocalizedMessage());
            }
        }
    };

    @SuppressWarnings("SameReturnValue")
    public CountingIdlingResource getIdlingResource() {
        return registerIdlingResource;
    }

    @SuppressWarnings("SameReturnValue")
    public CountingIdlingResource getRecordNowIdlingResource() {
        return recordNowIdlingResource;
    }
}
