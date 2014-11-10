package com.gatech.magpen.helper;

/**
 * Created by sismail on 11/9/14.
 */
public class MatrixHelper {

    public static float dotProduct(float[] x, float[] y) {
        return (x[0] * y[0])+(x[1] * y[1])+(x[2] * y[2]);
    }

    public static float[] multiplyMatrix(float[] x, float num) {

        float[] f = new float[3];
        f[0] = x[0]*num;
        f[1] = x[1]*num;
        f[2] = x[2]*num;
        return f;
    }


    public static float[] divideMatrix(float[] x, float num)
    {
        float[] f = new float[3];
        f[0] = x[0]/num;
        f[1] = x[1]/num;
        f[2] = x[2]/num;
        return f;
    }

    public static float[] subtractMatrix(float[] x, float[] y)
    {
        float[] f = new float[3];
        f[0] = x[0]-y[0];
        f[1] = x[1]-y[1];
        f[2] = x[2]-y[2];
        return f;
    }

    public static float[] crossProduct(float a[], float b[]) {
        float vR[] = new float[3];
        vR[0] = (a[1]*b[2]) - (b[1]*a[2]);
        vR[1] = (a[2]*b[0]) - (b[2]*a[0]);
        vR[2] = (a[0]*b[1]) - (b[0]*a[1]);

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