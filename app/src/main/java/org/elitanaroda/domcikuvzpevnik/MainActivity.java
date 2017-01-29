package org.elitanaroda.domcikuvzpevnik;

import android.app.ActivityManager;
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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/*
Global TODO:
NFC send song
permissions
chci zas v tobě spát je pochybný
*/

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
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
    private static String TAG = "Main";
    //TODO: Pořádný řazení... Problém je v tom, že i když srovná dva, tak nesrovná všechny :((
    private static final Comparator<Song> BY_DATE = new Comparator<Song>() {
        @Override
        public int compare(Song o1, Song o2) {
            Log.i(TAG, String.valueOf(o1.getmDateAdded()) + " " + String.valueOf(o2.getmDateAdded()));
            if (o1.getmDateAdded() == o2.getmDateAdded())
                return Normalizer.normalize(o1.getmTitle(), Normalizer.Form.NFD).
                        compareTo(Normalizer.normalize(o2.getmTitle(), Normalizer.Form.NFD));
            else
                return o1.getmDateAdded() - o2.getmDateAdded();

        }
    };
    private DBHelper mDBHelper;
    private RecyclerView songListView;
    private Toolbar mToolbar;
    private SongsAdapter mAdapter;
    private List<Song> mSongList;

    public static String makeTextNiceAgain(String uglyText) {
        return Normalizer.normalize(uglyText.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    //Vytvoření seznamu odpovídajícího hledání
    private static List<Song> filter(List<Song> songs, String query) {
        final String niceQuery = makeTextNiceAgain(query);
        Log.i(TAG, niceQuery);

        List<Song> filteredSongList = new ArrayList<>();
        for (Song song : songs) {
            final String text = makeTextNiceAgain(song.getmTitle());
            if (text.contains(niceQuery)) {
                filteredSongList.add(song);
                Log.i(TAG, song.getmTitle());
            }
        }
        return filteredSongList;
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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

        LoadSongsList(ALPHABETICAL_BY_TITLE);

        //SnapHelper snapHelper = new LinearSnapHelper();
        //snapHelper.attachToRecyclerView(songListView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    private void LoadSongsList(Comparator<Song> songComparator) {
        mAdapter = new SongsAdapter(this, songComparator);
        mAdapter.add(mSongList);
        songListView.setAdapter(mAdapter);
        //Čekání na výběr písně uživatelem
        mAdapter.setOnItemClickListener(new SongsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Song song) {
                if (view.getId() != R.id.YTButton) {
                    openPDFDocument(song);
                } else
                    openYoutube(song.getmArtist() + " - " + song.getmTitle());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        return true;
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
                LoadSongsList(ALPHABETICAL_BY_TITLE);
                return true;
            case R.id.sortByArtist:
                LoadSongsList(ALPHABETICAL_BY_ARTIST);
                return true;
            case R.id.sortByDate:
                LoadSongsList(BY_DATE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //TODO:close searchview on no text
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
        if (!song.ismIsOnLocalStorage() && isMyServiceRunning(this, DownloadSongIntentService.class)) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Dočkej času jako husa klasu!", Snackbar.LENGTH_LONG);
            snackbar.show();
        } else
            startActivity(intent);

        //Bez tohohle by to při dalším otevření nic neotevřelo, pokud se to smazalo
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        song.setmIsOnLocalStorage(sharedPref.getBoolean("keepFiles", true));
    }

    public void openYoutube(String song) {
        //TODO: Funguje to pochybně
        Intent intent = new Intent(this, YouTubeActivity.class);
        intent.putExtra("query", song);
        startActivity(intent);
    }

    private class FilterDatList extends AsyncTask<String, Void, List<Song>> {
        @Override
        protected List<Song> doInBackground(String... params) {
            final List<Song> filteredSongList = filter(mSongList, params[0]);
            return filteredSongList;
        }

        @Override
        protected void onPostExecute(List<Song> filteredSongList) {
            super.onPostExecute(filteredSongList);
            mAdapter.replaceAll(filteredSongList);
            songListView.scrollToPosition(0);
        }
    }
}

