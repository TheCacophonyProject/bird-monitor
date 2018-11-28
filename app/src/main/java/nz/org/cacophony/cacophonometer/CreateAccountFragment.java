package nz.org.cacophony.cacophonometer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class CreateAccountFragment extends Fragment {

    private Button btnSignUp;
    private Button btnSkip;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_create_account, container, false);
        btnSignUp = (Button) view.findViewById(R.id.btnSignUp);
        btnSkip = (Button) view.findViewById(R.id.btnSkip);


        btnSignUp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Create Account
            }
        });

        btnSkip.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ((SetupWizardActivity)getActivity()).setPageView(2); // 2 is the Create Account page
            }
        });



        return view;
    }

}
