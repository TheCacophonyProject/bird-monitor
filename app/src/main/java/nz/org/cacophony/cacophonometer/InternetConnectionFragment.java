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
import android.widget.RadioButton;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class InternetConnectionFragment extends Fragment {

    private static final String TAG = "InternetConnectionFragment";


    private RadioButton rbNormal;
    private RadioButton rbOnline;
    private RadioButton rbOffline;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_internet_connection, container, false);

        setUserVisibleHint(false);

        rbNormal = (RadioButton) view.findViewById(R.id.rbNormal);
        rbOnline = (RadioButton) view.findViewById(R.id.rbOnline);
        rbOffline = (RadioButton) view.findViewById(R.id.rbOffline);

        displayOrHideGUIObjects();

        rbNormal.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Prefs prefs = new Prefs(getActivity());
                prefs.setInternetConnectionMode("normal");
            }
        });


        rbOnline.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Prefs prefs = new Prefs(getActivity());
                prefs.setInternetConnectionMode("online");
            }
        });


        rbOffline.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Prefs prefs = new Prefs(getActivity());
                prefs.setInternetConnectionMode("offline");
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

    void displayOrHideGUIObjects() {
        Prefs prefs = new Prefs(getActivity().getApplicationContext());
        String mode = prefs.getInternetConnectionMode();

        switch (mode) {

            case "normal":
                rbNormal.setChecked(true);
                break;
            case "offline":
                rbOffline.setChecked(true);
                break;
            case "online":
                rbOnline.setChecked(true);
                break;
        }
    }

}
