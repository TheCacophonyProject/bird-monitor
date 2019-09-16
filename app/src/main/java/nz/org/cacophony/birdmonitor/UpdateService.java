package nz.org.cacophony.birdmonitor;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import org.fdroid.fdroid.privileged.IPrivilegedCallback;
import org.fdroid.fdroid.privileged.IPrivilegedService;

public class UpdateService  extends IntentService {
    private static final String TAG = UpdateService.class.getName();
    private ServiceConnection mServiceConnection;

    public UpdateService() {
        super("UpdateService");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String action = intent.getAction();
        Log.d(TAG,"Received intent" + action);
        if(action.equals(Prefs.UPDATE_INTENT)) {
            String updateURL = intent.getStringExtra(Prefs.UPDATE_URI);

            if(updateURL == null){
                Log.e(TAG,"Update intent with empty uri");
                return;
            }
            Log.d(TAG, "Installing update from " + updateURL);
             mServiceConnection = new ServiceConnection() {
                public void onServiceConnected(ComponentName name, IBinder service) {
                    IPrivilegedService privService = IPrivilegedService.Stub.asInterface(service);

                    IPrivilegedCallback callback = new IPrivilegedCallback.Stub() {
                        @Override
                        public void handleResult(String packageName, int returnCode) throws RemoteException {
                            Log.d("install", "return code " + returnCode);
                            unbindService(mServiceConnection);
                        }
                    };

                    try {
                        boolean hasPermissions = privService.hasPrivilegedPermissions();
                        if (!hasPermissions) {
                            return;
                        }

                        Prefs prefs = new Prefs(getApplicationContext());
                        if(prefs.getFlightModePending()) {
                            Util.enableFlightMode(getApplicationContext());
                        }
                        Util.createFailSafeAlarm(getApplicationContext());

                        privService.installPackage(Uri.parse(updateURL), Prefs.ACTION_INSTALL_REPLACE_EXISTING,
                                null, callback);

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
            this.bindService(serviceIntent, mServiceConnection,
                    Context.BIND_AUTO_CREATE);
            }
    }
}