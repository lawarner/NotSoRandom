package org.apps.notsorandom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Custom view that handles rendering and drawing of the Music Map.
 */
public class MusicMapView extends View implements View.OnTouchListener {
    private static final String TAG = "MusicMapView";

    private static final int colors_[] = {
        Color.RED, Color.BLUE, Color.GREEN,
        Color.CYAN, Color.MAGENTA, Color.YELLOW
    };
    private int currColor_ = 0;

    private static MusicMap musicMap_ = new MusicMap();
    // 3 + 3*8
    private static int randomPoint_ = 27;   // Mid point at first.
    private static PointF calc_ = new PointF(8f/473f, 8f/480f);
    private static RectF  boxDraw_ = new RectF(238f - 40f, 240 - 40f, 238f + 40f, 240 + 40f);

    private Bitmap bitmap_;
    private Paint  paint_;

    private PointF origin_ = new PointF();
    private float  radius_;


    // ------------------------------------------------------------------------
    private static float indexToPixel(int idx) {
        float fpix = Math.round((float) idx / calc_.x);
        return fpix;
    }
    /**
     * Get the order of song indices in the shufflelist.
     * Public interface to this view's data model.
     * @param reshuffle If true, the order of songs will be reshuffled.
     * @return list of songs indices. Can be used as parameter to getSongInfo()
     *         to retrieve the song information.
     */
    public static int[] getShuffleList(boolean reshuffle) {
        if (reshuffle || !musicMap_.isShuffled()) {
            if (randomPoint_ < 0)
                return musicMap_.randomShuffle();
            else {
                int[] mm = musicMap_.boxShuffle(randomPoint_, new Point(4,4));
                Rect rc = musicMap_.getBox();
                boxDraw_.set(indexToPixel(rc.left), indexToPixel(rc.top),
                             indexToPixel(rc.right), indexToPixel(rc.bottom));

                return mm;
            }
//                return musicMap_.puddleShuffle(randomPoint_);
        }

        return musicMap_.getShuffled();
    }

    // ------------------------------------------------------------------------

    public MusicMapView(Context c) {
        super(c);

        paint_ = new Paint(Paint.DITHER_FLAG);

        setOnTouchListener(this);
    }


    public boolean initLibrary() {
        return musicMap_.initLibrary();
    }

    public void setLibrary(MediaLibraryNSR library) {
        musicMap_.setLibrary(library);
    }

    public void setOrigin(float x, float y) {
        origin_.set(x, y);

        int cx = (int) Math.floor(x * calc_.x);
        int cy = (int) Math.floor(y * calc_.y);
        Log.d(TAG, "SET SHUFFLE ORIGIN TO (" + cx + "," + cy + ")");

        randomPoint_ = cx + cy * 8;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bitmap_ = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap_.eraseColor(Color.BLACK);
//        canvas_ = new Canvas(bitmap_);

        radius_ = (float) Math.max(6, w >> 4);
        calc_.x = 8f / w;
        calc_.y = 8f / h;

        setOrigin(w / 2, h / 2);
        getShuffleList(true);   // Reshuffle
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawBitmap(bitmap_, 0, 0, paint_);

        paint_.setColor(Color.BLUE);
        paint_.setStrokeWidth(0f);
        paint_.setStyle(Paint.Style.STROKE);
        canvas.drawRect(boxDraw_, paint_);
        paint_.setStyle(Paint.Style.FILL);
        canvas.drawCircle(origin_.x, origin_.y, 1f, paint_);

        paint_.setColor(colors_[currColor_]);
        int[] shuffles = getShuffleList(false);
        for (int i = 0; i < 12; i++) {
            float x = 24f + indexToPixel(shuffles[i] % 8);
            float y = 24f + indexToPixel(shuffles[i] / 8);
            canvas.drawCircle(x, y, radius_ / 2f, paint_);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getActionMasked();
        Log.d(TAG, "Action onTouch == " + action);
        if (action == MotionEvent.ACTION_DOWN ||
            action == MotionEvent.ACTION_POINTER_DOWN)
            return true;
        if (action != MotionEvent.ACTION_UP &&
            action != MotionEvent.ACTION_POINTER_UP)
            return false;
        if (motionEvent.getPointerCount() < 1)
            return false;

        calc_.x = 8f / getWidth();
        calc_.y = 8f / getHeight();

        setOrigin(motionEvent.getX(), motionEvent.getY());
        getShuffleList(true);   // Reshuffle

        /* ---------- color cycler
        currColor_++;
        currColor_ = currColor_ >= colors.length ? 0 : currColor_;
        bitmap_.eraseColor(colors[currColor_]);
        ------------- */
        invalidate();
/* -----
        Log.d(TAG, " WHOLE MAP AREA IS " + getWidth() + ", " + getHeight());
        for (int x = 0; x < getWidth(); x += (getWidth() / 8)) {
            for (int y = 0; y < getHeight(); y += (getHeight() / 8)) {
                int cx = (int) Math.floor(x * calc_.x);
                int cy = (int) Math.floor(y * calc_.y);
                int ival = cx + cy * 8;
                Log.d(TAG, "RC:  (" + x + "," + y + ") ==> " + ival + " : (" + cx + "," + cy + ")");
            }
        }
        for (int i = 0; i < 64; i++) {
            float x = Math.round((i % 8) / calc_.x);
            float y = Math.round((i / 8) / calc_.y);
            Log.d(TAG, "Cvt: " + i + " to (" + x + "," + y + ")");
        }
------ */
        return true;
    }

}
