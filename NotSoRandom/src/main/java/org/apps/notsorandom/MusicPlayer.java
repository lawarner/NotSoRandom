package org.apps.notsorandom;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Implements the main player controls, as well as the MusicMapView.
 *
 * The parent activity must implement the MusicPlayer.OnPlayerListener interface.
 */
public class MusicPlayer extends Fragment implements MediaController.MediaPlayerControl,
                                                        MediaPlayer.OnCompletionListener,
                                                        MediaPlayer.OnPreparedListener, View.OnTouchListener {
    private static final String TAG = "MusicPlayer";

    private static MusicMapView musicMapView_ = null;

    private static MediaController controller_ = null;

    private static MediaPlayer player_ = null;
    private static SongInfo currSong_ = null;

    // Used to call back the Activity that attached us.
    private OnPlayerListener callback_ = null;

    // Place in layout to attach (floating) media controller
    private View controlView_ = null;

    private TextView title_ = null;
    private TextView trackCounter_ = null;
    private TextView artist_ = null;

    private Handler handler_ = new Handler();

    private boolean isFirstTime_;


    /**
     * Interface that an Activity must implement in order to attach this Fragment.
     * This Fragment will call into the interface's methods.
     */
    public interface OnPlayerListener {
        /**
         * Gives the current numeric position in the queue as well as the queue length
         * @param outta array of 2 int's: queue position / total in queue
         * @return True if the current position is known, otherwise false.
         */
        public boolean getCurrQueuePos(int[] outta);

        public SongInfo[] getQueue();

        /**
         * Called to retrieve the current song to play.
         * @return Info of next song to play. The implementation should
         *     return null when no more songs are in the queue.
         */
        public SongInfo getCurrSong();

        public NSRMediaLibrary getLibrary();

        public MusicPlayerApp.LibraryCategory getLibCategory();

        /**
         * Called to retrieve the next song to play.
         * TODO: rewrite this to Iterator interface.
         * @param first If true will return the the first in list
         * @return Info of next song to play. The implementation should
         *     return null when no more songs are in the queue.
         */
        public SongInfo getNextSong(boolean first);

        /**
         * Called to retrieve the previous song to play.
         * TODO: rewrite this to Iterator interface.
         * @param first If true will return the last song in list
         * @return Info of next song to play. The implementation should
         *     return null when no more songs are in the queue.
         */
        public SongInfo getPrevSong(boolean first);

        /**
         * Retrieve the song info at index
         * @param ii song index
         * @return song info
         */
        //public SongInfo getSongInfo(int ii);

        /**
         * Called when a new song starts playing
         * @param song The info of the song.
         */
        public void onNewSong(SongInfo song);

        /**
         * Call to get new items into the queue
         * @param count Number of items to place into queue
         * @return Number of items actually placed into queue.
         */
        public int refreshQueue(int count);

        /**
         * Immediately start playing song.  Used from the library when a song
         * is selected.
         */
        public boolean playSong(SongInfo song);

        public void setCurrSong(SongInfo song);
    }

    /**
     * A slightly subclassed version of MediaController that will stay on the UI.
     * MediaController seems to autohide regardless of any settings.
     */
    private class MyMediaController extends MediaController {
        public MyMediaController(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public MyMediaController(Context context, boolean useFastForward) {
            super(context, useFastForward);
        }

        public MyMediaController(Context context) {
            super(context, true);
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            int keyCode = event.getKeyCode();
            if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
                MusicPlayerApp.log(TAG, "dispatch key event from MyMediaController.");
                hide_();
                getActivity().finish();
                return false;
            }

            return super.dispatchKeyEvent(event);
        }

        public void hide() {
            // Nope
        }

        public void hide_() {
            super.hide();   // OK, I'll hide
        }
    }


    public static void shutdown() {
        if (player_ != null) {
            try {
                player_.reset();
                player_.release();
            } catch (Exception ex) {
                //
            }
        }
        if (controller_ != null) {
            controller_.setEnabled(false);
            controller_ = null;
        }
        player_ = null;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach in MusicPlayer");    // On attach is called before onCreateView

        try {
            callback_ = (OnPlayerListener) activity;
        } catch (ClassCastException ex) {
            throw new ClassCastException(activity.toString()
                                + " must implement OnPlayerListener interface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MusicPlayerApp.log(TAG, " onCreateView in MusicPlayer");
        View view = inflater.inflate(R.layout.fragment_music_player, container, false);

        Config config = MusicPlayerApp.getConfig();

        // Deal with the column labels around the music map
        TextView tvrl = (TextView) view.findViewById(R.id.column_label);
        tvrl.setText(config.getXcomponent().getLabel());
        tvrl.setOnTouchListener(this);
        tvrl = (TextView) view.findViewById(R.id.row_label);
        tvrl.setRotation(-90);
        tvrl.setTranslationX(-52);
        tvrl.setTranslationY(96);
        tvrl.setText(config.getYcomponent().getLabel());
        tvrl.setOnTouchListener(this);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(510, 510);
        lp.addRule(RelativeLayout.BELOW, R.id.column_label);
        lp.setMargins(110, 4, 2, 0);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.player_layout);
        musicMapView_ = new MusicMapView(rl.getContext());
        rl.addView(musicMapView_, lp);
        musicMapView_.setId(R.id.music_map);
        musicMapView_.setTranslationX(-64);
        musicMapView_.setListener(callback_);

        ArrayList<View> alv = new ArrayList<View>();
        alv.add(musicMapView_);
        view.addTouchables(alv);

        controlView_ = view.findViewById(R.id.controlView);
        if (controller_ == null) {
            isFirstTime_ = true;
            controller_  = new MyMediaController(getActivity());
            View.OnClickListener prev = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO if current seek position > 2 secs, go to beginning of current song
                    SongInfo song = callback_.getPrevSong(false);
                    playSong(song);
                }
            };
            View.OnClickListener next = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SongInfo song = callback_.getNextSong(false);
                    playSong(song);
                }
            };
            controller_.setPrevNextListeners(next, prev);
        } else {
            isFirstTime_ = false;
            MusicPlayerApp.log(TAG, " Set AnchorView with control " + controlView_);
            controller_.setAnchorView(controlView_);
        }
        controller_.setMediaPlayer(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated in MusicPlayer");

        controlView_ = getView().findViewById(R.id.controlView);
        title_ = (TextView) getView().findViewById(R.id.current_song);
        trackCounter_ = (TextView) getView().findViewById(R.id.trackCounter);
        artist_ = (TextView) getView().findViewById(R.id.artist);

        musicMapView_.setLibrary(callback_.getLibrary());

        if (player_ == null) {
            player_ = new MediaPlayer();
            player_.setOnPreparedListener(this);
            player_.setOnCompletionListener(this);
            MusicPlayerApp.log(TAG, "Created new MediaPlayer");
        }

        SongInfo song = null;
        if (isFirstTime_) {
            musicMapView_.initLibrary(callback_.getLibCategory());
            musicMapView_.getShuffledList(false);

            callback_.refreshQueue(30);
//            setTrackAndTitle(callback_.getCurrSong());
            song = callback_.getNextSong(true);
            MusicPlayerApp.log(TAG, " first time Song.");
        } else {
            song = callback_.getCurrSong();
            if (song == null)
                song = callback_.getNextSong(true);
            MusicPlayerApp.log(TAG, " resuming on Curr/Next Song.");
            setTrackAndTitle(song);
        }

        if (queueSong(song)) {
//            if (!isFirstTime_)
            if (getView() != null) {
                MusicPlayerApp.log(TAG, "Show controller. player is in view.");
                controller_.show(0);
            }
        }
        else
            MusicPlayerApp.log(TAG, "Unable to queue song " + song);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView in MusicPlayer");
        if (controller_ != null)
            ((MyMediaController) controller_).hide_();
/*
        if (player_ != null) {
            try {
                if (player_.isPlaying()) {    // Maybe want to keep playing in background?
                    player_.stop();
                    player_.release();
                }
            } catch (Exception ex) {
                //
            }
        }
*/
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN ||
            action == MotionEvent.ACTION_POINTER_DOWN) {
            return true;
        } else
        if (action == MotionEvent.ACTION_UP ||
            action == MotionEvent.ACTION_POINTER_UP) {
            SenseComponent xc;
            SenseComponent yc;
            SenseComponent zc;
            Config config = MusicPlayerApp.getConfig();

            TextView tvxc = (TextView) getView().findViewById(R.id.column_label);
            TextView tvyc = (TextView) getView().findViewById(R.id.row_label);
            if (tvxc == view) {
                MusicPlayerApp.log(TAG, "X component touched");
                xc = config.getZcomponent();
                yc = config.getYcomponent();
                zc = config.getXcomponent();
                tvxc.setText(xc.getLabel());
            } else
            if (tvyc == view) {
                MusicPlayerApp.log(TAG, "Y component touched");
                xc = config.getXcomponent();
                yc = config.getZcomponent();
                zc = config.getYcomponent();
                tvyc.setText(yc.getLabel());
            } else
                return false;

            config.setXYZcomponents(xc, yc, zc);
            musicMapView_.redrawMap();     // make the map redraw
            return true;
        }

        return false;
    }

    private void setTrackAndTitle(SongInfo song) {
        String track = "-/-";
        String title = "";
        String artist = "";

        if (song != null) {
            int[] qpos = new int[2];
            if (callback_ != null && callback_.getCurrQueuePos(qpos))
                track = "" + qpos[0] + "/" + qpos[1];

            title = song.getTitle();
            artist = song.getArtist();
            if (artist.length() < 32)
                artist += song.getAlbum();
            MusicPlayerApp.log(TAG, "setTrackAndTitle with " + track + " " + title);
        }

        if (trackCounter_ != null)
            trackCounter_.setText(track);
        if (title_ != null)
            title_.setText(title);
        if (artist_ != null)
            artist_.setText(artist);
    }


    public static void initLibrary(MusicPlayerApp.LibraryCategory libCat) {
        if (musicMapView_ != null) {
            musicMapView_.initLibrary(libCat);
            musicMapView_.getShuffledList(true);
        }
    }

    public boolean playSong(SongInfo song) {
        if (player_ != null) {
            try {
                if (player_.isPlaying()) {
                    Log.d(TAG, "stop player");
                    player_.stop();
                    Log.d(TAG, "reset player");
                    player_.reset();
                }
            } catch (Exception ex) {
                // at least I tried...
            }
        }

        if (!queueSong(song))
            return false;

        callback_.onNewSong(song);

        start();

        return true;
    }

    public boolean queueSong(SongInfo song) {
        if (song == null) return false;

        if (player_ == null) {
            MusicPlayerApp.log(TAG, "In queueSong, player_ is STILL null!");
            player_ = new MediaPlayer();
            player_.setOnPreparedListener(this);
            player_.setOnCompletionListener(this);
            controller_.setMediaPlayer(this);
        }

        if (currSong_ != null)
            currSong_.setLongForm(false);

        boolean ret = true;
        try {
            if (!isFirstTime_ && player_.isPlaying()) {
                MusicPlayerApp.log(TAG, "queueSong, current song is still playing.");
            } else {
                Log.d(TAG, "queueSong 1 " + song.getFileName());
                player_.reset();
                Log.d(TAG, "queueSong 2");
                player_.setDataSource(song.getFileName());
                player_.prepare();
                if (isVisible()) {   // only show the controller from the player fragment
                    controller_.setEnabled(true);
                    MusicPlayerApp.log(TAG, "controller enabled isVisible");
                }
                Log.d(TAG, "queueSong 3");
                setTrackAndTitle(song);
                currSong_ = song;
                currSong_.setLongForm(true);
                musicMapView_.invalidate();     // make the map redraw
            }
        } catch (IllegalStateException ise) {
            Log.e(TAG, "Illegal state in queueSong: " + ise);
            ret = false;
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "Illegal arg in queueSong: " + iae);
            ret = false;
        } catch (Exception ex) {
            Log.e(TAG, "Exception in queueSong: " + ex);
            ret = false;
        }
//        throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {

        return ret;
    }

    public void stopSong() {
        pause();
        title_.setText("Stopped.");
    }
/*
    public void showPlaylist() {
        int songs[] = musicMapView_.getShuffleList();
        for (int song : songs) {
//            status_.append(musicMapView_.getFilename(song) + "\n");
        }
    }*/

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return player_.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return player_.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return player_.isPlaying();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onCompletion in MusicPlayer");

        SongInfo song = callback_.getNextSong(false);
        playSong(song);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onPrepared in MusicPlayer");
        if (getView() == null) {
            MusicPlayerApp.log(TAG, "Player not in view, return.");
            return;
        }
        Log.d(TAG, "onPrepared in MusicPlayer - show player controls.");
        controller_.setMediaPlayer(this);
        controller_.setAnchorView(controlView_);

        handler_.getLooper().getThread().setPriority(Thread.NORM_PRIORITY - 1);
        handler_.post(new Runnable() {
            public void run() {
                controller_.setEnabled(true);
                controller_.show(0);
            }
        });
    }

    @Override
    public void pause() {
        player_.pause();
    }

    @Override
    public void seekTo(int i) {
        player_.seekTo(i);
    }

    @Override
    public void start() {
        player_.start();
    }

}
