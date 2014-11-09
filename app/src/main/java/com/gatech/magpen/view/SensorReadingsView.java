package com.gatech.magpen.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.gatech.magpen.helper.MagPointsHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Brent on 11/4/2014.
 */
public class SensorReadingsView extends View{

    private static final String TAG = "SensorReadingsView";
    // Flags
    private boolean isFiltered;
    private boolean showReadings;

    // Saved Sensor Data
    private ArrayList<Float> xs;
    private ArrayList<Float> ys;
    private ArrayList<Float> zs;

    // Current Data
    private float[] prev;
    private float[] zeros;
    private float[] calculatedValues;

    // Misc
    private float yScale = 5.0f;
    private float ALPHA = 0.1f;

    public SensorReadingsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupSensorView();
    }

    // Initialize data structures and flags
    private void setupSensorView(){
        xs = new ArrayList<Float>();
        ys = new ArrayList<Float>();
        zs = new ArrayList<Float>();
        isFiltered = false;
        showReadings = true;
        zeros = new float[] {0.0f,0.0f,0.0f};
    }

    // Draw "graphed" readings / magnet location
    protected void onDraw(Canvas canvas){

        super.onDraw(canvas);

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3.0f);
        canvas.drawPaint(paint);

        // Graphed Readings
        if(showReadings){

            paint.setColor(Color.BLUE);

            if (xs.size() > 1) {
                for (int i = 1; i < xs.size(); i++) {
                    canvas.drawLine((i - 1) * 2, 400 - yScale * xs.get(i - 1), i * 2, 400 - yScale * xs.get(i), paint);
                }
            }

            paint.setColor(Color.GREEN);

            if (ys.size() > 1) {
                for (int i = 1; i < ys.size(); i++) {
                    canvas.drawLine((i - 1) * 2, 700 - yScale * ys.get(i - 1), i * 2, 700 - yScale * ys.get(i), paint);
                }
            }

            paint.setColor(Color.RED);

            if (zs.size() > 1) {
                for (int i = 1; i < zs.size(); i++) {
                    canvas.drawLine((i - 1) * 2, 1000 - yScale * zs.get(i - 1), i * 2, 1000 - yScale * zs.get(i), paint);
                }
            }

            // Clear arrays if too large to fit on a single screen
            if (xs.size() > width / 2) {
                xs.clear();
                ys.clear();
                zs.clear();
            }

        }

        // Magnet Location
        else{

            paint.setColor(Color.BLUE);
           // Log.d(TAG, "x: " + prev[0] + "\ty: " + prev[1] + "\tz: " + prev[2]);
           // Log.d(TAG, "0x: "+zeros[0]+"\t0y: "+zeros[1]+"\t0z: "+zeros[2]+"\n");
            float circleX = prev[0] - zeros[0];
            float circleY = prev[2] - zeros[2];
            canvas.drawCircle(Math.abs(circleX)*5, Math.abs(circleY)*5, 10, paint);

        }

    }

    // Simple low pass filter
    public float[] lowPass(float[] in, float[] out){
        if(out == null) return in;
        for(int i = 0; i < in.length; i++){
            out[i] = out[i] + ALPHA * (in[i]-out[i]);
        }
        return out;
    }

    // Take in sensor reading values and apply filter if enabled
    public void addValues(float[] vals, float[] calculatedValues, TextView tv){
        /*
         * IMPORTANT: Stored values do not have the zero offset applied
         */

        // Apply filter
        if(isFiltered)
            prev = lowPass(vals,prev);

        // Raw Readings
        else
            prev = vals;

        xs.add(prev[0]);
        ys.add(prev[1]);
        zs.add(prev[2]);

        if(tv != null)
        {
            String readingsText = String.format(" X: %.1f (%.1f) \n Y: %.1f (%.1f) \n Z: %.1f (%.1f) \n Angle: %.1f (%.1f)",
                    prev[0] - zeros[0],
                    640*calculatedValues[0],
                    prev[1] - zeros[1],
                    480*calculatedValues[1],
                    prev[2] - zeros[2],
                    0.0f,
                    Math.toDegrees(Math.atan((prev[1]-zeros[1])/(prev[0]-zeros[0]))),
                    Math.toDegrees(Math.atan((zeros[1])/(zeros[0]))));
            tv.setText(readingsText);
        }

    }

    // Toggle filter
    public void setFilter(boolean filterFlag){
        isFiltered = filterFlag;
    }

    // Toggle readings view / position view
    public void setReadings(boolean readingsFlag) {
        showReadings = readingsFlag;
    }

    // Set the offsets for zeroing the sensor
    public void setZeros(float x, float y, float z){
        zeros = new float[] {x,y,z};
    }

}
