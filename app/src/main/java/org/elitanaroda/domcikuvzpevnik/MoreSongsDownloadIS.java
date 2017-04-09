package org.elitanaroda.domcikuvzpevnik;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Parcelable;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Class used for download an array of Songs contained within the intent
 */
public class MoreSongsDownloadIS extends IntentService {
    private static final String TAG = "MoreSongsDownloadService";
    private static final String PREFIX = "org.elitanaroda.domkvzpvnk.downloadsongintentservice";
    /**
     * The constant BROADCAST_STOP_BATCH_DOWNLOAD.
     */
    public static final String BROADCAST_STOP_BATCH_DOWNLOAD = PREFIX + ".STOP";
    private final static int NOTIFICATION_ID = 12;
    private PowerManager.WakeLock mWakeLock;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;
    private String result;
    private boolean aborted = false;

    /**
     * Instantiates a new More songs download is.
     */
    public MoreSongsDownloadIS() {
        super("MoreSongsDownloadIS");
    }

    /**
     * Decide which file to download and do it
     *
     * @param context App context
     * @param song    Song to download
     * @param pdfDir  the pdf dir
     * @return Null on successful download
     */
    public static String DownloadSong(Context context, Song song, String pdfDir) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        if (cm.isActiveNetworkMetered()) {
            return Download(context, pdfDir + song.getFileName(false), song.getmSongFileSmall());
        } else {
            return Download(context, pdfDir + song.getFileName(true), song.getmSongFileOriginal());
        }
    }

    /**
     * A very general method for downloading any file
     *
     * @param context        App context
     * @param urlToDownload  URL where the file is
     * @param downloadToFile Where the file should be saved
     * @return Null on successful download
     */
    public static String Download(Context context, String urlToDownload, File downloadToFile) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlToDownload);
            Log.v(TAG, url.toString());
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // ověření odpovědi 200, aby se neuložil error
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }

            // download the file
            input = new BufferedInputStream(url.openStream(), 8192);
            output = new FileOutputStream(downloadToFile);

            byte data[] = new byte[1024];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
            Log.v(TAG, downloadToFile.getAbsolutePath() + " - sucessful download!");
        } catch (Exception e) {
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
                Log.e(TAG, ignored.getMessage());
            }

            if (connection != null)
                connection.disconnect();
        }
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        setStopReceiver();
    }

    private void setStopReceiver() {
        IntentFilter gottaStopFilter = new IntentFilter(BROADCAST_STOP_BATCH_DOWNLOAD);
        LocalBroadcastManager.getInstance(this).
                registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        aborted = true;
                    }
                }, gottaStopFilter);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //We don't want the OS to go to sleep
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();

        result = null;
        String pdfUrl = intent.getStringExtra(PDFActivity.FILES_ROOT_KEY);
        if (intent.hasExtra(PDFActivity.SONG_ARRAY_KEY)) {
            Parcelable[] parcelables = intent.getParcelableArrayExtra(PDFActivity.SONG_ARRAY_KEY);
            downloadArray(parcelables, pdfUrl);
        } else {
            Log.e(TAG, "Intent didn't contain data to download");
            stopSelf();
        }
        mWakeLock.release();
    }

    /**
     * Converts the parcelables provided into Song objects and downloads them
     * @param parcelables
     */
    private void downloadArray(Parcelable[] parcelables, String pdfUrl) {
        int progress = 0;
        int total = parcelables.length;
        //Create the notification
        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_true)
                .setContentTitle("Downloading your files")
                .setContentText("Download in progress");
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        setNotificationClickable(mBuilder);
        try {
            for (Parcelable song : parcelables) {
                //Download cancelled by the user
                if (aborted) {
                    setNotificationToAborted();
                    break;
                }
                //Only download if the file isn't already downloaded
                if (!((Song) song).ismIsOnLocalStorage()) {
                    result = DownloadSong(this, (Song) song, pdfUrl);
                    if (result != null)
                        Log.e(TAG, ((Song) song).getFileName(true) + " not downloaded:\n" + result);
                }
                progress++;
                updateNotificationProgress(progress, total);
            }
            if (!aborted)
                updateNotificationDone();
        } catch (Exception e) {
            Log.e(TAG, "Array not SongArray?\n" + e.getMessage());
        }
    }

    /**
     * Causes the notification to be clickable, when it's clicked, goes to the MainActivity
     * @param builder The builder connected to the notification
     */
    private void setNotificationClickable(NotificationCompat.Builder builder) {
        Intent showActivityIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(showActivityIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
    }

    private void updateNotificationProgress(int progress, int total) {
        mBuilder.setProgress(total, progress, false)
                .setContentText(String.valueOf(progress) + "/" + String.valueOf(total));
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    /**
     * Shows the text "Download complete" in the notification and hides the progess bar
     */
    private void updateNotificationDone() {
        mBuilder.setContentText("Download complete")
                // Removes the progress bar
                .setProgress(0, 0, false);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    /**
     * Shows the text "Action aborted" in the notification
     */
    private void setNotificationToAborted() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher_true)
                .setContentTitle("Action aborted")
                .setContentText("User cancelled the operation");
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(NOTIFICATION_ID, mBuilder.build());
    }
}