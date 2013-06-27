package org.apps.notsorandom;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by andy on 6/23/13.
 */
public class MediaLibraryDb extends MediaLibraryBaseImpl {
    private static final String TAG = "MediaLibraryDb";

    private DbHandler handler_;

    public class DbHandler extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "mediaLibrary";
        private static final int    DATABASE_VERSION = 1;
        private static final String DB_TABLE_SONGS = "songs";

        // Database layout
        private static final String COL_ID    = "id";
        private static final String COL_TITLE = "title";
        private static final String COL_FILE  = "file";
        private static final String COL_SENSE = "sense";


        DbHandler(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void addSong(SongInfo song) {
            SQLiteDatabase db = getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COL_TITLE, song.getTitle());
            values.put(COL_FILE, song.getFileName());
            values.put(COL_SENSE, song.getSenseValue());

            db.insert(DB_TABLE_SONGS, null, values);
            db.close(); // Close database connection
        }


        public ArrayList<SongInfo> getAllSongs() {
            ArrayList<SongInfo> songs = new ArrayList<SongInfo>();
            String sql = "SELECT * FROM " + DB_TABLE_SONGS;
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            if (cursor.moveToFirst()) {
                do {
                    String title = cursor.getString(1);
                    String file  = cursor.getString(2);
                    int sense = Integer.parseInt(cursor.getString(3));
                    SongInfo song = new SongInfo(title, file, sense);
                    songs.add(song);
                } while (cursor.moveToNext());
            }

            return songs;
        }

        public int getSongCount() {
            String sql = "SELECT * FROM " + DB_TABLE_SONGS;
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery(sql, null);
            cursor.close();

            return cursor.getCount();
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = "CREATE TABLE " + DB_TABLE_SONGS + " ("
                       + COL_ID + " INTEGER PRIMARY KEY,"
                       + COL_TITLE + " TEXT,"
                       + COL_FILE + " TEXT NOT NULL UNIQUE,"
                       + COL_SENSE + " INTEGER)";
//                       + "UNIQUE (" + COL_FILE + ") ON CONFLICT REPLACE)";

            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

        }
    }

    public MediaLibraryDb(Context context) {
        super();
        handler_ = new DbHandler(context);
    }

    public ArrayList<SongInfo> getAllSongs() {
        songs_ = handler_.getAllSongs();
        return songs_;
    }

    @Override
    public int scanForMedia(String folder, boolean subFolders) {

        Collection<File> all = new ArrayList<File>();
        File file = new File(folder);
        Log.d(TAG, " - scanForMedia: " + file.getAbsolutePath());
        if (subFolders) {
            _scanRecursive(file, all);
        } else {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    all.add(child);
                }
            }
        }

        Log.d(TAG, "scanForMedia found " + all.size());

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        for (File file1 : all) {
            mmr.setDataSource(file1.getAbsolutePath());
            String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            if (title == null || title.isEmpty())
                title = "--> File " + file1.getName();
            Log.d(TAG, "scanForMedia add: " + file1.getAbsolutePath() + ", " + title);
            SongInfo song = new SongInfo(title, file1.getAbsolutePath(), 0x33);
//            songs_.add(song);
            handler_.addSong(song);
        }

        return 0;
    }

    private static void _scanRecursive(File file, Collection<File> all) {

        File[] children = file.listFiles();
        if (children == null) {
            if (file.getName().endsWith(".mp3") || file.getName().endsWith(".MP3"))
                all.add(file);
        } else {
            for (File child : children) {
                _scanRecursive(child, all);
            }
        }
    }
}
