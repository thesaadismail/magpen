package com.gatech.magpen.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;

import com.gatech.magpen.helper.MagPoint;
import com.gatech.magpen.util.MagPenUtils;

import java.util.ArrayList;

/**
 * Created by Brent on 11/4/2014.
 */
public class SensorReadingsView extends View{

    // Flags
    private boolean showReadings;

    // Saved Sensor Data
    private ArrayList<Float> xs;
    private ArrayList<Float> ys;
    private ArrayList<Float> zs;

    // Current Data
    private float[] prev;
    private float[] zeros;

    // Calibrations
    private float[] topLeft, topRight, bottomLeft, bottomRight;
    private boolean[] calibrated = new boolean[] {false,false,false,false};
    public int calibrationState = 0;

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

            if(calibrationState > 3){
                paint.setColor(Color.BLUE);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
                int algoTypeSetting = Integer.parseInt(prefs.getString("pref_interpolation_algorithm_type", "0"));

                MagPoint topLeftMagPoint = new MagPoint(topLeft);
                MagPoint topRightMagPoint = new MagPoint(topRight);
                MagPoint bottomLeftMagPoint = new MagPoint(bottomLeft);
                MagPoint bottomRightMagPoint = new MagPoint(bottomRight);
                MagPoint lastKnownMagValue = new MagPoint(prev);

                MagPoint lastKnownPenValue = MagPenUtils.retrieveMagPoint(algoTypeSetting, topLeftMagPoint, topRightMagPoint,
                        bottomLeftMagPoint, bottomRightMagPoint, lastKnownMagValue,
                        width, height);

                canvas.drawCircle(lastKnownPenValue.xPoint,lastKnownPenValue.yPoint,10,paint);
                paint.setTextSize(40);
                String xPointFormatted = String.format("%.0f", lastKnownMagValue.xPoint);
                String yPointFormatted = String.format("%.0f", lastKnownMagValue.yPoint);

                paint.setColor(Color.GRAY);
                canvas.drawText("MagPoint: ",lastKnownPenValue.xPoint-80,lastKnownPenValue.yPoint-65,paint);
                canvas.drawText("("+xPointFormatted+","+yPointFormatted+")",lastKnownPenValue.xPoint-80,lastKnownPenValue.yPoint-20,paint);
            }

        }

    }

    // Take in sensor reading values and apply filter if enabled
    public void addValues(float[] vals){
        /*
         * IMPORTANT: Stored values do not have the zero offset applied
         */

        // Raw Readings
        prev = vals;

        xs.add(prev[0]);
        ys.add(prev[1]);
        zs.add(prev[2]);
    }

    // Toggle readings view / position view
    public void setReadings(boolean readingsFlag) {
        showReadings = readingsFlag;
    }

    // Set the offsets for zeroing the sensor
    public void setZeros(float x, float y, float z){
        zeros = new float[] {x,y,z};
    }

    public void setCalibration(float[] vals){
        switch(calibrationState){
            case 0:
                topLeft = vals;
                calibrationState++;
                break;
            case 1:
                topRight = vals;
                calibrationState++;
                break;
            case 2:
                bottomLeft = vals;
                calibrationState++;
                break;
            case 3:
                bottomRight = vals;
                calibrationState++;
                break;
            default:
                break;
        }
    }

}
