package nz.org.cacophony.cacophonometer;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * The app is designed to run on rooted phones so that it can toggle airplane/flight mode on and off
 * (to save power).  To be able to do this it needs to be able to run root commands - this class
 * can be used to determine if root commands can run and also to run them.
 *
 * The class was copied from http://muzikant-android.blogspot.com/2011/02/how-to-get-root-access-and-execute.html
 * and extended by ExecuteAsRootBase to allow specific command e.g. enable flight mode to run.
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
abstract class ExecuteAsRootBase
{
    public static boolean canRunRootCommands()
    {
        boolean returnValue = false;
        Process suProcess;

        try
        {
            suProcess = Runtime.getRuntime().exec("su");

            DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());

            // https://developer.android.com/reference/java/io/DataInputStream
           // DataInputStream osRes = new DataInputStream(suProcess.getInputStream());
            BufferedReader osRes = new BufferedReader(new InputStreamReader(suProcess.getInputStream()));

            //noinspection ConstantConditions
            if (null != osRes)
            {
                // Getting the id of the current user to check if this is root
                os.writeBytes("id\n");
                os.flush();

                String currUid = osRes.readLine();
                @SuppressWarnings("UnusedAssignment") boolean exitSu = false;
                if (null == currUid)
                {
                    returnValue = false;
                    exitSu = false;
                    Log.d("ROOT", "Can't get root access or denied by user");
                }
                else if (currUid.contains("uid=0"))
                {
                    returnValue = true;
                    exitSu = true;
                    Log.d("ROOT", "Root access granted");
                }
                else
                {
                    returnValue = false;
                    exitSu = true;
                    Log.d("ROOT", "Root access rejected: " + currUid);
                }

                if (exitSu)
                {
                    os.writeBytes("exit\n");
                    os.flush();
                }
            }
        }
        catch (Exception e)
        {
            // Can't get root !
            // Probably broken pipe exception on trying to write to output stream (os) after su failed, meaning that the device is not rooted

            returnValue = false;
            Log.d("ROOT", "Root access rejected [" + e.getClass().getName() + "] : " + e.getMessage());
        }

        return returnValue;
    }

    public final boolean execute(Context context)
    {
        boolean returnValue = false;

        try
        {
            ArrayList<String> commands = getCommandsToExecute();
            if (null != commands && commands.size() > 0)
            {
                Process suProcess = Runtime.getRuntime().exec("su");

                DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());

                // Execute commands that require root access
                for (String currCommand : commands)
                {
                    os.writeBytes(currCommand + "\n");
                    os.flush();
                }

                os.writeBytes("exit\n");
                os.flush();

                try
                {
                    int suProcessRetval = suProcess.waitFor();
                    //noinspection RedundantIfStatement
                    if (255 != suProcessRetval)
                    {
                        // Root access granted
                        returnValue = true;
                    }
                    else
                    {
                        // Root access denied
                        returnValue = false;
                    }
                }
                catch (Exception ex)
                {
                    Log.e("ROOT", "Error executing root action", ex);
                    String messageToDisplay = "";
                    JSONObject jsonObjectMessageToBroadcast = new JSONObject();
                    jsonObjectMessageToBroadcast.put("messageToType", "error_do_not_have_root");
                    jsonObjectMessageToBroadcast.put("messageToDisplay", "error_do_not_have_root");
                    Util.broadcastAMessage(context, "ROOT", jsonObjectMessageToBroadcast);
                  //  Util.broadcastAMessage(context, "error_do_not_have_root");
                }
            }
        }
        catch (IOException ex)
        {
            Log.w("ROOT", "Can't get root access", ex);

            String messageToDisplay = "";
            JSONObject jsonObjectMessageToBroadcast = new JSONObject();
            try {
                jsonObjectMessageToBroadcast.put("messageToType", "error_do_not_have_root");
                jsonObjectMessageToBroadcast.put("messageToDisplay", "error_do_not_have_root");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Util.broadcastAMessage(context, "ROOT", jsonObjectMessageToBroadcast);
            //Util.broadcastAMessage(context, "error_do_not_have_root");
        }
        catch (SecurityException ex)
        {
            Log.w("ROOT", "Can't get root access", ex);
            String messageToDisplay = "";
            JSONObject jsonObjectMessageToBroadcast = new JSONObject();
            try {
                jsonObjectMessageToBroadcast.put("messageToType", "error_do_not_have_root");
                jsonObjectMessageToBroadcast.put("messageToDisplay", "error_do_not_have_root");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Util.broadcastAMessage(context, "ROOT", jsonObjectMessageToBroadcast);
           // Util.broadcastAMessage(context, "error_do_not_have_root");
        }
        catch (Exception ex)
        {
            Log.w("ROOT", "Error executing internal operation", ex);
            String messageToDisplay = "";
            JSONObject jsonObjectMessageToBroadcast = new JSONObject();
            try {
                jsonObjectMessageToBroadcast.put("messageToType", "error_do_not_have_root");
                jsonObjectMessageToBroadcast.put("messageToDisplay", "error_do_not_have_root");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Util.broadcastAMessage(context, "ROOT", jsonObjectMessageToBroadcast);
            //Util.broadcastAMessage(context, "error_do_not_have_root");
        }

        return returnValue;
    }
    protected abstract ArrayList<String> getCommandsToExecute();
}
