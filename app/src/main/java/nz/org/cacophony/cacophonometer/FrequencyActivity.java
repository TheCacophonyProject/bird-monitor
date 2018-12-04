package nz.org.cacophony.cacophonometer;

import android.app.Activity;
import android.content.Intent;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;

public class FrequencyActivity extends AppCompatActivity implements IdlingResourceForEspressoTesting{
    private static final String TAG = FrequencyActivity.class.getName();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frequency);

        //https://developer.android.com/training/appbar/setting-up#java
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        final Prefs prefs = new Prefs(getApplicationContext());
        final Button btnNext = (Button)findViewById(R.id.btnNext);
//        if (prefs.getSettingsForTestServerEnabled()){
//            btnNext.setText("Next - Testing");
//        }

        final Switch switchUseFrequentRecordings = findViewById(R.id.swRecordMoreOften);
        switchUseFrequentRecordings.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!buttonView.isShown()){
                    return;
                }
                Util.setUseFrequentRecordings(getApplicationContext(), isChecked);
            }

        });

        final Switch swUseFrequentUploads = findViewById(R.id.swUseFrequentUploads);
        swUseFrequentUploads.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!buttonView.isShown()){
                    return;
                }
                prefs.setUseFrequentUploads(isChecked);
            }
        });

        final Switch swPeriodicallyUpdateGPS = findViewById(R.id.swPeriodicallyUpdateGPS);
        swPeriodicallyUpdateGPS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!buttonView.isShown()){
                    return;
                }
                Util.setPeriodicallyUpdateGPS(getApplicationContext(), isChecked);
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
        Prefs prefs = new Prefs(getApplicationContext());
        boolean useFrequentRecordings = prefs.getUseFrequentRecordings();

        final Switch switchUseFrequentRecordings = findViewById(R.id.swRecordMoreOften);
        switchUseFrequentRecordings.setChecked(useFrequentRecordings);

        boolean useFrequentUploads = prefs.getUseFrequentUploads();
        final Switch swUseFrequentUploads = findViewById(R.id.swUseFrequentUploads);
        swUseFrequentUploads.setChecked(useFrequentUploads);

        boolean periodicallyUpdateGPS = prefs.getPeriodicallyUpdateGPS();
        final Switch swPeriodicallyUpdateGPS = findViewById(R.id.swPeriodicallyUpdateGPS);
        swPeriodicallyUpdateGPS.setChecked(periodicallyUpdateGPS);
    }

    public void next(@SuppressWarnings("UnusedParameters") View v) {
        try {

            Intent intent = new Intent(this, RootedActivity.class);
            startActivity(intent);

//            final Prefs prefs = new Prefs(getApplicationContext());
//            if (prefs.getSettingsForTestServerEnabled()){
//                final Button btnNext = (Button)findViewById(R.id.btnNext);
//                btnNext.setText("Next - Testing");
//                Intent intent = new Intent(this, TestingActivity.class);
//                startActivity(intent);
//            }

            finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    public void back(@SuppressWarnings("UnusedParameters") View v) {
        try {
            Intent intent = new Intent(this, BatteryActivity.class);
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
                Util.displayHelp(this,  getResources().getString(R.string.activity_or_fragment_title_activity_frequency));

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("SameReturnValue")
    public CountingIdlingResource getIdlingResource() {
        return registerIdlingResource;
    }

    @SuppressWarnings("SameReturnValue")
    public CountingIdlingResource getRecordNowIdlingResource() {
        return recordNowIdlingResource;
    }
}
