package nz.org.cacophony.cacophonometer;

import android.app.Activity;
import android.content.Intent;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

public class TestingActivity extends AppCompatActivity implements IdlingResourceForEspressoTesting{
    private static final String TAG = TestingActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);
    }

    @Override
    public void onResume() {
        super.onResume();
        Prefs prefs = new Prefs(getApplicationContext());

        boolean useShortRecordings = prefs.getUseShortRecordings();
        final CheckBox checkBoxUseUseShortRecordings = findViewById(R.id.cbShortRecordings);
        checkBoxUseUseShortRecordings.setChecked(useShortRecordings);
//        if (useShortRecordings) {
//            checkBoxUseUseShortRecordings.setChecked(true);
//        } else
//            checkBoxUseUseShortRecordings.setChecked(false);

        boolean useTestServer = prefs.getUseTestServer();
        final CheckBox checkBoxUseTestServer = findViewById(R.id.cbUseTestServer);
        checkBoxUseTestServer.setChecked(useTestServer);
//        if (useTestServer) {
//            checkBoxUseTestServer.setChecked(true);
//        } else
//            checkBoxUseTestServer.setChecked(false);

        boolean useVeryFrequentRecordings = prefs.getUseVeryFrequentRecordings();
        final CheckBox checkBoxUseVeryFrequentRecordings = findViewById(R.id.cbUseVeryFrequentRecordings);
        checkBoxUseVeryFrequentRecordings.setChecked(useVeryFrequentRecordings);
//        if (useVeryFrequentRecordings) {
//            checkBoxUseVeryFrequentRecordings.setChecked(true);
//        } else
//            checkBoxUseVeryFrequentRecordings.setChecked(false);

        boolean useFrequentUploads = prefs.getUseFrequentUploads();
        final CheckBox checkBoxUseFrequentUploads = findViewById(R.id.cbUseFrequentUploads);
        checkBoxUseFrequentUploads.setChecked(useFrequentUploads);

        boolean periodicallyUpdateGPS = prefs.getPeriodicallyUpdateGPS();
        final CheckBox cbPeriodicallyUpdateGPS = findViewById(R.id.cbPeriodicallyUpdateGPS);
        cbPeriodicallyUpdateGPS.setChecked(periodicallyUpdateGPS);

    }


    public void onCheckboxShortRecordingsClicked(View v) {
        Prefs prefs = new Prefs(getApplicationContext());
        // Is the view now checked?
        boolean checked = ((CheckBox) v).isChecked();
        prefs.setUseShortRecordings(checked);
//        if (checked){
//            prefs.setUseShortRecordings(true);
//        }else{
//            prefs.setUseShortRecordings(false);
//        }
    }

    public void onCheckboxUseTestServerClicked(View v) {
        Prefs prefs = new Prefs(getApplicationContext());
        // Is the view now checked?
        boolean checked = ((CheckBox) v).isChecked();
        prefs.setUseTestServer(checked);
//        if (checked){
//            prefs.setUseTestServer(true);
//        }else{
//            prefs.setUseTestServer(false);
//        }
    }

    public void onCheckboxVeryFrequentRecordingsClicked(View v) {
        boolean checked = ((CheckBox) v).isChecked();
        Util.setUseVeryFrequentRecordings(getApplicationContext(), checked);
    }

    public void onCheckboxUseFrequentUploadsClicked(View v) {
        Prefs prefs = new Prefs(getApplicationContext());
        // Is the view now checked?
        boolean checked = ((CheckBox) v).isChecked();
        prefs.setUseFrequentUploads(checked);
    }

    public void onCheckboxUsePeriodicallyUpdateGPSClicked(View v) {
        boolean checked = ((CheckBox) v).isChecked();
        Util.setPeriodicallyUpdateGPS(getApplicationContext(), checked);
    }

    public void next(@SuppressWarnings("UnusedParameters") View v) {
        try {
//            Intent intent = new Intent(this, MainActivity2.class);
//            startActivity(intent);
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
