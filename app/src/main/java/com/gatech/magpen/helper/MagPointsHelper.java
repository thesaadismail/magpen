package com.gatech.magpen.helper;

import android.util.Log;

/**
 * Created by sismail on 11/9/14.
 */
public class MagPointsHelper {

    public static double singleEdgeInterpolation(float[] a, float[] b, float[] c)
    {
        float[] n = MatrixHelper.crossProduct(a, b);
        float[] clinha = MatrixHelper.crossProduct(c, n);

        double cosAB = cosEntreVectores(a, b);
        double cosAC = cosEntreVectores(a, clinha);
        double cosBC = cosEntreVectores(b, clinha);

        double angAB = Math.acos(cosAB);
        double angAC = Math.acos(cosAC);
        double angBC = Math.acos(cosBC);

        return angAC/angAB;
    }

    public static float[] interpolationUsingFourEdges(float[] topleft, float[] topright,
                                                     float[] bottomleft, float[] bottomright,
                                                     float[] pointer)
    {
        double AB = singleEdgeInterpolation(topleft, topright, pointer);
        double BC = singleEdgeInterpolation(topright, bottomright, pointer);
        double DC = singleEdgeInterpolation(bottomleft, bottomright, pointer);
        double AD = singleEdgeInterpolation(topleft, bottomleft, pointer);

        double x = (AD * (DC - AB) + AB) / (1 - (BC - AD) * (DC - AB));
        double y = x * (BC - AD) + AD;

        float[] points = new float[2];
        points[0] = (float)x;
        points[1] = (float)y;

        return points;
    }

    public static double cosEntreVectores(float[] x, float[] y)
    {
        return MatrixHelper.dotProduct(x, y)/MatrixHelper.frobeniusNorm(x)/MatrixHelper.frobeniusNorm(y);
    }
}
