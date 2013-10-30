package org.apps.notsorandom;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;

import java.util.ArrayList;


public class MusicPlayerApp extends FragmentActivity
        implements MusicPlayer.OnPlayerListener, NSRMediaLibrary.OnLibraryChangedListener {
    private static final String TAG = "MusicPlayerApp";

    private static Config config_;

    private MediaLibraryBaseImpl library_;

    private FragmentTabHost tabHost_;

    private MusicPlayer playerFrag_ = null;

    public enum LibraryCategory {
        CATEGORIZED,
        UNCATEGORIZED,
        ALL
    }
    private LibraryCategory libCat_ = LibraryCategory.ALL;


    ///////////  static methods used throughout app. ///////////

    /**
     * Get the global configuration
     * @return Configuration for current user.
     */
    public static Config getConfig() {
        return config_;
    }

    public static void log(String tag, String msg) {
        Log.d(tag, msg);
        MusicSettings.log(msg);
    }

    public static void log(String msg) {
        log(TAG, msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.main_player);

        tabHost_ = (FragmentTabHost) findViewById(R.id.mytabHost);
        if (tabHost_ != null /*&& savedInstanceState == null*/) {
            tabHost_.setId(R.id.mytabHost);
            tabHost_.setup(this, getSupportFragmentManager(), R.id.realTabContent);

            // Manually add the fragments as tabs
            tabHost_.addTab(tabHost_.newTabSpec("player").setIndicator(getString(R.string.title_section1)), MusicPlayer.class, null);
            tabHost_.addTab(tabHost_.newTabSpec("queue").setIndicator(getString(R.string.title_section2)), MusicQueue.class, null);
            tabHost_.addTab(tabHost_.newTabSpec("library").setIndicator(getString(R.string.title_section3)), MusicLibrary.class, null);
            tabHost_.addTab(tabHost_.newTabSpec("settings").setIndicator(getString(R.string.title_section4)), MusicSettings.class, null);
        }

        library_ = new MediaLibraryDb(this);  // = new MediaLibraryTest();
        //log(TAG, "Attempt to restore db from sdcard");
        library_.scanForMedia("RESTORE", false);

        library_.initialize();
        //TODO if library empty, popup to run media scan
//        library_.scanForMedia(Environment.getExternalStorageDirectory().getAbsolutePath(), true);
        config_ = library_.getConfig(Config.DEFAULT_USER);
        if (config_ != null) {
            log("Got config, x=" + config_.getXcomponent().getName() + ", y=" + config_.getYcomponent().getName()
                    + ", z=" + config_.getZcomponent().getName());
        }

        library_.registerOnLibraryChanged(this);
        library_.getAllSongs();
        library_.sortSongs();

        MusicLibrary.initDb(library_);
        MusicQueue.setLibrary(library_);
        MusicMap.setLibrary(library_);

        if (savedInstanceState != null)
            log("-restored from saved state.");

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
    public void onBackPressed() {
        log("onBackPressed");
        //TODO check if playing
        MusicPlayer.shutdown();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.music_player, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MusicPlayer.shutdown();
    }

    @Override
    protected void onResume() {
        super.onResume();
        log("MusicPlayerApp.onResume called.\n");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

    }

    public void onRadioButtonClicked(View view) {
        log(TAG, "clicked radio button " + view.getId());
        switch (view.getId()) {
            case R.id.selectCategorized:
                libCat_ = LibraryCategory.CATEGORIZED;
                break;
            case R.id.selectUncategorized:
                libCat_ = LibraryCategory.UNCATEGORIZED;
                break;
            case R.id.selectAll:
                libCat_ = LibraryCategory.ALL;
                break;
            default:
                log(TAG, "Unknown radio button clicked.");
                return;
        }

        MusicLibrary.updateDb(true, libCat_);
        playerFrag_.initLibrary(libCat_);
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
    public LibraryCategory getLibCategory() {
        return libCat_;
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
    public void onNewSong(SongInfo song) {
        log("Got call from Player onNewSong = " + song.getTitle() + "\n");

    }

    @Override
    public SongInfo[] getQueue() {
        ArrayList<SongInfo> arr = MusicQueue.getQueue();
        SongInfo[] songs = new SongInfo[arr.size()];
        arr.toArray(songs);
        return songs;
    }

    @Override
    public int refreshQueue(int count) {
        int actual = MusicQueue.refreshQueue(count);

        if (playerFrag_ != null)
            playerFrag_.queueSong(getNextSong(true));

        return actual;
    }

    @Override
    public boolean playSong(SongInfo song) {
        if (playerFrag_ != null)
            return playerFrag_.playSong(song);

        return false;
    }

    @Override
    public void setCurrSong(SongInfo song) {
        MusicQueue.insertInQueue(song);
    }

    @Override
    public void libraryUpdated(NSRMediaLibrary library) {
        log(TAG, "libraryUpdated triggered, calling fragments with update.");

        MusicLibrary.updateDb(true, libCat_);

//        MusicQueue.refreshQueue(200);

        log(TAG, "libraryUpdated finished.");
    }
}
