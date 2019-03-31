package nz.org.cacophony.cacophonometer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

public class RootedFragment extends Fragment {

    private Switch swRooted;
    private Button btnFinished;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_rooted, container, false);

        setUserVisibleHint(false);
        swRooted = view.findViewById(R.id.swRooted);
        btnFinished = view.findViewById(R.id.btnFinished);

        displayOrHideGUIObjects();

            btnFinished.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    ((AdvancedWizardActivity)getActivity()).nextPageView();
                }
            });

        swRooted.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Prefs prefs = new Prefs(getActivity());
                prefs.setHasRootAccess(swRooted.isChecked());
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
        swRooted.setChecked(prefs.getHasRootAccess());

        if (prefs.getHasRootAccess()){
            swRooted.setText("YES");
        }else{
            swRooted.setText("NO");
        }

        if (prefs.getSettingsForTestServerEnabled()){
            btnFinished.setVisibility(View.INVISIBLE);
        }else{
            btnFinished.setVisibility(View.VISIBLE);
        }
    }


}
