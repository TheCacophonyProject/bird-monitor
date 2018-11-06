package nz.org.cacophony.cacophonometer;

import android.content.Intent;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;

public class InternetConnectionActivity extends AppCompatActivity implements IdlingResourceForEspressoTesting{
    private static final String TAG = InternetConnectionActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet_connection);

        //https://developer.android.com/training/appbar/setting-up#java
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
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
            Intent intent = new Intent(this, ManageRecordingsActivity.class);
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
                Util.displayHelp(this, "Internet Connection");
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
