package nz.org.cacophony.cacophonometer;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.ToggleButton;

public class WalkingActivity extends AppCompatActivity {
    private static final String TAG = WalkingActivity.class.getName();
    private boolean walkingStateWhenActivityDisplays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walking);
    }

    @Override
    public void onResume() {
        super.onResume();
        Prefs prefs = new Prefs(getApplicationContext());

//        String modeStr = prefs.getMode();
//        final ToggleButton toggleButtonMode = findViewById(R.id.tgbWalking);
//        if (modeStr.equalsIgnoreCase("walking")) {
//            toggleButtonMode.setChecked(true);
//        } else
//            toggleButtonMode.setChecked(false);

        boolean walkingMode = true; // if any of the following are false, then change walking mode to false
        walkingStateWhenActivityDisplays = true;
        //if (!prefs.getOffLineMode()){
        if (!prefs.getInternetConnectionMode().equalsIgnoreCase("offline")) {
            walkingMode = false;
            walkingStateWhenActivityDisplays = false;
        }else if(!prefs.getUseFrequentRecordings()){
            walkingMode = false;
            walkingStateWhenActivityDisplays = false;
        }else if (!prefs.getIgnoreLowBattery()){
            walkingMode = false;
            walkingStateWhenActivityDisplays = false;
        }else if (!prefs.getPlayWarningSound()){
            walkingMode = false;
            walkingStateWhenActivityDisplays = false;
        }else if (!prefs.getPeriodicallyUpdateGPS()){
            walkingMode = false;
            walkingStateWhenActivityDisplays = false;
        }else if (!prefs.getIsDisableDawnDuskRecordings()){
            walkingMode = false;
            walkingStateWhenActivityDisplays = false;
        }else if (prefs.getUseFrequentUploads()){
            walkingMode = false;
            walkingStateWhenActivityDisplays = false;
        }

//        final ToggleButton toggleButtonMode = findViewById(R.id.tgbWalking);
//        toggleButtonMode.setChecked(walkingMode);

        final Switch switchWalking = findViewById(R.id.swWalking2);
        switchWalking.setChecked(prefs.getIsDisabled());
    }



    void setWalking(){
        // will check to see if mode has changed before changing prefs values etc
        final Switch switchWalking = findViewById(R.id.swWalking2);
        boolean isWalking = switchWalking.isChecked();
        if (walkingStateWhenActivityDisplays == isWalking){
            // User hasn't changed the state so don't do anything
            return;
        }
        Util.setWalkingMode(getApplicationContext(),isWalking);

    }

//    void setWalking(){
//        // will check to see if mode has changed before changing prefs values etc
//        final ToggleButton toggleButtonWalking = findViewById(R.id.tgbWalking);
//        boolean checked = ( toggleButtonWalking).isChecked();
//        if (walkingStateWhenActivityDisplays == checked){
//            // User hasn't changed the state so don't do anything
//            return;
//        }
//        Util.setWalkingMode(getApplicationContext(),checked);
//
//    }

    public void next(@SuppressWarnings("UnusedParameters") View v) {

        try {
            setWalking();
//            Intent intent = new Intent(this, MainActivity2.class);
//            startActivity(intent);
            finish();

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    public void back(@SuppressWarnings("UnusedParameters") View v) {

        try {
            setWalking();
            Intent intent = new Intent(this, GPSActivity.class);
            startActivity(intent);

            finish();

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }
}
