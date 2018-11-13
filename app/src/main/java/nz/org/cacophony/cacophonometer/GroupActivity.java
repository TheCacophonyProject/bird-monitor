package nz.org.cacophony.cacophonometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;

public class GroupActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = GroupActivity.class.getName();
    private EditText editTxt;
    private Button btn;
    private ListView list;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        //https://stackoverflow.com/questions/4540754/dynamically-add-elements-to-a-listview-android

        editTxt = (EditText) findViewById(R.id.etNewGroupInput);
        btn = (Button) findViewById(R.id.button);
        list = (ListView) findViewById(R.id.lvGroups);
        arrayList = new ArrayList<String>();

        // Adapter: You need three parameters 'the context, id of the layout (it will be where the data is shown),
        // and the array that contains the data
//        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, arrayList);

        adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.activity_group, arrayList);
        // Here, you set the data in your ListView
        list.setAdapter(adapter);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // this line adds the data of your EditText and puts in your array
                arrayList.add(editTxt.getText().toString());
                // next thing you have to do is check if your adapter has changed
                adapter.notifyDataSetChanged();
            }
        });

        //https://developer.android.com/training/appbar/setting-up#java
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    @Override
    public void onResume() {
        super.onResume();
        Prefs prefs = new Prefs(getApplicationContext());

        IntentFilter iff = new IntentFilter("event");
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
                Toast.LENGTH_SHORT).show();
    }

    private final BroadcastReceiver onNotice = new BroadcastReceiver() {
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

        // broadcast notification coming from ??
        @Override
        public void onReceive(Context context, Intent intent) {
            Prefs prefs = new Prefs(getApplicationContext());
            try {
                String message = intent.getStringExtra("message");
                if (message != null) {

                    JSONObject joMessage = new JSONObject(message);
                    String intendedActivity = joMessage.getString("activityName");
                    String messageToDisplay = joMessage.getString("messageToDisplay");

                    if (intendedActivity.equalsIgnoreCase("GroupActivity")){

                      // do something



                    }
                }


            } catch (Exception ex) {

                Log.e(TAG, ex.getLocalizedMessage());
            }
        }
    };
}
