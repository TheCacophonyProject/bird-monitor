package nz.org.cacophony.birdmonitor.views;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import nz.org.cacophony.birdmonitor.Prefs;
import nz.org.cacophony.birdmonitor.R;
import nz.org.cacophony.birdmonitor.Util;

public class DisableAutomaticRecordingActivity extends AppCompatActivity {
    private static final String TAG = DisableAutomaticRecordingActivity.class.getName();

    private Switch switchDisable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disable_automatic_recording);

        //https://developer.android.com/training/appbar/setting-up#java
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        final Prefs prefs = new Prefs(getApplicationContext());

        switchDisable = findViewById(R.id.swDisable);

        switchDisable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isShown()) {
                return;
            }
            prefs.setAutomaticRecordingsDisabled(!isChecked);
            displayOrHideGUIObjects();

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

    void displayOrHideGUIObjects() {
        Prefs prefs = new Prefs(getApplicationContext());
        boolean isDisabled = prefs.getAutomaticRecordingsDisabled();

        switchDisable.setChecked(!isDisabled);

        if (isDisabled) {
            switchDisable.setText("Recording is OFF");
        } else {
            switchDisable.setText("Recording is ON");
        }
    }


    public void finished(@SuppressWarnings("UnusedParameters") View v) {

        try {
            finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.button_help) {
            Util.displayHelp(this, getResources().getString(R.string.activity_or_fragment_title_turn_off_or_on));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
