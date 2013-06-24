package org.apps.notsorandom;

/**
 * Created by andy on 6/23/13.
 */
public class TestMediaLibrary extends MediaLibraryNSR {
    public TestMediaLibrary() {
        super();
    }

    public int scanForMedia(String folder, boolean subFolders) {
        songs_.add(new SongInfo("All I Want To Do...", "/mnt/sdcard/test1.mp3", 0x0101));
        songs_.add(new SongInfo("Get On Outch Ere", "/mnt/sdcard/test2.mp3", 0x0202));
        songs_.add(new SongInfo("Iggy Pop? Uh, I forget :)", "/mnt/sdcard/test3.mp3", 0x0303));

        return 3;
    }

}
