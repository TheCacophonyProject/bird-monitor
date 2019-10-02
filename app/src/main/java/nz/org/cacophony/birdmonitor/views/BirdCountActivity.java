package nz.org.cacophony.birdmonitor.views;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import nz.org.cacophony.birdmonitor.MessageHelper;
import nz.org.cacophony.birdmonitor.PermissionsHelper;
import nz.org.cacophony.birdmonitor.Prefs;
import nz.org.cacophony.birdmonitor.R;
import nz.org.cacophony.birdmonitor.RecordAndUpload;
import nz.org.cacophony.birdmonitor.RecordingsHelper;
import nz.org.cacophony.birdmonitor.StartRecordingReceiver;
import nz.org.cacophony.birdmonitor.Util;

import static nz.org.cacophony.birdmonitor.views.ManageRecordingsFragment.MANAGE_RECORDINGS_ACTION;

public class BirdCountActivity extends AppCompatActivity {

    private static final String TAG = BirdCountActivity.class.getName();
    CountDownTimer countDownTimer = null;
    private Button btnRecordNow;
    private Button btnFinished;
    private Button btnAddNotes;
    private TextView tvMessages;
    private TextView tvTitle;
    private AppCompatRadioButton rbFiveMinute;
    private AppCompatRadioButton rbTenMinute;
    private AppCompatRadioButton rbFifteenMinute;
    private PermissionsHelper permissionsHelper;
    private boolean recording = false;
    private final BroadcastReceiver messageHandler =
            RecordingsHelper.createMessageHandler(TAG, message -> tvMessages.setText(message), this::onRecordingFinished);
    private String weatherNote;
    private String countedByNote;
    private String otherNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bird_count);

        //https://developer.android.com/training/appbar/setting-up#java
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        btnRecordNow = findViewById(R.id.btnRecordNow);
        btnFinished = findViewById(R.id.btnFinished);
        btnAddNotes = findViewById(R.id.btnAddNotes);

        tvMessages = findViewById(R.id.tvMessages);
        tvTitle = findViewById(R.id.tvTitle);

        rbFiveMinute = findViewById(R.id.rbFiveMinute);
        rbTenMinute = findViewById(R.id.rbTenMinute);
        rbFifteenMinute = findViewById(R.id.rbFifteenMinute);


        btnRecordNow.setOnClickListener(v -> recordNow());

        btnFinished.setOnClickListener(v -> finished());

        btnAddNotes.setOnClickListener(v -> addNotes());

        rbFiveMinute.setOnClickListener(v -> {
            Prefs prefs = new Prefs(getApplicationContext());
            prefs.setBirdCountDuration("fiveMinute");
        });


        rbTenMinute.setOnClickListener(v -> {
            Prefs prefs = new Prefs(getApplicationContext());
            prefs.setBirdCountDuration("tenMinute");
        });


        rbFifteenMinute.setOnClickListener(v -> {
            Prefs prefs = new Prefs(getApplicationContext());
            prefs.setBirdCountDuration("fifteenMinute");
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
    protected void onPause() {
        super.onPause();
        MessageHelper.unregisterMessageHandler(messageHandler, this);
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
        MessageHelper.registerMessageHandler(MANAGE_RECORDINGS_ACTION, messageHandler, this);
    }

    public void recordNow() {


        btnRecordNow.setEnabled(false);

        Intent myIntent = new Intent(this, StartRecordingReceiver.class);
        myIntent.putExtra("callingCode", "recordNowButtonClicked"); // for debugging
        try {
            long durationInSeconds = 0;

            if (rbFiveMinute.isChecked()) {
                myIntent.putExtra(Prefs.INTENT_TYPE, Prefs.BIRD_COUNT_5_ALARM);
            } else if (rbTenMinute.isChecked()) {
                myIntent.putExtra(Prefs.INTENT_TYPE, Prefs.BIRD_COUNT_10_ALARM);
            } else if (rbFifteenMinute.isChecked()) {
                myIntent.putExtra(Prefs.INTENT_TYPE, Prefs.BIRD_COUNT_15_ALARM);
            }
            durationInSeconds = Util.getRecordingDuration(getApplicationContext(), myIntent.getStringExtra(Prefs.INTENT_TYPE), null);

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
                builder.setPositiveButton("Yes", (dialog, id) -> {
                    // How to cancel a recording?
                    Prefs prefs = new Prefs(getApplicationContext());
                    prefs.setCancelRecording(true);
                    if (countDownTimer != null) {
                        countDownTimer.cancel(); // this just cancels the timer
                    }
                    recording = false;
                    btnRecordNow.setEnabled(true);
                    btnRecordNow.setVisibility(View.VISIBLE);
                    btnRecordNow.setText("Record Now");
                    btnFinished.setText("Finished");
                });
                builder.setNegativeButton("No/Cancel", (dialog, id) -> { /*Exit the dialog*/ });
                builder.setMessage("Are you sure you want to stop recording?")
                        .setTitle("Stop recording");

                final AlertDialog dialog = builder.create();

                dialog.setOnShowListener(dialogInterface -> {
                    Button btnPositive = dialog.getButton(Dialog.BUTTON_POSITIVE);
                    btnPositive.setTextSize(24);
                    int btnPositiveColor = ResourcesCompat.getColor(getResources(), R.color.dialogButtonText, null);
                    btnPositive.setTextColor(btnPositiveColor);

                    Button btnNegative = dialog.getButton(Dialog.BUTTON_NEGATIVE);
                    btnNegative.setTextSize(24);
                    int btnNegativeColor = ResourcesCompat.getColor(getResources(), R.color.dialogButtonText, null);
                    btnNegative.setTextColor(btnNegativeColor);

                    //https://stackoverflow.com/questions/6562924/changing-font-size-into-an-alertdialog
                    TextView textView = dialog.findViewById(android.R.id.message);
                    textView.setTextSize(22);
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

    private void onRecordingFinished() {
        recording = false;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        tvTitle.setText(getResources().getString(R.string.bird_count_message));
        btnRecordNow.setEnabled(true);
        btnRecordNow.setVisibility(View.VISIBLE);
        btnRecordNow.setText("Record Now");
        btnFinished.setText("Finished");
    }

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


    private void addNotes() {
        Prefs prefs = new Prefs(this);

        String latestRecordingFileName = prefs.getLatestBirdCountRecordingFileNameNoExtension();

        if (latestRecordingFileName == null) {
            displayNoRecordingAlertDialog();
            return;
        }

        AlertDialog alertDialog = createNotesDialog(this, latestRecordingFileName);
        alertDialog.show();

    }

    private AlertDialog createNotesDialog(Context context, String latestRecordingFileName) {

        AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View birdCountNotesDialogView = inflater.inflate(R.layout.dialog_bird_count_notes, null);
        alertDialogBuilder.setView(birdCountNotesDialogView);

        final TextInputEditText etWeather = birdCountNotesDialogView.findViewById(R.id.etWeather);
        final TextInputEditText etCountedBy = birdCountNotesDialogView.findViewById(R.id.etCountedBy);
        final TextInputEditText etOther = birdCountNotesDialogView.findViewById(R.id.etOther);

        final Button btnOK = birdCountNotesDialogView.findViewById(R.id.btnOK);
        final Button btnCancel = birdCountNotesDialogView.findViewById(R.id.btnCancel);


        // Pre fill fields with last notes for this recording if they exist
        File notesFileNameForLastestRecording = Util.getNotesFileForLatestRecording(context);
        if (notesFileNameForLastestRecording != null) {
            if (notesFileNameForLastestRecording.exists()) {
                JSONObject jsonNotes = Util.getNotesFromNoteFile(notesFileNameForLastestRecording);
                try {
                    etWeather.setText(jsonNotes.getString("Weather"));
                    etCountedBy.setText(jsonNotes.getString("Counted By"));
                    etOther.setText(jsonNotes.getString("Other"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        alertDialogBuilder.setCancelable(false);

        AlertDialog alertDialog = alertDialogBuilder.create();

        btnOK.setOnClickListener(v -> {
            weatherNote = etWeather.getText().toString();
            countedByNote = etCountedBy.getText().toString();
            otherNote = etOther.getText().toString();
            Util.saveRecordingNote(context, latestRecordingFileName, weatherNote, countedByNote, otherNote);
            alertDialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        return alertDialog;
    }

    void displayNoRecordingAlertDialog() {

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setPositiveButton("OK", null)
                .setMessage("Sorry - There is no recording for you to attach notes to.")
                .setTitle("No Recording")
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button btnPositive = dialog.getButton(Dialog.BUTTON_POSITIVE);
            btnPositive.setTextSize(24);
            int btnPositiveColor = ResourcesCompat.getColor(this.getResources(), R.color.dialogButtonText, null);
            btnPositive.setTextColor(btnPositiveColor);


            //https://stackoverflow.com/questions/6562924/changing-font-size-into-an-alertdialog
            TextView textView = dialog.findViewById(android.R.id.message);
            textView.setTextSize(22);
        });
        dialog.show();
    }


}
