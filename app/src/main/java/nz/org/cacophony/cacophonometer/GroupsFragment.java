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
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;

public class GroupsFragment extends Fragment {
    private static final String TAG = "GroupsFragment";

    private EditText etNewGroupInput;
    private Button btnCreateGroup;
    private ListView lvGroups;
    private ArrayAdapter<String> adapter;
    private TextView tvMessages;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_groups, container, false);
        setUserVisibleHint(false);

        etNewGroupInput = (EditText) view.findViewById(R.id.etNewGroupInput);
        btnCreateGroup = (Button) view.findViewById(R.id.btnCreateGroup);
        lvGroups = (ListView) view.findViewById(R.id.lvGroups);
      //  arrayListGroups = Util.getGroupsStoredOnPhone(getActivity());

        //adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, arrayListGroups);
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, Util.getGroupsStoredOnPhone(getActivity()));
        lvGroups.setAdapter(adapter);

        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // this line adds the data of your EditText and puts in your array
                    String newGroup = etNewGroupInput.getText().toString();

                    // Check a group was entered
                    if (newGroup == null){
                       // Util.getToast(getActivity(), "Please enter a group name." , true).show();
                        tvMessages.setText("Please enter a group name.");
                        return;
                    }
                    // Check group name is at least 4 characters long
                    if (newGroup.length() < 4){
                      //  Util.getToast(getActivity(), "Please enter a group name of at least 4 characters." , true).show();
                        tvMessages.setText("Please enter a group name of at least 4 characters.");
                        return;
                    }

                    // Check if this group already exists
                   // if(arrayListGroups.contains(newGroup)){
                        if(Util.getGroupsStoredOnPhone(getActivity()).contains(newGroup)){
                      //  Util.getToast(getActivity(), "Sorry, can NOT add " + newGroup + " as it already exists." , true).show();
                            tvMessages.setText("Sorry, can NOT add \" + newGroup + \" as it already exists.");
                        return;
                    }
                    ((SetupWizardActivity) getActivity()).setGroup(newGroup);
                    Util.addGroupToServer(getActivity(), newGroup);
                    adapter.add(newGroup);
                    ((EditText) view.findViewById(R.id.etNewGroupInput)).setText("");
                   // ((SetupWizardActivity) getActivity()).setGroup(newGroup);

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
            adapter.clear();
            adapter.addAll(Util.getGroupsStoredOnPhone(getActivity()));
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

                if (getView() == null) {
                    return;
                }
                String jsonStringMessage = intent.getStringExtra("jsonStringMessage");

                if (jsonStringMessage != null) {

                    JSONObject joMessage = new JSONObject(jsonStringMessage);
                    String messageType = joMessage.getString("messageType");
                    String messageToDisplay = joMessage.getString("messageToDisplay");


                    if (messageType.equalsIgnoreCase("SUCCESSFULLY_ADDED_GROUP")) {
                        // update the list of groups from server
                     //   Util.getToast(getActivity(), messageToDisplay, false).show();
                        tvMessages.setText(messageToDisplay);
                        ((EditText) getView().findViewById(R.id.etNewGroupInput)).setText("");
                        // Need to note this group for Register Phone screen
                        ((SetupWizardActivity) getActivity()).nextPageView();

                    } else if(messageType.equalsIgnoreCase("FAILED_TO_ADD_GROUP")) {
                       // Util.getToast(getActivity(), messageToDisplay, true).show();
                        tvMessages.setText(messageToDisplay);

                        ((SetupWizardActivity) getActivity()).setGroup(null);

                        adapter.addAll(Util.getGroupsStoredOnPhone(getActivity()));
                        adapter.notifyDataSetChanged();
                    }else if (messageType.equalsIgnoreCase("SUCCESSFULLY_RETRIEVED_GROUPS")) {

                      adapter.clear();
                        //https://stackoverflow.com/questions/14503006/android-listview-not-refreshing-after-notifydatasetchanged
                        adapter.addAll(Util.getGroupsStoredOnPhone(getActivity()));
                        adapter.notifyDataSetChanged();

                    }else if (messageType.equalsIgnoreCase("FAILED_TO_RETRIEVE_GROUPS")) {
                       // Util.getToast(getActivity(), messageToDisplay, true).show();
                        tvMessages.setText(messageToDisplay);
                }

                }



            } catch (Exception ex) {

                Log.e(TAG, ex.getLocalizedMessage());
            }
        }
    };

}
