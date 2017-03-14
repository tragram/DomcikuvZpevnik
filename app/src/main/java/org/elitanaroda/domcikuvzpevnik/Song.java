package org.elitanaroda.domcikuvzpevnik;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

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

    private LanguageManager.LanguageEnum mLanguage;
    private boolean mHasPDFgen;
    private boolean mIsOnLocalStorage;
    private File mSongFileSkenOrGen;
    private File mSongFileComp;
    private File mSongFileGen;

    public Song(Context context, int id, String title, String artist, int dateAdded, String language, int hasPDFgen) {
        this.mId = id;
        this.mTitle = title;
        this.mArtist = artist;
        this.mDateAdded = dateAdded;
        this.mLanguage = LanguageManager.LanguageEnum.valueOf(language);
        if (hasPDFgen == 0)
            this.mHasPDFgen = false;
        else
            this.mHasPDFgen = true;

        this.mSongFileSkenOrGen = new File(context.getFilesDir().getAbsolutePath() + File.separatorChar + getFileName(true));
        this.mSongFileComp = new File(context.getFilesDir().getAbsolutePath() + File.separatorChar + getFileName(false));
        if (mSongFileSkenOrGen.isFile() && mSongFileComp.isFile())
            mSongFileComp.delete();
        mIsOnLocalStorage = (mSongFileComp.isFile() || mSongFileSkenOrGen.isFile());
    }

    /**
     * Implementation of the Parceleable Interface, reconstructing the object
     *
     * @param in Parcel to reconstruct the object from
     */
    private Song(Parcel in) {
        this.mId = in.readInt();
        this.mTitle = in.readString();
        this.mArtist = in.readString();
        this.mDateAdded = in.readInt();
        this.mLanguage = LanguageManager.LanguageEnum.valueOf(in.readString());
        this.mHasPDFgen = (boolean) in.readValue(null);
        this.mIsOnLocalStorage = (boolean) in.readValue(null);
        this.mSongFileSkenOrGen = new File(in.readString());
        this.mSongFileComp = new File(in.readString());
    }

    /**
     * Implementation of the Parceleable Interface - creating the parcel
     *
     * @param dest The parcel to write the data into
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mId);
        dest.writeString(this.mTitle);
        dest.writeString(this.mArtist);
        dest.writeInt(this.mDateAdded);
        dest.writeString(this.mLanguage.toString());
        dest.writeValue(this.mHasPDFgen);
        dest.writeValue(this.mIsOnLocalStorage);
        dest.writeString(mSongFileSkenOrGen.getAbsolutePath());
        dest.writeString(mSongFileComp.getAbsolutePath());
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public File getmSongFileComp() {
        return mSongFileComp;
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

    public LanguageManager.LanguageEnum getmLanguage() {
        return mLanguage;
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

    public File getmSongFileSkenOrGen() {
        return mSongFileSkenOrGen;
    }

    /**
     * Generates the expected file name of its object
     * @param highQuality Choose whether to generate the name of a high-quality PDF (generated where available)
     * @return The file name
     */
    public String getFileName(boolean highQuality) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.mArtist);
        stringBuilder.append("_");
        stringBuilder.append(this.mTitle);
        if (mHasPDFgen) {
            stringBuilder.append("-gen");
        } else if (highQuality) {
            stringBuilder.append("-sken");
        } else {
            stringBuilder.append("-comp");
        }
        stringBuilder.append(".pdf");
        return Utils.makeTextNiceAgain(stringBuilder.toString().replace(" ", "_").replace(",", ""));
    }

    //Generated methods for comparing two songs
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
}
