package nz.org.cacophony.birdmonitor.views;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.opengl.Visibility;
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
import nz.org.cacophony.birdmonitor.UpdateUtil;
import nz.org.cacophony.birdmonitor.Util;

import org.fdroid.fdroid.privileged.IPrivilegedService;

public class RootedFragment extends Fragment {

    private static final String TAG = "RootedFragment";
    private Switch swAeroplane, swAutoUpdate;
    private Boolean hasInstallPermission = false;
    private Boolean updateAvailable = false;
    private Boolean isDownloading = false;

    private Button btnRootCheck, btnInstall, btnUpdateCheck;
    private TextView tvRooted, tvUpdateStatus, tvPrivStatus, tvVersion;
    private ServiceConnection mServiceConnection;
    private String versionName;
    private ConstraintLayout updateView;
    private UpdateUtil.LatestVersion latestVersion;

    void checkRootAccess() {
        Prefs prefs = new Prefs(this.getContext());
        boolean rooted = CommonUtils.isRooted(this.getContext());
        if (rooted) {
            tvRooted.setText(getString(R.string.rooted));
            btnRootCheck.setVisibility(View.GONE);
        } else {
            tvRooted.setText(getString(R.string.not_rooted));
            btnRootCheck.setVisibility(View.VISIBLE);
        }

        if (prefs.getAutoUpdateAllowed() && rooted) {
            prefs.setHasRootAccess(true);
            updateView.setVisibility(View.VISIBLE);
        } else {
            prefs.setHasRootAccess(false);
            updateView.setVisibility(View.INVISIBLE);
        }
    }

    void toggleInstallButton() {
        if (!hasInstallPermission || !updateAvailable) {
            btnInstall.setVisibility(View.INVISIBLE);
        } else {
            btnInstall.setVisibility(View.VISIBLE);
        }
        btnInstall.setEnabled(hasInstallPermission && updateAvailable && !isDownloading);
    }

    void checkSystemService() {
        mServiceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                IPrivilegedService privService = IPrivilegedService.Stub.asInterface(service);
                try {
                    hasInstallPermission = privService.hasPrivilegedPermissions();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (hasInstallPermission) {
                            tvPrivStatus.setText(getString(R.string.can_auto_update));
                        } else {
                            tvPrivStatus.setText(getString(R.string.no_update_permissions));
                        }
                        toggleInstallButton();

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
        serviceIntent.setPackage(Prefs.PRIVILEGED_EXTENSION_PACKAGE);
        this.getContext().bindService(serviceIntent, mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    void checkUpdate() {
        new CheckUpdateTask().execute();
    }

    void checkDownloadStatus() {
        if (UpdateUtil.isDownloading(this.getContext())) {
            tvUpdateStatus.setText(getString(R.string.downloading_version, versionName));
            isDownloading = true;
        } else {
            isDownloading = false;
        }
        toggleInstallButton();

    }

    void installUpdates() {
        if (UpdateUtil.downloadAPK(this.getContext(), latestVersion)) {
            new Prefs(this.getContext()).setRelaunchOnUpdate(true);
        }
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rooted, container, false);

        setUserVisibleHint(false);
        btnRootCheck = view.findViewById(R.id.btnRootCheck);
        tvRooted = view.findViewById(R.id.tvRooted);
        tvUpdateStatus = view.findViewById(R.id.tvUpdateStatus);
        btnInstall = view.findViewById(R.id.btnInstall);
        btnUpdateCheck = view.findViewById(R.id.btnUpdateCheck);
        tvPrivStatus = view.findViewById(R.id.tvPrivStatus);
        swAeroplane = view.findViewById(R.id.swAeroplane);
        swAutoUpdate = view.findViewById(R.id.swAutoUpdate);
        tvVersion = view.findViewById(R.id.tvVersion);
        updateView = view.findViewById(R.id.updateOptions);

        checkRootAccess();
        setVersionInfo();
        checkSystemService();
        checkUpdate();
        applySavedSettings();
        checkDownloadStatus();

        swAeroplane.setOnCheckedChangeListener((buttonView, isChecked) -> setAeroplaneMode(isChecked));
        swAutoUpdate.setOnCheckedChangeListener((buttonView, isChecked) -> setAutoUpdate(isChecked));
        btnRootCheck.setOnClickListener(v -> checkRootAccess());
        btnUpdateCheck.setOnClickListener(v -> checkUpdate());
        btnInstall.setOnClickListener(v -> installUpdates());

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mServiceConnection != null) {
            this.getContext().unbindService(mServiceConnection);
        }
    }

    class CheckUpdateTask extends AsyncTask<Void, Void, UpdateUtil.LatestVersion> {

        protected UpdateUtil.LatestVersion doInBackground(Void... voids) {
            latestVersion = UpdateUtil.getLatestVersion();
            return latestVersion;
        }

        protected void onPostExecute(UpdateUtil.LatestVersion version) {
            updateAvailable = false;
            if (version != null) {
                if (UpdateUtil.isNewerVersion(version.Name) && !latestVersion.DownloadURL.isEmpty()) {
                    updateAvailable = true;
                    tvUpdateStatus.setText(getString(R.string.update_available, version.Name));
                } else {
                    tvUpdateStatus.setText(getString(R.string.up_to_date));
                }
            } else {
                tvUpdateStatus.setText(getString(R.string.update_no_connection));
            }
            toggleInstallButton();
        }
    }


}
