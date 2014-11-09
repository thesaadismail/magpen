package com.gatech.magpen.helper;

/**
 * Created by sismail on 11/9/14.
 */
public class MagPoint {
    float xPoint, yPoint, zPoint;
    public MagPoint(float x, float y, float z)
    {
        xPoint = x;
        yPoint = y;
        zPoint = z;
    }
    public float[] toFloatArray()
    {
        return new float[]{xPoint,yPoint,zPoint};
    }
}
