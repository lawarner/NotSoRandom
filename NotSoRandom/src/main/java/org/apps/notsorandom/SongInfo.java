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
    private String album_;
    private boolean longForm_;

    private String lazyToString_;


    public SongInfo(String title, String fileName, int senseValue, String artist) {
        title_ = title;
        fileName_ = fileName;
        senseValue_ = senseValue;
        artist_ = artist;
        longForm_ = false;
    }

    public String getAlbum() {
        if (album_ == null || album_.length() < 1)
            return "";

        return ", " + album_;
    }

    public String getArtist() {
        return artist_;
    }
/*
    public String getArtist(boolean init) {
        if (artist_ == null) {
            artist_ = "Unknown";
            if (!init) return artist_;

            String str = getRelativeFileName(null);
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

    /**
     * Get either the full path name, or a partial, relative path name, if the file is
     * in a sub-folder of path specified by rootPath.
     * @param fileName Full path name of file.
     * @param rootPath The root path of media files.  This is usually the mount point of
     *                 either an SDCARD storage.  If null, then
     *                 #Environment.getExternalStorageDirectory() is used.
     * @return
     */
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

    public int getSenseIndex(Config config) {
        return senseToIndex(senseValue_, config);
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

    public void setAlbum(String album) {
        if (album == album_)
            return;
        setModified();
        album_ = album;
    }

    public void setLongForm(boolean longForm) {
        if (longForm == longForm_)
            return;
        setModified();
        longForm_ = longForm;
    }

    private void setModified() {
        lazyToString_ = null;
    }

    /**
     * Set this song's sense value.
     * @param sense new sense value. No error checking is performed.
     * @return the previous sense value.
     */
    public int setSense(int sense) {
        int oldSense = senseValue_;
        lazyToString_ = null;
        senseValue_ = sense;
        return oldSense;
    }

    public String toString() {
        if (lazyToString_ == null) {
            String artist = getArtist();
            if (artist == null)
                artist = "";
            lazyToString_ = (longForm_ ? "* " : "") + getTitle() + "\n"
                    + getSenseString() + "  " + artist + getAlbum();
            if (longForm_)
                lazyToString_ += "\n-File: " + getRelativeFileName(getFileName(), null);
        }

        return lazyToString_;
    }

    /**
     * Simple convenience method. Converts the first 4 dimensions in order into an index.
     * @param idx Sequential index of values
     * @return Encoded sense value (current only 4 dimensions).
     */
    public static int indexToSense(int idx) {
        int sv = ((idx & 0xe000) << 3) | ((idx & 0x01c0) << 2)
               | ((idx & 0x0038) << 1) |  (idx & 0x0007);
        return sv;
    }

    public static int indexToSense(int idx, Config config) {
        int ix =  idx & 0x0007;
        int iy = (idx & 0x0038) >> 3;
        int iz = (idx & 0x01c0) >> 6;
        int i1 = (idx & 0xfffffe00);
//        int i4 = (idx & 0xe000) << 1;
        int ret = config.getXcomponent().getMaskedValue(ix)
                | config.getYcomponent().getMaskedValue(iy)
                | config.getZcomponent().getMaskedValue(iz)
                | (~config.getXYZMask() & i1);
        return ret;
    }

    /**
     * Simple convenience method.
     * @param sense Encoded sense value (current only 4 dimensions).
     * @return Sequential index of value
     */
    public static int senseToIndex(int sense) {
        int idx = ((sense & 0x7000) >> 3) | ((sense & 0x0700) >> 2)
                | ((sense & 0x0070) >> 1) | (sense & 0x0007);
        return idx;
    }

    public static int senseToIndex(int sense, Config config) {
        int ix = config.getXcomponent().getComponentValue(sense);
        int iy = config.getYcomponent().getComponentValue(sense);
        int iz = config.getZcomponent().getComponentValue(sense);
        int i1 = ~config.getXYZMask() & sense;
        int ret = ix | iy << 3 | iz << 6 | i1;

        return ret;
    }

}
