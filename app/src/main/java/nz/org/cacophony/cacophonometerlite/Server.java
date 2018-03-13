package nz.org.cacophony.cacophonometerlite;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import info.guardianproject.netcipher.NetCipher;

/**
 * This class deals with connecting to the server (test connection, Login, Register, upload recording).
 */

class Server {


    private static final String TAG = Server.class.getName();

    private static final String UPLOAD_AUDIO_API_URL = "/api/v1/audiorecordings";
    //private static final String PING_URL = "/ping";
    private static final String LOGIN_URL = "/authenticate_device";
    private static final String REGISTER_URL = "/api/v1/devices";

    static boolean serverConnection = false;
  //  static boolean loggedIn = false;  // going to use the presence of an uptodate webtoken instead
  //  private static String token = null;
    private static String errorMessage = null;
    private static boolean uploading = false;
    private static boolean uploadSuccess = false;


    static void updateServerConnectionStatus(Context context) {

        try {
            Util.disableFlightMode(context);
         //   Util.disableFlightModeTestSU(context);
            // Now wait for network connection as setFlightMode takes a while
            if (!Util.waitForNetworkConnection(context, true)) {
                Log.e(TAG, "Failed to disable airplane mode");
                return;
            }

            login(context);
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        } finally {

            Util.broadcastAMessage(context, "refresh_vitals_displayed_text");
            Util.broadcastAMessage(context, "enable_vitals_button");
//            Util.broadcastAMessage(context, "enable_test_recording_button");
//            Util.broadcastAMessage(context, "enable_setup_button");
        }
    }


    static boolean login(Context context) {
        final Prefs prefs = new Prefs(context);
        try {
            Util.disableFlightMode(context);
        //    Util.disableFlightModeTestSU(context);


            // Now wait for network connection as setFlightMode takes a while
            if (!Util.waitForNetworkConnection(context, true)) {
                Log.e(TAG, "Failed to disable airplane mode");
                return false;
            }

//            final Prefs prefs = new Prefs(context);
            String devicename = prefs.getDeviceName();
            String password = prefs.getPassword();
            String group = prefs.getGroupName();


            if (devicename == null || password == null || group == null) {

                // One or more credentials are null, so can not attempt to login.
                Log.e(TAG, "No credentials to login with.");
             //   loggedIn = false;
//                return loggedIn;
                return false;
            }


            String serverUrl = prefs.getServerUrl() + LOGIN_URL;
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("devicename", devicename);
            jsonParam.put("password", password);

            HttpsURLConnection myConnection = getHttpsURLConnection(serverUrl);

            DataOutputStream os = new DataOutputStream(myConnection.getOutputStream());
            os.writeBytes(jsonParam.toString());
            os.flush();

            //  Here you read any answer from server.
            BufferedReader serverAnswer = new BufferedReader(new InputStreamReader(myConnection.getInputStream()));
            String responseLine;

            responseLine = serverAnswer.readLine();
            os.close();
            serverAnswer.close();

            //    Log.i("STATUS", String.valueOf(myConnection.getResponseCode()));
            //  String status = String.valueOf(myConnection.getResponseCode());
            Log.i("MSG", myConnection.getResponseMessage());
            String responseCode = String.valueOf(myConnection.getResponseCode());

            myConnection.disconnect();
            //   if (status.equalsIgnoreCase("OK")) {
            if (responseCode.equalsIgnoreCase("200")) {


                JSONObject joRes = new JSONObject(responseLine);
                if (joRes.getBoolean("success")) {


                    Log.i(TAG, "Successful login.");
//                        logger.info("Login", "Successful login.");
               //     loggedIn = true;
                  //  setToken(joRes.getString("token"));  // Save JWT (JSON Web Token) // 8/12/17  Store token in prefs instead, as Server.token is not kept
                    prefs.setToken(joRes.getString("token"));
                    Log.d(TAG, "Web token has been refreshed");
                    prefs.setTokenLastRefreshed(new Date().getTime());


                } else { // not success
                //    loggedIn = false;
                    //setToken(null);
                    prefs.setToken(null);
                    Util.broadcastAMessage(context, "untick_logged_in_to_server");
                }

            } else { // STATUS not OK
            //    loggedIn = false;
                Log.e(TAG, "Invalid devicename or password for login.");
               // setToken(null);
                prefs.setToken(null);
            }

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
        Util.broadcastAMessage(context, "refresh_vitals_displayed_text");
        if (prefs.getToken() == null){
            return false;
        }else {
            return true;
        }
       // return loggedIn;
    }


    private static HttpsURLConnection getHttpsURLConnection(String serverUrl) {
        URL cacophonyRegisterEndpoint = null;
        HttpsURLConnection myConnection = null;
        try {
            cacophonyRegisterEndpoint = new URL(serverUrl);
            // Create connection

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                //https://stackoverflow.com/questions/26633349/disable-ssl-as-a-protocol-in-httpsurlconnection
                myConnection = NetCipher.getHttpsURLConnection(cacophonyRegisterEndpoint);
            } else {
                myConnection = (HttpsURLConnection) cacophonyRegisterEndpoint.openConnection();
            }

            myConnection.setRequestMethod("POST");
            myConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            myConnection.setRequestProperty("Accept", "application/json");
            myConnection.setDoOutput(true);
            myConnection.setDoInput(true);

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
        return myConnection;
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
            Log.i(TAG, "Invalid group name: " + group);
            return false;
        }

        boolean registered = false;
        final Prefs prefs = new Prefs(context);

        // https://stackoverflow.com/questions/42767249/android-post-request-with-json
        String registerUrl = prefs.getServerUrl() + REGISTER_URL;
        URL cacophonyRegisterEndpoint = null;
        try {
            HttpsURLConnection myConnection = getHttpsURLConnection(registerUrl);


            myConnection.setRequestMethod("POST");
            myConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            myConnection.setRequestProperty("Accept", "application/json");
            myConnection.setDoOutput(true);
            myConnection.setDoInput(true);

            final String devicename = RandomStringUtils.random(20, true, true);
            final String password = RandomStringUtils.random(20, true, true);

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("devicename", devicename);
            jsonParam.put("password", password);
            jsonParam.put("group", group);
            DataOutputStream os = new DataOutputStream(myConnection.getOutputStream());
            os.writeBytes(jsonParam.toString());
            os.flush();

            //  Here you read any answer from server.
            BufferedReader serverAnswer = new BufferedReader(new InputStreamReader(myConnection.getInputStream()));
            String responseLine;

            responseLine = serverAnswer.readLine();
            os.close();
            serverAnswer.close();

//        Log.i("STATUS",String.valueOf(myConnection.getResponseCode()));
//        String status=String.valueOf(myConnection.getResponseCode());
            Log.i("MSG", myConnection.getResponseMessage());
            String responseCode = String.valueOf(myConnection.getResponseCode());

            myConnection.disconnect();
            String responseString = new String(responseLine);

            if (responseCode.equalsIgnoreCase("200")) {


                JSONObject joRes = new JSONObject(responseString);
                if (joRes.getBoolean("success")) {
                    registered = true;
                  //  setToken(joRes.getString("token"));
                    prefs.setToken(joRes.getString("token"));
                    prefs.setTokenLastRefreshed(new Date().getTime());

                    // look at web token
//                    String deviceID = Util.getDeviceID(context, getToken());
                    String deviceID = Util.getDeviceID(context, prefs.getToken());
                    prefs.setDeviceId(deviceID);

                    prefs.setDeviceName(devicename);
                    prefs.setGroupName(group);
                    prefs.setPassword(password);

                    prefs.setDeviceId(deviceID);
                } else {
                    // Failed register.
                    Log.w(TAG, "Failed to register");
                    registered = false;
                }
            } else { // response code not 200
                Log.w(TAG, "Register Response from server not 200");
                JSONObject joRes = new JSONObject(responseString);
                JSONArray messages = joRes.getJSONArray("messages");
                String firstMessage = (String) messages.get(0);
                setErrorMessage(firstMessage);
                Log.i(TAG, firstMessage);
                registered = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return registered;
    }

    static boolean uploadAudioRecording(File audioFile, JSONObject data, Context context) {
        // http://www.codejava.net/java-se/networking/upload-files-by-sending-multipart-request-programmatically
        if (uploading) {
            Log.i(TAG, "Already uploading. Wait until last upload is finished.");
//            Util.writeLocalLogEntryUsingLogback(context, LOG_TAG, "Already uploading. Wait until last upload is finished.");
//            logger.info("Already uploading. Wait until last upload is finished.");
            Util.broadcastAMessage(context, "already_uploading");
            return false;
        }
        uploading = true;

        String charset = "UTF-8";

        Prefs prefs = new Prefs(context);
        String uploadUrl = prefs.getServerUrl() + UPLOAD_AUDIO_API_URL;
        try {
            MultipartUtility multipart = new MultipartUtility(uploadUrl, charset, prefs.getToken());


            multipart.addFormField("data", data.toString());
            multipart.addFilePart("file", audioFile);


            List<String> responseString = multipart.finish();


            Log.i(TAG, "SERVER REPLIED:");
            try {
                uploadSuccess = false;
                for (String line : responseString) {
                    JSONObject joRes = new JSONObject(line);

                    if (joRes.getBoolean("success")) {
                        uploadSuccess = true;
                        break;
                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (IOException ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }finally {
            uploading = false;
        }
       // uploading = false;
        return uploadSuccess;
    }

//    static String getToken() {
//        return token;
//
//    }

//    private static void setToken(String token) {
//        Server.token = token;
//    }

    private static void setErrorMessage(String errorMessage) {
        Server.errorMessage = errorMessage;
    }

    static String getErrorMessage() {
        return errorMessage;
    }


}
