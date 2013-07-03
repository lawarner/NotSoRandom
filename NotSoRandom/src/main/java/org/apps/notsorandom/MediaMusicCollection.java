package org.apps.notsorandom;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Read-only interface for working with a music collection.
 * The collection implementation could be backed by a queue, db, network media, etc.
 */
public interface MediaMusicCollection {

    public Collection<SongInfo> getAllSongs();

    public SongInfo getFirstSong();

    public SongInfo getNextSong();

    public SongInfo getSong(int idx);

    /**
     * Get song count.
     * @return number of songs in library.
     */
    public int getSongCount();

    /**
     * Get a shuffled list of song indices.  The list is reshuffled each time
     * this method is called, unless shuffle is false.
     * @param reshuffle If true then the list will be reshuffled.
     * @return list of indices into music collection, in random order.
     */
    public int[] getShuffledSongs(boolean reshuffle);

}
