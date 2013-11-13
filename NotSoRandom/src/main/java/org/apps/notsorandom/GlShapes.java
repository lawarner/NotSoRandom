package org.apps.notsorandom;

import android.opengl.GLES20;
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
    static private final int COORDS_PER_VERTEX = COORDS_PER_POINT;
    static private final int BYTES_PER_FLOAT = 4;


    public static BasicShape genShape(ShapeType type, boolean filled) {
        switch (type) {
            case TRIANGLE:
                return new Triangle(filled);
            case CUBE:
                return new Cube(filled);
            case SPHERE:
                break;
            default:
                Log.e(TAG, "Invalid shape type: " + type);
        }
        throw new RuntimeException("unable to generate shape, type "
                + type + ", filled=" + filled);
    }

    /**
     * Common base class of all shapes.  It is expected most subclasses
     * will override the draw() method.
     */
    static public class BasicShape {
        protected FloatBuffer vertexBuffer_;

        protected int points_;
        protected boolean filled_;

        protected BasicShape(int points, boolean filled) {
            points_ = points;
            filled_ = filled;
        }

        /**
         * Default implementation draws the vertex array as GL_TRIANGLES
         *
         * @param attrVposition which vertex attribute pointer to use
         */
        public void draw(int attrVposition) {

            vertexBuffer_.position(0);

            // Prepare the triangle coordinate data
            GLES20.glVertexAttribPointer(attrVposition, COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, false,
                    COORDS_PER_VERTEX * 4, vertexBuffer_);

            // Draw the triangle
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, points_);
            MusicMapGLView.checkGlError("glDrawArrays");
        }
    }

    static class Triangle extends BasicShape {

        float triangleCoords[] = { // in counterclockwise order:
                0.0f,  0.622008459f, 0.0f,   // top
                -0.5f, -0.311004243f, 0.0f,   // bottom left
                0.5f, -0.311004243f, 0.0f    // bottom right
        };

        public Triangle(boolean filled) {
            // for now, it is always filled
            super(3, true);

            // initialize vertex byte buffer for shape coordinates
            // (number of coordinate values * 4 bytes per float)
            ByteBuffer bb = ByteBuffer.allocateDirect(points_ * COORDS_PER_POINT * BYTES_PER_FLOAT);

            // create a floating point buffer in native order from the ByteBuffer
            vertexBuffer_ = bb.order(ByteOrder.nativeOrder()).asFloatBuffer();
            // add the coordinates to the FloatBuffer
            vertexBuffer_.put(triangleCoords);
            // set the buffer to read the first coordinate
            vertexBuffer_.position(0);
        }
    }

    static class Cube extends BasicShape {

        public Cube(boolean filled) {
            super(36, filled);

            // initialize vertex byte buffer for shape coordinates
            // (number of coordinate values * 4 bytes per float)
            ByteBuffer bb = ByteBuffer.allocateDirect(points_ * COORDS_PER_POINT * BYTES_PER_FLOAT);

            // create a floating point buffer in native order from the ByteBuffer
            vertexBuffer_ = bb.order(ByteOrder.nativeOrder()).asFloatBuffer();
            // add the coordinates to the FloatBuffer
            // Each face is 2 triangles
            putFace( 0.5f, 0);    // front
            putFace(-0.5f, 1);    // left
            putFace(-0.5f, 2);    // bottom
            putFace(-0.5f, 3);    // back
            putFace( 0.5f, 4);    // right
            putFace(0.5f, 5);    // top

            // set the buffer to read the first coordinate
            vertexBuffer_.position(0);
        }

        private void putFace(float zz, int face) {
            float coords[][] = { { -0.5f,0.5f }, { -0.5f,-0.5f }, { 0.5f,-0.5f }, { 0.5f,0.5f } };

            // Each face is composed of 2 triangles
            if (face > 2) {     // reverse back, right, top
                face -= 3;
                putPoint(coords[0], zz, face);
                putPoint(coords[2], zz, face);
                putPoint(coords[1], zz, face);
                putPoint(coords[0], zz, face);
                putPoint(coords[3], zz, face);
                putPoint(coords[2], zz, face);
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
        }
    }

}
