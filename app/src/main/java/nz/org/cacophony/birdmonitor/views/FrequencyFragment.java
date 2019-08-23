package nz.org.cacophony.birdmonitor.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import nz.org.cacophony.birdmonitor.Prefs;
import nz.org.cacophony.birdmonitor.R;
import nz.org.cacophony.birdmonitor.Util;

public class FrequencyFragment extends Fragment {

    private static final String TAG = "FrequencyFragment";


    private Switch swUseFrequentUploads;
    private TextView tvMessages;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_frequency, container, false);
        setUserVisibleHint(false);

        tvMessages = view.findViewById(R.id.tvMessages);
        swUseFrequentUploads = view.findViewById(R.id.swUseFrequentUploads);
        displayOrHideGUIObjects();

        swUseFrequentUploads.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Util.setUseFrequentUploads(getActivity().getApplicationContext(), swUseFrequentUploads.isChecked());
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
        Prefs prefs = new Prefs(getActivity());
        swUseFrequentUploads.setChecked(prefs.getUseFrequentUploads());

        if (prefs.getUseFrequentUploads()) {
            swUseFrequentUploads.setText("Upload after every recording is ON");
        } else {
            swUseFrequentUploads.setText("Upload after every recording is OFF");
        }

    }

}
