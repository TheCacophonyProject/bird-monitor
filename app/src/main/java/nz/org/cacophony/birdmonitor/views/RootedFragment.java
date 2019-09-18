package nz.org.cacophony.birdmonitor.views;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import io.fabric.sdk.android.services.common.CommonUtils;
import nz.org.cacophony.birdmonitor.Prefs;
import nz.org.cacophony.birdmonitor.R;
import nz.org.cacophony.birdmonitor.Util;

import org.fdroid.fdroid.privileged.IPrivilegedService;

public class RootedFragment extends Fragment {
    private static final String TAG = "RootedFragment";
    private Switch swAeroplane, swAutoUpdate;
    private Button btnRoot, btnInstall, btnUpdateCheck;
    private TextView tvMessages, tvRooted, tvUpdateStatus, tvPrivStatus, tvVersion;
    private ServiceConnection mServiceConnection;
    private String versionName;
    private ConstraintLayout rootView;
    private Util.LatestVersion latestVersion;

    void checkRootAccess() {
        Prefs prefs = new Prefs(this.getContext());
        if (CommonUtils.isRooted(this.getContext())) {
            tvRooted.setText(getString(R.string.rooted));
            prefs.setHasRootAccess(true);
            rootView.setVisibility(View.VISIBLE);
            btnRoot.setVisibility(View.GONE);
        } else {
            prefs.setHasRootAccess(false);
            tvRooted.setText(getString(R.string.not_rooted));
            rootView.setVisibility(View.INVISIBLE);
            btnRoot.setVisibility(View.VISIBLE);
        }
    }


    void checkSystemService() {
        mServiceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                IPrivilegedService privService = IPrivilegedService.Stub.asInterface(service);
                try {
                    boolean hasPermissions = privService.hasPrivilegedPermissions();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (hasPermissions) {
                            tvPrivStatus.setText(getString(R.string.can_auto_update));
                        } else {
                            tvPrivStatus.setText(getString(R.string.can_auto_update) + ", without permission");
                            btnInstall.setEnabled(false);

                        }
                    });
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException", e);

                } catch (Exception e) {
                    Log.e(TAG, "RemoteException", e);

                }
            }

            public void onServiceDisconnected(ComponentName name) {
            }
        };

        Intent serviceIntent = new Intent(Prefs.PRIVILEGED_EXTENSION_SERVICE_INTENT);
        serviceIntent.setPackage("org.fdroid.fdroid.privileged");
        this.getContext().bindService(serviceIntent, mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    void checkUpdate() {
        new CheckUpdateTask().execute();
    }

    void checkDownloadStatus() {
        if (Util.isDownloading(this.getContext())) {
            tvUpdateStatus.setText("Downloading " + versionName);
            btnInstall.setEnabled(false);
        }
    }

    void installUpdates() {
        Util.downloadAPK(this.getContext(), latestVersion);
        tvUpdateStatus.setText("Downloading " + latestVersion.Name);
        checkDownloadStatus();
    }

    void setVersionInfo() {
        versionName = Util.getVersionName();
        if (versionName == null) {
            tvVersion.setText("");
        } else {
            versionName = "v" + versionName;
            tvVersion.setText(versionName);
        }
    }

    void applySavedSettings() {
        Prefs prefs = new Prefs(this.getContext());
        swAeroplane.setChecked(prefs.getAeroplaneMode());
        swAutoUpdate.setChecked(prefs.getAutoUpdate());
    }

    void setAeroplaneMode(boolean checked) {
        Prefs prefs = new Prefs(this.getContext());
        prefs.setAeroplaneMode(checked);
    }

    void setAutoUpdate(boolean checked) {
        Prefs prefs = new Prefs(this.getContext());
        prefs.setAutoUpdate(checked);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.getContext().unbindService(mServiceConnection);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rooted, container, false);

        setUserVisibleHint(false);
        tvMessages = view.findViewById(R.id.tvMessages);
        btnRoot = view.findViewById(R.id.btnRoot);
        tvRooted = view.findViewById(R.id.tvRooted);
        tvUpdateStatus = view.findViewById(R.id.tvUpdateStatus);
        btnInstall = view.findViewById(R.id.btnInstall);
        btnUpdateCheck = view.findViewById(R.id.btnUpdateCheck);
        tvPrivStatus = view.findViewById(R.id.tvPrivStatus);
        swAeroplane = view.findViewById(R.id.swAeroplane);
        swAutoUpdate = view.findViewById(R.id.swAutoUpdate);
        tvVersion = view.findViewById(R.id.tvVersion);
        rootView = view.findViewById(R.id.rootOptions);

        checkRootAccess();
        setVersionInfo();
        checkSystemService();
        checkUpdate();
        applySavedSettings();
        checkDownloadStatus();
        swAeroplane.setOnCheckedChangeListener((buttonView, isChecked) -> setAeroplaneMode(isChecked));
        swAutoUpdate.setOnCheckedChangeListener((buttonView, isChecked) -> setAutoUpdate(isChecked));
        btnRoot.setOnClickListener(v -> checkRootAccess());
        btnUpdateCheck.setOnClickListener(v -> checkUpdate());
        btnInstall.setOnClickListener(v -> installUpdates());

        return view;
    }

    class CheckUpdateTask extends AsyncTask<Void, Void, Util.LatestVersion> {

        protected Util.LatestVersion doInBackground(Void... voids) {
            latestVersion = Util.getLatestVersion();
            return latestVersion;
        }

        protected void onPostExecute(Util.LatestVersion version) {

            if (version != null) {
                if ( Util.isNewerVersion(version.Name)) {
                    tvUpdateStatus.setText(version.Name + " of Bird Monitor is available");
                    btnInstall.setEnabled(true);
                } else {
                    tvUpdateStatus.setText(getString(R.string.up_to_date));
                    btnInstall.setEnabled(false);
                }
            } else {
                btnInstall.setEnabled(false);
                tvUpdateStatus.setText("Can't connect to check updates");
            }
        }
    }


}
