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
import android.widget.RadioButton;
import android.widget.Switch;

public class SoundActivity extends AppCompatActivity implements IdlingResourceForEspressoTesting{
    private static final String TAG = SoundActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound);

        final Prefs prefs = new Prefs(getApplicationContext());

        final Switch switchPlayWarningSound = findViewById(R.id.swPlayWarningSound);
        switchPlayWarningSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!buttonView.isShown()){
                    return;
                }
                prefs.setPlayWarningSound(isChecked);
            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Prefs prefs = new Prefs(getApplicationContext());
        boolean playWarningSound = prefs.getPlayWarningSound();

        final Switch switchPlayWarningSound = findViewById(R.id.swPlayWarningSound);
        switchPlayWarningSound.setChecked(playWarningSound);

    }


    public void next(@SuppressWarnings("UnusedParameters") View v) {
        try {

            Intent intent = new Intent(this, BatteryActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    public void back(@SuppressWarnings("UnusedParameters") View v) {
        try {

            Intent intent = new Intent(this, InternetConnectionActivity.class);
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
