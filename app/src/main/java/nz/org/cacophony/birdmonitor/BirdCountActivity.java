package nz.org.cacophony.birdmonitor;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import static nz.org.cacophony.birdmonitor.IdlingResourceForEspressoTesting.recordIdlingResource;
import static nz.org.cacophony.birdmonitor.IdlingResourceForEspressoTesting.uploadFilesIdlingResource;

public class BirdCountActivity extends AppCompatActivity implements IdlingResourceForEspressoTesting {

    private static final String TAG = BirdCountActivity.class.getName();

    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 0;
    private static final int PERMISSION_RECORD_AUDIO = 1;
    private static final int PERMISSION_LOCATION = 2;

    private Button btnRecordNow;
    private TextView tvMessages;
    private TextView tvTitle;

    private AppCompatRadioButton rbFiveMinute;
    private AppCompatRadioButton rbTenMinute;
    private AppCompatRadioButton rbFifteenMinute;

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter iff = new IntentFilter("MANAGE_RECORDINGS");
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bird_count);

        //https://developer.android.com/training/appbar/setting-up#java
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        btnRecordNow =  findViewById(R.id.btnRecordNow);
        tvMessages = findViewById(R.id.tvMessages);
        tvTitle = findViewById(R.id.tvTitle);

        rbFiveMinute = findViewById(R.id.rbFiveMinute);
        rbTenMinute =  findViewById(R.id.rbTenMinute);
        rbFifteenMinute =  findViewById(R.id.rbFifteenMinute);

        btnRecordNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordNowButtonPressed();
            }
        });

        rbFiveMinute.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Prefs prefs = new Prefs(getApplicationContext());
                prefs.setBirdCountDuration("fiveMinute");
            }
        });


        rbTenMinute.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Prefs prefs = new Prefs(getApplicationContext());
                prefs.setBirdCountDuration("tenMinute");
            }
        });


        rbFifteenMinute.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Prefs prefs = new Prefs(getApplicationContext());
                prefs.setBirdCountDuration("fifteenMinute");
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.button_help:
                Util.displayHelp(this, getResources().getString(R.string.activity_or_fragment_title_bird_count));
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
    }

    @Override
    protected void onPause() {
        super.onPause();

        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_help, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        displayOrHideGUIObjects();
    }

    public void recordNowButtonPressed() {
        boolean alreadyHavePermission =  requestPermissions();

        if (alreadyHavePermission){
            recordNow();
        }
    }

    public void recordNow(){
        btnRecordNow.setEnabled(false);

        Intent myIntent = new Intent(this, StartRecordingReceiver.class);
        myIntent.putExtra("callingCode", "recordNowButtonClicked"); // for debugging
        try {

            if (rbFiveMinute.isChecked()){
                myIntent.putExtra("type", "birdCountButton5");
            }else if (rbTenMinute.isChecked()){
                myIntent.putExtra("type", "birdCountButton10");
            }else if (rbFifteenMinute.isChecked()){
                myIntent.putExtra("type", "birdCountButton15");
            }

           sendBroadcast(myIntent);

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    private boolean requestPermissions(){
        // If Android OS >= 6 then need to ask user for permission to Write External Storage, Recording, Location
//        https://developer.android.com/training/permissions/requesting.html

        boolean allPermissionsAlreadyGranted = true;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            allPermissionsAlreadyGranted = false;

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_WRITE_EXTERNAL_STORAGE);

        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            allPermissionsAlreadyGranted = false;

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_RECORD_AUDIO);

        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            allPermissionsAlreadyGranted = false;

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_LOCATION);

        }

        return allPermissionsAlreadyGranted;
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == PERMISSION_WRITE_EXTERNAL_STORAGE) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start recording
                tvMessages.setText("WRITE_EXTERNAL_STORAGE permission granted");
            } else {
                tvMessages.setText("Do not have WRITE_EXTERNAL_STORAGE permission, You can NOT save recordings");
            }
        }

        if (requestCode == PERMISSION_RECORD_AUDIO) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start recording
                tvMessages.setText("RECORD_AUDIO permission granted");
            } else {
                tvMessages.setText("Do not have RECORD_AUDIO permission, You can NOT record");
            }
        }

        if (requestCode == PERMISSION_LOCATION) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start recording
                tvMessages.setText("LOCATION permission granted");
            } else {
                tvMessages.setText("Do not have LOCATION permission, You can NOT set the GPS position");
            }
        }

        // END_INCLUDE(onRequestPermissionsResult)
    }

    public void finished(@SuppressWarnings("UnusedParameters") View v) {
        try {
            finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }



    void displayOrHideGUIObjects(){

        Prefs prefs = new Prefs(getApplicationContext());
        String birdCountDuration = prefs.getBirdCountDuration();

        switch (birdCountDuration) {

            case "fiveMinute":
                rbFiveMinute.setChecked(true);
                break;
            case "tenMinute":
                rbTenMinute.setChecked(true);
                break;
            case "fifteenMinute":
                rbFifteenMinute.setChecked(true);
                break;
             }

        if (RecordAndUpload.isRecording) {
            btnRecordNow.setEnabled(false);
            tvTitle.setText("Can not record, as a recording is already in progress");
            btnRecordNow.setVisibility(View.GONE);
        } else {
          btnRecordNow.setEnabled(true);
            tvTitle.setText(getResources().getString(R.string.bird_count_message));
            btnRecordNow.setVisibility(View.VISIBLE);
        }
    }

    private final BroadcastReceiver onNotice = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            try {

                String jsonStringMessage = intent.getStringExtra("jsonStringMessage");

                if (jsonStringMessage != null) {

                    JSONObject joMessage = new JSONObject(jsonStringMessage);
                    String messageType = joMessage.getString("messageType");
                    String messageToDisplay = joMessage.getString("messageToDisplay");

                    if (messageType.equalsIgnoreCase("RECORDING_DISABLED")) {
                        tvMessages.setText(messageToDisplay);
//                    } else if (messageType.equalsIgnoreCase("ALREADY_RECORDING")) {
//                        tvMessages.setText(messageToDisplay);
//                        uploadFilesIdlingResource.decrement();
                    } else if (messageType.equalsIgnoreCase("NO_PERMISSION_TO_RECORD")) {
                        tvMessages.setText(messageToDisplay);
                    } else if (messageType.equalsIgnoreCase("UPLOADING_RECORDINGS")) {
                        tvMessages.setText(messageToDisplay);
                    } else if (messageType.equalsIgnoreCase("UPLOADING_FAILED")) {
                        tvMessages.setText(messageToDisplay);
                        uploadFilesIdlingResource.decrement();
                    } else if (messageType.equalsIgnoreCase("UPLOADING_FINISHED")) {
                        tvMessages.setText(messageToDisplay);
                        uploadFilesIdlingResource.decrement();
                    } else if (messageType.equalsIgnoreCase("GETTING_READY_TO_RECORD")) {
                        tvMessages.setText(messageToDisplay);
                    } else if (messageType.equalsIgnoreCase("FAILED_RECORDINGS_NOT_UPLOADED")) {
                        tvMessages.setText(messageToDisplay);
                    } else if (messageType.equalsIgnoreCase("RECORD_AND_UPLOAD_FAILED")) {
                        tvMessages.setText(messageToDisplay);
                        recordIdlingResource.decrement();
                    } else if (messageType.equalsIgnoreCase("UPLOADING_FAILED_NOT_REGISTERED")) {
                        tvMessages.setText(messageToDisplay);
                    } else if (messageType.equalsIgnoreCase("RECORDING_STARTED")) {
                        tvMessages.setText(messageToDisplay);
                    } else if (messageType.equalsIgnoreCase("RECORDING_FINISHED")) {
                        tvTitle.setText(getResources().getString(R.string.bird_count_message));
                        tvMessages.setText(messageToDisplay);
                        btnRecordNow.setEnabled(true);
                        btnRecordNow.setVisibility(View.VISIBLE);
                        recordIdlingResource.decrement();
                    }
                }

            } catch (Exception ex) {
                Log.e(TAG, ex.getLocalizedMessage());
                tvMessages.setText("Could not record");

            }

        }
    };
}
