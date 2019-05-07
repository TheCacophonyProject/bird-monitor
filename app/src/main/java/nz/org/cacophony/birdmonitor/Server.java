package nz.org.cacophony.birdmonitor;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import info.guardianproject.netcipher.NetCipher;
import okhttp3.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static nz.org.cacophony.birdmonitor.IdlingResourceForEspressoTesting.uploadFilesIdlingResource;


/**
 * This class deals with connecting to the server (test connection, Login, Register, upload recording).
 */

class Server {

    private static final String TAG = Server.class.getName();

    private static final int HTTP_422_UNPROCESSABLE_ENTITY = 422;

    private static final String SERVER_GROUPS_ACTION = "SERVER_GROUPS";

    private static final String UPLOAD_AUDIO_API_URL = "/api/v1/recordings";
    private static final String LOGIN_URL = "/authenticate_device";
    private static final String LOGIN_USER_URL = "/authenticate_user";
    private static final String REGISTER_URL = "/api/v1/devices";
    private static final String SIGNUP_URL = "/api/v1/users";
    private static final String GROUPS_URL = "api/v1/groups"; // deliberately don't have leading / https://square.github.io/okhttp/3.x/okhttp/ says not to have it
    private static final OkHttpClient client = new OkHttpClient();
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
            Log.e(TAG, ex.getLocalizedMessage(), ex);
        } finally {

            JSONObject jsonObjectMessageToBroadcast = new JSONObject();
            try {
                jsonObjectMessageToBroadcast.put("messageType", "refresh_vitals_displayed_text");
                Util.broadcastAMessage(context, "SERVER_CONNECTION", jsonObjectMessageToBroadcast);

                jsonObjectMessageToBroadcast.put("messageType", "enable_vitals_button");
                Util.broadcastAMessage(context, "SERVER_CONNECTION", jsonObjectMessageToBroadcast);

            } catch (Exception ex) {
                Log.e(TAG, ex.getLocalizedMessage(), ex);
            }
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


            String loginUrl = prefs.getServerUrl() + LOGIN_URL;

            RequestBody requestBody = new FormBody.Builder()
                    .add("devicename", devicename)
                    .add("password", devicePassword)
                    .build();
            PostResponse postResponse = makePost(loginUrl, requestBody);
            Response response = postResponse.response;
            JSONObject responseJson = postResponse.responseJson;


            if (response.code() == 200) {

                if (responseJson.getBoolean("success")) {

                    Log.i(TAG, "Successful login.");
                    prefs.setDeviceToken(responseJson.getString("token"));
                    Log.d(TAG, "Web token has been refreshed");
                    prefs.setTokenLastRefreshed(new Date().getTime());

                } else { // not success
                    prefs.setDeviceToken(null);

                    JSONObject jsonObjectMessageToBroadcast = new JSONObject();
                    jsonObjectMessageToBroadcast.put("messageType", "untick_logged_in_to_server");
                    Util.broadcastAMessage(context, "SERVER_DEVICE_LOGIN", jsonObjectMessageToBroadcast);
                }

            } else { // STATUS not OK
                Log.e(TAG, "Invalid devicename or password for login.");
                prefs.setDeviceToken(null);
            }

        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
        JSONObject jsonObjectMessageToBroadcast = new JSONObject();
        try {
            jsonObjectMessageToBroadcast.put("messageType", "refresh_vitals_displayed_text");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Util.broadcastAMessage(context, "SERVER_LOGIN", jsonObjectMessageToBroadcast);
        //noinspection RedundantIfStatement
        if (prefs.getToken() == null) {
            return false;
        } else {
            return true;
        }
    }

    static void loginUser(Context context) {

        final Prefs prefs = new Prefs(context);

        try {
            Util.disableFlightMode(context);

            // Now wait for network connection as setFlightMode takes a while
            if (!Util.waitForNetworkConnection(context, true)) {
                Log.e(TAG, "Failed to disable airplane mode");
                JSONObject jsonObjectMessageToBroadcast = new JSONObject();
                jsonObjectMessageToBroadcast.put("responseCode", -1);
                jsonObjectMessageToBroadcast.put("messageType", "NETWORK_ERROR");
                String messageToDisplay = "Unable to get an internet connection";
                jsonObjectMessageToBroadcast.put("messageToDisplay", messageToDisplay);

                Util.broadcastAMessage(context, "SERVER_USER_LOGIN", jsonObjectMessageToBroadcast);

                return;
            }

            String userName = prefs.getUsername();
            String usernameOrEmailAddress = prefs.getUserNameOrEmailAddress();
            String userNamePassword = prefs.getUsernamePassword();

            if (usernameOrEmailAddress == null) {
                usernameOrEmailAddress = userName;
            }

            if (usernameOrEmailAddress == null || userNamePassword == null) {

                // One or more credentials are null, so can not attempt to login.
                Log.e(TAG, "No credentials to login with.");
                JSONObject jsonObjectMessageToBroadcast = new JSONObject();
                jsonObjectMessageToBroadcast.put("messageType", "INVALID_CREDENTIALS");
                jsonObjectMessageToBroadcast.put("responseCode", -1);
                String messageToDisplay = "Error: Username/email address or password can not be missing";
                jsonObjectMessageToBroadcast.put("messageToDisplay", messageToDisplay);
                Util.broadcastAMessage(context, "SERVER_USER_LOGIN", jsonObjectMessageToBroadcast);

                return;
            }


            String loginUrl = prefs.getServerUrl() + LOGIN_USER_URL;

            RequestBody requestBody = new FormBody.Builder()
                    .add("nameOrEmail", usernameOrEmailAddress)
                    .add("password", userNamePassword)
                    .build();
            PostResponse postResponse = makePost(loginUrl, requestBody);
            Response response = postResponse.response;
            JSONObject responseJson = postResponse.responseJson;
            JSONObject jsonObjectMessageToBroadcast = new JSONObject();

            if (response.code() == 200) {
                //  Here you read any answer from server.

                if (responseJson.getBoolean("success")) {
                    jsonObjectMessageToBroadcast.put("messageType", "SUCCESSFULLY_SIGNED_IN");

                    String userToken = responseJson.getString("token");
                    prefs.setUserToken(userToken);
                    prefs.setTokenLastRefreshed(new Date().getTime());
                    prefs.setUserSignedIn(true);

                    boolean isItSignedIn = prefs.getUserSignedIn();
                    Log.e(TAG, "isItSignedIn" + isItSignedIn);

                    String messageToDisplay = "You have successfully signed in as ";
                    jsonObjectMessageToBroadcast.put("messageToDisplay", messageToDisplay);
                    Util.broadcastAMessage(context, "SERVER_USER_LOGIN", jsonObjectMessageToBroadcast);

                } else { // not success
                    prefs.setUserToken(null);
                    jsonObjectMessageToBroadcast.put("messageType", "UNABLE_TO_SIGNIN");
                    String messageToDisplay = "Error, unable to sign in.";
                    jsonObjectMessageToBroadcast.put("messageToDisplay", messageToDisplay);
                    Util.broadcastAMessage(context, "SERVER_USER_LOGIN", jsonObjectMessageToBroadcast);
                }

            } else if (response.code() == HTTP_422_UNPROCESSABLE_ENTITY) {
                String message = "Sorry could not sign in.";
                try {
                    String errorType = responseJson.getString("errorType");
                    if (errorType != null) {
                        if (errorType.equals("validation")) {
                            message = responseJson.getString("message");
                            if (message.startsWith("_error:")) {
                                message = message.substring("_error:".length() + 1);
                            }
                        }
                    }

                } catch (Exception e) {
                    Log.w(TAG, e.getLocalizedMessage(), e);
                }

                jsonObjectMessageToBroadcast.put("messageType", "UNABLE_TO_SIGNIN");
                jsonObjectMessageToBroadcast.put("messageToDisplay", message);
                Util.broadcastAMessage(context, "SERVER_USER_LOGIN", jsonObjectMessageToBroadcast);

            } else {
                JSONArray messages = responseJson.getJSONArray("messages");
                String firstMessage = (String) messages.get(0);
                jsonObjectMessageToBroadcast.put("messageType", "UNABLE_TO_SIGNIN");
                jsonObjectMessageToBroadcast.put("messageToDisplay", firstMessage);
                Util.broadcastAMessage(context, "SERVER_USER_LOGIN", jsonObjectMessageToBroadcast);
            }

        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            try {
                JSONObject jsonObjectMessageToBroadcast = new JSONObject();
                jsonObjectMessageToBroadcast.put("messageType", "UNABLE_TO_SIGNIN");
                String messageToDisplay = "Error, unable to sign in.";
                jsonObjectMessageToBroadcast.put("messageToDisplay", messageToDisplay);
                Util.broadcastAMessage(context, "SERVER_USER_LOGIN", jsonObjectMessageToBroadcast);
            } catch (JSONException e2) {
                Log.e(TAG, e2.getLocalizedMessage(), e2);
            }
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

        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }

        return conn;
    }

    private static HttpURLConnection openHttpsURL(URL url) throws IOException {
        // Create connection
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
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
     */
    static void registerDevice(final String group, final String deviceName, final Context context) {
        final Prefs prefs = new Prefs(context);

        // Check that the group name is valid, at least 4 characters.
        if (group == null || group.length() < 4) {
            Log.i(TAG, "Invalid group name: " + group);
            broadcastGenericError(context, "Group name must be at least 4 characters", "REGISTER_FAIL", "SERVER_REGISTER");
            return;
        }

        String registerUrl = prefs.getServerUrl() + REGISTER_URL;
        try {
            final String password = RandomStringUtils.random(20, true, true);

            RequestBody requestBody = new FormBody.Builder()
                    .add("devicename", deviceName)
                    .add("password", password)
                    .add("group", group)
                    .build();
            PostResponse postResponse = makePost(registerUrl, requestBody);
            Response response = postResponse.response;
            JSONObject responseJson = postResponse.responseJson;

            if (response.code() == HTTP_422_UNPROCESSABLE_ENTITY) {
                Log.i(TAG, "Register device response from server is 422");
                JSONObject jsonObjectMessageToBroadcast = new JSONObject();
                jsonObjectMessageToBroadcast.put("messageType", "422_FAILED_TO_CREATE_USER");
                String serverMessage = responseJson.getString("message");
                String messageToDisplay = "Sorry, you had the following issues: " + serverMessage.replace("; ", " and ").toLowerCase();
                jsonObjectMessageToBroadcast.put("messageToDisplay", messageToDisplay);
                Util.broadcastAMessage(context, "SERVER_REGISTER", jsonObjectMessageToBroadcast);
                return;
            }

            if (response.isSuccessful() && responseJson.getBoolean("success")) {

                prefs.setDeviceToken(responseJson.getString("token"));
                prefs.setTokenLastRefreshed(new Date().getTime());

                String deviceID = Util.getDeviceID(prefs.getToken());

                prefs.setDeviceName(deviceName);
                prefs.setGroupName(group);
                prefs.setDevicePassword(password);

                prefs.setDeviceId(deviceID);
                JSONObject jsonObjectMessageToBroadcast = new JSONObject();
                jsonObjectMessageToBroadcast.put("messageType", "REGISTER_SUCCESS");
                jsonObjectMessageToBroadcast.put("messageToDisplay", "Success - Your phone has been registered with the server :-)");
                Util.broadcastAMessage(context, "SERVER_REGISTER", jsonObjectMessageToBroadcast);

                return;
            }
            //Unexpected response code from server
            Log.w(TAG, String.format("Unexpected register response from server is: %s, with message: %s, and JSON response: %s",
                    response.code(), response.message(), postResponse.body));

            String errorType = responseJson.getString("errorType");
            String serverMessage = responseJson.getString("message");
            String messageToDisplay = String.format("Unable to register with an unknown error. errorType is %s, and message is %s", errorType, serverMessage);
            broadcastGenericError(context, messageToDisplay, "REGISTER_FAIL", "SERVER_REGISTER");

        } catch (Exception e) {
            Log.w(TAG, e);
            broadcastGenericError(context, "An unknown error occurred: " + e.toString(), "REGISTER_FAIL", "SERVER_REGISTER");
        }
    }

    private static PostResponse makePost(String url, RequestBody requestBody) throws IOException, JSONException {
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Response response = client.newCall(request).execute();
        Log.i("MSG", response.message());
        return new PostResponse(response);
    }

    static void signUp(final String username, final String emailAddress, final String password, final Context context) {
        final Prefs prefs = new Prefs(context);

        String signupUrl = prefs.getServerUrl() + SIGNUP_URL;

        try {
            RequestBody requestBody = new FormBody.Builder()
                    .add("username", username)
                    .add("password", password)
                    .add("email", emailAddress)
                    .build();
            PostResponse postResponse = makePost(signupUrl, requestBody);
            Response response = postResponse.response;
            JSONObject responseJson = postResponse.responseJson;

            if (response.code() == HTTP_422_UNPROCESSABLE_ENTITY) {
                Log.i(TAG, "Signup response from server is 422");
                JSONObject jsonObjectMessageToBroadcast = new JSONObject();
                jsonObjectMessageToBroadcast.put("messageType", "422_FAILED_TO_CREATE_USER");
                String serverMessage = responseJson.getString("message");
                String messageToDisplay = "Sorry, you had the following issues: " + serverMessage.replace("; ", " and ").toLowerCase();
                jsonObjectMessageToBroadcast.put("messageToDisplay", messageToDisplay);
                Util.broadcastAMessage(context, "SERVER_SIGNUP", jsonObjectMessageToBroadcast);
                return;
            }

            if (response.isSuccessful() && responseJson.getBoolean("success")) {
                if (responseJson.getBoolean("success")) {
                    prefs.setUserToken(responseJson.getString("token"));
                    prefs.setUserTokenLastRefreshed(new Date().getTime());
                    prefs.setUsername(username);
                    prefs.setUsernamePassword(password);
                    prefs.setEmailAddress(emailAddress);

                    JSONObject jsonObjectMessageToBroadcast = new JSONObject();
                    jsonObjectMessageToBroadcast.put("messageType", "SUCCESSFULLY_CREATED_USER");
                    String messageToDisplay = "Success, you have successfully created a new user account";
                    jsonObjectMessageToBroadcast.put("messageToDisplay", messageToDisplay);
                    Util.broadcastAMessage(context, "SERVER_SIGNUP", jsonObjectMessageToBroadcast);
                    return;
                }
            }

            //Unexpected response code from server
            Log.w(TAG, String.format("Unexpected sign up response from server is: %s, with message: %s, and JSON response: %s",
                    response.code(), response.message(), postResponse.body));

            String errorType = responseJson.getString("errorType");
            String serverMessage = responseJson.getString("message");
            String messageToDisplay = String.format("Unable to signup with an unknown error. errorType is %s, and message is %s", errorType, serverMessage);
            broadcastGenericError(context, messageToDisplay, "FAILED_TO_CREATE_USER", "SERVER_SIGNUP");

        } catch (Exception e) {
            Log.w(TAG, e);
            broadcastGenericError(context, "An unknown error occured: " + e.toString(), "FAILED_TO_CREATE_USER", "SERVER_SIGNUP");
        }
    }

    private static void broadcastGenericError(Context context, String messageToDisplay, String messageType, String action) {
        broadcastGenericError(context, messageToDisplay, messageType, action, new JSONObject());
    }

    private static void broadcastGenericError(Context context, String messageToDisplay, String messageType, String action, JSONObject jsonObjectMessageToBroadcast) {
        try {
            jsonObjectMessageToBroadcast.put("messageType", messageType);
            jsonObjectMessageToBroadcast.put("messageToDisplay", messageToDisplay);
            Util.broadcastAMessage(context, action, jsonObjectMessageToBroadcast);
        } catch (JSONException e) {
            Log.w(TAG, e);
        }
    }

    static boolean uploadAudioRecording(File audioFile, JSONObject data, Context context) {
        // http://www.codejava.net/java-se/networking/upload-files-by-sending-multipart-request-programmatically
        if (uploading) {
            Log.i(TAG, "Already uploading. Wait until last upload is finished.");

            JSONObject jsonObjectMessageToBroadcast = new JSONObject();
            try {
                jsonObjectMessageToBroadcast.put("messageType", "ALREADY_RECORDING");
                jsonObjectMessageToBroadcast.put("messageToDisplay", "Sorry, can not record as a recording is already in progress");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Util.broadcastAMessage(context, "MANAGE_RECORDINGS", jsonObjectMessageToBroadcast);
            return false;
        }
        uploading = true;
        uploadFilesIdlingResource.increment();

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
                    long check = prefs.getLastRecordIdReturnedFromServer();
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
            Log.e(TAG, ex.getLocalizedMessage(), ex);
        } finally {
            uploading = false;
            uploadFilesIdlingResource.decrement();
        }

        return uploadSuccess;
    }

    static ArrayList<String> getGroups(Context context) {
        final Prefs prefs = new Prefs(context);

        ArrayList<String> groups = new ArrayList<>();
        try {

            // Setup the query to run on the server
            // For now this is just an empty json object, but in future will be able to add
            // search terms

            JSONObject jsonSearchTerms = new JSONObject();
            String jsonSearchTermsString = jsonSearchTerms.toString();

            HttpUrl url = new HttpUrl.Builder()
                    .scheme(prefs.getServerScheme())
                    .host(prefs.getServerHost())
                    .addPathSegments(GROUPS_URL)
                    .addQueryParameter("where", jsonSearchTermsString)
                    .build();

            String authorization = prefs.getUserToken();
            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", authorization)
                    .build();

            String responseBody;

            Response response = client.newCall(request).execute();
            int responseCode = response.code();
            Log.e(TAG, "responseCode: " + responseCode);

            //Set message to broadcast
            String messageToDisplay;
            JSONObject jsonObjectMessageToBroadcast = new JSONObject();
            jsonObjectMessageToBroadcast.put("responseCode", responseCode);


            if (responseCode == 200 && response.body() != null) {
                responseBody = response.body().string();

                // Get groups from responseBody
                JSONObject joRes = new JSONObject(responseBody);

                if (joRes.getBoolean("success")) {
                    jsonObjectMessageToBroadcast.put("messageType", "SUCCESSFULLY_RETRIEVED_GROUPS");
                    JSONArray groupsJSONArray = joRes.getJSONArray("groups");
                    if (groupsJSONArray != null) {
                        for (int i = 0; i < groupsJSONArray.length(); i++) {
                            JSONObject groupJSONObject = new JSONObject(groupsJSONArray.getString(i));
                            String groupName = groupJSONObject.getString("groupname");
                            groups.add(groupName);
                        }
                    }

                    messageToDisplay = "Success, groups have been updated from server";
                } else {
                    jsonObjectMessageToBroadcast.put("messageType", "FAILED_TO_RETRIEVE_GROUPS");
                    messageToDisplay = "Error, unable to get groups from server";
                }

            } else { // not success
                messageToDisplay = "Error, unable to get groups from server";
                jsonObjectMessageToBroadcast.put("messageType", "FAILED_TO_RETRIEVE_GROUPS");
            }
            jsonObjectMessageToBroadcast.put("messageToDisplay", messageToDisplay);
            Util.broadcastAMessage(context, SERVER_GROUPS_ACTION, jsonObjectMessageToBroadcast);

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage(), ex);
        }

        return groups;
    }

    static boolean addGroupToServer(Context context, String groupName) {
        final Prefs prefs = new Prefs(context);
        try {
            HttpUrl url = new HttpUrl.Builder()
                    .scheme(prefs.getServerScheme())
                    .host(prefs.getServerHost())
                    .addPathSegments(GROUPS_URL)
                    .build();

            RequestBody formBody = new FormBody.Builder()
                    .add("groupname", groupName)
                    .build();

            String authorization = prefs.getUserToken();
            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", authorization)
                    .post(formBody)
                    .build();

            Response response = client.newCall(request).execute();
            Log.i("MSG", response.message());
            JSONObject jsonObjectMessageToBroadcast = new JSONObject().put("responseCode", response.code());

            if (response.code() == 200) {
                String messageToDisplay = "Success, the group " + groupName + " has been added to the server";
                // Now add it to local storage
                Util.addGroup(context, groupName);
                broadcastGenericError(context, messageToDisplay, "SUCCESSFULLY_ADDED_GROUP", SERVER_GROUPS_ACTION, jsonObjectMessageToBroadcast);
                return true;
            } else {
                String messageToDisplay = "Sorry, the group " + groupName + " could not be added to the server";
                broadcastGenericError(context, messageToDisplay, "FAILED_TO_ADD_GROUP", SERVER_GROUPS_ACTION, jsonObjectMessageToBroadcast);
                return false;
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage(), ex);
            return false;
        }
    }

    static class PostResponse {
        final Response response;
        final String body;
        final JSONObject responseJson;

        PostResponse(Response response) throws IOException, JSONException {
            this.response = response;
            ResponseBody responseBody = response.body();
            body = responseBody != null ? responseBody.string() : "";
            if (responseBody != null) {
                responseBody.close();
            }
            responseJson = new JSONObject(body);
        }
    }
}