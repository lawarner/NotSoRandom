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

    private int senseValues_[];
    private int shuffled_[];
    private int puddle_[];
    private MapEntry mapEntries_[];
    private Rect box_ = new Rect();

    private MediaLibraryNSR library_;


    private class MapEntry {
        private int start_;
        private int count_;

        public MapEntry() {
            this(-1, 0);
        }

        public MapEntry(int start, int count) {
            start_ = start;
            count_ = count;
        }

        public void addEntry() {
            count_++;
        }

        public void set(int start, int count) {
            start_ = start;
            count_ = count;
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
        shuffled_ = new int[MAPSIZE];
        mapEntries_ = new MapEntry[MAPSIZE];

        for (int i = 0; i < MAPSIZE; i++) {
            int j = (i % 8) | (( i / 8) << 4);
            senseValues_[i] = j;
            shuffled_[i] = -1;
            puddle_[i] = 0;
            mapEntries_[i].set(-1, 0);
//            Log.d(TAG, "Value: " + Integer.toHexString(j));
        }
    }

    public Rect getBox() {
        return box_;
    }

    public int getSize() {
        return MAPSIZE;
    }

    public int[] getShuffled() {
        return shuffled_;
    }

    public int[] getPuddle() {
        return puddle_;
    }

    public boolean isShuffled() {
        boolean shuffled = !Arrays.asList(shuffled_).contains(-1);
        return shuffled;
    }

    public void resetPuddle(int roof) {
        for (int i = 0; i < puddle_.length; i++) {
            puddle_[i] = roof;
        }
    }

    public String senseString(int idx) {
        if (senseValues_[idx] < 16)
            return "s0x0" + Integer.toHexString(senseValues_[idx]);
        else
            return "s0x" + Integer.toHexString(senseValues_[idx]);
    }

    public int senseValue(int idx) {
        return senseValues_[idx];
    }

    public boolean initLibrary() {
        if (library_ == null)
            return false;

        int lastIndex = -1;
        int mostDups  = -1;
//        for (SongInfo song = library_.getFirstSong(); song != null; song = library_.getNextSong()) {
        for (int idx = 0; idx < library_.getSongCount(); idx++) {
            SongInfo song = library_.getSong(idx);
            int ii = song.getSenseIndex();
            if (ii < 0 || ii >= MAPSIZE) {
                MusicPlayer.log(TAG, "Sense index " + ii + " out of range for " + song.getFileName());
                continue;
            }

            int cnt = mapEntries_[ii].getCount();
            if (cnt == -1) {
                mapEntries_[ii].set(idx, 1);
                lastIndex = ii;
                mostDups = Math.max(mostDups, 1);
            } else if (lastIndex == ii) {
                mapEntries_[ii].addEntry();
                mostDups = Math.max(mostDups, cnt + 1);
            } else {
                MusicPlayer.log(TAG, "Map entries not contiguous in library.");
                lastIndex = -1;
            }
        }

        MusicPlayer.log(TAG, "Song entries added from library, most dups = " + mostDups);

        return true;
    }

    public void setLibrary(MediaLibraryNSR library) {
        library_ = library;
    }

    public int[] boxShuffle(int center, Point size) {
        // size covers whole map, so just random shuffle
        if (size.x > MAPWIDTH && size.y > MAPHEIGHT)
            return randomShuffle();

        int halfX = Math.min(1, (size.x + 1) / 2);
        int halfY = Math.min(1, (size.y + 1) / 2);

        Random rnd = new Random();

        int[] used = new int[MAPSIZE];
        for (int i = 0; i < used.length; i++)
            used[i] = -1;

        Point pt = new Point(center % MAPWIDTH, center / MAPWIDTH);
        int left   = Math.min(MAPWIDTH - size.x,  Math.max(0, pt.x - halfX));
        int top    = Math.min(MAPHEIGHT - size.y, Math.max(0, pt.y - halfY));
        int boxOrigin = left + top * MAPWIDTH;
        int right  = Math.min(MAPWIDTH, left + size.x);
        int bottom = Math.min(MAPHEIGHT, top + size.y);
        box_ = new Rect(left, top, right, bottom);
        int boxCapacity = box_.width() * box_.height();
        if (boxCapacity >= MAPSIZE)
            return randomShuffle();

        MusicPlayer.log(TAG, "START BOX SHUFFLE AT:  " + center + ", pt=" + pt.toString()
                             + " size=" + size.toString());
        MusicPlayer.log(TAG, "Box is " + box_.toString() + " capacity=" + boxCapacity + " starting at " + boxOrigin);
        MusicPlayer.log(TAG, "Box does " + (box_.contains(3,3) ? "" : "not ") + "contain 3,3.");
        int ii = 0;
        int iters = 0;
        while (ii < boxCapacity) {
            iters++;
            int xval = box_.left + rnd.nextInt(box_.width());
            int yval = box_.top  + rnd.nextInt(box_.height());
            int ival = xval + yval * MAPWIDTH;
            if (used[ival] == -1) {
                shuffled_[ii] = ival;
                used[ival] = ii;
                ii++;
            }
        }
        MusicPlayer.log(TAG, "box filled " + ii + ", iterations=" + iters);
        while (ii < shuffled_.length) {
            iters++;
            int ival = rnd.nextInt(shuffled_.length);
            if (used[ival] == -1) {
                shuffled_[ii] = ival;
                used[ival] = ii;
                ii++;
            }
        }
        MusicPlayer.log(TAG, "map filled " + ii + ", iterations=" + iters);

        return shuffled_;
    }

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

    public int[] randomShuffle() {
        MusicPlayer.log(TAG, "START RANDOM SHUFFLE");

        Random rnd = new Random();

        for (int i = 0; i < shuffled_.length; i++)
            shuffled_[i] = -1;

        int ii = 0;
        while (ii < shuffled_.length) {
            double jj = rnd.nextDouble() * 64;
//            jj = (jj * jj / 63 + dist);
            int ji = (int) Math.round(jj) % shuffled_.length;
            if (shuffled_[ji] == -1) {
                shuffled_[ji] = ii++;
            }
        }
/* ---- note can make a bell curve, like so:
        for (int i = 0; i < 64; i++) {
            Log.d(TAG, "LOG " + i + " " + (i * i / 63));
        }
*/
        return shuffled_;
    }
}
