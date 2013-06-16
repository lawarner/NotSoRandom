package org.apps.notsorandom;

import java.util.Locale;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MusicPlayer extends FragmentActivity implements ActionBar.TabListener,
        MediaController.MediaPlayerControl {
    private static final String TAG = "MusicPlayer";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private View playerView_;
    private View queueView_;
    private static TextView status_;
    private MusicMapView musicMapView_;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_player);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

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
        if (playerView_ != null) {
            MediaController mc = (MediaController) playerView_.findViewById(R.id.controller);
            mc.setMediaPlayer(this);
            mc.show();
        }

        Log.d(TAG, "Going to show shuffle list of songs now.");
        View vw = mSectionsPagerAdapter.getView(0);
        musicMapView_ = (MusicMapView) vw.findViewById(R.id.music_map);
        if (musicMapView_ != null) {
            Log.d(TAG, "shuffle list not null");
            int songs[] = musicMapView_.getShuffleList();
            for (int song : songs) {
                status_.append(musicMapView_.getFilename(song) + "\n");
            }
        }
        else
            Log.d(TAG, " View to shuffle list is null");

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void start() {

    }

    @Override
    public void pause() {

    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public void seekTo(int i) {

    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return false;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private View view1_;
        private View view2_;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            DummySectionFragment fragment = new DummySectionFragment(position + 1);
            if (position == 0)
                view1_ = fragment.getView();
            else
                view2_ = fragment.getView();

            return fragment;
        }

        @Override
        public int getCount() {
            // total nr of pages.
            return 2;
        }

        public View getView(int idx) {
            if (idx == 0)
                return view1_;
            else
                return view2_;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public class DummySectionFragment extends Fragment {

        public DummySectionFragment(int page) {
            page_ = page;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            view_ = null;
            switch (page_) {
                case 1:
                    view_ = inflater.inflate(R.layout.fragment_music_player, container, false);
                    // rotate row_label
                    TextView tvrl = (TextView) view_.findViewById(R.id.row_label);
                    tvrl.setRotation(-90);
                    tvrl.setTranslationX(-40);
                    RelativeLayout rl = (RelativeLayout) view_.findViewById(R.id.player_layout);
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(480, 480);
                    lp.addRule(RelativeLayout.BELOW, R.id.column_label);
                    lp.addRule(RelativeLayout.RIGHT_OF, R.id.row_label);

                    musicMapView_ = new MusicMapView(rl.getContext());
                    rl.addView(musicMapView_, lp);
                    musicMapView_.setId(R.id.music_map);
                    musicMapView_.setTranslationX(-60);

                    playerView_ = view_;
                    break;
                case 2:
                    view_ = inflater.inflate(R.layout.fragment_music_status, container, false);
                    status_ = (TextView) view_.findViewById(R.id.section_label);
                    queueView_ = view_;
                    break;
                default:
                    Log.e(getTag(), "Error invalid tab page number " + page_);
            }
//            dummyTextView.setText(Integer.toString(page_));
            return view_;
        }

        public View getView() {
            return view_;
        }

        private int page_;
        private View view_;
    }

}
