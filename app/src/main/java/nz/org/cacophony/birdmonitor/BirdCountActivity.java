package nz.org.cacophony.birdmonitor;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class BirdCountActivity extends AppCompatActivity implements IdlingResourceForEspressoTesting {

    private static final String TAG = BirdCountActivity.class.getName();

    private Button btnRecordNow;
    private Button btnFinished;
    private TextView tvMessages;
    private TextView tvTitle;

    private AppCompatRadioButton rbFiveMinute;
    private AppCompatRadioButton rbTenMinute;
    private AppCompatRadioButton rbFifteenMinute;

    private PermissionsHelper permissionsHelper;

    private boolean recording = false;

    CountDownTimer countDownTimer = null;

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

        btnRecordNow = findViewById(R.id.btnRecordNow);
        btnFinished = findViewById(R.id.btnFinished);

        tvMessages = findViewById(R.id.tvMessages);
        tvTitle = findViewById(R.id.tvTitle);

        rbFiveMinute = findViewById(R.id.rbFiveMinute);
        rbTenMinute = findViewById(R.id.rbTenMinute);
        rbFifteenMinute = findViewById(R.id.rbFifteenMinute);


        btnRecordNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordNow();
            }
        });

        btnFinished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finished();
            }
        });

        rbFiveMinute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Prefs prefs = new Prefs(getApplicationContext());
                prefs.setBirdCountDuration("fiveMinute");
            }
        });


        rbTenMinute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Prefs prefs = new Prefs(getApplicationContext());
                prefs.setBirdCountDuration("tenMinute");
            }
        });


        rbFifteenMinute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Prefs prefs = new Prefs(getApplicationContext());
                prefs.setBirdCountDuration("fifteenMinute");
            }
        });

        checkPermissions();

        Prefs prefs = new Prefs(getApplicationContext());

        prefs.setCancelRecording(false);

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
    public void onStop() {
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

    public void recordNow() {


        btnRecordNow.setEnabled(false);

        Intent myIntent = new Intent(this, StartRecordingReceiver.class);
        myIntent.putExtra("callingCode", "recordNowButtonClicked"); // for debugging
        try {

            long durationInSeconds = 0;

            if (rbFiveMinute.isChecked()) {
                myIntent.putExtra("type", "birdCountButton5");
                durationInSeconds = Util.getRecordingDuration(getApplicationContext(), "birdCountButton5");
            } else if (rbTenMinute.isChecked()) {
                myIntent.putExtra("type", "birdCountButton10");
                durationInSeconds = Util.getRecordingDuration(getApplicationContext(), "birdCountButton10");
            } else if (rbFifteenMinute.isChecked()) {
                myIntent.putExtra("type", "birdCountButton15");
                durationInSeconds = Util.getRecordingDuration(getApplicationContext(), "birdCountButton15");
            }

            long durationInMilliSeconds = durationInSeconds * 1000;


            sendBroadcast(myIntent);
            recording = true;

            // A CountDownTimer is used for display purpose only - the actual duration of the
            // recording is controlled by Thread in RecordAndUpload (I couldn't see an easy
            // way of communicating between the two places.
            // However, if the user has selected to play a warning sound before the recording
            // starts, RecordAndUpload sleeps for 2 seconds before the recording starts and the
            // CountDownTimer is not in sync.            //
            // Putting a sleep on the GUI is not recommended/didn't work so I just added the 2
            // seconds to the CountDownTimerer :-)


            Prefs prefs = new Prefs(getApplicationContext());
            if (prefs.getPlayWarningSound()) {
                durationInMilliSeconds += 2000;
            }

            countDownTimer = new CountDownTimer(durationInMilliSeconds, 1000) {

                public void onTick(long millisUntilFinished) {
                    btnRecordNow.setText("seconds remaining: " + millisUntilFinished / 1000);
                    //here you can have your logic to set text to edittext
                }

                public void onFinish() {
                    btnRecordNow.setText("Record Now");
                    btnFinished.setText("Finished");
                    recording = false;
                }

            }.start();

            btnFinished.setText("Stop Recording");

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }


    public void finished() {
        try {
            if (recording) {
                // are you sure?
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                // Add the buttons
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // How to cancel a recording?
                        Prefs prefs = new Prefs(getApplicationContext());
                        prefs.setCancelRecording(true);
                        countDownTimer.cancel(); // this just cancels the timeer
                        recording = false;
                        btnRecordNow.setEnabled(true);
                        btnRecordNow.setVisibility(View.VISIBLE);
                        btnRecordNow.setText("Record Now");
                        btnFinished.setText("Finished");
                        //   recordIdlingResource.decrement();

                    }
                });
                builder.setNegativeButton("No/Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        return;
                    }
                });
                builder.setMessage("Are you sure you want to stop recording?")
                        .setTitle("Stop recording");

                final AlertDialog dialog = builder.create();

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button btnPositive = dialog.getButton(Dialog.BUTTON_POSITIVE);
                        btnPositive.setTextSize(24);
                        int btnPositiveColor = ResourcesCompat.getColor(getResources(), R.color.dialogButtonText, null);
                        btnPositive.setTextColor(btnPositiveColor);

                        Button btnNegative = dialog.getButton(Dialog.BUTTON_NEGATIVE);
                        btnNegative.setTextSize(24);
                        int btnNegativeColor = ResourcesCompat.getColor(getResources(), R.color.dialogButtonText, null);
                        btnNegative.setTextColor(btnNegativeColor);

                        //https://stackoverflow.com/questions/6562924/changing-font-size-into-an-alertdialog
                        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
                        textView.setTextSize(22);
                    }
                });
                dialog.show();
            } else {

                finish();
            }

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }


    void displayOrHideGUIObjects() {

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
            btnFinished.setText("Finished");
            tvTitle.setText(getResources().getString(R.string.bird_count_message));
            btnRecordNow.setVisibility(View.VISIBLE);
        }
    }

    private boolean equalsAnyIgnoreCase(String input, List<String> toCheck) {
        for (String check : toCheck) {
            if (input.equalsIgnoreCase(check)) {
                return true;
            }
        }
        return false;
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

                    List<String> tvMessageTypes = Arrays.asList(
                            "RECORDING_DISABLED", "NO_PERMISSION_TO_RECORD", "UPLOADING_RECORDINGS",
                            "UPLOADING_FAILED", "UPLOADING_FINISHED", "GETTING_READY_TO_RECORD",
                            "FAILED_RECORDINGS_NOT_UPLOADED", "RECORD_AND_UPLOAD_FAILED",
                            "UPLOADING_FAILED_NOT_REGISTERED", "RECORDING_STARTED", "RECORDING_FINISHED"
                    );

                    if (equalsAnyIgnoreCase(messageType,
                            tvMessageTypes)) {
                        tvMessages.setText(messageToDisplay);
                    }

                    if (equalsAnyIgnoreCase(messageType, Arrays.asList("UPLOADING_FAILED", "UPLOADING_FINISHED"))) {
                        uploadFilesIdlingResource.decrement();
                    } else if (messageType.equalsIgnoreCase("RECORD_AND_UPLOAD_FAILED")) {
                        recordIdlingResource.decrement();
                    } else if (messageType.equalsIgnoreCase("RECORDING_FINISHED")) {
                        recording = false;
                        countDownTimer.cancel();
                        tvTitle.setText(getResources().getString(R.string.bird_count_message));
                        btnRecordNow.setEnabled(true);
                        btnRecordNow.setVisibility(View.VISIBLE);
                        btnRecordNow.setText("Record Now");
                        btnFinished.setText("Finished");
                        recordIdlingResource.decrement();
                    }
                }

            } catch (Exception ex) {
                Log.e(TAG, ex.getLocalizedMessage());
                tvMessages.setText("Could not record");

            }

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsHelper.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    private void checkPermissions() {
        permissionsHelper = new PermissionsHelper();
        permissionsHelper.checkAndRequestPermissions(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE);
    }
}
