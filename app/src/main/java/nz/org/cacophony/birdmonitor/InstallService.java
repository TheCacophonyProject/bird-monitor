package nz.org.cacophony.birdmonitor;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.fdroid.fdroid.privileged.IPrivilegedCallback;
import org.fdroid.fdroid.privileged.IPrivilegedService;

/**
 * This service is used to bind to privileged extension system app and request
 * a local APK file to be installed. A full service is required to ensure it stays
 * alive while binding and calling the privileged-extension service
 */

public class InstallService extends Service {
    private static final String TAG = InstallService.class.getName();
    private ServiceConnection mServiceConnection;

    public InstallService() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mServiceConnection != null) {
            unbindService(mServiceConnection);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String updateURL = intent.getStringExtra(Prefs.UPDATE_URI);
        if (updateURL == null) {
            Log.e(TAG, "Update intent with empty uri");
            stopSelf();
            return START_STICKY;
        }

        mServiceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                IPrivilegedService privService = IPrivilegedService.Stub.asInterface(service);

                IPrivilegedCallback callback = new IPrivilegedCallback.Stub() {
                    @Override
                    public void handleResult(String packageName, int returnCode) throws RemoteException {
                        //this will only happen on error as if app replaced successfull service is stopped by update
                        Log.e(TAG, "handleResult for " + packageName + " result " + returnCode);
                        unbindService(mServiceConnection);
                        stopSelf();
                    }
                };

                try {
                    boolean hasPermissions = privService.hasPrivilegedPermissions();
                    if (!hasPermissions) {
                        return;
                    }

                    Prefs prefs = new Prefs(getApplicationContext());
                    if (prefs.getFlightModePending()) {
                        Util.enableFlightMode(getApplicationContext());
                    }
                    privService.installPackage(Uri.parse(updateURL), Prefs.ACTION_INSTALL_REPLACE_EXISTING,
                            null, callback);

                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException", e);

                } catch (Exception e) {
                    Log.e(TAG, "RemoteException", e);
                }finally {
                    stopSelf();
                }
            }

            public void onServiceDisconnected(ComponentName name) {
            }
        };

        Intent serviceIntent = new Intent(Prefs.PRIVILEGED_EXTENSION_SERVICE_INTENT);
        serviceIntent.setPackage(Prefs.PRIVILEGED_EXTENSION_PACKAGE);
        this.bindService(serviceIntent, mServiceConnection,
                Context.BIND_AUTO_CREATE);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
