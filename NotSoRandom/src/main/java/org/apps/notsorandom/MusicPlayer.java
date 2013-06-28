package org.apps.notsorandom;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.Menu;
import android.widget.TabHost;


public class MusicPlayer extends FragmentActivity
        implements PlayerFragment.OnPlayerListener {
    private static final String TAG = "MusicPlayer";

    private MediaLibraryBaseImpl library_;

    private FragmentTabHost tabHost_;


    public static void log(String tag, String msg) {
        Log.d(tag, msg);
        StatusFragment.log(msg);
    }

    public static void log(String msg) {
        log(TAG, msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_player);

        tabHost_ = (FragmentTabHost) findViewById(R.id.mytabHost);
        if (tabHost_ != null && savedInstanceState == null) {
            tabHost_.setId(R.id.mytabHost);
            tabHost_.setup(this, getSupportFragmentManager(), R.id.realTabContent);

            // Manually add the fragments as tabs
            String restr = getString(R.string.title_section1);
            tabHost_.addTab(tabHost_.newTabSpec(restr).setIndicator(restr), PlayerFragment.class, null);
            restr = getString(R.string.title_section2);
            tabHost_.addTab(tabHost_.newTabSpec(restr).setIndicator(restr), QueueFragment.class, null);
            restr = getString(R.string.title_section3);
            tabHost_.addTab(tabHost_.newTabSpec(restr).setIndicator(restr), StatusFragment.class, null);
//            tabHost_.addTab(tabHost_.newTabSpec("library").setIndicator(getString(R.string.title_section4)), LibraryFragment.class, null);
        }

        library_ = new MediaLibraryDb(this);  // = new MediaLibraryTest();
        library_.initialize();
        library_.scanForMedia(Environment.getExternalStorageDirectory().getAbsolutePath(), true);
//        library_.scanForMedia("RANDOM", true);
        library_.getAllSongs();
        library_.sortSongs();

        // Start with the whole library in the queue
        PlayerFragment.fillQueue(library_.getSongCount());
/*        QueueFragment.clearQueue();
        for (SongInfo song = library_.getFirstSong(); song != null; song = library_.getNextSong()) {
            QueueFragment.addToQueue(song);
        } */

        log("Player has initialized.\n");
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);

        Log.d(TAG, "onAttachFragment is called.");
//        if (fragment instanceof PlayerFragment)
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.music_player, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        log("MusicPlayer.onResume called.\n");
    }


    // -----------------------------------------------------------------
    @Override
    public SongInfo getSongInfo(int ii) {
        return QueueFragment.getItem(ii);
    }

    @Override
    public NSRMediaLibrary getLibrary() {
        return library_;
    }

    @Override
    public SongInfo getCurrSong() {
        SongInfo song = QueueFragment.getCurrItem();
        if (song != null)
            log("getCurrSong returns " + song.getTitle());

        return song;
    }

    @Override
    public SongInfo getNextSong(boolean first)
    {
        SongInfo song = QueueFragment.getNextItem(first);
        if (song != null)
            log("getNextSong returns " + song.getTitle());

        return song;
    }

    @Override
    public SongInfo getPrevSong(boolean last) {
        SongInfo song = QueueFragment.getPrevItem(last);
        if (song != null)
            log("getPrevSong returns " + song.getTitle());

        return song;
    }

    @Override
    public void onNewSong(SongInfo song) {
        log("Got call from Player fragment onNewSong = " + song.getTitle() + "\n");

    }
}
