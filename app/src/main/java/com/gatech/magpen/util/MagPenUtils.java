package com.gatech.magpen.util;

import com.gatech.magpen.helper.MagPoint;

/**
 * Created by sismail on 11/10/14.
 */
public class MagPenUtils {

    private static float ALPHA = 0.1f;

    public static MagPoint retrieveMagPoint(MagPoint topLeft, MagPoint topRight,
                                            MagPoint bottomLeft, MagPoint bottomRight,
                                            MagPoint actualPointer, float width, float height)
    {

        MagPoint bLVector = new MagPoint(bottomRight.xPoint-topLeft.xPoint,bottomRight.yPoint-topLeft.yPoint,bottomRight.zPoint-topLeft.zPoint);
        MagPoint currentVector = new MagPoint(actualPointer.xPoint-topLeft.xPoint,actualPointer.yPoint-topLeft.yPoint,actualPointer.zPoint-topLeft.zPoint);

        float scale = currentVector.magnitude()/ bLVector.magnitude();

        float power = 1;//(1-(scale*.66f));

        float xLoc1 = (topLeft.xPoint - actualPointer.xPoint) / (topLeft.xPoint - topRight.xPoint);
        float xLoc2 = (bottomLeft.xPoint - actualPointer.xPoint) / (bottomLeft.xPoint - bottomRight.xPoint);
        float xLoc = width * (float)Math.pow(((xLoc1 + xLoc2)/2.0f),power);

        float yLoc1 = (topLeft.yPoint - actualPointer.yPoint) / (topLeft.yPoint - bottomLeft.yPoint);
        float yLoc2 = (topRight.yPoint - actualPointer.yPoint) / (topRight.yPoint - bottomRight.yPoint);
        float yLoc =  height * (float)Math.pow(((yLoc1 + yLoc2)/2.0f),power);

//        float i1 = topLeft.xPoint;
//        float i2 = topRight.xPoint;
//        float i3 = bottomLeft.xPoint;
//        float j1 = topLeft.yPoint;
//        float j2 = topRight.yPoint;
//        float j3 = bottomLeft.yPoint;
//        float x = actualPointer.xPoint;
//        float y = actualPointer.yPoint;
//
//        float d1 = (float)Math.sqrt(Math.pow(x-i1,2) + Math.pow(y - j1,2));
//        float d2 = (float)Math.sqrt(Math.pow(x-i2,2) + Math.pow(y - j2,2));
//        float d3 = (float)Math.sqrt(Math.pow(x-i3,2) + Math.pow(y - j3,2));
//
//        float xLoc = (float)((((Math.pow(d1,2)-Math.pow(d2,2)) + (Math.pow(i2,2)-Math.pow(i1,2)) + (Math.pow(j2,2)-Math.pow(j1,2))) * (2*j3-2*j2) - ((Math.pow(d2,2)-Math.pow(d3,2)) + (Math.pow(i3,2)-Math.pow(i2,2)) + (Math.pow(j3,2)-Math.pow(j2,2))) *(2*j2-2*j1) ) / ( (2*i2-2*i3)*(2*j2-2*j1)-(2*i1-2*i2)*(2*j3-2*j2 ) ));
//        float yLoc = (float)(((Math.pow(d1,2)-Math.pow(d2,2)) + (Math.pow(i2,2)-Math.pow(i1,2)) + (Math.pow(j2,2)-Math.pow(j1,2)) + x*(2*i1-2*i2)) / (2*j2-2*j1));
//
//        float xPos = (float)Math.cbrt(((i1 - xLoc) / (i1 - bottomRight.xPoint))) * width;
//        float yPos = (float)Math.cbrt(((j1 - yLoc) / (j1 - bottomRight.yPoint))) * height;

        return new MagPoint(xLoc, yLoc, 0);
    }

    public static float[] lowPass(float[] in, float[] out){
        if(out == null) return in;
        for(int i = 0; i < in.length; i++){
            out[i] = out[i] + ALPHA * (in[i]-out[i]);
        }
        return out;
    }

    public static float magnitude(MagPoint p){
        return (float)Math.sqrt(Math.pow(p.xPoint,2.0) + Math.pow(p.yPoint,2.0) + Math.pow(p.zPoint,2.0));
    }

    public static float magnitude(float[] vals){
        float sidesTotal = 0;
        for(int i=0; i<vals.length; i++){
            sidesTotal += Math.pow(vals[i],2.0);
        }
        return (float)Math.sqrt(sidesTotal);
    }

}
