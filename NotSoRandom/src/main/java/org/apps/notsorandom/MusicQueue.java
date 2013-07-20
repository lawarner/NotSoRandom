package org.apps.notsorandom;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * This fragment contains the music queue of items playing.
 */
public class MusicQueue extends Fragment {
    private static final String TAG = "MusicQueue";

    private static ArrayAdapter<String> qArray_ = null;
    private static ArrayList<String> qArrList_ = new ArrayList<String>();
    private static ArrayList<SongInfo> qArrSongs_ = new ArrayList<SongInfo>();
    private static int currItem_;

    private static NSRMediaLibrary library_ = null;

    /**
     * Add a song to the queue
     *
     * @param song Info for song to be added.
     */
    public static void addToQueue(SongInfo song) {
        qArrSongs_.add(song);
        String str = song.getSenseString() + "   " + song.getTitle();
        if (qArray_ == null) {
            qArrList_.add(str);
        } else {
            qArray_.add(str);
            qArray_.notifyDataSetChanged();
        }
    }

    public static int insertInQueue(SongInfo song) {
        if (currItem_ < 0 || currItem_ >= qArrSongs_.size()) {
            addToQueue(song);
            currItem_ = qArrSongs_.size() - 1;
        } else {
            qArrSongs_.add(currItem_, song);
        }

        return currItem_;
    }

    public static void clearQueue() {
        MusicPlayerApp.log(TAG, " clearQueue called");
        if (qArray_ == null)
            qArrList_.clear();
        else
            qArray_.clear();

        qArrSongs_.clear();

        currItem_ = -1;
    }

    public static void redrawQueue() {
        if (qArray_ != null)
            qArray_.notifyDataSetChanged();
    }

    /**
     * Put number of items from library random list into queue.
     * @param count Number of items to place in queue.
     * @return the actual number of items placed in queue.
     */
    public static int refreshQueue(int count) {
        MusicPlayerApp.log(TAG, " Fill Queue with " + count);

        clearQueue();
        if (library_ == null)
            return 0;

        //int[] shuffles = library_.getShuffledSongs(false);
        int[] shuffles = MusicMapView.getShuffledList(false);
        for (int i : shuffles) {
            SongInfo song = library_.getSong(i);
//            MusicPlayerApp.log(TAG, "Added to Queue: " + song.getSenseString() + ": " + song.getTitle());
            addToQueue(song);
            if (--count <= 0)
                break;
        }

        return qArrSongs_.size();
    }

    public static void setLibrary(NSRMediaLibrary library) {
        library_ = library;
    }

    public static ArrayList<SongInfo> getQueue() {
        return qArrSongs_;
    }

    public static SongInfo getCurrItem() {
        MusicPlayerApp.log(TAG, "+getCurrItem=" + currItem_ + " size=" + qArrSongs_.size());
        if (currItem_ < 0 || currItem_ >= qArrSongs_.size())
            return null;

        return qArrSongs_.get(currItem_);
    }

    public static boolean getCurrQueuePos(int[] outta) {
        if (currItem_ < 0)
            return false;

        outta[0] = currItem_ + 1;
        outta[1] = qArrSongs_.size();
        return true;
    }

    public static SongInfo getPrevItem(boolean last) {
        if (last)
            currItem_ = qArrSongs_.size();

        if (currItem_ <= 0)
            return null;

        return setItem(--currItem_);
    }

    public static SongInfo getNextItem(boolean first) {
        MusicPlayerApp.log(TAG, "+getNextItem=" + currItem_ + " size=" + qArrSongs_.size());
        if (first)
            currItem_ = -1;

        if ((currItem_ + 1) >= qArrSongs_.size())
            return null;

        return setItem(++currItem_);
    }

    /**
     * Set the current item to specified index in song queue.
     * @param idx Index of song to set as current.
     * @return Returns the song on success, otherwise null.
     */
    protected static SongInfo setItem(int idx) {
        if (idx < 0 || idx >= qArrSongs_.size())
            return null;

        currItem_ = idx;
        MusicPlayerApp.log(TAG, "+ getItem set currItem_=" + currItem_);

        return qArrSongs_.get(idx);
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
