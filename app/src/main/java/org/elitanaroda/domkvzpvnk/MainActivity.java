package org.elitanaroda.domkvzpvnk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "Main";

    private DBHelper mDBHelper;
    private RecyclerView seznamPisnicek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seznamPisnicek=(RecyclerView)findViewById(R.id.Pisnicky);

        //Nacteni databaze a jeji ulozeni do arraylistu
        mDBHelper = new DBHelper(this);
        try {
            mDBHelper.createDataBase();
        } catch (IOException io) {
            io.getMessage();
        }
        mDBHelper.openDataBase();
        ArrayList<Pisnicka> arrayList = mDBHelper.getAllData();

        PisnickyAdapter adapter=new PisnickyAdapter(this,arrayList);
        seznamPisnicek.setAdapter(adapter);
        seznamPisnicek.setLayoutManager(new LinearLayoutManager(this));
    }
}
