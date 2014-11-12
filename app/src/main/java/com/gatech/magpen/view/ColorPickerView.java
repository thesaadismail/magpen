package com.gatech.magpen.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Brent on 11/11/2014.
 */
public class ColorPickerView extends View {

    // 0-100% value representing color from 0-red to 100-blue
    private int progress;

    // Paint settings
    private Paint paint;

    // CONSTRUCTOR
    public ColorPickerView(Context context, AttributeSet attrs){
        super(context,attrs);
        setupSlider();
    }

    // Setup slider settings
    private void setupSlider(){
        progress = 0;
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);
    }

    // Draw Method
    @Override
    protected void onDraw(Canvas canvas){
        float start = getWidth()*.1f;
        float end = getWidth()*.9f;
        float top = getHeight()*.3f;
        float bottom = getHeight()*.7f;

        // Draw the color palette
        for(int i = 0; i <= 100; i++){
            paint.setColor(convertValueToColor(i));
            canvas.drawRect(start + ((float)i/100.0f)*(end-start),top,start + ((float)(i+1)/100.0f)*(end-start),bottom,paint);
        }
        drawCursor(canvas,start,end,top,bottom);
    }

    // Draw the cursor for the currently selected color
    protected void drawCursor(Canvas canvas, float start, float end, float top, float bottom) {
        float width = (bottom - top) * .1f;
        paint.setColor(Color.BLACK);
        canvas.drawRect(start + (progress/100.0f)*(end-start)-15,top,start + (progress/100.0f)*(end-start)-5,bottom,paint);
        canvas.drawRect(start + (progress/100.0f)*(end-start)+5,top,start + (progress/100.0f)*(end-start)+15,bottom,paint);
        canvas.drawRect(start + (progress/100.0f)*(end-start)-15,top-10,start + (progress/100.0f)*(end-start)+15,top,paint);
        canvas.drawRect(start + (progress/100.0f)*(end-start)-15,bottom,start + (progress/100.0f)*(end-start)+15,bottom+10,paint);

    }

    // SETTER
    public void setProgress(int progress){
        this.progress = progress;
    }

    // Convert value(0-100) to a color int
    private int convertValueToColor(float val){
        float red,green,blue;

        if(val >= 50.0f){
            red = 0.0f;
            green = 255.0f - ((val - 50.0f)/50.0f)*255.0f;
            blue = ((val - 50.0f)/50.0f)*255.0f;
        }
        else{
            red = ((50.0f - val)/50.0f)*255.0f;
            green = 255.0f - ((50.0f - val)/50.0f)*255.0f;
            blue = 0.0f;
        }

        return Color.rgb((int) red, (int) green, (int) blue);

    }

}
