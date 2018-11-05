package nz.org.cacophony.cacophonometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.File;

public class UploadFilesActivity extends AppCompatActivity {
    private static final String TAG = UploadFilesActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_files);

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

        TextView deviceNameText = findViewById(R.id.tvNumberOfRecordings);
        deviceNameText.setText("Number of recordings on phone: " + getNumberOfRecordings());

        IntentFilter iff = new IntentFilter("event");
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);

        if(getNumberOfRecordings() == 0){
            findViewById(R.id.btnUploadFiles).setEnabled(false);
        }else{
            findViewById(R.id.btnUploadFiles).setEnabled(true);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
    }

    private int getNumberOfRecordings(){
        File recordingsFolder = Util.getRecordingsFolder(getApplicationContext());
        File recordingFiles[] = recordingsFolder.listFiles();
        return recordingFiles.length;
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

        if (getNumberOfRecordings() > 0){ // should be as button should be disabled if no recordings
            Util.getToast(getApplicationContext(), "About to upload " + numberOfFilesToUpload + " recordings.", false).show();
            findViewById(R.id.btnUploadFiles).setEnabled(false);
            Util.uploadFilesUsingUploadButton(getApplicationContext());
        }else{
            Util.getToast(getApplicationContext(), "There are no recordings on the phone to upload.", true).show();
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
                    TextView deviceNameText = findViewById(R.id.tvNumberOfRecordings);

                    if (message.equalsIgnoreCase("files_successfully_uploaded")) {
                        Util.getToast(getApplicationContext(), "Files have been uploaded to the server", false).show();
                       // findViewById(R.id.btnUploadFiles).setEnabled(true);
                        deviceNameText.setText("Number of recordings on phone: " + getNumberOfRecordings());
                    } else if (message.equalsIgnoreCase("files_not_uploaded")) {
                        Util.getToast(getApplicationContext(), "Error: Unable to upload files", true).show();
                        deviceNameText.setText("Number of recordings on phone: " + getNumberOfRecordings());
                        if (getNumberOfRecordings() > 0){
                            findViewById(R.id.btnUploadFiles).setEnabled(true);
                        }
                    }
                }

            } catch (Exception ex) {

                Log.e(TAG, ex.getLocalizedMessage());
            }
        }
    };

    public void next(@SuppressWarnings("UnusedParameters") View v) {
        try {

            finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    public void back(@SuppressWarnings("UnusedParameters") View v) {

        try {
            Intent intent = new Intent(this, WalkingActivity.class);
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
                Util.displayHelp(this, "Upload Recordings");
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }
}
