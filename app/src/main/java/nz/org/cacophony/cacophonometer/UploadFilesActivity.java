package nz.org.cacophony.cacophonometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;

public class UploadFilesActivity extends AppCompatActivity {
    private static final String TAG = UploadFilesActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_files);
    }

    @Override
    public void onResume() {
        super.onResume();
        Prefs prefs = new Prefs(getApplicationContext());

        IntentFilter iff = new IntentFilter("event");
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);

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

                    if (message.equalsIgnoreCase("files_successfully_uploaded")) {
                        Util.getToast(getApplicationContext(), "Files have been uploaded to the server", false).show();
                        findViewById(R.id.btnUploadFiles).setEnabled(true);
                    } else if (message.equalsIgnoreCase("files_not_uploaded")) {
                        Util.getToast(getApplicationContext(), "Error: Unable to upload files", true).show();
                        findViewById(R.id.btnUploadFiles).setEnabled(true);
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
}
