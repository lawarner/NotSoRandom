package org.apps.notsorandom;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * A collection of shapes for GL drawing
 */
public class GlShapes {
    private static final String TAG = GlShapes.class.getSimpleName();

    // types of shapes to define
    enum ShapeType {
        TRIANGLE,
        CUBE,
        SPHERE
    }

    // number of coordinates per vertex in this array
    static private final int COORDS_PER_POINT = 3;
    static private final int COORDS_PER_VERTEX = 6;
    static private final int BYTES_PER_FLOAT = 4;
    static private final int VERTEX_POSITION_OFFSET = 0;
    static private final int VERTEX_NORMAL_OFFSET = 3;


    public static BasicShape genShape(ShapeType type, boolean filled) {
        switch (type) {
            case TRIANGLE:
                return new Triangle(filled);
            case CUBE:
                return new TriangleFanCube(filled);
            case SPHERE:
                return new Sphere(filled, 12, 6);
            default:
                Log.e(TAG, "Invalid shape type: " + type);
        }
        throw new RuntimeException("unable to generate shape, type "
                + type + ", filled=" + filled);
    }

    public static BasicShape genSphere(boolean filled, int segments, int slices) {
        return new Sphere(filled, segments, slices);
    }

    /**
     * Common base class of all shapes.  It is expected most subclasses
     * will override the draw() method.
     */
    static public class BasicShape {
        public enum DrawAlgorithm {
            LINE_LOOP,
            TRIANGLES,
            TRIANGLE_FAN,
            TRIANGLE_STRIP,
            CUSTOM
        }

        protected DrawAlgorithm drawAlgorithm_;
        protected FloatBuffer vertexBuffer_;

        protected int points_;
        protected boolean filled_;

        /**
         * Common constructor.  Does not allocate vertex buffer.  Subclasses must handle
         * the alloc or call initVertexBuffer()
         * @param points
         * @param filled
         */
        protected BasicShape(int points, boolean filled, DrawAlgorithm drawAlgorithm) {
            points_ = points;
            filled_ = filled;
            drawAlgorithm_ = drawAlgorithm;
        }

        protected void initVertexBuffer() {
            // initialize vertex byte buffer for shape coordinates
            // (number of coordinate values * 4 bytes per float)
            ByteBuffer bb = ByteBuffer.allocateDirect(points_ * COORDS_PER_VERTEX * BYTES_PER_FLOAT);

            // create a floating point buffer in native order from the ByteBuffer
            vertexBuffer_ = bb.order(ByteOrder.nativeOrder()).asFloatBuffer();
        }

        /**
         * Default implementation draws the vertex array as GL_TRIANGLES
         *
         * @param attrVposition vertex attribute pointer to use for position
         * @param attrVnormal vertex attribute pointer to use for normals
         */
        public void draw(int attrVposition, int attrVnormal) {

            // Prepare the shape's coordinate data
            vertexBuffer_.position(VERTEX_POSITION_OFFSET);
            GLES20.glVertexAttribPointer(attrVposition, COORDS_PER_POINT,
                    GLES20.GL_FLOAT, false,
                    COORDS_PER_VERTEX * BYTES_PER_FLOAT, vertexBuffer_);

            vertexBuffer_.position(VERTEX_NORMAL_OFFSET);
            GLES20.glVertexAttribPointer(attrVnormal, COORDS_PER_POINT,
                    GLES20.GL_FLOAT, false,
                    COORDS_PER_VERTEX * BYTES_PER_FLOAT, vertexBuffer_);

            switch (drawAlgorithm_) {
                case TRIANGLES:
                    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, points_);
                    MusicMapGLView.checkGlError("glDrawArrays triangles");
                    break;
                case LINE_LOOP:
                    GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, points_);
                    MusicMapGLView.checkGlError("glDrawArrays line loop");
                    break;
                case TRIANGLE_FAN:
                case TRIANGLE_STRIP:
                    Log.e(TAG, "Draw algorithm: " + drawAlgorithm_ + " not yet implemented.");
                    break;
                case CUSTOM:
                    throw new RuntimeException("Subclass must implement custom draw() method");
                default:
                    Log.e(TAG, "Draw algorithm: " + drawAlgorithm_ + " unknown.");
            }
        }

        protected void putXYZ(float x, float y, float z) {
            vertexBuffer_.put(x);
            vertexBuffer_.put(y);
            vertexBuffer_.put(z);
        }

        protected void putXYZ(float[] xyz) {
            vertexBuffer_.put(xyz[0]);
            vertexBuffer_.put(xyz[1]);
            vertexBuffer_.put(xyz[2]);
        }
    }

    static class Triangle extends BasicShape {

        float triangleCoords[] = { // in counterclockwise order:
                0.0f,  0.622008459f, 0.0f,   // top
                0f, 0f, -1f,  // normal
                -0.5f, -0.311004243f, 0.0f,   // bottom left
                0f, 0f, -1f,  // normal
                0.5f, -0.311004243f, 0.0f,    // bottom right
                0f, 0f, -1f,  // normal
        };

        public Triangle(boolean filled) {
            // for now, it is always filled
            super(3, true, DrawAlgorithm.TRIANGLES);
            initVertexBuffer();

            // add the coordinates to the FloatBuffer
            vertexBuffer_.put(triangleCoords);
            // set the buffer to read the first coordinate
            vertexBuffer_.position(VERTEX_POSITION_OFFSET);
        }
    }

    static class TriangleFanCube extends BasicShape {

        public TriangleFanCube(boolean filled) {
            super(18, true, DrawAlgorithm.CUSTOM);

            initVertexBuffer();
            putArrays();
            // set the buffer to read the first coordinate
            vertexBuffer_.position(VERTEX_POSITION_OFFSET);
        }

        public void draw(int attrVposition, int attrVnormal) {

            // Prepare the shape's coordinate data
            vertexBuffer_.position(VERTEX_POSITION_OFFSET);
            GLES20.glVertexAttribPointer(attrVposition, COORDS_PER_POINT,
                    GLES20.GL_FLOAT, false,
                    COORDS_PER_VERTEX * BYTES_PER_FLOAT, vertexBuffer_);

            vertexBuffer_.position(VERTEX_NORMAL_OFFSET);
            GLES20.glVertexAttribPointer(attrVnormal, COORDS_PER_POINT,
                    GLES20.GL_FLOAT, false,
                    COORDS_PER_VERTEX * BYTES_PER_FLOAT, vertexBuffer_);

            // Draw with 2 triangle fans
            // Note: It is probably better to draw as 1 triangle strip, which means more
            //        points to draw, but it is 1 gl call vs. 2.
            if (filled_) {
                int pts = points_ / 2;
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, pts);
                MusicMapGLView.checkGlError("glDrawArrays triangle fan");
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, pts, pts);
                MusicMapGLView.checkGlError("glDrawArrays triangle fan");
            } else {
                GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, points_);
                MusicMapGLView.checkGlError("glDrawArrays line loop");
            }
        }

        private void putArrays() {
            final float one = 0.5f;

            float vertices[][] = {
                    { -one, -one, -one }, // 0 left,bottom,front
                    {  one, -one, -one }, // 1 right,bottom,front
                    {  one,  one, -one }, // 2 right,top,front
                    { -one,  one, -one }, // 3 left,top,front
                    { -one,  one,  one }, // 4 left,top,back
                    { -one, -one,  one }, // 5 left,bottom,back
                    {  one, -one,  one }, // 6 right,bottom,back
                    {  one,  one,  one }  // 7 right,top,back
            };
            int indicesA[] = {  0, 1, 2, 3, 4, 5, 6, 1, 0 };
            int normalsA[] = { -3,-3,-3,-2,-2,-1,-1,-1,-3 };
            int indicesB[] = {  7, 2, 1, 6, 5, 4, 3, 2, 7 };
            int normalsB[] = {  1, 1, 1, 3, 3, 2, 2, 2, 1 };
            for (int i = 0; i < indicesA.length; i++) {
                int idx = indicesA[i];
                Log.d(TAG, "putXyz, i=" + i + ", idx=" + idx);
                putXYZ(vertices[idx]);
                putNormal(normalsA[i]);
            }
            for (int i = 0; i < indicesB.length; i++) {
                int idx = indicesB[i];
                putXYZ(vertices[idx]);
                putNormal(normalsB[i]);
            }
        }

        private void putNormal(int norm) {
            float unit = norm < 0 ? -1 : 1;
            norm = Math.abs(norm);

            if (norm == 1) vertexBuffer_.put(unit);
            vertexBuffer_.put(0);
            if (norm == 2) vertexBuffer_.put(unit);
            vertexBuffer_.put(0);
            if (norm == 3) vertexBuffer_.put(unit);
        }
    }

    static class Cube extends BasicShape {

        public Cube(boolean filled) {
            super(filled ? 36 : 8, filled,
                  filled ? DrawAlgorithm.TRIANGLES : DrawAlgorithm.LINE_LOOP);

            initVertexBuffer();

            // add the coordinates to the FloatBuffer
            // Each face is 2 triangles
            putArrays();

            // set the buffer to read the first coordinate
            vertexBuffer_.position(VERTEX_POSITION_OFFSET);
        }

        private void putArrays() {
            final float one = 0.5f;
            float vertices[][] = {
                 { -one, -one, -one },
                 {  one, -one, -one },
                 {  one,  one, -one }, //2
                 { -one,  one, -one },
                 { -one,  one,  one }, //4 7
                 { -one, -one,  one }, //5 4
                 {  one, -one,  one }, //6 5
                 {  one,  one,  one }  //7 6
            };

            int indices[] = {
                    0, 5, 6,    0, 6, 1,  // bot -1,-1,-1 -1,-1,1  1,-1,1  -1,-1,-1  1,-1,1  1,-1,-1
                    1, 6, 7,    1, 7, 2,
                    2, 7, 4,    2, 4, 3,  // top 1,1,-1  1,1,1  -1,1,1  1,1,-1  -1,1,1  -1,1,-1
                    3, 4, 5,    3, 5, 0,
                    5, 4, 7,    5, 7, 6,
                    3, 0, 1,    3, 1, 2
            };
            int normals[] = { 1, 0, 1, 0, 2, 2 };

            if (filled_) {
                for (int face = 0; face < 6; face++) {
                    int normal = normals[face];
                    for (int pt = 0; pt < 6; pt++) {
                        int idx = indices[face * 6 + pt];
                        putVert(vertices[idx], normal);
                    }
                }
            } else {
                for (float vertex[] : vertices) {
                    putVert(vertex, normals[0]);
                }
            }
        }

        private void putVert(float v[], int normal) {
            vertexBuffer_.put(v);

            if (normal == 0) vertexBuffer_.put(v[0] < 0 ? -1 : 1);
            vertexBuffer_.put(0);
            if (normal == 1) vertexBuffer_.put(v[1] < 0 ? -1 : 1);
            vertexBuffer_.put(0);
            if (normal == 2) vertexBuffer_.put(v[2] < 0 ? -1 : 1);
        }

    }

    static class OldCube extends BasicShape {

        public OldCube(boolean filled) {
            super(36, true, DrawAlgorithm.TRIANGLES);

            initVertexBuffer();

            // add the coordinates to the FloatBuffer
            // Each face is 2 triangles
            Log.d(TAG, "Cube points: " + points_);
            putFace( 0.5f, 0);    // front
            putFace( 0.5f, 1);    // left
            putFace(-0.5f, 2);    // top
            putFace(-0.5f, 3);    // back
            putFace(-0.5f, 4);    // right
            putFace( 0.5f, 5);    // bottom

            // set the buffer to read the first coordinate
            vertexBuffer_.position(VERTEX_POSITION_OFFSET);
        }

        private void putFace(float zz, int face) {
            float coords[][] = { { -0.5f,0.5f }, { -0.5f,-0.5f }, { 0.5f,-0.5f }, { 0.5f,0.5f } };
            // Each face is composed of 2 triangles
            if (face > 2) {     // reverse back, right, bottom
                face -= 3;
                putPoint(coords[0], zz, face);
                putPoint(coords[3], zz, face);
                putPoint(coords[2], zz, face);
                putPoint(coords[0], zz, face);
                putPoint(coords[2], zz, face);
                putPoint(coords[1], zz, face);
            } else {
                putPoint(coords[0], zz, face);
                putPoint(coords[1], zz, face);
                putPoint(coords[2], zz, face);
                putPoint(coords[0], zz, face);
                putPoint(coords[2], zz, face);
                putPoint(coords[3], zz, face);
            }
        }

        // faces are 0/3=front/back, 1/4=left/right, 2/5=bottom/top
        // Note: reverse back, right
        private void putPoint(float[] xy, float z, int face) {
            if (face == 1) vertexBuffer_.put(z);
            vertexBuffer_.put(xy[0]);
            if (face == 2) vertexBuffer_.put(z);
            vertexBuffer_.put(xy[1]);
            if (face == 0) vertexBuffer_.put(z);

            z = z < 0 ? -1 : 1;
            if (face == 1) vertexBuffer_.put(z);
            vertexBuffer_.put(0f);
            if (face == 2) vertexBuffer_.put(z);
            vertexBuffer_.put(0f);
            if (face == 0) vertexBuffer_.put(z);
        }
    }

    static class Sphere extends BasicShape {

        public Sphere(boolean filled, int segments, int slices) {
            super(0, filled, DrawAlgorithm.CUSTOM);
            setup(segments, slices);
        }

        private void setup(int segments, int slices) {
            points_ = ((segments + 1) * slices);
            if (filled_) points_ += 2;
            Log.d(TAG, "Sphere has " + points_ + " points.");

            initVertexBuffer();

            double radius = 1;
            double fudge = 0.27;

            double dTheta = Math.PI * 2 / (segments - 1);
            double dPhi = (Math.PI - fudge * 2) / (slices - 1);

            // start in center for triangle fan
            if (filled_) {
                putXYZ(0, 0, 0);
                putXYZ(0, 0, -1);
            }

            float xyz[] = new float[3];
            float saveFirst[] = new float[3];
            double phi = Math.PI - fudge;
            for (int slice = 0; slice < slices; slice++, phi -= dPhi) {
//                Log.d(TAG, "Slice: " + out(Math.cos(phi)));

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

                    putXYZ(xyz);
                    putXYZ(xyz);
//                    Log.d(TAG, " pt " + segment + ": " + out(phi) + ", " + out(theta) +
//                            " :  " + out(xyz[0]) + "  " + out(xyz[1]) + "  " + out(xyz[2]));
                }
                // Close loop by reconnecting to first point
                putXYZ(saveFirst);
                putXYZ(saveFirst);
            }

            // close the shape
            if (filled_) {
                putXYZ(0, 0, 0);
                putXYZ(0, 0, -1);
            }

            // set the buffer to read the first coordinate
            vertexBuffer_.position(VERTEX_POSITION_OFFSET);
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

        public void draw(int attrVposition, int attrVnormal) {

            // Prepare the shape coordinate data
            vertexBuffer_.position(VERTEX_POSITION_OFFSET);
            GLES20.glVertexAttribPointer(attrVposition, COORDS_PER_POINT,
                    GLES20.GL_FLOAT, false,
                    COORDS_PER_VERTEX * 4, vertexBuffer_);

            vertexBuffer_.position(VERTEX_NORMAL_OFFSET);
            GLES20.glVertexAttribPointer(attrVnormal, COORDS_PER_POINT,
                    GLES20.GL_FLOAT, false,
                    COORDS_PER_VERTEX * 4, vertexBuffer_);

            // Draw the sphere
            if (filled_) {
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, points_);
            } else {
                GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, points_);
            }
            MusicMapGLView.checkGlError("glDrawArrays sphere");
        }
    }

}
