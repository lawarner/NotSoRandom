package org.apps.notsorandom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.Log;

/**
 * Subclass of MusicMapView that renders a 3D view.
 * This structure is temporary and common code will be migrated later
 * (when a third view is created).
 */
public class MusicMap3DView extends MusicMapView {
    private final static String TAG = MusicMap3DView.class.getSimpleName();

    private ShapeDrawable[] balls_;


    public MusicMap3DView(Context c) {
        super(c);

        balls_ = new ShapeDrawable[MusicMap.MAPSIZE];
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw need redraw=" + needRedraw_);
        if (getMapMode() != MapMode.ThreeDMode) {
            super.onDraw(canvas);
            return;
        }

        if (needRedraw_) {
            redrawMap();
            needRedraw_ = false;
        }

        canvas.drawBitmap(bitmap_, 0, 0, paint_);

        final int mapXYsize = MusicMap.MAPWIDTH * MusicMap.MAPHEIGHT;

        for (int depth = MusicMap.MAPDEPTH - 1; depth >= 0; depth--) {
            // for now, a pseudo 3d effect by scaling to depth
            canvas.save();
            // depth 0 - 7 = 1 - 0.5 scale = 1 - depth
            float dist = 1f - depth / (MusicMap.MAPDEPTH * 3f);
            canvas.scale(dist, dist);
            for (int ii = 0; ii < mapXYsize; ++ii) {
                int idx = depth * mapXYsize + ii;
                if (balls_[idx] != null) {
                    canvas.save();
                    PointF pt = indexToPixel(ii % 8, ii / 8);
                    canvas.translate(pt.x, pt.y);
                    //canvas.drawPaint(balls_[ii].getPaint());
                    Log.d(TAG, "Ball @ " + pt + " size=" + balls_[idx].getShape().getWidth() + ","
                                + balls_[idx].getShape().getHeight());
                    balls_[idx].draw(canvas);
                    canvas.restore();
                }
            }
            canvas.restore();
        }

    }

    private static int darken(int color) {
        int red = (color & 0x00ff0000) >> 16;
        int green = (color & 0x0000ff00) >> 8;
        int blue = color & 0x000000ff;
        int darkColor = 0xff000000 | red/4 << 16
                | green/4 << 8
                | blue/4;
        return darkColor;
    }

    @Override
    public void redrawMap() {
        Log.d(TAG, " redrawMap called");

        super.redrawMap();
        if (getMapMode() != MapMode.ThreeDMode) {
            return;
        }

        int currSenseIdx = -1;
        SongInfo song = listener_.getCurrSong();
        if (song != null) {
            currSenseIdx = song.getSenseIndex(MusicPlayerApp.getConfig());
            MusicPlayerApp.log(TAG, " Current sense index=" + currSenseIdx);
        }

        // The library map and shuffle entries
        MusicMap.MapEntry[] me = musicMap_.getLibEntries();
        MusicMap.MapEntry[] ms = musicMap_.getShuffleEntries();
        for (int ii = 0; ii < MusicMap.MAPSIZE; ii++) {
            int count = me[ii].getCount();
            if (count > 0) {
                float radius = calcRadius(count);
                Log.d(TAG, "-ball @" + ii + ", count=" + count + ", radius=" + radius);
                if (radius < 1.0f)
                    radius = 1.0f;
                OvalShape circle = new OvalShape();
                circle.resize(radius, radius);
                ShapeDrawable drawable = new ShapeDrawable(circle);
                Paint paint = drawable.getPaint();
                int color = Color.YELLOW;   // Color of library ball
                if (currSenseIdx == ii)
                    color = placeMode_ ? Color.CYAN : Color.GREEN;
                else if (ms[ii].getCount() > 0)
                    color = Color.RED;
                RadialGradient gradient = new RadialGradient(37.5f, radius/4,
                        radius, color, darken(color), Shader.TileMode.CLAMP);
                paint.setShader(gradient);
                drawable.getShape().resize(radius*2, radius*2);
                balls_[ii] = drawable;
            } else
                balls_[ii] = null;
        }

        super.invalidate();
/*
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
*/
    }
}
