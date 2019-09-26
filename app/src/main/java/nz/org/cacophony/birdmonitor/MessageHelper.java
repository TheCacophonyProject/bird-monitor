package nz.org.cacophony.birdmonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.core.util.Consumer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class provides simple wrapper methods to easily manage the sending and receiving of {@link LocalBroadcastManager} based messages.
 */
public class MessageHelper {

    private static final String TAG = MessageHelper.class.getName();

    /**
     * Register a {@link BroadcastReceiver} to trigger on the given {@link Action}.
     *
     * @param action            The Action to listen for.
     * @param broadcastReceiver The receiver to trigger upon message.
     * @param context           Any context within the app to get the LocalBroadcastManager instance.
     */
    public static void registerMessageHandler(Action action, BroadcastReceiver broadcastReceiver, Context context) {
        IntentFilter intentFilter = new IntentFilter(action.name);
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, intentFilter);
    }

    /**
     * Unregister a {@link BroadcastReceiver}.
     *
     * @param receiver The receiver to unregister.
     * @param context  Any context within the app to get the LocalBroadcastManager instance.
     */
    public static void unregisterMessageHandler(BroadcastReceiver receiver, Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    }

    /**
     * Creates and returns an {@link BroadcastReceiver} that will call the given function upon receiving a message.
     *
     * @param onMessage The function to call upon message.
     * @return The {@link BroadcastReceiver} instance.
     */
    public static BroadcastReceiver createReceiver(Consumer<Intent> onMessage) {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onMessage.accept(intent);
            }
        };
    }

    /**
     * Send a message on the LocalBroadcastManager stream targeted at the given Action.
     *
     * @param messageToDisplay The message that any given receiver should display.
     * @param messageType      The type of message, must match the appropriate Action.
     * @param action           The Action that the message relates to.
     * @param context          Any context within the app to get the LocalBroadcastManager instance.
     */
    public static void broadcastMessage(String messageToDisplay, Enum messageType, Action action, Context context) {
        broadcastMessage(messageToDisplay, new JSONObject(), messageType, action, context);
    }

    /**
     * Send a message on the LocalBroadcastManager stream targeted at the given Action. Includes any extras included.
     * messageType and messageToDisplay will be overwritten by the parameters if they are already set in the JSONObject.
     *
     * @param messageToDisplay             The message that any given receiver should display.
     * @param jsonObjectMessageToBroadcast Any additional extra information to include in the broadcast.
     * @param messageType                  The type of message, must match the appropriate Action.
     * @param action                       The Action that the message relates to.
     * @param context                      Any context within the app to get the LocalBroadcastManager instance.
     */
    public static void broadcastMessage(String messageToDisplay, JSONObject jsonObjectMessageToBroadcast, Enum messageType, Action action, Context context) {
        try {
            jsonObjectMessageToBroadcast.put("messageType", messageType.name());
            jsonObjectMessageToBroadcast.put("messageToDisplay", messageToDisplay);
            broadcastMessage(jsonObjectMessageToBroadcast, action.name, context);
        } catch (JSONException e) {
            Log.w(TAG, e);
        }
    }

    /**
     * Actually responsible for broadcasting the message.
     * This method is private to enforce the Enum + Action classes to prevent basic String inputs.
     *
     * @param jsonStringMessage The entire message, must include messageType and messageToDisplay
     * @param action            The action to trigger as a String.
     * @param context           Any context within the app to get the LocalBroadcastManager instance.
     */
    private static void broadcastMessage(JSONObject jsonStringMessage, String action, Context context) {
        // https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

        Intent intent = new Intent(action);
        intent.putExtra("jsonStringMessage", jsonStringMessage.toString());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Represents a specific action or set of actions that the UIs want to listen for.
     * Must have an accompanying Enum which contains the set of valid messageTypes.
     */
    public static class Action {
        private final String name;

        public Action(String name) {
            this.name = name;
        }
    }

}
