package nz.org.cacophony.cacophonometer;

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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;

import info.guardianproject.netcipher.NetCipher;


/**
 * This class deals with connecting to the server (test connection, Login, Register, upload recording).
 */

class Server {

    private static final String TAG = Server.class.getName();

   // private static final String UPLOAD_AUDIO_API_URL = "/api/v1/audiorecordings";
    private static final String UPLOAD_AUDIO_API_URL = "/api/v1/recordings";
    private static final String LOGIN_URL = "/authenticate_device";
    private static final String REGISTER_URL = "/api/v1/devices";

    // static boolean serverConnection = false;
    private static String errorMessage = null;
    private static boolean uploading = false;
    private static boolean uploadSuccess = false;


    static void updateServerConnectionStatus(Context context) {

        try {
            Util.disableFlightMode(context);

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
        }
    }


    static boolean login(Context context) {
        final Prefs prefs = new Prefs(context);
        try {
            Util.disableFlightMode(context);

            // Now wait for network connection as setFlightMode takes a while
            if (!Util.waitForNetworkConnection(context, true)) {
                Log.e(TAG, "Failed to disable airplane mode");
                return false;
            }

            String devicename = prefs.getDeviceName();
            String password = prefs.getPassword();
            String group = prefs.getGroupName();


            if (devicename == null || password == null || group == null) {

                // One or more credentials are null, so can not attempt to login.
                Log.e(TAG, "No credentials to login with.");

                return false;
            }


            String serverUrl = prefs.getServerUrl() + LOGIN_URL;
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("devicename", devicename);
            jsonParam.put("password", password);

            HttpURLConnection myConnection = openURL(serverUrl);

            DataOutputStream os = new DataOutputStream(myConnection.getOutputStream());
            os.writeBytes(jsonParam.toString());
            os.flush();

            //  Here you read any answer from server.
            BufferedReader serverAnswer = new BufferedReader(new InputStreamReader(myConnection.getInputStream()));
            String responseLine;

            responseLine = serverAnswer.readLine();
            os.close();
            serverAnswer.close();

             Log.i("MSG", myConnection.getResponseMessage());
            String responseCode = String.valueOf(myConnection.getResponseCode());

            myConnection.disconnect();

            if (responseCode.equalsIgnoreCase("200")) {

                JSONObject joRes = new JSONObject(responseLine);
                if (joRes.getBoolean("success")) {

                    Log.i(TAG, "Successful login."); prefs.setToken(joRes.getString("token"));
                    Log.d(TAG, "Web token has been refreshed");
                    prefs.setTokenLastRefreshed(new Date().getTime());

                } else { // not success
                    prefs.setToken(null);
                    Util.broadcastAMessage(context, "untick_logged_in_to_server");
                }

            } else { // STATUS not OK
                Log.e(TAG, "Invalid devicename or password for login.");
                prefs.setToken(null);
            }

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
        Util.broadcastAMessage(context, "refresh_vitals_displayed_text");
        //noinspection RedundantIfStatement
        if (prefs.getToken() == null){
            return false;
        }else {
            return true;
        }



    }

    private static HttpURLConnection openURL(String serverUrl) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(serverUrl);
            switch (url.getProtocol()) {
                case "http":
                    conn = (HttpURLConnection) url.openConnection();
                    break;
                case "https":
                    conn = openHttpsURL(url);
                    break;
                default:
                    throw new IllegalArgumentException("unsupported protocol");
            }

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }

        return conn;
    }

    private static HttpURLConnection openHttpsURL(URL url) throws IOException {
        // Create connection
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
        if ( Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            //https://stackoverflow.com/questions/26633349/disable-ssl-as-a-protocol-in-httpsurlconnection
            return NetCipher.getHttpsURLConnection(url);
        }
        return (HttpURLConnection) url.openConnection();
    }

    /**
     * Does a synchronous http request to register the device. Can't be run on main/UI thread.
     *
     * @param group   Name of group to register under.
     * @param context App context.
     * @return If the device successfully registered.
     */
    @SuppressWarnings("RedundantStringConstructorCall")
    static boolean register(final String group, final String deviceName,final Context context) {

        // Check that the group name is valid, at least 4 characters.
        if (group == null || group.length() < 4) {
            Log.i(TAG, "Invalid group name: " + group);
            return false;
        }

        boolean registered = false;
        final Prefs prefs = new Prefs(context);

        // https://stackoverflow.com/questions/42767249/android-post-request-with-json
        String registerUrl = prefs.getServerUrl() + REGISTER_URL;
    //    URL cacophonyRegisterEndpoint = null;
        try {
            HttpURLConnection myConnection = openURL(registerUrl);

         //   final String devicename = RandomStringUtils.random(20, true, true);
            final String password = RandomStringUtils.random(20, true, true);

            JSONObject jsonParam = new JSONObject();
         //   jsonParam.put("devicename", devicename);
            jsonParam.put("devicename", deviceName);
            jsonParam.put("password", password);
            jsonParam.put("group", group);
            DataOutputStream os = new DataOutputStream(myConnection.getOutputStream());
            os.writeBytes(jsonParam.toString());
            os.flush();


                      //  Here you read any answer from server.
            if (myConnection == null){
                Log.e(TAG, "myConnection is null");
                return false;
            }


            Log.i("MSG", myConnection.getResponseMessage());
            String responseCode = String.valueOf(myConnection.getResponseCode());
            // 26 Sept 2018
            // If the device name is already in use, the server returns a 422, and attempting to read
            // the normal stream with getInputStream throws a FileNotFoundException, so instead
            // read getErrorStream
            // https://developer.android.com/reference/java/net/HttpURLConnection.html#getErrorStream()
            BufferedReader serverAnswer;
            if (responseCode.equalsIgnoreCase("422")) {
                serverAnswer = new BufferedReader(new InputStreamReader(myConnection.getErrorStream()));
            }else{
                serverAnswer = new BufferedReader(new InputStreamReader(myConnection.getInputStream()));
            }
            String responseLine;

            responseLine = serverAnswer.readLine();
            os.close();
            serverAnswer.close();


            myConnection.disconnect();
            String responseString = new String(responseLine);

            if (responseCode.equalsIgnoreCase("200")) {

                JSONObject joRes = new JSONObject(responseString);
                if (joRes.getBoolean("success")) {
                    registered = true;

                    prefs.setToken(joRes.getString("token"));
                    prefs.setTokenLastRefreshed(new Date().getTime());

                    // look at web token

                    String deviceID = Util.getDeviceID(prefs.getToken());
                    prefs.setDeviceId(deviceID);

                 //   prefs.setDeviceName(devicename);
                    prefs.setDeviceName(deviceName);
                    prefs.setGroupName(group);
                    prefs.setPassword(password);

                    prefs.setDeviceId(deviceID);
                } else {
                    // Failed register.
                    Log.w(TAG, "Failed to register");
                    registered = false;
                }
            } else if (responseCode.equalsIgnoreCase("422")){ // 422 error response

                Log.w(TAG, "Register Response from server is 422");
                JSONObject joRes = new JSONObject(responseString);
               String errorType = joRes.getString("errorType");
                String message = joRes.getString("message");


                setErrorMessage(message);
                Log.i(TAG, message);
                registered = false;
            } else { // response code not 200 or 422 - left this here from Cameron's code as it
                    // didn't work with 422 response, but not sure if needed for other responses
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
            Util.broadcastAMessage(context, "already_uploading");
            return false;
        }
        uploading = true;

        String charset = "UTF-8";

        Prefs prefs = new Prefs(context);
        String uploadUrl = prefs.getServerUrl() + UPLOAD_AUDIO_API_URL;
        try {
            HttpURLConnection conn = openURL(uploadUrl);

            MultipartUtility multipart = new MultipartUtility(conn, charset, prefs.getToken());

            multipart.addFormField("data", data.toString());
            multipart.addFilePart("file", audioFile);

            List<String> responseString = multipart.finish();

            Log.i(TAG, "SERVER REPLIED:");
            try {
                uploadSuccess = false;
                for (String line : responseString) {
                    JSONObject joRes = new JSONObject(line);
                    long recordingId = joRes.getLong("recordingId");

                    prefs.setLastRecordIdReturnedFromServer(recordingId);
                  long check =  prefs.getLastRecordIdReturnedFromServer();
                  if (recordingId != check) {
                        Log.e(TAG, "Error with recording id");
                    }


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



    private static void setErrorMessage(String errorMessage) {
        Server.errorMessage = errorMessage;
    }

    static String getErrorMessage() {
        return errorMessage;
    }


}