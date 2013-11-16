package org.apps.notsorandom;

import android.content.Context;
import android.graphics.Canvas;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

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
    private float[] matRotate_ = new float[MAT4x4];
    private float[] matView_ = new float[MAT4x4];
    private float[] matProjection_ = new float[MAT4x4];
    private float[] matScale_ = new float[MAT4x4];
    private float[] matWorld_ = new float[MAT4x4];
    private float[] matNormal_ = new float[MAT4x4];
    private float[] eyePos_ = new float[]{ 0f, 0f, -4f };
    private float[] look_ = new float[]{ 0f, 0f, 0f };
    private float[] lightPos_ = { 3f, 1f, -6f, 1 };
    private float[] lightColor_ = { 0.8f, 0.8f, 0.8f, 1 };

    private float[] matAmbient_ = new float[4];
    private float[] matDiffuse_ = new float[4];
    private float[] matSpecular_ = new float[4];

    private int attr_matnormal;
    private int attr_matworld;
    private int attr_vposition;
    private int attr_vnormal;
    //private int attr_vcolor;
    private int attr_eyepos;

    private float ratio_;

    private class MyGLSurfaceView extends GLSurfaceView {
        private float startX_;
        private float startY_;

        public MyGLSurfaceView(Context context) {
            super(context);

            ratio_ = 1;
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
                    eyePos_[2] = -4f;
                    look_[0] = 0f;
                    look_[1] = 0f;
                    look_[2] = 0f;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float radius = 4f;
                    float dx = (x - startX_) / 40f;
                    float dy = (y - startY_) / 40f;
                    // x,y,z  0,0,-1   1,0,0  1,1,0
                    eyePos_[0] = (float) (radius * Math.sin(dx));
                    eyePos_[1] = (float) (radius * Math.sin(dy));
                    eyePos_[2] = (float) (-radius * (Math.cos(dx) * Math.cos(dy)));
                    if (Math.cos(dx) < 0 && Math.cos(dy) < 0) {
                        eyePos_[2] = -eyePos_[2];
                    }
                    requestRender();
            }

            return true;
        }
    }

    class SphereOld {
        // number of coordinates per vertex in this array
        static private final int COORDS_PER_VERTEX = 3;
        static private final int BYTES_PER_FLOAT = 4;

        private FloatBuffer vertexBuffer_;

        private int points_;
        private boolean filled_;

        public SphereOld(int segments, int slices, boolean filled) {
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
                    //Log.d(TAG, " pt " + segment + ": " + out(phi) + ", " + out(theta) +
                    //        " :  " + out(xyz[0]) + "  " + out(xyz[1]) + "  " + out(xyz[2]));
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
            boolean neg = (d < 0);
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
/*
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
*/
        private final String litVertexShaderCode =
            "uniform mat4 matWorld; " +
            "uniform mat4 normalMatrix; " +

            "attribute vec4 vPosition; " +
            "attribute vec3 vNormal; " +

            "uniform vec3 eyePos; " +
            "uniform vec4 lightPos; " +
            "uniform vec4 lightColor; " +

            "uniform vec4 matAmbient; " +
            "uniform vec4 matDiffuse; " +
            "uniform vec4 matSpecular; " +

            "varying vec3 EyespaceNormal; " +
            "varying vec3 lightDir, eyeVec; " +

            "void main() { " +
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

        // Set color with red, green, blue
        private final float green_[] = { 0f, 0.998f, 0f };
        private final float yellow_[] = { 0.8f, 0.8f, 0f };
        private final float red_[] = { 0.998f, 0f, 0.02f };
        private final float cyan_[] = { 0f, 0.998f, 0.998f };

        private int glProgram_;

        private GlShapes.BasicShape sphere_;
        private GlShapes.BasicShape cube_;
        private GlShapes.BasicShape triangle_;

        @Override
        public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
            GLES20.glClearColor(0.001f, 0f, 0.2f, 1f);
            GLES20.glClearDepthf(9f);

            GLES20.glEnable(GLES20.GL_CULL_FACE);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);

            GLES20.glCullFace(GLES20.GL_BACK);
//            GLES20.glFrontFace(GLES20.GL_CW);
            GLES20.glDepthFunc(GLES20.GL_LEQUAL);

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
            attr_vnormal = GLES20.glGetAttribLocation(glProgram_, "vNormal");
            checkGlError("uniformloc vNormal");
            attr_matworld = GLES20.glGetUniformLocation(glProgram_, "matWorld");
            checkGlError("uniformloc matWorld");
            attr_matnormal = GLES20.glGetUniformLocation(glProgram_, "normalMatrix");
            checkGlError("uniformloc normalMatrix");
            attr_eyepos = GLES20.glGetUniformLocation(glProgram_, "eyePos");
            checkGlError("uniformloc eyePos");

            sphere_ = GlShapes.genSphere(false, 12, 6);
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

            ratio_ = (float) width / height;
            if (getMapMode() == MapMode.AnimateMode) {
                Matrix.frustumM(matProjection_, 0, -ratio_, ratio_, -1, 1, 0, 5);
            } else {
                Matrix.frustumM(matProjection_, 0, -ratio_, ratio_, -1, 1, 2, 9);
            }
            // android bug?
            //matProjection_[8] /= 2;
        }

        private boolean initAnim = false;

        @Override
        public void onDrawFrame(GL10 gl10) {
            //Log.d(TAG, "onDrawFrame");
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            checkGlError("glClear");

            if (getMapMode() == MapMode.AnimateMode) {
                // frame time runs from 0 to totaltime-1, but remains 0 if not animating
                long frameTime = 0;
                final long BEGINTIME = 2000L;
                final long MOVETIME =  5000L;
                final long STOPTIME =  1000L;
                final long TOTALTIME = BEGINTIME + MOVETIME + STOPTIME;
                final long ENDMOVETIME = BEGINTIME + MOVETIME;
                frameTime = SystemClock.uptimeMillis() % TOTALTIME;

                float origPos = -11;
                Random rand = new Random();
                if (frameTime < BEGINTIME) {
                    // just stay still
                    if (!initAnim) {
                        look_[0] = rand.nextFloat() * 4 - 2;
                        look_[1] = rand.nextFloat() * 4 - 2;
                        look_[2] = rand.nextFloat() * 4 - 2;
                        eyePos_[0] = 0;
                        eyePos_[1] = 0;
                        eyePos_[2] = origPos;
                        initAnim = true;
                    }
                } else if (frameTime < ENDMOVETIME) {
                    // eyepos 0,0,-4 -> look  in 0 to movetime
                    frameTime -= BEGINTIME;
                    float ratio = (float) frameTime / (MOVETIME - 1);
                    eyePos_[0] = ratio * look_[0];
                    eyePos_[1] = ratio * look_[1];
                    eyePos_[2] = ratio * look_[2] + (1 - ratio) * origPos;
                } else {
                    frameTime -= ENDMOVETIME;
                    float ratio = (float) (STOPTIME - frameTime) / STOPTIME;
                    eyePos_[0] = ratio * look_[0];
                    eyePos_[1] = ratio * look_[1];
                    eyePos_[2] = ratio * look_[2] + (1 - ratio) * origPos;

                    initAnim = false;
                }
            }

            setupDraw(true);
            if (getMapMode() == MapMode.AnimateMode)
                setModel(0.9f, 0, 0, 0, true);
            else
                setModel(1f/3.3f, 0, 0, 0, true);
            modelToWorld(false);

            GLES20.glUniform3f(attr_eyepos, eyePos_[0], eyePos_[1], eyePos_[2]);
            checkGlError("load uniform eyePos");

            GLES20.glUniform4f(GLES20.glGetUniformLocation(glProgram_, "lightColor"),
                    lightColor_[0], lightColor_[1], lightColor_[2], lightColor_[3]);
            checkGlError("load uniform lightColor");

            float lightPos[] = new float[4];
            Matrix.multiplyMV(lightPos, 0, matWorld_, 0, lightPos_, 0);
            GLES20.glUniform4f(GLES20.glGetUniformLocation(glProgram_, "lightPos"),
                    lightPos[0], lightPos[1], lightPos[2], lightPos[3]);
            checkGlError("load uniform lightPos");

            // Set color for drawing the triangle
            //GLES20.glUniform4f(attr_vcolor, red_[0], red_[1], red_[2], red_[3]);

            final int mapXYsize = MusicMap.MAPWIDTH * MusicMap.MAPHEIGHT;

            int currSenseIdx = -1;
            SongInfo song = listener_.getCurrSong();
            if (song != null) {
                currSenseIdx = song.getSenseIndex(MusicPlayerApp.getConfig());
                MusicPlayerApp.log(TAG, " Current sense index=" + currSenseIdx);
            }

            // The shuffle map
            renderColor(red_, 1);
            MusicMap.MapEntry[] me = musicMap_.getShuffleEntries();
            for (int xyz = 0; xyz < me.length; xyz++) {
                int count = me[xyz].getCount();
                if (count > 0) {
                    int row = xyz % 8;
                    int col = (xyz % mapXYsize) / 8;
                    int z   = xyz / mapXYsize;
                    float radius = calcUnitRadius(count);
                    if (xyz == currSenseIdx) {
                        renderColor(yellow_, 1);
                        float ballRadius = Math.min(radius, 0.9f);
                        stackModel(true);
                        setModel(ballRadius, (float) col - 4, (float) row - 4, (float) z - 4, false);
                        modelToWorld(true);
                        sphere_.draw(attr_vposition, attr_vnormal);
                        stackModel(false);

                        renderColor(placeMode_ ? cyan_ : green_, 1);
                    }
                    if (radius >= 0.009f) {
                        stackModel(true);
                        setModel(radius, (float) col - 4, (float) row - 4, (float) z - 4, false);
                        modelToWorld(true);
                        cube_.draw(attr_vposition, attr_vnormal);
                        stackModel(false);
                    }
                    if (xyz == currSenseIdx) {
                        renderColor(red_, 1);
                        currSenseIdx = -1;
                    }
                }
            }

            // The library map
            renderColor(yellow_, 0.3f);
            me = musicMap_.getLibEntries();
            for (int xyz = 0; xyz < me.length; xyz++) {
                int count = me[xyz].getCount();
                if (count > 0) {
                    int row = xyz % 8;
                    int col = (xyz % mapXYsize) / 8;
                    int z   = xyz / mapXYsize;
                    float radius = calcUnitRadius(count);
                    if (radius >= 0.009f) {
                        stackModel(true);
                        setModel(radius, (float) col - 4, (float) row - 4, (float) z - 4, false);
                        modelToWorld(true);
                        cube_.draw(attr_vposition, attr_vnormal);
                        stackModel(false);
                    }
                }
            }

            setupDraw(false);
        }

        void renderColor(float[] color, float alpha) {
            for (int i = 0; i < 3; i++) {
                matAmbient_[i] = color[i] * 0.88f;
                matDiffuse_[i] = color[i] * 0.75f;
                matSpecular_[i] = color[i] * 0.9f;
            }
            matAmbient_[3] = alpha;
            matDiffuse_[3] = alpha;
            matSpecular_[3] = alpha;

            GLES20.glUniform4f(GLES20.glGetUniformLocation(glProgram_, "matAmbient"),
                    matAmbient_[0], matAmbient_[1], matAmbient_[2], matAmbient_[3]);
            checkGlError("load uniform matAmbient");
            GLES20.glUniform4f(GLES20.glGetUniformLocation(glProgram_, "matDiffuse"),
                    matDiffuse_[0], matDiffuse_[1], matDiffuse_[2], matDiffuse_[3]);
            checkGlError("load uniform matDiffuse");
            GLES20.glUniform4f(GLES20.glGetUniformLocation(glProgram_, "matSpecular"),
                    matSpecular_[0], matSpecular_[1], matSpecular_[2], matSpecular_[3]);
            checkGlError("load uniform matSpecular");
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
                GLES20.glEnableVertexAttribArray(attr_vnormal);
                checkGlError("glEnableVertexAttribArray vnormal");
            } else {
                // Disable vertex array
                GLES20.glDisableVertexAttribArray(attr_vposition);
                checkGlError("glDisableVertexAttribArray vposition");
                GLES20.glDisableVertexAttribArray(attr_vnormal);
                checkGlError("glDisableVertexAttribArray vnormal");
            }
        }
    }

    private void modelToWorld(boolean loadVals) {
        //                      offset,         eyeXYZ,
        Matrix.setLookAtM(matView_, 0, eyePos_[0], eyePos_[1], eyePos_[2],
        //           centerXYZ,              upXYZ
             look_[0], look_[1], look_[2], 0f, 1f, 0f);
//        Matrix.multiplyMM(matWorld_, 0, matRotate_, 0, matModel_, 0);
//        Matrix.multiplyMM(matWorld_, 0, matView_, 0, matWorld_, 0);
        float[] temp = new float[MAT4x4];
        Matrix.multiplyMM(temp, 0, matView_, 0, matModel_, 0);
        Matrix.multiplyMM(matWorld_, 0, matProjection_, 0, temp, 0);

        if (loadVals) loadUniforms();
    }

    private void loadUniforms() {
        GLES20.glUniformMatrix4fv(attr_matworld, 1, false, matWorld_, 0);
        checkGlError("uniform matworld");
        float normalT[] = new float[MAT4x4];
        Matrix.invertM(normalT, 0, matModel_, 0);
        Matrix.transposeM(matNormal_, 0, normalT, 0);
        GLES20.glUniformMatrix4fv(attr_matnormal, 1, false, matNormal_, 0);
        checkGlError("uniform matnormal");
    }

    private void setModel(float scaleFactor, float x, float y, float z, boolean reset) {
        if (reset) {
            Matrix.setRotateM(matModel_, 0, 90, 0f, 0f, 1f);
            //Matrix.setRotateM(matRotate_, 0, 0, 0, 1, 0);
        }
        Matrix.translateM(matModel_, 0, x, y, z);
        Matrix.scaleM(matModel_, 0, scaleFactor, scaleFactor, scaleFactor);
        /*
        if (reset) {
            Matrix.setIdentityM(matModel_, 0);
            Matrix.setIdentityM(matScale_, 0);
            Matrix.setIdentityM(matTranslate_, 0);
            Matrix.setRotateM(matRotate_, 0, 90, 0f, 0f, 1f);
            Matrix.rotateM(matModel_, 0, 90, 0, 0, 1);
        }
        Matrix.translateM(matModel_, 0, x, y, z);
        Matrix.scaleM(matScale_, 0, scaleFactor, scaleFactor, scaleFactor);
        float temp[] = new float[MAT4x4];
        //Matrix.multiplyMM(temp, 0, matModel_, 0, matRotate_, 0);
        Matrix.multiplyMM(matModel_, 0, matScale_, 0, temp, 0);
        */
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
//        glView_.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
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
        if (getMapMode() != MapMode.ThreeDMode && getMapMode() != MapMode.AnimateMode) {
            super.onDraw(canvas);
        } else {
            if (needRedraw_) {
                redrawMap();
                needRedraw_ = false;
            } else {
                glView_.requestRender();
            }
        }
    }


    @Override
    public void redrawMap() {
        Log.d(TAG, " redrawMap called");
        super.redrawMap();
        if (getMapMode() == MapMode.ThreeDMode || getMapMode() == MapMode.AnimateMode) {
            glView_.requestRender();
        }
    }

    public static void checkGlError(String op) {
        int error;
        int lastError = GLES20.GL_NO_ERROR;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + "error: " + error + ": " + GLUtils.getEGLErrorString(error));
            lastError = error;
        }
        if (lastError != GLES20.GL_NO_ERROR) {
            throw new RuntimeException(op + ": glError " + lastError);
        }
    }
}
