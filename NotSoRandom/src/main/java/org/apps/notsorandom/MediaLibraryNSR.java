package org.apps.notsorandom;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Abstraction of the media library, regardless of data sources.
 */
public class MediaLibraryNSR {
    MediaLibraryNSR() {
        songs_ = new ArrayList<SongInfo>(10);
    }

    public int scanForMedia(String folder, boolean subFolders) {
        return 0;
    }

    public SongInfo getFirstSong() {
        iter_ = songs_.iterator();
        return getNextSong();
    }

    public SongInfo getNextSong() {
        if (iter_.hasNext())
            return iter_.next();

        return null;
    }

    public SongInfo getSong(int idx) {
        if (idx >= songs_.size())
            return null;

        return songs_.get(idx);
    }

    public int getSongCount() {
        return songs_.size();
    }

    /**
     * Make sure all the songs with the same sense value are grouped together.
     * The songs do not have to be strictly in order, as long as sense values are
     * grouped contiguous together.
     */
    public void sortSongs() {

    }

    protected ArrayList<SongInfo> songs_;
    protected Iterator<SongInfo> iter_;
}
