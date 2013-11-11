package org.apps.notsorandom;

import android.content.Context;
import android.graphics.Canvas;
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

    private static final int MAT4x4 = 16;
    private static final double DEG = Math.PI / 180;

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
    private float[] matSpecular_ = { 1f, 1f, 1f, 1f };

    private int attr_matnormal;
    private int attr_matworld;
    private int attr_vposition;
    //private int attr_vcolor;


    class Sphere {
        // number of coordinates per vertex in this array
        static private final int COORDS_PER_VERTEX = 3;

        private FloatBuffer vertexBuffer_;

        private int points_;

        public Sphere(int segments, int slices) {
            points_ = setup(segments, slices);
        }

        private int setup(int segments, int slices) {
            int sz = (segments * slices /*+ 2*/) * COORDS_PER_VERTEX;

            // initialize vertex byte buffer for shape coordinates
            // (number of coordinate values * 4 bytes per float)
            ByteBuffer bb = ByteBuffer.allocateDirect(sz * 4);

            // create a floating point buffer in native order from the ByteBuffer
            vertexBuffer_ = bb.order(ByteOrder.nativeOrder()).asFloatBuffer();

            double radius = 1;

            double dTheta = Math.PI * 2 / slices;
            double dPhi = Math.PI * 2 / segments;
            int points = 0;

            // start in center for triangle fan
/*            vertexBuffer_.put(0f);
            vertexBuffer_.put(0f);
            vertexBuffer_.put(0f);
            points++;
*/
            for (double phi = 0; phi < 2 * Math.PI; phi += dPhi) {
                //for each stage calculating the slices
                for (double theta = 2 * Math.PI; theta > 0.0; theta -= dTheta) {
                    Log.d(TAG, " vb put " + points + ": " + phi + ", " + theta);

                    vertexBuffer_.put((float) (radius * Math.sin(phi) * Math.cos(theta)) );
                    vertexBuffer_.put((float) (radius * Math.sin(phi) * Math.sin(theta)) );
                    vertexBuffer_.put((float) (radius * Math.cos(phi)) );
                    points++;
                }
            }

            // close the shape
/*            double phi = 2 * Math.PI;
            double theta = 2 * Math.PI;
            vertexBuffer_.put((float) (radius * Math.sin(phi) * Math.cos(theta)) );
            vertexBuffer_.put((float) (radius * Math.sin(phi) * Math.sin(theta)) );
            vertexBuffer_.put((float) (radius * Math.cos(phi)) );
            points++;
*/
            // set the buffer to read the first coordinate
            vertexBuffer_.position(0);

            return points;
        }

        public void draw(int attrVposition) {

            vertexBuffer_.position(0);

            // Prepare the shape coordinate data
            GLES20.glVertexAttribPointer(attrVposition, COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, false,
                    COORDS_PER_VERTEX * 4, vertexBuffer_);

            // Draw the sphere
            //GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, points_);
            GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, points_);
            checkGlError("glDrawArrays");
        }
    }

    class Triangle {

        private FloatBuffer vertexBuffer_;

        // number of coordinates per vertex in this array
        static final int COORDS_PER_VERTEX = 3;
        float triangleCoords[] = { // in counterclockwise order:
                0.0f,  0.622008459f, 0.0f,   // top
                -0.5f, -0.311004243f, 0.0f,   // bottom left
                0.5f, -0.311004243f, 0.0f    // bottom right
        };


        public Triangle() {
            // initialize vertex byte buffer for shape coordinates
            // (number of coordinate values * 4 bytes per float)
            ByteBuffer bb = ByteBuffer.allocateDirect(triangleCoords.length * 4);

            // create a floating point buffer in native order from the ByteBuffer
            vertexBuffer_ = bb.order(ByteOrder.nativeOrder()).asFloatBuffer();
            // add the coordinates to the FloatBuffer
            vertexBuffer_.put(triangleCoords);
            // set the buffer to read the first coordinate
            vertexBuffer_.position(0);
        }

        public void draw(int attrVposition) {

            vertexBuffer_.position(0);

            // Prepare the triangle coordinate data
            GLES20.glVertexAttribPointer(attrVposition, COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, false,
                    COORDS_PER_VERTEX * 4, vertexBuffer_);

            // Draw the triangle
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
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
                        "uniform vec4 vColor; " +
                        "void main() {" +
                        "  gl_FragColor = vColor;" +
                        "}\n";

        private final String litVertexShaderCode =
            "uniform mat4 matWorld; " +
            "uniform mat4 normalMatrix; " +
            "uniform vec3 eyePos; " +

            "attribute vec4 vPosition; " +

            "uniform vec4 lightPos; " +
            "uniform vec4 lightColor; " +

            "varying vec3 EyespaceNormal; " +
            "varying vec3 lightDir, eyeVec; " +

            "void main() { " +
                "vec3 vNormal = normalize(vPosition.xyz); " +
                "EyespaceNormal = vec3(normalMatrix * vec4(vNormal, 1.0)); " +

                "vec4 position = matWorld * vPosition; " +
                "lightDir = lightPos.xyz - position.xyz; " +
                "eyeVec = -position.xyz; " +

                "gl_Position = matWorld * vPosition; " +
             "}\n";

        private final String litFragmentShaderCode =
            "precision mediump float; " +

            "uniform vec4 lightPos; " +
            "uniform vec4 lightColor; " +

            "uniform vec4 matAmbient; " +
            "uniform vec4 matDiffuse; " +
            "uniform vec4 matSpecular; " +

            "uniform vec3 eyePos; " +

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
        private Triangle triangle_;

        @Override
        public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
            GLES20.glClearColor(0.001f, 0f, 0.2f, 1f);

            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, litVertexShaderCode);
            int fragShader = loadShader(GLES20.GL_FRAGMENT_SHADER, litFragmentShaderCode);
            glProgram_ = GLES20.glCreateProgram();
            GLES20.glAttachShader(glProgram_, vertexShader);
            GLES20.glAttachShader(glProgram_, fragShader);

            GLES20.glLinkProgram(glProgram_);

            attr_vposition = GLES20.glGetAttribLocation(glProgram_, "vPosition");
            attr_matworld = GLES20.glGetUniformLocation(glProgram_, "matWorld");
            attr_matnormal = GLES20.glGetUniformLocation(glProgram_, "matNormal");
            //attr_vcolor  = GLES20.glGetUniformLocation(glProgram_, "vColor");

            sphere_ = new Sphere(12, 12);
            triangle_ = new Triangle();

            //                      offset,         eyeXYZ,
            Matrix.setLookAtM(matView_, 0, eyePos_[0], eyePos_[1], eyePos_[2],
            //      centerXYZ,    upXYZ
                    0f, 0f, 0f, 0f, 1f, 0f);
        }

        @Override
        public void onSurfaceChanged(GL10 gl10, int width, int height) {
            GLES20.glViewport(0, 0, width, height);

            float ratio = (float) width / height;
            Matrix.frustumM(matProjection_, 0, -ratio, ratio, -1, 1, 3, 8);
            // android bug
            //matProjection_[8] /= 2;
        }

        int frames = 0;
        float offset = 0f;
        @Override
        public void onDrawFrame(GL10 gl10) {
            //Log.d(TAG, "onDrawFrame");
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            if (frames++ > 20) {
                frames = 0;
                offset = offset >= 359f ? 0f : offset++;
            }
            Matrix.setIdentityM(matModel_, 0);
            Matrix.rotateM(matModel_, 0, 90, 0f, 0f, 1f);

            setupDraw(true);

            GLES20.glUniform3f(GLES20.glGetUniformLocation(glProgram_, "eyePos"),
                    eyePos_[0], eyePos_[1], eyePos_[2]);

            GLES20.glUniform4f(GLES20.glGetUniformLocation(glProgram_, "lightColor"),
                    lightColor_[0], lightColor_[1], lightColor_[2], lightColor_[3]);

            float lightPos[] = new float[4];
            Matrix.multiplyMV(lightPos, 0, matModel_, 0, lightPos_, 0);
            GLES20.glUniform4f(GLES20.glGetUniformLocation(glProgram_, "lightPos"),
                    lightPos[0], lightPos[1], lightPos[2], lightPos[3]);


            GLES20.glUniform4f(GLES20.glGetUniformLocation(glProgram_, "matAmbient"),
                    matAmbient_[0], matAmbient_[1], matAmbient_[2], matAmbient_[3]);
            GLES20.glUniform4f(GLES20.glGetUniformLocation(glProgram_, "matDiffuse"),
                    matDiffuse_[0], matDiffuse_[1], matDiffuse_[2], matDiffuse_[3]);
            GLES20.glUniform4f(GLES20.glGetUniformLocation(glProgram_, "matSpecular"),
                    matSpecular_[0], matSpecular_[1], matSpecular_[2], matSpecular_[3]);

            // Set color for drawing the triangle
            //GLES20.glUniform4f(attr_vcolor, green_[0], green_[1], green_[2], green_[3]);

            modelToWorld();
            sphere_.draw(attr_vposition);

            // Set color for drawing the triangle
            //GLES20.glUniform4f(attr_vcolor, red_[0], red_[1], red_[2], red_[3]);

            Matrix.translateM(matModel_, 0, 1.7f, 1.2f, 0f);
            modelToWorld();
            triangle_.draw(attr_vposition);

            // Set color for sphere
            //GLES20.glUniform4f(attr_vcolor, cyan_[0], cyan_[1], cyan_[2], cyan_[3]);

            Matrix.setIdentityM(matModel_, 0);
            Matrix.setRotateM(matModel_, 0, offset, 0f, 0f, 1f);
            Matrix.translateM(matModel_, 0, -1.9f, -1.5f, 0f);
            Matrix.scaleM(matModel_, 0, 0.8f, 0.8f, 0.8f);
            modelToWorld();
            sphere_.draw(attr_vposition);

            Matrix.translateM(matModel_, 0, 2f, 3f, 1f);
            modelToWorld();
            sphere_.draw(attr_vposition);

            setupDraw(false);
        }

        private void setupDraw(boolean begin) {
            if (begin) {
                // Add program to OpenGL ES environment
                GLES20.glUseProgram(glProgram_);

                // get handle to vertex shader's vPosition member
                //mPositionHandle = GLES20.glGetAttribLocation(glProgram_, "vPosition");

                // get handle to fragment shader's vColor member
                //mColorHandle = GLES20.glGetUniformLocation(glProgram_, "vColor");

                // Enable a handle to the triangle vertices
                GLES20.glEnableVertexAttribArray(attr_vposition);
            } else {
                // Disable vertex array
                GLES20.glDisableVertexAttribArray(attr_vposition);
            }
        }
    }

    private void modelToWorld() {
        Matrix.multiplyMM(matWorld_, 0, matView_, 0, matModel_, 0);
        Matrix.multiplyMM(matWorld_, 0, matProjection_, 0, matWorld_, 0);
        GLES20.glUniformMatrix4fv(attr_matworld, 1, false, matWorld_, 0);

        float normalT[] = new float[MAT4x4];
        Matrix.invertM(normalT, 0, matWorld_, 0);
        Matrix.transposeM(matNormal_, 0, normalT, 0);
        GLES20.glUniformMatrix4fv(attr_matnormal, 1, false, matNormal_, 0);
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
