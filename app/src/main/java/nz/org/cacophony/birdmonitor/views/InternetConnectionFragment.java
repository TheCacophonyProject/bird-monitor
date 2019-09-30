package nz.org.cacophony.birdmonitor.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.fragment.app.Fragment;

import nz.org.cacophony.birdmonitor.Prefs;
import nz.org.cacophony.birdmonitor.R;

public class InternetConnectionFragment extends Fragment {

    private static final String TAG = "InternetConnectionFragment";


    private AppCompatRadioButton rbNormal;
    private AppCompatRadioButton rbOnline;
    private AppCompatRadioButton rbOffline;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_internet_connection, container, false);

        setUserVisibleHint(false);

        rbNormal = view.findViewById(R.id.rbNormal);
        rbOnline = view.findViewById(R.id.rbOnline);
        rbOffline = view.findViewById(R.id.rbOffline);

        displayOrHideGUIObjects();

        rbNormal.setOnClickListener(v -> {
            Prefs prefs = new Prefs(getActivity());
            prefs.setInternetConnectionMode("normal");
        });


        rbOnline.setOnClickListener(v -> {
            Prefs prefs = new Prefs(getActivity());
            prefs.setInternetConnectionMode("online");
        });


        rbOffline.setOnClickListener(v -> {
            Prefs prefs = new Prefs(getActivity());
            prefs.setInternetConnectionMode("offline");
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
