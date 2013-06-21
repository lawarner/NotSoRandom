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
        songs_.add(new SongInfo("All I Want To Do...", "/mnt/sdcard/test1.mp3", 0x0101));
        songs_.add(new SongInfo("Get On Outch Ere", "/mnt/sdcard/test2.mp3", 0x0202));
        songs_.add(new SongInfo("Iggy Pop? Uh, I forget :)", "/mnt/sdcard/test3.mp3", 0x0303));

        return 3;
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

    public int getNumberSongs() {
        return songs_.size();
    }

    public SongInfo getSong(int idx) {
        if (idx >= songs_.size())
            return null;

        return songs_.get(idx);
    }

    private ArrayList<SongInfo> songs_;
    private Iterator<SongInfo> iter_;
}
