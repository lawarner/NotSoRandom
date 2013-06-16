package org.apps.notsorandom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

/**
 * Created by andy on 6/15/13.
 */
public class MusicMapView extends View {
    private Bitmap bitmap_;
    private Canvas canvas_;

    public MusicMapView(Context c) {
        super(c);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bitmap_ = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas_ = new Canvas(bitmap_);
    }


}
