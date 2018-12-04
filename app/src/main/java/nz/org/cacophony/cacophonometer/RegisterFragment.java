package nz.org.cacophony.cacophonometer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

public class RegisterFragment extends Fragment {
    private static final String TAG = "RegisterFragment";


    private Button btnRegister;
    private Button btnUnRegister;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_register, container, false);

        setUserVisibleHint(false);



        btnRegister = (Button) view.findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
               registerButtonPressed();
            }
        });

        btnUnRegister = (Button) view.findViewById(R.id.btnUnRegister);
        btnUnRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                unregisterButtonPressed();
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

            IntentFilter iff = new IntentFilter("SERVER_REGISTER");
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onNotice, iff);

            String group = ((SetupWizardActivity) getActivity()).getGroup();
            ((EditText) getView().findViewById(R.id.setupGroupNameInput)).setText(group);

            displayOrHideGUIObjects();

        }else{

            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onNotice);
        }
    }

    private final BroadcastReceiver onNotice = new BroadcastReceiver() {
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

        // broadcast notification coming from ??
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

                    if (messageType != null) {


                        if (messageType.equalsIgnoreCase("REGISTER_SUCCESS")) {
                            Util.getToast(getActivity().getApplicationContext(), messageToDisplay, false).show();
                            //registerIdlingResource.decrement();
                            try {
                                displayOrHideGUIObjects();

                            } catch (Exception ex) {
                                Log.e(TAG, ex.getLocalizedMessage());
                            }

                        } else  {
                            Util.getToast(getActivity().getApplicationContext(), messageToDisplay, true).show();
                        }
                    }
                }

            } catch (Exception ex) {
                Log.e(TAG, ex.getLocalizedMessage());
                Util.getToast(getActivity().getApplicationContext(), "Oops, your phone did not register - not sure why", true).show();
            }
        }
    };

    void displayOrHideGUIObjects() {
        Prefs prefs = new Prefs(getActivity().getApplicationContext());
        if (prefs.getGroupName() != null && prefs.getDeviceName() != null) {
            // Phone is registered
            // Phone is NOT registered
            //Input fields to be INVISIBLE
            getView().findViewById(R.id.setupGroupNameInput).setVisibility(View.INVISIBLE);
            getView().findViewById(R.id.setupDeviceNameInput).setVisibility(View.INVISIBLE);

            //Set appropriate messages

            ((TextView) getView().findViewById(R.id.tvTitleMessage)).setText(getString(R.string.register_title_registered));
            ((TextView) getView().findViewById(R.id.tvGroupName)).setText(getString(R.string.group_name_registered) + prefs.getGroupName());
            ((TextView) getView().findViewById(R.id.tvDeviceName)).setText(getString(R.string.device_name_registered) + prefs.getDeviceName());

            //Only unregister button is visible
            getView().findViewById(R.id.btnRegister).setVisibility(View.INVISIBLE);
            getView().findViewById(R.id.btnUnRegister).setVisibility(View.VISIBLE);


        } else {
            // Phone is NOT registered
            //Input fields to be visible
            getView().findViewById(R.id.setupGroupNameInput).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.setupDeviceNameInput).setVisibility(View.VISIBLE);

            //Set appropriate messages

            ((TextView) getView().findViewById(R.id.tvTitleMessage)).setText(getString(R.string.register_title_unregistered));
            ((TextView) getView().findViewById(R.id.tvGroupName)).setText(getString(R.string.group_name_unregistered));
            ((TextView) getView().findViewById(R.id.tvDeviceName)).setText(getString(R.string.device_name_unregistered));

            //Only register button is visible
            getView().findViewById(R.id.btnRegister).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.btnUnRegister).setVisibility(View.INVISIBLE);

            //Nudge user to Enter Group Name box
            getView().findViewById(R.id.tvGroupName).requestFocus();

        }


        if (prefs.getGroupName() != null) {
            ((TextView) getView().findViewById(R.id.tvGroupName)).setText("Group - " + prefs.getGroupName());
        }
        if (prefs.getDeviceName() != null) {
            ((TextView) getView().findViewById(R.id.tvDeviceName)).setText("Device Name - " + prefs.getDeviceName());

        }
    }

    public void registerButtonPressed() {
        //  registerIdlingResource.increment();

        Prefs prefs = new Prefs(getActivity().getApplicationContext());

        // if (prefs.getOffLineMode()){
        if (prefs.getInternetConnectionMode().equalsIgnoreCase("offline")) {
            Util.getToast(getActivity().getApplicationContext(), "The internet connection (in Advanced) has been set 'offline' - so this device can not be registered", true).show();
            return;
        }

        if (!Util.isNetworkConnected(getActivity().getApplicationContext())) {
            Util.getToast(getActivity().getApplicationContext(), "The phone is not currently connected to the internet - please fix and try again", true).show();
            return;
        }

        if (prefs.getGroupName() != null) {
            Util.getToast(getActivity().getApplicationContext(), "Already registered - press UNREGISTER first (if you really want to change group)", true).show();
            return;
        }
        // Check that the group name is valid, at least 4 characters.
        String group = ((EditText)  getView().findViewById(R.id.setupGroupNameInput)).getText().toString();
        if (group.length() < 1) {
            Util.getToast(getActivity().getApplicationContext(), "Please enter a group name of at least 4 characters (no spaces)", true).show();
            return;
        } else if (group.length() < 4) {
            Log.i(TAG, "Invalid group name: " + group);

            Util.getToast(getActivity().getApplicationContext(), group + " is not a valid group name. Please use at least 4 characters (no spaces)", true).show();
            return;
        }

        // Check that the device name is valid, at least 4 characters.
        String deviceName = ((EditText)  getView().findViewById(R.id.setupDeviceNameInput)).getText().toString();
        if (deviceName.length() < 1) {
            Util.getToast(getActivity().getApplicationContext(), "Please enter a device name of at least 4 characters (no spaces)", true).show();
            return;
        } else if (deviceName.length() < 4) {
            Log.i(TAG, "Invalid device name: " + deviceName);

            Util.getToast(getActivity().getApplicationContext(), deviceName + " is not a valid device name. Please use at least 4 characters (no spaces)", true).show();
            return;
        }

        Util.getToast(getActivity().getApplicationContext(), "Attempting to register with server - please wait", false).show();

        // https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null){
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }

        String groupName = prefs.getGroupName();
        if (groupName != null && groupName.equals(group)) {
            // Try to login with username and password.
            Util.getToast(getActivity().getApplicationContext(), "Already registered with that group", true).show();
            return;
        }

        register(group, deviceName, getActivity().getApplicationContext());

    }

    /**
     * Will register the device in the given group saving the JSON Web Token, devicename, and password.
     *
     * @param group name of group to join.
     */
    private void register(final String group, final String deviceName, final Context context) {
        // Check that the group name is valid, at least 4 characters.
        if (group == null || group.length() < 4) {

            Log.e(TAG, "Invalid group name - this should have already been picked up");
            return;
        }

        disableFlightMode();

        // Now wait for network connection as setFlightMode takes a while
        if (!Util.waitForNetworkConnection(getActivity().getApplicationContext(), true)) {
            Log.e(TAG, "Failed to disable airplane mode");
            return;
        }

        Thread registerThread = new Thread() {
            @Override
            public void run() {
                Server.register(group, deviceName, context);
                ;
            }
        };
        registerThread.start();
    }


    public void unregisterButtonPressed() {
        Prefs prefs = new Prefs(getActivity().getApplicationContext());
        if (prefs.getGroupName() == null) {
            Util.getToast(getActivity().getApplicationContext(), "Not currently registered - so can not unregister :-(", true).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Add the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                unregister();
            }
        });
        builder.setNegativeButton("No/Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                return;
            }
        });
        builder.setMessage("Are you sure?")
                .setTitle("Un-register this phone");
        AlertDialog dialog = builder.create();
        dialog.show();


    }

    private void unregister() {


        try {

            Prefs prefs = new Prefs(getActivity().getApplicationContext());
            prefs.setGroupName(null);
            prefs.setDevicePassword(null);
            prefs.setDeviceName(null);
            prefs.setDeviceToken(null);

            Util.getToast(getActivity().getApplicationContext(), "Success - Device is no longer registered", false).show();
            displayOrHideGUIObjects();

        } catch (Exception ex) {
            Log.e(TAG, "Error Un-registering device.");
        }

    }

    private void disableFlightMode() {
        try {
            Util.disableFlightMode(getActivity().getApplicationContext());


        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
            Util.getToast(getActivity().getApplicationContext(), "Error disabling flight mode", true).show();
        }
    }


}
