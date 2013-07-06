package org.apps.notsorandom;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

/**
 *
 * TODO implement a puddleMap for randomizing values.
 */
public class MusicMap {
    private static final String TAG = "MusicMap";

    public static final int MAPWIDTH  = 8;   // matrix width
    public static final int MAPHEIGHT = 8;   // matrix height
    public static final int MAPSIZE = MAPWIDTH * MAPHEIGHT;

    private static Rect box_ = new Rect();

    private static NSRMediaLibrary library_;

    private int senseValues_[];
    private int puddle_[];
    private MapEntry libEntries_[];
    private MapEntry shuffleEntries_[];
    private int maxMapEntry_;
    private int songsInBox_ = 0;

    private static int[] shuffleIndices_ = null;

    public static void setLibrary(NSRMediaLibrary library) {
        library_ = library;
    }


    public class MapEntry {
        private int start_;
        private int count_;

        public MapEntry() {
            this(-1, 0);
        }

        public MapEntry(int start, int count) {
            start_ = start;
            count_ = count;
        }

        public int addEntry() {
            return ++count_;
        }

        public int removeEntry() {
            if (count_ > 0)
                return count_--;

            return 0;
        }

        public void set(int start, int count) {
            start_ = start;
            count_ = count;
        }

        public void set(int start) {
            start_ = start;
        }

        public int getStart() {
            return start_;
        }

        public int getCount() {
            return count_;
        }
    }


    public MusicMap() {
        puddle_ = new int[MAPSIZE];
        senseValues_ = new int[MAPSIZE];
        libEntries_ = new MapEntry[MAPSIZE];
        shuffleEntries_ = new MapEntry[MAPSIZE];

        Log.d(TAG, "Initing MusicMap");
        for (int i = 0; i < MAPSIZE; i++) {
            int j = (i % 8) | (( i / 8) << 4);
            senseValues_[i] = j;
            puddle_[i] = 0;
            libEntries_[i] = new MapEntry(-1, 0);
            shuffleEntries_[i] = new MapEntry(-1, 0);
//            Log.d(TAG, "Value: " + Integer.toHexString(j));
        }
        Log.d(TAG, "Done Initing MusicMap");
    }


    /**
     * Populate the libEntries_ array that represents the distribution of
     * library songs to their sense value.
     * @return  false if the media library has not yet been set, otherwise true.
     */
    public boolean fillLibEntries() {
        if (library_ == null)
            return false;

//        library_.sortSongs();

        for (int i = 0; i < libEntries_.length; i++) {
            libEntries_[i].set(-1, 0);
        }

        maxMapEntry_ = 0;
        int lastIndex = -1;
        int skipped = 0;

        // Fill in libEntries_ array from media library
        for (int idx = 0; idx < library_.getSongCount(); idx++) {
            SongInfo song = library_.getSong(idx);
            int ii = song.getSenseIndex();
            if (ii < 0 || ii >= MAPSIZE) {
                MusicPlayerApp.log(TAG, "Sense index " + ii + " out of range for " + song.getFileName());
                skipped++;
                continue;
            }

            if (libEntries_[ii].getStart() == -1) {
                libEntries_[ii].set(idx, 1);
                lastIndex = ii;
                maxMapEntry_ = Math.max(maxMapEntry_, 1);
            } else if (lastIndex == ii) {
                //libEntries_[ii].set(idx);
                maxMapEntry_ = Math.max(maxMapEntry_, libEntries_[ii].addEntry());
            } else {
                MusicPlayerApp.log(TAG, "Map entries not contiguous in library at " + idx + ".");
                lastIndex = -1;
                skipped++;
            }
        }

        MusicPlayerApp.log(TAG, "Library has " + library_.getSongCount() + " songs, skipped "
                + skipped + ". Max dups = " + maxMapEntry_);

        return true;
    }

    public MapEntry[] fillShuffleEntries(int[] indices) {
        resetShuffle();
        for (int i : indices) {
            SongInfo song = library_.getSong(i);
            int ii = song.getSenseIndex();

            shuffleEntries_[ii].set(libEntries_[ii].getStart());
            shuffleEntries_[ii].addEntry();
        }

        return shuffleEntries_;
    }

    public Rect getBox() {
        return box_;
    }

    public MapEntry[] getLibEntries() {
        return libEntries_;
    }

    public int getMaxMapEntry() {
        return maxMapEntry_;
    }

    public MapEntry[] getShuffleEntries() {
        return shuffleEntries_;
    }

    /**
     * This returns a list of indices into the MusicLibrary's songs.
     * If one of the shuffle routines has filled a shuffle list, it is returned,
     * otherwise the MusicLibrary's total list of shuffled indices is returned.
     *
     * @return List of indices into MusicLibrary's songs, or an empty list if library
     *         not yet initialized.
     */
    public int[] getShuffledList() {
        if (shuffleIndices_ != null)
            return shuffleIndices_;

        if (library_ == null)
            return new int[0];

        return library_.getShuffledSongs(false);
    }

    public boolean isShuffled() {
//        boolean shuffled = !Arrays.asList(shuffled_).contains(-1);
        boolean shuffled = false;
        for (MapEntry entry : shuffleEntries_) {
            if (entry.getStart() != -1) {
                shuffled = true;
                break;
            }
        }
        MusicPlayerApp.log(TAG, "isShuffled = " + shuffled);
        return shuffled;
    }

    public void resetShuffle() {
        for (MapEntry entry : shuffleEntries_) {
            entry.set(-1, 0);
        }
    }


    public int[] boxShuffle(Rect box) {
        MusicPlayerApp.log(TAG, "START BOX SHUFFLE");

        // Sanitize and set box
        int left   = Math.min(MAPWIDTH,  Math.max(0, box.left));
        int top    = Math.min(MAPHEIGHT, Math.max(0, box.top));
        int right  = Math.min(MAPWIDTH,  Math.max(0, box.right));
        int bottom = Math.min(MAPHEIGHT, Math.max(0, box.bottom));
        box_.set(left, top, right, bottom);
        box_.sort();

        // size covers whole map, so just random shuffle
        if (box_.width() >= MAPWIDTH && box_.height() >= MAPHEIGHT) {
            MusicPlayerApp.log(TAG, "box is whole library -- go to random shuffle");
            return randomShuffle(library_.getSongCount());
        }

        // Find out total songs in the box
        fillLibEntries();
        songsInBox_ = 0;    // box_.width() * box_.height();
        int start = box_.left + box_.top * MAPWIDTH;
        for (int y = 0; y < box_.height(); y++) {
            for (int x = 0; x < box_.width(); x++) {
                int idx = start + x + y * MAPWIDTH;
                songsInBox_ += libEntries_[idx].getCount();
            }
        }

        MusicPlayerApp.log(TAG, " + BOX SHUFFLE AT: " + box_.toString()
                           + ", Lib songs=" + library_.getSongCount() + ", in box=" + songsInBox_);

        int[] ret = new int[songsInBox_];

        if (songsInBox_ > 0) {
            int cnt = 0;
            int[] shuffled = library_.getShuffledSongs(true);
            for (int idx : shuffled) {
                SongInfo song = library_.getSong(idx);
                int sense = song.getSenseIndex();
                int x = sense % MAPWIDTH;
                int y = sense / MAPWIDTH;
                if (box_.contains(x, y) && libEntries_[sense].removeEntry() > 0) {
                //    MusicPlayerApp.log(TAG, "song @ " + sense + " (" + x + "," + y + ") "
                //                            + song.getSenseString() + ": " + song.getTitle());
                    ret[cnt++] = idx;
                    if (cnt >= songsInBox_)
                        break;
                }
            }
        }

        fillShuffleEntries(ret);
        shuffleIndices_ = ret;
        return ret;
    }


    public int[] randomShuffle(int count) {
        MusicPlayerApp.log(TAG, "RANDOM SHUFFLE " + count);
        int[] arr = library_.getShuffledSongs(true);
        if (arr.length == 0)
            return arr;

        if (count > arr.length)
            count = arr.length;

        int[] ret = new int[count];
        System.arraycopy(arr, 0, ret, 0, count);

        fillShuffleEntries(ret);
        shuffleIndices_ = ret;
        return ret;
    }

}
/* ----------------
    public int[] puddleShuffle(int center) {
        Random rnd = new Random();

        for (int i = 0; i < shuffled_.length; i++)
            shuffled_[i] = -1;

        resetPuddle(3);
        //puddle_[center] = 0;

        Point pt1 = new Point(center % 8, center / 8);
        Point zpt = new Point();
        MusicPlayerApp.log(TAG, "START PUDDLE SHUFFLE AT: " + center + ", pt=" + pt1.toString());
        int ii = 0;
        while (ii < shuffled_.length) {
            int ival = rnd.nextInt(shuffled_.length);

            zpt.set(ival % 8, ival / 8);
            int dist = (zpt.x - pt1.x) * (zpt.x - pt1.x)
                    + (zpt.y - pt1.y) * (zpt.y - pt1.y);

            if (dist > 16 && ii < 12)
                continue;   // Cutoff -- too far away
            else if (dist > 4 && rnd.nextInt(dist) > 1) {
                //Log.d(TAG, ">>>> SPLASH <<<< " + ival + ", dist=" + dist);
                continue;   // Splash
            }

            shuffled_[ii++] = ival;
        }

        return shuffled_;
    }
------------ */
