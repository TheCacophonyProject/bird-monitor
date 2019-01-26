package nz.org.cacophony.cacophonometer;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class WelcomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_welcome, container, false);

        setUserVisibleHint(true);

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
            boolean isFirstTime = prefs.getIsFirstTime();
            if (isFirstTime) {

                Util.displayHelp(getActivity(), getResources().getString(R.string.app_icon_name));
            }

            prefs.setIsFirstTimeFalse(); // this is important -stops help being displayed every time, and stops default settings being reapplied every time


        }
    }

}
