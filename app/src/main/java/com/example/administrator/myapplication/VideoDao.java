package com.example.administrator.myapplication;

/**
 * Created by Administrator on 2018/6/7.
 */
public class VideoDao implements Comparable<VideoDao>{

    private long videoId;

    private String videoName;

    private byte[] videoFile;

    private long userId;

    private String username;

    private int liked;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getVideoId() {
        return videoId;
    }

    public void setVideoId(long videoId) {
        this.videoId = videoId;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public byte[] getVideoFile() {
        return videoFile;
    }

    public void setVideoFile(byte[] videoFile) {
        this.videoFile = videoFile;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getLiked() {
        return liked;
    }

    public void setLiked(int liked) {
        this.liked = liked;
    }

    @Override
    public int compareTo(VideoDao o) {
        int i = (int) (this.getVideoId() - o.getVideoId());//先按照年龄排序
        return i;
    }
}
