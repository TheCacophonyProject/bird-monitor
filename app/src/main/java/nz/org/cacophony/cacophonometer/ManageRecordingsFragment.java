package nz.org.cacophony.cacophonometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
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

public class ManageRecordingsFragment extends Fragment {

    private static final String TAG = "GPSFragment";


    private Button btnUploadFiles;
    private Button btnDeleteAllRecordings;
    TextView tvNumberOfRecordings;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_manage_recordings, container, false);

        setUserVisibleHint(false);

        btnUploadFiles = (Button) view.findViewById(R.id.btnUploadFiles);
        btnUploadFiles.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                uploadRecordings();
            }
        });

        btnDeleteAllRecordings = (Button) view.findViewById(R.id.btnDeleteAllRecordings);
        btnDeleteAllRecordings.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                deleteAllRecordingsButton();
            }
        });

         tvNumberOfRecordings = view.findViewById(R.id.tvNumberOfRecordings);


        return view;
    }

    @Override
    public void setUserVisibleHint(final boolean visible) {
        super.setUserVisibleHint(visible);
        if (getActivity() == null){
            return;
        }
        if (visible) {

            IntentFilter iff = new IntentFilter("MANAGE_RECORDINGS");
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onNotice, iff);

            tvNumberOfRecordings.setText("Number of recordings on phone: " + getNumberOfRecordings());

            if(getNumberOfRecordings() == 0){
                getView().findViewById(R.id.btnUploadFiles).setEnabled(false);
                getView().findViewById(R.id.btnDeleteAllRecordings).setEnabled(false);
            }else{
                getView().findViewById(R.id.btnUploadFiles).setEnabled(true);
                getView().findViewById(R.id.btnDeleteAllRecordings).setEnabled(true);
            }

        }else{

            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onNotice);

        }
    }







    public void uploadRecordings(){

        if (!Util.isNetworkConnected(getActivity().getApplicationContext())){
            Util.getToast(getActivity().getApplicationContext(),"The phone is not currently connected to the internet - please fix and try again", true ).show();
            return;
        }

        Prefs prefs = new Prefs(getActivity().getApplicationContext());
        if (prefs.getGroupName() == null){
            Util.getToast(getActivity().getApplicationContext(),"You need to register this phone before you can upload", true ).show();
            return;
        }

        File recordingsFolder = Util.getRecordingsFolder(getActivity().getApplicationContext());
        File recordingFiles[] = recordingsFolder.listFiles();
        int numberOfFilesToUpload = recordingFiles.length;

        if (getNumberOfRecordings() > 0){ // should be as button should be disabled if no recordings
            Util.getToast(getActivity().getApplicationContext(), "About to upload " + numberOfFilesToUpload + " recordings.", false).show();
            getView().findViewById(R.id.btnUploadFiles).setEnabled(false);
            Util.uploadFilesUsingUploadButton(getActivity().getApplicationContext());
        }else{
            Util.getToast(getActivity().getApplicationContext(), "There are no recordings on the phone to upload.", true).show();
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
        AlertDialog dialog = builder.create();
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
                            Util.getToast(getActivity().getApplicationContext(), messageToDisplay, false).show();
                        } else if (messageType.equalsIgnoreCase("FAILED_RECORDINGS_NOT_DELETED")) {
                            Util.getToast(getActivity().getApplicationContext(), messageToDisplay, true).show();
                        }

                        // Update button and message (get number of recordings again just in case a new recording has occurred)
                        int numberOfRecordingsOnPhone = getNumberOfRecordings();
                        tvNumberOfRecordings.setText("Number of recordings on phone: " + numberOfRecordingsOnPhone);
                        if (numberOfRecordingsOnPhone == 0){
                            getView().findViewById(R.id.btnUploadFiles).setEnabled(false);
                            getView().findViewById(R.id.btnDeleteAllRecordings).setEnabled(false);
                        }else{
                            getView().findViewById(R.id.btnUploadFiles).setEnabled(true);
                            getView().findViewById(R.id.btnDeleteAllRecordings).setEnabled(true);
                        }
                    }
                }

            } catch (Exception ex) {
                Log.e(TAG, ex.getLocalizedMessage());
            }
        }
    };

}
