package nz.org.cacophony.birdmonitor;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Methods used for auto updating Bird Monitor
 */
public class UpdateUtil {
    private static final String TAG = UpdateUtil.class.getName();


    public static boolean isDownloading(Context context) {
        DownloadManager downloadManager = (DownloadManager)
                context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();

        query.setFilterByStatus(DownloadManager.STATUS_RUNNING);
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
            c.close();
            return true;
        }
        return false;
    }

    private static void deleteIfExists(String filename) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
        if (file.exists()) {
            Log.d(TAG, "deleting " + file.getAbsoluteFile());
            file.delete();
        }
    }

    // Request DownloadManager to downlaod the requested version
    public static boolean downloadAPK(Context context, LatestVersion latestVersion) {
        if (isDownloading(context)) {
            return false;
        }
        deleteIfExists("bird-monitor-latest.apk");
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(latestVersion.DownloadURL));
        request.setDescription("Getting Bird Monitor " + latestVersion.Name);
        request.setTitle("Downloading Bird Monitor");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        }
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "bird-monitor-latest.apk");
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
        return true;
    }

    public static boolean isNewerVersion(String versionName) {
        Pattern p = Pattern.compile(".*(\\d+)[\\.](\\d+)[\\.](\\d+)");
        Matcher currentMatches = p.matcher(Util.getVersionName());
        Matcher newVersionMatches = p.matcher(versionName);
        int curV, newV;
        if (newVersionMatches.matches() && currentMatches.matches()) {
            for (int i = 0; i < currentMatches.groupCount(); i++) {
                curV = Integer.parseInt(currentMatches.group(i + 1));
                if (i < newVersionMatches.groupCount()) {
                    newV = Integer.parseInt(newVersionMatches.group(i + 1));
                    if (newV > curV) {
                        return true;
                    } else if (newV < curV) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    // Download and install updated apk if a newer version exists
    public static boolean updateIfAvailable(Context context) {
        LatestVersion latestVersion = getLatestVersion();
        if (latestVersion != null && isNewerVersion(latestVersion.Name)) {
            downloadAPK(context, latestVersion);
            return true;
        }
        return false;
    }


    public static boolean isAutoUpdateAllowed(Context context) {
        try {
            byte[] currentPackageCert = getPackageCertificate(context, BuildConfig.APPLICATION_ID);
            for (String whitelistHashString : Prefs.ALLOWED_UPDATES) {
                byte[] whitelistHash = hexStringToByteArray(whitelistHashString);
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] packageHash = digest.digest(currentPackageCert);

                boolean packageCertMatches = Arrays.equals(whitelistHash, packageHash);
                if (packageCertMatches) {
                    Log.d(TAG, "Auto update is allowed");
                    return true;
                }
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }
        Log.d(TAG, "Auto update is not allowed");
        return false;
    }

    private static byte[] getPackageCertificate(Context context, String packageName) {
        try {
            // we do check the byte array of *all* signatures
            @SuppressLint("PackageManagerGetSignatures")
            PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);

            // NOTE: Silly Android API naming: Signatures are actually certificates
            Signature[] certificates = pkgInfo.signatures;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            for (Signature cert : certificates) {
                outputStream.write(cert.toByteArray());
            }

            // Even if an apk has several certificates, these certificates should never change
            // Google Play does not allow the introduction of new certificates into an existing apk
            // Also see this attack: http://stackoverflow.com/a/10567852
            return outputStream.toByteArray();
        } catch (PackageManager.NameNotFoundException | IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }


    public static LatestVersion getLatestVersion() {
        try {
            URL u = new URL(Prefs.UPDATE_CHECK_URL);
            HttpURLConnection conn = null;

            conn = (HttpURLConnection) u.openConnection();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()), 8192);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                reader.close();

                JSONObject jObj = new JSONObject(sb.toString());
                JSONArray assets = jObj.getJSONArray("assets");
                String latestDownload = "";
                for (int i = 0; i < assets.length(); i++) {
                    JSONObject asset = assets.getJSONObject(i);
                    String downloadURL = asset.getString("browser_download_url");
                    if (downloadURL.endsWith(("apk"))) {
                        latestDownload = downloadURL;
                        break;
                    }
                }
                if (latestDownload == null) {
                    return null;
                }
                String version = jObj.getString("tag_name");
                return new LatestVersion(version, latestDownload);
            }
        } catch (JSONException | IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static class LatestVersion {
        public String Name;
        public String DownloadURL;

        public LatestVersion(String name, String downloadURL) {
            this.Name = name;
            this.DownloadURL = downloadURL;
        }
    }
}
