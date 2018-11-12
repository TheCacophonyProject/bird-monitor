package nz.org.cacophony.cacophonometer;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class WelcomeActivity extends AppCompatActivity implements IdlingResourceForEspressoTesting{
    private static final String TAG = WelcomeActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    @Override
    public void onResume() {
        super.onResume();

       // displayOrHideGUIObjects();

     //   IntentFilter iff = new IntentFilter("event");
      //  LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);


    }

    public void yes(View v) {
        try {
            Intent intent = new Intent(this, RegisterActivity.class);
            intent.setType("text/plain");
            intent.putExtra("sending_activity", "WelcomeActivity");
            startActivity(intent);
           // finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }

    }

    public void noNotSure(View v) {

        try {
            Intent intent = new Intent(this, AccountQuestionActivity.class);
            startActivity(intent);
         //   finish();
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
