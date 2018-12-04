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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;

public class TestingActivity extends AppCompatActivity implements IdlingResourceForEspressoTesting{
    private static final String TAG = TestingActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        //https://developer.android.com/training/appbar/setting-up#java
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

       final Prefs prefs = new Prefs(getApplicationContext());



        final Switch swUseTestServer = findViewById(R.id.swUseTestServer);
        swUseTestServer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // https://stackoverflow.com/questions/17372750/android-setoncheckedchangelistener-calls-again-when-old-view-comes-back
                if (!buttonView.isShown()){
                    return;
                }
                prefs.setUseTestServer(isChecked);

                final Switch swUseShortRecordings = findViewById(R.id.swShortRecordings);
                swUseShortRecordings.setEnabled(isChecked);

                final Switch swUseVeryFrequentRecordings = findViewById(R.id.swUseVeryFrequentRecordings);
                swUseVeryFrequentRecordings.setEnabled(isChecked);

                if (!isChecked){
                    prefs.setUseShortRecordings(false);
                    swUseShortRecordings.setChecked(false);
                    swUseShortRecordings.setEnabled(false);

                    prefs.setUseVeryFrequentRecordings(false);
                    swUseVeryFrequentRecordings.setChecked(false);
                    swUseVeryFrequentRecordings.setEnabled(false);
                }

            }
        });



        final Switch swUseVeryFrequentRecordings = findViewById(R.id.swUseVeryFrequentRecordings);
        swUseVeryFrequentRecordings.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!buttonView.isShown()){
                    return;
                }
                Util.setUseVeryFrequentRecordings(getApplicationContext(), isChecked);
            }
        });

        final Switch swUseShortRecordings = findViewById(R.id.swShortRecordings);
        swUseShortRecordings.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!buttonView.isShown()){
                    return;
                }
                prefs.setUseShortRecordings(isChecked);
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

        boolean useTestServer = prefs.getUseTestServer();
        final Switch swUseTestServer = findViewById(R.id.swUseTestServer);
        swUseTestServer.setChecked(useTestServer);

        boolean useVeryFrequentRecordings = prefs.getUseVeryFrequentRecordings();
        final Switch swUseVeryFrequentRecordings = findViewById(R.id.swUseVeryFrequentRecordings);
        swUseVeryFrequentRecordings.setChecked(useVeryFrequentRecordings);
        swUseVeryFrequentRecordings.setEnabled(useTestServer);// only enabled if using test server

        boolean useShortRecordings = prefs.getUseShortRecordings();
        final Switch swUseShortRecordings = findViewById(R.id.swShortRecordings);
        swUseShortRecordings.setChecked(useShortRecordings);
        swUseShortRecordings.setEnabled(useTestServer); // only enabled if using test server

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
//            Intent intent = new Intent(this, FrequencyActivity.class);
            Intent intent = new Intent(this, RootedActivity.class);
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
                Util.displayHelp(this, getResources().getString(R.string.activity_or_fragment_title_settings_for_testing));
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
