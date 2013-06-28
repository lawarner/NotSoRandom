package org.apps.notsorandom;

import java.util.Collection;

/**
 * Read-only interface for working with a music collection.
 * The collection implementation could be backed by a queue, db, network media, etc.
 */
public interface MusicCollection {

    public Collection<SongInfo> getAllSongs();

    public SongInfo getFirstSong();

    public SongInfo getNextSong();

    public SongInfo getSong(int idx);

    /**
     * Get song count.
     * @return number of songs in library.
     */
    public int getSongCount();

}
