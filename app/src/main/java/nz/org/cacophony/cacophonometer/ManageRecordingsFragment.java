package nz.org.cacophony.cacophonometer;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import static nz.org.cacophony.cacophonometer.IdlingResourceForEspressoTesting.uploadFilesIdlingResource;

public class ManageRecordingsFragment extends Fragment {

    private static final String TAG = "ManageRecordFragment";


    private Button btnUploadFiles;
    private Button btnDeleteAllRecordings;
    TextView tvNumberOfRecordings;
    private TextView tvMessages;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_manage_recordings, container, false);

        IntentFilter iff = new IntentFilter("MANAGE_RECORDINGS");
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onNotice, iff);

        setUserVisibleHint(false);
        tvMessages = (TextView) view.findViewById(R.id.tvMessagesManageRecordings);
        btnUploadFiles = (Button) view.findViewById(R.id.btnUploadFiles);
        btnUploadFiles.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                tvMessages.setText("");
                uploadRecordings();
            }
        });

        btnDeleteAllRecordings = (Button) view.findViewById(R.id.btnDeleteAllRecordings);
        btnDeleteAllRecordings.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                tvMessages.setText("");
                deleteAllRecordingsButton();
            }
        });

         tvNumberOfRecordings = view.findViewById(R.id.tvNumberOfRecordings);
        displayOrHideGUIObjects();

        return view;
    }

    @Override
    public void onStop(){
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onNotice);
    }

    @Override
    public void setUserVisibleHint(final boolean visible) {
        super.setUserVisibleHint(visible);
        if (getActivity() == null){
            return;
        }
        if (visible) {
            displayOrHideGUIObjects();

        }
    }

    void displayOrHideGUIObjects() {
        int numberOfRecordings = getNumberOfRecordings();
        tvNumberOfRecordings.setText("Number of recordings on phone: " + numberOfRecordings);

        if(numberOfRecordings == 0){
            btnUploadFiles.setEnabled(false);
            btnDeleteAllRecordings.setEnabled(false);
        }else{
            btnUploadFiles.setEnabled(true);
            btnDeleteAllRecordings.setEnabled(true);
        }
    }





    public void uploadRecordings(){

        if (!Util.isNetworkConnected(getActivity().getApplicationContext())){
            tvMessages.setText("The phone is not currently connected to the internet - please fix and try again");
            return;
        }

        Prefs prefs = new Prefs(getActivity().getApplicationContext());
        if (prefs.getGroupName() == null){
            tvMessages.setText("You need to register this phone before you can upload");
            return;
        }

        File recordingsFolder = Util.getRecordingsFolder(getActivity().getApplicationContext());
        File recordingFiles[] = recordingsFolder.listFiles();
        int numberOfFilesToUpload = recordingFiles.length;

        if (getNumberOfRecordings() > 0){ // should be as button should be disabled if no recordings
            tvMessages.setText("About to upload " + numberOfFilesToUpload + " recordings.");
            getView().findViewById(R.id.btnUploadFiles).setEnabled(false);
            Util.uploadFilesUsingUploadButton(getActivity().getApplicationContext());
        }else{
            tvMessages.setText("There are no recordings on the phone to upload.");
        }
    }

    private int getNumberOfRecordings(){
        File recordingsFolder = Util.getRecordingsFolder(getActivity().getApplicationContext());
        File recordingFiles[] = recordingsFolder.listFiles();
        return recordingFiles.length;
    }

    public void deleteAllRecordingsButton(){

        // are you sure?
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Add the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              deleteAllRecordings();
            }
        });
        builder.setNegativeButton("No/Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                return;
            }
        });
        builder.setMessage("Are you sure you want to delete all the recordings on this phone?")
                .setTitle("Delete ALL Recordings");

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btnPositive = dialog.getButton(Dialog.BUTTON_POSITIVE);
                btnPositive.setTextSize(24);
                int btnPositiveColor = ResourcesCompat.getColor(getActivity().getResources(), R.color.dialogButtonText, null);
                btnPositive.setTextColor(btnPositiveColor);

                Button btnNegative = dialog.getButton(Dialog.BUTTON_NEGATIVE);
                btnNegative.setTextSize(24);
                int btnNegativeColor = ResourcesCompat.getColor(getActivity().getResources(), R.color.dialogButtonText, null);
                btnNegative.setTextColor(btnNegativeColor);

                //https://stackoverflow.com/questions/6562924/changing-font-size-into-an-alertdialog
                TextView textView = (TextView) dialog.findViewById(android.R.id.message);
                textView.setTextSize(22);
            }
        });
        dialog.show();

    }
    public void deleteAllRecordings(){

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
                        } else if (messageType.equalsIgnoreCase("FAILED_RECORDINGS_NOT_DELETED")) {
                            tvMessages.setText(messageToDisplay);
                        } else if (messageType.equalsIgnoreCase("SUCCESSFULLY_UPLOADED_RECORDINGS")) {
                            tvMessages.setText(messageToDisplay);
                            uploadFilesIdlingResource.decrement();
                        }

                        displayOrHideGUIObjects();
                    }
                }

            } catch (Exception ex) {
                Log.e(TAG, ex.getLocalizedMessage());
            }
        }
    };

}
