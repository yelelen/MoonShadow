package com.yll.moonshadow.beans;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yelelen on 7/14/2017.
 */

public class VideoItem implements Parcelable{

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


    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeLong(duration);
        dest.writeLong(size);
        dest.writeString(path);
        dest.writeString(artist);
    }

    public VideoItem(){}

    public VideoItem(Parcel in){
        name = in.readString();
        duration = in.readLong();
        size = in.readLong();
        path = in.readString();
        artist = in.readString();
    }

    public static final Parcelable.Creator<VideoItem> CREATOR = new Parcelable.Creator<VideoItem>() {
        public VideoItem createFromParcel(Parcel in) {
            return new VideoItem(in);
        }

        @Override
        public VideoItem[] newArray(int size) {
            return new VideoItem[size];
        }
    };
}
