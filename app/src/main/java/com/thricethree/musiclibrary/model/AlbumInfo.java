package com.thricethree.musiclibrary.model;

/**
 * Created by thricethree on 6/24/15.
 */
public class AlbumInfo {
    private String artistID;
    private String artistName;
    private String albumTitle;
    private String albumID;
    private String albumCoverUrlSmall;
    private String albumCoverUrlMedium;
    private boolean isSelected;

    public String getAlbumTitle() {
        return albumTitle;
    }

    public void setAlbumTitle(String albumTitle) {
        this.albumTitle = albumTitle;
    }

    public String getArtistID() {
        return artistID;
    }

    public void setArtistID(String artistID) {
        this.artistID = artistID;
    }

    public String getAlbumCoverUrlSmall() {
        return albumCoverUrlSmall;
    }

    public void setAlbumCoverUrlSmall(String albumCoverUrlSmall) {
        this.albumCoverUrlSmall = albumCoverUrlSmall;
    }

    public String getAlbumCoverUrlMedium() {
        return albumCoverUrlMedium;
    }

    public void setAlbumCoverUrlMedium(String albumCoverUrlMedium) {
        this.albumCoverUrlMedium = albumCoverUrlMedium;
    }



    public String getAlbumID() {
        return albumID;
    }

    public void setAlbumID(String albumID) {
        this.albumID = albumID;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

}