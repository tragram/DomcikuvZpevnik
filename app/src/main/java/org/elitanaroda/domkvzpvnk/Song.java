package org.elitanaroda.domkvzpvnk;

import java.text.Normalizer;

/**
 * Created by Dominik on 10.12.2016.
 */

public class Song {
    private int mId;
    private String mTitle;
    private String mArtist;

    private int mDateAdded;
    private byte mLanguage;
    private boolean mHasPDFgen;

    public Song(int id, String title, String artist, int dateAdded, int language, int hasPDFgen) {
        this.mId = id;
        this.mTitle = title;
        this.mArtist = artist;
        this.mDateAdded = dateAdded;
        this.mLanguage = ((byte) language);
        if (hasPDFgen == 0)
            this.mHasPDFgen = false;
        else
            this.mHasPDFgen = true;
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

    /*public boolean mHasPDFgen() {
        return mHasPDFgen;
    }*/

    public int getmLanguage() {
        return mLanguage;
    }

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
}
