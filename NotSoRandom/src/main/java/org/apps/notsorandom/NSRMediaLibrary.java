package org.apps.notsorandom;

import java.util.Collection;

/**
 * Interface that media library class must implement.
 */
public interface NSRMediaLibrary {
    public interface OnLibraryChangedListener {
        void libraryUpdated(NSRMediaLibrary library);
    }

    public SongInfo getFirstSong();

    public SongInfo getNextSong();

    public SongInfo getSong(int idx);

    /**
     * Get song count.
     * @return number of songs in library.
     */
    public int getSongCount();

    /**
     * Register to listen for library change events.
     * @param listener class implementing interface to be notified when the media library updated.
     * @return the previous listener, or null if there was none.
     */
    public OnLibraryChangedListener registerOnLibraryChanged(OnLibraryChangedListener listener);

    public Collection<SongInfo> getAllSongs();

    /**
     * Scan the folder and optionally subfolders for media and add any found to the library.
     * @param folder  Path name to the media
     * @param subFolders True if subfolders should be scanned recurvsively for media.
     * @return the number of media added to the library.
     */
    public int scanForMedia(String folder, boolean subFolders);

    /**
     * Make sure all the songs with the same sense value are grouped together.
     * The songs do not have to be strictly in order, as long as sense values are
     * grouped contiguous together.
     */
    public void sortSongs();

}