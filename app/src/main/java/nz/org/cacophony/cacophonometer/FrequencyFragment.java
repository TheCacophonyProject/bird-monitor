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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class FrequencyFragment extends Fragment {

    private static final String TAG = "FrequencyFragment";


    private Switch swRecordMoreOften;
    private Switch swUseFrequentUploads;
    private Switch swPeriodicallyUpdateGPS;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_sound, container, false);

        setUserVisibleHint(false);

        swRecordMoreOften = (Switch) view.findViewById(R.id.swRecordMoreOften);
        swRecordMoreOften.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Util.setUseFrequentRecordings(getActivity().getApplicationContext(), swRecordMoreOften.isChecked());
            }
        });

        swUseFrequentUploads = (Switch) view.findViewById(R.id.swUseFrequentUploads);
        swUseFrequentUploads.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Util.setUseFrequentUploads(getActivity().getApplicationContext(), swUseFrequentUploads.isChecked());
            }
        });

        swPeriodicallyUpdateGPS = (Switch) view.findViewById(R.id.swPeriodicallyUpdateGPS);
        swPeriodicallyUpdateGPS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Util.setPeriodicallyUpdateGPS(getActivity().getApplicationContext(), swPeriodicallyUpdateGPS.isChecked());
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

            Prefs prefs = new Prefs(getActivity());
            ((Switch) getView().findViewById(R.id.swRecordMoreOften)).setChecked(prefs.getUseFrequentRecordings());
            ((Switch) getView().findViewById(R.id.swUseFrequentUploads)).setChecked(prefs.getUseFrequentUploads());
            ((Switch) getView().findViewById(R.id.swPeriodicallyUpdateGPS)).setChecked(prefs.getPeriodicallyUpdateGPS());

        }
    }








}
