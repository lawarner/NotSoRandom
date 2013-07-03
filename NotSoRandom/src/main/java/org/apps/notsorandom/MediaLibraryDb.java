package org.apps.notsorandom;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Implementation of media library using an RDBMS store
 */
public class MediaLibraryDb extends MediaLibraryBaseImpl {
    private static final String TAG = "MusicMediaLibraryDb";

    private String sdDir_ = null;

    private DbHandler handler_;

    public class DbHandler extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "mediaLibrary.db";
        private static final int    DATABASE_VERSION = 2;
        private static final String TABLE_SONGS = "songs";
        private static final String TABLE_COMPONENT = "component";
        private static final String TABLE_CONFIG = "config";

        // Database layout songs
        private static final String COL_ID    = "id";
        private static final String COL_TITLE = "title";
        private static final String COL_FILE  = "file";
        private static final String COL_SENSE = "sense";
        // Database layout components
        // COL_ID
        private static final String COL_NAME  = "name";
        private static final String COL_LABEL = "label";
        private static final String COL_MASK  = "mask";
        private static final String COL_SORT_ORDER = "sortorder";
        private static final String COL_DEFAULT_VALUE  = "defaultvalue";
        // Database layout config
        // COL_ID
        private static final String COL_USER     = "user";
        private static final String COL_ROOT     = "root";
        private static final String COL_X_COMP   = "xcomponent";
        private static final String COL_Y_COMP   = "ycomponent";
        private static final String COL_LASTSCAN = "lastscan";


        DbHandler(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);

            String root = Environment.getExternalStorageDirectory().getAbsolutePath();
            if (root == null || root.isEmpty())
                root = "/mnt/sdcard/";   // just a fallback value
            else if (!root.endsWith("/"))
                root += "/";
            sdDir_ = root;
        }

        public void addComponent(SenseComponent sc) {
            SQLiteDatabase db = getWritableDatabase();
            addComponent(db, sc);
            db.close(); // Close database connection
        }

        public void addComponent(SQLiteDatabase db, SenseComponent sc) {
            ContentValues values = new ContentValues();
            values.put(COL_NAME, sc.getName());
            values.put(COL_LABEL, sc.getLabel());
            values.put(COL_MASK, sc.getMask());
            values.put(COL_SORT_ORDER, sc.getSortOrder());
            values.put(COL_DEFAULT_VALUE, sc.getDefaultValue());
            try {   //TODO change to insertWithOnConflict()
                db.insertOrThrow(TABLE_COMPONENT, null, values);
            } catch (SQLiteConstraintException ce) {
                // Probably record already exists.
                Log.e(TAG, "Cannot store component in db: " + ce.getMessage());
            }
        }

        public void addConfig(Config config) {
            SQLiteDatabase db = getWritableDatabase();
            addConfig(db, config);
            db.close(); // Close database connection
        }

        public void addConfig(SQLiteDatabase db, Config config) {
            ContentValues values = new ContentValues();
            values.put(COL_USER, config.getUser());
            values.put(COL_ROOT, config.getRoot());
            values.put(COL_X_COMP, config.getXcomponent());
            values.put(COL_Y_COMP, config.getYcomponent_());
            values.put(COL_LASTSCAN, config.getLastScan());
            try {   //TODO change to insertWithOnConflict()
                db.insertOrThrow(TABLE_CONFIG, null, values);
            } catch (SQLiteConstraintException ce) {
                // Probably record already exists.
            }
        }

        public boolean addSong(SongInfo song) {
            SQLiteDatabase db = getWritableDatabase();
            boolean ret = addSong(db, song);
            db.close(); // Close database connection
            return ret;
        }

        public boolean addSong(SQLiteDatabase db, SongInfo song) {
            ContentValues values = new ContentValues();
            values.put(COL_TITLE, song.getTitle());

            String fileName = song.getFileName();
            if (sdDir_ != null && fileName.startsWith(sdDir_))
                fileName = fileName.substring(sdDir_.length());
            values.put(COL_FILE, fileName);

            values.put(COL_SENSE, song.getSenseValue());
            boolean ret = true;
            try {   //TODO change to insertWithOnConflict()
                db.insertOrThrow(TABLE_SONGS, null, values);
//                db.insert(TABLE_SONGS, null, values);
            } catch (SQLiteConstraintException ce) {
                // Probably record already exists.
                ret = false;
            }

            return ret;
        }


        public ArrayList<SongInfo> getAllSongs() {
            ArrayList<SongInfo> songs = new ArrayList<SongInfo>();
            String sql = "SELECT * FROM " + TABLE_SONGS;
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            if (cursor.moveToFirst()) {
                do {
                    String title = cursor.getString(1);
                    String file  = cursor.getString(2);
                    if (!file.startsWith("/"))
                        file = sdDir_ + file;
                    int sense = Integer.parseInt(cursor.getString(3));
                    SongInfo song = new SongInfo(title, file, sense);
                    songs.add(song);
                } while (cursor.moveToNext());
            }

            db.close();
            return songs;
        }

        public int getSongCount() {
            String sql = "SELECT * FROM " + TABLE_SONGS;
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery(sql, null);
            cursor.close();

            int ret = cursor.getCount();
            db.close();
            return ret;
        }

        public boolean cleanupDb() {
            boolean ret = true;

            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            String where = "substr(file,1,12) != \"/mnt/sdcard/\"";
            try {
                db.delete(TABLE_SONGS, where, null);
                db.execSQL("update songs set file=substr(file,13)");
            } catch (SQLiteConstraintException ce) {
                ret = false;
            }

            db.close();
            return ret;
        }

        public boolean updateSense(String file, int sense) {
            boolean ret = true;

            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            if (sdDir_ != null && file.startsWith(sdDir_))
                file = file.substring(sdDir_.length());
            String where = COL_FILE + " = \"" + file + "\"";
            values.put(COL_SENSE, sense);
            try {
                db.update(TABLE_SONGS, values, where, null);
            } catch (SQLiteConstraintException ce) {
                ret = false;
            }

            db.close();
            return ret;
        }

        public void initDatabase() {
            SQLiteDatabase db = getWritableDatabase();

            String root = Environment.getExternalStorageDirectory().getAbsolutePath();
            if (root == null || root.isEmpty())
                root = "/mnt/sdcard/";   // just a fallback value
            else if (!root.endsWith("/"))
                root += "/";

            Config config = new Config("0", root, "tempo", "roughness", 0);
            addConfig(db, config);

            SenseComponent component = new SenseComponent("tempo", "slow / fast", 0x00000f, 1, 4);
            addComponent(db, component);
            component = new SenseComponent("roughness", "soft / hard", 0x0000f0, 2, 3);
            addComponent(db, component);
            component = new SenseComponent("humor", "light / dark", 0x000f00, 3, 3);
            addComponent(db, component);
            component = new SenseComponent("taste", "sweet / sour", 0x00f000, 4, 4);
            addComponent(db, component);
            component = new SenseComponent("mood", "sad / happy", 0x0f0000, 5, 4);
            addComponent(db, component);
            component = new SenseComponent("depth", "shallow / deep", 0xf00000, 6, 4);
            addComponent(db, component);

            db.close(); // Close database connection
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = "CREATE TABLE " + TABLE_SONGS + " ("
                       + COL_ID + " INTEGER PRIMARY KEY,"
                       + COL_TITLE + " TEXT,"
                       + COL_FILE + " TEXT NOT NULL UNIQUE,"
                       + COL_SENSE + " INTEGER)";
//                       + "UNIQUE (" + COL_FILE + ") ON CONFLICT REPLACE)";

            db.execSQL(sql);

            // component and config tables, v2
            onUpgrade(db, 1, 2);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion == 1 && newVersion == 2) {
                String sql = "CREATE TABLE " + TABLE_COMPONENT + " ("
                        + COL_ID + " INTEGER PRIMARY KEY,"
                        + COL_NAME + " TEXT NOT NULL UNIQUE,"
                        + COL_LABEL + " TEXT,"
                        + COL_MASK + " INTEGER,"
                        + COL_SORT_ORDER + " INTEGER,"
                        + COL_DEFAULT_VALUE + " INTEGER)";
                db.execSQL(sql);

                sql = "CREATE TABLE " + TABLE_CONFIG + " ("
                        + COL_ID + " INTEGER PRIMARY KEY,"
                        + COL_USER + " TEXT NOT NULL UNIQUE,"
                        + COL_ROOT + " TEXT,"
                        + COL_X_COMP + " TEXT,"
                        + COL_Y_COMP + " TEXT,"
                        + COL_LASTSCAN + " DATETIME)";
                db.execSQL(sql);
            } else
                Log.e(TAG, "Unexpected database upgrade from " + oldVersion + " to " + newVersion);
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
    public void initialize() {
        handler_.initDatabase();

    }

    @Override
    public int scanForMedia(String folder, boolean subFolders) {

        if (folder.equals("SDCARD")) {
            folder = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        else if (folder.equals("CLEANUP")) {
            handler_.cleanupDb();
            return 0;
        }

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
            if (title == null || title.isEmpty()) {
                title = file1.getName();
                if (title.toLowerCase().endsWith(".mp3"))
                    title = title.substring(0, title.length() - 4);
                title += " (File)";
            }
            String genre = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
            if (genre == null || genre.isEmpty())
                genre = "(Unknown)";

            SongInfo song = new SongInfo(title, file1.getAbsolutePath(), 0x33);
//            songs_.add(song);
            boolean added = handler_.addSong(song);
            Log.d(TAG, "scanForMedia: " + file1.getAbsolutePath() + (added ? " added, " : " exists, ")
                           + title + "  " + genre);

        }
        mmr.release();

        return all.size();
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

    @Override
    public boolean updateSenseValue(int item, int sense) {
        SongInfo song = getSong(item);
        if (song == null)
            return false;

        song.setSense(sense);
        return updateSongInfo(item, song);
    }

    @Override
    public boolean updateSongInfo(int item, SongInfo song) {
        if (song == null)
            return false;

        MusicPlayerApp.log(TAG, "Updating item=" + item + ", song=" + song.getTitle());

        if (!handler_.updateSense(song.getFileName(), song.getSenseValue()))
            return false;

        // The base implementation updates the array backing store
        return super.updateSongInfo(item, song);
    }

}
