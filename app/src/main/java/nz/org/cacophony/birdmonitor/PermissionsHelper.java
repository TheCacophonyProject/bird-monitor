package nz.org.cacophony.birdmonitor;
// https://stackoverflow.com/questions/34342816/android-6-0-multiple-permissions

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PermissionsHelper {

    private static final String TAG = "PermissionsHelper";

    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 100; // any code you want.

    public void checkAndRequestPermissions(Activity activity, String... permissions) {
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(permission);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
        }
    }

    public void onRequestPermissionsResult(Activity activity, int requestCode, String permissions[], int[] grantResults) {

        ArrayList<String> deniedPermissions = new ArrayList<>();

        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                String deniedPermission = permissions[i];
                try {
                    // Each permission seems to start with 'android.permission.' so have removed these parts to make message nicer
                    String[] parts = deniedPermission.split("\\.");
                    deniedPermission = parts[parts.length - 1];
                } catch (Exception ex) {
                    Log.e(TAG, ex.getLocalizedMessage());
                }
                deniedPermissions.add(deniedPermission);
            }
        }

        if (deniedPermissions.size() > 0) {
            // One or more permissions were denied
            StringBuilder message = new StringBuilder("The following permissions have not been granted:\n\n");
            for (String deniedPermission : deniedPermissions) {
                message.append(deniedPermission);
                message.append("\n\n");
            }
            message.append("The app cannot function properly without these permissions. Please consider granting these permissions in the phone\'s Settings.");

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(message)//
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() //https://stackoverflow.com/questions/11585099/alertdialog-show-new-alertdialog-builderthis-is-undefined
                    {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.getWindow().setBackgroundDrawableResource(android.R.color.white); // https://stackoverflow.com/questions/18346920/change-the-background-color-of-a-pop-up-dialog
            alert.show();
        }

    }
}

