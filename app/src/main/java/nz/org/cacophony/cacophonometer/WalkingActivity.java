package nz.org.cacophony.cacophonometer;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ToggleButton;

public class WalkingActivity extends AppCompatActivity {
    private static final String TAG = WalkingActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walking);
    }

    @Override
    public void onResume() {
        super.onResume();
        Prefs prefs = new Prefs(getApplicationContext());

        String modeStr = prefs.getMode();
        final ToggleButton toggleButtonMode = findViewById(R.id.tgbWalking);
        if (modeStr.equalsIgnoreCase("walking")) {
            toggleButtonMode.setChecked(true);
        } else
            toggleButtonMode.setChecked(false);
    }



    void setWalking(){
        Prefs prefs = new Prefs(getApplicationContext());
        final ToggleButton toggleButtonWalking = findViewById(R.id.tgbWalking);
        boolean checked = ( toggleButtonWalking).isChecked();
        if (checked){
            prefs.setMode("walking");
        }else{
            prefs.setMode("normal");
        }
    }

    public void next(@SuppressWarnings("UnusedParameters") View v) {

        try {
            setWalking();
//            Intent intent = new Intent(this, MainActivity2.class);
//            startActivity(intent);
            finish();

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    public void back(@SuppressWarnings("UnusedParameters") View v) {

        try {
            setWalking();
            Intent intent = new Intent(this, GPSActivity.class);
            startActivity(intent);

            finish();

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }
}
