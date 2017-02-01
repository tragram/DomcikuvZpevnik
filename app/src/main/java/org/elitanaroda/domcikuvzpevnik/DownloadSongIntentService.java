package org.elitanaroda.domcikuvzpevnik;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.os.PowerManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadSongIntentService extends IntentService {
    private static final String TAG = "SongDownloadService";

    private static final String PREFIX = "org.elitanaroda.domkvzpvnk.downloadsongintentservice";
    public static final String BROADCAST_DOWNLOAD_FINISHED = PREFIX + ".FINISHED";
    public static final String BROADCAST_SHOW_ERROR = PREFIX + ".SHOW";
    public static final String BROADCAST_PROGRESS_UPDATE = PREFIX + ".PROGRESS";
    public static final String BROADCAST_STOP_BATCH_DOWNLOAD = PREFIX + ".STOP";

    private static final String PDF_DIR = "http://elitanaroda.org/zpevnik/pdfs/";
    private final static int NOTIFICATION_ID = 12;
    private Song mSong;
    private PowerManager.WakeLock mWakeLock;

    private boolean shouldContinue = true;

    public DownloadSongIntentService() {
        super("DownloadSongIntentService");
    }

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

            //pro výpočet % stažených
            int fileLength = connection.getContentLength();

            // download the file
            input = new BufferedInputStream(url.openStream(), 8192);
            output = new FileOutputStream(downloadToFile);

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;

                // publishing the progress only if total length is known
                if (fileLength > 0) {
                    Intent localIntent = new Intent(BROADCAST_PROGRESS_UPDATE);
                    localIntent.putExtra("progress", (int) (total * 100 / fileLength));
                    LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
                }
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
        IntentFilter gottaStopFilter =
                new IntentFilter(BROADCAST_STOP_BATCH_DOWNLOAD);
        LocalBroadcastManager.getInstance(this).
                registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Log.w(TAG, "BROADCAST RECEIVED");
                        shouldContinue = false;
                    }
                }, gottaStopFilter);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();

        String result = null;
        if (intent.hasExtra(PDFActivity.SONG_KEY)) {
            this.mSong = intent.getParcelableExtra(PDFActivity.SONG_KEY);
            result = DownloadSong(this, mSong);
        } else if (intent.hasExtra(PDFActivity.SONG_ARRAY_KEY)) {
            Parcelable[] parcelable = intent.getParcelableArrayExtra(PDFActivity.SONG_ARRAY_KEY);

            int progress = 0;
            int total = parcelable.length;
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher_true)
                            .setContentTitle("Downloading your files")
                            .setContentText("Download in progress");
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Intent showActivityIntent = new Intent(this, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);

            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(showActivityIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);

            try {
                for (Parcelable song : parcelable) {
                    if (!shouldContinue) {
                        setNotificationToAborted();
                        break;
                    }
                    if (!((Song) song).ismIsOnLocalStorage()) {
                        result = DownloadSong(this, (Song) song);
                        if (result != null)
                            Log.e(TAG, ((Song) song).getFileName() + " not downloaded:\n" + result);
                    }
                    progress++;
                    mBuilder.setProgress(total, progress, false);
                    mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
                    mBuilder.setContentText("Download complete")
                            // Removes the progress bar
                            .setProgress(0, 0, false);
                    mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
                }
            } catch (Exception e) {
                Log.e(TAG, "Array not SongArray?\n" + e.getMessage());
            }

        } else {
            Log.e(TAG, "Intent didnt contain data to download");
            stopSelf();
        }

        if (result != null) {
            Intent localIntent = new Intent(BROADCAST_SHOW_ERROR);
            localIntent.putExtra(PDFActivity.MESSAGE_KEY, "Download error:\n" + result)
                    .putExtra(PDFActivity.RETRY_KEY, true);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            Log.e(TAG, result);
        } else if (intent.getBooleanExtra("openPDF", true)) {
            Intent localIntent = new Intent(BROADCAST_DOWNLOAD_FINISHED);
            localIntent.putExtra(PDFActivity.SONG_KEY, mSong)
                    .putExtra(PDFActivity.MESSAGE_KEY, "File downloaded!")
                    .putExtra(PDFActivity.RETRY_KEY, false);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }

        mWakeLock.release();
    }

    private void setNotificationToAborted() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher_true)
                .setContentTitle("Action aborted")
                .setContentText("User cancelled the operation");
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(NOTIFICATION_ID, mBuilder.build());
    }

    private String DownloadSong(Context context, Song song) {
        return Download(context, PDF_DIR + song.getFileName(), song.getmSongFile());
    }
}
