package org.elitanaroda.domkvzpvnk;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PDFActivity extends AppCompatActivity implements OnLoadCompleteListener {
    private static String TAG = "PDFViewActivity";
    private final String PDF_DIR = "http://elitanaroda.org/zpevnik/pdfs/";

    //PDF variables
    private PDFView pdfView;
    private int pageNumber = 0;
    private String pdfFileName;
    private File mSongFile;

    //Scrolling variables
    private boolean doScroll = false;
    private Button mScrollButton;
    private Handler mScrollHandler;
    private Context mContext;

    private RelativeLayout rl;

    //Posun obrazu
    private Runnable ScrollRunnable = new Runnable() {
        @Override
        public void run() {
            //pdfView.setPositionOffset(pdfView.getCurrentYOffset() + 0.05f);
            pdfView.moveRelativeTo(0, -3);
            mScrollHandler.postDelayed(this, 15);
        }
    };

    //Doostření nových částí dokumentu
    private Runnable RefreshPageRunnable = new Runnable() {
        @Override
        public void run() {
            pdfView.loadPages();
            mScrollHandler.postDelayed(this, 700);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getApplicationContext();
        rl = (RelativeLayout) findViewById(R.id.pdfView);
        //Inicializace UI
        setContentView(R.layout.activity_pdfview);
        pdfView = (PDFView) findViewById(R.id.pdfView);
        //mScrollButton = (Button) findViewById(R.id.scrollButton);

        /*mScrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!doScroll) {
                    doScroll = true;
                    mScrollHandler = new Handler();
                    mScrollHandler.post(ScrollRunnable);
                    mScrollHandler.postDelayed(RefreshPageRunnable, 500);

                } else {
                    doScroll = false;
                    mScrollHandler.removeCallbacks(ScrollRunnable);
                }
            }
        });*/

        //Get file location
        Intent intent = getIntent();
        pdfFileName = intent.getStringExtra("fileName");
        mSongFile = new File(this.getFilesDir().getAbsolutePath() + File.separatorChar + pdfFileName);

        //Pokud už máme soubor, není nutné ho znovu stahovat
        if (mSongFile.exists()) {
            displayFromFile(mSongFile);
            Log.i(TAG, "File Exists");
        } else if (hasInternetConnection()) {
            final DownloadTask downloadTask = new DownloadTask(PDFActivity.this);
            downloadTask.execute(PDF_DIR + pdfFileName);
        } else {
            Snackbar snackbar = Snackbar
                    .make(findViewById(android.R.id.content), "No Internet Connection. File not available on local storage, sorry.",
                            Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    //Checks if there's internet connection, returns true when there is
    private boolean hasInternetConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            return true;
        } else
            return false;
    }

    //Načtení konkrétního souboru
    private void displayFromFile(File file) {
        Log.i(TAG, file.getAbsolutePath());
        pdfView.fromFile(file)
                .defaultPage(pageNumber)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .load();
    }

    /*//Načtení souboru z assetu
    private void displayFromAsset(String assetFileName) {
        pdfFileName = assetFileName;

        pdfView.fromAsset(pdfFileName)
                .defaultPage(pageNumber)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .load();
    }*/

    @Override
    public void loadComplete(int nbPages) {
    }

    //Obnovit scrollování a refresh
    @Override
    public void onResume() {
        super.onResume();
        if (doScroll) {
            mScrollHandler.post(ScrollRunnable);
            mScrollHandler.postDelayed(RefreshPageRunnable, 700);
        }

    }

    //nechceme dál scrollovat a refreshovat
    @Override
    public void onStop() {
        super.onPause();
        if (doScroll)
            mScrollHandler.removeCallbacks(ScrollRunnable, RefreshPageRunnable);
    }

    private void showDialog() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.activity_pdfview, DownloadingFragment.newInstance(), "loadingfragment")
                .commit();
        Log.e(TAG, "Dialog called");
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
            //TODO: Zavolat fragment
            //showDialog();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            //TODO: Update the fragment
        }

        //pustit wakelock a načíst soubor
        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            if (result != null) {
                Snackbar snackbar = Snackbar
                        .make(findViewById(android.R.id.content), "Download error: " + result, Snackbar.LENGTH_LONG)
                        .setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final DownloadTask downloadTask = new DownloadTask(PDFActivity.this);
                                downloadTask.execute(PDF_DIR + pdfFileName);
                            }
                        });
                snackbar.show();
            } else {
                Snackbar snackbar = Snackbar
                        .make(findViewById(android.R.id.content), "File downloaded!", Snackbar.LENGTH_LONG);
                snackbar.show();
                displayFromFile(mSongFile);
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
