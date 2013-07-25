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
import java.util.Collection;
import java.util.List;

/**
 * Represents the library of songs.  Allows searching for songs and editing sense values for songs.
 */
public class MusicLibrary extends Fragment implements AdapterView.OnItemClickListener,
                                                    AdapterView.OnItemSelectedListener,
                                                    SeekBar.OnSeekBarChangeListener, SearchView.OnQueryTextListener {
    private static final String TAG = "MusicLibrary";

    private static SongsAdapter libArray_ = null;
    private static ArrayList<String> libArrList_ = new ArrayList<String>();

    private static MediaLibraryBaseImpl library_;
    private static int currItem_ = -1;
    private static SongInfo currSong_ = null;
    private static TextView currItemView_ = null;

    private static SenseComponent xSense_;
    private static SenseComponent ySense_;
    private static SenseComponent zSense_;

    private final Object lock_ = new Object();

    // Used to call back the Activity that attached us.
    protected MusicPlayer.OnPlayerListener callback_ = null;

    private static final int HILIGHT_COLOR = Color.argb(0xff, 130, 209, 236);


    protected class SongsAdapter extends ArrayAdapter<String> {
        protected Filter filter_;
        protected List<String> objects_;
        protected ArrayList<String> originals_;

        public SongsAdapter(Context context, int textViewId, List<String> objects) {
            super(context, textViewId, objects);
            objects_ = objects;
        }

        @Override
        public void addAll(Collection<? extends String> objects) {
            super.addAll(objects);
            objects_.addAll(objects);
            originals_.addAll(objects);
        }

        @Override
        public void clear() {
            if (originals_ != null)
                originals_.clear();
            else
                objects_.clear();
            super.clear();
        }

        @Override
        public Filter getFilter() {
            if (filter_ == null)
                filter_ = new SongsFilter();

            return filter_;
        }

        public List<String> getList() {
            return objects_;
        }

        public ArrayList<String> getOriginals() {
            if (originals_ == null) {
                synchronized (lock_) {
                    originals_ = new ArrayList<String>(objects_);
                }
            }
            return originals_;
        }

        protected class SongsFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence filterWords) {
                FilterResults results = new FilterResults();

                if (originals_ == null) {
                    synchronized (lock_) {
                        originals_ = new ArrayList<String>(objects_);
                    }
                }

                if (filterWords == null || filterWords.length() == 0) {
                    ArrayList<String> list;
                    synchronized (lock_) {
                        list = new ArrayList<String>(originals_);
                    }
                    results.values = list;
                    results.count = list.size();
                } else {
                    String[] words = filterWords.toString().toLowerCase().split("[ ,]+");

                    ArrayList<String> values;
                    synchronized (lock_) {
                        values = new ArrayList<String>(originals_);
                    }

                    final int count = values.size();
                    final ArrayList<String> newValues = new ArrayList<String>();

                    for (int i = 0; i < count; i++) {
                        final String value = values.get(i);
                        final String valueText = value.toLowerCase();

                        boolean matched = true;
                        for (String word : words) {
                            if (valueText.indexOf(word) == -1) {
                                matched = false;
                                break;
                            }
                        }

                        if (matched)
                            newValues.add(value);
                    }

                    results.values = newValues;
                    results.count = newValues.size();
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults results) {
                objects_.clear();
                objects_.addAll((List<String>) results.values);
                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }

            }
        }
    }

    private static void setupComponents() {
        // get the x,y,z values from config section of library.
        Config config = library_.getConfig(Config.DEFAULT_USER);
        if (config != null) {
            xSense_ = config.getXcomponent();
            ySense_ = config.getYcomponent();
            zSense_ = config.getZcomponent();
        } else {    // Load global defaults
            xSense_ = library_.getComponent(Config.DEFAULT_X_COMPONENT);
            ySense_ = library_.getComponent(Config.DEFAULT_Y_COMPONENT);
            zSense_ = library_.getComponent(Config.DEFAULT_Z_COMPONENT);
        }
    }

    public static void initDb(MediaLibraryBaseImpl library) {
        library_ = library;
        setupComponents();
        updateDb(true, MusicPlayerApp.LibraryCategory.ALL);
    }

    public static void updateDb(boolean clear, MusicPlayerApp.LibraryCategory libCat) {
        if (library_ == null)
            return;

        ArrayList<String> songs = new ArrayList<String>(library_.getSongCount());
        for (SongInfo song = library_.getFirstSong(); song != null; song = library_.getNextSong()) {
            int sense = song.getSenseValue();
            boolean gutter = (sense & xSense_.getMask()) == 0 || (sense & ySense_.getMask()) == 0;
            if (libCat == MusicPlayerApp.LibraryCategory.CATEGORIZED && gutter)
                continue;
            if (libCat == MusicPlayerApp.LibraryCategory.UNCATEGORIZED && !gutter)
                continue;

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

        SeekBar sb = (SeekBar) getView().findViewById(R.id.xSeekBar);
        sb.setProgress(xSense_.getComponentIndex(currSong_.getSenseValue()));
        sb = (SeekBar) getView().findViewById(R.id.ySeekBar);
        sb.setProgress(ySense_.getComponentIndex(currSong_.getSenseValue()));
        sb = (SeekBar) getView().findViewById(R.id.zSeekBar);
        sb.setProgress(zSense_.getComponentIndex(currSong_.getSenseValue()));
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
            libArray_ = new SongsAdapter(getActivity(), R.layout.simplerow, libArrList_);
        }
        lv.setAdapter(libArray_);
        lv.setOnItemClickListener(this);
        lv.setOnItemSelectedListener(this);
        lv.setTextFilterEnabled(true);

        TextView tv = (TextView) view.findViewById(R.id.xLabel);
        tv.setText(xSense_.getName());
        tv = (TextView) view.findViewById(R.id.yLabel);
        tv.setText(ySense_.getName());
        tv = (TextView) view.findViewById(R.id.zLabel);
        tv.setText(zSense_.getName());

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
        currItem_ = -1;
//        hilightItem(null, -1);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int item, long lParam) {
        MusicPlayerApp.log(TAG, "onItemClick Item " + item + ", param=" + lParam);

        String strItem = libArray_.getList().get(item);
        //String strItem = (String) adapterView.getItemAtPosition(item);
        int currItem = 0;
        ArrayList<String> origArr = libArray_.getOriginals();
        for ( ; currItem < origArr.size(); currItem++) {
            if (strItem.equals(origArr.get(currItem)))
                break;
        }
        MusicPlayerApp.log(TAG, " item is " + currItem + ", strItem=" + strItem);
        if (currItem >= origArr.size())
            return;

        currItem_ = currItem;
        currSong_ = library_.getSong(currItem_);
        MusicPlayerApp.log(TAG, "CurrSong is " + currSong_.getTitle());
//        if (hilightItem(view, currItem)) {
            adapterView.setSelection(currItem_);
            adapterView.setSelected(true);
            setSliders();
            callback_.setCurrSong(currSong_);
            callback_.playSong(currSong_);
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
                  | ySense_.getMaskedValue(yval)
                  | zSense_.getMaskedValue(zval);

        Log.d(TAG, "New sense value = " + Integer.toHexString(sense)
                 + ", item=" + currItem_ + ", song=" + currSong_.getTitle());
        library_.updateSenseValue(currSong_, sense);
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
