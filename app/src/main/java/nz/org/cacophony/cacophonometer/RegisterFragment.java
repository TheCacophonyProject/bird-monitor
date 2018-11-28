package nz.org.cacophony.cacophonometer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class RegisterFragment extends Fragment {

    private Button btnBack;
    private Button btnNext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_register, container, false);

        btnBack = (Button) view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ((SetupWizardActivity)getActivity()).previousPageView();

            }
        });

        btnNext = (Button) view.findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ((SetupWizardActivity)getActivity()).nextPageView();
            }
        });



        return view;
    }

}
