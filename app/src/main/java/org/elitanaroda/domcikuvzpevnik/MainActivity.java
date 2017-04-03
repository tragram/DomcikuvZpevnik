package org.elitanaroda.domcikuvzpevnik;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

/**
 * @author Dominik Hodan
 */
/*
Global TODO:
NFC send song
permissions
chci zas v tobě spát je pochybný
*/
//<div>Icons made by <a href="http://www.flaticon.com/authors/dimi-kazak" title="Dimi Kazak">Dimi Kazak</a> from <a href="http://www.flaticon.com" title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, FilterSongList.onFilterDone, SettingsFragment.OnDeleteSongs {
    private static String TAG = "Main";
    private DBHelper mDBHelper;
    private RecyclerView songListRView;
    private Toolbar mToolbar;
    private SongsAdapter mAdapter;
    private List<Song> mSongList;
    private Context mContext;
    private EnumSet<LanguageManager.LanguageEnum> mCurrentLanguageSettings;
    private SearchView mSearchView;
    private FilterSongList mFilterSongList;
    private SongComparator<Song> mCurrentComparator;

    /**
     * Reacts to user changing the sort settings
     */
    PopupMenu.OnMenuItemClickListener onSortMenuClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.sortByTitle:
                    mCurrentComparator = ComparatorManager.ALPHABETICAL_BY_TITLE;
                    mFilterSongList.filter(mSongList, mCurrentLanguageSettings);
                    break;
                case R.id.sortByArtist:
                    mCurrentComparator = ComparatorManager.ALPHABETICAL_BY_ARTIST;
                    mFilterSongList.filter(mSongList, mCurrentLanguageSettings);
                    break;
                case R.id.sortByDate:
                    mCurrentComparator = ComparatorManager.BY_DATE;
                    mFilterSongList.filter(mSongList, mCurrentLanguageSettings);
                    break;
                default:
                    Log.e(TAG, "Unexpected sort selected");
                    break;
            }
            return true;
        }
    };
    private ComparatorManager mComparatorManager;
    private LanguageManager mLanguageManager;
    private PopupMenu sortPopupMenu;
    private PopupMenu languagePopupMenu;

    /**
     * Reacts to user changing the language settings
     */
    PopupMenu.OnMenuItemClickListener onLanguageMenuClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.selectAll:
                    Menu menu = languagePopupMenu.getMenu();
                    un_selectAll(menu.findItem(R.id.czech), menu.findItem(R.id.english), menu.findItem(R.id.slovak), menu.findItem(R.id.spanish), menu.findItem(R.id.other));
                    break;
                case R.id.czech:
                    changeLanguage(item, LanguageManager.LanguageEnum.CZECH);
                    break;
                case R.id.english:
                    changeLanguage(item, LanguageManager.LanguageEnum.ENGLISH);
                    break;
                case R.id.slovak:
                    changeLanguage(item, LanguageManager.LanguageEnum.SLOVAK);
                    break;
                case R.id.spanish:
                    changeLanguage(item, LanguageManager.LanguageEnum.SPANISH);
                    break;
                case R.id.other:
                    changeLanguage(item, LanguageManager.LanguageEnum.OTHER);
                    break;
                default:
                    Log.e(TAG, "Unexpected language selected");
                    break;
            }
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
            item.setActionView(new View(mContext));
            return false;
        }
    };

    /**
     * Sets the title "Domcikuv Zpevnik" with the correct formatting to the provided toolbar
     *
     * @param context
     * @param toolbar The toolbar you want to apply the header to
     * @return Returns the customized toolbar.
     */
    public static Toolbar setToolbarText(Context context, Toolbar toolbar) {
        toolbar.setTitle("Domčíkův");
        toolbar.setSubtitle("      Zpěvník");
        toolbar.setTitleTextAppearance(context, R.style.ActionBarTitle);
        toolbar.setSubtitleTextAppearance(context, R.style.ActionBarTitle);
        return toolbar;
    }

    public List<Song> getmSongList() {
        return mSongList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the UI
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();

        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        setToolbarText(this, mToolbar);
        setSupportActionBar(mToolbar);

        //Load the song list
        songListRView = createRecyclerView(this);
        mSongList = getSongsFromDB();

        mComparatorManager = new ComparatorManager(this);
        mLanguageManager = new LanguageManager(this);

        mFilterSongList = new FilterSongList();
        mFilterSongList.setOnFilterDoneListener(this);

        mCurrentComparator = mComparatorManager.getComparatorPreferences();
        mCurrentLanguageSettings = mLanguageManager.getLanguagePreferences();
        mFilterSongList.filter(mSongList, mCurrentLanguageSettings, null);
    }

    private RecyclerView createRecyclerView(Context context) {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.SongRView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
        //SnapHelper snapHelper = new LinearSnapHelper();
        //snapHelper.attachToRecyclerView(songListRView);
        return recyclerView;
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Load settings
        mCurrentComparator = mComparatorManager.getComparatorPreferences();
        mCurrentLanguageSettings = mLanguageManager.getLanguagePreferences();

        //Possibly update the DB
        if (PDFActivity.hasInternetConnection(this)) {
            new UpdateDB().execute();
        } else {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                    "No internet connection, couldn't update the DB", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        //Reloads the list so that when the user returns, the checkmark is shown on a newly downloaded song
        mAdapter.notifyDataSetChanged();

        //Save settings
        mComparatorManager.saveComparator(mCurrentComparator);
        mLanguageManager.saveLanguages(mCurrentLanguageSettings);
    }

    /**
     * Loads the DB from local storage
     *
     * @return List of songs saved in the DB
     */
    private List<Song> getSongsFromDB() {
        mDBHelper = new DBHelper(this);
        try {
            return mDBHelper.getAllSongs();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return Collections.emptyList();
    }

    @Override
    public void onFilter(List<Song> songs) {
        LoadSongsList(songs, mCurrentComparator);
    }

    @Override
    public void reloadSongs() {
        mFilterSongList.filter(mSongList, mCurrentLanguageSettings);
    }

    /**
     * Creates an adapter, sets it to the recycler view, waits for item selection.
     *
     * @param songs          The list of songs to be shown to the user.
     * @param songComparator How to sort the list in the View.
     */
    private void LoadSongsList(List<Song> songs, Comparator<Song> songComparator) {
        mAdapter = new SongsAdapter(this, songComparator);
        mAdapter.add(songs);
        songListRView.setAdapter(mAdapter);
        //Čekání na výběr písně uživatelem
        mAdapter.setOnItemClickListener(new SongsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Song song) {
                if (view.getId() != R.id.YTButton) {
                    openPDFDocument(song);
                } else {
                    new SearchAndOpenYT(mContext).openYoutubeVideo(song);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setOnQueryTextListener(this);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mSearchView.setMaxWidth(9 * metrics.widthPixels / 16);
        return true;
    }

    private void initiateSortMenu() {
        sortPopupMenu = new PopupMenu(this, findViewById(R.id.action_sortBy));
        sortPopupMenu.getMenuInflater().inflate(R.menu.sort_menu, sortPopupMenu.getMenu());
        sortPopupMenu.setOnMenuItemClickListener(onSortMenuClickListener);
    }

    private void initiateLanguageMenu() {
        languagePopupMenu = new PopupMenu(this, findViewById(R.id.action_languages));
        languagePopupMenu.getMenuInflater().inflate(R.menu.language_menu, languagePopupMenu.getMenu());
        setLanguagesChecked(languagePopupMenu.getMenu());
        languagePopupMenu.setOnMenuItemClickListener(onLanguageMenuClickListener);
    }

    private void setLanguagesChecked(Menu menu) {
        try {
            if (mCurrentLanguageSettings.contains(LanguageManager.LanguageEnum.CZECH))
                menu.findItem(R.id.czech).setChecked(true);
            if (mCurrentLanguageSettings.contains(LanguageManager.LanguageEnum.ENGLISH))
                menu.findItem(R.id.english).setChecked(true);
            if (mCurrentLanguageSettings.contains(LanguageManager.LanguageEnum.SPANISH))
                menu.findItem(R.id.spanish).setChecked(true);
            if (mCurrentLanguageSettings.contains(LanguageManager.LanguageEnum.SLOVAK))
                menu.findItem(R.id.slovak).setChecked(true);
            if (mCurrentLanguageSettings.contains(LanguageManager.LanguageEnum.OTHER))
                menu.findItem(R.id.other).setChecked(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (!mSearchView.isIconified()) {
            mSearchView.onActionViewCollapsed();
        } else
            super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                //Show the settings fragment
                SettingsFragment settingsFragment = new SettingsFragment();
                settingsFragment.setOnDeleteSongsListener(this);
                getFragmentManager().beginTransaction()
                        .replace(R.id.chordProContent, settingsFragment)
                        .addToBackStack("SettingsFragment")
                        .commit();
                return true;
            case R.id.action_random:
                openRandomSong();
                return true;
            case R.id.action_sortBy:
                initiateSortMenu();
                sortPopupMenu.show();
                return true;
            case R.id.action_languages:
                initiateLanguageMenu();
                languagePopupMenu.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Checks if there are languages to go through, if user selected not ignoring language choice and then opens a random song.
     */
    private void openRandomSong() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean ignore = sharedPref.getBoolean("randomIgnoresLanguage", false);
        if (mCurrentLanguageSettings.isEmpty() && !ignore) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                    "How about choosing some languages? ;)", Snackbar.LENGTH_LONG);
            snackbar.show();
        } else {
            safeOpenRandomSong(ignore);
        }
    }

    /**
     * Opens a random song, shouldn't be called directly!
     * <p>
     * To be used with {@link #openRandomSong()}.
     *
     * @param ignore Whether ignore or not the language settings.
     */
    private void safeOpenRandomSong(boolean ignore) {
        Song randomSong;
        boolean hasInternetConnection = PDFActivity.hasInternetConnection(this);

        Random randomGenerator = new Random();
        if (!ignore) {
            do {
                randomSong = mSongList.get(randomGenerator.nextInt(mSongList.size()));
            } while (!mCurrentLanguageSettings.contains(randomSong.getmLanguage()));
        } else
            randomSong = mSongList.get(randomGenerator.nextInt(mSongList.size()));

        if (hasInternetConnection || randomSong.ismIsOnLocalStorage()) {
            openPDFDocument(randomSong);
        } else {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                    "Random song not available on local storage and you are offline :(   Better luck next time!",
                    Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    /**
     * Checks all items provided, unless all are checked.
     *
     * @param items Items to (un)check.
     */
    private void un_selectAll(MenuItem... items) {
        boolean anyUnchecked = false;
        for (MenuItem item : items) {
            if (!item.isChecked())
                anyUnchecked = true;
        }
        for (MenuItem item : items) {
            item.setChecked(anyUnchecked);
        }
        if (anyUnchecked)
            mCurrentLanguageSettings = EnumSet.allOf(LanguageManager.LanguageEnum.class);
        else
            mCurrentLanguageSettings = EnumSet.noneOf(LanguageManager.LanguageEnum.class);
        mFilterSongList.filter(mSongList, mCurrentLanguageSettings);
    }


    /**
     * Changes the language filter settings and reloads the list.
     *
     * @param item         MenuItem to (un)check.
     * @param languageEnum New language filter settings.
     */
    private void changeLanguage(MenuItem item, LanguageManager.LanguageEnum languageEnum) {
        if (item.isChecked()) {
            item.setChecked(false);
            mCurrentLanguageSettings.remove(languageEnum);
        } else {
            item.setChecked(true);
            mCurrentLanguageSettings.add(languageEnum);
        }
        mFilterSongList.filter(mSongList, mCurrentLanguageSettings);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mFilterSongList.filter(mSongList, mCurrentLanguageSettings, newText);
        return true;
    }

    /**
     * Opens a new PDFActivity showing the selected document.
     *
     * @param song Song to open.
     */
    public void openPDFDocument(Song song) {
        //We won't be downloading this
        if (song.hasChordPro() && PDFActivity.hasInternetConnection(this)) {
            Intent intent = new Intent(this, ChordProActivity.class);
            intent.putExtra(PDFActivity.SONG_KEY, song);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            //Setting the expected file availability after finishing the PDFActivity
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            song.setmIsOnLocalStorage(sharedPref.getBoolean("keepFiles", true));

            Intent intent = new Intent(this, PDFActivity.class);
            intent.putExtra(PDFActivity.SONG_KEY, song);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    /**
     * Updates the database, shows a popup on update or fail to check
     */
    private class UpdateDB extends AsyncTask<Void, Void, String> {
        public static final String DB_URL = "http://elitanaroda.org/zpevnik/FinalDB.db";
        public File dbDir = new File(getFilesDir() + File.separator + "db");

        File localDB;

        @Override
        protected String doInBackground(Void... params) {
            try {
                if (!dbDir.isDirectory())
                    dbDir.mkdirs();
                localDB = new File(dbDir, DBHelper.DB_NAME);
                URL url = new URL(DB_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                Long serverDBLastModified = connection.getLastModified();
                Log.v(TAG, connection.getHeaderFields().toString());
                Log.v(TAG, "Server:" + serverDBLastModified.toString() + "\n Local:" + localDB.lastModified());
                connection.disconnect();

                //Only download if the db has been updated
                if (localDB.lastModified() < serverDBLastModified) {
                    if (OneSongDownloadIS.Download(getBaseContext(), DB_URL, localDB) == null)
                        return "Database updated";
                    else
                        return "Error updating the DB";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);
            //On update reload the list
            if (message == "Database updated") {
                mSongList = getSongsFromDB();
                mFilterSongList.filter(mSongList, mCurrentLanguageSettings);
            }
            if (message != null) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        }
    }
}

