package nz.org.cacophony.birdmonitor.views;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.io.File;

import nz.org.cacophony.birdmonitor.MessageHelper;
import nz.org.cacophony.birdmonitor.MessageHelper.Action;
import nz.org.cacophony.birdmonitor.PermissionsHelper;
import nz.org.cacophony.birdmonitor.Prefs;
import nz.org.cacophony.birdmonitor.R;
import nz.org.cacophony.birdmonitor.RecordAndUpload;
import nz.org.cacophony.birdmonitor.Util;

public class ManageRecordingsFragment extends Fragment {

    public static final Action MANAGE_RECORDINGS_ACTION = new Action("MANAGE_RECORDINGS");
    private static final String TAG = "ManageRecordFragment";
    TextView tvNumberOfRecordings;
    private Button btnUploadFiles;
    private Button btnDeleteAllRecordings;
    private TextView tvMessages;
    private PermissionsHelper permissionsHelper;
    private Button btnCancel;
    private final BroadcastReceiver messageHandler = MessageHelper.createReceiver(this::onMessage);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manage_recordings, container, false);

        MessageHelper.registerMessageHandler(MANAGE_RECORDINGS_ACTION, messageHandler, getActivity());

        setUserVisibleHint(false);
        tvMessages = view.findViewById(R.id.tvMessagesManageRecordings);
        btnUploadFiles = view.findViewById(R.id.btnUploadFiles);
        btnUploadFiles.setOnClickListener(v -> {
            tvMessages.setText("");
            uploadRecordings();
        });

        btnDeleteAllRecordings = view.findViewById(R.id.btnDeleteAllRecordings);
        btnDeleteAllRecordings.setOnClickListener(v -> {
            tvMessages.setText("");
            deleteAllRecordingsButton();
        });

        btnCancel = view.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> {
            cancelButtonPressed();
        });

        tvNumberOfRecordings = view.findViewById(R.id.tvNumberOfRecordings);
        displayOrHideGUIObjects();

        btnCancel.setEnabled(false);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        MessageHelper.unregisterMessageHandler(messageHandler, getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        MessageHelper.registerMessageHandler(MANAGE_RECORDINGS_ACTION, messageHandler, getActivity());
    }

    @Override
    public void setUserVisibleHint(final boolean visible) {
        super.setUserVisibleHint(visible);

        if (getActivity() == null) {
            return;
        }
        checkPermissions();
        if (visible) {

            displayOrHideGUIObjects();
        }
    }

    public void displayOrHideGUIObjects() {
        int numberOfRecordings = getNumberOfRecordings();
        tvNumberOfRecordings.setText("Number of recordings on phone: " + numberOfRecordings);

        if (numberOfRecordings == 0) {
            btnUploadFiles.setEnabled(false);
            btnDeleteAllRecordings.setEnabled(false);
        } else {
            btnUploadFiles.setEnabled(true);
            btnDeleteAllRecordings.setEnabled(true);
        }
    }

    public void uploadRecordings() {
        if (RecordAndUpload.isRecording) {
            Toast.makeText(this.getContext(), R.string.upload_currently_recording, Toast.LENGTH_SHORT).show();
            return;
        }
        if (Util.isAirplaneModeOn(getContext())) {
            Util.disableFlightMode(getContext());
        } else if (!Util.isNetworkConnected(getActivity().getApplicationContext())) {
            tvMessages.setText("The phone is not currently connected to the internet - please fix and try again");
            return;
        }

        Prefs prefs = new Prefs(getActivity().getApplicationContext());
        if (prefs.getGroupName() == null) {
            tvMessages.setText("You need to register this phone before you can upload");
            return;
        }

        File recordingsFolder = Util.getRecordingsFolder(getActivity().getApplicationContext());
        File recordingFiles[] = recordingsFolder.listFiles();
        int numberOfFilesToUpload = recordingFiles.length;

        if (getNumberOfRecordings() > 0) { // should be as button should be disabled if no recordings
            tvMessages.setText("About to upload " + numberOfFilesToUpload + " recordings.");
            getView().findViewById(R.id.btnUploadFiles).setEnabled(false);
            btnCancel.setEnabled(true);
            RecordAndUpload.setCancelUploadingRecordings(false);
            Util.uploadFilesUsingUploadButton(getActivity().getApplicationContext());
        } else {
            tvMessages.setText("There are no recordings on the phone to upload.");
        }
    }

    private int getNumberOfRecordings() {
        int numberOfRecordings = -1;

        boolean alreadyHavePermission = haveAllPermissions(getActivity().getApplicationContext());

        if (alreadyHavePermission) {
            numberOfRecordings = getNumberOfRecordingsNoPermissionCheck();
        }

        return numberOfRecordings;
    }

    private int getNumberOfRecordingsNoPermissionCheck() {

        File recordingsFolder = Util.getRecordingsFolder(getActivity().getApplicationContext());
        File recordingFiles[] = recordingsFolder.listFiles();
        return recordingFiles.length;
    }

    public void deleteAllRecordingsButton() {

        // are you sure?
        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setPositiveButton("Yes", (di, id) -> deleteAllRecordings())
                .setNegativeButton("No/Cancel", (di, id) -> { /*Exit the dialog*/ })
                .setMessage("Are you sure you want to delete all the recordings on this phone?")
                .setTitle("Delete ALL Recordings")
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button btnPositive = dialog.getButton(Dialog.BUTTON_POSITIVE);
            btnPositive.setTextSize(24);
            int btnPositiveColor = ResourcesCompat.getColor(getActivity().getResources(), R.color.dialogButtonText, null);
            btnPositive.setTextColor(btnPositiveColor);

            Button btnNegative = dialog.getButton(Dialog.BUTTON_NEGATIVE);
            btnNegative.setTextSize(24);
            int btnNegativeColor = ResourcesCompat.getColor(getActivity().getResources(), R.color.dialogButtonText, null);
            btnNegative.setTextColor(btnNegativeColor);

            //https://stackoverflow.com/questions/6562924/changing-font-size-into-an-alertdialog
            TextView textView = dialog.findViewById(android.R.id.message);
            textView.setTextSize(22);
        });
        dialog.show();
    }

    public void deleteAllRecordings() {
        Util.deleteAllRecordingsOnPhoneUsingDeleteButton(getActivity().getApplicationContext());
    }

    private void onMessage(Intent intent) {
        try {
            if (getView() == null) {
                return;
            }

            String jsonStringMessage = intent.getStringExtra("jsonStringMessage");
            if (jsonStringMessage != null) {

                JSONObject joMessage = new JSONObject(jsonStringMessage);
                String messageTypeStr = joMessage.optString("messageType");
                String messageToDisplay = joMessage.getString("messageToDisplay");

                if (!messageTypeStr.isEmpty()) {
                    MessageType messageType = MessageType.valueOf(messageTypeStr);
                    switch (messageType) {
                        case FAILED_RECORDINGS_NOT_DELETED:
                        case SUCCESSFULLY_DELETED_RECORDINGS:
                        case FAILED_RECORDINGS_NOT_UPLOADED_USING_UPLOAD_BUTTON:
                        case PREPARING_TO_UPLOAD:
                        case CONNECTED_TO_SERVER:
                            tvMessages.setText(messageToDisplay);
                            break;
                        case UPLOADING_RECORDINGS:
                            btnCancel.setEnabled(true);
                            tvMessages.setText(messageToDisplay);
                            break;
                        case UPLOADING_STOPPED:
                        case SUCCESSFULLY_UPLOADED_RECORDINGS_USING_UPLOAD_BUTTON:
                            btnCancel.setEnabled(false);
                            tvMessages.setText(messageToDisplay);
                            break;
                        case RECORDING_DELETED:
                            displayOrHideGUIObjects();
                            break;
                    }
                    displayOrHideGUIObjects();
                }
            }

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage(), ex);
        }
    }

    private boolean haveAllPermissions(Context context) {
        boolean allPermissionsAlreadyGranted = true;

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            allPermissionsAlreadyGranted = false;

        }

        return allPermissionsAlreadyGranted;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsHelper.onRequestPermissionsResult(getActivity(), requestCode, permissions, grantResults);
    }

    private void checkPermissions() {
        permissionsHelper = new PermissionsHelper();
        permissionsHelper.checkAndRequestPermissions(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public void cancelButtonPressed() {
        RecordAndUpload.setCancelUploadingRecordings(true);
        btnCancel.setEnabled(false);
        tvMessages.setText("Stopping the uploading of Recordings.");
    }

    public enum MessageType {
        RECORDING_DISABLED,
        NO_PERMISSION_TO_RECORD,
        UPLOADING_FAILED,
        UPLOADING_FINISHED,
        GETTING_READY_TO_RECORD,
        FAILED_RECORDINGS_NOT_UPLOADED,
        RECORD_AND_UPLOAD_FAILED,
        UPLOADING_FAILED_NOT_REGISTERED,
        RECORDING_STARTED,
        RECORDING_FINISHED,
        ALREADY_RECORDING,
        SUCCESSFULLY_DELETED_RECORDINGS,
        FAILED_RECORDINGS_NOT_DELETED,
        UPLOADING_RECORDINGS,
        SUCCESSFULLY_UPLOADED_RECORDINGS_USING_UPLOAD_BUTTON,
        FAILED_RECORDINGS_NOT_UPLOADED_USING_UPLOAD_BUTTON,
        PREPARING_TO_UPLOAD,
        CONNECTED_TO_SERVER,
        UPLOADING_STOPPED,
        RECORDING_DELETED
    }

}
