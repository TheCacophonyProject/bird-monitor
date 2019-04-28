package nz.org.cacophony.birdmonitor;

import android.content.BroadcastReceiver;
import android.support.v4.util.Consumer;
import android.util.Log;
import nz.org.cacophony.birdmonitor.views.ManageRecordingsFragment;
import org.json.JSONObject;

import static nz.org.cacophony.birdmonitor.IdlingResourceForEspressoTesting.recordIdlingResource;
import static nz.org.cacophony.birdmonitor.IdlingResourceForEspressoTesting.uploadFilesIdlingResource;

public class RecordingsHelper {

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
