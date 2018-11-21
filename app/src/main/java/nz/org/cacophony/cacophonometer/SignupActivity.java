package nz.org.cacophony.cacophonometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

public class SignupActivity extends AppCompatActivity implements IdlingResourceForEspressoTesting{

    private static final String TAG = SignupActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //https://developer.android.com/training/appbar/setting-up#java
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    @Override
    public void onResume() {
        super.onResume();

        displayOrHideGUIObjects();

        IntentFilter iff = new IntentFilter("event");
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


    @SuppressWarnings("SameReturnValue")
    public CountingIdlingResource getIdlingResource() {
        return registerIdlingResource;
    }

    @SuppressWarnings("SameReturnValue")
    public CountingIdlingResource getRecordNowIdlingResource() {
        return recordNowIdlingResource;
    }

    public void signupButton(View v) {
       // registerIdlingResource.increment();

        Prefs prefs = new Prefs(getApplicationContext());

        // if (prefs.getOffLineMode()){
        if (prefs.getInternetConnectionMode().equalsIgnoreCase("offline")){
            Util.getToast(getApplicationContext(),"The internet connection (in Advanced) has been set 'offline' - so this device can not be registered", true ).show();
            return;
        }

        if (!Util.isNetworkConnected(getApplicationContext())){
            Util.getToast(getApplicationContext(),"The phone is not currently connected to the internet - please fix and try again", true ).show();
            return;
        }


        // Check that the username is valid, at least 5 characters.
        String username = ((EditText) findViewById(R.id.etUsername)).getText().toString();
        if (username.length() < 1){
            Util.getToast(getApplicationContext(),"Please enter a Username of at least 5 characters (no spaces)", true ).show();
            return;
        }else if (username.length() < 5) {
            Log.i(TAG, "Invalid Username: "+ username);

            Util.getToast(getApplicationContext(),username + " is not a valid username. Please use at least 5 characters (no spaces)", true ).show();
            return;
        }

        //Check email is valid
        String emailAddress = ((EditText) findViewById(R.id.etEmail)).getText().toString();

        if (emailAddress.length() < 1) {
            Util.getToast(getApplicationContext(), "Please enter an email address", true).show();
            return;
        } else if (!Util.isValidEmail(emailAddress)){
           Util.getToast(getApplicationContext(),emailAddress + " is not a valid email address.", true ).show();
           return;
       }

       //Check password is valid
        String etPassword1 = ((EditText) findViewById(R.id.etPassword1)).getText().toString();
        if (etPassword1.length() < 8){
            Util.getToast(getApplicationContext(),"Minimum password length is 8 characters.", true ).show();
            return;
        }
        String etPassword2 = ((EditText) findViewById(R.id.etPassword2)).getText().toString();
        if (!etPassword1.equals(etPassword2)){
            Util.getToast(getApplicationContext(),"Passwords must match.", true ).show();
            return;
        }


        Util.getToast(getApplicationContext(),"Attempting to sign-up with server - please wait", false ).show();

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null){
            Log.e(TAG, "imm is null");
            return;
        }

        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);



        signUp(username, emailAddress, etPassword1, getApplicationContext());

    }

    private void signUp(final String username, final String emailAddress, final String password, final Context context) {
        disableFlightMode();

        // Now wait for network connection as disableFlightMode takes a while
        if (!Util.waitForNetworkConnection(getApplicationContext(), true)){
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
            Util.disableFlightMode(getApplicationContext());


        }catch (Exception ex){
            Log.e(TAG, ex.getLocalizedMessage());
            Util.getToast(getApplicationContext(), "Error disabling flight mode", true).show();
        }
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

                    if (intendedActivity.equalsIgnoreCase("SignupActivity")){

                        int responseCode = joMessage.getInt("responseCode");


                        if (responseCode == 200){
                            Util.getToast(getApplicationContext(),messageToDisplay, false ).show();
                        }else{
                            Util.getToast(getApplicationContext(),messageToDisplay, true ).show();
                        }
                        displayOrHideGUIObjects();
                    }
                }

            } catch (Exception ex) {

                Log.e(TAG, ex.getLocalizedMessage());
            }
        }
    };

    void displayOrHideGUIObjects(){
        Prefs prefs = new Prefs(getApplicationContext());

        if (prefs.getUsername() == null){
            ((TextView) findViewById(R.id.tvChooseUsername)).setText("Choose a username");
            ((TextView) findViewById(R.id.tvEnterEmail)).setText("Email address");
            ((TextView) findViewById(R.id.tvTitle)).setText("You need to signup to the Cacophony Server");

            findViewById(R.id.etUsername).setVisibility(View.VISIBLE);
            findViewById(R.id.etEmail).setVisibility(View.VISIBLE);
            findViewById(R.id.etEmail).setVisibility(View.VISIBLE);
            findViewById(R.id.tvEnterPassword).setVisibility(View.VISIBLE);
            findViewById(R.id.etPassword1).setVisibility(View.VISIBLE);
            findViewById(R.id.tvConfirmPassword).setVisibility(View.VISIBLE);
            findViewById(R.id.etPassword2).setVisibility(View.VISIBLE);
            findViewById(R.id.btnSignUp).setVisibility(View.VISIBLE);
            findViewById(R.id.btnForgetUser).setVisibility(View.INVISIBLE);
        }else{

            ((TextView) findViewById(R.id.tvChooseUsername)).setText("Username is " + prefs.getUsername());
            ((TextView) findViewById(R.id.tvEnterEmail)).setText("Email address is " + prefs.getEmailAddress());
            ((TextView) findViewById(R.id.tvTitle)).setText("This phone is registered with a valid Cacophonometer account.");

            findViewById(R.id.etUsername).setVisibility(View.INVISIBLE);
            findViewById(R.id.etEmail).setVisibility(View.INVISIBLE);
            findViewById(R.id.etEmail).setVisibility(View.INVISIBLE);
            findViewById(R.id.tvEnterPassword).setVisibility(View.INVISIBLE);
            findViewById(R.id.etPassword1).setVisibility(View.INVISIBLE);
            findViewById(R.id.tvConfirmPassword).setVisibility(View.INVISIBLE);
            findViewById(R.id.etPassword2).setVisibility(View.INVISIBLE);
            findViewById(R.id.btnSignUp).setVisibility(View.INVISIBLE);
            findViewById(R.id.btnForgetUser).setVisibility(View.VISIBLE);
        }

    }
    public void forgetUser(View v) {
        Prefs prefs = new Prefs(getApplicationContext());
        prefs.setUsername(null);
        displayOrHideGUIObjects();
    }

}