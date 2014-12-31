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

    // Percentage change allowance for low-pass filter
    private static float ALPHA = 0.1f;

    // Calculate Point
    public static MagPoint retrieveMagPoint(int algoType,
                                            MagPoint topLeft, MagPoint topRight,
                                            MagPoint bottomLeft, MagPoint bottomRight,
                                            MagPoint actualPointer, float width, float height)
    {
        float xPos = 0, yPos = 0;
        if(algoType == 0)
        {
            //4 corners algorithm
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

            // Distances from reference points to current point
            float d1 = (float)Math.sqrt(Math.pow(x-i1,2) + Math.pow(y - j1,2));
            float d2 = (float)Math.sqrt(Math.pow(x-i2,2) + Math.pow(y - j2,2));
            float d3 = (float)Math.sqrt(Math.pow(x-i3,2) + Math.pow(y - j3,2));

            // estimated location
            float xLoc = (float)((((Math.pow(d1,2)-Math.pow(d2,2)) + (Math.pow(i2,2)-Math.pow(i1,2)) + (Math.pow(j2,2)-Math.pow(j1,2))) * (2*j3-2*j2) - ((Math.pow(d2,2)-Math.pow(d3,2)) + (Math.pow(i3,2)-Math.pow(i2,2)) + (Math.pow(j3,2)-Math.pow(j2,2))) *(2*j2-2*j1) ) / ( (2*i2-2*i3)*(2*j2-2*j1)-(2*i1-2*i2)*(2*j3-2*j2 ) ));
            float yLoc = (float)(((Math.pow(d1,2)-Math.pow(d2,2)) + (Math.pow(i2,2)-Math.pow(i1,2)) + (Math.pow(j2,2)-Math.pow(j1,2)) + x*(2*i1-2*i2)) / (2*j2-2*j1));

            // account for cubic fall off
            xPos = (float)Math.cbrt(((i1 - xLoc) / (i1 - bottomRight.xPoint))) * width;
            yPos = (float)Math.cbrt(((j1 - yLoc) / (j1 - bottomRight.yPoint))) * height;
        }
        else if(algoType == 2)
        {
            // Projection On Planes

            // CENTERED ON TOP RIGHT

            // Rotation angle around Z
            float theta = (float)Math.atan((topRight.yPoint - topLeft.yPoint)/(topLeft.xPoint - topRight.xPoint));

            // Rotate around z axis
            float y32 = (float)(bottomRight.xPoint * Math.sin(theta) + bottomRight.yPoint * Math.cos(theta));
            float y22 = (float)(topRight.xPoint * Math.sin(theta) + topRight.yPoint * Math.cos(theta));
            float y12 = (float)(topLeft.xPoint * Math.sin(theta) + topLeft.yPoint * Math.cos(theta));
            float x32 = (float)(bottomRight.xPoint * Math.cos(theta) - bottomRight.yPoint * Math.sin(theta));
            float x22 = (float)(topRight.xPoint * Math.cos(theta) - topRight.yPoint * Math.sin(theta));
            float x12 = (float)(topLeft.xPoint * Math.cos(theta) - topLeft.yPoint * Math.sin(theta));

            // Rotation angle around Y
            float theta2 = (float)Math.atan((topLeft.zPoint - topRight.zPoint)/(topLeft.xPoint - topRight.xPoint));

            // Rotate around y axis
            float y3 = (float)(x32 * Math.sin(theta2) + y32 * Math.cos(theta2));
            float y2 = (float)(x22 * Math.sin(theta2) + y22 * Math.cos(theta2));
            float x3 = (float)(x32 * Math.cos(theta2) - y32 * Math.sin(theta2));
            float x1 = (float)(x12 * Math.cos(theta2) - y12 * Math.sin(theta2));

            // Scale and offset
            float a = 1.0f/(x3-x1);
            float b = 1.0f/(y3-y2);
            float i = 1.0f - a*bottomRight.xPoint;
            float j = 1.0f - b*bottomRight.yPoint;

            float newX = (float)(actualPointer.xPoint * Math.cos(theta) - actualPointer.yPoint * Math.sin(theta));
            float newY = (float)(actualPointer.xPoint * Math.sin(theta) + actualPointer.yPoint * Math.cos(theta));

            float newX2 = (float)(newX * Math.cos(theta2) - newY * Math.sin(theta2));
            float newY2 = (float)(newX * Math.sin(theta2) + newY * Math.cos(theta2));

            float xRatio = (a*newX2 + i);
            float yRatio = (b*newY2 + j);
            float xLoc = xRatio * width;
            float yLoc = yRatio * height;


            float fixRatio = bottomLeft.yPoint / bottomRight.yPoint;
            float yDivider = fixRatio*2 - xRatio*(fixRatio*2-1.0f);

            xPos = xLoc;
            yPos = yLoc;// /yDivider;

            // Pos Based on Top Right
            MagPoint tR = new MagPoint(xPos,yPos,0);

            // CENTERED ON TOP LEFT

            // Rotation angle around Z
            theta = (float)Math.atan((topRight.yPoint - topLeft.yPoint)/(topLeft.xPoint - topRight.xPoint));

            // Rotate around z axis
            y32 = (float)(topRight.xPoint * Math.sin(theta) + topRight.yPoint * Math.cos(theta));
            y22 = (float)(topLeft.xPoint * Math.sin(theta) + topLeft.yPoint * Math.cos(theta));
            y12 = (float)(bottomLeft.xPoint * Math.sin(theta) + bottomLeft.yPoint * Math.cos(theta));
            x32 = (float)(topRight.xPoint * Math.cos(theta) - topRight.yPoint * Math.sin(theta));
            x22 = (float)(topLeft.xPoint * Math.cos(theta) - topLeft.yPoint * Math.sin(theta));
            x12 = (float)(bottomLeft.xPoint * Math.cos(theta) - bottomLeft.yPoint * Math.sin(theta));

            // Rotation angle around Y
            theta2 = (float)Math.atan((topLeft.zPoint - topRight.zPoint)/(topLeft.xPoint - topRight.xPoint));

            // Rotate around y axis
            y3 = (float)(x32 * Math.sin(theta2) + y32 * Math.cos(theta2));
            y2 = (float)(x22 * Math.sin(theta2) + y22 * Math.cos(theta2));
            float y1 = (float)(x12 * Math.sin(theta2) + y12 * Math.cos(theta2));
            x3 = (float)(x32 * Math.cos(theta2) - y32 * Math.sin(theta2));
            x1 = (float)(x12 * Math.cos(theta2) - y12 * Math.sin(theta2));
            float x2 = (float)(x22 * Math.cos(theta2) - y22 * Math.sin(theta2));

            // Scale and offset
            a = 1.0f/(x3-x2);
            b = 1.0f/(y1-y3);
            i = 1.0f - a*bottomRight.xPoint;
            j = 1.0f - b*bottomRight.yPoint;

            newX = (float)(actualPointer.xPoint * Math.cos(theta) - actualPointer.yPoint * Math.sin(theta));
            newY = (float)(actualPointer.xPoint * Math.sin(theta) + actualPointer.yPoint * Math.cos(theta));

            newX2 = (float)(newX * Math.cos(theta2) - newY * Math.sin(theta2));
            newY2 = (float)(newX * Math.sin(theta2) + newY * Math.cos(theta2));

            xRatio = (a*newX2 + i);
            yRatio = (b*newY2 + j);
            xLoc = xRatio * width;
            yLoc = yRatio * height;


            fixRatio = bottomLeft.yPoint / bottomRight.yPoint;
            yDivider = fixRatio*2 - xRatio*(fixRatio*2-1.0f);

            xPos = xLoc;
            yPos = yLoc;// /yDivider;

            // Pos Based on Top Left
            MagPoint tL = new MagPoint(xPos,yPos,0);

            // CENTERED ON BOTTOM LEFT

            // Rotation angle around Z
            theta = (float)Math.atan((bottomLeft.yPoint - bottomRight.yPoint)/(bottomRight.xPoint - bottomLeft.xPoint));

            // Rotate around z axis
            y32 = (float)(topLeft.xPoint * Math.sin(theta) + topLeft.yPoint * Math.cos(theta));
            y22 = (float)(bottomLeft.xPoint * Math.sin(theta) + bottomLeft.yPoint * Math.cos(theta));
            y12 = (float)(bottomRight.xPoint * Math.sin(theta) + bottomRight.yPoint * Math.cos(theta));
            x32 = (float)(topLeft.xPoint * Math.cos(theta) - topLeft.yPoint * Math.sin(theta));
            x22 = (float)(bottomLeft.xPoint * Math.cos(theta) - bottomLeft.yPoint * Math.sin(theta));
            x12 = (float)(bottomRight.xPoint * Math.cos(theta) - bottomRight.yPoint * Math.sin(theta));

            // Rotation angle around Y
            theta2 = (float)Math.atan((bottomRight.zPoint - bottomLeft.zPoint)/(bottomRight.xPoint - bottomLeft.xPoint));

            // Rotate around y axis
            y3 = (float)(x32 * Math.sin(theta2) + y32 * Math.cos(theta2));
            y2 = (float)(x22 * Math.sin(theta2) + y22 * Math.cos(theta2));
            y1 = (float)(x12 * Math.sin(theta2) + y12 * Math.cos(theta2));
            x3 = (float)(x32 * Math.cos(theta2) - y32 * Math.sin(theta2));
            x2 = (float)(x22 * Math.cos(theta2) - y22 * Math.sin(theta2));
            x1 = (float)(x12 * Math.cos(theta2) - y12 * Math.sin(theta2));

            // Scale and offset
            a = 1.0f/(x1-x2);
            b = 1.0f/(y1-y3);
            i = 1.0f - a*bottomRight.xPoint;
            j = 1.0f - b*bottomRight.yPoint;

            newX = (float)(actualPointer.xPoint * Math.cos(theta) - actualPointer.yPoint * Math.sin(theta));
            newY = (float)(actualPointer.xPoint * Math.sin(theta) + actualPointer.yPoint * Math.cos(theta));

            newX2 = (float)(newX * Math.cos(theta2) - newY * Math.sin(theta2));
            newY2 = (float)(newX * Math.sin(theta2) + newY * Math.cos(theta2));

            xRatio = (a*newX2 + i);
            yRatio = (b*newY2 + j);
            xLoc = xRatio * width;
            yLoc = yRatio * height;


            fixRatio = bottomLeft.yPoint / bottomRight.yPoint;
            yDivider = fixRatio*2 - xRatio*(fixRatio*2-1.0f);

            xPos = xLoc;
            yPos = yLoc;// /yDivider;

            // Pos Based on Bottom Left
            MagPoint bL = new MagPoint(xPos,yPos,0);

            // CENTERED ON BOTTOM RIGHT

            // Rotation angle around Z
            theta = (float)Math.atan((topRight.yPoint - bottomRight.yPoint)/(bottomRight.xPoint - topRight.xPoint));

            // Rotate around z axis
            y32 = (float)(bottomLeft.xPoint * Math.sin(theta) + bottomLeft.yPoint * Math.cos(theta));
            y22 = (float)(bottomRight.xPoint * Math.sin(theta) + bottomRight.yPoint * Math.cos(theta));
            y12 = (float)(topRight.xPoint * Math.sin(theta) + topRight.yPoint * Math.cos(theta));
            x32 = (float)(bottomLeft.xPoint * Math.cos(theta) - bottomLeft.yPoint * Math.sin(theta));
            x22 = (float)(bottomRight.xPoint * Math.cos(theta) - bottomRight.yPoint * Math.sin(theta));
            x12 = (float)(topRight.xPoint * Math.cos(theta) - topRight.yPoint * Math.sin(theta));

            // Rotation angle around Y
            theta2 = (float)Math.atan((bottomRight.zPoint - bottomLeft.zPoint)/(bottomRight.xPoint - bottomLeft.xPoint));

            // Rotate around y axis
            y3 = (float)(x32 * Math.sin(theta2) + y32 * Math.cos(theta2));
            y2 = (float)(x22 * Math.sin(theta2) + y22 * Math.cos(theta2));
            y1 = (float)(x12 * Math.sin(theta2) + y12 * Math.cos(theta2));
            x3 = (float)(x32 * Math.cos(theta2) - y32 * Math.sin(theta2));
            x1 = (float)(x12 * Math.cos(theta2) - y12 * Math.sin(theta2));

            // Scale and offset
            a = 1.0f/(x1-x3);
            b = 1.0f/(y3-y1);
            i = 1.0f - a*bottomRight.xPoint;
            j = 1.0f - b*bottomRight.yPoint;

            newX = (float)(actualPointer.xPoint * Math.cos(theta) - actualPointer.yPoint * Math.sin(theta));
            newY = (float)(actualPointer.xPoint * Math.sin(theta) + actualPointer.yPoint * Math.cos(theta));

            newX2 = (float)(newX * Math.cos(theta2) - newY * Math.sin(theta2));
            newY2 = (float)(newX * Math.sin(theta2) + newY * Math.cos(theta2));

            xRatio = (a*newX2 + i);
            yRatio = (b*newY2 + j);
            xLoc = xRatio * width;
            yLoc = yRatio * height;


            fixRatio = bottomLeft.yPoint / bottomRight.yPoint;
            yDivider = fixRatio*2 - xRatio*(fixRatio*2-1.0f);

            xPos = xLoc;
            yPos = yLoc;// /yDivider;

            // Pos Based on Bottom Right
            MagPoint bR = new MagPoint(xPos,yPos,0);


            // Weight Reference Points
            MagPoint[] ref = new MagPoint[]{topRight,topLeft,bottomLeft,bottomRight};
            MagPoint[] ret = new MagPoint[]{tR,tL,bL,bR};
            float[] weights = new float[4];
            float totalWeight = 0.0f;

            for(int index = 0; index < 3; index++){
                weights[index] = 1.0f/(float)Math.pow(distance(actualPointer,ref[index]),3);
                totalWeight += weights[index];
            }

            xPos = 0.0f;
            yPos = 0.0f;

            for(int index = 0; index < 3; index++){
                xPos += ret[index].xPoint * weights[index];
                yPos += ret[index].yPoint * weights[index];
            }

            xPos /= totalWeight;
            yPos /= totalWeight;

            //xPos = bR.xPoint;
            //yPos = bR.yPoint;

        }

        return new MagPoint(xPos, yPos, 0);
    }

    // Low-pass filter
    public static float[] lowPass(float[] currentValues, float[] previousValues){
        if(previousValues == null) return currentValues;
        for(int i = 0; i < currentValues.length; i++){
            previousValues[i] = previousValues[i] + ALPHA * (currentValues[i]-previousValues[i]);
        }
        return previousValues;
    }

    // Magnitude from point
    public static float magnitude(MagPoint p){
        return (float)Math.sqrt(Math.pow(p.xPoint,2.0) + Math.pow(p.yPoint,2.0) + Math.pow(p.zPoint,2.0));
    }

    // Magnitude from vector array
    public static float magnitude(float[] vals){
        float sidesTotal = 0;
        for(int i=0; i<vals.length; i++){
            sidesTotal += Math.pow(vals[i],2.0);
        }
        return (float)Math.sqrt(sidesTotal);
    }

    // Click detection
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

    // New click detection
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

    // Single click
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

    // Double click
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

    // Mean of array
    public static float mean(float[] m) {
        float sum = 0;
        for (int i = 0; i < m.length; i++) {
            sum += m[i];
        }
        return sum / m.length;
    }

    // Median of array
    public static float median(float[] m) {
        int middle = m.length/2;
        if (m.length%2 == 1) {
            return m[middle];
        } else {
            return (float)((m[middle-1] + m[middle]) / 2.0);
        }
    }

    // Max of array
    public static float maxVal (float[] numbers) {

        float highest = numbers[0];
        for (int index = 1; index < numbers.length; index ++) {
            if (numbers[index] > highest) {
                highest = numbers [index];
            }
        }

        return highest;
    }

    // Min of array
    public static float minVal (float[] numbers) {

        float lowest = numbers[0];
        for (int index = 1; index < numbers.length; index ++) {
            if (numbers[index] < lowest) {
                lowest = numbers [index];
            }
        }

        return lowest;
    }

    // Distance Between MagPoints
    public static float distance(MagPoint p1, MagPoint p2) {
        return (float)Math.sqrt( Math.pow(p2.xPoint - p1.xPoint,2) + Math.pow(p2.yPoint - p1.yPoint,2) + Math.pow(p2.zPoint - p1.zPoint,2));
    }

}
