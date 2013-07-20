package org.apps.notsorandom;

import android.os.Environment;

import java.util.Random;

/**
 * Created by andy on 6/23/13.
 */
public class MediaLibraryTest extends MediaLibraryBaseImpl {
    public MediaLibraryTest() {
        super();
    }

    @Override
    public int scanForMedia(String folder, boolean subFolders) {
        int count = -songs_.size();
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (root == null || root.isEmpty())
            root = "/mnt/sdcard/";   // just a fallback value
        else if (!root.endsWith("/"))
            root += "/";

        if (folder.equals("RANDOM")) {
            Random rnd = new Random();

            for (int ii = 1; ii <= 200; ii++) {
                int idx = rnd.nextInt(64);
                int sv = SongInfo.indexToSense(idx);
                String name = "test" + ii;

                songs_.add(new SongInfo("Song " + name, root + name + ".mp3", sv, "artist"));
            }
        } else {
            songs_.add(new SongInfo("All I Want To Do...", root + "test1.mp3", 0x00, "artist"));
            songs_.add(new SongInfo("Get On Outch Ere", root + "test2.mp3", 0x01, "artist"));
            songs_.add(new SongInfo("Iggy Pop? Uh, I forget :)", root + "test3.mp3", 0x02, "artist"));

            for (int ii = 3; ii < 64; ii++) {
                int sv = SongInfo.indexToSense(ii);
                String name = "test" + (ii + 1);
                songs_.add(new SongInfo("Song " + name, root + name + ".mp3", sv, "artist"));
            }
        }

        count += songs_.size();
        return count;
    }

}
