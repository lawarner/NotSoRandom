package org.apps.notsorandom;

import android.graphics.Point;
import android.util.Log;

import java.util.Arrays;
import java.util.Random;

/**
 *
 * TODO implement a puddleMap for randomizing values.
 */
public class MusicMap {
    private static final String TAG = "MusicMap";

    private int senseValues_[];
    private int shuffled_[];
    private int puddle_[];

    public static final int MAPWIDTH  = 8;   // matrix width
    public static final int MAPHEIGHT = 8;   // matrix height
    public static final int MAPSIZE = MAPWIDTH * MAPHEIGHT;

    public MusicMap() {
        puddle_ = new int[MAPSIZE];
        senseValues_ = new int[MAPSIZE];
        shuffled_ = new int[MAPSIZE];
        for (int i = 0; i < MAPSIZE; i++) {
            int j = (i % 8) | (( i / 8) << 4);
            senseValues_[i] = j;
            shuffled_[i] = -1;
            puddle_[i] = 0;
//            Log.d(TAG, "Value: " + Integer.toHexString(j));
        }
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

    public int[] puddleShuffle(int center) {
        Random rnd = new Random();

        for (int i = 0; i < shuffled_.length; i++)
            shuffled_[i] = -1;

        resetPuddle(3);
        //puddle_[center] = 0;

        Point pt1 = new Point(center % 8, center / 8);
        Point zpt = new Point();
        Log.d(TAG, "START SHUFFLE AT: " + center + ", pt=" + pt1.toString());
        int ii = 0;
        while (ii < shuffled_.length) {
            int ival = rnd.nextInt(63);

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
/*
        for (int i = 0; i < 64; i++) {
            Log.d(TAG, "LOG " + i + " " + (i * i / 63));
        }
*/
        return shuffled_;
    }
}
