package org.apps.notsorandom;

/**
 * Interface that media library class must implement.
 */
public interface NSRMediaLibrary extends MediaMusicCollection {

    public interface OnLibraryChangedListener {
        void libraryUpdated(NSRMediaLibrary library);
    }

    public void initialize();

    /**
     * Register to listen for library change events.
     * @param listener class implementing interface to be notified when the media library updated.
     * @return the previous listener, or null if there was none.
     */
    public OnLibraryChangedListener registerOnLibraryChanged(OnLibraryChangedListener listener);

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

    /**
     * Update a song's sense value in the database.
     * @param song The song to update.
     * @param sense New sense value to store.
     * @return True if song was updated in database successfully, otherwise false.
     */
    public boolean updateSenseValue(SongInfo song, int sense);

    /**
     * Update a song's info in the database.  This currently only updates the sense value, but
     * in future, it could update any changed songinfo columns.
     * @param item The index into the song array.
     * @param song Song values to update.  The song's primary unique key on path name is used
     *             as key of song record to update.
     * @return True if song was updated in database successfully, otherwise false.
     */
    public boolean updateSongInfo(int item, SongInfo song);

    public boolean updateSongInfo(SongInfo song);

}
