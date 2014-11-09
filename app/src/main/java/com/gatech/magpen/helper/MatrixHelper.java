package com.gatech.magpen.helper;

import Jama.util.Maths;

/**
 * Created by sismail on 11/9/14.
 */
public class MatrixHelper {

    public static float dotProduct(float[] x, float[] y) {
        return (x[0] * y[0])+(x[1] * y[1])+(x[2] * y[2]);
    }


    public static float[] crossProduct(float a[], float b[]) {
        float vR[] = new float[3];
        vR[0] = (a[1]*b[2]) - (b[1]*a[2]);
        vR[1] = (a[2]*b[0]) - (b[2]*a[0]);
        vR[2] = (a[0]*b[1]) - (b[0]*a[1]);

        return vR;
    }

    public static float[] normalize(float v1[]) {
        float vR[] = new float[3];
        float fMag;

        fMag = (float) Math.sqrt(Math.pow(v1[0], 2) +
                        Math.pow(v1[1], 2) +
                        Math.pow(v1[2], 2)
        );

        vR[0] = v1[0] / fMag;
        vR[1] = v1[1] / fMag;
        vR[2] = v1[2] / fMag;

        return vR;
    }

    public static double frobeniusNorm (float[] a) {
        double total = 0;

        int size = a.length;

        for( int i = 0; i < size; i++ ) {
            double val = a[i];
            total += val*val;
        }

        return Math.sqrt(total);
    }
}