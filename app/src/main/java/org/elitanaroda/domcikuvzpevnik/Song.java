package org.elitanaroda.domcikuvzpevnik;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

/**
 * The class responsible for one record of a "song"
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
    private boolean mHasChordPro;
    private boolean mIsOnLocalStorage;
    private File mSongFileSmall;
    private File mSongFileOriginal;

    public Song(Context context, int id, String title, String artist, int dateAdded, String language, int hasPDFgen, int hasChordPro) {
        this.mId = id;
        this.mTitle = title;
        this.mArtist = artist;
        this.mDateAdded = dateAdded;
        this.mLanguage = LanguageManager.LanguageEnum.valueOf(language);
        if (hasPDFgen == 0) {
            this.mHasPDFgen = false;
        } else {
            this.mHasPDFgen = true;
        }
        if (hasChordPro == 0) {
            this.mHasChordPro = false;
        } else {
            this.mHasChordPro = true;
        }

        this.mSongFileSmall = new File(context.getFilesDir().getAbsolutePath() + File.separatorChar + getFileName(false));
        this.mSongFileOriginal = new File(context.getFilesDir().getAbsolutePath() + File.separatorChar + getFileName(true));
        if (mSongFileSmall.isFile() && mSongFileOriginal.isFile()) {
            mSongFileOriginal.delete();
        }
        mIsOnLocalStorage = (mSongFileOriginal.isFile() || mSongFileSmall.isFile());
    }

    /**
     * Implementation of the Parceleable Interface in order to be able to pass the object within an intent - reconstructing the object
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
        this.mHasChordPro = (boolean) in.readValue(null);
        this.mIsOnLocalStorage = (boolean) in.readValue(null);
        this.mSongFileSmall = new File(in.readString());
        this.mSongFileOriginal = new File(in.readString());
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
        dest.writeValue(this.mHasChordPro);
        dest.writeValue(this.mIsOnLocalStorage);
        dest.writeString(mSongFileSmall.getAbsolutePath());
        dest.writeString(mSongFileOriginal.getAbsolutePath());
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public File getmSongFileOriginal() {
        return mSongFileOriginal;
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


    public boolean hasChordPro() {
        return mHasChordPro;
    }

    public String getChordProFileName() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.mArtist);
        stringBuilder.append("_");
        stringBuilder.append(this.mTitle);
        stringBuilder.append("-chordpro.txt");
        return Utils.makeTextNiceAgain(stringBuilder.toString().replace(" ", "_").replace(",", ""));
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

    public File getmSongFileSmall() {
        return mSongFileSmall;
    }

    /**
     * Generates the expected file name of its object
     *
     * @param originalQuality Choose whether to show the high quality image with my notes
     * @return The file name
     */
    public String getFileName(boolean originalQuality) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.mArtist);
        stringBuilder.append("_");
        stringBuilder.append(this.mTitle);
        if (originalQuality) {
            stringBuilder.append("-sken.pdf");
        } else if (mHasPDFgen) {
            stringBuilder.append("-gen.pdf");
        } else {
            stringBuilder.append("-comp.pdf");
        }
        return Utils.makeTextNiceAgain(stringBuilder.toString().replace(" ", "_").replace(",", ""));
    }

    /**
     * Generated methods for comparing two songs
     */

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
