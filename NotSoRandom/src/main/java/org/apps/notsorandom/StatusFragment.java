package org.apps.notsorandom;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by andy on 6/16/13.
 */
public class StatusFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_status, container, false);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        log("Status Fragment started.\n");
    }


    public void log(String msg) {
        if (getView() == null) {
            Log.d("MusicStatus", "getView is null");
            return;
        }
        TextView tv = (TextView) getView().findViewById(R.id.status_text);
        tv.append(msg);
    }

    public void clear() {
        set("");
    }

    public void set(String msg) {
        if (getView() == null) {
            Log.d("MusicStatus", "getView is null");
            return;
        }
        TextView tv = (TextView) getView().findViewById(R.id.status_text);
        tv.setText(msg);
    }

}
