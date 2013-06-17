package org.apps.notsorandom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by andy on 6/15/13.
 */
public class MusicMapView extends View {
    private static final String TAG = "MusicMapView";

    private static final int colors[] = {
        Color.RED, Color.BLUE, Color.GREEN,
        Color.CYAN, Color.YELLOW, Color.BLACK
    };
    int currColor_ = 0;

    private Bitmap bitmap_;
    private Canvas canvas_;
    private Paint  paint_;

    private MusicMap musicMap_;


    public MusicMapView(Context c) {
        super(c);

        paint_ = new Paint(Paint.DITHER_FLAG);
        musicMap_ = new MusicMap();

        setOnTouchListener(touchListener_);

    }


    public int[] getShuffleList() {
        return musicMap_.shuffle(0);
    }

    public String getFilename(int ii) {
        return musicMap_.fileName(ii);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bitmap_ = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap_.eraseColor(Color.BLUE);

        canvas_ = new Canvas(bitmap_);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(0xFFAAAAAA);
//        bitmap_.eraseColor(Color.BLUE);

        canvas.drawBitmap(bitmap_, 0, 0, paint_);
    }

    private OnTouchListener touchListener_ = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            Log.d(TAG,"onTouchListener.onTouch");
            currColor_++;
            currColor_ = currColor_ >= colors.length ? 0 : currColor_;
            bitmap_.eraseColor(colors[currColor_]);
            invalidate();

            return true;
        }
    };
}
