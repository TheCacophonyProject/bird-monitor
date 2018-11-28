package nz.org.cacophony.cacophonometer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class WelcomeFragment extends Fragment {
    private Button btnStartSetup;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_welcome, container, false);
        btnStartSetup = (Button) view.findViewById(R.id.btnStartSetup);


        btnStartSetup.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ((SetupWizardActivity)getActivity()).setPageView(1); // 1 is the Create Account page
            }
        });






        return view;
    }


}
