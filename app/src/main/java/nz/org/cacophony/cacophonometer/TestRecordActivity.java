package nz.org.cacophony.cacophonometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TestRecordActivity extends AppCompatActivity implements IdlingResourceForEspressoTesting{
    private static final String TAG = TestRecordActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_record);

        //https://developer.android.com/training/appbar/setting-up#java
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_help, menu);
        return true;
    }

    @Override
    public void onResume() {
         super.onResume();
         Prefs prefs = new Prefs(getApplicationContext());
        findViewById(R.id.btnRecordNow).setEnabled(!(RecordAndUpload.isRecording || prefs.getIsDisabled()));
        if (prefs.getIsDisabled()){
            ((TextView)findViewById(R.id.tvMessages)).setText("The App is currently disabled");
        }



    }

    @Override
    protected void onPause() {
        super.onPause();
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.button_help:
                Util.displayHelp(this, "Test Record");
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    public void recordNowButtonClicked(@SuppressWarnings("UnusedParameters") View v) {

        recordNowIdlingResource.increment();

        Util.getToast(getApplicationContext(), "Prepare to start recording", false).show();

        findViewById(R.id.btnRecordNow).setEnabled(false);

        Intent myIntent = new Intent(TestRecordActivity.this, StartRecordingReceiver.class);
        myIntent.putExtra("callingCode", "recordNowButtonClicked"); // for debugging
        try {
            myIntent.putExtra("type", "recordNowButton");
            sendBroadcast(myIntent);

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }

      //  Util.createCreateAlarms(getApplicationContext());
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

                    if (message.equalsIgnoreCase("recordNowButton_finished")) {
                        findViewById(R.id.btnRecordNow).setEnabled(true);
                        tvMessages.setText("Finished");
                        recordNowIdlingResource.decrement();
                    } else if (message.equalsIgnoreCase("recording_started")) {
                        tvMessages.setText("Recording");
                        Util.getToast(getApplicationContext(), "Recording started", false).show();
                    } else if (message.equalsIgnoreCase("recording_finished")) {
                        tvMessages.setText("Finished");
                        Util.getToast(getApplicationContext(), "Recording finished", false).show();
                    } else if (message.equalsIgnoreCase("about_to_upload_files")) {
                        tvMessages.setText("About to upload files");
                        Util.getToast(getApplicationContext(), "About to upload files", false).show();
                    } else if (message.equalsIgnoreCase("files_successfully_uploaded")) {
                        tvMessages.setText("Files successfully uploaded");
                        Util.getToast(getApplicationContext(), "Files successfully uploaded", false).show();
                    } else if (message.equalsIgnoreCase("already_uploading")) {
                        tvMessages.setText("Files are already uploading");
                        Util.getToast(getApplicationContext(), "Files are already uploading", false).show();
                    } else if (message.equalsIgnoreCase("no_permission_to_record")) {
                        tvMessages.setText("Can not record.  Please go to Android settings and enable all required permissions for this app");
                        Util.getToast(getApplicationContext(), "Can not record.  Please go to Android settings and enable all required permissions for this app", true).show();
                        findViewById(R.id.btnRecordNow).setEnabled(true);
                        recordNowIdlingResource.decrement();
                    } else if (message.equalsIgnoreCase("recording_and_uploading_finished")) {
                        tvMessages.setText("Recording and uploading finished");
                        Util.getToast(getApplicationContext(), "Recording and uploading finished", false).show();

                    } else if (message.equalsIgnoreCase("recording_finished_but_uploading_failed")) {
                        tvMessages.setText("Recording finished but uploading failed");
                        Util.getToast(context, "Recording finished but uploading failed", true).show();
                        findViewById(R.id.btnRecordNow).setEnabled(true && !prefs.getIsDisabled());
                        recordNowIdlingResource.decrement();
                    } else if (message.equalsIgnoreCase("recorded_successfully_no_network")) {
                        tvMessages.setText("Recorded successfully, no network connection so did not upload");
                        Util.getToast(getApplicationContext(), "Recorded successfully, no network connection so did not upload", false).show();
                    } else if (message.equalsIgnoreCase("recording_failed")) {
                        tvMessages.setText("Recording failed");
                        Util.getToast(getApplicationContext(), "Recording failed", true).show();
                        findViewById(R.id.btnRecordNow).setEnabled(true && !prefs.getIsDisabled());
                    } else if (message.equalsIgnoreCase("not_logged_in")) {
                        tvMessages.setText("Not logged in to server, could not upload files");
                        Util.getToast(getApplicationContext(), "Not logged in to server, could not upload files", true).show();
                    } else if (message.equalsIgnoreCase("is_already_recording")) {                  //      uploadingIdlingResource.decrement();
                        // Will need enable Record Now button
                        findViewById(R.id.btnRecordNow).setEnabled(true && !prefs.getIsDisabled());
                        recordNowIdlingResource.decrement();
                        tvMessages.setText("Could not do a recording as another recording is already in progress");
                        Util.getToast(getApplicationContext(), "Could not do a recording as another recording is already in progress", true).show();
                        findViewById(R.id.btnRecordNow).setEnabled(true && !prefs.getIsDisabled());
                        recordNowIdlingResource.decrement();
                    } else if (message.equalsIgnoreCase("error_do_not_have_root")) {
                        tvMessages.setText("It looks like you have incorrectly indicated in settings that this phone has been rooted");
                        Util.getToast(getApplicationContext(), "It looks like you have incorrectly indicated in settings that this phone has been rooted", true).show();
                    }else if (message.equalsIgnoreCase("update_record_now_button")){
                        findViewById(R.id.btnRecordNow).setEnabled(!RecordAndUpload.isRecording && !prefs.getIsDisabled());
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
