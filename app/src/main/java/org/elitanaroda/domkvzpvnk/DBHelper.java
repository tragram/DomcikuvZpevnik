package org.elitanaroda.domkvzpvnk;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dominik on 09.12.2016.
 */

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "FinalDB.db";
    public static String DB_PATH = "";
    private static String TAG = "ZpevnikHelper";
    private final Context mContext;
    private SQLiteDatabase mDataBase;

    //Konstruktor
    public DBHelper(Context context) {
        super(context, DB_NAME, null, 1);
        getDBPath(context);
        this.mContext = context;
    }

    //Úložiště databáze záleží na verzi Androidu
    private void getDBPath(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 17) {
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        } else {
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        }
    }

    //Tvorba databaze
    public void createDataBase() throws IOException {
        //Kopirujeme ji jen kdyz zatim neexistuje
        boolean mDataBaseExist = checkDataBase();
        if (!mDataBaseExist) {
            Log.i(TAG, "The database doesn't exist - copying it from the assets.");
            this.getReadableDatabase();
            try {
                //Copy the database from assests
                copyDataBase();
                Log.i(TAG, "createDatabase database created");
            } catch (IOException mIOException) {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    //Copy the database from assets
    private void copyDataBase() throws IOException {
        InputStream mInput = mContext.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream mOutput = new FileOutputStream(outFileName);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0) {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    public void openDataBase() throws SQLException {
        //Open the database
        String myPath = DB_PATH + DB_NAME;
        mDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        Log.i(TAG, "Databaze " + myPath + " byla otevrena!");
    }

    public List<Song> getAllData() {
        List<Song> arrayList = new ArrayList<>();
        try {
            Cursor res = mDataBase.rawQuery("SELECT * from Songs", null);
            res.moveToFirst();

            //Vytvorime ArrayList objektu pisnicek pro RecyclerView
            while (!res.isAfterLast()) {
                Song song = new Song(
                        res.getInt(0), //res.getColumnIndex("_id")
                        res.getString(res.getColumnIndex("Title")),
                        res.getString(res.getColumnIndex("Artist")),
                        res.getInt(res.getColumnIndex("AddedOn")),
                        res.getInt(res.getColumnIndex("Language")),
                        res.getInt(res.getColumnIndex("hasGen"))
                );

                arrayList.add(song);
                res.moveToNext();
            }
            res.close();
        } catch (Exception ex) {
            ex.getMessage();
            Log.e(TAG, "tak to teda ne, vracim prazdny arraylist");
        }
        return arrayList;
    }

    @Override
    public synchronized void close() {
        if (mDataBase != null)
            mDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e(TAG, "(Ne)provadim upgrade");
    }

}
