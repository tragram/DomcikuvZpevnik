package org.elitanaroda.domkvzpvnk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.View;


import java.io.IOException;
import java.util.ArrayList;


import android.content.Intent;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "Main";

    private DBHelper mDBHelper;
    private RecyclerView seznamPisnicek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seznamPisnicek = (RecyclerView) findViewById(R.id.Pisnicky);

        //Nacteni databaze a jeji ulozeni do arraylistu
        mDBHelper = new DBHelper(this);
        try {
            mDBHelper.createDataBase();
        } catch (IOException io) {
            io.getMessage();
        }
        mDBHelper.openDataBase();
        ArrayList<Pisnicka> arrayList = mDBHelper.getAllData();

        //Připnutí adapteru a nastavení jeho parametrů
        PisnickyAdapter adapter = new PisnickyAdapter(this, arrayList);
        seznamPisnicek.setAdapter(adapter);
        seznamPisnicek.setLayoutManager(new LinearLayoutManager(this));
        seznamPisnicek.setHasFixedSize(true);
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(seznamPisnicek);

        //Čekání na výběr písně uživatelem
        adapter.setOnItemClickListener(new PisnickyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view) {
                openPDFDocument(view);
            }
        });
    }

    //Spuštění aktivity se souborem
    public void openPDFDocument(View view){
        Intent intent=new Intent(this, PDFActivity.class);
        intent.putExtra("nazevPisne","zkouska.pdf");
        startActivity(intent);
    }
}

