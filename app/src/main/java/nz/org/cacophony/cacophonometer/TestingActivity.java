package nz.org.cacophony.cacophonometer;

import android.app.Activity;
import android.content.Intent;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;

public class TestingActivity extends AppCompatActivity implements IdlingResourceForEspressoTesting{
    private static final String TAG = TestingActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

       final Prefs prefs = new Prefs(getApplicationContext());

        final Switch swUseShortRecordings = findViewById(R.id.swShortRecordings);
        swUseShortRecordings.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.setUseShortRecordings(isChecked);
            }
        });

        final Switch swUseTestServer = findViewById(R.id.swUseTestServer);
        swUseTestServer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.setUseTestServer(isChecked);
            }
        });

        final Switch swUseVeryFrequentRecordings = findViewById(R.id.swUseVeryFrequentRecordings);
        swUseVeryFrequentRecordings.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.setUseVeryFrequentRecordings(isChecked);
            }
        });

        final Switch swUseFrequentUploads = findViewById(R.id.swUseFrequentUploads);
        swUseFrequentUploads.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.setUseFrequentUploads(isChecked);
            }
        });

        final Switch swPeriodicallyUpdateGPS = findViewById(R.id.swPeriodicallyUpdateGPS);
        swPeriodicallyUpdateGPS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.setPeriodicallyUpdateGPS(isChecked);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        Prefs prefs = new Prefs(getApplicationContext());

        boolean useShortRecordings = prefs.getUseShortRecordings();
        final Switch swUseShortRecordings = findViewById(R.id.swShortRecordings);
        swUseShortRecordings.setChecked(useShortRecordings);

        boolean useTestServer = prefs.getUseTestServer();
        final Switch swUseTestServer = findViewById(R.id.swUseTestServer);
        swUseTestServer.setChecked(useTestServer);

        boolean useVeryFrequentRecordings = prefs.getUseVeryFrequentRecordings();
        final Switch swUseVeryFrequentRecordings = findViewById(R.id.swUseVeryFrequentRecordings);
        swUseVeryFrequentRecordings.setChecked(useVeryFrequentRecordings);

        boolean useFrequentUploads = prefs.getUseFrequentUploads();
        final Switch swUseFrequentUploads = findViewById(R.id.swUseFrequentUploads);
        swUseFrequentUploads.setChecked(useFrequentUploads);

        boolean periodicallyUpdateGPS = prefs.getPeriodicallyUpdateGPS();
        final Switch swPeriodicallyUpdateGPS = findViewById(R.id.swPeriodicallyUpdateGPS);
        swPeriodicallyUpdateGPS.setChecked(periodicallyUpdateGPS);

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
            Intent intent = new Intent(this, FrequencyActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
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
