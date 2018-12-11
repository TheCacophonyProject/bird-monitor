package nz.org.cacophony.cacophonometer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class TestingFragment extends Fragment {

    private static final String TAG = "TestingFragment";


    private Switch swUseTestServer;
    private Switch swUseVeryFrequentRecordings;
    private Switch swShortRecordings;
    private Button btnFinished;
    private TextView tvMessages;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_testing, container, false);

        setUserVisibleHint(false);
        tvMessages = (TextView) view.findViewById(R.id.tvMessages);
        btnFinished = (Button) view.findViewById(R.id.btnFinished);
        btnFinished.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                ((AdvancedWizardActivity)getActivity()).nextPageView();
            }
        });

        swUseTestServer = view.findViewById(R.id.swUseTestServer);
        swUseTestServer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // https://stackoverflow.com/questions/17372750/android-setoncheckedchangelistener-calls-again-when-old-view-comes-back
                if (!buttonView.isShown()){
                    return;
                }

                Util.setUseTestServer(getActivity().getApplicationContext(), isChecked);

                final Switch swUseShortRecordings = getView().findViewById(R.id.swShortRecordings);
                swUseShortRecordings.setEnabled(isChecked);

                final Switch swUseVeryFrequentRecordings = getView().findViewById(R.id.swUseVeryFrequentRecordings);
                swUseVeryFrequentRecordings.setEnabled(isChecked);

                if (!isChecked){
                    Prefs prefs = new Prefs(getActivity().getApplicationContext());
                    prefs.setUseShortRecordings(false);
                    swUseShortRecordings.setChecked(false);
                    swUseShortRecordings.setEnabled(false);

                    prefs.setUseVeryFrequentRecordings(false);
                    swUseVeryFrequentRecordings.setChecked(false);
                    swUseVeryFrequentRecordings.setEnabled(false);
                }

            }
        });

        swUseVeryFrequentRecordings = view.findViewById(R.id.swUseVeryFrequentRecordings);
        swUseVeryFrequentRecordings.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!buttonView.isShown()){
                    return;
                }
                Util.setUseVeryFrequentRecordings(getActivity().getApplicationContext(), isChecked);
            }
        });

         swShortRecordings = view.findViewById(R.id.swShortRecordings);
        swShortRecordings.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!buttonView.isShown()){
                    return;
                }
                Prefs prefs = new Prefs(getActivity().getApplicationContext());
                prefs.setUseShortRecordings(isChecked);
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
            Prefs prefs = new Prefs(getActivity().getApplicationContext());

            boolean useTestServer = prefs.getUseTestServer();
            swUseTestServer = getView().findViewById(R.id.swUseTestServer);
            swUseTestServer.setChecked(useTestServer);

            boolean useVeryFrequentRecordings = prefs.getUseVeryFrequentRecordings();
            swUseVeryFrequentRecordings = getView().findViewById(R.id.swUseVeryFrequentRecordings);
            swUseVeryFrequentRecordings.setChecked(useVeryFrequentRecordings);
            swUseVeryFrequentRecordings.setEnabled(useTestServer);// only enabled if using test server

            boolean useShortRecordings = prefs.getUseShortRecordings();
            swShortRecordings = getView().findViewById(R.id.swShortRecordings);
            swShortRecordings.setChecked(useShortRecordings);
            swShortRecordings.setEnabled(useTestServer); // only enabled if using test server


        }
    }








}
