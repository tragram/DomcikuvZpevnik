package org.elitanaroda.domkvzpvnk;

import android.content.Intent;
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
zkulturnit kód
*/
public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
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
    private RecyclerView songList;
    private Toolbar mToolbar;
    private SongsAdapter mAdapter;
    private ArrayList<Song> mSongArrayList;

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
        String pisnicky = "";
        for (Song song : filteredSongList) {
            pisnicky += (song.getmTitle() + " ");
        }
        Log.i(TAG, "___" + pisnicky);
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

        songList = (RecyclerView) findViewById(R.id.Pisnicky);
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
        mSongArrayList = mDBHelper.getAllData();

        //Připnutí adapteru a nastavení jeho parametrů
        mAdapter = new SongsAdapter(this, ALPHABETICAL_BY_TITLE);
        mAdapter.add(mSongArrayList);
        songList.setAdapter(mAdapter);
        songList.setLayoutManager(new LinearLayoutManager(this));
        songList.setHasFixedSize(true);
        songList.addItemDecoration(new SimpleDividerItemDecoration(this));
        //SnapHelper snapHelper = new LinearSnapHelper();
        //snapHelper.attachToRecyclerView(songList);

        //Čekání na výběr písně uživatelem
        mAdapter.setOnItemClickListener(new SongsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Song song) {
                openPDFDocument(song);
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
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final List<Song> filteredSongList = filter(mSongArrayList, newText);
        //mAdapter.clear();
        mAdapter.replaceAll(filteredSongList);
        //TODO: Scroll to position 0
        return true;
    }

    //Spuštění aktivity se souborem
    public void openPDFDocument(Song song) {
        Intent intent = new Intent(this, PDFActivity.class);
        intent.putExtra("fileName", song.getFileName());
        startActivity(intent);
    }

}

