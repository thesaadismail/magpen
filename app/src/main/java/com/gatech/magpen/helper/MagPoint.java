package com.gatech.magpen.helper;

/**
 * Created by sismail on 11/10/14.
 */
public class MagPoint {
    public float xPoint;
    public float yPoint;
    public float zPoint;

    public MagPoint(float x, float y, float z)
    {
        xPoint = x;
        yPoint = y;
        zPoint = z;
    }

    public MagPoint(float[] values)
    {
        if(values.length == 3)
        {
            xPoint = values[0];
            yPoint = values[1];
            zPoint = values[2];
        }
    }

    public float[] toFloatArray()
    {
        return new float[]{xPoint, yPoint, zPoint};
    }

    public float magnitude(){
        return (float)Math.sqrt(Math.pow(xPoint,2.0) + Math.pow(yPoint,2.0) + Math.pow(zPoint,2.0));
    }

    public void subtract(MagPoint sub)
    {
        xPoint -= sub.xPoint;
        yPoint -= sub.yPoint;
        zPoint -= sub.zPoint;
    }

    public String toString()
    {
        return String.format("MagPoint: x: %.4f | y: %.4f | z: %.4f", xPoint, yPoint, zPoint);
    }



}
