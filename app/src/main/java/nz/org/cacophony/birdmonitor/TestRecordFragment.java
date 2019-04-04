package nz.org.cacophony.birdmonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.text.util.LinkifyCompat;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import nz.org.cacophony.birdmonitor.R;

import static nz.org.cacophony.birdmonitor.IdlingResourceForEspressoTesting.recordIdlingResource;
import static nz.org.cacophony.birdmonitor.IdlingResourceForEspressoTesting.uploadFilesIdlingResource;

public class TestRecordFragment extends Fragment {
    private static final String TAG = "TestRecordFragment";

    private Button btnNext;
    private Button btnRecordNow;
    private TextView tvServerLink;
    private TextView tvTitleMessage;
    private TextView tvMessages;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_test_record, container, false);

        setUserVisibleHint(false);
        tvTitleMessage = view.findViewById(R.id.tvTitleMessage);
        tvMessages = view.findViewById(R.id.tvMessages);

        btnNext = view.findViewById(R.id.btnFinished);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((SetupWizardActivity) getActivity()).nextPageView();
            }
        });

        btnRecordNow =  view.findViewById(R.id.btnRecordNow);
        btnRecordNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordNowButtonPressed();
            }
        });

        // Turn the words 'Cacophony Server' in the text view into a link
        // https://stackoverflow.com/questions/2734270/how-do-i-make-links-in-a-textview-clickable
        // and
        // https://android-developers.googleblog.com/2008/03/linkify-your-text.html

        // Note also used the following to set the color of the url link using the xml method
        // https://stackoverflow.com/questions/13520193/android-linkify-how-to-set-custom-link-color

        tvServerLink = (TextView) view.findViewById(R.id.tvServerLink);
        Prefs prefs = new Prefs(getActivity());
        String tvServerLinkText = tvServerLink.getText() + " " + prefs.getBrowseRecordingsServerUrl();
        tvServerLink.setText(tvServerLinkText);
        LinkifyCompat.addLinks(tvServerLink, Linkify.ALL);
        return view;
    }

    @Override
    public void setUserVisibleHint(final boolean visible) {

        super.setUserVisibleHint(visible);
        if (getActivity() == null) {
            return;
        }
        if (visible) {
            IntentFilter iff = new IntentFilter("MANAGE_RECORDINGS");
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onNotice, iff);
            displayOrHideGUIObjects();
        } else {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onNotice);
        }
    }

    void displayOrHideGUIObjects(){

        if (RecordAndUpload.isRecording) {
            getView().findViewById(R.id.btnRecordNow).setEnabled(false);
            tvTitleMessage.setText("Can not record, as a recording is already in progress");
            btnRecordNow.setVisibility(View.GONE);
        } else {
            getView().findViewById(R.id.btnRecordNow).setEnabled(true);
            tvTitleMessage.setText("Press the RECORD NOW button to check that your phone has been setup correctly.");
            btnRecordNow.setVisibility(View.VISIBLE);
        }
    }

    public void recordNowButtonPressed() {

        btnRecordNow.setEnabled(false);

        Intent myIntent = new Intent(getActivity(), StartRecordingReceiver.class);
        myIntent.putExtra("callingCode", "recordNowButtonClicked"); // for debugging
        try {
            myIntent.putExtra("type", "recordNowButton");
            getActivity().sendBroadcast(myIntent);

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    private final BroadcastReceiver onNotice = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (getView() == null) {
                return;
            }

            try {

                String jsonStringMessage = intent.getStringExtra("jsonStringMessage");

                if (jsonStringMessage != null) {

                    JSONObject joMessage = new JSONObject(jsonStringMessage);
                    String messageType = joMessage.getString("messageType");
                    String messageToDisplay = joMessage.getString("messageToDisplay");

                    if (messageType.equalsIgnoreCase("RECORDING_DISABLED")) {
                        tvMessages.setText(messageToDisplay);
                    } else if (messageType.equalsIgnoreCase("ALREADY_RECORDING")) {
                        tvMessages.setText(messageToDisplay);
                        uploadFilesIdlingResource.decrement();
                    } else if (messageType.equalsIgnoreCase("NO_PERMISSION_TO_RECORD")) {
                        tvMessages.setText(messageToDisplay);
                    } else if (messageType.equalsIgnoreCase("UPLOADING_RECORDINGS")) {
                        tvMessages.setText(messageToDisplay);
                    } else if (messageType.equalsIgnoreCase("UPLOADING_FAILED")) {
                        tvMessages.setText(messageToDisplay);
                        uploadFilesIdlingResource.decrement();
                    } else if (messageType.equalsIgnoreCase("UPLOADING_FINISHED")) {
                        tvMessages.setText(messageToDisplay);
                        uploadFilesIdlingResource.decrement();
                    } else if (messageType.equalsIgnoreCase("GETTING_READY_TO_RECORD")) {
                        tvMessages.setText(messageToDisplay);
                    } else if (messageType.equalsIgnoreCase("FAILED_RECORDINGS_NOT_UPLOADED")) {
                        tvMessages.setText(messageToDisplay);
                    } else if (messageType.equalsIgnoreCase("RECORD_AND_UPLOAD_FAILED")) {
                        tvMessages.setText(messageToDisplay);
                        recordIdlingResource.decrement();
                    } else if (messageType.equalsIgnoreCase("UPLOADING_FAILED_NOT_REGISTERED")) {
                        tvMessages.setText(messageToDisplay);
                    } else if (messageType.equalsIgnoreCase("RECORDING_STARTED")) {
                        tvMessages.setText(messageToDisplay);
                    } else if (messageType.equalsIgnoreCase("RECORDING_FINISHED")) {
                        tvMessages.setText(messageToDisplay);
                        btnRecordNow.setEnabled(true);
                        btnRecordNow.setVisibility(View.VISIBLE);
                        recordIdlingResource.decrement();
                    }
                }

            } catch (Exception ex) {
                Log.e(TAG, ex.getLocalizedMessage());
                tvMessages.setText("Could not record");

            }

        }
    };

}
