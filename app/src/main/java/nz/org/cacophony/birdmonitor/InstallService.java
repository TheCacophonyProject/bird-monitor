package nz.org.cacophony.birdmonitor;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

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
        Prefs prefs = new Prefs(getApplicationContext());
        prefs.setCrashlyticsUser();

        if (intent == null || intent.getExtras() == null) {
            Log.e(TAG, "Update Service with empty uri");
            FirebaseCrashlytics.getInstance().recordException(new Throwable(TAG + " Update Service null intent"));

            stopSelf();
            return START_NOT_STICKY;
        }
        String updateURL = intent.getStringExtra(Prefs.UPDATE_URI);
        if (updateURL == null) {
            Log.e(TAG, "Update intent with empty uri");
            FirebaseCrashlytics.getInstance().recordException(new Throwable(TAG + " Update intent with empty uri"));

            stopSelf();
            return START_NOT_STICKY;
        }

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "Cacophonometer:MainServiceWakelockTag");
        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);

        mServiceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                IPrivilegedService privService = IPrivilegedService.Stub.asInterface(service);

                IPrivilegedCallback callback = new IPrivilegedCallback.Stub() {
                    @Override
                    public void handleResult(String packageName, int returnCode) throws RemoteException {
                        if (wakeLock.isHeld()) {
                            wakeLock.release();
                        }
                        //this will only happen on error as if app replaced successfull service is stopped by update
                        Log.e(TAG, "handleResult for " + packageName + " result " + returnCode);
                        FirebaseCrashlytics.getInstance().recordException(new Throwable(TAG + " handleResult for " + packageName + " result " + returnCode));
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
                    prefs.setInternetRequired(false, Prefs.FLIGHT_MODE_PENDING_UPDATE);
                    Util.enableFlightMode(getApplicationContext());

                    privService.installPackage(Uri.parse(updateURL), Prefs.ACTION_INSTALL_REPLACE_EXISTING,
                            null, callback);

                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException", e);
                    FirebaseCrashlytics.getInstance().recordException(e);
                } catch (Exception e) {
                    Log.e(TAG, "RemoteException", e);
                    FirebaseCrashlytics.getInstance().recordException(e);
                } finally {
                    prefs.setInternetRequired(false, Prefs.FLIGHT_MODE_PENDING_UPDATE);
                    Util.enableFlightMode(getApplicationContext());
                    stopSelf();
                    if (wakeLock.isHeld()) {
                        wakeLock.release();
                    }
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
