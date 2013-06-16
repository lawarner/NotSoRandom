package org.apps.notsorandom;

import android.util.Log;

import java.util.Random;

/**
 * Created by andy on 6/16/13.
 */
public class MusicMap {
    private static final String TAG = "MusicMap";

    private int senseValues_[];
    private int shuffled_[];

    public MusicMap() {
        senseValues_ = new int[64];
        shuffled_ = new int[64];
        for (int i = 0; i < 64; i++) {
            int j = (i % 8) | (( i / 8) << 4);
            senseValues_[i] = j;
            shuffled_[i] = -1;
//            Log.d(TAG, "Value: " + Integer.toHexString(j));
        }

    }

    public int getSize() {
        return 64;
    }

    public int getValue(int idx) {
        return senseValues_[idx];
    }

    public String fileName(int idx) {
        return "File" + Integer.toHexString(senseValues_[idx]);
    }

    public int[] shuffle(int dist) {
        Random rnd = new Random();

        for (int i = 0; i < 64; i++)
            shuffled_[i] = -1;

        int ii = 0;
        while (ii < 64) {
            int jj = rnd.nextInt(64);
            if (shuffled_[jj] == -1)
                shuffled_[jj] = senseValues_[ii++];

        }

        return shuffled_;
    }
}
