package nz.org.cacophony.birdmonitor.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import nz.org.cacophony.birdmonitor.Prefs;
import nz.org.cacophony.birdmonitor.R;

public class BatteryFragment extends Fragment {

    private static final String TAG = "BatteryFragment";


    private Switch swIgnoreLowBattery;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_battery, container, false);

        setUserVisibleHint(false);

        swIgnoreLowBattery = view.findViewById(R.id.swIgnoreLowBattery);

        displayOrHideGUIObjects();

        swIgnoreLowBattery.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs prefs = new Prefs(getActivity());
            prefs.setIgnoreLowBattery(swIgnoreLowBattery.isChecked());
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
        swIgnoreLowBattery.setChecked(prefs.getIgnoreLowBattery());
        if (prefs.getIgnoreLowBattery()) {
            swIgnoreLowBattery.setText("Record with low battery is ON");
        } else {
            swIgnoreLowBattery.setText("Record with low battery is OFF");
        }
    }

}
