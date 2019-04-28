package nz.org.cacophony.birdmonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Consumer;

public class MessageHelper {

    public static void registerMessageHandler(String action, BroadcastReceiver broadcastReceiver, Context context) {
        IntentFilter intentFilter = new IntentFilter(action);
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

}
