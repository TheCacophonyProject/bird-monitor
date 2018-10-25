package nz.org.cacophony.cacophonometer;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

public class RootedActivity extends AppCompatActivity {
    private static final String TAG = RootedActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooted);
    }

    @Override
    public void onResume() {
        super.onResume();
        Prefs prefs = new Prefs(getApplicationContext());
        boolean hasRootAccess = prefs.getHasRootAccess();

        final Switch switchRooted = findViewById(R.id.swRooted);
        switchRooted.setChecked(hasRootAccess);
//        final ToggleButton toggleButtonRootAccess = findViewById(R.id.tgbRooted);
//        if (hasRootAccess) {
//            toggleButtonRootAccess.setChecked(true);
//        } else
//            toggleButtonRootAccess.setChecked(false);
    }

    void setRooted(){
        final Switch switchHasRootAccess = findViewById(R.id.swRooted);
        boolean hasRootAccess = switchHasRootAccess.isChecked();
        Prefs prefs = new Prefs(getApplicationContext());
        prefs.setHasRootAccess(hasRootAccess);
//
//        final ToggleButton toggleButtonRooted = findViewById(R.id.tgbRooted);
//        boolean checked = ( toggleButtonRooted).isChecked();
//        if (checked){
//            prefs.setHasRootAccess(true);
//        }else{
//            prefs.setHasRootAccess(false);
//        }
    }

//    public void ontoggleButtonRootAccess(View v) {
//        Prefs prefs = new Prefs(getApplicationContext());
//        // Is the view now checked?
//        boolean checked = ((ToggleButton) v).isChecked();
//        if (checked){
//            prefs.setHasRootAccess(true);
//        }else{
//            prefs.setHasRootAccess(false);
//        }
//    }

//    void setRooted(){
//        Prefs prefs = new Prefs(getApplicationContext());
//        final ToggleButton toggleButtonRooted = findViewById(R.id.tgbRooted);
//        boolean checked = ( toggleButtonRooted).isChecked();
//        if (checked){
//            prefs.setHasRootAccess(true);
//        }else{
//            prefs.setHasRootAccess(false);
//        }
//    }

    public void next(@SuppressWarnings("UnusedParameters") View v) {

        try {
            setRooted();
            Intent intent = new Intent(this, GPSActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    public void back(@SuppressWarnings("UnusedParameters") View v) {

        try {
            setRooted();
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

}
