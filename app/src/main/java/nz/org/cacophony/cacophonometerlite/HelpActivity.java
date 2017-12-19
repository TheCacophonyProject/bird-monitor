package nz.org.cacophony.cacophonometerlite;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.slf4j.Logger;

import static com.loopj.android.http.AsyncHttpClient.LOG_TAG;

/**
 * Created by User on 24-Mar-17.
 * Code for the user help screen
 */

public class HelpActivity extends AppCompatActivity {
//    private static Logger logger = null;
private static final String TAG = HelpActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
//        logger = Util.getAndConfigureLogger(getApplicationContext(), LOG_TAG);
//        logger.info("HelpActivity onCreate" );
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
       if (ab != null){
           ab.setDisplayHomeAsUpEnabled(true);
           ab.setDisplayUseLogoEnabled(true);
           ab.setLogo(R.mipmap.ic_launcher);
       }else{
//           Log.w(LOG_TAG, "ActionBar ab is null");
//           Util.writeLocalLogEntryUsingLogback(getApplicationContext(), LOG_TAG, "ActionBar ab is null");
//           logger.info("ActionBar ab is null");
       }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_help, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_settings:
                openSettings();
                return true;

            case R.id.action_vitals:
                openVitals();
                return true;


            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void openSettings() {
        try{
            disableFlightMode();
            Intent intent = new Intent(this, SetupActivity.class);
            startActivity(intent);
        }catch (Exception ex){
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    private void openVitals() {
        Intent intent = new Intent(this, VitalsActivity.class);
        startActivity(intent);
    }

    private void openHelp() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    public void disableFlightMode(){
        try {
            //https://stackoverflow.com/questions/3875184/cant-create-handler-inside-thread-that-has-not-called-looper-prepare
            new Thread()
            {
                public void run()
                {
                    HelpActivity.this.runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            Util.disableFlightMode(getApplicationContext());
                            //  Util.disableFlightModeTestSU(getApplicationContext());
                        }
                    });
                }
            }.start();



        }catch (Exception ex){
//            logger.error(ex.getLocalizedMessage());
            Log.e(TAG, ex.getLocalizedMessage());
            Util.getToast(getApplicationContext(), "Error disabling flight mode", true).show();
        }
    }
}