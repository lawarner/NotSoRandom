package org.apps.notsorandom;

import android.media.MediaMetadataRetriever;
import android.os.Environment;

/**
 * Represent info for a song.
 *
 * TODO add number of components
 */
public class SongInfo {
//    private static MediaMetadataRetriever mmr_ = new MediaMetadataRetriever();

    private String title_;
    private String fileName_;
    private int senseValue_;
    private String artist_;

    public SongInfo(String title, String fileName, int senseValue, String artist) {
        title_ = title;
        fileName_ = fileName;
        senseValue_ = senseValue;
        artist_ = artist;
    }

    public String getArtist() {
        return artist_;
    }
/*
    public String getArtist(boolean init) {
        if (artist_ == null) {
            if (!init) return "Unknown";

            mmr_.setDataSource(getFileName());
            String str = mmr_.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            if (str == null || str.isEmpty()) {
                artist_ = "Unknown";
                str = getRelativeFileName(null);
                int slash = str.lastIndexOf('/');
                if (slash > 2) {
                    int slash2 = str.lastIndexOf('/', slash - 1);
                    if (slash2 >= 0) {
                        artist_ = str.substring(slash2 + 1, slash);
                        slash = str.lastIndexOf('/', slash2 - 1);
                        if (slash >= 0) {
                            String artist2 = str.substring(slash + 1, slash2);
                            if (artist2.compareToIgnoreCase("0ther") == 0)
                                artist_ = "Soundtrack";
                            else if (artist2.compareToIgnoreCase("music") != 0)
                                artist_ = artist2;
                        }
                    }
                }
            } else
                artist_ = str;
        }

        return artist_;
    }
*/

    public String getFileName() {
        return fileName_;
    }

    public String getBaseFileName() {
        int endPath = fileName_.lastIndexOf('/');
        if (endPath >= 0)
            return fileName_.substring(endPath + 1);
        return fileName_;
    }

    public static String getRelativeFileName(String fileName, String rootPath) {
        String root;
        if (rootPath == null || rootPath.isEmpty())
            root = Environment.getExternalStorageDirectory().getAbsolutePath();
        else
            root = rootPath;

        if (fileName.startsWith(root))
            return fileName.substring(root.length());

        return fileName;    // Not the right prefix, return as is
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

    public String getTitle() {
        if (title_.isEmpty())
            return getBaseFileName();

        return title_;
    }

    /**
     * Set this song's sense value.
     * @param sense new sense value. No error checking is performed.
     * @return the previous sense value.
     */
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
