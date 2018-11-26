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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.json.JSONObject;

public class SigninActivity extends AppCompatActivity {
    private static final String TAG = SigninActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        //https://developer.android.com/training/appbar/setting-up#java
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    @Override
    public void onResume() {
        super.onResume();
        Prefs prefs = new Prefs(getApplicationContext());

        IntentFilter iff = new IntentFilter("SERVER_USER_LOGIN");
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);

       String username = prefs.getUsername();
        String emailAddress = prefs.getEmailAddress();
        String usernameOrEmailAddress = prefs.getUserNameOrEmailAddress();
        if (usernameOrEmailAddress !=null){
            ((EditText)findViewById(R.id.etUserNameOrPasswordInput)).setText(usernameOrEmailAddress);
        }else if (username != null){
           ((EditText)findViewById(R.id.etUserNameOrPasswordInput)).setText(username);
       }else if(emailAddress != null){
           ((EditText)findViewById(R.id.etUserNameOrPasswordInput)).setText(emailAddress);
        }
       String password = prefs.getUsernamePassword();
        if (password != null){
            ((EditText)findViewById(R.id.etPasswordInput)).setText(password);
        }

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


    public void signinButton(View v) {
        try {
            Prefs prefs = new Prefs(getApplicationContext());

            if (prefs.getInternetConnectionMode().equalsIgnoreCase("offline")){
                Util.getToast(getApplicationContext(),"The internet connection (in Advanced) has been set 'offline' - so this device can not be registered", true ).show();
                return;
            }

            if (!Util.isNetworkConnected(getApplicationContext())){
                Util.getToast(getApplicationContext(),"The phone is not currently connected to the internet - please fix and try again", true ).show();
                return;
            }

            // Check that the user name is valid, at least 5 characters.
            String usernameOrEmailAddress = ((EditText) findViewById(R.id.etUserNameOrPasswordInput)).getText().toString();
            if (usernameOrEmailAddress.length() < 1){
                Util.getToast(getApplicationContext(),"Please enter a username of at least 5 characters (no spaces)", true ).show();
                return;
            }else if (usernameOrEmailAddress.length() < 5 && !usernameOrEmailAddress.contains("@")) {
                Log.i(TAG, "Invalid usernameOrEmailAddress: "+usernameOrEmailAddress);

                Util.getToast(getApplicationContext(),usernameOrEmailAddress + " is not a valid username or email address.", true ).show();
                return;
            }

            // Check that the password is valid, at least 8 characters.
            String password = ((EditText) findViewById(R.id.etPasswordInput)).getText().toString();
            if (password.length() < 1){
                Util.getToast(getApplicationContext(),"Please enter a passord of at least 8 characters (no spaces)", true ).show();
                return;
            }else if (password.length() < 5) {
                Log.i(TAG, "Invalid password");

                Util.getToast(getApplicationContext(), "Please use at least 8 characters (no spaces)", true ).show();
                return;
            }

            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm == null){
                Log.e(TAG, "imm is null");
                return;
            }

            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

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

        Util.getToast(getApplicationContext(),"Attempting to sign into the server - please wait", false ).show();

    }

    private void login(){
        disableFlightMode();

        // Now wait for network connection as setFlightMode takes a while
        if (!Util.waitForNetworkConnection(getApplicationContext(), true)){
            Log.e(TAG, "Failed to disable airplane mode");
            return ;
        }

        Thread loginThread = new Thread() {
            @Override
            public void run() {
                Server.loginUser(getApplicationContext());;
            }
        };
        loginThread.start();
    }


    private void disableFlightMode(){
        try {
            Util.disableFlightMode(getApplicationContext());


        }catch (Exception ex){
            Log.e(TAG, ex.getLocalizedMessage());
            Util.getToast(getApplicationContext(), "Error disabling flight mode", true).show();
        }
    }

    private void nextActivity(){
        try {
            Intent intentNext = new Intent(this, GroupActivity.class);
            startActivity(intentNext);
            //  finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    private final BroadcastReceiver onNotice = new BroadcastReceiver() {
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

        // broadcast notification coming from ??
        @Override
        public void onReceive(Context context, Intent intent) {

           // Prefs prefs = new Prefs(getApplicationContext());
            try {


                String jsonStringMessage = intent.getStringExtra("jsonStringMessage");

                if (jsonStringMessage != null) {


                    JSONObject joMessage = new JSONObject(jsonStringMessage);
                    String messageType = joMessage.getString("messageType");
                    String messageToDisplay = joMessage.getString("messageToDisplay");

                    if (messageType.equalsIgnoreCase("SUCCESSFULLY_SIGNED_IN")){
                        Util.getToast(getApplicationContext(),messageToDisplay, false ).show();
                        Util.getGroupsFromServer(getApplicationContext());
                        nextActivity();
                    } else{
                        Util.getToast(getApplicationContext(),messageToDisplay, true ).show();
                        return;
                    }

                }

            } catch (Exception ex) {
                Log.e(TAG, ex.getLocalizedMessage());
                Util.getToast(getApplicationContext(),"Could not login", true ).show();
            }
        }
    };
}
