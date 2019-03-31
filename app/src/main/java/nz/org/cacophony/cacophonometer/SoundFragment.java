package nz.org.cacophony.cacophonometer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

public class SoundFragment extends Fragment {

    private Switch swPlayWarningSound;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_sound, container, false);

        setUserVisibleHint(false);
        swPlayWarningSound = view.findViewById(R.id.swPlayWarningSound);

        displayOrHideGUIObjects();

        swPlayWarningSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Prefs prefs = new Prefs(getActivity());
                prefs.setPlayWarningSound(swPlayWarningSound.isChecked());
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

    void displayOrHideGUIObjects() {
        Prefs prefs = new Prefs(getActivity());
        swPlayWarningSound.setChecked(prefs.getPlayWarningSound());
        if (prefs.getPlayWarningSound()){
            swPlayWarningSound.setText("Warning sound is ON");
        }else{
            swPlayWarningSound.setText("Warning sound is OFF");
        }
    }

}
