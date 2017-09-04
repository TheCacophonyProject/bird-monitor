package nz.org.cacophony.cacophonometerlite;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
//import android.util.Log;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import cz.msebera.android.httpclient.Header;

import static nz.org.cacophony.cacophonometerlite.Util.sendMainActivityAMessage;

/**
 * This class deals with connecting to the server (test connection, Login, Register, upload recording).
 */

class Server {
    private static final String LOG_TAG = Server.class.getName();

    private static final String UPLOAD_AUDIO_API_URL = "/api/v1/audiorecordings";
    private static final String PING_URL = "/ping";
    private static final String LOGIN_URL = "/authenticate_device";
    private static final String REGISTER_URL = "/api/v1/devices";

    static boolean serverConnection = false;
    static boolean loggedIn = false;
    private static String token = null;
    private static String errorMessage = null;
    private static boolean uploading = false;
    private static boolean uploadSuccess = false;
    private static Logger logger = null;
    static
    {
        if (logger == null){
            logger =  LoggerFactory.getLogger(LOG_TAG); // couldn't call getAndConfigureLogger as no context is currently available.  But this should work as Util.configureLogbackDirectly(context) should have been called by now.
        }
    }

    /**
     * Will ping server and try to login.
     *
     * @param context app context
     */
    static void updateServerConnectionStatus(Context context) {
        logger.info("updateServerConnectionStatus method");
try {
    Util.disableFlightMode(context);
    // Now wait for network connection as setFlightMode takes a while
    if (!Util.waitForNetworkConnection(context, true)) {

        logger.error("Failed to disable airplane mode");
        return;
    }



    if (!ping(context)) {
        logger.error("Could not connect to server");
    } else {
        login(context);
    }

//    Util.enableFlightMode(context);
}catch (Exception ex){
    logger.error(ex.getLocalizedMessage());
}finally {
    Util.sendMainActivityAMessage(context, "enable_vitals_button");
    Util.sendMainActivityAMessage(context, "enable_test_recording_button");
    Util.sendMainActivityAMessage(context, "enable_setup_button");
}
    }

    /**
     * Pings server. Can't be run on main thread as it does a synchronous http request.
     * Use runPing instead if on main thread or want it to be asynchronous.
     *
     * @return if got a response from server.
     */
    private static boolean ping(Context context) {

        if (!Util.isNetworkConnected(context)){
            Util.disableFlightMode(context);
        }

        // Now wait for network connection as setFlightMode takes a while
        if (!Util.waitForNetworkConnection(context, true)){
//            Log.e(LOG_TAG, "Failed to disable airplane mode");
//            Util.writeLocalLogEntryUsingLogback(context, LOG_TAG, "Failed to disable airplane mode");
            logger.error("Failed to disable airplane mode");
            return false;
        }

        SyncHttpClient client = new SyncHttpClient();

        Prefs prefs = new Prefs(context);
//        boolean useTestServer = prefs.getUseTestServer();
//        client.get(prefs.getServerUrl(useTestServer) + PING_URL, null, new AsyncHttpResponseHandler() {
             client.get(prefs.getServerUrl() + PING_URL, null, new AsyncHttpResponseHandler() {

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
     *
     * @param context app context
     * @return if login was successful
     */
//    private static boolean login(Context context) {
    static boolean login(Context context) {

        Util.disableFlightMode(context);

        // Now wait for network connection as setFlightMode takes a while
        if (!Util.waitForNetworkConnection(context, true)){
//            Log.e(LOG_TAG, "Failed to disable airplane mode");
//            Util.writeLocalLogEntryUsingLogback(context, LOG_TAG, "Failed to disable airplane mode");

            logger.error("Failed to disable airplane mode");
            return false;
        }

        // Get credentials from shared preferences.
        Prefs prefs = new Prefs(context);
        String devicename = prefs.getDeviceName();
        String password = prefs.getPassword();
        String group = prefs.getGroupName();
        if (devicename == null || password == null || group == null) {

            // One or more credentials are null, so can not attempt to login.
//            Log.e(LOG_TAG, "No credentials to login with.");
//            Util.writeLocalLogEntryUsingLogback(context, LOG_TAG, "No credentials to login with.");
            logger.error("No credentials to login with.");
            loggedIn = false;
            return false;
        }

        SyncHttpClient client = new SyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("devicename", devicename);
        params.put("password", password);

//        boolean useTestServer = prefs.getUseTestServer();
//
//        client.post(prefs.getServerUrl(useTestServer) + LOGIN_URL, params, new AsyncHttpResponseHandler() {
            client.post(prefs.getServerUrl() + LOGIN_URL, params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String responseString = new String(response);
                try {
                    JSONObject joRes = new JSONObject(responseString);
                    if (joRes.getBoolean("success")) {
//                        Log.i("Login", "Successful login.");
                        logger.info("Login", "Successful login.");
                        loggedIn = true;
                        setToken(joRes.getString("token"));  // Save JWT (JSON Web Token)

                    } else {
                        loggedIn = false;
                        setToken(null);
                    }
                } catch (JSONException e) {
                   // Log.e(LOG_TAG, "Error with parsing register response into a JSON.");
                    logger.error("Error with parsing register response into a JSON.");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                if (statusCode == 401) {
                    loggedIn = false;
//                    Log.e(LOG_TAG, "Invalid devicename or password for login.");
                    logger.error("Invalid devicename or password for login.");
                } else {
                    loggedIn = false;
                }
                setToken(null);
            }
        });
        return loggedIn;
    }

    /**
     * Does a synchronous http request to register the device. Can't be run on main/UI thread.
     *
     * @param group   Name of group to register under.
     * @param context App context.
     * @return If the device successfully registered.
     */
    static boolean register(final String group, final Context context) {

        // Check that the group name is valid, at least 4 characters.
        if (group == null || group.length() < 4) {
//            Log.i("Register", "Invalid group name: " + group);
//            Util.writeLocalLogEntryUsingLogback(context, LOG_TAG, "Invalid group name: " + group);
            logger.info("Register", "Invalid group name: " + group);
            return false;
        }
        SyncHttpClient client = new SyncHttpClient();
        RequestParams params = new RequestParams();

        final String devicename = RandomStringUtils.random(20, true, true);
        final String password = RandomStringUtils.random(20, true, true);

        params.put("devicename", devicename);
        params.put("password", password);
        params.put("group", group);

        final Prefs prefs = new Prefs(context);

        // Sent Post request.
//        boolean useTestServer = prefs.getUseTestServer();
//        client.post(prefs.getServerUrl(useTestServer) + REGISTER_URL, params, new AsyncHttpResponseHandler() {
        client.post(prefs.getServerUrl() + REGISTER_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String responseString = new String(response);
                try {
                    JSONObject joRes = new JSONObject(responseString);
                    if (joRes.getBoolean("success")) {
                        loggedIn = true;
                        setToken(joRes.getString("token"));

                        // look at web token
                        String deviceID = Util.getDeviceID(context, getToken());
                        prefs.setDeviceId(deviceID);

                        prefs.setDeviceName(devicename);
                        prefs.setGroupName(group);
                        prefs.setPassword(password);

                        prefs.setDeviceId(deviceID);
                    } else {
                        // Failed register.
                        loggedIn = false;
                    }
                } catch (JSONException ex) {
                    loggedIn = false;
//                    Log.e(LOG_TAG, "Error with parsing register response into a JSON");
//                    Util.writeLocalLogEntryUsingLogback(context, LOG_TAG, "Error with parsing register response into a JSON");
                    logger.error("Error with parsing register response into a JSON");
                } catch (Exception ex) {
                    ex.printStackTrace();
//                    Util.writeLocalLogEntryUsingLogback(context, LOG_TAG, ex.getLocalizedMessage());
                    logger.error(ex.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                String responseString = new String(errorResponse);
                try {
                    JSONObject joRes = new JSONObject(responseString);
                   JSONArray messages = joRes.getJSONArray("messages");
                    String firstMessage = (String) messages.get(0);
                    setErrorMessage(firstMessage);
//                    Log.i(LOG_TAG, firstMessage);
                    logger.info(firstMessage);
                }catch (Exception ex){
//                    Log.e(LOG_TAG, "Error with parsing register errorResponse into a JSON");
//                    Util.writeLocalLogEntryUsingLogback(context, LOG_TAG, "Error with parsing register errorResponse into a JSON");
                    logger.error("Error with parsing register errorResponse into a JSON");
                }
                loggedIn = false;
//                Log.e(LOG_TAG, "Error with getting response from server");
//                Util.writeLocalLogEntryUsingLogback(context, LOG_TAG, "Error with getting response from server");
                logger.error("Error with getting response from server");
            }
        });
        return loggedIn;
    }

    /**
     * Does a synchronous http request to upload the file and JSON Object to the server as an audio
     * recording. Can't be run on main/UI thread.
     *
     * @param audioFile recording.
     * @param data      metadata.
     * @return If upload was successful
     */
    static boolean uploadAudioRecording(File audioFile, JSONObject data, Context context) {
        Prefs prefs = new Prefs(context);

        if (audioFile == null || data == null) {
//            Log.e(LOG_TAG, "uploadAudioRecording: Invalid audioFile or JSONObject. Aborting upload");
//            Util.writeLocalLogEntryUsingLogback(context, LOG_TAG, "uploadAudioRecording: Invalid audioFile or JSONObject. Aborting upload");
            logger.error("uploadAudioRecording: Invalid audioFile or JSONObject. Aborting upload");
            return false;
        }

        // Check that there is a JWT (JSON Web Token)
        if (getToken() == null) {
            if (!login(context)) {
//                Log.w(LOG_TAG, "sendFile: no JWT. Aborting upload");
//                Util.writeLocalLogEntryUsingLogback(context, LOG_TAG, "sendFile: no JWT. Aborting upload");
                logger.error("sendFile: no JWT. Aborting upload");

                return false; // Can't upload without JWT, login/register device to get JWT.
            }
        }

        // Building POST request
        SyncHttpClient client = new SyncHttpClient();
        client.addHeader("Authorization", getToken());
        RequestParams params = new RequestParams();
        params.put("data", data.toString());
        try {
            params.put("file", audioFile);
        } catch (FileNotFoundException e) {
//            Log.e(LOG_TAG, "File not found, can't upload...");
//            Util.writeLocalLogEntryUsingLogback(context, LOG_TAG, "File not found, can't upload...");
            logger.error("File not found, can't upload...");
            return false;
        }

        if (uploading) {
//            Log.d(LOG_TAG, "Already uploading. Wait until last upload is finished.");
//            Util.writeLocalLogEntryUsingLogback(context, LOG_TAG, "Already uploading. Wait until last upload is finished.");
            logger.info("Already uploading. Wait until last upload is finished.");
            return false;
        }
        uploading = true;


        // Send request
       // boolean useTestServer = prefs.getUseTestServer();
//        client.post(prefs.getServerUrl(useTestServer) + UPLOAD_AUDIO_API_URL, params, new AsyncHttpResponseHandler() {
        client.post(prefs.getServerUrl() + UPLOAD_AUDIO_API_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
//                Log.i(LOG_TAG, "sendFile: onSuccess: Successful upload.");
                logger.info("Successful upload");
                uploadSuccess = true;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
//                Log.w(LOG_TAG, "sendFile: onSuccess: Failed upload.");
                logger.error("Failed upload.");

                uploadSuccess = false;
            }
        });
//        Log.d(LOG_TAG, "uploadAudioRecording: finished.");
    //    logger.info("uploadAudioRecording: finished.");
        uploading = false;

        return uploadSuccess;
    }

    static String getToken() {
        return token;
    }

    private static void setToken(String token) {
        Server.token = token;
    }

    private static void setErrorMessage(String errorMessage){
        Server.errorMessage = errorMessage;
    }

    static String getErrorMessage() {
        return errorMessage;
    }


//    static void sendMainActivityAMessage(Context context, String message){
//        // https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager
//
//        Intent intent = new Intent("event");
//        // You can also include some extra data.
//      //  intent.putExtra("message", "enable_vitals_button");
//        intent.putExtra("message", message);
//        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
//
//    }


}
