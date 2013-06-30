package org.apps.notsorandom;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.Menu;


public class MusicPlayerApp extends FragmentActivity
        implements MusicPlayer.OnPlayerListener, NSRMediaLibrary.OnLibraryChangedListener {
    private static final String TAG = "MusicPlayerApp";

    private MediaLibraryBaseImpl library_;

    private FragmentTabHost tabHost_;

    private MusicPlayer playerFrag_ = null;


    public static void log(String tag, String msg) {
        Log.d(tag, msg);
        MusicStatus.log(msg);
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
            tabHost_.addTab(tabHost_.newTabSpec("player").setIndicator(getString(R.string.title_section1)), MusicPlayer.class, null);
            tabHost_.addTab(tabHost_.newTabSpec("queue").setIndicator(getString(R.string.title_section2)), MusicQueue.class, null);
            tabHost_.addTab(tabHost_.newTabSpec("library").setIndicator(getString(R.string.title_section3)), MusicLibrary.class, null);
            tabHost_.addTab(tabHost_.newTabSpec("status").setIndicator(getString(R.string.title_section4)), MusicStatus.class, null);
        }

        library_ = new MediaLibraryDb(this);  // = new MediaLibraryTest();
        library_.initialize();
        library_.scanForMedia(Environment.getExternalStorageDirectory().getAbsolutePath(), true);
//        library_.scanForMedia("RANDOM", true);
        library_.registerOnLibraryChanged(this);
        library_.getAllSongs();
        library_.sortSongs();

        MusicLibrary.initDb(library_);
        MusicQueue.setLibrary(library_);
        MusicMap.setLibrary(library_);

        // Start with 200 in the queue
//        MusicQueue.refreshQueue(200);

        log("Player has initialized.\n");
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);

        Log.d(TAG, "onAttachFragment is called.");
        if (fragment instanceof MusicPlayer)
            playerFrag_ = (MusicPlayer) fragment;
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
        log("MusicPlayerApp.onResume called.\n");
    }


    // -----------------------------------------------------------------
    @Override
    public boolean getCurrQueuePos(int[] outta) {
        return MusicQueue.getCurrQueuePos(outta);
    }

    @Override
    public SongInfo getCurrSong() {
        SongInfo song = MusicQueue.getCurrItem();
        if (song != null)
            log("getCurrSong returns " + song.getTitle());

        return song;
    }

    @Override
    public NSRMediaLibrary getLibrary() {
        return library_;
    }

    @Override
    public SongInfo getNextSong(boolean first)
    {
        SongInfo song = MusicQueue.getNextItem(first);
        if (song != null)
            log("getNextSong returns " + song.getTitle());

        return song;
    }

    @Override
    public SongInfo getPrevSong(boolean last) {
        SongInfo song = MusicQueue.getPrevItem(last);
        if (song != null)
            log("getPrevSong returns " + song.getTitle());

        return song;
    }

    @Override
    public SongInfo getSongInfo(int ii) {
        return MusicQueue.getItem(ii);
    }

    @Override
    public void onNewSong(SongInfo song) {
        log("Got call from Player fragment onNewSong = " + song.getTitle() + "\n");

    }

    @Override
    public int refreshQueue(int count) {
        int actual = MusicQueue.refreshQueue(count);

        if (playerFrag_ != null)
            playerFrag_.queueSong(getNextSong(true));

        return actual;
    }

    @Override
    public void libraryUpdated(NSRMediaLibrary library) {
        log(TAG, "libraryUpdated triggered, calling fragments with update.");

        MusicLibrary.updateDb(true);

        MusicQueue.refreshQueue(200);

        log(TAG, "libraryUpdated finished.");
    }
}
