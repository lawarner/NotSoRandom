package org.apps.notsorandom;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Represents the library and allows editting sense values for songs.
 */
public class MusicLibrary extends Fragment implements AdapterView.OnItemClickListener,
                                                    AdapterView.OnItemSelectedListener,
                                                    SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "MusicLibrary";

    private static ArrayAdapter<String> libArray_ = null;
    private static ArrayList<String> libArrList_ = new ArrayList<String>();

    private static MediaLibraryBaseImpl library_;
    private static int currItem_ = -1;
    private static TextView currItemView_ = null;

    private static SenseComponent xSense_;
    private static SenseComponent ySense_;
    private static SenseComponent zSense_;


    public static void initDb(MediaLibraryBaseImpl library) {
        library_ = library;

        // TODO: get the x,y,z values from config section of library.
        xSense_ = new SenseComponent("tempo",     "slower / faster", 0x000f, 1,  3);
        ySense_ = new SenseComponent("roughness", "softer / harder", 0x00f0, 2,  4);
        zSense_ = new SenseComponent("humor",    "lighter / darker", 0x0f00, -1, 0);

        updateDb(true);
    }

    public static void updateDb(boolean clear) {
        if (library_ == null)
            return;

        ArrayList<String> songs = new ArrayList<String>(library_.getSongCount());
        for (SongInfo song = library_.getFirstSong(); song != null; song = library_.getNextSong()) {
            String str = song.getSenseString() + " " + song.getTitle();
            songs.add(str);
        }

        if (clear)
            libArrList_.clear();
        if (libArray_ == null)
            libArrList_.addAll(songs);
        else {
            if (clear)
                libArray_.clear();
            libArray_.addAll(songs);
            libArray_.notifyDataSetChanged();
        }
    }

    // -----------------------------------------------------------------------------

    private void setSliders() {
        if (currItem_ < 0 || currItem_ > library_.getSongCount())
            return;

        SongInfo song = library_.getSong(currItem_);
        if (song == null)
            return;

        SeekBar sb = (SeekBar) getView().findViewById(R.id.xSeekBar);
        sb.setProgress(xSense_.getComponentIndex(song.getSenseValue()));
        sb = (SeekBar) getView().findViewById(R.id.ySeekBar);
        sb.setProgress(ySense_.getComponentIndex(song.getSenseValue()));
        sb = (SeekBar) getView().findViewById(R.id.zSeekBar);
        sb.setProgress(zSense_.getComponentIndex(song.getSenseValue()));
    }

    private boolean starItem(View view, int item) {
        if (item == currItem_)
            return true;

        // unstar previous item if it was starred.
        if (currItemView_ != null) {
            String str = currItemView_.getText().toString();
            if (str.startsWith("* ")) {
                currItemView_.setText(str.substring(2));
            }
        }

        currItem_ = item;
        currItemView_ = (view == null) ? null : (TextView) view;
        if (item < 0 || item > library_.getSongCount())
            return false;

        // star current item.
        if (currItemView_ != null) {
            String str = currItemView_.getText().toString();
            if (!str.startsWith("* ")) {
                currItemView_.setText("* " + str);
            }
        }

        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_library, container, false);
        ListView lv = (ListView) view.findViewById(R.id.libraryView);

        if (libArray_ == null) {
            libArray_ = new ArrayAdapter<String>(getActivity(), R.layout.simplerow, libArrList_);
        }
        lv.setAdapter(libArray_);
        lv.setOnItemClickListener(this);
        lv.setOnItemSelectedListener(this);

        SeekBar sb = (SeekBar) view.findViewById(R.id.xSeekBar);
        sb.setOnSeekBarChangeListener(this);
        sb = (SeekBar) view.findViewById(R.id.ySeekBar);
        sb.setOnSeekBarChangeListener(this);
        sb = (SeekBar) view.findViewById(R.id.zSeekBar);
        sb.setOnSeekBarChangeListener(this);

        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int item, long lParam) {
        MusicPlayerApp.log(TAG, "Selected Item " + item + ", param=" + lParam);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        MusicPlayerApp.log(TAG, "Nothing Selected.");
//        currItem_ = -1;
        starItem(null, -1);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int item, long lParam) {
        MusicPlayerApp.log(TAG, "onItemClick Item " + item + ", param=" + lParam);
        if (starItem(view, item)) {
            adapterView.setSelection(item);
            setSliders();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int val, boolean byUser) {
        if (!byUser)
            return;

        if (currItem_ < 0 || currItem_ > library_.getSongCount())
            return;

        SeekBar sb = (SeekBar) getView().findViewById(R.id.xSeekBar);
        int xval = sb.getProgress() + 1;
        sb = (SeekBar) getView().findViewById(R.id.ySeekBar);
        int yval = sb.getProgress() + 1;
        sb = (SeekBar) getView().findViewById(R.id.zSeekBar);
        int zval = sb.getProgress() + 1;

        int sense = xSense_.getMaskedValue(xval)
                  | ySense_.getMaskedValue(yval);
      //TODO      | zSense_.getMaskedValue(zval);

        Log.d(TAG, "New sense value = " + Integer.toHexString(sense) + ", item=" + currItem_);
        library_.updateSenseValue(currItem_, sense);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
