package org.apps.notsorandom;

/**
 * Created by andy on 6/22/13.
 */
public class SongInfo {
    private String title_;
    private String fileName_;
    private int senseValue_;

    public SongInfo(String title, String fileName, int senseValue) {
        title_ = title;
        fileName_ = fileName;
        senseValue_ = senseValue;
    }

    public String getTitle() {
        return title_;
    }

    public String getFileName() {
        return fileName_;
    }

    public int getSenseIndex() {
        int ret = (senseValue_ & 0xf0) >> 1 | (senseValue_ & 0x0f);
        return ret;
    }

    public int getSenseValue() {
        return senseValue_;
    }
}
