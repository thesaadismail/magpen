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
        float xLoc1 = (topLeft.xPoint - actualPointer.xPoint) / (topLeft.xPoint - topRight.xPoint);
        float xLoc2 = (bottomLeft.xPoint - actualPointer.xPoint) / (bottomLeft.xPoint - bottomRight.xPoint);
        float xLoc = width * ((xLoc1 + xLoc2)/2.0f);

        float yLoc1 = (topLeft.yPoint - actualPointer.yPoint) / (topLeft.yPoint - bottomLeft.yPoint);
        float yLoc2 = (topRight.yPoint - actualPointer.yPoint) / (topRight.yPoint - bottomRight.yPoint);
        float yLoc =  height * ((yLoc1 + yLoc2)/2.0f);

        return new MagPoint(xLoc, yLoc, 0);
    }

    public static float[] lowPass(float[] in, float[] out){
        if(out == null) return in;
        for(int i = 0; i < in.length; i++){
            out[i] = out[i] + ALPHA * (in[i]-out[i]);
        }
        return out;
    }

}
