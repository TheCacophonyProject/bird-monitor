package nz.org.cacophony.cacophonometer;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import info.guardianproject.netcipher.NetCipher;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * This class deals with connecting to the server (test connection, Login, Register, upload recording).
 */

class Server {

    private static final String TAG = Server.class.getName();

    private static final String UPLOAD_AUDIO_API_URL = "/api/v1/recordings";
    private static final String LOGIN_URL = "/authenticate_device";
    private static final String LOGIN_USER_URL = "/authenticate_user";
    private static final String REGISTER_URL = "/api/v1/devices";
    private static final String SIGNUP_URL = "/api/v1/users";
    private static final String GROUPS_URL = "/api/v1/groups";


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
            String devicePassword = prefs.getDevicePassword();
            String group = prefs.getGroupName();


            if (devicename == null || devicePassword == null || group == null) {

                // One or more credentials are null, so can not attempt to login.
                Log.e(TAG, "No credentials to login with.");

                return false;
            }


            String serverUrl = prefs.getServerUrl() + LOGIN_URL;
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("devicename", devicename);
            jsonParam.put("password", devicePassword);

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

                    Log.i(TAG, "Successful login."); prefs.setDeviceToken(joRes.getString("token"));
                    Log.d(TAG, "Web token has been refreshed");
                    prefs.setTokenLastRefreshed(new Date().getTime());

                } else { // not success
                    prefs.setDeviceToken(null);
                    Util.broadcastAMessage(context, "untick_logged_in_to_server");
                }

            } else { // STATUS not OK
                Log.e(TAG, "Invalid devicename or password for login.");
                prefs.setDeviceToken(null);
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

    static void loginUser(Context context) {
        final Prefs prefs = new Prefs(context);

        try {
            String messageToDisplay = "";
            JSONObject jsonObjectMessageToBroadcast = new JSONObject();
            jsonObjectMessageToBroadcast.put("activityName", "SigninActivity");

            Util.disableFlightMode(context);

            // Now wait for network connection as setFlightMode takes a while
            if (!Util.waitForNetworkConnection(context, true)) {
                Log.e(TAG, "Failed to disable airplane mode");
                jsonObjectMessageToBroadcast.put("responseCode", -1);
                jsonObjectMessageToBroadcast.put("activityName", "SigninActivity");
                messageToDisplay = "Unable to get an internet connection";
                jsonObjectMessageToBroadcast.put("messageToDisplay", messageToDisplay);
                Util.broadcastAMessage(context,jsonObjectMessageToBroadcast.toString());
                return;
            }

            String userName = prefs.getUsername();
            String usernameOrEmailAddress = prefs.getUserNameOrEmailAddress();
            String userNamePassword = prefs.getUsernamePassword();

            if (usernameOrEmailAddress == null){
                usernameOrEmailAddress = userName;
            }



            if (usernameOrEmailAddress == null || userNamePassword == null ) {

                // One or more credentials are null, so can not attempt to login.
                Log.e(TAG, "No credentials to login with.");
                jsonObjectMessageToBroadcast.put("responseCode", -1);
                jsonObjectMessageToBroadcast.put("activityName", "SigninActivity");
                messageToDisplay = "Error: Username/email address or password can not be missing";
                jsonObjectMessageToBroadcast.put("messageToDisplay", messageToDisplay);
                Util.broadcastAMessage(context,jsonObjectMessageToBroadcast.toString());
                return ;
            }


            String serverUrl = prefs.getServerUrl() + LOGIN_USER_URL;
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("nameOrEmail", usernameOrEmailAddress);
            jsonParam.put("password", userNamePassword);

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

                    Log.i(TAG, "Successful authentication.");
                    String userToken = joRes.getString("token");
                    prefs.setUserToken(userToken);
                    Log.d(TAG, "User Web token has been refreshed");
                    prefs.setTokenLastRefreshed(new Date().getTime());

                    jsonObjectMessageToBroadcast.put("responseCode", 200);
                    jsonObjectMessageToBroadcast.put("activityName", "SigninActivity");
                    messageToDisplay = "Success, you have successfully signed in";
                    jsonObjectMessageToBroadcast.put("messageToDisplay", messageToDisplay);
                    Util.broadcastAMessage(context,jsonObjectMessageToBroadcast.toString());

                } else { // not success
                    prefs.setUserToken(null);
                    jsonObjectMessageToBroadcast.put("responseCode", responseCode);
                    jsonObjectMessageToBroadcast.put("activityName", "SigninActivity");
                    messageToDisplay = "Error, unable to sign in";
                    jsonObjectMessageToBroadcast.put("messageToDisplay", messageToDisplay);
                    Util.broadcastAMessage(context,jsonObjectMessageToBroadcast.toString());
                }

            } else { // STATUS not OK
                Log.e(TAG, "Invalid user for login.");
                prefs.setUserToken(null);
                jsonObjectMessageToBroadcast.put("responseCode", responseCode);
                jsonObjectMessageToBroadcast.put("activityName", "SigninActivity");
                messageToDisplay = "Error, unable to sign in";
                jsonObjectMessageToBroadcast.put("messageToDisplay", messageToDisplay);
                Util.broadcastAMessage(context,jsonObjectMessageToBroadcast.toString());
            }

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
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

    private static HttpURLConnection openURLGet(String serverUrl) {
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

            conn.setRequestMethod("GET");
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

                    prefs.setDeviceToken(joRes.getString("token"));
                    prefs.setTokenLastRefreshed(new Date().getTime());

                    // look at web token

                    String deviceID = Util.getDeviceID(prefs.getToken());
                    prefs.setDeviceId(deviceID);

                 //   prefs.setDeviceName(devicename);
                    prefs.setDeviceName(deviceName);
                    prefs.setGroupName(group);
                    prefs.setDevicePassword(password);

                    prefs.setDeviceId(deviceID);
                    Util.broadcastAMessage(context,"REGISTER_SUCCESS");
                } else {
                    // Failed register.
                    Log.w(TAG, "Failed to register");
                    registered = false;
                    Util.broadcastAMessage(context,"REGISTER_FAIL_UNKNOWN_REASON");
                }
            } else if (responseCode.equalsIgnoreCase("422")){ // 422 error response

                Log.w(TAG, "Register Response from server is 422");
                JSONObject joRes = new JSONObject(responseString);
               String errorType = joRes.getString("errorType");
                String message = joRes.getString("message");


                setErrorMessage( message);
                Log.i(TAG, message);
                registered = false;
                Util.broadcastAMessage(context, message);
            } else { // response code not 200 or 422 - left this here from Cameron's code as it
                    // didn't work with 422 response, but not sure if needed for other responses
                Log.w(TAG, "Register Response from server not 200");
                JSONObject joRes = new JSONObject(responseString);
                JSONArray messages = joRes.getJSONArray("messages");
                String firstMessage = (String) messages.get(0);
                setErrorMessage("NEITHER_200_NOR_422" + firstMessage);
                Log.i(TAG, firstMessage);
                registered = false;
                Util.broadcastAMessage(context,firstMessage);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return registered;
    }

    static boolean signUp(final String username, final String emailAddress, final String usernamePassword, final Context context) {

        boolean signedUp = false;
        final Prefs prefs = new Prefs(context);

        // https://stackoverflow.com/questions/42767249/android-post-request-with-json
        String registerUrl = prefs.getServerUrl() + SIGNUP_URL;

        try {
            HttpURLConnection myConnection = openURL(registerUrl);

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("username", username);
            jsonParam.put("password", usernamePassword);
            jsonParam.put("email", emailAddress);
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
            JSONObject joResponseString = new JSONObject(responseString);
            String messageToDisplay = "";
            JSONObject jsonObjectMessageToBroadcast = new JSONObject();
            jsonObjectMessageToBroadcast.put("activityName", "SignupActivity");

            if (responseCode.equalsIgnoreCase("200")) {


                if (joResponseString.getBoolean("success")) {
                    signedUp = true;

                    prefs.setUserToken(joResponseString.getString("token"));
                    prefs.setUserTokenLastRefreshed(new Date().getTime());
                    prefs.setUsername(username);
                    prefs.setUsernamePassword(usernamePassword);
                    prefs.setEmailAddress(emailAddress);

                    jsonObjectMessageToBroadcast.put("responseCode", 200);
                    messageToDisplay = "Success, you have successfully signed up";
                    jsonObjectMessageToBroadcast.put("messageToDisplay", messageToDisplay);
                    Util.broadcastAMessage(context,jsonObjectMessageToBroadcast.toString());
                } else {
                    // Failed register.
                    Log.w(TAG, "Failed to signup");
                    signedUp = false;
                    jsonObjectMessageToBroadcast.put("responseCode", responseCode);
                    messageToDisplay = "Sorry - failed to sign up";
                    jsonObjectMessageToBroadcast.put("messageToDisplay", messageToDisplay);
                    Util.broadcastAMessage(context,jsonObjectMessageToBroadcast.toString());
                }
            } else if (responseCode.equalsIgnoreCase("422")){ // 422 error response
                Log.w(TAG, "Signup Response from server is 422");


                jsonObjectMessageToBroadcast.put("responseCode", responseCode);
                String errorType = joResponseString.getString("errorType");
                String message = joResponseString.getString("message");

                if (errorType.equalsIgnoreCase("validation")){
                    if (message.equalsIgnoreCase("email: email in use")){
                        messageToDisplay = "Sorry this email address is already being used.";
                    }else if (message.equalsIgnoreCase("email: Invalid value")){
                        messageToDisplay = "Sorry this email address is not valid.";
                    }else{
                        messageToDisplay = "Sorry there is a problem with email address";
                    }
                }
                jsonObjectMessageToBroadcast.put("messageToDisplay", messageToDisplay);
                Util.broadcastAMessage(context,jsonObjectMessageToBroadcast.toString());
            }else { // 422 error response
                Log.w(TAG, "Signup Response from server is " + responseCode);

                jsonObjectMessageToBroadcast.put("responseCode", responseCode);
                String errorType = joResponseString.getString("errorType");
                String message = joResponseString.getString("message");
                messageToDisplay = "Unable to sign up (ErrorType is " + errorType + " Message is " + message; // needs improving, find out what the other error codes might be?
                jsonObjectMessageToBroadcast.put("messageToDisplay", messageToDisplay);
                Util.broadcastAMessage(context,jsonObjectMessageToBroadcast.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return signedUp;
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

    static ArrayList<String> getGroups(Context context) {
        final Prefs prefs = new Prefs(context);

        ArrayList<String> groups = new ArrayList<String>();
        try {
            OkHttpClient client = new OkHttpClient();

            String url = "https://api-test.cacophony.org.nz/api/v1/groups?where={}";

            String authorization = prefs.getUserToken();
            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", authorization)
                    .build();
            String responseBody = "";

            Response  response = client.newCall(request).execute();
            int responseCode = response.code();
            Log.e(TAG,"responseCode: " + responseCode);

            //Set message to broadcast
            String messageToDisplay = "";
            JSONObject jsonObjectMessageToBroadcast = new JSONObject();
            jsonObjectMessageToBroadcast.put("activityName", "GroupActivity");
            jsonObjectMessageToBroadcast.put("responseCode", responseCode); // need to check this

            if (responseCode == 200){
                responseBody = response.body().string();

                // Get groups from responseBody
                JSONObject joRes = new JSONObject(responseBody);

                if (joRes.getBoolean("success")) {
                    JSONArray groupsJSONArray = joRes.getJSONArray("groups");
                    if (groupsJSONArray != null){
                        for (int i=0;i<groupsJSONArray.length();i++){
                            JSONObject groupJSONObject = new JSONObject(groupsJSONArray.getString(i));
                            String groupName = groupJSONObject.getString("groupname");
                            groups.add(groupName);
                        }
                    }

                    messageToDisplay = "Success, groups have been updated from server";
                    jsonObjectMessageToBroadcast.put("messageToDisplay", messageToDisplay);
                }

            } else { // not success

                messageToDisplay = "Error, unabale to get groups from server";
                jsonObjectMessageToBroadcast.put("messageToDisplay", messageToDisplay);
            }
            Util.broadcastAMessage(context,jsonObjectMessageToBroadcast.toString());

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }

        return groups;
    }



    static ArrayList<String> getGroups3(Context context) {
        final Prefs prefs = new Prefs(context);

        ArrayList<String> groups  = new ArrayList<String>();
        try{
        //String groupsUrl = prefs.getServerUrl() + GROUPS_URL;
        String query = "{}";

            String param1After = URLEncoder.encode(query, "UTF-8");
            Log.e(TAG, param1After);

       // String groupsUrl = "https://api-test.cacophony.org.nz/api/v1/groups?where={}";
        String groupsUrl = "https://api-test.cacophony.org.nz/api/v1/groups?where=" + param1After;
            Log.e(TAG, groupsUrl);

            HttpURLConnection conn = openURLGet(groupsUrl);
            String authorization = prefs.getUserToken();
            Log.e(TAG, authorization);
            conn.setRequestProperty("authorization", authorization);

            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            // conn.setRequestMethod("POST");
//            conn.setDoInput(true);
//            conn.setDoOutput(true);

//            Uri.Builder builder = new Uri.Builder()
//                    .appendQueryParameter("where", "{}");
//
//            String query = builder.build().getEncodedQuery();
//
//            OutputStream os = conn.getOutputStream();
//            BufferedWriter writer = new BufferedWriter(
//                    new OutputStreamWriter(os, "UTF-8"));
//            writer.write(query);
//            writer.flush();
//            writer.close();
//            os.close();

            conn.connect();

            if (conn.getResponseCode() != 200) {
                int responseCode = conn.getResponseCode();
                Log.e(TAG, "responseCode is: " + responseCode);
//                throw new RuntimeException("Failed : HTTP error code : "
//                        + conn.getResponseCode());
            }

            String assembledOutput = "";

            BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            System.out.println("Output from Server:\n");
            while ((output = responseBuffer.readLine()) != null) {
                System.out.println(output);
                assembledOutput = assembledOutput + output;
            }

            conn.disconnect();

        }catch(Exception ex){
            Log.e(TAG, ex.getLocalizedMessage());
        }

        return groups;
    }


    static ArrayList<String> getGroups2(Context context) {
        final Prefs prefs = new Prefs(context);
        ArrayList<String> groups  = new ArrayList<String>();
        String groupsUrl = prefs.getServerUrl() + GROUPS_URL;
        try{
            HttpURLConnection conn = openURLGet(groupsUrl);
            String authorization = prefs.getUserToken();
            Log.e(TAG, authorization);
            conn.setRequestProperty("authorization", authorization);

            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
           // conn.setRequestMethod("POST");
//            conn.setDoInput(true);
//            conn.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("where", "{}");

            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            conn.connect();

            if (conn.getResponseCode() != 200) {
                int responseCode = conn.getResponseCode();
                Log.e(TAG, "responseCode is: " + responseCode);
//                throw new RuntimeException("Failed : HTTP error code : "
//                        + conn.getResponseCode());
            }

            String assembledOutput = "";

            BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            System.out.println("Output from Server:\n");
            while ((output = responseBuffer.readLine()) != null) {
                System.out.println(output);
                assembledOutput = assembledOutput + output;
            }

            conn.disconnect();

        }catch(Exception ex){
            Log.e(TAG, ex.getLocalizedMessage());
        }

        return groups;
    }

    static ArrayList<String> getGroupsOld(Context context) {
        final Prefs prefs = new Prefs(context);
        ArrayList<String> groups  = new ArrayList<String>();
//        String groupsUrl = prefs.getServerUrl() + GROUPS_URL;
        String groupsUrl = "https://api-test.cacophony.org.nz/api/v1/groups?where={}";
        String charset = "UTF-8";

        try {
            HttpURLConnection conn = openURLGet(groupsUrl);
            String authorization = prefs.getUserToken();
            Log.e(TAG, authorization);
            conn.setRequestProperty("authorization", authorization);

            JSONObject jsonParam = new JSONObject();

            try {
//                String whereClause = "{}";
//                jsonParam.put("where", whereClause);
//
//                String jsonParamString = jsonParam.toString();
////                String jsonParamString = "where: {}";

//            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
//            os.writeBytes(jsonParamString);
//            os.flush();

            conn.connect();

            if (conn.getResponseCode() != 200) {
                int responseCode = conn.getResponseCode();
                Log.e(TAG, "responseCode is: " + responseCode);
//                throw new RuntimeException("Failed : HTTP error code : "
//                        + conn.getResponseCode());
            }

            String assembledOutput = "";

            BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            System.out.println("Output from Server:\n");
            while ((output = responseBuffer.readLine()) != null) {
                System.out.println(output);
                assembledOutput = assembledOutput + output;
            }

            conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }
//            //  Here you read any answer from server.
//            if (conn == null){
//                Log.e(TAG, "conn is null");
//                return null;
//            }
//            Log.i(TAG, "SERVER REPLIED:");
//            try {
//                uploadSuccess = false;
//                for (String line : responseString) {
//                    JSONObject joRes = new JSONObject(line);
//                   Log.e(TAG,joRes.toString() );
//
//                }
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }finally {
            uploading = false;
        }
        return groups;
    }
}