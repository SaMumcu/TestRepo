package com.gradientinsight.gallery01.model;

import java.util.Comparator;

public class Album implements Comparable {

    private String albumId;
    private String albumName;
    private String path;
    private String timeStamp;
    private String date;
    private String photoCount;

    public Album(String albumId, String albumName, String path, String timeStamp, String date, String photoCount) {
        this.albumId = albumId;
        this.albumName = albumName;
        this.path = path;
        this.timeStamp = timeStamp;
        this.date = date;
        this.photoCount = photoCount;
    }

    public String getAlbumId() {
        return albumId;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getPath() {
        return path;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getDate() {
        return date;
    }

    public String getPhotoCount() {
        return photoCount;
    }

    @Override
    public int compareTo(Object o) {
        Album compare = (Album) o;
        if (compare.getAlbumId().equals(this.albumId) && compare.albumName.equals(this.albumName) && compare.timeStamp.equals(this.timeStamp)) {
            return 0;
        }
        return 1;
    }
}
