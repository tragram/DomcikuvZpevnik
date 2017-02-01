package org.elitanaroda.domcikuvzpevnik;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.youtube.player.YouTubeIntents;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Normalizer;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
Global TODO:
NFC send song
permissions
chci zas v tobě spát je pochybný
*/

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, FilterSongList.onFilterDone {
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
            else {
                return Normalizer.normalize(o1.getmTitle(), Normalizer.Form.NFD).
                        compareTo(Normalizer.normalize(o2.getmTitle(), Normalizer.Form.NFD));
            }
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

    public List<Song> getmSongList() {
        return mSongList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();

        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
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
    protected void onStart() {
        super.onStart();
        currentComparator = loadComparatorPreferences();
        currentLanguageSettings = loadLanguagePreferences();
        mFilterSongList.filter(mSongList, currentLanguageSettings, null);

        if (PDFActivity.hasInternetConnection(this)) {
            new UpdateDB().execute();
        } else {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                    "No internet connection, couldn't update the DB", Snackbar.LENGTH_LONG);
            snackbar.show();
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
    protected void onStop() {
        super.onStop();
        saveComparatorPreferences();
        saveLanguagePreferences();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);

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
        return true;
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
                getFragmentManager().beginTransaction()
                        .replace(R.id.content, new SettingsFragment())
                        .addToBackStack("SettingsFragment")
                        .commit();
                return true;
            case R.id.sortByName:
                currentComparator = ALPHABETICAL_BY_TITLE;
                mFilterSongList.filter(mSongList, currentLanguageSettings);
                return true;
            case R.id.sortByArtist:
                currentComparator = ALPHABETICAL_BY_ARTIST;
                mFilterSongList.filter(mSongList, currentLanguageSettings);
                return true;
            case R.id.sortByDate:
                currentComparator = BY_DATE;
                mFilterSongList.filter(mSongList, currentLanguageSettings);
                return true;
            //TODO: Dont dismiss the menu
            case R.id.selectAll:
                un_selectAll();
                return true;
            case R.id.czech:
                changeLanguage(item, Helper.LanguageEnum.CZECH);
                return true;
            case R.id.english:
                changeLanguage(item, Helper.LanguageEnum.ENGLISH);
                return true;
            case R.id.slovak:
                changeLanguage(item, Helper.LanguageEnum.SLOVAK);
                return true;
            case R.id.spanish:
                changeLanguage(item, Helper.LanguageEnum.SPANISH);
                return true;
            case R.id.other:
                changeLanguage(item, Helper.LanguageEnum.OTHER);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveComparatorPreferences() {
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (currentComparator.ID) {
            case 0:
                editor.putInt(getString(R.string.sort_settings), 0);
                break;
            case 1:
                editor.putInt(getString(R.string.sort_settings), 1);
                break;
            case 2:
                editor.putInt(getString(R.string.sort_settings), 2);
                break;
            default:
                Log.e(TAG, "current comparator has weird ID");
                editor.putInt(getString(R.string.sort_settings), 0);
                break;
        }
        editor.apply();
    }

    private void un_selectAll(MenuItem... items) {
        //TODO: This
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

        if (!song.ismIsOnLocalStorage() && Helper.isMyServiceRunning(this, DownloadSongIntentService.class)) {
            Intent localIntent = new Intent(DownloadSongIntentService.BROADCAST_STOP_BATCH_DOWNLOAD);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            startActivity(intent);
            //TODO: Stáhne to jednu dvakrát, prostě to nenačte ten arraylist, pracuje to s původní proměnou
            //SettingsFragment.downloadAllSongs(this, getmSongList());
        } else
            startActivity(intent);

        //Bez tohohle by to při dalším otevření nic neotevřelo, pokud se to smazalo
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        song.setmIsOnLocalStorage(sharedPref.getBoolean("keepFiles", true));
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
                } else
                    new MainActivity.openYoutube().execute(song.getmArtist() + " - " + song.getmTitle());
            }
        });

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
                    if (DownloadSongIntentService.Download(getBaseContext(), DB_URL, localDB) == null)
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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == null) {
                mSongList = getSongsFromDB();
                mFilterSongList.filter(mSongList, currentLanguageSettings);
            } else {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), s, Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }
    }

    private class openYoutube extends AsyncTask<String, Void, Void> {
        private static final String YOUTUBE_DATA_API_KEY = "AIzaSyCcGkkc3xOCvUfo-4VTejHq5QrZh0qV90U";
        private YouTube youtube;

        @Override
        protected Void doInBackground(String... params) {
            try {
                // This object is used to make YouTube Data API requests
                youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
                    public void initialize(HttpRequest request) throws IOException {
                    }
                }).setApplicationName(getPackageName()).build();

                // Define the API request for retrieving search results.
                YouTube.Search.List search = youtube.search().list("id");
                search.setKey(YOUTUBE_DATA_API_KEY)
                        .setQ(params[0])
                        .setType("video")
                        .setFields("items(id/videoId)")
                        .setMaxResults((long) 1);

                // Call the API
                SearchListResponse searchResponse = search.execute();
                List<SearchResult> searchResultList = searchResponse.getItems();

                if (searchResultList != null && YouTubeIntents.isYouTubeInstalled(mContext))
                    startActivity(YouTubeIntents.createPlayVideoIntentWithOptions(mContext, searchResultList.get(0).getId().getVideoId(), true, true));
                else {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Couldn't load the YouTube App", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            } catch (GoogleJsonResponseException e) {
                Log.e(TAG, "There was a service error: " + e.getDetails().getCode() + " : "
                        + e.getDetails().getMessage());
            } catch (IOException e) {
                Log.e(TAG, "There was an IO error: " + e.getCause() + " : " + e.getMessage());
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return null;
        }
    }
}

