package org.apps.notsorandom;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.PrintStreamPrinter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * This fragment contains the music queue of items playing.
 */
public class QueueFragment extends Fragment {
    private static final String TAG = "MusicQueue";

    private static ArrayAdapter<String> qArray_ = null;
    private static ArrayList<String> qArrList_ = new ArrayList<String>();
    private static ArrayList<SongInfo> qArrSongs_ = new ArrayList<SongInfo>();
    private static int currItem_;

    /**
     * Add a song to the queue
     *
     * @param song Info for song to be added.
     */
    public static void addToQueue(SongInfo song) {
        qArrSongs_.add(song);
        String str = song.getSenseString() + " " + song.getTitle() + " (" + song.getBaseFileName() + ")";
        if (qArray_ == null) {
            qArrList_.add(str);
        } else {
            qArray_.add(str);
            qArray_.notifyDataSetChanged();
        }
    }

    public static void clearQueue() {
        MusicPlayer.log(TAG, " clearQueue called");
        if (qArray_ == null)
            qArrList_.clear();
        else
            qArray_.clear();

        qArrSongs_.clear();

        currItem_ = -1;
    }

    public static ArrayList<SongInfo> getQueue() {
        return qArrSongs_;
    }


    public static SongInfo getItem(int idx) {
        if (idx < 0 || idx >= qArrSongs_.size())
            return null;

        currItem_ = idx;
        MusicPlayer.log(TAG, "+ getItem set currItem_=" + currItem_);

        return qArrSongs_.get(idx);
    }

    public static SongInfo getCurrItem() {
        MusicPlayer.log(TAG, "+getCurrItem=" + currItem_ + " size=" + qArrSongs_.size());
        if (currItem_ < 0 || currItem_ >= qArrSongs_.size())
            return null;

        return getItem(currItem_);
    }

    public static SongInfo getPrevItem(boolean last) {
        if (last)
            currItem_ = qArrSongs_.size();

        if (currItem_ <= 0)
            return null;

        return getItem(--currItem_);
    }

    public static SongInfo getNextItem(boolean first) {
        MusicPlayer.log(TAG, "+getNextItem=" + currItem_ + " size=" + qArrSongs_.size());
        if (first)
            currItem_ = -1;

        if ((currItem_ + 1) >= qArrSongs_.size())
            return null;

        return getItem(++currItem_);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_queue, container, false);
        ListView lv = (ListView) view.findViewById(R.id.queueView);

        if (qArray_ == null) {
            qArray_ = new ArrayAdapter<String>(getActivity(), R.layout.simplerow, qArrList_);
        }
        lv.setAdapter(qArray_);

        return view;
    }

}
