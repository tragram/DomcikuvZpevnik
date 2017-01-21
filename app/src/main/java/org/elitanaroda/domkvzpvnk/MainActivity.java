package org.elitanaroda.domkvzpvnk;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
search in async
download files
material design
search on youtube
select autoscroll speed
NFC send song
hlavolam.hasGen=true, navíc není vůbec sken ani u drobné paralely, el condor pasa, chci zas v tobě spát je pochybný
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
    private DBHelper mDBHelper;
    private RecyclerView songListView;
    private Toolbar mToolbar;
    private SongsAdapter mAdapter;
    private List<Song> mSongList;

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

    static String makeTextNiceAgain(String uglyText) {
        return Normalizer.normalize(uglyText.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        songListView = (RecyclerView) findViewById(R.id.SongRView);
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

        songListView.setLayoutManager(new LinearLayoutManager(this));
        songListView.setHasFixedSize(true);
        songListView.addItemDecoration(new SimpleDividerItemDecoration(this));
        //SnapHelper snapHelper = new LinearSnapHelper();
        //snapHelper.attachToRecyclerView(songListView);

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
            case R.id.sortBy:
                LoadSongsList(ALPHABETICAL_BY_ARTIST);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
        intent.putExtra("fileName", song.getFileName());
        startActivity(intent);
    }

    public void openYoutube(String song) {
        //TODO: Funguje to pochybně
        Intent intent = new Intent(Intent.ACTION_SEARCH);
        intent.setPackage("com.google.android.youtube");
        intent.putExtra("query", song);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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

