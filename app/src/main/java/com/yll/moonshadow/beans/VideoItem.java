package com.yll.moonshadow.beans;

/**
 * Created by yelelen on 7/14/2017.
 */

public class VideoItem {

    private String name;

    private long duration;

    private long size;

    private String path;

    private String artist;

    @Override
    public String toString() {
        return "VideoItem{" +
                "name='" + name + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                ", path='" + path + '\'' +
                ", artist='" + artist + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
