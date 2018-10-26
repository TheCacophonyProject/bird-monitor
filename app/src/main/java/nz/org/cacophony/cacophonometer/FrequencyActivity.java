package nz.org.cacophony.cacophonometer;

import android.app.Activity;
import android.content.Intent;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Switch;

public class FrequencyActivity extends AppCompatActivity implements IdlingResourceForEspressoTesting{
    private static final String TAG = FrequencyActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frequency);
    }

    @Override
    public void onResume() {
        super.onResume();
        Prefs prefs = new Prefs(getApplicationContext());
        boolean useFrequentRecordings = prefs.getUseFrequentRecordings();

        final Switch switchUseFrequentRecordings = findViewById(R.id.swRecordMoreOften);
        switchUseFrequentRecordings.setChecked(useFrequentRecordings);
    }



    void setUseFrequentRecordings(){
        final Switch switchUseFrequentRecordings = findViewById(R.id.swRecordMoreOften);
        boolean useFrequentRecordings = switchUseFrequentRecordings.isChecked();
        Util.setUseFrequentRecordings(getApplicationContext(), useFrequentRecordings);

    }

    public void next(@SuppressWarnings("UnusedParameters") View v) {
        try {
            setUseFrequentRecordings();
            Intent intent = new Intent(this, TestingActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    public void back(@SuppressWarnings("UnusedParameters") View v) {
        try {
            setUseFrequentRecordings();
            Intent intent = new Intent(this, BatteryActivity.class);
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
