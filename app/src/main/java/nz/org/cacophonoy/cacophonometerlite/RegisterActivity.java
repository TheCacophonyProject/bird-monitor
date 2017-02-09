package nz.org.cacophonoy.cacophonometerlite;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

public class RegisterActivity extends Activity {

    public static String token = null;
    public static final String PREFS_NAME = "Cacophony_App";

    // Handler status indicators
    private static final int REGISTER_SUCCESS = 1;
    private static final int REGISTER_FAIL = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        EditText serverUrlEditText = (EditText) findViewById(R.id.serverEditText);
        serverUrlEditText.setText(Server.SERVER_URL);
    }

    @Override
    public void onResume() {
        TextView registerStatus = (TextView) findViewById(R.id.register_status);
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(RegisterActivity.PREFS_NAME, Context.MODE_PRIVATE);

        String group =prefs.getString("group", null);
        if (group != null)
            registerStatus.setText("Registered in group: "+group);
        else
            registerStatus.setText("Device not registered.");
        super.onResume();
    }




    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {
            Log.d(TAG, "Received message.");
            switch (inputMessage.what) {
                case REGISTER_SUCCESS:
                    onResume();
                    Toast.makeText(getApplicationContext(), "Registered device.", Toast.LENGTH_SHORT).show();
                    break;
                case REGISTER_FAIL:
                    onResume();
                    Toast.makeText(getApplicationContext(), "Failed to register.", Toast.LENGTH_SHORT).show();
                default:
                    // Unknown case
                    break;
            }
        }
    };

    public void registerButton(View v) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        //Get Group from text field.
        String group = ((EditText) findViewById(R.id.group_edit_text)).getText().toString();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.getString("group", "").equals(group)) {
            // Try to login with username and password.
            Toast.makeText(getApplicationContext(), "Already registered with that group.", Toast.LENGTH_SHORT).show();
            return;
        }
        register(group, getApplicationContext());
    }

    /**
     * Un-registered a device deleting the password, devicename, and JWT.
     * @param v View
     */
    public void unRegisterButton(View v) {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(RegisterActivity.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove("group").apply();
        prefs.edit().remove("password").apply();
        prefs.edit().remove("devicename").apply();
        onResume();
        Server.loggedIn = false;
    }

    /**
     * Will register the device in the given group saving the JSON Web Token, devicename, and password.
     * @param group name of goup to join.
     */
    public void register(final String group, final Context context) {
        // Check that the group name is valid, at least 4 characters.
        if (group == null || group.length() < 4) {
            Log.i("Register", "Invalid group name: "+group);
            Toast.makeText(context, "Invalid Group name: "+group, Toast.LENGTH_SHORT).show();
        }

        Thread registerThread = new Thread() {
            @Override
            public void run() {
                Message message = handler.obtainMessage();
                if (Server.register(group, context)) {
                    message.what = REGISTER_SUCCESS;
                } else {
                    message.what = REGISTER_FAIL;
                }
                message.sendToTarget();
            }
        };
        registerThread.start();
    }

    public void updateServerUrlButton(View v) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        String newUrl =  ((EditText) findViewById(R.id.serverEditText)).getText().toString();
        if (URLUtil.isValidUrl(newUrl)) {
            Server.SERVER_URL = newUrl;
            Toast.makeText(getApplicationContext(), "Updated Server URL", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Invalid URL", Toast.LENGTH_SHORT).show();
        }
    }
}
