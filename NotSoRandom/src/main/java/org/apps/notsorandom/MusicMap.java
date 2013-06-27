package org.apps.notsorandom;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import java.util.Arrays;
import java.util.Random;

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

    private int senseValues_[];
    private int puddle_[];
    private MapEntry libEntries_[];
    private MapEntry shuffleEntries_[];
    private int totalLibEntries_ = 0;
    private int totalShuffleEntries_ = 0;
    private int maxMapEntry_;
    private int songsInBox_ = 0;

    private NSRMediaLibrary library_;


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
        totalLibEntries_ = 0;
        totalShuffleEntries_ = 0;

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

    public Rect getBox() {
        return box_;
    }

    public MapEntry[] getLibEntries() {
        return libEntries_;
    }

    public MapEntry[] getShuffleEntries() {
        return shuffleEntries_;
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
        return shuffled;
    }

    public void resetShuffle() {
        totalShuffleEntries_ = 0;
        for (int i = 0; i < shuffleEntries_.length; i++) {
            shuffleEntries_[i].set(-1, 0);
        }
/*        for (MapEntry entry : shuffleEntries_) {
            entry.set(-1, 0);
        } */
        MusicPlayer.log(TAG, "resetShuffle shuffled is " + isShuffled());
    }

    /**
     * Populate the libEntries_ array that represents the distribution of
     * library songs to their sense value.
     * @return  false if the media library has not yet been set, otherwise true.
     */
    public boolean fillLibEntries() {
        totalLibEntries_ = 0;
        if (library_ == null)
            return false;

        library_.sortSongs();

        for (int i = 0; i < MAPSIZE; i++) {
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
                MusicPlayer.log(TAG, "Sense index " + ii + " out of range for " + song.getFileName());
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
                MusicPlayer.log(TAG, "Map entries not contiguous in library at " + idx + ".");
                lastIndex = -1;
                skipped++;
            }
        }

        MusicPlayer.log(TAG, "Library has " + library_.getSongCount() + " songs, skipped "
                             + skipped + ". Max dups = " + maxMapEntry_);

        totalLibEntries_ = library_.getSongCount() - skipped;
        return true;
    }

    /**
     * This routine will clear the queue and fill it with the specified number of semirandom songs.
     * @param count number of songs to fill queue.
     */
    public void fillQueue(int count) {
        QueueFragment.clearQueue();
        if (library_ == null)
            return;

        Random rnd = new Random();

        if (count > totalShuffleEntries_)
            count = totalShuffleEntries_;

        MusicPlayer.log(TAG, " Fill Queue with " + count);
        while (count > 0) {
            int ii = rnd.nextInt(shuffleEntries_.length);
            MapEntry me = shuffleEntries_[ii];
            if (me.getStart() >= 0) {
                int il = me.getStart() + rnd.nextInt(me.getCount());

                SongInfo song = library_.getSong(il);
                if (song != null) {
                    QueueFragment.addToQueue(song);
                    count--;
                }
                else
                    MusicPlayer.log(TAG, "Got a null at library entry " + il);
            }
        }
    }


    public void setLibrary(NSRMediaLibrary library) {
        library_ = library;
    }

    public MapEntry[] boxShuffle(Rect box) {
        MusicPlayer.log(TAG, "START BOX SHUFFLE");

        // Sanitize and set box
        int left   = Math.min(MAPWIDTH,  Math.max(0, box.left));
        int top    = Math.min(MAPHEIGHT, Math.max(0, box.top));
        int right  = Math.min(MAPWIDTH,  Math.max(0, box.right));
        int bottom = Math.min(MAPHEIGHT, Math.max(0, box.bottom));
        box_.set(left, top, right, bottom);
        box_.sort();
        Point center = new Point(box_.centerX(), box_.centerY());

        // size covers whole map, so just random shuffle
        if (box_.width() >= MAPWIDTH && box_.height() >= MAPHEIGHT) {
            MusicPlayer.log(TAG, "box is whole library -- go to random shuffle");
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

        MusicPlayer.log(TAG, " + BOX SHUFFLE AT:  " + box_.toString() + ", center=" + center.toString());
        MusicPlayer.log(TAG, "Library size = " + library_.getSongCount() + ", in box=" + songsInBox_);

        // If box is whole library, then just copy libEntries to shuffleEntries
        if (songsInBox_ == library_.getSongCount()) {
            System.arraycopy(libEntries_, 0, shuffleEntries_, 0, shuffleEntries_.length);
            totalShuffleEntries_ = totalLibEntries_;
            fillQueue(Math.min(20, totalShuffleEntries_));
            MusicPlayer.log(TAG, "box filled with whole library ");
            return shuffleEntries_;
        }

        resetShuffle();

        Random rnd = new Random();

        int count = songsInBox_;
        int iters = 0;
        while (count > 0) {
            iters++;
            int xval = box_.left + rnd.nextInt(box_.width());
            int yval = box_.top  + rnd.nextInt(box_.height());
            int ival = xval + yval * MAPWIDTH;
            if (libEntries_[ival].getStart() == -1)
                continue;
            MapEntry entry = shuffleEntries_[ival];
            if (libEntries_[ival].getCount() > entry.getCount()) {
                entry.set(libEntries_[ival].getStart());
                entry.addEntry();
                count--;
            } else if (iters > 100000) {
                MusicPlayer.log(TAG, "Too many iterations");
                break;
            }
        }
        MusicPlayer.log(TAG, "+-+ box filled " + (songsInBox_ - count) + ", iterations=" + iters);
        totalShuffleEntries_ = songsInBox_ - count;

        fillQueue(Math.min(20, totalShuffleEntries_));
        return shuffleEntries_;
    }

    public MapEntry[] randomShuffle(int count) {
        MusicPlayer.log(TAG, "START RANDOM SHUFFLE");

        resetShuffle();
        fillLibEntries();

        Random rnd = new Random();

        int totalSongs = library_.getSongCount();
        if (count > totalSongs)
            count = totalSongs;

        MusicPlayer.log(TAG, " + RANDOM SHUFFLE of " + count);

        int i = 0;
        int iters = 0;      // prevent endless loop
        while (i < count ) {
            iters++;
            int idx = rnd.nextInt(totalSongs);
            SongInfo song = library_.getSong(idx);
            int ii = song.getSenseIndex();
            if (shuffleEntries_[ii].getCount() < libEntries_[ii].getCount()) {
                MusicPlayer.log(TAG, "Add to entry " + ii + ", count=" + shuffleEntries_[ii].getCount());
                shuffleEntries_[ii].set(libEntries_[ii].getStart());
                shuffleEntries_[ii].addEntry();
                i++;
            } else if (iters > 50) {
                MusicPlayer.log(TAG, "Too many iterations");
                break;
            } else
                MusicPlayer.log(TAG, " === count too big " + shuffleEntries_[ii].getCount() + " at " + ii);
        }
        MusicPlayer.log(TAG, "+-+ random songs " + i + " fill from total " + totalLibEntries_);
        totalShuffleEntries_ = i;

        fillQueue(Math.min(20, totalShuffleEntries_));
        return shuffleEntries_;
    }
}
/* ---- note can make a bell curve, like so:
        for (int i = 0; i < 64; i++) {
            Log.d(TAG, "LOG " + i + " " + (i * i / 63));
        }
*/
/* ----------------
    public int[] puddleShuffle(int center) {
        Random rnd = new Random();

        for (int i = 0; i < shuffled_.length; i++)
            shuffled_[i] = -1;

        resetPuddle(3);
        //puddle_[center] = 0;

        Point pt1 = new Point(center % 8, center / 8);
        Point zpt = new Point();
        MusicPlayer.log(TAG, "START PUDDLE SHUFFLE AT: " + center + ", pt=" + pt1.toString());
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
