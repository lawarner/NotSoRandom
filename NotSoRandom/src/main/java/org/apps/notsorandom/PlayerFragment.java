package org.apps.notsorandom;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by andy on 6/16/13.
 */
public class PlayerFragment extends Fragment implements MediaController.MediaPlayerControl {
    private MusicMapView musicMapView_;
    private OnPlayerListener callback_;

    private MediaController controller_;
    private MediaPlayer player_;


    public interface OnPlayerListener {
        public void onNewSong(String song);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callback_ = (OnPlayerListener) activity;
        } catch (ClassCastException ex) {
            throw new ClassCastException(activity.toString() + " must implement OnPlayerListener interface");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_player, container, false);

        TextView tvrl = (TextView) view.findViewById(R.id.row_label);
        tvrl.setRotation(-90);
        tvrl.setTranslationX(-40);
        RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.player_layout);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(480, 480);
        lp.addRule(RelativeLayout.BELOW, R.id.column_label);
        lp.addRule(RelativeLayout.RIGHT_OF, R.id.row_label);

        musicMapView_ = new MusicMapView(rl.getContext());
        rl.addView(musicMapView_, lp);
        musicMapView_.setId(R.id.music_map);
        musicMapView_.setTranslationX(-60);

        Button playBut = (Button) view.findViewById(R.id.playButton);
        playBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                songplay("/mnt/sdcard/test1.mp3");
            }
        });

        ArrayList<View> alv = new ArrayList<View>();
        alv.add(musicMapView_);
        view.addTouchables(alv);

        return view;
    }

    public boolean songplay(String song) {
        TextView tv = (TextView) getView().findViewById(R.id.current_song);
        tv.setText("Now playing " + song + " ...");

        callback_.onNewSong(song);

        try {
            player_ = new MediaPlayer();
            player_.setDataSource(song);
            player_.prepare();
            player_.start();
        } catch (Exception ex) {
            Log.e("MusicPlayer", "Exception: " + ex.getMessage());
        }
        MediaController mc = (MediaController) getView().findViewById(R.id.controller);
        mc.setMediaPlayer(this);

        mc.show();

        return true;
    }

    public void songstop() {
        TextView tv = (TextView) getView().findViewById(R.id.current_song);
        tv.setText("Stopped.");
    }

    public void showPlaylist() {
        int songs[] = musicMapView_.getShuffleList();
        for (int song : songs) {
//            status_.append(musicMapView_.getFilename(song) + "\n");
        }

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
}
