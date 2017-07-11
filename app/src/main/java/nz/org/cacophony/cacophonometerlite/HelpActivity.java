package nz.org.cacophony.cacophonometerlite;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;

import org.slf4j.Logger;

import static com.loopj.android.http.AsyncHttpClient.LOG_TAG;

/**
 * Created by User on 24-Mar-17.
 * Code for the user help screen
 */

public class HelpActivity extends AppCompatActivity {
    private static Logger logger = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        logger = Util.getAndConfigureLogger(getApplicationContext(), LOG_TAG);
        logger.info("HelpActivity onCreate" );
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
           logger.info("ActionBar ab is null");
       }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}