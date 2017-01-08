package org.elitanaroda.domkvzpvnk;

/**
 * Created by Dominik on 10.12.2016.
 */

public class Song {
    private int mId;
    private String mTitle;
    private String mArtist;

    private int mDateAdded;
    private int mLanguge;
    private int mFileTypes;


    public Song(int id, String title, String artist, int dateAdded, int language, int fileTypes) {
        this.mId = id;
        this.mTitle = title;
        this.mArtist = artist;
        this.mDateAdded=dateAdded;
        this.mLanguge = language;
        this.mFileTypes=fileTypes;
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

    public int getmDateAdded() {
        return mDateAdded;
    }

    public int getmFileTypes() {
        return mFileTypes;
    }

    public int getmLanguge() {
        return mLanguge;
    }


    //Vygenerované metody pro porovnávání objektů při řazení
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Song song = (Song) o;

        if (mDateAdded != song.mDateAdded) return false;
        if (mLanguge != song.mLanguge) return false;
        if (mFileTypes != song.mFileTypes) return false;
        if (mTitle != null ? !mTitle.equals(song.mTitle) : song.mTitle != null) return false;
        return mArtist != null ? mArtist.equals(song.mArtist) : song.mArtist == null;

    }

    @Override
    public int hashCode() {
        int result = mTitle != null ? mTitle.hashCode() : 0;
        result = 31 * result + (mArtist != null ? mArtist.hashCode() : 0);
        result = 31 * result + mDateAdded;
        result = 31 * result + mLanguge;
        result = 31 * result + mFileTypes;
        return result;
    }
}
