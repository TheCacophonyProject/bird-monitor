package nz.org.cacophony.cacophonometer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class AccountQuestionActivity extends AppCompatActivity {
    private static final String TAG = AccountQuestionActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_question);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public void back(@SuppressWarnings("UnusedParameters") View v) {
        try {
            finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    public void yes(View v) {
        try {
            Intent intent = new Intent(this, SigninActivity.class);
            intent.setType("text/plain");
            intent.putExtra("sending_activity", "AccountQuestionActivity");
            startActivity(intent);
          //  finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }

    }

    public void no(View v) {
        try {
            Intent intent = new Intent(this, SignupActivity.class);
            intent.setType("text/plain");
            intent.putExtra("sending_activity", "AccountQuestionActivity");
            startActivity(intent);
         //   finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }

    }

}
