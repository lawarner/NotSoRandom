package org.apps.notsorandom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
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

    private final static int BallColor = Color.argb(0x81, 0xff, 0xff, 0x09); //argb

    private ShapeDrawable[] balls_;
    private ShapeDrawable[] ballsSelected_;


    public MusicMap3DView(Context c) {
        super(c);

        balls_ = new ShapeDrawable[MusicMap.MAPSIZE];
        ballsSelected_ = new ShapeDrawable[MusicMap.MAPSIZE];
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
            final float halfH = height_ / 16f;
            final float halfW = width_ / 16f;
            canvas.translate(depth * halfW * 0.5f, depth * halfH * 0.5f);
            // depth 0 - 7 = 1 - 0.5 scale = 1 - depth
            float dist = 1f - depth / (MusicMap.MAPDEPTH * 2.6f);
            canvas.scale(dist, dist);

            // Draw a grey box around the level boundaries
            if (depth == MusicMap.MAPDEPTH - 1) {
                paint_.setColor(Color.GRAY);
                paint_.setStyle(Paint.Style.STROKE);
                paint_.setStrokeWidth(2f);
                RectF pixBox = inBoxToPixelBox(new Rect(0, 0, 8, 8));
                canvas.drawRect(pixBox, paint_);
            }

            for (int ixy = 0; ixy < mapXYsize; ++ixy) {
                int idx = depth * mapXYsize + ixy;
                if (balls_[idx] != null || ballsSelected_[idx] != null) {
                    canvas.save();
                    PointF pt = indexToPixel(ixy % 8, ixy / 8);
                    canvas.translate(pt.x - halfW, pt.y - halfH);
                    //canvas.drawPaint(balls_[ixy].getPaint());
                    if (balls_[idx] != null) {
                        //Log.d(TAG, "Put lib ball at " + idx);
                        balls_[idx].draw(canvas);
                    }
                    if (ballsSelected_[idx] != null) {
                        //Log.d(TAG, "Put shuffle ball at " + idx);
                        ballsSelected_[idx].draw(canvas);
                    }
                    canvas.restore();
                }
            }
            canvas.restore();
        }

    }

    private static int darken(int color) {
        int alpha = color & 0xff000000;
        int red = (color & 0x00ff0000) >> 16;
        int green = (color & 0x0000ff00) >> 8;
        int blue = color & 0x000000ff;
        int darkColor = alpha | red/4 << 16
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
            balls_[ii] = createBall(me[ii].getCount(), BallColor);

            // The shuffle map
            int color = Color.RED;
            if (currSenseIdx == ii)
                color = placeMode_ ? Color.CYAN : Color.GREEN;
            ballsSelected_[ii] = createBall(ms[ii].getCount(), color);
        }

        super.invalidate();
    }

    private ShapeDrawable createBall(int count, int color) {
        if (count <= 0)
            return null;

        float radius = calcRadius(count);
//        Log.d(TAG, "-ball @" + ii + ", count=" + count + ", radius=" + radius);
        if (radius < 2.0f)
            radius = 2.0f;
        OvalShape circle = new OvalShape();
        circle.resize(radius, radius);
        ShapeDrawable drawable = new ShapeDrawable(circle);
        Paint paint = drawable.getPaint();

        RadialGradient gradient = new RadialGradient(radius/1.6f, radius/3,
                radius, color, darken(color), Shader.TileMode.CLAMP);
        paint.setShader(gradient);
        drawable.getShape().resize(radius*2, radius*2);

        return drawable;
    }
}
