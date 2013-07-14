package org.apps.notsorandom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Common implementation of the media library, regardless of data sources.
 */
public class MediaLibraryBaseImpl implements NSRMediaLibrary {
    private static final String TAG = "MusicMediaLibraryBase";

    protected ArrayList<SongInfo> songs_;
    protected Iterator<SongInfo> iter_;
    protected ArrayList<Integer> shuffled_;

    protected  OnLibraryChangedListener listener_;


    MediaLibraryBaseImpl() {
        songs_ = new ArrayList<SongInfo>(10);
        shuffled_ = null;
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
    public SenseComponent getComponent(String name) {
        return null;
    }

    @Override
    public Config getConfig(String user) {
        return null;
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
    public int[] getShuffledSongs(boolean reshuffle) {
        if (getSongCount() < 1)     // there are no songs
            return new int[0];

        if (shuffled_ == null) {
            shuffled_ = new ArrayList<Integer>(getSongCount());
            for (int i = 0; i < getSongCount(); i++)
                shuffled_.add(i, new Integer(i));

            reshuffle = true;     // force first-time shuffle
        }

        if (reshuffle)
            Collections.shuffle(shuffled_);

        int[] ret = new int[shuffled_.size()];
        int i = 0;
        for (Integer n : shuffled_) {
            ret[i++] = n.intValue();
        }

        return ret;
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

                // within same sense value, sort by title
                return songInfo.getTitle().compareToIgnoreCase(songInfo2.getTitle());
            }
        });

        if (listener_ != null) {
            listener_.libraryUpdated(this);
        }
    }

    @Override
    public boolean updateSenseValue(SongInfo song, int sense) {
        if (song == null)
            return false;

        song.setSense(sense);
        return updateSongInfo(song);
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
            MusicPlayerApp.log(TAG, "updateSongInfo(" + item + ") " + song.getTitle());
            songs_.set(item, song);
            sortSongs();
        }
        catch (IndexOutOfBoundsException ie) {
            ret = false;
        }

        if (listener_ != null && ret)
            listener_.libraryUpdated(this);

        return ret;
    }

    @Override
    public boolean updateSongInfo(SongInfo song) {
        return true;
    }
}
