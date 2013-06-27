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
 * Show status of player.
 */
public class StatusFragment extends Fragment {
    private static final String TAG = "MusicStatusFragment";

    private static String statusStr_ = "";

    public static void log(String msg) {
        statusStr_ += msg;
        if (!msg.endsWith("\n"))
            statusStr_ += "\n";
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
        Log.d(TAG, "====== onCreateView Status Fragment " + statusStr_.length());
        View view = inflater.inflate(R.layout.fragment_music_status, container, false);

        TextView tv = (TextView) view.findViewById(R.id.statusText);
        tv.setText(statusStr_, TextView.BufferType.EDITABLE);

        return view;
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "====== onDestroyView of StatusFragment called.");
        TextView tv = (TextView) getView().findViewById(R.id.statusText);
//        statusStr_ = tv.getText().toString();

        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "====== onResume Status Fragment");
//        _log("Status Fragment started.\n");

        TextView tv = (TextView) getView().findViewById(R.id.statusText);
        tv.setText(statusStr_, TextView.BufferType.EDITABLE);
    }


    /**
     * These versions of the logging methods that start with underscore "_" can only
     * be used when this fragment is instanciated.  Which means only internally to this
     * class.
     * @param msg The message to write to the status log.
     */
    private void _log(String msg) {
        if (getView() == null) {
            Log.d("MusicStatus", "getView is null");
            log(msg);
            return;
        }
        TextView tv = (TextView) getView().findViewById(R.id.statusText);
        tv.append(msg);
    }

    private void _clear() {
        _set("");
    }

    private void _set(String msg) {
        if (getView() != null) {
            TextView tv = (TextView) getView().findViewById(R.id.statusText);
            tv.setText(msg, TextView.BufferType.EDITABLE);
        }
        else
            set(msg);
    }

}
