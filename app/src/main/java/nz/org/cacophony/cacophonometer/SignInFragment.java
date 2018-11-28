package nz.org.cacophony.cacophonometer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class SignInFragment extends Fragment {
    private Button btnSignIn;
    private Button btnSkip;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_sign_in, container, false);
        btnSignIn = (Button) view.findViewById(R.id.btnSignIn);
        btnSkip = (Button) view.findViewById(R.id.btnSkip);

        btnSignIn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Sign in
            }
        });

        btnSkip.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ((SetupWizardActivity)getActivity()).setPageView(3); // 2 is the Create Account page
            }
        });


        return view;
    }

}
