package nz.org.cacophony.cacophonometer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

public class FrequencyFragment extends Fragment {

    private Switch swRecordMoreOften;
    private Switch swUseFrequentUploads;
    private Switch swPeriodicallyUpdateGPS;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_frequency, container, false);

        setUserVisibleHint(false);

        swRecordMoreOften = view.findViewById(R.id.swRecordMoreOften);
        swUseFrequentUploads = view.findViewById(R.id.swUseFrequentUploads);
        swPeriodicallyUpdateGPS = view.findViewById(R.id.swPeriodicallyUpdateGPS);

        displayOrHideGUIObjects();

        swRecordMoreOften.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Util.setUseFrequentRecordings(getActivity().getApplicationContext(), swRecordMoreOften.isChecked());
                displayOrHideGUIObjects();
            }
        });


        swUseFrequentUploads.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Util.setUseFrequentUploads(getActivity().getApplicationContext(), swUseFrequentUploads.isChecked());
                displayOrHideGUIObjects();
            }
        });


        swPeriodicallyUpdateGPS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Util.setPeriodicallyUpdateGPS(getActivity().getApplicationContext(), swPeriodicallyUpdateGPS.isChecked());
                displayOrHideGUIObjects();
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
            displayOrHideGUIObjects();
        }
    }


    private void displayOrHideGUIObjects() {
        Prefs prefs = new Prefs(getActivity());
        swRecordMoreOften.setChecked(prefs.getUseFrequentRecordings());
        if (prefs.getUseFrequentRecordings()){
            swRecordMoreOften.setText("Record more often is ON");
        }else{
            swRecordMoreOften.setText("Record more often is OFF");
        }



        swUseFrequentUploads.setChecked(prefs.getUseFrequentUploads());

        if (prefs.getUseFrequentUploads()){
            swUseFrequentUploads.setText("Upload after every recording is ON");
        }else{
            swUseFrequentUploads.setText("Upload after every recording is OFF");
        }


       swPeriodicallyUpdateGPS.setChecked(prefs.getPeriodicallyUpdateGPS());
        if (prefs.getPeriodicallyUpdateGPS()){
            swPeriodicallyUpdateGPS.setText("Periodically update GPS is ON");
        }else{
            swPeriodicallyUpdateGPS.setText("Periodically update GPS is OFF");
        }
    }

}
