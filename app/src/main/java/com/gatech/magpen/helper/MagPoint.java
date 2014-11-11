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


}
