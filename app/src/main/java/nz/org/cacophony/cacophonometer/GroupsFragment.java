package nz.org.cacophony.cacophonometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONObject;

import java.util.ArrayList;

public class GroupsFragment extends Fragment {
    private static final String TAG = "GroupsFragment";
    private Button btnBack;
    private EditText etNewGroupInput;
    private Button btnAddGroup;
    private ListView lvGroups;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayListGroups;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_groups, container, false);
        setUserVisibleHint(false);

        btnBack = (Button) view.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ((SetupWizardActivity)getActivity()).previousPageView();
            }
        });

        etNewGroupInput = (EditText) view.findViewById(R.id.etNewGroupInput);
        btnAddGroup = (Button) view.findViewById(R.id.btnAddGroup);
        lvGroups = (ListView) view.findViewById(R.id.lvGroups);
        arrayListGroups = Util.getGroups(getActivity());

        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, arrayListGroups);
        lvGroups.setAdapter(adapter);

        btnAddGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // this line adds the data of your EditText and puts in your array
                    String newGroup = etNewGroupInput.getText().toString();

                    // Check a group was entered
                    if (newGroup == null){
                        Util.getToast(getActivity(), "Please enter a group name." , true).show();
                        return;
                    }
                    // Check group name is at least 4 characters long
                    if (newGroup.length() < 4){
                        Util.getToast(getActivity(), "Please enter a group name of at least 4 characters." , true).show();
                        return;
                    }

                    // Check if this group already exists
                    if(arrayListGroups.contains(newGroup)){
                        Util.getToast(getActivity(), "Sorry, can NOT add " + newGroup + " as it already exists." , true).show();
                        return;
                    }

                    Util.addGroupToServer(getActivity(), newGroup);
                    adapter.add(newGroup);
                    ((EditText) view.findViewById(R.id.etNewGroupInput)).setText("");
                    ((SetupWizardActivity) getActivity()).setGroup(newGroup);

                } catch (Exception ex) {
                    Log.e(TAG, ex.getLocalizedMessage());
                }
            }
        });

        lvGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String group = lvGroups.getItemAtPosition(i).toString();
                ((SetupWizardActivity) getActivity()).setGroup(group);
                ((SetupWizardActivity) getActivity()).nextPageView();


            }
        });


        return view;
    }

    @Override
    public void setUserVisibleHint(final boolean visible) {
        super.setUserVisibleHint(visible);
        if (getActivity() == null){
            return;
        }
        if (visible) {

            //Populate group list
            arrayListGroups = Util.getGroups(getActivity());
            adapter.notifyDataSetChanged();

            IntentFilter iff = new IntentFilter("SERVER_GROUPS");
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onNotice, iff);

        }else{
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onNotice);
        }
    }

    private final BroadcastReceiver onNotice = new BroadcastReceiver() {
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

        @Override
        public void onReceive(Context context, Intent intent) {
           
            try {
                String jsonStringMessage = intent.getStringExtra("jsonStringMessage");

                if (jsonStringMessage != null) {

                    JSONObject joMessage = new JSONObject(jsonStringMessage);
                    String messageType = joMessage.getString("messageType");
                    String messageToDisplay = joMessage.getString("messageToDisplay");

                    // update the list of groups from server
                    if (messageType.equalsIgnoreCase("SUCCESSFULLY_ADDED_GROUP")) {
                        Util.getToast(getActivity(), messageToDisplay, false).show();
                        ((EditText) getView().findViewById(R.id.etNewGroupInput)).setText("");
                        ((SetupWizardActivity) getActivity()).nextPageView();

                    } else {
                        Util.getToast(getActivity(), messageToDisplay, true).show();
                        // Need to update list view of groups as it shouldn't have the group that the user was trying to add
                        //Populate group list
                        arrayListGroups = Util.getGroups(getActivity());
                        adapter.notifyDataSetChanged();
                    }

                }



            } catch (Exception ex) {

                Log.e(TAG, ex.getLocalizedMessage());
            }
        }
    };

}
