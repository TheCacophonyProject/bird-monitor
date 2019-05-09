package nz.org.cacophony.birdmonitor;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
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
    private Button btnSignOutUser;
    private TextView tvMessages;
    private EditText etUserNameOrPasswordInput;
    private EditText etPasswordInput;
    private TextInputLayout tilUserNameOrPassword;
    private TextInputLayout tilPassword;
    private TextView tvTitleMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);
        setUserVisibleHint(false);
        etUserNameOrPasswordInput = view.findViewById(R.id.etUserNameOrEmailInput);
        etPasswordInput = view.findViewById(R.id.etPasswordInput);
        tilUserNameOrPassword = view.findViewById(R.id.tilUserNameOrEmail);
        tilPassword = view.findViewById(R.id.tilPassword);
        tvTitleMessage = view.findViewById(R.id.tvTitleMessageSignIn);

        btnSignIn = view.findViewById(R.id.btnSignIn);
        btnSignOutUser = view.findViewById(R.id.btnSignOutUser);
        tvMessages = view.findViewById(R.id.tvMessages);

        btnSignIn.setOnClickListener(v -> {
            // https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
            signinButtonPressed();
        });

        btnSignOutUser.setOnClickListener(v -> {
            // https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
            signOutUserButtonPressed();
        });

        displayOrHideGUIObjects();
        return view;
    }

    private void signInUser() {

        disableFlightMode();

        // Now wait for network connection as setFlightMode takes a while
        if (!Util.waitForNetworkConnection(getActivity().getApplicationContext(), true)) {
            Log.e(TAG, "Failed to disable airplane mode");
            return;
        }

        Thread loginThread = new Thread() {
            @Override
            public void run() {
                Server.loginUser(getActivity().getApplicationContext());
            }
        };
        loginThread.start();
    }

    private void disableFlightMode() {
        try {
            Util.disableFlightMode(getActivity().getApplicationContext());
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage(), ex);
            ((SetupWizardActivity) getActivity()).displayOKDialogMessage("Error", "Error disabling flight mode.");
        }
    }

    @Override
    public void setUserVisibleHint(final boolean visible) {
        super.setUserVisibleHint(visible);
        if (getActivity() == null) {
            return;
        }
        if (visible) {
            //  signInGUIIdlingResource.increment();
            IntentFilter iff = new IntentFilter("SERVER_USER_LOGIN");
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onNotice, iff);
            displayOrHideGUIObjects();
        } else {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onNotice);
        }
    }

    void displayOrHideGUIObjects() {

        Prefs prefs = new Prefs(getActivity().getApplicationContext());

        boolean signedIn = prefs.getUserSignedIn();
        String userNameOrEmailAddress = prefs.getUserNameOrEmailAddress();
        if (signedIn) {

            tilUserNameOrPassword.setVisibility(View.GONE);
            tilPassword.setVisibility(View.GONE);
            tvTitleMessage.setText("Signed In");
            btnSignIn.setVisibility(View.GONE);
            tvMessages.setText("You are signed in as " + userNameOrEmailAddress + "\n\n \'Swipe\' to the next step.");

            btnSignOutUser.setVisibility(View.VISIBLE);
            btnSignOutUser.setEnabled(true);
            btnSignOutUser.requestFocus();
        } else {
            tvTitleMessage.setText("Enter your Cacophony Username and password");
            tilUserNameOrPassword.setVisibility(View.VISIBLE);
            tilPassword.setVisibility(View.VISIBLE);

            if (userNameOrEmailAddress == null) {
                etUserNameOrPasswordInput.requestFocus();
            } else {
                etUserNameOrPasswordInput.setText(userNameOrEmailAddress);
                etPasswordInput.requestFocus();
                //  signInGUIIdlingResource.decrement();
            }
            btnSignIn.setVisibility(View.VISIBLE);
            btnSignOutUser.setVisibility(View.GONE);
        }

    }


    private final BroadcastReceiver onNotice = new BroadcastReceiver() {
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

        // broadcast notification coming from ??
        @Override
        public void onReceive(Context context, Intent intent) {
            Prefs prefs = new Prefs(context);
            tvMessages.setText("");

            String userNameOrEmailAddress = "";
            if (prefs.getUserNameOrEmailAddress() != null) {
                userNameOrEmailAddress = prefs.getUserNameOrEmailAddress();
            } else if (prefs.getUsername() != null) {
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


                    if (messageType.equalsIgnoreCase("SUCCESSFULLY_SIGNED_IN")) {

                        ((SetupWizardActivity) getActivity()).setNumberOfPagesForSignedInNotRegistered();

                        tvMessages.setText(messageToDisplay + userNameOrEmailAddress + "\n\n \'Swipe\' to the next step.");
                        tvTitleMessage.setText("Signed In");
                        btnSignIn.setEnabled(false);
                        btnSignIn.setVisibility(View.GONE);
                        btnSignOutUser.setEnabled(true);
                        btnSignOutUser.setVisibility(View.GONE);
                        tilUserNameOrPassword.setVisibility(View.GONE);
                        tilPassword.setVisibility(View.GONE);
                        etPasswordInput.setText("");

                        Util.getGroupsFromServer(getActivity().getApplicationContext());

                    } else if (messageType.equalsIgnoreCase("NETWORK_ERROR")) {
                        ((SetupWizardActivity) getActivity()).displayOKDialogMessage("Error", messageToDisplay);
                        etUserNameOrPasswordInput.setText(userNameOrEmailAddress);
                        return;

                    } else if (messageType.equalsIgnoreCase("INVALID_CREDENTIALS")) {
                        ((SetupWizardActivity) getActivity()).displayOKDialogMessage("Error", messageToDisplay);
                        etUserNameOrPasswordInput.setText(userNameOrEmailAddress);
                        return;

                    } else if (messageType.equalsIgnoreCase("UNABLE_TO_SIGNIN")) {

                        ((SetupWizardActivity) getActivity()).displayOKDialogMessage("Error", messageToDisplay);
                        etUserNameOrPasswordInput.setText(userNameOrEmailAddress);
                        return;
                    }

                }

            } catch (Exception ex) {
                Log.e(TAG, ex.getLocalizedMessage(), ex);
                ((SetupWizardActivity) getActivity()).displayOKDialogMessage("Error", "Could not login.");
            }
        }
    };

    public void signinButtonPressed() {

        try {


            Prefs prefs = new Prefs(getActivity().getApplicationContext());

            if (prefs.getInternetConnectionMode().equalsIgnoreCase("offline")) {

                ((SetupWizardActivity) getActivity()).displayOKDialogMessage("Oops", "The internet connection (in Advanced) has been set 'offline' - so this device can not be registered.");
                return;
            }

            if (!Util.isNetworkConnected(getActivity().getApplicationContext())) {

                ((SetupWizardActivity) getActivity()).displayOKDialogMessage("Oops", "The phone is not currently connected to the internet - please fix and try again.");
                return;
            }

            // Check that the user name is valid, at least 5 characters.
            String usernameOrEmailAddress = ((EditText) getView().findViewById(R.id.etUserNameOrEmailInput)).getText().toString();
            if (usernameOrEmailAddress.length() < 1) {

                ((SetupWizardActivity) getActivity()).displayOKDialogMessage("Oops", "Please enter a username of at least 5 characters (no spaces).");
                return;
            } else if (usernameOrEmailAddress.length() < 5 && !usernameOrEmailAddress.contains("@")) {
                Log.i(TAG, "Invalid usernameOrEmailAddress: " + usernameOrEmailAddress);

                ((SetupWizardActivity) getActivity()).displayOKDialogMessage("Oops", "Invalid usernameOrEmailAddress: " + usernameOrEmailAddress);
                return;
            }

            // Check that the password is valid, at least 8 characters.
            String password = ((EditText) getView().findViewById(R.id.etPasswordInput)).getText().toString();
            if (password.length() < 1) {

                ((SetupWizardActivity) getActivity()).displayOKDialogMessage("Oops", "Please enter a password of at least 8 characters (no spaces)");
                return;
            } else if (password.length() < 5) {

                ((SetupWizardActivity) getActivity()).displayOKDialogMessage("Oops", "Please use at least 8 characters (no spaces)");
                return;
            }

            String userNameFromPrefs = prefs.getUsername();
            if (userNameFromPrefs == null) {
                userNameFromPrefs = "";
            }

            String emailAddressFromPrefs = prefs.getEmailAddress();
            if (emailAddressFromPrefs == null) {
                emailAddressFromPrefs = "";
            }

            if (!usernameOrEmailAddress.equalsIgnoreCase(userNameFromPrefs) && !usernameOrEmailAddress.equalsIgnoreCase(emailAddressFromPrefs)) {
                prefs.setUsername(null);
                prefs.setEmailAddress(null);
            }
            prefs.setUserNameOrEmailAddress(usernameOrEmailAddress);
            prefs.setUsernamePassword(password);


            signInUser();


        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage(), ex);
        }

        tvMessages.setText("Attempting to sign into the server - please wait");
    }

    public void signOutUserButtonPressed() {

        Util.signOutUser(getActivity().getApplicationContext());
        ((SetupWizardActivity) getActivity()).setNumberOfPagesForNotSigned();

        tvMessages.setText("You have been signed out from this phone");
        tilUserNameOrPassword.setVisibility(View.VISIBLE);
        tilPassword.setVisibility(View.VISIBLE);
        btnSignIn.setEnabled(true);
        btnSignIn.setVisibility(View.VISIBLE);
        btnSignOutUser.setEnabled(false);
        btnSignOutUser.setVisibility(View.INVISIBLE);

        displayOrHideGUIObjects();
    }
}
