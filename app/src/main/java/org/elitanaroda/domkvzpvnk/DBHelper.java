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

/**
 * Created by Dominik on 09.12.2016.
 */

public class DBHelper extends SQLiteOpenHelper {
    private static String TAG = "ZpevnikHelper";

    public static final String DB_NAME = "data.db";
    public static String DB_PATH = "";
    private SQLiteDatabase mDataBase;
    private final Context mContext;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, 1);
        getDBPath(context);
        this.mContext = context;
    }

    //U jinych verzi se databaze uklada jinam
    private void getDBPath(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 17) {
            this.DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        } else {
            this.DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        }
        Log.e(TAG, "Saving DB_PATH as " + DB_PATH);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    public void createDataBase() throws IOException {
        //If the database does not exist, copy it from the assets.
        boolean mDataBaseExist = checkDataBase();
        Log.e(TAG, "The database exists? " + String.valueOf(mDataBaseExist));
        if (!mDataBaseExist) {
            this.getReadableDatabase();
            try {
                //Copy the database from assests
                copyDataBase();
                Log.e(TAG, "createDatabase database created");
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
        Log.e(TAG, "Databaze " + myPath + " byla otevrena!");
    }

    @Override
    public synchronized void close() {
        if (mDataBase != null)
            mDataBase.close();
        super.close();
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e(TAG, "(Ne)provadim upgrade");
    }

    public ArrayList<String> getAllData() {
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            Cursor res = mDataBase.rawQuery("SELECT * from data", null);
            res.moveToFirst();

            while (!res.isAfterLast()) {
                arrayList.add(res.getString(res.getColumnIndex("nazev")));
                res.moveToNext();
            }
        } catch (Exception ex) {
            ex.getMessage();
            Log.e(TAG, "tak to teda ne, vracim prazdny arraylist");
        }
        return arrayList;
    }
}
