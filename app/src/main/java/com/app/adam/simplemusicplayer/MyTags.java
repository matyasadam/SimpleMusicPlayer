package com.app.adam.simplemusicplayer;

/**
 * Created by Adam on 5/24/2015.
 */
public class MyTags {


    private String genre;
    private String bmp;

    public MyTags() {
        genre=" ";
        bmp=" ";
    }
    public MyTags(String _genre,String _bmp) {
        genre=_genre;
        bmp=_bmp;
    }


    public String getBmp() {
        return bmp;
    }

    public void setBmp(String bmp) {
        this.bmp = bmp;
    }
    public String getGenre() {
        return genre;
    }
    public void setGenre(String genre) {
        this.genre = genre;
    }
}
