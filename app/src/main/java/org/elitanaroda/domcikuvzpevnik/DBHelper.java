package org.elitanaroda.domcikuvzpevnik;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by Dominik Hodan on 09.12.2016.
 */

/**
 * Class used to read the DB and save it to an array
 */
public class DBHelper extends SQLiteOpenHelper {
    /**
     * The constant DB_NAME.
     */
    public static final String DB_NAME = "FinalDB.db";
    private static String TAG = "ZpevnikHelper";
    private final Context mContext;
    private SQLiteDatabase mDataBase;

    /**
     * Instantiates a new Db helper.
     *
     * @param context the context
     */
//Konstruktor
    public DBHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.mContext = context;
    }

    /**
     * Creates the directory if neccessary
     *
     * @return The db folder directory path
     */
    public String getDBPath() {
        File dbDir = new File(mContext.getFilesDir() + File.separator + "db");
        if (!dbDir.isDirectory())
            dbDir.mkdirs();
        return dbDir.toString();
    }

    /**
     *
     * @throws SQLException Upon unsuccessful opening of the DB
     */
    private void openDataBase() throws SQLException {
        String myPath = getDBPath() + File.separator + DB_NAME;
        mDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        Log.v(TAG, "Database " + myPath + " opened!");
    }

    /**
     * Queries the DB for data and creates the Song objects
     *
     * @return An array list of all the songs in the DB
     */
    public List<Song> getAllSongs() {
        openDataBase();
        List<Song> arrayListSongs = new ArrayList<>();
        try {
            Cursor res = mDataBase.rawQuery("SELECT * from Songs", null);
            res.moveToFirst();

            //Vytvorime ArrayList objektu pisnicek pro RecyclerView
            int id = res.getColumnIndex("_id");
            int titleColumn = res.getColumnIndex("Title");
            int artistColumn = res.getColumnIndex("Artist");
            int addedOnColumn = res.getColumnIndex("AddedOn");
            int languageColumn = res.getColumnIndex("Lang");
            int hasGenColumn = res.getColumnIndex("hasGen");
            int hasChordProColumn = res.getColumnIndex("hasChordPro");

            while (!res.isAfterLast()) {
                Song song = new Song(
                        mContext,
                        res.getInt(id),
                        res.getString(titleColumn),
                        res.getString(artistColumn),
                        res.getInt(addedOnColumn),
                        res.getString(languageColumn),
                        res.getInt(hasGenColumn),
                        res.getInt(hasChordProColumn)
                );

                arrayListSongs.add(song);
                res.moveToNext();
            }
            res.close();
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
        return arrayListSongs;
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
