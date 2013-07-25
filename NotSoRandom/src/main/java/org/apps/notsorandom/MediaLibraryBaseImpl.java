package org.apps.notsorandom;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Common implementation of the media library, independent of data sources.
 */
public class MediaLibraryBaseImpl implements NSRMediaLibrary {
    private static final String TAG = "MusicMediaLibraryBase";

    protected ArrayList<SongInfo> songs_;
    protected Iterator<SongInfo> iter_;
    protected ArrayList<SongInfo> shuffled_;

    protected Config config_;

    protected boolean isSorted_;

    protected  OnLibraryChangedListener listener_;


    MediaLibraryBaseImpl() {
        songs_ = new ArrayList<SongInfo>(10);
        shuffled_ = null;
        config_ = null;
        isSorted_ = false;
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
    public ArrayList<SongInfo> getShuffledSongs(boolean reshuffle) {
        if (getSongCount() < 1)     // there are no songs
            return new ArrayList<SongInfo>(0);

        if (shuffled_ == null) {
            shuffled_ = new ArrayList<SongInfo>(getAllSongs());
            reshuffle = true;     // force first-time shuffle
        }

        if (reshuffle)
            Collections.shuffle(shuffled_);

        return shuffled_;
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


    protected class SenseComparator implements Comparator<SongInfo> {
        ArrayList<SenseComponent> components_;

        public SenseComparator(ArrayList<SenseComponent> components) {
            // First, sort the components by their sort order
            components_ = components;
            Collections.sort(components_, new Comparator<SenseComponent>() {
                @Override
                public int compare(SenseComponent sc1, SenseComponent sc2) {
                    if (sc1.getSortOrder() > sc2.getSortOrder())
                        return -1;
                    else if (sc1.getSortOrder() < sc2.getSortOrder())
                        return 1;

                    return 0;
                }
            });
        }

        @Override
        public int compare(SongInfo si1, SongInfo si2) {
            for (SenseComponent comp : components_) {
                if (comp.getComponentValue(si1.getSenseValue()) <
                    comp.getComponentValue(si2.getSenseValue()))
                    return -1;
                else if (comp.getComponentValue(si1.getSenseValue()) >
                        comp.getComponentValue(si2.getSenseValue()))
                    return 1;
            }

            // within same sense value, sort by title
            return si1.getTitle().compareToIgnoreCase(si2.getTitle());
        }
    }

    @Override
    public void sortSongs() {
//        if (isSorted_) return;

        if (config_ == null) {
            Log.d(TAG, "Sort songs, default sort order.");
            Collections.sort(songs_, new Comparator<SongInfo>() {
                @Override
                public int compare(SongInfo si1, SongInfo si2) {
                    if (si1.getSenseValue() < si2.getSenseValue())
                        return -1;
                    else if (si1.getSenseValue() > si2.getSenseValue())
                        return 1;

                    // within same sense value, sort by title
                    return si1.getTitle().compareToIgnoreCase(si2.getTitle());
                }
            });
        } else {
            Log.d(TAG, "Sort songs, sort order: " + config_.getXcomponent().getSortOrder() + ","
                    + config_.getYcomponent().getSortOrder() + "," + config_.getZcomponent().getSortOrder());
            ArrayList<SenseComponent> components = new ArrayList<SenseComponent>(3);
            if (config_.getXcomponent().getSortOrder() > 0) components.add(config_.getXcomponent());
            if (config_.getYcomponent().getSortOrder() > 0) components.add(config_.getYcomponent());
            if (config_.getZcomponent().getSortOrder() > 0) components.add(config_.getZcomponent());
            Collections.sort(songs_, new SenseComparator(components));
        }

        isSorted_ = true;
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
    public boolean updateSongInfo(int item, SongInfo song) {
        boolean ret = true;
        try {
            MusicPlayerApp.log(TAG, "updateSongInfo(" + item + ") " + song.getTitle());
            songs_.set(item, song);
            isSorted_ = false;
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
        isSorted_ = false;
        return true;
    }
}
