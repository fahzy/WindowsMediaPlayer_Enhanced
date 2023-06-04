package com.fahzycoding.windowsmediaplayer_enhanced;

import java.awt.*;

public class MediaInfo {
    public MediaInfo() {

    }

    MediaInfo(String username, String directory, String filename, String filetype, boolean onDevice, boolean backedUp){
        this.userId = username;
        this.directory = directory;
        this.filename = filename;
        this.onDevice = onDevice;
        this.backedUp = backedUp;
        this.filetype = filetype;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    private String mediaId;
    private String userId;
    private String directory;
    private String filename;
    private String filetype;
    private boolean backedUp;
    private boolean onDevice;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean isBackedUp() {
        return backedUp;
    }

    public void setBackedUp(boolean backedUp) {
        this.backedUp = backedUp;
    }

    public boolean isOnDevice() {
        return onDevice;
    }

    public void setOnDevice(boolean onDevice) {
        this.onDevice = onDevice;
    }
}
