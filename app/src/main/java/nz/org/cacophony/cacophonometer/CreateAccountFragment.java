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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

public class CreateAccountFragment extends Fragment {
    private static final String TAG = "CreateAccountFragment";

    private Button btnSignUp;
    private Button btnSkip_Next;
    private Button btnForgetUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_account, container, false);

        setUserVisibleHint(false);
        btnSignUp = (Button) view.findViewById(R.id.btnSignUp);
        btnSkip_Next = (Button) view.findViewById(R.id.btnSkip_Next);
        btnForgetUser = (Button) view.findViewById(R.id.btnForgetUser);


        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUserButtonPressed();
            }
        });

        btnSkip_Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SetupWizardActivity) getActivity()).nextPageView();
            }
        });

        btnForgetUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgetUser();
            }
        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
       // Util.getToast(getActivity().getApplicationContext(),"Create Account Fragment onResume", true ).show();

        displayOrHideGUIObjects();



    }

    @Override
    public void onStart() {
        super.onStart();
     //   Util.getToast(getActivity().getApplicationContext(),"Create Account Fragment onSTART", true ).show();
    }

    @Override
    public void onPause() {
        super.onPause();
      //  Util.getToast(getActivity().getApplicationContext(),"Create Account Fragment onPAUSE", true ).show();
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

    }

    @Override
    public void setUserVisibleHint(final boolean visible) {
        super.setUserVisibleHint(visible);
        if (getActivity() == null){
            return;
        }
        if (visible) {
            //Util.getToast(getActivity().getApplicationContext(),"Create Account Fragment came visible", true ).show();

            IntentFilter iff = new IntentFilter("SERVER_SIGNUP");
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onNotice, iff);
        }else{
           // Util.getToast(getActivity().getApplicationContext(),"Create Account Fragment disappeared", true ).show();
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onNotice);
        }
    }



    void displayOrHideGUIObjects(){
        Prefs prefs = new Prefs(getActivity().getApplicationContext());

        if (prefs.getUsername() == null){
            ((TextView) getView().findViewById(R.id.tvChooseUsername)).setText("Choose a username");
            ((TextView) getView().findViewById(R.id.tvEnterEmail)).setText("Email address");
            ((TextView) getView().findViewById(R.id.tvTitle)).setText("You need to signup to the Cacophony Server");

            getView().findViewById(R.id.etUsername).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.etEmail).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.etEmail).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.tvEnterPassword).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.etPassword1).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.tvConfirmPassword).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.etPassword2).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.btnSignUp).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.btnForgetUser).setVisibility(View.INVISIBLE);
            btnSkip_Next.setText("Skip");
        }else{

            ((TextView) getView().findViewById(R.id.tvChooseUsername)).setText("Username is " + prefs.getUsername());
            ((TextView) getView().findViewById(R.id.tvEnterEmail)).setText("Email address is " + prefs.getEmailAddress());
            ((TextView) getView().findViewById(R.id.tvTitle)).setText("This phone is registered with a valid Cacophonometer account.");

            getView().findViewById(R.id.etUsername).setVisibility(View.INVISIBLE);
            getView().findViewById(R.id.etEmail).setVisibility(View.INVISIBLE);
            getView().findViewById(R.id.etEmail).setVisibility(View.INVISIBLE);
            getView().findViewById(R.id.tvEnterPassword).setVisibility(View.INVISIBLE);
            getView().findViewById(R.id.etPassword1).setVisibility(View.INVISIBLE);
            getView().findViewById(R.id.tvConfirmPassword).setVisibility(View.INVISIBLE);
            getView().findViewById(R.id.etPassword2).setVisibility(View.INVISIBLE);
            getView().findViewById(R.id.btnSignUp).setVisibility(View.INVISIBLE);
            getView().findViewById(R.id.btnForgetUser).setVisibility(View.VISIBLE);
            btnSkip_Next.setText("Next");
        }

    }

    private void forgetUser() {
        Prefs prefs = new Prefs(getActivity().getApplicationContext());
        prefs.setUsername(null);
        displayOrHideGUIObjects();
    }

    private void createUserButtonPressed(){
        Prefs prefs = new Prefs(getActivity().getApplicationContext());

        // if (prefs.getOffLineMode()){
        if (prefs.getInternetConnectionMode().equalsIgnoreCase("offline")){
            Util.getToast(getActivity().getApplicationContext(),"The internet connection (in Advanced) has been set 'offline' - so this device can not be registered", true ).show();
            return;
        }

        if (!Util.isNetworkConnected(getActivity().getApplicationContext())){
            Util.getToast(getActivity().getApplicationContext(),"The phone is not currently connected to the internet - please fix and try again", true ).show();
            return;
        }


        // Check that the username is valid, at least 5 characters.
        String username = ((EditText) getView().findViewById(R.id.etUsername)).getText().toString();
        if (username.length() < 1){
            Util.getToast(getActivity().getApplicationContext(),"Please enter a Username of at least 5 characters (no spaces)", true ).show();
            return;
        }else if (username.length() < 5) {
            Log.i(TAG, "Invalid Username: "+ username);

            Util.getToast(getActivity().getApplicationContext(),username + " is not a valid username. Please use at least 5 characters (no spaces)", true ).show();
            return;
        }

        //Check email is valid
        String emailAddress = ((EditText) getView().findViewById(R.id.etEmail)).getText().toString();

        if (emailAddress.length() < 1) {
            Util.getToast(getActivity().getApplicationContext(), "Please enter an email address", true).show();
            return;
        } else if (!Util.isValidEmail(emailAddress)){
            Util.getToast(getActivity().getApplicationContext(),emailAddress + " is not a valid email address.", true ).show();
            return;
        }

        //Check password is valid
        String etPassword1 = ((EditText) getView().findViewById(R.id.etPassword1)).getText().toString();
        if (etPassword1.length() < 8){
            Util.getToast(getActivity().getApplicationContext(),"Minimum password length is 8 characters.", true ).show();
            return;
        }
        String etPassword2 = ((EditText) getView().findViewById(R.id.etPassword2)).getText().toString();
        if (!etPassword1.equals(etPassword2)){
            Util.getToast(getActivity().getApplicationContext(),"Passwords must match.", true ).show();
            return;
        }


        Util.getToast(getActivity().getApplicationContext(),"Attempting to sign-up with server - please wait", false ).show();

//        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//        if (imm == null){
//            Log.e(TAG, "imm is null");
//            return;
//        }
//
//        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);



        signUp(username, emailAddress, etPassword1, getActivity().getApplicationContext());
    }

    private void signUp(final String username, final String emailAddress, final String password, final Context context) {
        disableFlightMode();

        // Now wait for network connection as disableFlightMode takes a while
        if (!Util.waitForNetworkConnection(getActivity().getApplicationContext(), true)){
            Log.e(TAG, "Failed to disable airplane mode");
            return ;
        }

        Thread signUpThread = new Thread() {
            @Override
            public void run() {
                Server.signUp(username, emailAddress, password, context);;
            }
        };
        signUpThread.start();

    }

    private void disableFlightMode(){
        try {
            Util.disableFlightMode(getActivity().getApplicationContext());


        }catch (Exception ex){
            Log.e(TAG, ex.getLocalizedMessage());
            Util.getToast(getActivity().getApplicationContext(), "Error disabling flight mode", true).show();
        }
    }

    private final BroadcastReceiver onNotice = new BroadcastReceiver() {
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

        // broadcast notification coming from ??
        @Override
        public void onReceive(Context context, Intent intent) {
          //  Prefs prefs = new Prefs(getActivity().getApplicationContext());
            try {
                String jsonStringMessage = intent.getStringExtra("jsonStringMessage");

                if (jsonStringMessage != null) {


                    JSONObject joMessage = new JSONObject(jsonStringMessage);
                    String messageType = joMessage.getString("messageType");
                    String messageToDisplay = joMessage.getString("messageToDisplay");

                    if (messageType.equalsIgnoreCase("SUCCESSFULLY_CREATED_USER")) {
                        Util.getToast(getActivity().getApplicationContext(), messageToDisplay, false).show();
                    }else if(messageType.equalsIgnoreCase("FAILED_TO_CREATE_USER")) {
                        Util.getToast(getActivity().getApplicationContext(), messageToDisplay, true).show();
                    }else if (messageType.equalsIgnoreCase("422_FAILED_TO_CREATE_USER")) {
                        Util.getToast(getActivity().getApplicationContext(), messageToDisplay, true).show();
                    }

                    }

                displayOrHideGUIObjects();

            } catch (Exception ex) {

                Log.e(TAG, ex.getLocalizedMessage());
            }
        }
    };

}
