package org.elitanaroda.domcikuvzpevnik;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.PowerManager;
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

//TODO: Rewrite this as a normal service
public class OneSongDownloadIS extends IntentService {
    private static final String TAG = "SongDownloadService";
    private static final String PREFIX = "org.elitanaroda.domkvzpvnk.downloadsongintentservice";
    public static final String BROADCAST_DOWNLOAD_FINISHED = PREFIX + ".FINISHED";
    public static final String BROADCAST_SHOW_ERROR = PREFIX + ".SHOW";
    public static final String BROADCAST_PROGRESS_UPDATE = PREFIX + ".PROGRESS";

    private static final String PDF_DIR = "http://elitanaroda.org/zpevnik/pdfs/";
    private Song mSong;
    private PowerManager.WakeLock mWakeLock;
    private String result;

    public OneSongDownloadIS() {
        super("OneSongDownloadIS");
    }

    public static String DownloadSong(Context context, Song song) {
        return Download(context, PDF_DIR + song.getFileName(), song.getmSongFile());
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
    protected void onHandleIntent(Intent intent) {
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();

        result = null;

        //Pokud tam je jen jedna písnička
        if (intent.hasExtra(PDFActivity.SONG_KEY)) {
            this.mSong = intent.getParcelableExtra(PDFActivity.SONG_KEY);
            result = DownloadSong(this, mSong);
            if (result == null) {
                sendFinishedBroadcast();
            } else
                sendErrorBroadcast(result);
        } else {
            Log.e(TAG, "Intent didn't contain data to download");
            stopSelf();
        }
        mWakeLock.release();
    }

    private void sendErrorBroadcast(String result) {
        Intent localIntent = new Intent(BROADCAST_SHOW_ERROR);
        localIntent.putExtra(PDFActivity.MESSAGE_KEY, "Download error:\n" + result)
                .putExtra(PDFActivity.RETRY_KEY, true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        Log.e(TAG, result);
    }

    private void sendFinishedBroadcast() {
        Intent localIntent = new Intent(BROADCAST_DOWNLOAD_FINISHED);
        localIntent.putExtra(PDFActivity.SONG_KEY, mSong)
                .putExtra(PDFActivity.MESSAGE_KEY, "File downloaded!")
                .putExtra(PDFActivity.RETRY_KEY, false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
