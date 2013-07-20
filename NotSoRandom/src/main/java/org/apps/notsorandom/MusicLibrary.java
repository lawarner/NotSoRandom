package org.apps.notsorandom;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Represents the library and allows editing sense values for songs.
 */
public class MusicLibrary extends Fragment implements AdapterView.OnItemClickListener,
                                                    AdapterView.OnItemSelectedListener,
                                                    SeekBar.OnSeekBarChangeListener, SearchView.OnQueryTextListener {
    private static final String TAG = "MusicLibrary";

    private static ArrayAdapter<String> libArray_ = null;
    private static ArrayList<String> libArrList_ = new ArrayList<String>();

    private static MediaLibraryBaseImpl library_;
    private static int currItem_ = -1;
    private static TextView currItemView_ = null;

    private static SenseComponent xSense_;
    private static SenseComponent ySense_;
    private static SenseComponent zSense_;

    // Used to call back the Activity that attached us.
    private MusicPlayer.OnPlayerListener callback_ = null;

    private static final int HILIGHT_COLOR = Color.argb(0xff, 130, 209, 236);

    public static void initDb(MediaLibraryBaseImpl library) {
        library_ = library;

        // get the x,y,z values from config section of library.
        Config config = library_.getConfig(Config.DEFAULT_USER);
        if (config != null) {
            xSense_ = config.getXcomponent();
            ySense_ = config.getYcomponent();
            zSense_ = config.getZcomponent();
        } else {    // Load global defaults
            xSense_ = library_.getComponent("tempo");
            ySense_ = library_.getComponent("roughness");
            zSense_ = library_.getComponent("humor");
        }
        updateDb(true);
    }

    public static void updateDb(boolean clear) {
        if (library_ == null)
            return;

        ArrayList<String> songs = new ArrayList<String>(library_.getSongCount());
        for (SongInfo song = library_.getFirstSong(); song != null; song = library_.getNextSong()) {
            String title = song.getTitle();
            String artist = song.getArtist();
            if (artist == null)
                artist = "";
            String str = title + "\n"
                       + song.getSenseString() + "  " + artist;
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

    private boolean hilightItem(View view, int item) {
        if (item == currItem_)
            return true;
/*
        // unstar previous item if it was starred.
        if (currItemView_ != null) {
            currItemView_.setBackgroundColor(Color.WHITE);
        }

        currItem_ = item;
        currItemView_ = (view == null) ? null : (TextView) view;
        if (item < 0 || item > library_.getSongCount())
            return false;

        // star current item.
        if (currItemView_ != null) {
            currItemView_.setBackgroundColor(HILIGHT_COLOR);
        }
*/
        return true;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach in MusicLibrary");    // On attach is called before onCreateView

        try {
            callback_ = (MusicPlayer.OnPlayerListener) activity;
        } catch (ClassCastException ex) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnPlayerListener interface");
        }
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
        lv.setTextFilterEnabled(true);

        SeekBar sb = (SeekBar) view.findViewById(R.id.xSeekBar);
        sb.setOnSeekBarChangeListener(this);
        sb = (SeekBar) view.findViewById(R.id.ySeekBar);
        sb.setOnSeekBarChangeListener(this);
        sb = (SeekBar) view.findViewById(R.id.zSeekBar);
        sb.setOnSeekBarChangeListener(this);

        // Associate searchable configuration with the SearchView
        SearchView searchView = (SearchView) view.findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(false);
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setSuggestionsAdapter(null);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Hide the soft keyboard when switching views
        View svw = getView().findViewById(R.id.searchView);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(svw.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int item, long lParam) {
        MusicPlayerApp.log(TAG, "Selected Item " + item + ", param=" + lParam);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        MusicPlayerApp.log(TAG, "Nothing Selected.");
//        currItem_ = -1;
//        hilightItem(null, -1);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int item, long lParam) {
        MusicPlayerApp.log(TAG, "onItemClick Item " + item + ", param=" + lParam);

        String strItem = (String) adapterView.getItemAtPosition(item);
        int currItem = 0;
        for ( ; currItem < libArrList_.size(); currItem++) {
            if (strItem.equals(libArrList_.get(currItem)))
                break;
        }
        MusicPlayerApp.log(TAG, " item id=" + adapterView.getItemIdAtPosition(item) + " is " + currItem);
        if (currItem >= libArrList_.size())
            return;

        currItem_ = currItem;
//        if (hilightItem(view, currItem)) {
            adapterView.setSelection(currItem);
            adapterView.setSelected(true);
            setSliders();
            SongInfo song = library_.getSong(currItem_);
            if (song != null) {
                callback_.setCurrSong(song);
                callback_.playSong(song);
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

    @Override
    public boolean onQueryTextChange(String str) {
        Filter filter = libArray_.getFilter();
        filter.filter(str);
        libArray_.notifyDataSetChanged();
        /*
        ListView lv = (ListView) getView().findViewById(R.id.libraryView);
        if (str.isEmpty()) {
            lv.clearTextFilter();
        } else {
            MusicPlayerApp.log(TAG, "Filter on text: " + str + ".");
            lv.setFilterText(str);
        }
        */
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String str) {
        return false;
    }

}
