package com.gatech.magpen.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gatech.magpen.helper.MagPoint;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sismail on 11/10/14.
 */
public class MagPenUtils {

    private static float ALPHA = 0.1f;

    public static MagPoint retrieveMagPoint(int algoType,
                                            MagPoint topLeft, MagPoint topRight,
                                            MagPoint bottomLeft, MagPoint bottomRight,
                                            MagPoint actualPointer, float width, float height)
    {
        float xPos = 0, yPos = 0;
        if(algoType == 0)
        {
            //4 corners algorithm
            //float xRatio = (topLeft.xPoint - actualPointer.xPoint) / (topLeft.xPoint - bottomRight.xPoint);
            //float yRatio = Math.abs((topLeft.yPoint - actualPointer.yPoint) / (topLeft.yPoint - bottomRight.yPoint));

            float xLoc1 = ((topLeft.xPoint - actualPointer.xPoint) / (topLeft.xPoint - topRight.xPoint));
            float xLoc2 = (bottomLeft.xPoint - actualPointer.xPoint) / (bottomLeft.xPoint - bottomRight.xPoint);
            xPos = width * ((xLoc1 + xLoc2)/2.0f);

            float yLoc1 = (topLeft.yPoint - actualPointer.yPoint) / (topLeft.yPoint - bottomLeft.yPoint);
            float yLoc2 = (topRight.yPoint - actualPointer.yPoint) / (topRight.yPoint - bottomRight.yPoint);
            yPos =  height * ((yLoc1 + yLoc2)/2.0f);
        }
        else if(algoType == 1)
        {
            //trilateration
            float i1 = topLeft.xPoint;
            float i2 = topRight.xPoint;
            float i3 = bottomLeft.xPoint;
            float j1 = topLeft.yPoint;
            float j2 = topRight.yPoint;
            float j3 = bottomLeft.yPoint;
            float x = actualPointer.xPoint;
            float y = actualPointer.yPoint;

            float d1 = (float)Math.sqrt(Math.pow(x-i1,2) + Math.pow(y - j1,2));
            float d2 = (float)Math.sqrt(Math.pow(x-i2,2) + Math.pow(y - j2,2));
            float d3 = (float)Math.sqrt(Math.pow(x-i3,2) + Math.pow(y - j3,2));

            float xLoc = (float)((((Math.pow(d1,2)-Math.pow(d2,2)) + (Math.pow(i2,2)-Math.pow(i1,2)) + (Math.pow(j2,2)-Math.pow(j1,2))) * (2*j3-2*j2) - ((Math.pow(d2,2)-Math.pow(d3,2)) + (Math.pow(i3,2)-Math.pow(i2,2)) + (Math.pow(j3,2)-Math.pow(j2,2))) *(2*j2-2*j1) ) / ( (2*i2-2*i3)*(2*j2-2*j1)-(2*i1-2*i2)*(2*j3-2*j2 ) ));
            float yLoc = (float)(((Math.pow(d1,2)-Math.pow(d2,2)) + (Math.pow(i2,2)-Math.pow(i1,2)) + (Math.pow(j2,2)-Math.pow(j1,2)) + x*(2*i1-2*i2)) / (2*j2-2*j1));

            xPos = (float)Math.cbrt(((i1 - xLoc) / (i1 - bottomRight.xPoint))) * width;
            yPos = (float)Math.cbrt(((j1 - yLoc) / (j1 - bottomRight.yPoint))) * height;
        }
        else if(algoType == 2)
        {
            //projection on planes
//            float[] newTopRight = new float[3];
//            newTopRight[0] = topLeft.xPoint - topRight.xPoint;
//            newTopRight[1] = topLeft.yPoint - topRight.yPoint;
//            newTopRight[2] = topLeft.zPoint - topRight.zPoint;
//
//            float [] newBottomLeft = new float[3];
//            newBottomLeft[0] = topLeft.xPoint - bottomLeft.xPoint;
//            newBottomLeft[1] = topLeft.yPoint - bottomLeft.yPoint;
//            newBottomLeft[2] = topLeft.zPoint - bottomLeft.zPoint;
//
//            float scaleX = MagPenUtils.magnitude(newTopRight) / (topRight.xPoint - topLeft.xPoint);
//            float deltaX = -scaleX*topLeft.xPoint;
//
//            float scaleY = MagPenUtils.magnitude(newBottomLeft) / (bottomLeft.yPoint - topLeft.yPoint);
//            float deltaY = -scaleY*topLeft.yPoint;
//
//            xPos = (actualPointer.xPoint * scaleX + deltaX) / MagPenUtils.magnitude(newTopRight) * width;
//            yPos = (actualPointer.yPoint * scaleY + deltaY) / MagPenUtils.magnitude(newBottomLeft) * height;

            float theta = (float)Math.atan((topRight.yPoint - topLeft.yPoint)/(topLeft.xPoint - topRight.xPoint));

            float y3 = (float)(bottomRight.xPoint * Math.sin(theta) + bottomRight.yPoint * Math.cos(theta));
            float y2 = (float)(topRight.xPoint * Math.sin(theta) + topRight.yPoint * Math.cos(theta));
            float x3 = (float)(bottomRight.xPoint * Math.cos(theta) - bottomRight.yPoint * Math.sin(theta));
            float x1 = (float)(topLeft.xPoint * Math.cos(theta) - topLeft.yPoint * Math.sin(theta));

            float a = 1.0f/(x3-x1);
            float b = 1.0f/(y3-y2);
            float i = 1.0f - a*bottomRight.xPoint;
            float j = 1.0f - b*bottomRight.yPoint;

            float newX = (float)(actualPointer.xPoint * Math.cos(theta) - actualPointer.yPoint * Math.sin(theta));
            float newY = (float)(actualPointer.xPoint * Math.sin(theta) + actualPointer.yPoint * Math.cos(theta));

            float xRatio = (a*newX + i);
            float yRatio = (b*newY + j);
            float xLoc = xRatio * width;
            float yLoc = yRatio * height;

            xPos = xLoc;
            yPos = yLoc;

        }

        return new MagPoint(xPos, yPos, 0);
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

    public static int detectNumberOfClicks(List<MagPoint> magPointList, float THRESHOLD)
    {
        float[] sortedMagPointMagnitudes = new float[magPointList.size()];
        for (int i = 0; i<magPointList.size(); i++)
        {
            sortedMagPointMagnitudes[i] = MagPenUtils.magnitude(magPointList.get(i));
        }
        Arrays.sort(sortedMagPointMagnitudes);

        float average = mean(sortedMagPointMagnitudes);
        float median = median(sortedMagPointMagnitudes);
        float maxVal = sortedMagPointMagnitudes[sortedMagPointMagnitudes.length-1];
        float minVal = sortedMagPointMagnitudes[0];

        StringBuilder patternBuilder = new StringBuilder();
        float clickDetectedMagnitude = 0;

        for(MagPoint point : magPointList)
        {
            float pointMagnitude = MagPenUtils.magnitude(point);
            if(pointMagnitude <= average+THRESHOLD && pointMagnitude >= average-THRESHOLD)
            {
                patternBuilder.append("B");
            }
            else
            {
                clickDetectedMagnitude = pointMagnitude;
                patternBuilder.append("A");
            }
        }

        String patternStr = patternBuilder.toString();

        Pattern p = Pattern.compile("BA+B");
        Matcher matcher = p.matcher(patternStr);


        Pattern p2 = Pattern.compile("B+A+B+A+");
        Matcher matcher2 = p.matcher(patternStr);

        Pattern p3 = Pattern.compile("A+B+A+B+");
        Matcher matcher3 = p.matcher(patternStr);

        int count = 0;
        while (matcher.find())
            count++;

        while (matcher2.find())
            count++;

        while (matcher3.find())
            count++;


        Log.d("Click Detection Pattern", "average: "+average+
                " numOfClicks:"+count+" Pattern: "+patternStr);
        if(count > 0)
        {
        }

        return count;
    }

    public static int detectNumberOfClicksNew(List<MagPoint> magPointList, float THRESHOLD)
    {
        float[] magPointMagnitudes = new float[magPointList.size()];
        float[] magPointDifferences = new float[magPointList.size()-1];
        for (int i = 0; i<magPointList.size(); i++)
        {
            magPointMagnitudes[i] = MagPenUtils.magnitude(magPointList.get(i));
        }

        for (int i = 1; i<magPointList.size(); i++)
        {
            magPointDifferences[i-1] = Math.abs(magPointMagnitudes[i]-magPointMagnitudes[i-1]);
        }
        //magPointDifferences[magPointDifferences.length-1] = 0;

        StringBuilder patternBuilder = new StringBuilder();
        float clickDetectedMagnitude = 0;

        for(float pointDifference : magPointDifferences)
        {
            if(pointDifference <= THRESHOLD)
            {
                patternBuilder.append("B");
            }
            else
            {
                clickDetectedMagnitude = pointDifference;
                patternBuilder.append("A");
            }
        }

        String patternStr = patternBuilder.toString();

        Pattern p = Pattern.compile("BA+B");
        Matcher matcher = p.matcher(patternStr);

        int count = 0;
        while (matcher.find())
            count++;

        if(count > 0)
        {
            Log.d("Click Detection Pattern", " numOfClicks:"+count+" Pattern: "+patternStr);
        }

        return count;
    }

    public static boolean runSingleClickDetection(List<MagPoint> magPointList)
    {
        int numOfClicks = detectNumberOfClicksNew(magPointList, 7);

        //Log.d(" Pattern: ", patternStr);
        if(numOfClicks > 0)
        {
            return true;
        }

        return false;
    }

    public static boolean runDoubleClickDetection(List<MagPoint> magPointList)
    {
        int numOfClicks = detectNumberOfClicksNew(magPointList, 7);

        //Log.d(" Pattern: ", patternStr);
        if(numOfClicks == 2)
        {
            return true;
        }

        return false;
    }


    public static float mean(float[] m) {
        float sum = 0;
        for (int i = 0; i < m.length; i++) {
            sum += m[i];
        }
        return sum / m.length;
    }

    public static float median(float[] m) {
        int middle = m.length/2;
        if (m.length%2 == 1) {
            return m[middle];
        } else {
            return (float)((m[middle-1] + m[middle]) / 2.0);
        }
    }

    public static float maxVal (float[] numbers) {

        float highest = numbers[0];
        for (int index = 1; index < numbers.length; index ++) {
            if (numbers[index] > highest) {
                highest = numbers [index];
            }
        }

        return highest;
    }

    public static float minVal (float[] numbers) {

        float lowest = numbers[0];
        for (int index = 1; index < numbers.length; index ++) {
            if (numbers[index] < lowest) {
                lowest = numbers [index];
            }
        }

        return lowest;
    }


}
