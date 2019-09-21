package nz.org.cacophony.birdmonitor.views;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import nz.org.cacophony.birdmonitor.InputFilterMinMax;
import nz.org.cacophony.birdmonitor.Prefs;
import nz.org.cacophony.birdmonitor.R;
import nz.org.cacophony.birdmonitor.Util;

public class SunriseAlarmFragment extends Fragment {
    private static final String TAG = SunriseAlarmFragment.class.getName();
    private Switch swUseDawnDusk;
    private EditText etRecLength, etSunriseOffset, etNoonOffset, etSunsetOffset;
    private Button btnSave;
    private ConstraintLayout dawkDuskView;

    void updateValues(){
        Prefs prefs = new Prefs(this.getContext());
        prefs.setSunriseOffset(etSunriseOffset.getText().toString());
        prefs.setNoonOffset(etNoonOffset.getText().toString());
        prefs.setSunsetOffset(etSunsetOffset.getText().toString());
        prefs.setRecLength(etRecLength.getText().toString());
        loadValues();

    }
    void dawnDuskChecked(boolean checked){
        Prefs prefs = new Prefs(this.getContext());
        prefs.setUseDuskDawnAlarms(checked);
        toggleView(checked);
        Util.changeAlarmType(this.getContext());
    }

    void toggleView(boolean useDawnDusk) {
        if (useDawnDusk) {
            dawkDuskView.setVisibility(View.VISIBLE);
        }else{
            dawkDuskView.setVisibility(View.GONE);
        }
    }

    void loadValues(){
        Prefs prefs = new Prefs(this.getContext());
        boolean useDawkDusk = prefs.getUseDuskDawnAlarms();
        swUseDawnDusk.setChecked(useDawkDusk);
        toggleView(useDawkDusk);
        etRecLength.setText(String.valueOf(prefs.getRecLength()));
        etSunriseOffset.setText(String.valueOf(prefs.getSunriseOffset()));
        etNoonOffset.setText(String.valueOf(prefs.getNoonOffset()));
        etSunsetOffset.setText(String.valueOf(prefs.getSunsetOffset()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sunrise_alarm, container, false);
        setUserVisibleHint(false);
        swUseDawnDusk = view.findViewById(R.id.swDawnDuskAlarms);
        etRecLength = view.findViewById(R.id.etRecLength);
        etSunriseOffset = view.findViewById(R.id.etSunriseOffset);
        etNoonOffset = view.findViewById(R.id.etNoonOffset);
        etSunsetOffset = view.findViewById(R.id.etSunsetOffset);
        btnSave = view.findViewById(R.id.btnSave);
        dawkDuskView = view.findViewById(R.id.dawkDuskView);

        InputFilter[] offsetFilters = new InputFilter[]{new InputFilterMinMax(-Prefs.MAX_ALARM_OFFSET,Prefs.MAX_ALARM_OFFSET)};
        etSunriseOffset.setFilters(offsetFilters);
        etNoonOffset.setFilters(offsetFilters);
        etSunsetOffset.setFilters(offsetFilters);
        etRecLength.setFilters(new InputFilter[]{new InputFilterMinMax(Prefs.MIN_REC_LENGTH,Prefs.MAX_REC_LENGTH)});


        loadValues();

        swUseDawnDusk.setOnCheckedChangeListener((buttonView, isChecked) -> dawnDuskChecked(isChecked));
        btnSave.setOnClickListener(v -> updateValues());
        return view;
    }


}
