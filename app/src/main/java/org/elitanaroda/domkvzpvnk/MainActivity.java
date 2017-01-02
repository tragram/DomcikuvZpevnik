package org.elitanaroda.domkvzpvnk;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.util.ArrayList;


import android.content.Intent;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{
    private static String TAG = "Main";

    private DBHelper mDBHelper;
    private RecyclerView songList;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        songList = (RecyclerView) findViewById(R.id.Pisnicky);
        mToolbar=(Toolbar)findViewById(R.id.mToolbar) ;
        setSupportActionBar(mToolbar);

        //Nacteni databaze a jeji ulozeni do arraylistu
        mDBHelper = new DBHelper(this);
        try {
            mDBHelper.createDataBase();
        } catch (IOException io) {
            io.getMessage();
        }
        mDBHelper.openDataBase();
        ArrayList<Song> arrayList = mDBHelper.getAllData();

        //Připnutí adapteru a nastavení jeho parametrů
        SongsAdapter adapter = new SongsAdapter(this, arrayList);
        songList.setAdapter(adapter);
        songList.setLayoutManager(new LinearLayoutManager(this));
        songList.setHasFixedSize(true);
        songList.addItemDecoration(new SimpleDividerItemDecoration(this));
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(songList);

        //Čekání na výběr písně uživatelem
        adapter.setOnItemClickListener(new SongsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view) {
                openPDFDocument(view);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }


    //Spuštění aktivity se souborem
    public void openPDFDocument(View view){
        Intent intent=new Intent(this, PDFActivity.class);
        intent.putExtra("nazevPisne","zkouska.pdf");
        startActivity(intent);
    }

}

