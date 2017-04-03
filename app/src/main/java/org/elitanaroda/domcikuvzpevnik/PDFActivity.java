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
    private static final String TAG = "PDFViewActivity";

    private Song mSong;
    private Context mContext;
    private Menu mMenu;
    private PDFView pdfView;
    private boolean doScroll = false;
    private Handler mScrollHandler;
    //Focusing newly shown parts of the document
    private final Runnable RefreshPageRunnable = new Runnable() {
        @Override
        public void run() {
            pdfView.loadPages();
            mScrollHandler.postDelayed(this, 700);
        }
    };
    private float mScrollSpeed = 0.8f;
    //Moving the document
    private final Runnable ScrollRunnable = new Runnable() {
        @Override
        public void run() {
            pdfView.moveRelativeTo(0, -mScrollSpeed);
            mScrollHandler.postDelayed(this, 15);
        }
    };

    //On download finished informs the user and shows the document
    private BroadcastReceiver onFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mSong.setmIsOnLocalStorage(true);
            Song song = intent.getParcelableExtra(SONG_KEY);
            showSong(song);
            Snackbar snackbar = Snackbar
                    .make(findViewById(android.R.id.content),
                            "File downloaded!", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
    };

    //On download error dismisses the download dialog and informs the user
    private BroadcastReceiver onErrorReceiver = new BroadcastReceiver() {
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
    };

    /**
     * Checks if there's internet connection
     *
     * @param context
     * @return Returns true on internet connection available
     */
    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getApplicationContext();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Initialize the UI
        setContentView(R.layout.activity_pdfview);
        pdfView = (PDFView) findViewById(R.id.pdfView);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        MainActivity.setToolbarText(this, mToolbar);
        setSupportActionBar(mToolbar);

        mScrollHandler = new Handler();

        //Show the song
        Intent intent = getIntent();
        mSong = intent.getParcelableExtra(SONG_KEY);
        showSong(mSong);
    }

    /**
     * Shows the best quality available.
     * @param songToOpen Which song to open.
     */
    private void showSong(Song songToOpen) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(CONNECTIVITY_SERVICE);
        //If we already have the file in high quality, there is no need to download it again
        if (songToOpen.getmSongFileOriginal().isFile()) {
            displayFromFile(songToOpen.getmSongFileOriginal());
        } else if (cm.isActiveNetworkMetered() && songToOpen.getmSongFileSmall().isFile()) {
            displayFromFile(songToOpen.getmSongFileSmall());
        } else if (hasInternetConnection(mContext)) {
            downloadSong(songToOpen);
        } else {
            Snackbar snackbar = Snackbar
                    .make(findViewById(android.R.id.content), "No Internet Connection. \n " +
                            "File not available on local storage, sorry.", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    private void downloadSong(Song song) {
        Intent serviceIntent = new Intent(this, OneSongDownloadIS.class);
        serviceIntent.putExtra(SONG_KEY, song);
        this.startService(serviceIntent);
        DownloadDialogFragment downloadDialogFragment = new DownloadDialogFragment();
        downloadDialogFragment.show(getFragmentManager(), DOWNLOADING_FRAGMENT_TAG);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerFinishedReceiver(this);
        registerErrorReceiver(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        //Resume scrolling and refreshing
        if (doScroll) {
            startScrolling();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //Pause scrolling and refreshing
        if (doScroll)
            mScrollHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(onFinishedReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(onErrorReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerFinishedReceiver(Context context) {
        IntentFilter fileDownloadedFilter =
                new IntentFilter(OneSongDownloadIS.BROADCAST_DOWNLOAD_FINISHED);
        LocalBroadcastManager.getInstance(context).
                registerReceiver(onFinishedReceiver, fileDownloadedFilter);
    }

    private void registerErrorReceiver(Context context) {
        IntentFilter showSnackbarFilter =
                new IntentFilter(OneSongDownloadIS.BROADCAST_SHOW_ERROR);
        LocalBroadcastManager.getInstance(context).
                registerReceiver(onErrorReceiver, showSnackbarFilter);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pdf_menu, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                SettingsFragment settingsFragment = new SettingsFragment();
                getFragmentManager().beginTransaction()
                        .replace(R.id.frame, settingsFragment)
                        .addToBackStack("SettingsFragment")
                        .commit();
                return true;
            case R.id.youtube:
                new SearchAndOpenYT(this).openYoutubeVideo(mSong);
                break;
            case R.id.startStop:
                //Switch between srolling and not scrolling
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

    /**
     * Adds runnables to the queque
     */
    private void startScrolling() {
        doScroll = true;
        mScrollHandler = new Handler();
        mScrollHandler.post(ScrollRunnable);
        mScrollHandler.postDelayed(RefreshPageRunnable, 500);
    }

    /**
     * Removes all callbacks from the queque
     */
    private void stopScrolling() {
        doScroll = false;
        try {
            mScrollHandler.removeCallbacksAndMessages(null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void startDownload(Song songToDownload) {
        Intent serviceIntent = new Intent(this, OneSongDownloadIS.class);
        serviceIntent.putExtra(SONG_KEY, songToDownload);
        this.startService(serviceIntent);
    }

    /**
     * Actually load the song
     *
     * @param fileToOpen The file where the song is saved
     */
    private void displayFromFile(File fileToOpen) {
        Log.i(TAG, fileToOpen.getAbsolutePath());
        if (fileToOpen.exists()) {
            pdfView.fromFile(fileToOpen)
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
        } else
            Log.e(TAG, "File doesn't exist, couldn't open it.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //TODO: Wont be deleted on force closing the app, only on back-press
        //Delete if user doesn't wish to keep the file
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPref.getBoolean("keepFiles", true)) {
            try {
                deleteFile(mSong.getmSongFileSmall().getName());
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
