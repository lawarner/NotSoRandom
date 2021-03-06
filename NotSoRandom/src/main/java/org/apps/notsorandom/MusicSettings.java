package org.apps.notsorandom;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * Show status of player.  Currently this fragment is only used as a debugging
 * window and could be hidden unless a debug option is enabled.
 */
public class MusicSettings extends Fragment implements View.OnLongClickListener {
    private static final String TAG = "MusicSettings";

    private static String statusStr_ = "";

    private static int queueSizeLimit_ = 1000;

    private static float gravity_ = 0.07f;

    private static QueueSizeListAdapter qsAdapter_ = null;

    private TextView statusView_ = null;

    private ExpandableListView queueSizeView_ = null;

    private boolean isScanning_ = false;

    private Handler handler_ = new Handler();

    // Used to call back the Activity that attached us.
    private MusicPlayer.OnPlayerListener callback_ = null;


    public static int getQueueSizeLimit() {
        if (qsAdapter_ != null)
            queueSizeLimit_ = qsAdapter_.getCurrent();

        return queueSizeLimit_;
    }

    public static float getGravity() {
        return gravity_;
    }

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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach in MusicSettings");    // On attach is called before onCreateView

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
        View view = inflater.inflate(R.layout.fragment_music_status, container, false);

        statusView_ = (TextView) view.findViewById(R.id.statusText);
        statusView_.setOnLongClickListener(this);

        queueSizeView_ = (ExpandableListView) view.findViewById(R.id.queueSizes);
        qsAdapter_ = new QueueSizeListAdapter(this.getActivity());
        queueSizeView_.setAdapter(qsAdapter_);
        queueSizeView_.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View view,
                                        int group, int child, long id) {
                qsAdapter_.setCurrent(child);
                queueSizeView_.setSelectedChild(group, child, false);
                queueSizeView_.collapseGroup(group);
                return true;
            }
        });

        TextView tv = (TextView) view.findViewById(R.id.gravity);
        tv.setText("" + gravity_);
        tv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String str = editable.toString();
                try {
                    gravity_ = Float.valueOf(str);
                } catch (NumberFormatException e) {
                    gravity_ = 0.07f;
                }

            }
        });

        Button but = (Button) view.findViewById(R.id.scanForMedia);
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setEnabled(false);
                isScanning_ = true;
                AsyncTask<View, Void, Void> at = new AsyncTask<View, Void, Void>() {

                    @Override
                    protected Void doInBackground(View... views) {
                        NSRMediaLibrary lib = callback_.getLibrary();
                        if (lib != null) {
                            //TODO scan in a worker thread
                            MusicPlayerApp.log(TAG, "Scanning SD card for new media...");
                            int orig = lib.getSongCount();
                            int nr = lib.scanForMedia("SDCARD", true);
                            MusicPlayerApp.log(TAG, "Scan complete.  Found " + nr + " new items.");
                            nr = lib.scanForMedia("SDCARDEXT", true);
                            MusicPlayerApp.log(TAG, "Scan ext complete.  Found " + nr + " new items.");
                            lib.getAllSongs();
                            lib.sortSongs();
                            MusicLibrary.updateDb(true, MusicPlayerApp.LibraryCategory.ALL);
                        }
                        isScanning_ = false;
                        final View view = views[0];
                        handler_.post(new Runnable() {
                            public void run() {
                                view.setEnabled(true);
                            }
                        });

                        return null;
                    }
                };

                at.execute(view);
            }
        });

        but = (Button) view.findViewById(R.id.showLog);
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                statusView_.setText(statusStr_, TextView.BufferType.EDITABLE);
            }
        });

        but = (Button) view.findViewById(R.id.backupDb);
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NSRMediaLibrary lib = callback_.getLibrary();
                if (lib != null) {
                    lib.scanForMedia("BACKUP", false);
                    MusicPlayerApp.log(TAG, "Db backed up to SD card");
                }
            }
        });

    /*          NSRMediaLibrary lib = callback_.getLibrary();
                if (lib != null) {
                    MusicPlayerApp.log(TAG, "Database cleanup...");
                    lib.scanForMedia("CLEANUP", true);
                    MusicPlayerApp.log(TAG, "Cleanup complete.");
    */
        // radio buttons for Library selection
        RadioButton rb = (RadioButton) view.findViewById(R.id.selectAll);
        rb.setChecked(true);

        return view;
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();
        statusView_ = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "====== onResume Status Fragment");

        statusView_ = (TextView) getView().findViewById(R.id.statusText);
//        statusView_.setText(statusStr_, TextView.BufferType.EDITABLE);
    }

    @Override
    public boolean onLongClick(View view) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        clear();
                        if (statusView_ != null)
                            statusStr_ = "";
                            statusView_.setText(statusStr_);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setMessage("Clear the status log?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

        return true;
    }

}
