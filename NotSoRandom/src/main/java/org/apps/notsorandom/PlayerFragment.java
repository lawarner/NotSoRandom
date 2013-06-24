package org.apps.notsorandom;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Implements the main player controls, as well as the MusicMapView.
 *
 * The parent activity must implement the PlayerFragment.OnPlayerListener interface.
 */
public class PlayerFragment extends Fragment implements MediaController.MediaPlayerControl,
                                                        MediaPlayer.OnCompletionListener,
                                                        MediaPlayer.OnPreparedListener {
    private static final String TAG = "MusicPlayerFragment";

    private static MusicMapView musicMapView_;

    // Used to call back the attached Activity
    private OnPlayerListener callback_ = null;

    private MediaController controller_ = null;
    private MediaPlayer player_;
    private View controlView_ = null;

    private Handler handler_ = new Handler();


    /**
     * The activity can call this to get a list of indices into the shuffled
     * list of songs.
     * @return array of song indices.
     */
    public static int[] getShuffleList() {
        return musicMapView_.getShuffleList(true);
    }

    /**
     * Interface that an Activity must implement in order to attach this Fragment.
     * This Fragment will call into the interface's methods.
     */
    public interface OnPlayerListener {
        /**
         * Retrieve the song info at index
         * @param ii song index
         * @return song info
         */
        public SongInfo getSongInfo(int ii);

        public MediaLibraryNSR getLibrary();

        /**
         * Called to retrieve the next song to play.
         * TODO: rewrite this to Iterator interface.
         * @param first If true will return the the first in list
         * @return Info of next song to play. The implementation should
         *     return null when no more songs are in the queue.
         */
        public SongInfo getNextSong(boolean first);

        /**
         * Called when a new song starts playing
         * @param song The info of the song.
         */
        public void onNewSong(SongInfo song);
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

        public void hide() {
            // Nope
        }

        public void hide_() {
            super.hide();   // OK, I'll hide
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach in PlayerFragment");    // On attach is called before onCreateView

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
        Log.d(TAG, "onCreateView in PlayerFragment");
        View view = inflater.inflate(R.layout.fragment_music_player, container, false);

        // Deal with the column labels around the music map
        TextView tvrl = (TextView) view.findViewById(R.id.row_label);
        tvrl.setRotation(-90);
        tvrl.setTranslationX(-40);
        tvrl.setTranslationY(48);
        RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.player_layout);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(480, 480);
        lp.addRule(RelativeLayout.BELOW, R.id.column_label);
        lp.addRule(RelativeLayout.RIGHT_OF, R.id.row_label);

        musicMapView_ = new MusicMapView(rl.getContext());
        rl.addView(musicMapView_, lp);
        musicMapView_.setId(R.id.music_map);
        musicMapView_.setTranslationX(-64);

        ArrayList<View> alv = new ArrayList<View>();
        alv.add(musicMapView_);
        view.addTouchables(alv);

        controlView_ = (View) view.findViewById(R.id.controlView);
        controller_  = new MyMediaController(getActivity());

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated in PlayerFragment");

        musicMapView_.setLibrary(callback_.getLibrary());
        musicMapView_.initLibrary();

        SongInfo song = callback_.getNextSong(true);
        controlView_ = (View) getView().findViewById(R.id.controlView);
        if (queueSong(song)) {
            TextView tv = (TextView) getView().findViewById(R.id.current_song);
            tv.setText("Next " + song.getTitle() + " ...");
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView in PlayerFragment");
        ((MyMediaController) controller_).hide_();
        if (player_.isPlaying())    // Maybe want to keep playing in background?
            player_.stop();
        player_.release();
    }

    public boolean playSong(SongInfo song) {
        if (queueSong(song) == false)
            return false;

        TextView tv = (TextView) getView().findViewById(R.id.current_song);
        tv.setText("Now playing " + song.getTitle() + " ...");

        callback_.onNewSong(song);

        start();

        return true;
    }

    public boolean queueSong(SongInfo song) {
        if (song == null) return false;

        player_ = new MediaPlayer();
        player_.setOnPreparedListener(this);
        player_.setOnCompletionListener(this);

//        controller_.setMediaPlayer(this);

        try {
            player_.setDataSource(song.getFileName());
            player_.prepare();
//            player_.start();
        } catch (Exception ex) {
            Log.e(TAG, "Exception: " + ex.getMessage());
        }

        return true;
    }

    public void stopSong() {
        pause();
        TextView tv = (TextView) getView().findViewById(R.id.current_song);
        tv.setText("Stopped.");
    }
/*
    public void showPlaylist() {
        int songs[] = musicMapView_.getShuffleList();
        for (int song : songs) {
//            status_.append(musicMapView_.getFilename(song) + "\n");
        }

    }
*/
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onPrepared in PlayerFragment");
        controlView_ = (View) getView().findViewById(R.id.controlView);
        controller_.setMediaPlayer(this);
        controller_.setAnchorView(controlView_);

        handler_.post(new Runnable() {
            public void run() {
                controller_.setEnabled(true);
                controller_.show(0);
            }
        });
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onCompletion in PlayerFragment");

        SongInfo song = callback_.getNextSong(false);
        playSong(song);
    }

    @Override
    public void start() {
        player_.start();
    }

    @Override
    public void pause() {
        player_.pause();
    }

    @Override
    public int getDuration() {
        return player_.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return player_.getCurrentPosition();
    }

    @Override
    public void seekTo(int i) {
        player_.seekTo(i);
    }

    @Override
    public boolean isPlaying() {
        return player_.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

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

}
