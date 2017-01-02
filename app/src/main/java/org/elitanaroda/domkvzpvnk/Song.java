package org.elitanaroda.domkvzpvnk;

/**
 * Created by Dominik on 10.12.2016.
 */

public class Song {
    private String mTitle;
    private String mArtist;

    private int mDateAdded;
    private int mLanguge;
    private int mFileTypes;


    public Song(String title, String artist, int dateAdded, int languge, int fileTypes) {
        this.mTitle = title;
        this.mArtist = artist;
        this.mDateAdded=dateAdded;
        this.mLanguge=languge;
        this.mFileTypes=fileTypes;
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

}
