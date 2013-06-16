package org.apps.notsorandom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by andy on 6/15/13.
 */
public class MusicMapView extends View {
    private Bitmap bitmap_;
    private Canvas canvas_;
    private Paint  paint_;

    private MusicMap musicMap_;


    public MusicMapView(Context c) {
        super(c);

        paint_ = new Paint(Paint.DITHER_FLAG);
        musicMap_ = new MusicMap();
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

}
