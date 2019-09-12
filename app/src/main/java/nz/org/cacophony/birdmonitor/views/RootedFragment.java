package nz.org.cacophony.birdmonitor.views;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
>>>>>>> Stashed changes
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import nz.org.cacophony.birdmonitor.Prefs;
import nz.org.cacophony.birdmonitor.R;
import nz.org.cacophony.birdmonitor.Util;

import org.fdroid.fdroid.privileged.IPrivilegedCallback;
import org.fdroid.fdroid.privileged.IPrivilegedService;

import java.net.URI;

public class RootedFragment extends Fragment {
//
     private IPrivilegedService service;
//    private RemoteServiceConnection serviceConnection;
    private static final String TAG = "RootedFragment";
    private static final String PRIVILEGED_EXTENSION_SERVICE_INTENT
            = "org.fdroid.fdroid.privileged.IPrivilegedService";
    public static final int ACTION_INSTALL_REPLACE_EXISTING = 2;

    private Switch swRooted;
    private Button btnFinished;
    private TextView tvMessages;

    protected void installPackageInternal(Context context) {
        Log.e("install","install packaged internal");
        ServiceConnection mServiceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.e("Install","Connected");
                IPrivilegedService privService = IPrivilegedService.Stub.asInterface(service);

                IPrivilegedCallback callback = new IPrivilegedCallback.Stub() {
                    @Override
                    public void handleResult(String packageName, int returnCode) throws RemoteException {
                      Log.d("install", "return ccode " + returnCode);
                    }
                };

                try {
                    boolean hasPermissions = privService.hasPrivilegedPermissions();
                    Log.e("Install","permissions?" + hasPermissions);

                    if (!hasPermissions) {
                        return;
                    }

                    privService.installPackage(Uri.parse("http://www.google.com"), ACTION_INSTALL_REPLACE_EXISTING,
                            null, callback);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException", e);

                }catch(Exception e){
                    Log.e(TAG, "RemoteException", e);

                }
            }

            public void onServiceDisconnected(ComponentName name) {
            }
        };

        Intent serviceIntent = new Intent(PRIVILEGED_EXTENSION_SERVICE_INTENT);
        serviceIntent.setPackage("org.fdroid.fdroid.privileged");
        context.getApplicationContext().bindService(serviceIntent, mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rooted, container, false);

        setUserVisibleHint(false);
        tvMessages = view.findViewById(R.id.tvMessages);
        swRooted = view.findViewById(R.id.swRooted);
        btnFinished = view.findViewById(R.id.btnFinished);

        displayOrHideGUIObjects();

        btnFinished.setOnClickListener(v -> ((AdvancedWizardActivity) getActivity()).nextPageView());

        swRooted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs prefs = new Prefs(getActivity());
            prefs.setHasRootAccess(swRooted.isChecked());
            displayOrHideGUIObjects();
            if (swRooted.isChecked()) {
                Util.checkSuperUserAccess();
            }
        });
        installPackageInternal(this.getContext());
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
        Prefs prefs = new Prefs(getActivity());
        swRooted.setChecked(prefs.getHasRootAccess());

        if (prefs.getHasRootAccess()) {
            swRooted.setText("YES");
        } else {
            swRooted.setText("NO");
        }

        if (prefs.getVeryAdvancedSettingsEnabled()) {
            btnFinished.setVisibility(View.INVISIBLE);
        } else {
            btnFinished.setVisibility(View.VISIBLE);
        }
    }


}
