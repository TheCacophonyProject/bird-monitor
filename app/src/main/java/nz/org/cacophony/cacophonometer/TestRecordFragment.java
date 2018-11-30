package nz.org.cacophony.cacophonometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

public class TestRecordFragment extends Fragment {
    private static final String TAG = "TestRecordFragment";
    private Button btnBack;
    private Button btnNext;
    private Button btnRecordNow;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_test_record, container, false);

        setUserVisibleHint(false);

        btnBack = (Button) view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ((SetupWizardActivity)getActivity()).previousPageView();
            }
        });

        btnNext = (Button) view.findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                ((SetupWizardActivity)getActivity()).nextPageView();
            }
        });

        btnRecordNow = (Button) view.findViewById(R.id.btnRecordNow);
        btnRecordNow.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                recordNowButtonPressed();
            }
        });


        return view;
    }

    @Override
    public void setUserVisibleHint(final boolean visible) {

        super.setUserVisibleHint(visible);
        if (getActivity() == null){
            return;
        }
        if (visible) {
            IntentFilter iff = new IntentFilter("RECORDING");
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onNotice, iff);

            TextView tvMessages = getView().findViewById(R.id.tvMessages);
            Prefs prefs = new Prefs(getActivity().getApplicationContext());
           if (prefs.getIsDisabled() ){
                getView().findViewById(R.id.btnRecordNow).setEnabled(false);
               tvMessages.setText("Recording is currently disabled on this phone");
            }else if (RecordAndUpload.isRecording){
               getView().findViewById(R.id.btnRecordNow).setEnabled(false);
               tvMessages.setText("Can not record, as a recording is already in progress");
            } else{
                getView().findViewById(R.id.btnRecordNow).setEnabled(true);
               tvMessages.setText("");
            }

        }else{
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onNotice);
        }
    }

    public void recordNowButtonPressed() {

       // recordNowIdlingResource.increment();


        getView().findViewById(R.id.btnRecordNow).setEnabled(false);

        Intent myIntent = new Intent(getActivity(), StartRecordingReceiver.class);
        myIntent.putExtra("callingCode", "recordNowButtonClicked"); // for debugging
        try {
            myIntent.putExtra("type", "recordNowButton");
            getActivity().sendBroadcast(myIntent);

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }

        //  Util.createCreateAlarms(getApplicationContext());
    }

    private final BroadcastReceiver onNotice = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            TextView tvMessages = getView().findViewById(R.id.tvMessages);
            try {
                Prefs prefs = new Prefs(context);

                String jsonStringMessage = intent.getStringExtra("jsonStringMessage");

                if (jsonStringMessage != null) {

                    JSONObject joMessage = new JSONObject(jsonStringMessage);
                    String messageType = joMessage.getString("messageType");
                    String messageToDisplay = joMessage.getString("messageToDisplay");

                    if (messageType.equalsIgnoreCase("RECORDING_DISABLED")) {
                        //getView().findViewById(R.id.btnRecordNow).setEnabled(true);
                        tvMessages.setText(messageToDisplay);
                    } else if (messageType.equalsIgnoreCase("ALREADY_RECORDING")) {
                        tvMessages.setText(messageToDisplay);

                    } else if (messageType.equalsIgnoreCase("UPLOADING_FINISHED")) {
                        tvMessages.setText(messageToDisplay);

                    } else if (messageType.equalsIgnoreCase("UPLOADING_FAILED")) {
                        tvMessages.setText(messageToDisplay);

                    } else if (messageType.equalsIgnoreCase("RECORDING_STARTED")) {
                        tvMessages.setText(messageToDisplay);
                    } else if (messageType.equalsIgnoreCase("RECORDING_FINISHED")) {
                    tvMessages.setText(messageToDisplay);
                        getView().findViewById(R.id.btnRecordNow).setEnabled(true);
                }
                }

            } catch (Exception ex) {
                Log.e(TAG, ex.getLocalizedMessage());
                tvMessages.setText("Could not record");

            }
//                TextView tvMessages = getView().findViewById(R.id.tvMessages);
//                if (message != null) {
//
//                    if (message.equalsIgnoreCase("recordNowButton_finished")) {
//                        getView().findViewById(R.id.btnRecordNow).setEnabled(true);
//                        tvMessages.setText("Finished");
//                       // recordNowIdlingResource.decrement();
//                    } else if (message.equalsIgnoreCase("recording_started")) {
//                        tvMessages.setText("Recording");
//                        Util.getToast(getActivity().getApplicationContext(), "Recording started", false).show();
//                    } else if (message.equalsIgnoreCase("recording_finished")) {
//                        tvMessages.setText("Finished");
//                        Util.getToast(getActivity().getApplicationContext(), "Recording finished", false).show();
//                    } else if (message.equalsIgnoreCase("about_to_upload_files")) {
//                        tvMessages.setText("About to upload files");
//                        Util.getToast(getActivity().getApplicationContext(), "About to upload files", false).show();
//                    } else if (message.equalsIgnoreCase("files_successfully_uploaded")) {
//                        tvMessages.setText("Files successfully uploaded");
//                        Util.getToast(getActivity().getApplicationContext(), "Files successfully uploaded", false).show();
//                    } else if (message.equalsIgnoreCase("already_uploading")) {
//                        tvMessages.setText("Files are already uploading");
//                        Util.getToast(getActivity().getApplicationContext(), "Files are already uploading", false).show();
//                    } else if (message.equalsIgnoreCase("no_permission_to_record")) {
//                        tvMessages.setText("Can not record.  Please go to Android settings and enable all required permissions for this app");
//                        Util.getToast(getActivity().getApplicationContext(), "Can not record.  Please go to Android settings and enable all required permissions for this app", true).show();
//                        getView().findViewById(R.id.btnRecordNow).setEnabled(true);
//                      //  recordNowIdlingResource.decrement();
//                    } else if (message.equalsIgnoreCase("recording_and_uploading_finished")) {
//                        tvMessages.setText("Recording and uploading finished");
//                        Util.getToast(getActivity().getApplicationContext(), "Recording and uploading finished", false).show();
//
//                    } else if (message.equalsIgnoreCase("recording_finished_but_uploading_failed")) {
//                        tvMessages.setText("Recording finished but uploading failed");
//                        Util.getToast(context, "Recording finished but uploading failed", true).show();
//                        getView().findViewById(R.id.btnRecordNow).setEnabled(true && !prefs.getIsDisabled());
//                     //   recordNowIdlingResource.decrement();
//                    } else if (message.equalsIgnoreCase("recorded_successfully_no_network")) {
//                        tvMessages.setText("Recorded successfully, no network connection so did not upload");
//                        Util.getToast(getActivity().getApplicationContext(), "Recorded successfully, no network connection so did not upload", false).show();
//                    } else if (message.equalsIgnoreCase("recording_failed")) {
//                        tvMessages.setText("Recording failed");
//                        Util.getToast(getActivity().getApplicationContext(), "Recording failed", true).show();
//                        getView().findViewById(R.id.btnRecordNow).setEnabled(true && !prefs.getIsDisabled());
//                    } else if (message.equalsIgnoreCase("not_logged_in")) {
//                        tvMessages.setText("Not logged in to server, could not upload files");
//                        Util.getToast(getActivity().getApplicationContext(), "Not logged in to server, could not upload files", true).show();
//                    } else if (message.equalsIgnoreCase("is_already_recording")) {                  //      uploadingIdlingResource.decrement();
//                        // Will need enable Record Now button
//                        getView().findViewById(R.id.btnRecordNow).setEnabled(true && !prefs.getIsDisabled());
//                     //   recordNowIdlingResource.decrement();
//                        tvMessages.setText("Could not do a recording as another recording is already in progress");
//                        Util.getToast(getActivity().getApplicationContext(), "Could not do a recording as another recording is already in progress", true).show();
//                        getView().findViewById(R.id.btnRecordNow).setEnabled(true && !prefs.getIsDisabled());
//                    //    recordNowIdlingResource.decrement();
//                    } else if (message.equalsIgnoreCase("error_do_not_have_root")) {
//                        tvMessages.setText("It looks like you have incorrectly indicated in settings that this phone has been rooted");
//                        Util.getToast(getActivity().getApplicationContext(), "It looks like you have incorrectly indicated in settings that this phone has been rooted", true).show();
//                    }else if (message.equalsIgnoreCase("update_record_now_button")){
//                        getView().findViewById(R.id.btnRecordNow).setEnabled(!RecordAndUpload.isRecording && !prefs.getIsDisabled());
//                    }
//
//                }
//
//            } catch (Exception ex) {
//
//                Log.e(TAG, ex.getLocalizedMessage());
//            }
        }
    };

}
