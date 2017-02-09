package nz.org.cacophonoy.cacophonometerlite;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;

import cz.msebera.android.httpclient.Header;


/**
 * This class deals with connecting to the server (test connection, Login, Register, upload recording).
 */

class Server {
    private static final String LOG_TAG = "Server.java";

    public static String SERVER_URL = "http://52.64.67.145:8888";       // Server URL
    //public static String SERVER_URL = "http://192.168.1.13:8888";     // Local Server URL
    private static final String UPLOAD_AUDIO_API_URL = "/api/v1/audiorecordings";
    private static final String PING_URL =  "/ping";
    private static final String LOGIN_URL = "/authenticate_device";
    private static final String REGISTER_URL = "/api/v1/devices";

    static boolean serverConnection = false;
    static boolean loggedIn = false;
    private static String token = null;
    private static boolean uploading = false;
    private static boolean uploadSuccess = false;

    /**
     * Will ping server and try to login.
     * @param context app context
     */
    static void updateServerConnectionStatus(Context context) {
        if (!ping()) {
            Log.d(LOG_TAG, "Could not connect to server");
        } else {
            login(context);
        }
    }

    /**
     * Pings server. Can't be run on main thread as it does a synchronous http request.
     * Use runPing instead if on main thread or want it to be asynchronous.
     * @return if got a response from server.
     */
    private static boolean ping() {
        SyncHttpClient client = new SyncHttpClient();
        client.get(SERVER_URL + PING_URL, null, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String responseString = new String(response);
                serverConnection = (responseString.equals("pong..."));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                serverConnection = false;
            }
        });
        return serverConnection;
    }

    /**
     * Will login and save JSON Web Token. Can't be run on main/UI thread as it does a synchronous http request.
     * @param context app context
     * @return if login was successful
     */
    static boolean login(final Context context) {
        // Get credentials from shared preferences.
        SharedPreferences prefs = context.getSharedPreferences(RegisterActivity.PREFS_NAME, Context.MODE_PRIVATE);
        String devicename = prefs.getString("devicename", null);
        String password = prefs.getString("password", null);
        String group = prefs.getString("group", null);
        if (devicename == null || password == null || group == null) {
            // One or more credentials are null, failed login.
            Log.d(LOG_TAG, "No credentials to login with.");
            loggedIn = false;
            return false;
        }

        SyncHttpClient client = new SyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("devicename", devicename);
        params.put("password", password);

        client.post(SERVER_URL+LOGIN_URL, params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String responseString = new String(response);
                try {
                    JSONObject joRes = new JSONObject(responseString);
                    if (joRes.getBoolean("success")) {
                        Log.i("Login", "Successful login.");
                        loggedIn = true;
                        token = joRes.getString("token");  // Save JWT (JSON Web Token)
                    } else {
                        loggedIn = false;
                        token = null;
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error with parsing register response into a JSON.");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                if (statusCode == 401) {
                    loggedIn = false;
                    Log.e(LOG_TAG, "Invalid devicename or password for login.");
                } else {
                    loggedIn = false;
                }
                token = null;
            }
        });
        return loggedIn;
    }

    /**
     * Does a synchronous http request to register the device. Can't be run on main/UI thread.
     * @param group Name of group to register under.
     * @param context App context.
     * @return If the device successfully registered.
     */
    static boolean register(final String group, final Context context) {
        // Check that the group name is valid, at least 4 characters.
        if (group == null || group.length() < 4) {
            Log.i("Register", "Invalid group name: "+group);
            return false;
        }
        SyncHttpClient client = new SyncHttpClient();
        RequestParams params = new RequestParams();

        final String devicename = RandomStringUtils.random(20, true, true);
        final String password = RandomStringUtils.random(20, true, true);

        params.put("devicename", devicename);
        params.put("password", password);
        params.put("group", group);

        // Sent Post request.
        client.post(SERVER_URL + REGISTER_URL, params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String responseString = new String(response);
                try {
                    JSONObject joRes = new JSONObject(responseString);
                    if (joRes.getBoolean("success")) {
                        loggedIn = true;
                        token = joRes.getString("token");
                        SharedPreferences prefs = context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
                        prefs.edit().putString("devicename", devicename).apply();
                        prefs.edit().putString("password", password).apply();
                        prefs.edit().putString("group", group).apply();
                    } else {
                        // Failed register.
                        loggedIn = false;
                    }
                } catch (JSONException e) {
                    loggedIn = false;
                    Log.e(LOG_TAG, "Error with parsing register response into a JSON");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                loggedIn = false;
                Log.e(LOG_TAG, "Error with getting response from server");
            }
        });
        return loggedIn;
    }

    /**
     * Does a synchronous http request to upload the file and JSON Object to the server as an audio
     * recording. Can't be run on main/UI thread.
     * @param audioFile recording.
     * @param data metadata.
     * @return If upload was successful
     */
    static boolean uploadAudioRecording(File audioFile, JSONObject data) {
        if (uploading) {
            Log.d(LOG_TAG, "Already uploading. Wait until last upload is finished.");
            return false;
        }
        uploading = true;

        if (audioFile == null || data == null) {
            Log.e(LOG_TAG, "uploadAudioRecording: Invalid audioFile or JSONObject. Aborting upload");
            return false;
        }

        System.out.println("Going to upload file " + audioFile.getName());

        // Check that there is a JWT (JSON Web Token)
        if (token == null) {
            Log.w(LOG_TAG, "sendFile: no JWT. Aborting upload");
            return false; // Can't upload without JWT, login/register device to get JWT.
        }

        // Building POST request
        SyncHttpClient client = new SyncHttpClient();
        client.addHeader("Authorization", token);
        RequestParams params = new RequestParams();
        params.put("data", data.toString());
        try {
            params.put("file", audioFile);
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "File not found, can't upload...");
            return false;
        }

        // Send request
        client.post(SERVER_URL + UPLOAD_AUDIO_API_URL, params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                Log.i(LOG_TAG, "sendFile: onSuccess: Successful upload.");
                uploadSuccess = true;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.i(LOG_TAG, "sendFile: onSuccess: Failed upload.");
                uploadSuccess = false;
            }
        });
        Log.d(LOG_TAG, "uploadAudioRecording: finished.");
        uploading = false;
        return uploadSuccess;
    }
}
