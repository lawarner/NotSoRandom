package org.apps.notsorandom;

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

        if (folder.equals("RANDOM")) {
            Random rnd = new Random();

            for (int ii = 1; ii <= 200; ii++) {
                int idx = rnd.nextInt(64);
                int sv = SongInfo.indexToSense(idx);
                String name = "test" + ii;
                songs_.add(new SongInfo("Song " + name, "/mnt/sdcard/" + name + ".mp3", sv));
            }
        } else {
            songs_.add(new SongInfo("All I Want To Do...", "/mnt/sdcard/test1.mp3", 0x00));
            songs_.add(new SongInfo("Get On Outch Ere", "/mnt/sdcard/test2.mp3", 0x01));
            songs_.add(new SongInfo("Iggy Pop? Uh, I forget :)", "/mnt/sdcard/test3.mp3", 0x02));

            for (int ii = 3; ii < 64; ii++) {
                int sv = SongInfo.indexToSense(ii);
                String name = "test" + (ii + 1);
                songs_.add(new SongInfo("Song " + name, "/mnt/sdcard/" + name + ".mp3", sv));
            }
        }

        count += songs_.size();
        return count;
    }

}
