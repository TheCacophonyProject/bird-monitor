package nz.org.cacophony.birdmonitor;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.Tag;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.fdroid.fdroid.privileged.IPrivilegedCallback;
import org.fdroid.fdroid.privileged.IPrivilegedService;

import java.io.File;
import static android.content.Context.DOWNLOAD_SERVICE;

public class UpdateInstaller extends BroadcastReceiver {


    private static final String TAG = UpdateInstaller.class.getName();
    private ServiceConnection mServiceConnection;

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            long downloadId = intent.getLongExtra(
                    DownloadManager.EXTRA_DOWNLOAD_ID, 0);

            DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            Cursor c = downloadManager.query(query);
            String localPath = null;
            if (c.moveToFirst()) {
                localPath = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            }
            c.close();
            if(localPath != null) {
                Intent mainServiceIntent = new Intent(Prefs.UPDATE_INTENT,null,context, UpdateService.class);
                mainServiceIntent.putExtra(Prefs.UPDATE_URI, localPath);
                context.startService(mainServiceIntent);
            }else{
                Log.e(TAG, "Could not find downloaded path");
            }

        }
    }

}