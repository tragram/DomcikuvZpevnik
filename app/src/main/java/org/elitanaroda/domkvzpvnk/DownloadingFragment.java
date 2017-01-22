package org.elitanaroda.domkvzpvnk;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daimajia.numberprogressbar.NumberProgressBar;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadingFragment extends Fragment {
    private static final String TAG = "DownloadingFragment";
    private static final String mSongDir = "SongDir";
    private final String PDF_DIR = "http://elitanaroda.org/zpevnik/pdfs/";
    private String pdfFileName = "něco";
    private File mSongFile;
    private NumberProgressBar numberProgressBar;

    public DownloadingFragment() {
        // Required empty public constructor
    }

    public static DownloadingFragment newInstance(String pdfFileName, String mSongFileDir) {
        final Bundle args = new Bundle();
        args.putString("pdfName", pdfFileName);
        args.putString("SongDir", mSongFileDir);
        DownloadingFragment downloadingFragment = new DownloadingFragment();
        downloadingFragment.setArguments(args);
        return downloadingFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        mSongFile = new File(args.getString(mSongDir));
        pdfFileName = args.getString("pdfName");
        final DownloadTask downloadTask = new DownloadTask(getActivity());
        downloadTask.execute(PDF_DIR + pdfFileName);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.downloading_fragment, container, false);
        numberProgressBar = (NumberProgressBar) v.findViewById(R.id.number_progress_bar);
        return v;
    }

    //Stará se o stahování souboru
    private class DownloadTask extends AsyncTask<String, Integer, String> {
        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // při vypnutí obrazovky chceme dostahovat soubor, teprve pak může jít CPU chrupkat
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            numberProgressBar.setProgress(progress[0]);
        }

        //pustit wakelock a načíst soubor
        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            if (result != null) {
                Snackbar snackbar = Snackbar
                        .make(getActivity().findViewById(android.R.id.content), "Download error:\n" + result, Snackbar.LENGTH_LONG)
                        .setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final DownloadTask downloadTask = new DownloadTask(getActivity());
                                downloadTask.execute(PDF_DIR + pdfFileName);
                            }
                        });
                snackbar.show();
            } else {
                Snackbar snackbar = Snackbar
                        .make(getActivity().findViewById(android.R.id.content), "File downloaded!", Snackbar.LENGTH_LONG);
                snackbar.show();
                ((PDFActivity) getActivity()).displayFromFile(mSongFile);
            }
            //TODO: Close the fragment
        }

        @Override
        protected String doInBackground(String... params) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(params[0]);
                Log.i(TAG, url.toString());
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
                output = new FileOutputStream(mSongFile);

                byte data[] = new byte[1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress...
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
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
    }
}
