package org.apps.notsorandom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Custom view that handles rendering and drawing of the Music Map.
 *
 * TODO incorporate labels, mode buttons
 */
public class MusicMapView extends View implements View.OnTouchListener {
    private static final String TAG = "MusicMapView";
/*
    private static final int colors_[] = {
        Color.RED, Color.BLUE, Color.GREEN,
        Color.CYAN, Color.MAGENTA, Color.YELLOW
    };
    private int currColor_ = 0;
*/
    protected static MusicMap musicMap_ = new MusicMap();

    private static float height_ = 500f;
    private static float width_  = 500f;
    private static PointF calc_ = new PointF(7.5f/500f, 7.5f/500f);
    private static RectF  boxDraw_ = new RectF();
    private static PointF center_ = new PointF();
    private static Rect newbox_ = new Rect();

    protected static boolean placeMode_ = false;

    protected Bitmap bitmap_;
    protected Paint  paint_;

    protected boolean needRedraw_ = true;

    private PointF start_  = new PointF();
    private PointF stop_   = new PointF();

    private boolean dragBox_ = false;


    protected MusicPlayer.OnPlayerListener listener_;

    ///////////  static methods  ///////////

    protected static PointF indexToPixel(int x, int y) {
        PointF fpt = new PointF(Math.round((float) x / calc_.x),
                                height_ - Math.round((float) y / calc_.y));
        return fpt;
    }

    private static RectF boxToPixelBox(Rect box) {
        float halfX = width_ / 15f;
        float halfY = height_ / 15f;
        float left = ((float) box.left / calc_.x) - halfX;
        float top  = height_ - ((float) box.bottom / calc_.y) + halfY;
        float right = ((float) box.right / calc_.x) - halfX;
        float bottom = height_ - ((float) box.top / calc_.y) + halfY;

        RectF rc = new RectF(left, top, right, bottom);
        return rc;
    }

    private static Rect pointsToRect(RectF scrn) {

        scrn.offset(width_ / 15f, -height_ / 15f);
        int left = (int) Math.floor(scrn.left * calc_.x);
        int top = (int) Math.floor((height_ - scrn.bottom) * calc_.y);
        int right = (int) Math.ceil(scrn.right * calc_.x);
        int bottom  = (int) Math.ceil((height_ - scrn.top) * calc_.y);

        return new Rect(left, top, right, bottom);
    }

    /**
     * Get the order of song indices in the shufflelist.
     * Public interface to this view's data model.
     * @param reshuffle If true, the order of songs will be reshuffled.
     * @return list of songs indices. Can be used as parameter to getSongInfo()
     *         to retrieve the song information.
     */
    public static SongInfo[] getShuffledList(boolean reshuffle) {
        if (reshuffle || !musicMap_.isShuffled()) {
            if (newbox_.isEmpty()) {
                boxDraw_.setEmpty();
                return musicMap_.randomShuffle(50);
            } else {
                SongInfo[] mm = musicMap_.boxShuffle(newbox_);
                Rect rc = musicMap_.getBox();
                MusicPlayerApp.log(TAG, "Box in: " + newbox_.toString() + "  Box out: " + rc.toString());
                boxDraw_.set(boxToPixelBox(rc));
                center_.set(boxDraw_.centerX(), boxDraw_.centerY());

                return mm;
            }
//                return musicMap_.puddleShuffle(randomPoint_);
        }

        return musicMap_.getShuffledList();
    }

    public static boolean getPlaceMode() {
        return placeMode_;
    }

    public static void setPlaceMode(boolean placeMode) {
        placeMode_ = placeMode;
    }

    // ------------------------------------------------------------------------

    public MusicMapView(Context c) {
        super(c);

        paint_ = new Paint(Paint.DITHER_FLAG);

        setOnTouchListener(this);
    }


    protected float calcRadius(int count) {
        float maxRadius = Math.max((float) bitmap_.getWidth() / 14f, 10f);
        int maxDups = Math.max(1, musicMap_.getMaxMapEntry());
        float pos = (float) maxDups - Math.max(0, Math.min(count, maxDups));

        // Make a curve from 0 to maxDups
        float posExp = (float) maxDups - (pos * pos / maxDups);
        if (posExp > maxDups) {
            MusicPlayerApp.log(TAG, "WARN Radius " + posExp + " bigger than " + maxDups);
        }

//        MusicPlayerApp.log(TAG, "==== radius: " + radius + " ;  scaled: " + radius * maxRadius / maxDups);
//        return Math.max(count, posExp * maxRadius / maxDups);
        float ret = posExp * maxRadius / maxDups;
        if (count > 0 && ret < 0.5f)
            ret = 0.5f;

        return ret;
    }

    public boolean initLibrary(MusicPlayerApp.LibraryCategory libCat) {
        return musicMap_.fillLibEntries(libCat);
    }

    public void setLibrary(NSRMediaLibrary library) {
        MusicMap.setLibrary(library);
    }

    public void setListener(MusicPlayer.OnPlayerListener listener) {
        listener_ = listener;
    }

    public void setStart(float x, float y) {
        start_.set(x, y);
    }

    public void setStop(float x, float y) {
        stop_.set(x, y);

        RectF rc = new RectF(start_.x, start_.y, stop_.x, stop_.y);
        rc.sort();
        newbox_.set(pointsToRect(rc));
        newbox_.sort();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bitmap_ = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap_.eraseColor(Color.BLACK);
//        canvas_ = new Canvas(bitmap_);

        calc_.x = 7.5f / w;
        calc_.y = 7.5f / h;
        height_ = h;
        width_ = w;
        MusicPlayerApp.log(TAG, "onSizeChanged to (" + w + "," + h + ") from ("
                                + oldw + "," + oldh + ") calc=(" + calc_.x + "," + calc_.y + ")");

        newbox_.setEmpty();
        boxDraw_.setEmpty();

        needRedraw_ = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawBitmap(bitmap_, 0, 0, paint_);
        paint_.setStrokeWidth(0f);

        if (!boxDraw_.isEmpty()) {
            paint_.setColor(Color.BLUE);
            paint_.setStyle(Paint.Style.STROKE);
            canvas.drawRect(boxDraw_, paint_);
            paint_.setStyle(Paint.Style.FILL);
            canvas.drawCircle(center_.x, center_.y, 1f, paint_);
        }

        final int mapXYsize = MusicMap.MAPWIDTH * MusicMap.MAPHEIGHT;

        // The library map
        paint_.setStyle(Paint.Style.STROKE);
        paint_.setColor(Color.YELLOW);
        MusicMap.MapEntry[] me = musicMap_.getLibEntries();
        for (int ii = 0; ii < mapXYsize; ii++) {
            int count = 0;      // count all layers
            for (int jj = ii; jj < me.length; jj += mapXYsize)
                count += me[jj].getCount();
            if (count > 0) {
                PointF pt = indexToPixel(ii % 8, ii / 8);
                float radius = calcRadius(count);
                if (radius < 0.51f)
                    canvas.drawPoint(pt.x, pt.y, paint_);
                else
                    canvas.drawCircle(pt.x, pt.y, radius, paint_);
            }
        }

        int currSenseIdx = -1;
        SongInfo song = listener_.getCurrSong();
        if (song != null) {
            currSenseIdx = song.getSenseIndex(MusicPlayerApp.getConfig()) % mapXYsize;
            MusicPlayerApp.log(TAG, " Current sense index=" + currSenseIdx);
        }

        // The shuffle map
        paint_.setStyle(Paint.Style.FILL);
        paint_.setColor(Color.RED);
        me = musicMap_.getShuffleEntries();
        for (int ii = 0; ii < mapXYsize; ii++) {
            int count = 0;      // count all layers
            for (int jj = ii; jj < me.length; jj += mapXYsize)
                count += me[jj].getCount();
            if (count > 0) {
                PointF pt = indexToPixel(ii % 8, ii / 8);
                float radius = calcRadius(count);
                if (ii == currSenseIdx)
                    paint_.setColor(placeMode_ ? Color.CYAN : Color.GREEN);
                if (radius < 0.51f)
                    radius = 1f;

                canvas.drawCircle(pt.x, pt.y, radius, paint_);
                if (ii == currSenseIdx) {
                    paint_.setColor(Color.RED);
                    currSenseIdx = -1;
                }
            }
        }
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getActionMasked();
        if (placeMode_) {
            SongInfo song = listener_.getCurrSong();
            if (song == null || motionEvent.getPointerCount() < 1)
                return false;
            if (action == MotionEvent.ACTION_DOWN ||
                action == MotionEvent.ACTION_POINTER_DOWN) {
                return true;
            } else
            if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_POINTER_UP) {
                Rect saveNewBox = new Rect(newbox_);
                setStart(motionEvent.getX(), motionEvent.getY());
                setStop(motionEvent.getX(), motionEvent.getY());
                int x = newbox_.centerX();
                int y = newbox_.centerY();
                newbox_.set(saveNewBox);

                MusicPlayerApp.log(TAG, "placeMode @ (" + x + "," + y + ")");
                if (x >= 0 && x < 8 && y >= 0 && y < 8) {
                    MediaLibraryBaseImpl lib = (MediaLibraryBaseImpl) listener_.getLibrary();
                    // Use SenseComponents for x y z
                    Config config = MusicPlayerApp.getConfig();
                    SenseComponent xcomp = config.getXcomponent();
                    SenseComponent ycomp = config.getYcomponent();
                    SenseComponent zcomp = config.getZcomponent();
                    int sense = (song.getSenseValue() & zcomp.getMask())
                              | (ycomp.getMaskedValue(y))
                              | (xcomp.getMaskedValue(x));
                    if (lib.updateSenseValue(song, sense)) {
                        redrawMap();
                        MusicQueue.redrawQueue();
                    }
                }
                return true;
            }
            return false;
        }

        // Selection mode
        if (action == MotionEvent.ACTION_DOWN ||
            action == MotionEvent.ACTION_POINTER_DOWN) {
            Log.d(TAG, "Action onTouch == ACTION_DOWN " + action);
            setStart(motionEvent.getX(), motionEvent.getY());
            dragBox_ = false;
            return true;
        }
        dragBox_ = false;
        if (action != MotionEvent.ACTION_UP &&
            action != MotionEvent.ACTION_POINTER_UP)
            return false;
        if (motionEvent.getPointerCount() < 1)
            return false;
        Log.d(TAG, "Action onTouch == ACTION_UP " + action);

        setStop(motionEvent.getX(), motionEvent.getY());
        SongInfo[] arr = getShuffledList(true);   // Reshuffle
        listener_.refreshQueue(arr.length);

        invalidate();

        return true;
    }

    public void redrawMap() {
        musicMap_.fillLibEntries(listener_.getLibCategory());
        musicMap_.fillShuffleEntries(listener_.getQueue());
        invalidate();
    }
/*        if (action == MotionEvent.ACTION_MOVE) {
            dragBox_ = true;
            stop_.set(motionEvent.getX(), motionEvent.getY());
        } else { */
/*        MusicPlayerApp.log(TAG, " WHOLE MAP AREA IS " + getWidth() + ", " + getHeight());
        for (int x = 0; x < getWidth(); x += (getWidth() / 8)) {
            for (int y = 0; y < getHeight(); y += (getHeight() / 8)) {
                int cx = (int) Math.floor(x * calc_.x);
                int cy = (int) Math.floor(y * calc_.y);
                int ival = cx + cy * 8;
                MusicPlayerApp.log(TAG, "RC:  (" + x + "," + y + ") ==> " + ival + " : (" + cx + "," + cy + ")");
            }
        }
        for (int i = 0; i < 64; i++) {
            float x = Math.round((i % 8) / calc_.x);
            float y = Math.round((i / 8) / calc_.y);
            MusicPlayerApp.log(TAG, "Cvt: " + i + " to (" + x + "," + y + ")");
        }
        for (int y = 0; y < getHeight(); y += (getHeight() / 8)) {
            for (int x = 0; x < getWidth(); x += (getWidth() / 8)) {
                int cx = (int) Math.floor(pt.x * calc_.x + 0.49);
                int cy = (int) Math.floor(pt.y * calc_.y + 0.49);
                MusicPlayerApp.log(TAG, "Convert Pixel:  (" + x + "," + y +
                                     ") ==> (" + pt.x + "," + cy + ")");
            }
        } */
}
