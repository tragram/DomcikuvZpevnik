package org.elitanaroda.domcikuvzpevnik;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.text.Normalizer;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/*
Global TODO:
NFC send song
permissions
chci zas v tobě spát je pochybný
*/
//<div>Icons made by <a href="http://www.flaticon.com/authors/dimi-kazak" title="Dimi Kazak">Dimi Kazak</a> from <a href="http://www.flaticon.com" title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, FilterSongList.onFilterDone, SettingsFragment.OnDeleteSongs {
    //Comparatory pro řazení písniček v seznamu
    private static final SongComparator<Song> ALPHABETICAL_BY_TITLE = new SongComparator<Song>(0) {
        @Override
        public int compare(Song o1, Song o2) {
            return Normalizer.normalize(o1.getmTitle(), Normalizer.Form.NFD).
                    compareTo(Normalizer.normalize(o2.getmTitle(), Normalizer.Form.NFD));
        }
    };
    private static final SongComparator<Song> ALPHABETICAL_BY_ARTIST = new SongComparator<Song>(1) {
        @Override
        public int compare(Song o1, Song o2) {
            return Normalizer.normalize(o1.getmArtist(), Normalizer.Form.NFD).
                    compareTo(Normalizer.normalize(o2.getmArtist(), Normalizer.Form.NFD));
        }
    };
    private static final SongComparator<Song> BY_DATE = new SongComparator<Song>(2) {
        @Override
        public int compare(Song o1, Song o2) {
            int difference = o2.getmDateAdded() - o1.getmDateAdded();
            if (difference != 0)
                return difference;
            else
                return ALPHABETICAL_BY_TITLE.compare(o1, o2);
        }
    };
    private static String TAG = "Main";
    private DBHelper mDBHelper;
    private RecyclerView songListView;
    private Toolbar mToolbar;
    private SongsAdapter mAdapter;
    private List<Song> mSongList;
    private Context mContext;
    private EnumSet<Helper.LanguageEnum> currentLanguageSettings;
    private SearchView searchView;
    private FilterSongList mFilterSongList;
    private SongComparator<Song> currentComparator;
    PopupMenu.OnMenuItemClickListener onSortMenuClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.sortByName:
                    currentComparator = ALPHABETICAL_BY_TITLE;
                    mFilterSongList.filter(mSongList, currentLanguageSettings);
                    break;
                case R.id.sortByArtist:
                    currentComparator = ALPHABETICAL_BY_ARTIST;
                    mFilterSongList.filter(mSongList, currentLanguageSettings);
                    break;
                case R.id.sortByDate:
                    currentComparator = BY_DATE;
                    mFilterSongList.filter(mSongList, currentLanguageSettings);
                    break;
                default:
                    Log.e(TAG, "Unexpected sort selected");
                    break;
            }
            return true;
        }
    };
    private PopupMenu languagePopupMenu;
    PopupMenu.OnMenuItemClickListener onLanguageMenuClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.selectAll:
                    Menu menu = languagePopupMenu.getMenu();
                    un_selectAll(menu.findItem(R.id.czech), menu.findItem(R.id.english), menu.findItem(R.id.slovak), menu.findItem(R.id.spanish), menu.findItem(R.id.other));
                    break;
                case R.id.czech:
                    changeLanguage(item, Helper.LanguageEnum.CZECH);
                    languagePopupMenu.show();
                    break;
                case R.id.english:
                    changeLanguage(item, Helper.LanguageEnum.ENGLISH);
                    break;
                case R.id.slovak:
                    changeLanguage(item, Helper.LanguageEnum.SLOVAK);
                    break;
                case R.id.spanish:
                    changeLanguage(item, Helper.LanguageEnum.SPANISH);
                    break;
                case R.id.other:
                    changeLanguage(item, Helper.LanguageEnum.OTHER);
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
    private PopupMenu sortPopupMenu;

    public List<Song> getmSongList() {
        return mSongList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();

        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        mToolbar.setTitle("Domčíkův");
        mToolbar.setSubtitle("      Zpěvník");
        mToolbar.setTitleTextAppearance(this, R.style.ActionBarTitle);
        mToolbar.setSubtitleTextAppearance(this, R.style.ActionBarTitle);
        setSupportActionBar(mToolbar);

        songListView = (RecyclerView) findViewById(R.id.SongRView);
        songListView.setLayoutManager(new LinearLayoutManager(this));
        songListView.setHasFixedSize(true);
        songListView.addItemDecoration(new SimpleDividerItemDecoration(this));

        mSongList = getSongsFromDB();

        mFilterSongList = new FilterSongList();
        mFilterSongList.setOnFilterDoneListener(this);
        //SnapHelper snapHelper = new LinearSnapHelper();
        //snapHelper.attachToRecyclerView(songListView);
    }

    private List<Song> getSongsFromDB() {
        //Nacteni databaze a jeji ulozeni do arraylistu
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
        LoadSongsList(songs, currentComparator);
    }

    @Override
    public void reloadSongs() {
        mFilterSongList.filter(mSongList, currentLanguageSettings);
    }

    private void LoadSongsList(List<Song> songs, Comparator<Song> songComparator) {
        mAdapter = new SongsAdapter(this, songComparator);
        mAdapter.add(songs);
        songListView.setAdapter(mAdapter);
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
    protected void onStart() {
        super.onStart();
        currentComparator = loadComparatorPreferences();
        currentLanguageSettings = loadLanguagePreferences();
        if (PDFActivity.hasInternetConnection(this)) {
            new UpdateDB().execute();
        } else {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                    "No internet connection, couldn't update the DB", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFilterSongList.filter(mSongList, currentLanguageSettings, null);
    }

    private SongComparator<Song> loadComparatorPreferences() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        switch (sharedPref.getInt(getString(R.string.sort_settings), 0)) {
            case 0:
                return ALPHABETICAL_BY_TITLE;
            case 1:
                return ALPHABETICAL_BY_ARTIST;
            case 2:
                return BY_DATE;
            default:
                Log.e(TAG, "Unexpected value in sort_settings");
                return ALPHABETICAL_BY_TITLE;
        }
    }

    private EnumSet<Helper.LanguageEnum> loadLanguagePreferences() {
        SharedPreferences languagePreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> languagePreferencesStringSet = languagePreferences.getStringSet(getString(R.string.language_settings), null);
        EnumSet<Helper.LanguageEnum> languageSettings = EnumSet.noneOf(Helper.LanguageEnum.class);
        if (languagePreferencesStringSet == null)
            languageSettings = EnumSet.allOf(Helper.LanguageEnum.class);
        else {
            for (String language : languagePreferencesStringSet) {
                languageSettings.add(Helper.toLanguageEnum(language));
            }
        }
        return languageSettings;
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveComparatorPreferences();
        saveLanguagePreferences();
    }

    private void saveComparatorPreferences() {
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getString(R.string.sort_settings), currentComparator.ID);
        editor.apply();
    }

    private void saveLanguagePreferences() {
        Set<String> stringSet = new HashSet<>();
        for (Helper.LanguageEnum language : currentLanguageSettings) {
            stringSet.add(language.toString());
        }
        Log.i(TAG, stringSet.toString());
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(getString(R.string.language_settings), stringSet);
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        searchView.setMaxWidth(9 * metrics.widthPixels / 16);
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
            if (currentLanguageSettings.contains(Helper.LanguageEnum.CZECH))
                menu.findItem(R.id.czech).setChecked(true);
            if (currentLanguageSettings.contains(Helper.LanguageEnum.ENGLISH))
                menu.findItem(R.id.english).setChecked(true);
            if (currentLanguageSettings.contains(Helper.LanguageEnum.SPANISH))
                menu.findItem(R.id.spanish).setChecked(true);
            if (currentLanguageSettings.contains(Helper.LanguageEnum.SLOVAK))
                menu.findItem(R.id.slovak).setChecked(true);
            if (currentLanguageSettings.contains(Helper.LanguageEnum.OTHER))
                menu.findItem(R.id.other).setChecked(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.onActionViewCollapsed();
        } else
            super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                SettingsFragment settingsFragment = new SettingsFragment();
                settingsFragment.setOnDeleteSongsListener(this);
                getFragmentManager().beginTransaction()
                        .replace(R.id.content, settingsFragment)
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

    private void openRandomSong() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean ignore = sharedPref.getBoolean("randomIgnoresLanguage", false);
        if (currentLanguageSettings.isEmpty() && !ignore) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                    "How about choosing some languages? ;)", Snackbar.LENGTH_LONG);
            snackbar.show();
        } else {
            actuallyOpenRandomSong(ignore);
        }
    }

    private void actuallyOpenRandomSong(boolean ignore) {
        Song randomSong;
        boolean hasInternetConnection = PDFActivity.hasInternetConnection(this);

        if (!ignore && !currentLanguageSettings.isEmpty()) {
            do {
                randomSong = mSongList.get(new Random().nextInt(mSongList.size()));
            } while (!currentLanguageSettings.contains(randomSong.getmLanguage()));
        } else
            randomSong = mSongList.get(new Random().nextInt(mSongList.size()));

        if (hasInternetConnection || randomSong.ismIsOnLocalStorage())
            openPDFDocument(randomSong);
        else {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                    "Random song not available on local storage and you are offline :(   Better luck next time!",
                    Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

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
            currentLanguageSettings = EnumSet.allOf(Helper.LanguageEnum.class);
        else
            currentLanguageSettings = EnumSet.noneOf(Helper.LanguageEnum.class);
        mFilterSongList.filter(mSongList, currentLanguageSettings);
    }


    private void changeLanguage(MenuItem item, Helper.LanguageEnum languageEnum) {
        if (item.isChecked()) {
            item.setChecked(false);
            currentLanguageSettings.remove(languageEnum);
        } else {
            item.setChecked(true);
            currentLanguageSettings.add(languageEnum);
        }
        mFilterSongList.filter(mSongList, currentLanguageSettings);
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mFilterSongList.filter(mSongList, currentLanguageSettings, newText);
        return true;
    }

    //Spuštění aktivity se souborem
    public void openPDFDocument(Song song) {
        Intent intent = new Intent(this, PDFActivity.class);
        intent.putExtra(PDFActivity.SONG_KEY, song);
        startActivity(intent);

        //Bez tohohle by to při dalším otevření nic neotevřelo, pokud se to smazalo
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        song.setmIsOnLocalStorage(sharedPref.getBoolean("keepFiles", true));
    }

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
                Log.i(TAG, connection.getHeaderFields().toString());
                Log.i(TAG, "Server:" + serverDBLastModified.toString() + "\n Local:" + localDB.lastModified());
                connection.disconnect();
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
            if (message == "Database updated") {
                mSongList = getSongsFromDB();
                mFilterSongList.filter(mSongList, currentLanguageSettings);
            }
            if (message != null) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        }
    }


}

