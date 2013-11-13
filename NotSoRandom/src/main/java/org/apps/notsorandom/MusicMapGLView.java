package org.apps.notsorandom;

import android.content.Context;
import android.graphics.Canvas;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

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

    private static final int MAT4x4 = 16;
    private static final double DEG = Math.PI / 180;

    private float[] saveMatModel_ = new float[MAT4x4];
    private float[] matModel_ = new float[MAT4x4];
    private float[] matView_ = new float[MAT4x4];
    private float[] matProjection_ = new float[MAT4x4];
    private float[] matWorld_ = new float[MAT4x4];
    private float[] matNormal_ = new float[MAT4x4];
    private float[] eyePos_ = { 0f, 0f, -4f };
    private float[] lightPos_ = { 9f, 4f, -5f, 1 };
    private float[] lightColor_ = { 0.8f, 0.8f, 0.8f, 1 };

    private float[] matAmbient_ = { 1f, 0.2f, 0.2f, 1f };
    private float[] matDiffuse_ = { 0.6f, 0.5f, 0.5f, 1f };
    private float[] matSpecular_ = { 0.99f, 0.98f, 1f, 1f };

    private int attr_matnormal;
    private int attr_matworld;
    private int attr_vposition;
    //private int attr_vcolor;
    private int attr_eyepos;


    private class MyGLSurfaceView extends GLSurfaceView {
        private float startX_;
        private float startY_;

        public MyGLSurfaceView(Context context) {
            super(context);

        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            // MotionEvent reports input details from the touch screen
            // and other input controls. In this case, you are only
            // interested in events where the touch position changed.

            float x = e.getX();
            float y = e.getY();

            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX_ = x;
                    startY_ = y;
                    eyePos_[0] = 0f;
                    eyePos_[1] = 0f;
                    break;
                case MotionEvent.ACTION_MOVE:

                    float dx = (x - startX_) / 20f;
                    float dy = (y - startY_) / 20f;
                    eyePos_[0] = dx;
                    eyePos_[1] = dy;

                    requestRender();
            }

            return true;
        }
    }

    class Sphere {
        // number of coordinates per vertex in this array
        static private final int COORDS_PER_VERTEX = 3;
        static private final int BYTES_PER_FLOAT = 4;

        private FloatBuffer vertexBuffer_;

        private int points_;
        private boolean filled_;

        public Sphere(int segments, int slices, boolean filled) {
            filled_ = filled;
            points_ = setup(segments, slices);
        }

        private int setup(int segments, int slices) {
            int points = ((segments + 1) * slices);
            if (filled_) points += 2;

            int sz = points * COORDS_PER_VERTEX;

            // initialize vertex byte buffer for shape coordinates
            // (number of coordinate values * 4 bytes per float)
            ByteBuffer bb = ByteBuffer.allocateDirect(sz * BYTES_PER_FLOAT);

            // create a floating point buffer in native order from the ByteBuffer
            vertexBuffer_ = bb.order(ByteOrder.nativeOrder()).asFloatBuffer();

            double radius = 1;
            double fudge = 0.27;

            double dTheta = Math.PI * 2 / (segments - 1);
            double dPhi = (Math.PI - fudge * 2) / (slices - 1);

            // start in center for triangle fan
            if (filled_) {
                vertexBuffer_.put(0f);
                vertexBuffer_.put(0f);
                vertexBuffer_.put(0f);
            }

            float xyz[] = new float[3];
            float saveFirst[] = new float[3];
            double phi = Math.PI - fudge;
            for (int slice = 0; slice < slices; slice++, phi -= dPhi) {
                Log.d(TAG, "Slice: " + out(Math.cos(phi)));

                //for each stage calculating the segments
                double theta = 0;
                for (int segment = 0; segment < segments; segment++, theta += dTheta) {
                    xyz[0] = (float) (radius * Math.sin(phi) * Math.cos(theta));
                    xyz[1] = (float) (radius * Math.sin(phi) * Math.sin(theta));
                    xyz[2] = (float) (radius * Math.cos(phi));

                    if (segment == 0) {
                        saveFirst[0] = xyz[0];
                        saveFirst[1] = xyz[1];
                        saveFirst[2] = xyz[2];
                    }

                    vertexBuffer_.put(xyz[0]);
                    vertexBuffer_.put(xyz[1]);
                    vertexBuffer_.put(xyz[2]);
                    Log.d(TAG, " pt " + segment + ": " + out(phi) + ", " + out(theta) +
                            " :  " + out(xyz[0]) + "  " + out(xyz[1]) + "  " + out(xyz[2]));
                }
                // Close loop by reconnecting to first point
                vertexBuffer_.put(saveFirst[0]);
                vertexBuffer_.put(saveFirst[1]);
                vertexBuffer_.put(saveFirst[2]);
            }

            // close the shape
            if (filled_) {
                vertexBuffer_.put(0f);
                vertexBuffer_.put(0f);
                vertexBuffer_.put(0f);
            }

            // set the buffer to read the first coordinate
            vertexBuffer_.position(0);

            Log.d(TAG, "Sphere has " + points + " points.");
            return points;
        }


        private int oldsetup(int segments, int slices) {
            int points = ((segments + 1) * slices);
            if (filled_) points += 2;

            int sz = points * COORDS_PER_VERTEX;

            // initialize vertex byte buffer for shape coordinates
            // (number of coordinate values * 4 bytes per float)
            ByteBuffer bb = ByteBuffer.allocateDirect(sz * BYTES_PER_FLOAT);

            // create a floating point buffer in native order from the ByteBuffer
            vertexBuffer_ = bb.order(ByteOrder.nativeOrder()).asFloatBuffer();

            double radius = 1;
            double fudge = 0.27;

            double dTheta = Math.PI * 2 / (segments - 1);
            double dPhi = (Math.PI - fudge * 2) / (slices - 1);

            // start in center for triangle fan
            if (filled_) {
                vertexBuffer_.put(0f);
                vertexBuffer_.put(0f);
                vertexBuffer_.put(0f);
            }

            float xyz[] = new float[3];
            double phi = Math.PI - fudge;
            for (int slice = 0; slice < slices; slice++, phi -= dPhi) {
                Log.d(TAG, "Slice: " + out(Math.cos(phi)));

                //for each stage calculating the segments
                float saveFirst[] = new float[3];
                double theta = 0;
                for (int segment = 0; segment < segments; segment++, theta += dTheta) {
                    xyz[0] = (float) (radius * Math.sin(phi) * Math.cos(theta));
                    xyz[1] = (float) (radius * Math.sin(phi) * Math.sin(theta));
                    xyz[2] = (float) (radius * Math.cos(phi));

                    if (segment == 0) {
                        saveFirst[0] = xyz[0];
                        saveFirst[1] = xyz[1];
                        saveFirst[2] = xyz[2];
                        xyz = new float[3];
                    }

                    vertexBuffer_.put(xyz[0]);
                    vertexBuffer_.put(xyz[1]);
                    vertexBuffer_.put(xyz[2]);
                    Log.d(TAG, " pt " + segment + ": " + out(phi) + ", " + out(theta) +
                            " :  " + out(xyz[0]) + "  " + out(xyz[1]) + "  " + out(xyz[2]));
                }
                // Close loop by reconnecting to first point
                vertexBuffer_.put(saveFirst[0]);
                vertexBuffer_.put(saveFirst[1]);
                vertexBuffer_.put(saveFirst[2]);
            }

            // close the shape
            if (filled_) {
                vertexBuffer_.put(0f);
                vertexBuffer_.put(0f);
                vertexBuffer_.put(0f);
            }

            // set the buffer to read the first coordinate
            vertexBuffer_.position(0);

            Log.d(TAG, "Sphere has " + points + " points.");
            return points;
        }

        private String out(double d) {
            // round to positions
            boolean neg = (d < 0) ? true : false;
            long lval = Math.round(Math.abs(d) * 1000);
            String deci = "000" + (lval % 1000);
            deci = "." + deci.substring(deci.length() - 3);

            if (lval < 1000)
                return (neg ? "-0" : "0") + deci;
            else
                return (neg ? "-" : "") + (lval / 1000) + deci;
        }

        public void draw(int attrVposition) {

            vertexBuffer_.position(0);

            // Prepare the shape coordinate data
            GLES20.glVertexAttribPointer(attrVposition, COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, false,
                    COORDS_PER_VERTEX * 4, vertexBuffer_);

            // Draw the sphere
            if (filled_) {
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, points_);
            } else {
                GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, points_);
            }
            checkGlError("glDrawArrays");
        }
    }


    private class MusicMapRenderer implements GLSurfaceView.Renderer {

        private final String vertexShaderCode =
                "uniform mat4 matWorld; " +
                        "attribute vec4 vPosition; " +
                        "void main() {" +
                        "  gl_Position = matWorld * vPosition;" +
                        "}\n";

        private final String fragmentShaderCode =
                "precision mediump float;" +
                        "uniform vec4 lightColor; " +
                        "void main() {" +
                        "  gl_FragColor = lightColor;" +
                        "}\n";

        private final String litVertexShaderCode =
            "uniform mat4 matWorld; " +
            "uniform mat4 normalMatrix; " +

            "attribute vec4 vPosition; " +

            "uniform vec3 eyePos; " +
            "uniform vec4 lightPos; " +
            "uniform vec4 lightColor; " +

            "uniform vec4 matAmbient; " +
            "uniform vec4 matDiffuse; " +
            "uniform vec4 matSpecular; " +

            "varying vec3 EyespaceNormal; " +
            "varying vec3 lightDir, eyeVec; " +

            "void main() { " +
                "vec3 vNormal = normalize(vPosition.xyz); " +
                "EyespaceNormal = vec3(normalMatrix * vec4(vNormal, 1.0)); " +

                "vec4 position = matWorld * vPosition; " +
                "lightDir = lightPos.xyz - position.xyz; " +
                "eyeVec = eyePos - position.xyz; " +

                "gl_Position = matWorld * vPosition; " +
             "}\n";

        private final String litFragmentShaderCode =
            "precision mediump float; " +

            "uniform vec3 eyePos; " +
            "uniform vec4 lightPos; " +
            "uniform vec4 lightColor; " +

            "uniform vec4 matAmbient; " +
            "uniform vec4 matDiffuse; " +
            "uniform vec4 matSpecular; " +

            "varying vec3 EyespaceNormal; " +
            "varying vec3 lightDir, eyeVec; " +

            "void main() { " +
            "    vec3 N = normalize(EyespaceNormal); " +
            "    vec3 E = normalize(eyeVec); " +
            "    vec3 L = normalize(lightDir); " +
            "    vec3 reflectV = reflect(-L, N); " +

            "    vec4 ambientTerm = matAmbient * lightColor; " +
            "    vec4 diffuseTerm = matDiffuse * max(dot(N, L), 0.0); " +
            "    vec4 specularTerm = matSpecular * pow(max(dot(reflectV, E), 0.0), 5.0); " +

            "    gl_FragColor =  ambientTerm + diffuseTerm + specularTerm; " +
            "}\n";

        // Set color with red, green, blue and alpha (opacity) values
        private final float green_[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
        private final float red_[] = { 0.998f, 0f, 0f, 1.0f };
        private final float cyan_[] = { 0f, 0.998f, 0.998f, 1.0f };

        private int glProgram_;

        private Sphere sphere_;
        private GlShapes.BasicShape cube_;
        private GlShapes.BasicShape triangle_;

        @Override
        public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
            GLES20.glClearColor(0.001f, 0f, 0.2f, 1f);

//            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
//            int fragShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, litVertexShaderCode);
            int fragShader = loadShader(GLES20.GL_FRAGMENT_SHADER, litFragmentShaderCode);
            glProgram_ = GLES20.glCreateProgram();
            GLES20.glAttachShader(glProgram_, vertexShader);
            GLES20.glAttachShader(glProgram_, fragShader);

            GLES20.glLinkProgram(glProgram_);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(glProgram_, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(glProgram_));
                GLES20.glDeleteProgram(glProgram_);
                glProgram_ = 0;
                throw new RuntimeException("Link error");
            }

            attr_vposition = GLES20.glGetAttribLocation(glProgram_, "vPosition");
            checkGlError("uniformloc vPosition");
            attr_matworld = GLES20.glGetUniformLocation(glProgram_, "matWorld");
            checkGlError("uniformloc matWorld");
            attr_matnormal = GLES20.glGetUniformLocation(glProgram_, "normalMatrix");
            checkGlError("uniformloc normalMatrix");
            attr_eyepos = GLES20.glGetUniformLocation(glProgram_, "eyePos");
            checkGlError("uniformloc eyePos");
            //attr_vcolor  = GLES20.glGetUniformLocation(glProgram_, "vColor");

            sphere_ = new Sphere(12, 6, false);
            triangle_ = GlShapes.genShape(GlShapes.ShapeType.TRIANGLE, true);
            cube_ = GlShapes.genShape(GlShapes.ShapeType.CUBE, true);

            //                      offset,         eyeXYZ,
            Matrix.setLookAtM(matView_, 0, eyePos_[0], eyePos_[1], eyePos_[2],
            //      centerXYZ,    upXYZ
                    0f, 0f, 0f, 0f, 1f, 0f);
        }

        @Override
        public void onSurfaceChanged(GL10 gl10, int width, int height) {
            GLES20.glViewport(0, 0, width, height);

            float ratio = (float) width / height;
            Matrix.frustumM(matProjection_, 0, -ratio, ratio, -1, 1, 3, 7);
            // android bug
            //matProjection_[8] /= 2;
        }

        int frames = 0;
        float offset = 0f;
        @Override
        public void onDrawFrame(GL10 gl10) {
            //Log.d(TAG, "onDrawFrame");
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            checkGlError("glClear");

            if (frames++ > 20) {
                frames = 0;
                offset = offset >= 359f ? 0f : offset++;
            }

            setupDraw(true);
            setModel(1f/4, true);
            modelToWorld();

            GLES20.glUniform3f(attr_eyepos, eyePos_[0], eyePos_[1], eyePos_[2]);
            checkGlError("load uniform eyePos");

            GLES20.glUniform4f(GLES20.glGetUniformLocation(glProgram_, "lightColor"),
                    lightColor_[0], lightColor_[1], lightColor_[2], lightColor_[3]);
            checkGlError("load uniform lightColor");

            float lightPos[] = new float[4];
            Matrix.multiplyMV(lightPos, 0, matModel_, 0, lightPos_, 0);
            GLES20.glUniform4f(GLES20.glGetUniformLocation(glProgram_, "lightPos"),
                    lightPos[0], lightPos[1], lightPos[2], lightPos[3]);
            checkGlError("load uniform lightPos");

            GLES20.glUniform4f(GLES20.glGetUniformLocation(glProgram_, "matAmbient"),
                    matAmbient_[0], matAmbient_[1], matAmbient_[2], matAmbient_[3]);
            checkGlError("load uniform matAmbient");
            GLES20.glUniform4f(GLES20.glGetUniformLocation(glProgram_, "matDiffuse"),
                    matDiffuse_[0], matDiffuse_[1], matDiffuse_[2], matDiffuse_[3]);
            checkGlError("load uniform matDiffuse");
            GLES20.glUniform4f(GLES20.glGetUniformLocation(glProgram_, "matSpecular"),
                    matSpecular_[0], matSpecular_[1], matSpecular_[2], matSpecular_[3]);
            checkGlError("load uniform matSpecular");

            // Set color for drawing the triangle
            //GLES20.glUniform4f(attr_vcolor, red_[0], red_[1], red_[2], red_[3]);

            for (int z = 2; z >= -2; z -= 2) {
                for (int row = -4; row < 4; row++) {
                    for (int col = -4; col < 4; col++) {
                        stackModel(true);
                        Matrix.translateM(matModel_, 0, (float) row, (float) col, (float) z);
                        setModel(0.92f, false);
                        modelToWorld();
                        //triangle_.draw(attr_vposition);
                        cube_.draw(attr_vposition);
                        stackModel(false);
                    }
                }
            }

            GLES20.glUniform4f(GLES20.glGetUniformLocation(glProgram_, "matAmbient"),
                    green_[0], green_[1], green_[2], green_[3]);
            stackModel(true);
            setModel(2.2f, false);
            Matrix.rotateM(matModel_, 0, offset, 1, 0, 0);
            modelToWorld();
            sphere_.draw(attr_vposition);
            stackModel(false);

            // Set color for sphere
            //GLES20.glUniform4f(attr_vcolor, cyan_[0], cyan_[1], cyan_[2], cyan_[3]);
/*
            Matrix.translateM(matModel_, 0, -2f, -1.5f, 0f);
            modelToWorld();
            sphere_.draw(attr_vposition);

            Matrix.translateM(matModel_, 0, 1f, 3f, 1f);
            modelToWorld();
            sphere_.draw(attr_vposition);
*/
            setupDraw(false);
        }

        boolean stackModel(boolean push) {
            for (int ii = 0; ii < matModel_.length; ii++) {
                if (push)
                    saveMatModel_[ii] = matModel_[ii];
                else
                    matModel_[ii] = saveMatModel_[ii];
            }
            return true;
        }
        private void setupDraw(boolean begin) {
            if (begin) {
                // Add program to OpenGL ES environment
                GLES20.glUseProgram(glProgram_);
                checkGlError("glUseProgram");

                // get handle to vertex shader's vPosition member
                //mPositionHandle = GLES20.glGetAttribLocation(glProgram_, "vPosition");

                // get handle to fragment shader's vColor member
                //mColorHandle = GLES20.glGetUniformLocation(glProgram_, "vColor");

                // Enable a handle to the triangle vertices
                GLES20.glEnableVertexAttribArray(attr_vposition);
                checkGlError("glEnableVertexAttribArray vposition");
            } else {
                // Disable vertex array
                GLES20.glDisableVertexAttribArray(attr_vposition);
            }
        }
    }

    private void modelToWorld() {
        //                      offset,         eyeXYZ,
        Matrix.setLookAtM(matView_, 0, eyePos_[0], eyePos_[1], eyePos_[2],
                //      centerXYZ,    upXYZ
                0f, 0f, 0f, 0f, 1f, 0f);
        Matrix.multiplyMM(matWorld_, 0, matView_, 0, matModel_, 0);
        Matrix.multiplyMM(matWorld_, 0, matProjection_, 0, matWorld_, 0);
        GLES20.glUniformMatrix4fv(attr_matworld, 1, false, matWorld_, 0);
        checkGlError("uniform matworld");
        float normalT[] = new float[MAT4x4];
        Matrix.invertM(normalT, 0, matWorld_, 0);
        Matrix.transposeM(matNormal_, 0, normalT, 0);
        GLES20.glUniformMatrix4fv(attr_matnormal, 1, false, matNormal_, 0);
        checkGlError("uniform matnormal");
    }

    private void setModel(float scaleFactor, boolean reset) {
        if (reset) {
            Matrix.setIdentityM(matModel_, 0);
            Matrix.rotateM(matModel_, 0, 90, 0f, 0f, 1f);
        }
        Matrix.scaleM(matModel_, 0, scaleFactor, scaleFactor, scaleFactor);


    }

    public static int loadShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);
        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            GLES20.glDeleteShader(shader);
            throw new RuntimeException("Compile error " + compileStatus[0]);
        }

        return shader;
    }


    public MusicMapGLView(Context context) {
        super(context);

        glView_ = new MyGLSurfaceView(this.getContext());
        glView_.setEGLContextClientVersion(2);
        glView_.setPreserveEGLContextOnPause(true);
        glView_.setRenderer(new MusicMapRenderer());
        glView_.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        //glView_.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        // the gl view seems to ignore this flag:
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

        //glView_.draw(canvas);
        glView_.requestRender();
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

    public static void checkGlError(String op) {
        int error;
        boolean toss = false;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            toss = true;
            Log.e(TAG, op + ": glError " + error);
        }
        if (toss) throw new RuntimeException(op + ": glError " + error);
    }

}
