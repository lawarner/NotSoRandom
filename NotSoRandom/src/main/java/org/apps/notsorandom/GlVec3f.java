package org.apps.notsorandom;

import android.util.Log;

/**
 * Created by andy on 11/30/13.
 */
public class GlVec3f {
    private static final String TAG = "GlVec3f";

    private float[] mXYZ = new float[3];

    public GlVec3f() {
        set(0, 0, 0);
    }

    public GlVec3f(float[] xyz) {
        set(xyz);
    }

    public GlVec3f(GlVec3f other) {
        set(other);
    }

    public GlVec3f(float x, float y, float z) {
        set(x, y, z);
    }

    //
    // Some getters and setters

    public float[] asArray() {
        return mXYZ;
    }

    public float getX() {
        return mXYZ[0];
    }

    public float getY() {
        return mXYZ[1];
    }

    public float getZ() {
        return mXYZ[2];
    }

    public float get(int idx) {
        return mXYZ[idx];
    }

    public void set(int idx, float val) {
        mXYZ[idx] = val;
    }

    public void set(GlVec3f other) {
        mXYZ[0] = other.mXYZ[0];
        mXYZ[1] = other.mXYZ[1];
        mXYZ[2] = other.mXYZ[2];
    }

    public void set(float[] xyz) {
        mXYZ[0] = xyz[0];
        mXYZ[1] = xyz[1];
        mXYZ[2] = xyz[2];
    }

    public void set(float x, float y, float z) {
        mXYZ[0] = x;
        mXYZ[1] = y;
        mXYZ[2] = z;
    }

    //
    // Public API methods :

    public float distance(GlVec3f other) {
        return distance(other.mXYZ);
    }

    public float distance(float x, float y, float z) {
        GlVec3f dvec = new GlVec3f(x, y, z);
        dvec.subtract(this);

        return dvec.length();
    }

    public float distance(float[] xyz) {
        GlVec3f dvec = new GlVec3f(xyz);
        dvec.subtract(this);

        return dvec.length();
    }

    public float length() {
        float len = (float) Math.sqrt(mXYZ[0]*mXYZ[0] + mXYZ[1]*mXYZ[1] + mXYZ[2]*mXYZ[2]);
        return len;
    }

    public void normalize() {
        float len = length();
        if (len > 0.99999f && len < 1.00001f) return;   // already normalized

        mXYZ[0] /= len;
        mXYZ[1] /= len;
        mXYZ[2] /= len;
    }

    public GlVec3f normal() {
        GlVec3f vec = new GlVec3f(this);
        vec.normalize();
        return vec;
    }

    public void scale(float factor) {
        mXYZ[0] *= factor;
        mXYZ[1] *= factor;
        mXYZ[2] *= factor;
    }

    public GlVec3f scaled(float factor) {
        GlVec3f vec = new GlVec3f(this);
        vec.scale(factor);
        return vec;
    }

    public void add(GlVec3f other) {
        add(other.mXYZ);
    }

    public void add(float[] xyz) {
       mXYZ[0] += xyz[0];
       mXYZ[1] += xyz[1];
       mXYZ[2] += xyz[2];
    }

    public void add(float x, float y, float z) {
        mXYZ[0] += x;
        mXYZ[1] += y;
        mXYZ[2] += z;
    }

    public void addComponent(int componentIndex, float val) {
        mXYZ[componentIndex] += val;
    }

    /* Z
     |cos θ   -sin θ   0| |x|   |x cos θ - y sin θ|   |x'|
    |sin θ    cos θ   0| |y| = |x sin θ + y cos θ| = |y'|
    |  0       0      1| |z|   |        z        |   |z'|
    Y
       | cos θ    0   sin θ| |x|   | x cos θ + z sin θ|   |x'|
    |   0      1       0| |y| = |         y        | = |y'|
    |-sin θ    0   cos θ| |z|   |-x sin θ + z cos θ|   |z'|
    X
     |1     0           0| |x|   |        x        |   |x'|
    |0   cos θ    -sin θ| |y| = |y cos θ - z sin θ| = |y'|
    |0   sin θ     cos θ| |z|   |y sin θ + z cos θ|   |z'|
     */
    public void rotateX(float radians) {  rotateComponent(0, radians);  }
    public void rotateY(float radians) {  rotateComponent(1, radians);  }
    public void rotateZ(float radians) {  rotateComponent(2, radians);  }

    public void rotateComponent(int compIndex, float radians) {
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        double newX;
        double newY;
        double newZ;

        switch (compIndex) {
            case 0:
                newY = mXYZ[1] * cos - mXYZ[2] * sin;
                newZ = mXYZ[1] * sin + mXYZ[2] * cos;
                mXYZ[1] = (float) newY;
                mXYZ[2] = (float) newZ;
                break;
            case 1:
                newX =  mXYZ[0] * cos + mXYZ[2] * sin;
                newZ = -mXYZ[0] * sin + mXYZ[2] * cos;
                mXYZ[0] = (float) newX;
                mXYZ[2] = (float) newZ;
                break;
            case 2:
                newX =  mXYZ[0] * cos - mXYZ[1] * sin;
                newY =  mXYZ[0] * sin + mXYZ[1] * cos;
                mXYZ[0] = (float) newX;
                mXYZ[1] = (float) newY;
                break;
            default:
                Log.e(TAG, "ERROR: " + compIndex + " is not a valid component index.");
        }
    }

    public void multiply(GlVec3f other) {
        multiply(other.mXYZ);
    }

    public void multiply(float[] xyz) {
        multiply(xyz[0], xyz[1], xyz[2]);
    }

    public void multiply(float x, float y, float z) {
        mXYZ[0] *= x;
        mXYZ[1] *= y;
        mXYZ[2] *= z;
    }

    public void multiplyComponent(int compIndex, float scale) {
        mXYZ[compIndex] *= scale;
    }

    public void subtract(GlVec3f other) {
        subtract(other.mXYZ);
    }

    public void subtract(float[] xyz) {
        subtract(xyz[0], xyz[1], xyz[2]);
    }

    public void subtract(float x, float y, float z) {
        mXYZ[0] -= x;
        mXYZ[1] -= y;
        mXYZ[2] -= z;
    }

    public String toString() {
        return "(" + mXYZ[0] + ", " + mXYZ[1] + ", " + mXYZ[2] + ")";
    }
}
