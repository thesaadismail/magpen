package com.gatech.magpen.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Brent on 11/13/2014.
 */
public class StrokeWidthView extends View {

    // current color value
    private int progress;

    // Default color
    private int color = 0xFF660000;

    // placeholder for text bounds
    private Rect bounds = new Rect();

    // CONSTTRUCTOR
    public StrokeWidthView(Context context, AttributeSet attrs){
        super(context,attrs);
        progress = 100;
    }

    // Draw the view
    @Override
    protected void onDraw(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);

        //canvas.drawRect(getWidth()*.2f,getHeight()*.2f,getWidth()*.8f,getHeight()*.8f,paint);

        // Title background
        paint.setColor(Color.rgb(8,81,156));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0,0,getWidth(),getHeight()/4.0f,paint);

        // Title
        String title = "Stroke Width";
        paint.setTextSize(getHeight()/4.0f*.5f);
        float textWidth = paint.measureText(title);
        paint.getTextBounds(title,0,title.length(),bounds);
        float textHeight = bounds.height();
        paint.setColor(Color.WHITE);
        canvas.drawText(title,getWidth()*.5f - textWidth/2.0f,getHeight()/8.0f + textHeight/2.0f,paint);


        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);

        // Draw updated stroke bar
        float start = getWidth()*.2f;
        float end = getWidth()*.8f;
        float width = end-start;
        //canvas.drawCircle(start + ((float)progress/100.0f)*width,getHeight()/2.0f,convertValueT,oWidth(progress)*3,paint);
        for(int i = 0; i <= progress*4; i++){
            canvas.drawCircle(start + (float)i/400.0f * width,getHeight()*2.0f/3.0f,convertValueToWidth((float)i/4.0f)*5,paint);
        }

    }

    public void setProgress(int progress,int color){
        this.progress = progress;
        this.color = color;
    }

    // Coverts percentage value to stroke width
    private float convertValueToWidth(float val){

        if(val > 100) return 100.0f;
        if(val < 0) return 0.0f;

        return 3.0f + 20.0f * (val/100.0f);

    }

}
