package nz.org.cacophony.cacophonometer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;



public class MainActivity2 extends AppCompatActivity {
    private static final String TAG = MainActivity2.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);



    }

    public void setup(@SuppressWarnings("UnusedParameters") View v) {

        try {

            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }


    }

}
