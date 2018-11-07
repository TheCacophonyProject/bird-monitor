package nz.org.cacophony.cacophonometer;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

public class RootedActivity extends AppCompatActivity {
    private static final String TAG = RootedActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooted);

        //https://developer.android.com/training/appbar/setting-up#java
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        final Prefs prefs = new Prefs(getApplicationContext());

        final Switch switchHasRootAccess = findViewById(R.id.swRooted);
        switchHasRootAccess.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.setHasRootAccess(isChecked);
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
        boolean hasRootAccess = prefs.getHasRootAccess();

        final Switch switchRooted = findViewById(R.id.swRooted);
        switchRooted.setChecked(hasRootAccess);
    }

    public void next(@SuppressWarnings("UnusedParameters") View v) {

        try {
//            Intent intent = new Intent(this, GPSActivity.class);
//            startActivity(intent);
            final Prefs prefs = new Prefs(getApplicationContext());
            if (prefs.getSettingsForTestServerEnabled()){
                final Button btnNext = (Button)findViewById(R.id.btnNext);
               // btnNext.setText("Next - Testing");
                Intent intent = new Intent(this, TestingActivity.class);
                startActivity(intent);
            }
            finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    public void back(@SuppressWarnings("UnusedParameters") View v) {

        try {
           // Intent intent = new Intent(this, RegisterActivity.class);
            Intent intent = new Intent(this, FrequencyActivity.class);
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
                Util.displayHelp(this, "Rooted");
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

}
