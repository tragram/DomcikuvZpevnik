package org.elitanaroda.domkvzpvnk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import java.io.File;

public class PDFActivity extends AppCompatActivity {
    public static final String SONG_KEY = "songObject";
    public static final String SONG_ARRAY_KEY = "songObjectArray";
    public static final String MESSAGE_KEY = "message";
    public static final String RETRY_KEY = "retry";
    private static final String DOWNLOADING_FRAGMENT_TAG = "downloadingFragment";
    private static String TAG = "PDFViewActivity";
    //PDF variables
    private PDFView pdfView;
    private int pageNumber = 0;
    private Song mSong;

    //Scrolling variables
    private boolean doScroll = false;
    private Button mScrollButton;
    private Handler mScrollHandler;
    private Context mContext;

    //Posun obrazu
    private Runnable ScrollRunnable = new Runnable() {
        @Override
        public void run() {
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
        //Inicializace UI
        setContentView(R.layout.activity_pdfview);
        pdfView = (PDFView) findViewById(R.id.pdfView);
        Intent intent = getIntent();
        mSong = intent.getParcelableExtra(SONG_KEY);
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
        //Pokud už máme soubor, není nutné ho znovu stahovat
        //TODO:Check for SD CARD
        if (mSong.ismIsOnLocalStorage()) {
            displayFromFile(mSong.getmSongFile());
            Log.i(TAG, "File Exists");
        } else if (hasInternetConnection()) {
            startDownload(mSong);

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.activity_pdfview, DownloadingFragment
                            .newInstance(), DOWNLOADING_FRAGMENT_TAG)
                    .commit();
        } else {
            Snackbar snackbar = Snackbar
                    .make(findViewById(android.R.id.content), "No Internet Connection. \n " +
                            "File not available on local storage, sorry.", Snackbar.LENGTH_LONG);
            snackbar.show();
        }

        IntentFilter fileDownloadedFilter =
                new IntentFilter(DownloadSongIntentService.BROADCAST_DOWNLOAD_FINISHED);
        LocalBroadcastManager.getInstance(this).
                registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Song song = intent.getParcelableExtra(SONG_KEY);
                        displayFromFile(song.getmSongFile());
                        Snackbar snackbar = Snackbar
                                .make(findViewById(android.R.id.content),
                                        "File downloaded!", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                }, fileDownloadedFilter);

        IntentFilter showSnackbarFilter =
                new IntentFilter(DownloadSongIntentService.BROADCAST_SHOW_ERROR);
        LocalBroadcastManager.getInstance(this).
                registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Bundle args = intent.getExtras();
                        Snackbar snackbar = Snackbar
                                .make(findViewById(android.R.id.content),
                                        args.getString(MESSAGE_KEY, "No message sent... WTF"), Snackbar.LENGTH_LONG);
                        if (args.getBoolean(RETRY_KEY, true)) {
                            snackbar.setAction("RETRY", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startDownload(mSong);
                                }
                            });
                        }
                        snackbar.show();
                    }
                }, showSnackbarFilter);

    }

    //Checks if there's internet connection, returns true when there is
    private boolean hasInternetConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }

    private void startDownload(Song songToDownload) {
        Intent serviceIntent = new Intent(this, DownloadSongIntentService.class);
        serviceIntent.putExtra(SONG_KEY, songToDownload);
        this.startService(serviceIntent);
    }

    //Načtení konkrétního souboru
    private void displayFromFile(File file) {
        Log.i(TAG, file.getAbsolutePath());
        pdfView.fromFile(file)
                .defaultPage(pageNumber)
                .enableAnnotationRendering(true)
                .scrollHandle(new DefaultScrollHandle(this))
                .onLoad(new OnLoadCompleteListener() {
                    @Override
                    public void loadComplete(int nbPages) {
                        if (getSupportFragmentManager().findFragmentByTag(DOWNLOADING_FRAGMENT_TAG) != null)
                            getSupportFragmentManager().beginTransaction()
                                    .remove(getSupportFragmentManager()
                                            .findFragmentByTag(DOWNLOADING_FRAGMENT_TAG)).commit();
                    }
                })
                .load();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPref.getBoolean("keepFiles", true)) {
            try {
                deleteFile(mSong.getmSongFile().getName());
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
