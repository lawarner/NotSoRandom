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

//TODO make sure this does not recalc the musicmap when reactivating the fragment view.
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

    private static PointF calc_ = new PointF(8f/473f, 8f/480f);
    private static RectF  boxDraw_ = new RectF();
    private static Rect newbox_ = new Rect();

    private Bitmap bitmap_;
    private Paint  paint_;

    private PointF center_ = new PointF();
    private PointF start_  = new PointF();
    private PointF stop_   = new PointF();
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
    private static MusicMap.MapEntry[] getShuffleList(boolean reshuffle) {
        if (reshuffle || !musicMap_.isShuffled()) {
            if (newbox_.isEmpty()) {
                boxDraw_.setEmpty();
                return musicMap_.randomShuffle(20);
            } else {
                MusicMap.MapEntry[] mm = musicMap_.boxShuffle(newbox_);
                Rect rc = musicMap_.getBox();
                boxDraw_.set(indexToPixel(rc.left), indexToPixel(rc.top),
                             indexToPixel(rc.right), indexToPixel(rc.bottom));

                return mm;
            }
//                return musicMap_.puddleShuffle(randomPoint_);
        }

        return musicMap_.getShuffleEntries();
    }

    // ------------------------------------------------------------------------

    public MusicMapView(Context c) {
        super(c);

        paint_ = new Paint(Paint.DITHER_FLAG);

        setOnTouchListener(this);
    }


    public boolean initLibrary() {
        return musicMap_.fillLibEntries();
    }

    public void setLibrary(NSRMediaLibrary library) {
        musicMap_.setLibrary(library);
    }

    public void setStart(float x, float y) {
        start_.set(x, y);
    }

    public void setStop(float x, float y) {
        stop_.set(x, y);

        RectF rc = new RectF(start_.x, start_.y, stop_.x, stop_.y);
        rc.sort();
        float fx = rc.centerX();
        float fy = rc.centerY();
        int cx = Math.round(fx * calc_.x);
        int cy = Math.round(fy * calc_.y);
        Log.d(TAG, "SET SHUFFLE ORIGIN TO (" + cx + "," + cy + ")");

        cx = (int) Math.floor(rc.left * calc_.x);
        cy = (int) Math.floor(rc.top  * calc_.y);
        int ix = (int) Math.ceil(rc.right * calc_.x);
        int iy = (int) Math.ceil(rc.bottom * calc_.y);
        newbox_.set(cx, cy, ix, iy);
        newbox_.sort();
        center_.set(fx, fy);     // Now, set the center
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

        //setStart(w / 4, h / 4);
        //setStop(w * 3 / 4, h * 3 / 4);
        newbox_.setEmpty();
        getShuffleList(true);   // Reshuffle
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

        // The library map
        paint_.setColor(Color.YELLOW);
        MusicMap.MapEntry[] me = musicMap_.getLibEntries();
        for (int ii = 0; ii < me.length; ii++) {
            int cnt = me[ii].getCount();
            if (cnt > 0) {
                float x = 24f + indexToPixel(ii % 8);
                float y = 24f + indexToPixel(ii / 8);
                canvas.drawCircle(x, y, 2f * cnt, paint_);
            }
        }

        // The shuffle map
        paint_.setColor(colors_[currColor_]);
        me = getShuffleList(false);
        for (int ii = 0; ii < me.length; ii++) {
            int cnt = me[ii].getCount();
            if (cnt > 0) {
                float x = 24f + indexToPixel(ii % 8);
                float y = 24f + indexToPixel(ii / 8);
                canvas.drawCircle(x, y, 2f * cnt, paint_);
            }
        }
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN ||
            action == MotionEvent.ACTION_POINTER_DOWN) {
            Log.d(TAG, "Action onTouch == ACTION_DOWN " + action);
            setStart(motionEvent.getX(), motionEvent.getY());
            return true;
        }
        if (action != MotionEvent.ACTION_UP &&
            action != MotionEvent.ACTION_POINTER_UP)
            return false;
        if (motionEvent.getPointerCount() < 1)
            return false;
        Log.d(TAG, "Action onTouch == ACTION_UP " + action);

        calc_.x = 8f / getWidth();
        calc_.y = 8f / getHeight();

        setStop(motionEvent.getX(), motionEvent.getY());
        getShuffleList(true);   // Reshuffle

        invalidate();

        return true;
    }

/*        MusicPlayer.log(TAG, " WHOLE MAP AREA IS " + getWidth() + ", " + getHeight());
        for (int x = 0; x < getWidth(); x += (getWidth() / 8)) {
            for (int y = 0; y < getHeight(); y += (getHeight() / 8)) {
                int cx = (int) Math.floor(x * calc_.x);
                int cy = (int) Math.floor(y * calc_.y);
                int ival = cx + cy * 8;
                MusicPlayer.log(TAG, "RC:  (" + x + "," + y + ") ==> " + ival + " : (" + cx + "," + cy + ")");
            }
        }
        for (int i = 0; i < 64; i++) {
            float x = Math.round((i % 8) / calc_.x);
            float y = Math.round((i / 8) / calc_.y);
            MusicPlayer.log(TAG, "Cvt: " + i + " to (" + x + "," + y + ")");
        }
        for (int y = 0; y < getHeight(); y += (getHeight() / 8)) {
            for (int x = 0; x < getWidth(); x += (getWidth() / 8)) {
                int cx = (int) Math.floor(pt.x * calc_.x + 0.49);
                int cy = (int) Math.floor(pt.y * calc_.y + 0.49);
                MusicPlayer.log(TAG, "Convert Pixel:  (" + x + "," + y +
                                     ") ==> (" + pt.x + "," + cy + ")");
            }
        } */

}
