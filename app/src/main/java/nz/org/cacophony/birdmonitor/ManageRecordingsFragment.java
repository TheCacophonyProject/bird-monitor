package nz.org.cacophony.birdmonitor;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.File;

import static nz.org.cacophony.birdmonitor.IdlingResourceForEspressoTesting.uploadFilesIdlingResource;

public class ManageRecordingsFragment extends Fragment {

    private static final String TAG = "ManageRecordFragment";

    private Button btnUploadFiles;
    private Button btnDeleteAllRecordings;
    TextView tvNumberOfRecordings;
    private TextView tvMessages;
    private PermissionsHelper permissionsHelper;
    private Button btnCancel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manage_recordings, container, false);

        IntentFilter iff = new IntentFilter("MANAGE_RECORDINGS");
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onNotice, iff);

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
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onNotice);
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


        if (!Util.isNetworkConnected(getActivity().getApplicationContext())) {
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

    private final BroadcastReceiver onNotice = new BroadcastReceiver() {
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (getView() == null) {
                    return;
                }

                String jsonStringMessage = intent.getStringExtra("jsonStringMessage");
                if (jsonStringMessage != null) {

                    JSONObject joMessage = new JSONObject(jsonStringMessage);
                    String messageType = joMessage.getString("messageType");
                    String messageToDisplay = joMessage.getString("messageToDisplay");

                    // Need to handle broadcasts

                    if (messageType != null) {
                        if (messageType.equalsIgnoreCase("SUCCESSFULLY_DELETED_RECORDINGS")) {
                            tvMessages.setText(messageToDisplay);
                        } else if (messageType.equalsIgnoreCase("RECORDING_DELETED")) {
                            displayOrHideGUIObjects();
                        } else if (messageType.equalsIgnoreCase("FAILED_RECORDINGS_NOT_DELETED")) {
                            tvMessages.setText(messageToDisplay);
                        } else if (messageType.equalsIgnoreCase("UPLOADING_RECORDINGS")) {
                            btnCancel.setEnabled(true);
                            tvMessages.setText(messageToDisplay);
                        } else if (messageType.equalsIgnoreCase("UPLOADING_STOPPED")) {
                            btnCancel.setEnabled(false);
                            tvMessages.setText(messageToDisplay);
                        } else if (messageType.equalsIgnoreCase("PREPARING_TO_UPLOAD")) {
                            tvMessages.setText(messageToDisplay);
                        } else if (messageType.equalsIgnoreCase("CONNECTED_TO_SERVER")) {
                            tvMessages.setText(messageToDisplay);
                        } else if (messageType.equalsIgnoreCase("SUCCESSFULLY_UPLOADED_RECORDINGS_USING_UPLOAD_BUTTON")) {
                            btnCancel.setEnabled(false);
                            tvMessages.setText(messageToDisplay);
                            uploadFilesIdlingResource.decrement();
                        }

                        displayOrHideGUIObjects();
                    }
                }

            } catch (Exception ex) {
                Log.e(TAG, ex.getLocalizedMessage(), ex);
            }
        }
    };


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

}
