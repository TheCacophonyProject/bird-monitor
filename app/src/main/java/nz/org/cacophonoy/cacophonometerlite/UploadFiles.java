package nz.org.cacophonoy.cacophonometerlite;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import static android.os.BatteryManager.BATTERY_STATUS_CHARGING;

class UploadFiles implements Runnable { // http://stackoverflow.com/questions/15666260/urlconnection-android-4-2-doesnt-work
    private static final String LOG_TAG = UploadFiles.class.getName();

    private Context context = null;


    UploadFiles(Context context) {
        if (context == null) {
            Log.e(LOG_TAG, "Making UploadFile without a context. context is needed for this.");
        }
        this.context = context;
    }


    private void sendFile(File aFile) {
        System.out.println("Going to upload file " + aFile.getName());
        Log.i(LOG_TAG, "sendFile: Start");

        // Get metadata and put into JSON.
        JSONObject audioRecording = new JSONObject();

        String fileName = aFile.getName();
        // http://stackoverflow.com/questions/3481828/how-to-split-a-string-in-java
        //http://stackoverflow.com/questions/3387622/split-string-on-dot-as-delimiter
        String[] fileNameParts = fileName.split("[. ]");

        String year = fileNameParts[0];
        String month = fileNameParts[1];
        String day = fileNameParts[2];
        String hour = fileNameParts[3];
        String minute = fileNameParts[4];
        String second = fileNameParts[5];

        String localFilePath = "/data/data/com.thecacophonytrust.cacophonometer/app_cacophony/recordings/" + fileName;
        String recordingDateTime = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
        String recordingTime = hour + ":" + minute + ":" + second;

        boolean batteryCharging = isBatteryCharging();
        float batteryLevel = getBatteryLevel();


        Prefs prefs = new Prefs(context);

        try {
            audioRecording.put("location", "Lat: " + prefs.getLatitude() + ", Lon: " + prefs.getLongitude());
            audioRecording.put("duration", 6);
            audioRecording.put("localFilePath", localFilePath);
            audioRecording.put("recordingDateTime", recordingDateTime);
            audioRecording.put("recordingTime", recordingTime);

            audioRecording.put("batteryCharging", batteryCharging);
            audioRecording.put("batteryLevel", batteryLevel);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Server.uploadAudioRecording(aFile, audioRecording, context);
    }

    @Override
    public void run() {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        try {
            // http://stackoverflow.com/questions/3930990/android-how-to-enable-disable-wifi-or-internet-connection-programmatically

            if (!wifi.isWifiEnabled()) {
                wifi.setWifiEnabled(true);
            }
            File recordingsFolder = Util.getRecordingsFolder();
            File recordingFiles[] = recordingsFolder.listFiles();
            if (recordingFiles != null) {
                for (File aFile : recordingFiles) {
                    System.out.println("File name is " + aFile.getName());
                    sendFile(aFile);
                    if (!aFile.delete())
                        Log.w(LOG_TAG, "Deleting audio file failed");
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        } finally {
            if (wifi.isWifiEnabled()) {
                wifi.setWifiEnabled(false);
            }
        }
    }

    public boolean isBatteryCharging() {
        // https://developer.android.com/training/monitoring-device-state/battery-monitoring.html
        // http://stackoverflow.com/questions/24934260/intentreceiver-components-are-not-allowed-to-register-to-receive-intents-when
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.getApplicationContext().registerReceiver(null, ifilter);
        // Are we charging / charged?
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
            return true;
        } else {
            return false;
        }
    }

    public float getBatteryLevel() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.getApplicationContext().registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float) scale;
        return batteryPct;
    }
}
