package nz.org.cacophony.cacophonometer;

import android.app.Activity;
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

//public class GroupActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    public class GroupActivity extends AppCompatActivity {
    private static final String TAG = GroupActivity.class.getName();
    private EditText etNewGroupInput;
    private Button btnAddGroup;
    private ListView lvGroups;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayListGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        //https://developer.android.com/training/appbar/setting-up#java
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        //https://stackoverflow.com/questions/4540754/dynamically-add-elements-to-a-listview-android

        etNewGroupInput = (EditText) findViewById(R.id.etNewGroupInput);
        btnAddGroup = (Button) findViewById(R.id.btnAddGroup);
        lvGroups = (ListView) findViewById(R.id.lvGroups);
        arrayListGroups = Util.getGroups(this);

        // Adapter: You need three parameters 'the context, id of the layout (it will be where the data is shown),
        // and the array that contains the data
//        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, arrayListGroups);

        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayListGroups);
        // Here, you set the data in your ListView
        lvGroups.setAdapter(adapter);

        btnAddGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // this line adds the data of your EditText and puts in your array
                    String newGroup = etNewGroupInput.getText().toString();

                    // Check a group was entered
                    if (newGroup == null){
                        Util.getToast(getApplicationContext(), "Please enter a group name." , true).show();
                        return;
                    }
                    // Check group name is at least 4 characters long
                    if (newGroup.length() < 4){
                        Util.getToast(getApplicationContext(), "Please enter a group name of at least 4 characters." , true).show();
                        return;
                    }

                    // Check if this group already exists
                    if(arrayListGroups.contains(newGroup)){
                        Util.getToast(getApplicationContext(), "Sorry, can NOT add " + newGroup + " as it already exists." , true).show();
                        return;
                    }

                    Util.addGroupToServer(getApplicationContext(), newGroup);
                    adapter.add(newGroup);
                    ((EditText) findViewById(R.id.etNewGroupInput)).setText("");

                } catch (Exception ex) {
                    Log.e(TAG, ex.getLocalizedMessage());
                }
            }
        });

        lvGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String group = lvGroups.getItemAtPosition(i).toString();
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                intent.putExtra("GROUP", group);
                startActivity(intent);

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Prefs prefs = new Prefs(getApplicationContext());

        //Populate group list
        arrayListGroups = Util.getGroups(this);
        adapter.notifyDataSetChanged();

        IntentFilter iff = new IntentFilter("SERVER_GROUPS");
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
    }

    public void back(@SuppressWarnings("UnusedParameters") View v) {
        try {
            finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    private final BroadcastReceiver onNotice = new BroadcastReceiver() {
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

       @Override
        public void onReceive(Context context, Intent intent) {
            Prefs prefs = new Prefs(getApplicationContext());
            try {
                String jsonStringMessage = intent.getStringExtra("jsonStringMessage");

                if (jsonStringMessage != null) {


                    JSONObject joMessage = new JSONObject(jsonStringMessage);
                    String messageType = joMessage.getString("messageType");
                    String messageToDisplay = joMessage.getString("messageToDisplay");

                    // do something
                    // update the list of groups from server
                    if (messageType.equalsIgnoreCase("SUCCESSFULLY_ADDED_GROUP")) {
                        Util.getToast(getApplicationContext(), messageToDisplay, false).show();
                        // Don't need to update list view of groups
                    } else {
                        Util.getToast(getApplicationContext(), messageToDisplay, true).show();
                        // Need to update list view of groups as it shouldn't have the group that the user was trying to add
                        //Populate group list
                        arrayListGroups = Util.getGroups(getApplicationContext());
                        adapter.notifyDataSetChanged();
                    }

                }



            } catch (Exception ex) {

                Log.e(TAG, ex.getLocalizedMessage());
            }
        }
    };
}
