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
import java.text.Normalizer;
import java.util.ArrayList;
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

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private static final String YOUTUBE_DATA_API_KEY = "AIzaSyCcGkkc3xOCvUfo-4VTejHq5QrZh0qV90U";
    //Comparatory pro řazení písniček v seznamu
    private static final Comparator<Song> ALPHABETICAL_BY_TITLE = new Comparator<Song>() {
        @Override
        public int compare(Song o1, Song o2) {
            return Normalizer.normalize(o1.getmTitle(), Normalizer.Form.NFD).
                    compareTo(Normalizer.normalize(o2.getmTitle(), Normalizer.Form.NFD));
        }
    };
    private static final Comparator<Song> ALPHABETICAL_BY_ARTIST = new Comparator<Song>() {
        @Override
        public int compare(Song o1, Song o2) {
            return Normalizer.normalize(o1.getmArtist(), Normalizer.Form.NFD).
                    compareTo(Normalizer.normalize(o2.getmArtist(), Normalizer.Form.NFD));
        }
    };
    private static final Comparator<Song> BY_DATE = new Comparator<Song>() {
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
    private static YouTube youtube;
    private DBHelper mDBHelper;
    private RecyclerView songListView;
    private Toolbar mToolbar;
    private SongsAdapter mAdapter;
    private List<Song> mSongList;
    private Context mContext;
    private EnumSet<Helper.LanguageEnum> languageEna;
    private SearchView searchView;
    private boolean queryHandled = true;

    //Vytvoření seznamu odpovídajícího hledání
    private static List<Song> filterByQuery(List<Song> songs, String query) {
        final String niceQuery = Helper.makeTextNiceAgain(query);
        Log.i(TAG, niceQuery);

        List<Song> filteredSongList = new ArrayList<>();
        for (Song song : songs) {
            final String artist = Helper.makeTextNiceAgain(song.getmArtist());
            final String title = Helper.makeTextNiceAgain(song.getmTitle());
            if (artist.contains(niceQuery) || title.contains(niceQuery)) {
                filteredSongList.add(song);
            }
        }
        return filteredSongList;
    }

    public List<Song> getmSongList() {
        return mSongList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        songListView = (RecyclerView) findViewById(R.id.SongRView);
        songListView.setLayoutManager(new LinearLayoutManager(this));
        songListView.setHasFixedSize(true);
        songListView.addItemDecoration(new SimpleDividerItemDecoration(this));

        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        setSupportActionBar(mToolbar);

        //Nacteni databaze a jeji ulozeni do arraylistu
        mDBHelper = new DBHelper(this);
        try {
            mDBHelper.createDataBase();
        } catch (IOException io) {
            io.getMessage();
        }
        mDBHelper.openDataBase();

        mSongList = mDBHelper.getAllSongs();

        mContext = getApplicationContext();
        //SnapHelper snapHelper = new LinearSnapHelper();
        //snapHelper.attachToRecyclerView(songListView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Load language preferences
        SharedPreferences languagePreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> languagePreferencesStringSet = languagePreferences.getStringSet(getString(R.string.language_settings), null);
        languageEna = EnumSet.noneOf(Helper.LanguageEnum.class);
        if (languagePreferencesStringSet == null)
            languageEna = EnumSet.allOf(Helper.LanguageEnum.class);
        else {
            for (String language : languagePreferencesStringSet) {
                languageEna.add(Helper.toLanguageEnum(language));
            }
        }
        LoadSongsList(mSongList, getComparatorFromSettings(), languageEna);

        /*if (PDFActivity.hasInternetConnection(this)) {
            new UpdateDB().execute();
        } else {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                    "No internet connection, couldn't update the DB", Snackbar.LENGTH_LONG);
            snackbar.show();
        }*/
    }

    @Override
    protected void onStop() {
        super.onStop();
        //TODO: SAVE PREFERENCES
        //Save language preferences
        Set<String> stringSet = new HashSet<>();
        for (Helper.LanguageEnum language : languageEna) {
            stringSet.add(language.toString());
        }
        Log.i(TAG, stringSet.toString());
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(getString(R.string.language_settings), stringSet);
        editor.apply();
    }

    private Comparator<Song> getComparatorFromSettings() {
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

        if (languageEna.contains(Helper.LanguageEnum.CZECH))
            menu.findItem(R.id.czech).setChecked(true);
        if (languageEna.contains(Helper.LanguageEnum.ENGLISH))
            menu.findItem(R.id.english).setChecked(true);
        if (languageEna.contains(Helper.LanguageEnum.SPANISH))
            menu.findItem(R.id.spanish).setChecked(true);
        if (languageEna.contains(Helper.LanguageEnum.SLOVAK))
            menu.findItem(R.id.slovak).setChecked(true);
        if (languageEna.contains(Helper.LanguageEnum.OTHER))
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
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (item.getItemId()) {
            case R.id.action_settings:
                getFragmentManager().beginTransaction()
                        .replace(R.id.content, new SettingsFragment())
                        .addToBackStack("SettingsFragment")
                        .commit();
                return true;
            case R.id.sortByName:
                LoadSongsList(mSongList, ALPHABETICAL_BY_TITLE, languageEna);
                editor.putInt(getString(R.string.sort_settings), 0);
                editor.apply();
                return true;
            case R.id.sortByArtist:
                LoadSongsList(mSongList, ALPHABETICAL_BY_ARTIST, languageEna);
                editor.putInt(getString(R.string.sort_settings), 1);
                editor.apply();
                return true;
            case R.id.sortByDate:
                LoadSongsList(mSongList, BY_DATE, languageEna);
                editor.putInt(getString(R.string.sort_settings), 2);
                editor.apply();
                return true;
            //TODO: Dont dismiss the menu
            case R.id.selectAll:
                un_selectAll();
            case R.id.czech:
                changeLanguage(item, Helper.LanguageEnum.CZECH);
                this.openOptionsMenu();
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

    private void un_selectAll(MenuItem... items) {
        //TODO: This
    }

    private void changeLanguage(MenuItem item, Helper.LanguageEnum languageEnum) {
        if (item.isChecked()) {
            item.setChecked(false);
            languageEna.remove(languageEnum);
        } else {
            item.setChecked(true);
            languageEna.add(languageEnum);
        }
        queryHandled = false;
        LoadSongsList(mSongList, getComparatorFromSettings(), languageEna);
    }

    private List<Song> filterByLanguage(List<Song> completeList, EnumSet<Helper.LanguageEnum> languageSetFilter) {
        List<Song> filteredSongList = new ArrayList<>();
        for (Song song : completeList) {
            if (languageSetFilter.contains(song.getmLanguage())) {
                filteredSongList.add(song);
            }
        }
        return filteredSongList;
    }

    private void LoadSongsList(List<Song> songs, Comparator<Song> songComparator, EnumSet<Helper.LanguageEnum> languageSetFilter) {
        mAdapter = new SongsAdapter(this, songComparator);
        if (!queryHandled) {
            FilterDatList filterDatList = new FilterDatList();
            filterDatList.execute(searchView.getQuery().toString());
        } else {
            mAdapter.add(filterByLanguage(songs, languageSetFilter));
            songListView.setAdapter(mAdapter);
            //Čekání na výběr písně uživatelem
            mAdapter.setOnItemClickListener(new SongsAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, Song song) {
                    if (view.getId() != R.id.YTButton) {
                        openPDFDocument(song);
                    } else
                        new openYoutube().execute(song.getmArtist() + " - " + song.getmTitle());
                }
            });
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        FilterDatList filterDatList = new FilterDatList();
        filterDatList.execute(newText);
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

    private class FilterDatList extends AsyncTask<String, Void, List<Song>> {
        @Override
        protected List<Song> doInBackground(String... params) {
            final List<Song> filteredSongList = filterByQuery(mSongList, params[0]);
            return filteredSongList;
        }

        @Override
        protected void onPostExecute(List<Song> filteredSongList) {
            super.onPostExecute(filteredSongList);
            queryHandled = true;
            LoadSongsList(filteredSongList, getComparatorFromSettings(), languageEna);
            songListView.scrollToPosition(0);
        }
    }

    private class UpdateDB extends AsyncTask<Void, Void, String> {
        File FinalDB;

        @Override
        protected String doInBackground(Void... params) {
            FinalDB = new File(getFilesDir(), "FinalDB");
            DownloadSongIntentService.Download(getBaseContext(), "someUrl", FinalDB);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == null) {
                //TODO: ReloadDB
            } else {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), s, Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }
    }

    private class openYoutube extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try {
                // This object is used to make YouTube Data API requests
                youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
                    public void initialize(HttpRequest request) throws IOException {
                    }
                }).setApplicationName(getPackageName()).build();

                // Define the API request for retrieving search results.
                YouTube.Search.List search = youtube.search().list("id,snippet");
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

