package org.elitanaroda.domcikuvzpevnik;

import android.app.DialogFragment;
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
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

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

    private Song mSong;

    //PDF variables
    private PDFView pdfView;
    //Scrolling variables
    private boolean doScroll = false;
    private Handler mScrollHandler;
    private float mScrollSpeed = 1f;
    private Context mContext;

    //Posun obrazu
    private Runnable ScrollRunnable = new Runnable() {
        @Override
        public void run() {
            pdfView.moveRelativeTo(0, -mScrollSpeed);
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
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getApplicationContext();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Inicializace UI
        setContentView(R.layout.activity_pdfview);
        pdfView = (PDFView) findViewById(R.id.pdfView);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Intent intent = getIntent();
        mSong = intent.getParcelableExtra(SONG_KEY);

        mScrollHandler = new Handler();

        //Get file location
        //Pokud už máme soubor, není nutné ho znovu stahovat
        //TODO:Check for SD CARD
        if (mSong.ismIsOnLocalStorage()) {
            displayFromFile(mSong.getmSongFile());
            Log.i(TAG, "File Exists");
        } else if (hasInternetConnection()) {
            startDownload(mSong);

            DownloadDialogFragment downloadDialogFragment = new DownloadDialogFragment();
            downloadDialogFragment.show(getFragmentManager(), DOWNLOADING_FRAGMENT_TAG);
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
                        if (getFragmentManager().findFragmentByTag(DOWNLOADING_FRAGMENT_TAG) != null)
                            ((DialogFragment) getFragmentManager().findFragmentByTag(DOWNLOADING_FRAGMENT_TAG)).dismiss();
                        if (args.getBoolean(RETRY_KEY, true)) {
                            snackbar.setAction("RETRY", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startDownload(mSong);
                                    (new DownloadDialogFragment()).show(getFragmentManager(), DOWNLOADING_FRAGMENT_TAG);
                                }
                            });
                        }
                        snackbar.show();
                    }
                }, showSnackbarFilter);

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pdf_menu, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.startStop:
                if (doScroll) {
                    stopScrolling();
                    item.setIcon(ContextCompat.getDrawable(mContext, R.drawable.ic_play_arrow_white_24dp));
                    mMenu.findItem(R.id.scrollFaster).setVisible(false);
                    mMenu.findItem(R.id.scrollSlower).setVisible(false);

                } else {
                    startScrolling();
                    item.setIcon(ContextCompat.getDrawable(mContext, R.drawable.ic_pause_white_24dp));
                    mMenu.findItem(R.id.scrollFaster).setVisible(true);
                    mMenu.findItem(R.id.scrollSlower).setVisible(true);
                }
                break;
            case R.id.scrollFaster:
                mScrollSpeed += 0.3f;
                break;
            case R.id.scrollSlower:
                mScrollSpeed -= 0.3f;
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    //adds runnables to the queque
    private void startScrolling() {
        doScroll = true;
        mScrollHandler = new Handler();
        mScrollHandler.post(ScrollRunnable);
        mScrollHandler.postDelayed(RefreshPageRunnable, 500);
    }

    //removes all callbacks from the queque
    private void stopScrolling() {
        doScroll = false;
        try {
            mScrollHandler.removeCallbacksAndMessages(null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
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
                .defaultPage(0)
                .enableAnnotationRendering(true)
                .scrollHandle(new DefaultScrollHandle(this))
                .onLoad(new OnLoadCompleteListener() {
                    @Override
                    public void loadComplete(int nbPages) {
                        if (getFragmentManager().findFragmentByTag(DOWNLOADING_FRAGMENT_TAG) != null)
                            ((DialogFragment) getFragmentManager().findFragmentByTag(DOWNLOADING_FRAGMENT_TAG)).dismiss();
                    }
                })
                .load();
    }

    //Obnovit scrollování a refresh
    @Override
    public void onResume() {
        super.onResume();
        if (doScroll) {
            startScrolling();
        }
    }

    //nechceme dál scrollovat a refreshovat
    @Override
    public void onPause() {
        super.onPause();
        if (doScroll)
            mScrollHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //pokud je zvolena možnost mazat, tak smazat
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
