package nz.org.cacophony.cacophonometer;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.ToggleButton;

public class WalkingActivity extends AppCompatActivity {
    private static final String TAG = WalkingActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walking);

        final Switch switchWalking = findViewById(R.id.swWalking2);
        switchWalking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean isWalking = switchWalking.isChecked();
                Util.setWalkingMode(getApplicationContext(),isWalking);
                findViewById(R.id.btnUploadFiles).setEnabled(!isWalking);
            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Prefs prefs = new Prefs(getApplicationContext());

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


    public void uploadFiles(@SuppressWarnings("UnusedParameters") View v){
        Util.uploadFilesUsingUploadButton(getApplicationContext());
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
}
