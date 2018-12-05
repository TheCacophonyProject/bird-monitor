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

import org.json.JSONObject;

public class SignInFragment extends Fragment {
    private static final String TAG = "SignInFragment";

    private Button btnSignIn;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_sign_in, container, false);
        setUserVisibleHint(false);
        btnSignIn = (Button) view.findViewById(R.id.btnSignIn);


        btnSignIn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                // https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                if (imm != null){
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                }

                signinButton();
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
            Util.getToast(getActivity().getApplicationContext(), "Error disabling flight mode", true).show();
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

            Prefs prefs = new Prefs(getActivity().getApplicationContext());

            String username = prefs.getUsername();
            String emailAddress = prefs.getEmailAddress();
            String usernameOrEmailAddress = prefs.getUserNameOrEmailAddress();
            if (usernameOrEmailAddress !=null){
                ((EditText)getView().findViewById(R.id.etUserNameOrPasswordInput)).setText(usernameOrEmailAddress);
            }else if (username != null){
                ((EditText)getView().findViewById(R.id.etUserNameOrPasswordInput)).setText(username);
            }else if(emailAddress != null){
                ((EditText)getView().findViewById(R.id.etUserNameOrPasswordInput)).setText(emailAddress);
            }
            String password = prefs.getUsernamePassword();
            if (password != null){
                ((EditText)getView().findViewById(R.id.etPasswordInput)).setText(password);
            }

        }else{

            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onNotice);
        }
    }

    private final BroadcastReceiver onNotice = new BroadcastReceiver() {
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

        // broadcast notification coming from ??
        @Override
        public void onReceive(Context context, Intent intent) {

            // Prefs prefs = new Prefs(getApplicationContext());
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
                        Util.getToast(getActivity().getApplicationContext(),messageToDisplay, false ).show();
                        Util.getGroupsFromServer(getActivity().getApplicationContext());
                        ((SetupWizardActivity) getActivity()).nextPageView();
                    } else{
                        Util.getToast(getActivity().getApplicationContext(),messageToDisplay, true ).show();
                        return;
                    }

                }

            } catch (Exception ex) {
                Log.e(TAG, ex.getLocalizedMessage());
                Util.getToast(getActivity().getApplicationContext(),"Could not login", true ).show();
            }
        }
    };
    public void signinButton() {
        try {
            Prefs prefs = new Prefs(getActivity().getApplicationContext());

            if (prefs.getInternetConnectionMode().equalsIgnoreCase("offline")){
                Util.getToast(getActivity().getApplicationContext(),"The internet connection (in Advanced) has been set 'offline' - so this device can not be registered", true ).show();
                return;
            }

            if (!Util.isNetworkConnected(getActivity().getApplicationContext())){
                Util.getToast(getActivity().getApplicationContext(),"The phone is not currently connected to the internet - please fix and try again", true ).show();
                return;
            }

            // Check that the user name is valid, at least 5 characters.
            String usernameOrEmailAddress = ((EditText) getView().findViewById(R.id.etUserNameOrPasswordInput)).getText().toString();
            if (usernameOrEmailAddress.length() < 1){
                Util.getToast(getActivity().getApplicationContext(),"Please enter a username of at least 5 characters (no spaces)", true ).show();
                return;
            }else if (usernameOrEmailAddress.length() < 5 && !usernameOrEmailAddress.contains("@")) {
                Log.i(TAG, "Invalid usernameOrEmailAddress: "+usernameOrEmailAddress);

                Util.getToast(getActivity().getApplicationContext(),usernameOrEmailAddress + " is not a valid username or email address.", true ).show();
                return;
            }

            // Check that the password is valid, at least 8 characters.
            String password = ((EditText) getView().findViewById(R.id.etPasswordInput)).getText().toString();
            if (password.length() < 1){
                Util.getToast(getActivity().getApplicationContext(),"Please enter a passord of at least 8 characters (no spaces)", true ).show();
                return;
            }else if (password.length() < 5) {
                Log.i(TAG, "Invalid password");

                Util.getToast(getActivity().getApplicationContext(), "Please use at least 8 characters (no spaces)", true ).show();
                return;
            }

//            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//            if (imm == null){
//                Log.e(TAG, "imm is null");
//                return;
//            }
//
//            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

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

        Util.getToast(getActivity().getApplicationContext(),"Attempting to sign into the server - please wait", false ).show();

    }

}
