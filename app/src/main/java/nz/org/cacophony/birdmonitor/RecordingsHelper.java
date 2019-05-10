package nz.org.cacophony.birdmonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.support.v4.util.Consumer;
import android.util.Log;
import nz.org.cacophony.birdmonitor.views.ManageRecordingsFragment;
import org.json.JSONObject;


public class RecordingsHelper {

    /**
     * Creates a specialised BroadcastReceiver that is specific to the {@link ManageRecordingsFragment#MANAGE_RECORDINGS_ACTION} type.
     * This class will updateTvMessages on appropriate messages, and will also call the provided method on RECORDING_FINISHED.
     * @param TAG The logging tag to use if any exceptions occur
     * @param updateTvMessage A function that takes a String, which should call a method to display the given message
     * @param onFinished Code to run upon receiving the {@link ManageRecordingsFragment.MessageType#RECORDING_FINISHED} message.
     * @return The BroadcastReceiver which then needs to be registered using the {@link MessageHelper#registerMessageHandler(MessageHelper.Action, BroadcastReceiver, Context)}
     */
    public static BroadcastReceiver createMessageHandler(String TAG, Consumer<String> updateTvMessage, Runnable onFinished) {
        return MessageHelper.createReceiver(intent -> {
            try {
                String jsonStringMessage = intent.getStringExtra("jsonStringMessage");
                if (jsonStringMessage == null) {
                    return;
                }

                JSONObject joMessage = new JSONObject(jsonStringMessage);
                String messageTypeStr = joMessage.optString("messageType");
                String messageToDisplay = joMessage.optString("messageToDisplay");
                if (messageTypeStr.isEmpty()) {
                    return;
                }

                ManageRecordingsFragment.MessageType messageType = ManageRecordingsFragment.MessageType.valueOf(messageTypeStr);
                switch (messageType) {
                    case RECORDING_DISABLED:
                    case NO_PERMISSION_TO_RECORD:
                    case UPLOADING_RECORDINGS:
                    case GETTING_READY_TO_RECORD:
                    case FAILED_RECORDINGS_NOT_UPLOADED:
                    case UPLOADING_FAILED_NOT_REGISTERED:
                    case RECORDING_STARTED:
                    case ALREADY_RECORDING:
                    case UPLOADING_FAILED:
                    case UPLOADING_FINISHED:
                    case RECORD_AND_UPLOAD_FAILED:
                        updateTvMessage.accept(messageToDisplay);
                        break;
                    case RECORDING_FINISHED:
                        updateTvMessage.accept(messageToDisplay);
                        onFinished.run();
                        break;
                }

            } catch (Exception ex) {
                Log.e(TAG, ex.getLocalizedMessage(), ex);
                updateTvMessage.accept("Could not record due to unknown error: " + ex.getLocalizedMessage());
            }
        });
    }
}
