package nz.org.cacophony.birdmonitor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import nz.org.cacophony.birdmonitor.R;

public class WalkingActivity extends AppCompatActivity implements IdlingResourceForEspressoTesting{
    private static final String TAG = WalkingActivity.class.getName();
    private Switch switchWalking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walking);

        //https://developer.android.com/training/appbar/setting-up#java
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        switchWalking = findViewById(R.id.swWalking2);
        switchWalking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (switchWalking.isShown()) { // Listener was firing each time activity loaded - https://stackoverflow.com/questions/17372750/android-setoncheckedchangelistener-calls-again-when-old-view-comes-back
                    Util.setWalkingMode(getApplicationContext(), isChecked);
                    displayOrHideGUIObjects();
                }
            }
        });
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


    void displayOrHideGUIObjects(){
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

        switchWalking.setChecked(walkingMode);

        if (walkingMode){
            switchWalking.setText("Walking is ON");
        }else{
            switchWalking.setText("Walking is OFF");
        }
    }

    public void finished(@SuppressWarnings("UnusedParameters") View v) {
        try {
            finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.button_help:
                Util.displayHelp(this, getResources().getString(R.string.activity_or_fragment_title_walking));
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }


//    @SuppressWarnings("SameReturnValue")
//    public CountingIdlingResource getIdlingResource() {
//        return registerIdlingResource;
//    }
//
//    @SuppressWarnings("SameReturnValue")
//    public CountingIdlingResource getRecordNowIdlingResource() {
//        return recordNowIdlingResource;
//    }
}
