package org.elitanaroda.domkvzpvnk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "Main";
    /*private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;*/
    private ListView seznam;
    private DBHelper mDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        mRecyclerView = (RecyclerView) findViewById(R.id.SeznamPisni);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter=new MyAdapter(myDataset);
        mRecyclerView.setAdapter(mAdapter);


        DBHelper mDBHelper = new DBHelper(this);


        mDBAdapter.createDatabase();
        mDBAdapter.open();

        ArrayList<String> arrayList = mDBAdapter.getZpevnikData();
        mDBAdapter.close();*/
        mDBHelper = new DBHelper(this);
        try {
            mDBHelper.createDataBase();
        } catch (IOException io) {
            io.getMessage();
        }
        Log.e(TAG, "Oteviram...");
        mDBHelper.openDataBase();
        ArrayList<String> arrayList = mDBHelper.getAllData();

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayList);

        seznam = (ListView) findViewById(R.id.listView1);
        seznam.setAdapter(arrayAdapter);
        seznam.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO: Vyresit kliknuti
            }
        });
    }
}
