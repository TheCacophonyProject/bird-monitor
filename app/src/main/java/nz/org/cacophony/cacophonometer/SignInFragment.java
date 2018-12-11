package nz.org.cacophony.cacophonometer;

import android.app.Activity;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

public class SignInFragment extends Fragment {
    private static final String TAG = "SignInFragment";

    private Button btnSignIn;
    private Button btnForgetUser;
    private TextView tvMessages;
    private EditText etUserNameOrPasswordInput;
    private EditText etPasswordInput;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_sign_in, container, false);
        setUserVisibleHint(false);
        etUserNameOrPasswordInput = (EditText)view.findViewById(R.id.etUserNameOrPasswordInput);
        etPasswordInput = (EditText)view.findViewById(R.id.etPasswordInput);
        btnSignIn = (Button) view.findViewById(R.id.btnSignIn);
        btnForgetUser = (Button) view.findViewById(R.id.btnForgetUser);
        tvMessages = (TextView) view.findViewById(R.id.tvMessages);

        btnSignIn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                // https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                if (imm != null){
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                }

                signinButtonPressed();
            }
        });

        btnForgetUser.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                // https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                if (imm != null){
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                }

                forgetUserButtonPressed();
            }
        });

         return view;
    }

    private void login(){
        disableFlightMode();

        // Now wait for network connection as setFlightMode takes a while
        if (!Util.waitForNetworkConnection(getActivity().getApplicationContext(), true)){
            Log.e(TAG, "Failed to disable airplane mode");
            return ;
        }

        Thread loginThread = new Thread() {
            @Override
            public void run() {
                Server.loginUser(getActivity().getApplicationContext());;
            }
        };
        loginThread.start();
    }

    private void disableFlightMode(){
        try {
            Util.disableFlightMode(getActivity().getApplicationContext());


        }catch (Exception ex){
            Log.e(TAG, ex.getLocalizedMessage());

            tvMessages.setText("Error disabling flight mode");
        }
    }

    @Override
    public void setUserVisibleHint(final boolean visible) {
        super.setUserVisibleHint(visible);
        if (getActivity() == null){
            return;
        }
        if (visible) {

            IntentFilter iff = new IntentFilter("SERVER_USER_LOGIN");
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onNotice, iff);

            displayOrHideGUIObjects();

        }else{

            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onNotice);
        }
    }

    void displayOrHideGUIObjects(){

        Prefs prefs = new Prefs(getActivity().getApplicationContext());
        if (prefs.getUsername() == null && prefs.getUserNameOrEmailAddress() == null){
           // Show normal signin page
            btnSignIn.setEnabled(true);
            btnForgetUser.setEnabled(false);
            etUserNameOrPasswordInput.setText("");
            etPasswordInput.setText("");

        }else{

            if (prefs.getUserSignedIn()){
                String userNameOrEmailAddress = "";
                if (prefs.getUserNameOrEmailAddress()!= null){
                    userNameOrEmailAddress = prefs.getUserNameOrEmailAddress();
                }else if (prefs.getUsername()!= null){
                    userNameOrEmailAddress = prefs.getUsername();
                }
                tvMessages.setText("You are signed in as " + userNameOrEmailAddress + "\n\n \'Swipe\' to the next step.");
                btnSignIn.setEnabled(false);
                btnForgetUser.setEnabled(true);
                etUserNameOrPasswordInput.setEnabled(false);
                etPasswordInput.setEnabled(false);
            }else{
                // try to signin
                // next if on broadcast receiver
                tvMessages.setText("Signing in to your account");
                btnSignIn.setEnabled(true);
                btnForgetUser.setEnabled(true);
                login();
            }


        }
    }


    private final BroadcastReceiver onNotice = new BroadcastReceiver() {
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

        // broadcast notification coming from ??
        @Override
        public void onReceive(Context context, Intent intent) {

             Prefs prefs = new Prefs(context);

            String userNameOrEmailAddress = "";
            if (prefs.getUserNameOrEmailAddress()!= null){
                userNameOrEmailAddress = prefs.getUserNameOrEmailAddress();
            }else if (prefs.getUsername()!= null){
                userNameOrEmailAddress = prefs.getUsername();
            }
            try {
                if (getView() == null) {
                    return;
                }


                String jsonStringMessage = intent.getStringExtra("jsonStringMessage");

                if (jsonStringMessage != null) {


                    JSONObject joMessage = new JSONObject(jsonStringMessage);
                    String messageType = joMessage.getString("messageType");
                    String messageToDisplay = joMessage.getString("messageToDisplay");


                    if (messageType.equalsIgnoreCase("SUCCESSFULLY_SIGNED_IN")){

                        prefs.setUserSignedIn(true);

                        tvMessages.setText(messageToDisplay + " as " + userNameOrEmailAddress + "\n\n \'Swipe\' to the next step.");
                        btnSignIn.setEnabled(false);
                        btnForgetUser.setEnabled(true);
                        etUserNameOrPasswordInput.setEnabled(false);
                        etPasswordInput.setEnabled(false);
                        Util.getGroupsFromServer(getActivity().getApplicationContext());
                       // ((SetupWizardActivity) getActivity()).nextPageView();
                    } else  if (messageType.equalsIgnoreCase("NETWORK_ERROR")){
                       // Util.getToast(getActivity().getApplicationContext(),messageToDisplay, true ).show();
                        tvMessages.setText(messageToDisplay);
                        etUserNameOrPasswordInput.setText(userNameOrEmailAddress);
                        return;
                    }else  if (messageType.equalsIgnoreCase("INVALID_CREDENTIALS")){
                            // Util.getToast(getActivity().getApplicationContext(),messageToDisplay, true ).show();
                            tvMessages.setText(messageToDisplay);
                            etUserNameOrPasswordInput.setText(userNameOrEmailAddress);
                            return;
                    }else  if (messageType.equalsIgnoreCase("UNABLE_TO_SIGNIN")){

                    tvMessages.setText(messageToDisplay);
                    etUserNameOrPasswordInput.setText(userNameOrEmailAddress);
                    return;
                }

                }

            } catch (Exception ex) {
                Log.e(TAG, ex.getLocalizedMessage());

                tvMessages.setText("Could not login.");
            }
        }
    };
    public void signinButtonPressed() {
        try {
            Prefs prefs = new Prefs(getActivity().getApplicationContext());

            if (prefs.getInternetConnectionMode().equalsIgnoreCase("offline")){
             tvMessages.setText("The internet connection (in Advanced) has been set 'offline' - so this device can not be registered");
                return;
            }

            if (!Util.isNetworkConnected(getActivity().getApplicationContext())){
                tvMessages.setText("The phone is not currently connected to the internet - please fix and try again");
                return;
            }

            // Check that the user name is valid, at least 5 characters.
            String usernameOrEmailAddress = ((EditText) getView().findViewById(R.id.etUserNameOrPasswordInput)).getText().toString();
            if (usernameOrEmailAddress.length() < 1){
                tvMessages.setText("Please enter a username of at least 5 characters (no spaces)");
                return;
            }else if (usernameOrEmailAddress.length() < 5 && !usernameOrEmailAddress.contains("@")) {
                Log.i(TAG, "Invalid usernameOrEmailAddress: "+usernameOrEmailAddress);

                tvMessages.setText(usernameOrEmailAddress + " is not a valid username or email address.");
                return;
            }

            // Check that the password is valid, at least 8 characters.
            String password = ((EditText) getView().findViewById(R.id.etPasswordInput)).getText().toString();
            if (password.length() < 1){
               tvMessages.setText("Please enter a password of at least 8 characters (no spaces)");
                return;
            }else if (password.length() < 5) {
                tvMessages.setText("Please use at least 8 characters (no spaces)");
                return;
            }

            String userNameFromPrefs = prefs.getUsername();
            if (userNameFromPrefs == null){
                userNameFromPrefs = "";
            }

            String emailAddressFromPrefs = prefs.getEmailAddress();
            if (emailAddressFromPrefs == null){
                emailAddressFromPrefs = "";
            }

            if (!usernameOrEmailAddress.equalsIgnoreCase(userNameFromPrefs) && !usernameOrEmailAddress.equalsIgnoreCase(emailAddressFromPrefs)){
                prefs.setUsername(null);
                prefs.setEmailAddress(null);
            }
            prefs.setUserNameOrEmailAddress(usernameOrEmailAddress);
            prefs.setUsernamePassword(password);

            login();


        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }

        tvMessages.setText("Attempting to sign into the server - please wait");
    }

    public void forgetUserButtonPressed() {
        Prefs prefs = new Prefs(getActivity().getApplicationContext());
        prefs.setUsername(null);
        Util.unregisterUser(getActivity().getApplicationContext());
        tvMessages.setText("The user details have been removed from this phone");
        btnSignIn.setEnabled(true);
        btnForgetUser.setEnabled(false);

        etUserNameOrPasswordInput.setEnabled(true);
        etPasswordInput.setEnabled(true);

        prefs.setUserSignedIn(false);

        displayOrHideGUIObjects();
    }



}
