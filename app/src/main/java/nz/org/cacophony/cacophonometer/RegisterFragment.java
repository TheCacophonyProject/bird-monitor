package nz.org.cacophony.cacophonometer;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
    private TextView tvMessages;
    private EditText etGroupNameInput;
    private EditText etDeviceNameInput;
    private TextView tvTitleMessage;
    private TextView tvGroupName;
    private TextView tvDeviceName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_register, container, false);

        setUserVisibleHint(false);

        tvMessages = (TextView) view.findViewById(R.id.tvMessages);
        etGroupNameInput =  view.findViewById(R.id.etGroupNameInput);
        etDeviceNameInput =  view.findViewById(R.id.etDeviceNameInput);

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

        tvTitleMessage = (TextView) view.findViewById(R.id.tvTitleMessage);
       tvGroupName = (TextView) view.findViewById(R.id.tvGroupName);
        tvDeviceName = (TextView) view.findViewById(R.id.tvDeviceName);

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
            if (group != null){
                etGroupNameInput.setText(group);
            }

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
//                            ((SetupWizardActivity) getActivity()).addLastPages();
//                            ((SetupWizardActivity) getActivity()).setNumberOfPagesForRegisterd();

                            tvMessages.setText(messageToDisplay);

                            try {
                                displayOrHideGUIObjects();

                            } catch (Exception ex) {
                                Log.e(TAG, ex.getLocalizedMessage());
                            }

                        } else  {

                            tvMessages.setText(messageToDisplay);
                        }
                    }
                }

            } catch (Exception ex) {
                Log.e(TAG, ex.getLocalizedMessage());

                tvMessages.setText("Oops, your phone did not register - not sure why");
            }
        }
    };

    void displayOrHideGUIObjects() {
        Prefs prefs = new Prefs(getActivity().getApplicationContext());
        String groupName = prefs.getGroupName();
        String deviceName = prefs.getDeviceName();
        if (groupName != null && deviceName != null) {
            ((SetupWizardActivity) getActivity()).setNumberOfPagesForRegisterd();
            // Phone is registered
            // Phone is NOT registered
            //Input fields to be INVISIBLE
            etGroupNameInput.setVisibility(View.INVISIBLE);
            etDeviceNameInput.setVisibility(View.INVISIBLE);

            //Set appropriate messages

            tvTitleMessage.setText(getString(R.string.register_title_registered));
            tvGroupName.setText(getString(R.string.group_name_registered) + prefs.getGroupName());
            tvDeviceName.setText(getString(R.string.device_name_registered) + prefs.getDeviceName());

            //Only unregister button is visible
            btnRegister.setVisibility(View.INVISIBLE);
            btnUnRegister.setVisibility(View.VISIBLE);


        } else {
            // Phone is NOT registered
            //Input fields to be visible
            ((SetupWizardActivity) getActivity()).setNumberOfPagesForSignedInNotRegistered();

            etGroupNameInput.setVisibility(View.VISIBLE);
            etDeviceNameInput.setVisibility(View.VISIBLE);

            //Set appropriate messages

            tvTitleMessage.setText(getString(R.string.register_title_unregistered));
            tvGroupName.setText(getString(R.string.group_name_unregistered));
            tvDeviceName.setText(getString(R.string.device_name_unregistered));

            //Only register button is visible
            btnRegister.setVisibility(View.VISIBLE);
            btnUnRegister.setVisibility(View.INVISIBLE);

            //Nudge user to Enter Group Name box
            tvGroupName.requestFocus();

        }


        if (prefs.getGroupName() != null) {
            tvGroupName.setText("Group - " + prefs.getGroupName());
        }
        if (prefs.getDeviceName() != null) {
            tvDeviceName.setText("Device Name - " + prefs.getDeviceName());

        }
    }

    public void registerButtonPressed() {
        //  registerIdlingResource.increment();

        Prefs prefs = new Prefs(getActivity().getApplicationContext());

        if (prefs.getInternetConnectionMode().equalsIgnoreCase("offline")) {
            tvMessages.setText("The internet connection (in Advanced) has been set 'offline' - so this device can not be registered");
            return;
        }

        if (!Util.isNetworkConnected(getActivity().getApplicationContext())) {
            tvMessages.setText("The phone is not currently connected to the internet - please fix and try again");
            return;
        }

        if (prefs.getGroupName() != null) {
            tvMessages.setText("Already registered - press UNREGISTER first (if you really want to change group)");
            return;
        }
        // Check that the group name is valid, at least 4 characters.
        String group = etGroupNameInput.getText().toString();
        if (group.length() < 1) {
            tvMessages.setText("Please enter a group name of at least 4 characters (no spaces)");
            return;
        } else if (group.length() < 4) {
            tvMessages.setText(group + " is not a valid group name. Please use at least 4 characters (no spaces)");
            return;
        }

        // Check that the device name is valid, at least 4 characters.
        String deviceName = ((EditText)  getView().findViewById(R.id.etDeviceNameInput)).getText().toString();
        if (deviceName.length() < 1) {
            tvMessages.setText("Please enter a device name of at least 4 characters (no spaces)");
            return;
        } else if (deviceName.length() < 4) {
            tvMessages.setText(deviceName + " is not a valid device name. Please use at least 4 characters (no spaces)");
            return;
        }

        tvMessages.setText("Attempting to register with server - please wait");

        // https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null){
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }

        String groupName = prefs.getGroupName();
        if (groupName != null && groupName.equals(group)) {
            // Try to login with username and password.
            tvMessages.setText("Already registered with that group");
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
            }
        };
        registerThread.start();
    }


    public void unregisterButtonPressed() {
        Prefs prefs = new Prefs(getActivity().getApplicationContext());
        if (prefs.getGroupName() == null) {
            tvMessages.setText("Not currently registered - so can not unregister :-(");
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

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btnPositive = dialog.getButton(Dialog.BUTTON_POSITIVE);
                btnPositive.setTextSize(24);
                int btnPositiveColor = ResourcesCompat.getColor(getActivity().getResources(), R.color.dialogButtonText, null);
                btnPositive.setTextColor(btnPositiveColor);

                Button btnNegative = dialog.getButton(Dialog.BUTTON_NEGATIVE);
                btnNegative.setTextSize(24);
                int btnNegativeColor = ResourcesCompat.getColor(getActivity().getResources(), R.color.dialogButtonText, null);
                btnNegative.setTextColor(btnNegativeColor);

                //https://stackoverflow.com/questions/6562924/changing-font-size-into-an-alertdialog
                TextView textView = (TextView) dialog.findViewById(android.R.id.message);
                textView.setTextSize(22);
            }
        });

        dialog.show();


    }

    private void unregister() {

        try {
            Util.unregisterPhone(getActivity().getApplicationContext());
            ((SetupWizardActivity) getActivity()).setNumberOfPagesForSignedInNotRegistered();
            tvMessages.setText("Success - Device is no longer registered");
            etGroupNameInput.setText("");
            etDeviceNameInput.setText("");

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
            tvMessages.setText("Error disabling flight mode");
        }
    }


}
