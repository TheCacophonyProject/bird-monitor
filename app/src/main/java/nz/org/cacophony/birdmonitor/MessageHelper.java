package nz.org.cacophony.birdmonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Consumer;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

public class MessageHelper {

    private static final String TAG = MessageHelper.class.getName();

    public static void registerMessageHandler(Action action, BroadcastReceiver broadcastReceiver, Context context) {
        IntentFilter intentFilter = new IntentFilter(action.name);
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, intentFilter);
    }

    public static void unregisterMessageHandler(BroadcastReceiver receiver, Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    }

    public static BroadcastReceiver createReceiver(Consumer<Intent> onMessage) {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onMessage.accept(intent);
            }
        };
    }

    public static void broadcastMessage(String messageToDisplay, Enum messageType, Action action, Context context) {
        broadcastMessage(messageToDisplay, new JSONObject(), messageType, action, context);
    }

    public static void broadcastMessage(String messageToDisplay, JSONObject jsonObjectMessageToBroadcast, Enum messageType, Action action, Context context) {
        try {
            jsonObjectMessageToBroadcast.put("messageType", messageType.name());
            jsonObjectMessageToBroadcast.put("messageToDisplay", messageToDisplay);
            broadcastMessage(jsonObjectMessageToBroadcast, action.name, context);
        } catch (JSONException e) {
            Log.w(TAG, e);
        }
    }

    private static void broadcastMessage(JSONObject jsonStringMessage, String action, Context context) {
        // https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

        Intent intent = new Intent(action);
        intent.putExtra("jsonStringMessage", jsonStringMessage.toString());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static class Action {
        private final String name;

        public Action(String name) {
            this.name = name;
        }
    }

}
