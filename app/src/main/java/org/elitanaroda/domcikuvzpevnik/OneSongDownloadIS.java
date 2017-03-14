package org.elitanaroda.domcikuvzpevnik;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.ConnectivityManager;
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

/**
 * Class used for downloading an array of Songs contained within the intent
 */

public class OneSongDownloadIS extends IntentService {
    private static final String TAG = "OneSongDownloadService";
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

    /**
     * Decide which file to download and do it
     *
     * @param context App context
     * @param song    Song to download
     * @return Null on successful download
     */
    public static String DownloadSong(Context context, Song song) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        if (cm.isActiveNetworkMetered()) {
            return Download(context, PDF_DIR + song.getFileName(false), song.getmSongFileComp());
        } else {
            return Download(context, PDF_DIR + song.getFileName(true), song.getmSongFileSkenOrGen());
        }
    }

    /**
     * A very general method for downloading any file, broadcasts progress update
     * @param context App context
     * @param urlToDownload URL where the file is
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

            //Checking for code 00, so that we don't accidentally save an error
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }

            //To be able to calculate % downloaded
            int fileLength = connection.getContentLength();

            //Download the file
            input = new BufferedInputStream(url.openStream(), 8192);
            output = new FileOutputStream(downloadToFile);

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;

                //Publishing the progress only if total length is known
                if (fileLength > 0) {
                    Intent localIntent = new Intent(BROADCAST_PROGRESS_UPDATE);
                    localIntent.putExtra("progress", (int) (total * 100 / fileLength));
                    LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
                }
                output.write(data, 0, count);
            }
            Log.v(TAG, downloadToFile.getAbsolutePath() + " - sucessful download!");
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
                ignored.printStackTrace();
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

    /**
     * Sends an error broadcast
     * @param result A message to be included within the intent
     */
    private void sendErrorBroadcast(String result) {
        Intent localIntent = new Intent(BROADCAST_SHOW_ERROR);
        localIntent.putExtra(PDFActivity.MESSAGE_KEY, "Download error:\n" + result)
                .putExtra(PDFActivity.RETRY_KEY, true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        Log.e(TAG, result);
    }

    /**
     * Sends a Finished broadcast
     */
    private void sendFinishedBroadcast() {
        Intent localIntent = new Intent(BROADCAST_DOWNLOAD_FINISHED);
        localIntent.putExtra(PDFActivity.SONG_KEY, mSong)
                .putExtra(PDFActivity.MESSAGE_KEY, "File downloaded!")
                .putExtra(PDFActivity.RETRY_KEY, false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
