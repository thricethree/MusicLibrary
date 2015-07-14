package com.thricethree.musiclibrary.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by thricethree on 6/24/15.
 */
public class ArtistInfo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7582328802860337836L;
    private String artistName;
    private ArrayList<AlbumInfo> artistAlbums;
    private String artistID;

    public String getArtistName() {
        return artistName;
    }

    public ArrayList<AlbumInfo> getArtistAlbums() {
        return artistAlbums;
    }

    public String getArtistID() {
        return artistID;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setArtistAlbums(ArrayList<AlbumInfo> artistAlbums) {
        this.artistAlbums = artistAlbums;
    }

    public void setArtistID(String artistID) {
        this.artistID = artistID;
    }

    public void addAlbumToList(AlbumInfo album){
        artistAlbums.add(album);
    }

}
