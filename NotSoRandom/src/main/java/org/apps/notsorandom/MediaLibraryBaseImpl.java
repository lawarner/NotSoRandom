package org.apps.notsorandom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Common implementation of the media library, regardless of data sources.
 */
public class MediaLibraryBaseImpl implements NSRMediaLibrary {
    protected ArrayList<SongInfo> songs_;
    protected Iterator<SongInfo> iter_;

    protected  OnLibraryChangedListener listener_;


    MediaLibraryBaseImpl() {
        songs_ = new ArrayList<SongInfo>(10);
        listener_ = null;
    }

    @Override
    public void initialize() {

    }

    @Override
    public ArrayList<SongInfo> getAllSongs() {
        return songs_;
    }

    @Override
    public SongInfo getFirstSong() {
        iter_ = songs_.iterator();
        return getNextSong();
    }

    @Override
    public SongInfo getNextSong() {
        if (iter_.hasNext())
            return iter_.next();

        return null;
    }

    @Override
    public SongInfo getSong(int idx) {
        if (idx >= songs_.size())
            return null;

        return songs_.get(idx);
    }

    @Override
    public int getSongCount() {
        return songs_.size();
    }

    @Override
    public OnLibraryChangedListener registerOnLibraryChanged(OnLibraryChangedListener listener) {
        OnLibraryChangedListener temp = listener_;
        listener_ = listener;
        return temp;
    }

    @Override
    public int scanForMedia(String folder, boolean subFolders) {
        return 0;
    }

    @Override
    public void sortSongs() {
        //TODO keep an isSorted flag
        //TODO sort according to x,y,z SenseComponents (see Config)
        Collections.sort(songs_, new Comparator<SongInfo>() {
            @Override
            public int compare(SongInfo songInfo, SongInfo songInfo2) {
                if (songInfo.getSenseValue() < songInfo2.getSenseValue())
                    return -1;
                else if (songInfo.getSenseValue() > songInfo2.getSenseValue())
                    return 1;

                return 0;
            }
        });

        if (listener_ != null)
            listener_.libraryUpdated(this);
    }

    @Override
    public boolean updateSenseValue(int item, int sense) {
        if (item < 0 || item > songs_.size())
            return false;

        SongInfo song = songs_.get(item);
        if (song == null)
            return false;

        song.setSense(sense);
        return updateSongInfo(item, song);
    }

    @Override
    public boolean updateSongInfo(int item, SongInfo song) {
        boolean ret = true;
        try {
            songs_.set(item, song);
        }
        catch (IndexOutOfBoundsException ie) {
            ret = false;
        }

        if (listener_ != null && ret)
            listener_.libraryUpdated(this);

        return ret;
    }

}
