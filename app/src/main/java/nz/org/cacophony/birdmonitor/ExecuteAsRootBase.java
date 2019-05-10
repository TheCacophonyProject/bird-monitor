package nz.org.cacophony.birdmonitor;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static nz.org.cacophony.birdmonitor.views.GPSFragment.ROOT_ACTION;
import static nz.org.cacophony.birdmonitor.views.GPSFragment.RootMessageType.ERROR_DO_NOT_HAVE_ROOT;

/**
 * The app is designed to run on rooted phones so that it can toggle airplane/flight mode on and off
 * (to save power).  To be able to do this it needs to be able to run root commands - this class
 * can be used to determine if root commands can run and also to run them.
 * <p>
 * The class was copied from http://muzikant-android.blogspot.com/2011/02/how-to-get-root-access-and-execute.html
 * and extended by ExecuteAsRootBase to allow specific command e.g. enable flight mode to run.
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
abstract class ExecuteAsRootBase {
    
    private static final String TAG = "ROOT";
    
    public static boolean canRunRootCommands() {
        boolean returnValue = false;
        Process suProcess;

        try {
            suProcess = Runtime.getRuntime().exec("su");

            DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());

            // https://developer.android.com/reference/java/io/DataInputStream
            BufferedReader osRes = new BufferedReader(new InputStreamReader(suProcess.getInputStream()));

            //noinspection ConstantConditions
            if (null != osRes) {
                // Getting the id of the current user to check if this is root
                os.writeBytes("id\n");
                os.flush();

                String currUid = osRes.readLine();
                @SuppressWarnings("UnusedAssignment") boolean exitSu = false;
                if (null == currUid) {
                    returnValue = false;
                    exitSu = false;
                    Log.d(TAG, "Can't get root access or denied by user");
                } else if (currUid.contains("uid=0")) {
                    returnValue = true;
                    exitSu = true;
                    Log.d(TAG, "Root access granted");
                } else {
                    returnValue = false;
                    exitSu = true;
                    Log.d(TAG, "Root access rejected: " + currUid);
                }

                if (exitSu) {
                    os.writeBytes("exit\n");
                    os.flush();
                }
            }
        } catch (Exception e) {
            // Can't get root !
            // Probably broken pipe exception on trying to write to output stream (os) after su failed, meaning that the device is not rooted

            returnValue = false;
            Log.d(TAG, "Root access rejected [" + e.getClass().getName() + "] : " + e.getMessage());
        }

        return returnValue;
    }

    public final boolean execute(Context context) {
        try {
            ArrayList<String> commands = getCommandsToExecute();
            if (null != commands && commands.size() > 0) {
                Process suProcess = Runtime.getRuntime().exec("su");

                DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());

                // Execute commands that require root access
                for (String currCommand : commands) {
                    os.writeBytes(currCommand + "\n");
                    os.flush();
                }

                os.writeBytes("exit\n");
                os.flush();

                try {
                    int suProcessRetval = suProcess.waitFor();
                    if (255 != suProcessRetval) {
                        // Root access granted
                        return true;
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Error executing root action", ex);
                    MessageHelper.broadcastMessage("error", ERROR_DO_NOT_HAVE_ROOT, ROOT_ACTION, context);
                }
            }
        } catch (Exception ex) {
            Log.w(TAG, "Can't get root access", ex);
            MessageHelper.broadcastMessage("error", ERROR_DO_NOT_HAVE_ROOT, ROOT_ACTION, context);
        }

        return false;
    }

    protected abstract ArrayList<String> getCommandsToExecute();
}
