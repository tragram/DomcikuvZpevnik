package org.elitanaroda.domkvzpvnk;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
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

import static android.os.Environment.MEDIA_MOUNTED;

public class PDFActivity extends AppCompatActivity implements OnLoadCompleteListener {
    private static String TAG = "PDFViewActivity";

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

    public File getmSongFile() {
        return mSongFile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getApplicationContext();
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
        if (Environment.getExternalStorageState() == MEDIA_MOUNTED) {
            mSongFile = new File(Environment.getExternalStorageDirectory().toString()
                    + File.separator + "Domčíkuv Zpěvník", pdfFileName);
        } else
            mSongFile = new File(this.getFilesDir().getAbsolutePath() + File.separatorChar + pdfFileName);

        //Pokud už máme soubor, není nutné ho znovu stahovat
        if (mSongFile.exists()) {
            displayFromFile(mSongFile);
            Log.i(TAG, "File Exists");
        } else if (hasInternetConnection()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.activity_pdfview, DownloadingFragment
                            .newInstance(pdfFileName, mSongFile.getAbsolutePath()), "loadingfragment")
                    .commit();
        } else {
            Snackbar snackbar = Snackbar
                    .make(findViewById(android.R.id.content), "No Internet Connection. \n " +
                            "File not available on local storage, sorry.", Snackbar.LENGTH_LONG);
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
    public void displayFromFile(File file) {
        Log.i(TAG, file.getAbsolutePath());
        pdfView.fromFile(file)
                .defaultPage(pageNumber)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .onLoad(new OnLoadCompleteListener() {
                    @Override
                    public void loadComplete(int nbPages) {
                        if (getSupportFragmentManager().findFragmentByTag("loadingfragment") != null)
                            getSupportFragmentManager().beginTransaction()
                                    .remove(getSupportFragmentManager()
                                            .findFragmentByTag("loadingfragment")).commit();
                    }
                })
                .load();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPref.getBoolean("keepFiles", true))
            deleteFile(mSongFile.getName());
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
}
