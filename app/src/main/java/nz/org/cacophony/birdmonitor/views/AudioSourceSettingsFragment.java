package nz.org.cacophony.birdmonitor.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import nz.org.cacophony.birdmonitor.Prefs;
import nz.org.cacophony.birdmonitor.R;

public class AudioSourceSettingsFragment extends Fragment {

    private static final String TAG = "AudioSourceSettingsFragment";


    private AppCompatRadioButton rbCAMCORDER;
    private AppCompatRadioButton rbDEFAULT;
    private AppCompatRadioButton rbMIC;
    private AppCompatRadioButton rbUNPROCESSED;
    private AppCompatRadioButton rbVOICE_COMMUNICATION;
    private AppCompatRadioButton rbVOICE_RECOGNITION;
    private Switch swUSE_BAT_SAMPLING_FREQUENCY;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_audio_source_settings, container, false);

        setUserVisibleHint(false);
        rbCAMCORDER = view.findViewById(R.id.rbCAMCORDER);
        rbDEFAULT = view.findViewById(R.id.rbDEFAULT);
        rbMIC = view.findViewById(R.id.rbMIC);
        rbUNPROCESSED = view.findViewById(R.id.rbUNPROCESSED);
        rbVOICE_COMMUNICATION = view.findViewById(R.id.rbVOICE_COMMUNICATION);
        rbVOICE_RECOGNITION = view.findViewById(R.id.rbVOICE_RECOGNITION);
        swUSE_BAT_SAMPLING_FREQUENCY = view.findViewById(R.id.swUSE_BAT_SAMPLING_FREQUENCY);

        displayOrHideGUIObjects();

        rbCAMCORDER.setOnClickListener(v -> {
            Prefs prefs = new Prefs(getActivity());
            prefs.setAudioSource("CAMCORDER");
        });


        rbDEFAULT.setOnClickListener(v -> {
            Prefs prefs = new Prefs(getActivity());
            prefs.setAudioSource("DEFAULT");
        });


        rbMIC.setOnClickListener(v -> {
            Prefs prefs = new Prefs(getActivity());
            prefs.setAudioSource("MIC");
        });

        rbUNPROCESSED.setOnClickListener(v -> {
            Prefs prefs = new Prefs(getActivity());
            prefs.setAudioSource("UNPROCESSED");
        });

        rbVOICE_COMMUNICATION.setOnClickListener(v -> {
            Prefs prefs = new Prefs(getActivity());
            prefs.setAudioSource("VOICE_COMMUNICATION");
        });

        rbVOICE_RECOGNITION.setOnClickListener(v -> {
            Prefs prefs = new Prefs(getActivity());
            prefs.setAudioSource("VOICE_RECOGNITION");
        });

        swUSE_BAT_SAMPLING_FREQUENCY.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs prefs = new Prefs(getActivity());
            prefs.setUseBatSamplingFrequency(swUSE_BAT_SAMPLING_FREQUENCY.isChecked());
            displayOrHideGUIObjects();
        });


        return view;
    }

    @Override
    public void setUserVisibleHint(final boolean visible) {
        super.setUserVisibleHint(visible);
        if (getActivity() == null) {
            return;
        }
        if (visible) {
            displayOrHideGUIObjects();
        }
    }

    void displayOrHideGUIObjects() {
        Prefs prefs = new Prefs(getActivity().getApplicationContext());
        String audioSource = prefs.getAudioSource();

        switch (audioSource) {

            case "CAMCORDER":
                rbCAMCORDER.setChecked(true);
                break;
            case "DEFAULT":
                rbDEFAULT.setChecked(true);
                break;
            case "MIC":
                rbMIC.setChecked(true);
                break;
            case "UNPROCESSED":
                rbUNPROCESSED.setChecked(true);
                break;
            case "VOICE_COMMUNICATION":
                rbVOICE_COMMUNICATION.setChecked(true);
                break;
            case "VOICE_RECOGNITION":
                rbVOICE_RECOGNITION.setChecked(true);
                break;
        }

        swUSE_BAT_SAMPLING_FREQUENCY.setChecked(prefs.getUseBatSamplingFrequency());
        if (prefs.getUseBatSamplingFrequency()) {
            swUSE_BAT_SAMPLING_FREQUENCY.setText("Bat Sampling is ON");
        } else {
            swUSE_BAT_SAMPLING_FREQUENCY.setText("Bat Sampling is OFF");
        }
    }


}
