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
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Subclass of MusicMapView that renders a 3D view.
 * This structure is temporary and common code will be migrated later
 * (when a third view is created).
 */
public class MusicMapGLView extends MusicMapView {
    private final static String TAG = MusicMapGLView.class.getSimpleName();

    private GLSurfaceView glView_;


    class Triangle {

        private FloatBuffer vertexBuffer;

        // number of coordinates per vertex in this array
        static final int COORDS_PER_VERTEX = 3;
        float triangleCoords[] = { // in counterclockwise order:
                0.0f,  0.622008459f, 0.0f,   // top
                -0.5f, -0.311004243f, 0.0f,   // bottom left
                0.5f, -0.311004243f, 0.0f    // bottom right
        };

        // Set color with red, green, blue and alpha (opacity) values
        float color_[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

        private static final int ATTR_MATWORLD  = 0;
        private static final int ATTR_VPOSITION = 1;
        private static final int ATTR_VCOLOR    = 2;
        private int attr_matworld;
        private int attr_vposition;
        private int attr_vcolor;

        private final String vertexShaderCode =
                "uniform mat4 matWorld; " +
                "attribute vec4 vPosition; " +
                        "void main() {" +
                        "  gl_Position = matWorld * vPosition;" +
                        "}\n";

        private final String fragmentShaderCode =
                "precision mediump float;" +
                        "uniform vec4 vColor; " +
                        "void main() {" +
                        "  gl_FragColor = vColor;" +
                        "}\n";

        private int glProgram_;


        public Triangle() {
            // initialize vertex byte buffer for shape coordinates
            // (number of coordinate values * 4 bytes per float)
            ByteBuffer bb = ByteBuffer.allocateDirect(triangleCoords.length * 4);

            // create a floating point buffer in native order from the ByteBuffer
            vertexBuffer = bb.order(ByteOrder.nativeOrder()).asFloatBuffer();
            // add the coordinates to the FloatBuffer
            vertexBuffer.put(triangleCoords);
            // set the buffer to read the first coordinate
            vertexBuffer.position(0);

            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
            int fragShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
            glProgram_ = GLES20.glCreateProgram();
            GLES20.glAttachShader(glProgram_, vertexShader);
            GLES20.glAttachShader(glProgram_, fragShader);

//            GLES20.glBindAttribLocation(glProgram_, ATTR_MATWORLD, "matWorld");
//            GLES20.glBindAttribLocation(glProgram_, ATTR_VPOSITION, "vPosition");
//            GLES20.glBindAttribLocation(glProgram_, ATTR_VCOLOR, "vColor");

            GLES20.glLinkProgram(glProgram_);

            attr_vposition = GLES20.glGetAttribLocation(glProgram_, "vPosition");
            attr_matworld = GLES20.glGetUniformLocation(glProgram_, "matWorld");
            attr_vcolor  = GLES20.glGetUniformLocation(glProgram_, "vColor");
        }

        public void draw(float[] worldMatrix) {
            // Add program to OpenGL ES environment
            GLES20.glUseProgram(glProgram_);

            // get handle to vertex shader's vPosition member
            //mPositionHandle = GLES20.glGetAttribLocation(glProgram_, "vPosition");

            // get handle to fragment shader's vColor member
            //mColorHandle = GLES20.glGetUniformLocation(glProgram_, "vColor");

            // Set color for drawing the triangle
            GLES20.glUniform4f(attr_vcolor, color_[0], color_[1], color_[2], color_[3]);

            GLES20.glUniformMatrix4fv(attr_matworld, 1, false, worldMatrix, 0);

            vertexBuffer.position(0);

            // Prepare the triangle coordinate data
            GLES20.glVertexAttribPointer(attr_vposition, COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, true,
                    COORDS_PER_VERTEX * 4, vertexBuffer);

            // Enable a handle to the triangle vertices
            GLES20.glEnableVertexAttribArray(attr_vposition);

            // Draw the triangle
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
//            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, COORDS_PER_VERTEX * 4);
            checkGlError("glDrawArrays");

            // Disable vertex array
            GLES20.glDisableVertexAttribArray(attr_vposition);
        }
    }

    private class MusicMapRenderer implements GLSurfaceView.Renderer {

        private static final int MAT4x4 = 16;

        private float[] matModel_ = new float[MAT4x4];
        private float[] matView_ = new float[MAT4x4];
        private float[] matProjection_ = new float[MAT4x4];
        private float[] matWorld_ = new float[MAT4x4];

        private Triangle triangle_;

        @Override
        public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
            GLES20.glClearColor(0f, 0f, 0.4f, 1f);

            triangle_ = new Triangle();

            Matrix.setLookAtM(matView_, 0, 0, 0, -5, 0f, 0f, 0f, 0f, 1f, 0f);
        }

        @Override
        public void onSurfaceChanged(GL10 gl10, int width, int height) {
            GLES20.glViewport(0, 0, width, height);

            float ratio = (float) width / height;
            Matrix.frustumM(matProjection_, 0, -ratio, ratio, -1, 1, 1, 12);
        }

        int frames = 0;
        int angle = 90;
        @Override
        public void onDrawFrame(GL10 gl10) {
            Log.d(TAG, "onDrawFrame");
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            if (frames++ > 10) {
                frames = 0;
                angle = angle == 359 ? 0 : angle++;
            }
            Matrix.setIdentityM(matModel_, 0);
            Matrix.rotateM(matModel_, 0, angle, 0f, 0f, 1f);
            Matrix.multiplyMM(matWorld_, 0, matView_, 0, matModel_, 0);
            Matrix.multiplyMM(matWorld_, 0, matProjection_, 0, matWorld_, 0);

            triangle_.draw(matWorld_);
        }
    }


    public static int loadShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        return shader;
    }


    public MusicMapGLView(Context context) {
        super(context);

        glView_ = new GLSurfaceView(this.getContext());
        glView_.setEGLContextClientVersion(2);
        glView_.setPreserveEGLContextOnPause(true);
        glView_.setRenderer(new MusicMapRenderer());
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        glView_.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        glView_.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR);

    }

    public GLSurfaceView getGlView() {
        return glView_;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        glView_.setMinimumHeight(h);
        glView_.setMinimumWidth(w);

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

        glView_.draw(canvas);
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

        super.invalidate();
    }

    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

}
