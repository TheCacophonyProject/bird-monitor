package nz.org.cacophony.birdmonitor.views;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import nz.org.cacophony.birdmonitor.Prefs;
import nz.org.cacophony.birdmonitor.R;
import nz.org.cacophony.birdmonitor.Util;

public class TestingFragment extends Fragment {

    private Switch swUseTestServer;
    private Switch swUseVeryFrequentRecordings;
    private Switch swShortRecordings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_testing, container, false);

        setUserVisibleHint(false);
        Button btnFinished = view.findViewById(R.id.btnFinished);
        swUseTestServer = view.findViewById(R.id.swUseTestServer);
        swUseVeryFrequentRecordings = view.findViewById(R.id.swUseVeryFrequentRecordings);
        swShortRecordings = view.findViewById(R.id.swShortRecordings);


        displayOrHideGUIObjects();
        btnFinished.setOnClickListener(v -> ((AdvancedWizardActivity) getActivity()).nextPageView());


        swUseTestServer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // https://stackoverflow.com/questions/17372750/android-setoncheckedchangelistener-calls-again-when-old-view-comes-back
            if (!buttonView.isShown()) {
                return;
            }

            Util.setUseTestServer(getActivity().getApplicationContext(), isChecked);

            if (!isChecked) {
                Prefs prefs = new Prefs(getActivity().getApplicationContext());
                prefs.setUseShortRecordings(false);
                prefs.setUseVeryFrequentRecordings(false);
            }
            displayOrHideGUIObjects();

        });


        swUseVeryFrequentRecordings.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isShown()) {
                return;
            }
            Util.setUseVeryFrequentRecordings(getActivity().getApplicationContext(), isChecked);
            displayOrHideGUIObjects();
        });


        swShortRecordings.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isShown()) {
                return;
            }
            Prefs prefs = new Prefs(getActivity().getApplicationContext());
            prefs.setUseShortRecordings(isChecked);
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

        boolean useTestServer = prefs.getUseTestServer();
        swUseTestServer.setChecked(useTestServer);
        if (prefs.getUseTestServer()) {
            swUseTestServer.setText("Use Test Server is ON");
            swShortRecordings.setEnabled(true);
            swUseVeryFrequentRecordings.setEnabled(true);

        } else {
            swUseTestServer.setText("Use Test Server is OFF");

            swShortRecordings.setChecked(false);
            swShortRecordings.setEnabled(false);

            swUseVeryFrequentRecordings.setChecked(false);
            swUseVeryFrequentRecordings.setEnabled(false);
        }


        boolean useVeryFrequentRecordings = prefs.getUseVeryFrequentRecordings();

        swUseVeryFrequentRecordings.setChecked(useVeryFrequentRecordings);
        swUseVeryFrequentRecordings.setEnabled(useTestServer);// only enabled if using test server
        if (prefs.getUseVeryFrequentRecordings()) {
            swUseVeryFrequentRecordings.setText("Very frequent recordings is ON");
        } else {
            swUseVeryFrequentRecordings.setText("Very frequent recordings is OFF");
        }

        boolean useShortRecordings = prefs.getUseShortRecordings();

        swShortRecordings.setChecked(useShortRecordings);
        swShortRecordings.setEnabled(useTestServer); // only enabled if using test server
        if (prefs.getUseShortRecordings()) {
            swShortRecordings.setText("Short recordings is ON");
        } else {
            swShortRecordings.setText("Short recordings is OFF");
        }
    }
}
