package nz.org.cacophony.cacophonometer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.ToggleButton;

public class DisableActivity extends AppCompatActivity {
    private static final String TAG = DisableActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disable);

    }

    public void onSwitchDisabledClicked(View v){
        Prefs prefs = new Prefs(getApplicationContext());
        boolean isDisabled = ((Switch) v).isChecked();
        prefs.setIsDisabled(isDisabled);
    }

    @Override
    public void onResume() {
        super.onResume();
        Prefs prefs = new Prefs(getApplicationContext());

        final Switch switchDisable = findViewById(R.id.swDisable);
        switchDisable.setChecked(prefs.getIsDisabled());

    }



    public void back(@SuppressWarnings("UnusedParameters") View v) {

        try {
            finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

}
