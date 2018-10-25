package nz.org.cacophony.cacophonometer;

import android.content.Intent;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;

public class InternetConnectionActivity extends AppCompatActivity implements IdlingResourceForEspressoTesting{
    private static final String TAG = InternetConnectionActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet_connection);
    }


    @Override
    public void onResume() {
        super.onResume();
        Prefs prefs = new Prefs(getApplicationContext());
        String mode = prefs.getInternetConnectionMode();
        switch (mode) {

            case "normal":
                final RadioButton normalInternetModeRadioButton = findViewById(R.id.rbNormal);
                normalInternetModeRadioButton.setChecked(true);
                break;
            case "offline":
                final RadioButton offlineInternetModeRadioButton = findViewById(R.id.rbOffline);
                offlineInternetModeRadioButton.setChecked(true);
                break;
            case "online":
                final RadioButton onlineInternetModeRadioButton = findViewById(R.id.rbOnline);
                onlineInternetModeRadioButton.setChecked(true);
                break;
        }

    }



    public void next(@SuppressWarnings("UnusedParameters") View v) {
        try {
            Intent intent = new Intent(this, SoundActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    public void back(@SuppressWarnings("UnusedParameters") View v) {
        try {
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

    public void onInternetModeRadioButtonClicked(@SuppressWarnings("UnusedParameters") View v) {
        Prefs prefs = new Prefs(getApplicationContext());
        boolean checked = ((RadioButton) v).isChecked();
        // Check which radio button was clicked
        switch (v.getId()) {
            case R.id.rbNormal:
                if (checked) {
                    prefs.setInternetConnectionMode("normal");
                }
                break;
            case R.id.rbOffline:
                if (checked) {
                    prefs.setInternetConnectionMode("offline");
                }
                break;
            case R.id.rbOnline:
                if (checked) {
                    prefs.setInternetConnectionMode("online");
                }
                break;

        }
    }


}
