package org.apps.notsorandom;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;

import java.util.Locale;


public class MusicPlayer extends FragmentActivity implements PlayerFragment.OnPlayerListener /*implements ActionBar.TabListener*/ {
    private static final String TAG = "MusicPlayer";

    private FragmentTabHost tabHost_;
    private PlayerFragment player_ = null;
    private QueueFragment  queue_  = null;
    private StatusFragment status_ = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_player);

        tabHost_ = (FragmentTabHost) findViewById(R.id.mytabHost);
        if (tabHost_ != null && savedInstanceState == null) {
            FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        tabHost_.setup(this, getSupportFragmentManager(), R.id.realTabContent);

        // Manually add the fragments as tabs
        String restr = getString(R.string.title_section1);
        tabHost_.addTab(tabHost_.newTabSpec(restr).setIndicator(restr), PlayerFragment.class, null);
        restr = getString(R.string.title_section2);
        tabHost_.addTab(tabHost_.newTabSpec(restr).setIndicator(restr), QueueFragment.class, null);
        restr = getString(R.string.title_section3);
        tabHost_.addTab(tabHost_.newTabSpec(restr).setIndicator(restr), StatusFragment.class, null);

            trans.commit();
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        Log.d(TAG, "onAttachFragment is called.");
        if (fragment instanceof PlayerFragment)
            player_ = (PlayerFragment) fragment;
        else if (fragment instanceof QueueFragment)
            queue_ = (QueueFragment) fragment;
        else if (fragment instanceof StatusFragment) {
            Log.d(TAG, "-attach status fragment: " + fragment);
            status_ = (StatusFragment) fragment;
        }
        blog("This message from onAttachFragment override.\n");
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
        blog("I wrote this message in my onResume override.\n");
        Log.d(TAG, "This Message means activity onResume is called and log happened.");
    }


    public void blog(String msg) {
        if (status_ != null && status_.isInLayout()) {
            Log.d(TAG, "write log: " + msg);
            status_.log(msg);
        }
//        StatusFragment sf = (StatusFragment) getSupportFragmentManager().findFragmentByTag(R.layout.fragment_music_status);
//        FrameLayout fl = (FrameLayout) findViewById(R.id.realTabContent);
/***
        StatusFragment sf = (StatusFragment) getSupportFragmentManager().findFragmentById(R.id.mytabHost)
                .getChildFragmentManager().findFragmentById(R.layout.fragment_music_status);

        Log.d(TAG, "blog got status fragment.");
        if (sf == null) Log.d(TAG, "StatusFragment is null\n");
        else {
            if (sf.isDetached()) Log.d(TAG, "StatusFragment is Detached\n");
            if (sf.isAdded()) Log.d(TAG, "StatusFragment is Added\n");
            if (sf.isInLayout()) Log.d(TAG, "StatusFragment is in Layout\n");
        }
        if (sf != null && !sf.isDetached()) {
            sf.log(msg);
        }
***/
    }

    @Override
    public void onNewSong(String song) {
        Log.d(TAG, "Got call from Player fragment onNewSong = " + song);
        queue_.addToQueue(song);

    }
}
