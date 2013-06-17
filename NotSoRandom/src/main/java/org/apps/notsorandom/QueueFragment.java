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
 * Created by andy on 6/16/13.
 */
public class QueueFragment extends Fragment {
    private ArrayAdapter<String> qArray_;
    private View view_;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_queue, container, false);
        ListView lv = (ListView) view.findViewById(R.id.queueView);
        ArrayList<String> arl = new ArrayList<String>();
        arl.add("Initial");

        qArray_ = new ArrayAdapter<String>(getActivity(), R.layout.simplerow, arl);
        lv.setAdapter(qArray_);

        view_ = view;
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        addToQueue("First time songs.");
    }

    public void addToQueue(String song) {
        qArray_.add(song);
        qArray_.notifyDataSetChanged();
//        ListView lv = (ListView) view_.findViewById(R.id.queueView);
//        lv.invalidateViews();
    }

}
