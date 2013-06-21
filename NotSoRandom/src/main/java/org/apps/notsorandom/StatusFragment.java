package org.apps.notsorandom;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by andy on 6/16/13.
 */
public class StatusFragment extends Fragment {
    private static final String TAG = "MusicStatusFragment";

    private static String statusStr_ = "";

    public static void log(String msg) {
        statusStr_ += msg;
    }

    public static void clear() {
        statusStr_ = "";
    }

    public static void set(String msg) {
        statusStr_ = msg;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        Log.d(TAG, "====== onCreateView Status Fragment");
        View view = inflater.inflate(R.layout.fragment_music_status, container, false);

        TextView tv = (TextView) view.findViewById(R.id.statusText);
        tv.setText(statusStr_);

        return view;
    }

    @Override
    public void onDestroyView() {
//        Log.d(TAG, "====== onDestroyView of StatusFragment called.");
        TextView tv = (TextView) getView().findViewById(R.id.statusText);
        statusStr_ = tv.getText().toString();

        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "====== onResume Status Fragment");
//        _log("Status Fragment started.\n");
    }


    /**
     * These versions of the logging methods that start with underscore "_" can only
     * be used when this fragment is instanciated.  Which means only internally to this
     * class.
     * @param msg The message to write to the status log.
     */
    public void _log(String msg) {
        if (getView() == null) {
            Log.d("MusicStatus", "getView is null");
            return;
        }
        TextView tv = (TextView) getView().findViewById(R.id.statusText);
        tv.append(msg);
    }

    public void _clear() {
        _set("");
    }

    public void _set(String msg) {
        if (getView() != null) {
            TextView tv = (TextView) getView().findViewById(R.id.statusText);
            tv.setText(msg);
        }
    }

}
