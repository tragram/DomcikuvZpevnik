package org.elitanaroda.domkvzpvnk;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.text.Normalizer;

/**
 * Created by Dominik on 10.12.2016.
 */

public class Song implements Parcelable {
    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {

        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
    private int mId;
    private String mTitle;
    private String mArtist;
    private int mDateAdded;
    private byte mLanguage;
    private boolean mHasPDFgen;
    private boolean mIsOnLocalStorage;
    private File mSongFile;

    public Song(Context context, int id, String title, String artist, int dateAdded, int language, int hasPDFgen) {
        this.mId = id;
        this.mTitle = title;
        this.mArtist = artist;
        this.mDateAdded = dateAdded;
        this.mLanguage = ((byte) language);
        if (hasPDFgen == 0)
            this.mHasPDFgen = false;
        else
            this.mHasPDFgen = true;

        this.mSongFile = new File(context.getFilesDir().getAbsolutePath() + File.separatorChar + getFileName());
        if (mSongFile.isFile()) {
            mIsOnLocalStorage = true;
        } else
            mIsOnLocalStorage = false;
    }

    private Song(Parcel in) {
        this.mId = in.readInt();
        this.mTitle = in.readString();
        this.mArtist = in.readString();
        this.mDateAdded = in.readInt();
        this.mLanguage = in.readByte();
        this.mHasPDFgen = (boolean) in.readValue(null);
        this.mIsOnLocalStorage = (boolean) in.readValue(null);
        this.mSongFile = new File(in.readString());
    }

    public int getmId() {
        return mId;
    }

    public String getmTitle() {
        return mTitle;
    }

    public String getmArtist() {
        return mArtist;
    }

    public boolean ismIsOnLocalStorage() {
        return mIsOnLocalStorage;
    }

    public void setmIsOnLocalStorage(boolean mIsOnLocalStorage) {
        this.mIsOnLocalStorage = mIsOnLocalStorage;
    }

    public int getmDateAdded() {
        return mDateAdded;
    }

    public int getmLanguage() {
        return mLanguage;
    }

    public File getmSongFile() {
        return mSongFile;
    }

    //vygenerování názvu PDF souboru
    public String getFileName() {
        String fileName = this.mArtist + "_" + this.mTitle;
        fileName = MainActivity.makeTextNiceAgain(fileName);
        fileName = fileName.replace(" ", "_").replace(",", "");
        if (mHasPDFgen) {
            fileName += "-gen";
        } else {
            fileName += "-sken";
        }
        fileName += ".pdf";
        return fileName;
    }

    //Vygenerované metody pro porovnávání objektů při řazení
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Song song = (Song) o;

        if (mDateAdded != song.mDateAdded) return false;
        if (mLanguage != song.mLanguage) return false;
        if (mHasPDFgen != song.mHasPDFgen) return false;
        if (mTitle != null ? !mTitle.equals(song.mTitle) : song.mTitle != null) return false;
        return mArtist != null ? mArtist.equals(song.mArtist) : song.mArtist == null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mId);
        dest.writeString(this.mTitle);
        dest.writeString(this.mArtist);
        dest.writeInt(this.mDateAdded);
        dest.writeByte(this.mLanguage);
        dest.writeValue(this.mHasPDFgen);
        dest.writeValue(this.mIsOnLocalStorage);
        dest.writeString(mSongFile.getAbsolutePath());
    }
}
