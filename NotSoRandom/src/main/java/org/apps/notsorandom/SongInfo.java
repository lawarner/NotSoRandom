package org.apps.notsorandom;

import android.os.Environment;

/**
 * Created by andy on 6/22/13.
 *
 * TODO add number of components
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
        if (title_.isEmpty())
            return "File: " + getBaseFileName();

        return title_;
    }

    public String getFileName() {
        return fileName_;
    }

    public String getBaseFileName() {
        int endPath = fileName_.lastIndexOf('/');
        if (endPath >= 0)
            return fileName_.substring(endPath + 1);
        return fileName_;
    }

    public String getRelativeFileName(String rootPath) {
        String root;
        if (rootPath == null || rootPath.isEmpty())
            root = Environment.getExternalStorageDirectory().getAbsolutePath();
        else
            root = rootPath;

        if (fileName_.startsWith(root))
            return fileName_.substring(root.length());

        return fileName_;    // Not the right prefix, return as is
    }

    public int getSenseIndex() {
        return senseToIndex(senseValue_);
    }

    public String getSenseString() {
        if (senseValue_ < 16)
            return "s00" + Integer.toHexString(senseValue_);
        else if (senseValue_ < 256)
            return "s0" + Integer.toHexString(senseValue_);
        else
            return "s" + Integer.toHexString(senseValue_);
    }

    public int getSenseValue() {
        return senseValue_;
    }

    public int setSense(int sense) {
        int oldSense = senseValue_;
        senseValue_ = sense;
        return oldSense;
    }

    /**
     * Simple convenience method.
     * @param idx Sequential index of values
     * @return Encoded sense value (current only 2 parts).
     */
    public static int indexToSense(int idx) {
        int sv = ((idx & 0xf8) << 1) | (idx & 0x07);
        return sv;
    }

    /**
     * Simple convenience method.
     * @param sense Encoded sense value
     * @return Sequential index of value
     */
    public static int senseToIndex(int sense) {
        int idx = ((sense & 0xf0) >> 1) | (sense & 0x07);
        return idx;
    }

}
