package org.apps.notsorandom;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Read-only interface for working with a music collection.
 * The collection implementation could be backed by a queue, db, network media, etc.
 */
public interface MediaMusicCollection {

    public Collection<SongInfo> getAllSongs();

    public SenseComponent getComponent(String name);

    public Config getConfig(String user);

    public SongInfo getFirstSong();

    public SongInfo getNextSong();

    public SongInfo getSong(int idx);

    /**
     * Get song count.
     * @return number of songs in library.
     */
    public int getSongCount();

    /**
     * Get a shuffled list of songs.  The list is reshuffled each time
     * this method is called, unless shuffle is false.
     * @param reshuffle If true then the list will be reshuffled.
     * @return list of songs in music collection, in random order.
     */
    public ArrayList<SongInfo> getShuffledSongs(boolean reshuffle);

}
