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

    private static final String TAG = "MagPenUtils";
    // Percentage change allowance for low-pass filter
    private static float ALPHA = 0.1f;

    // Calculate Point
    public static MagPoint retrieveMagPoint(int algoType,
                                            MagPoint topLeft, MagPoint topRight,
                                            MagPoint bottomLeft, MagPoint bottomRight,
                                            MagPoint actualPointer, float width, float height)
    {
        MagPoint originalTopLeft = new MagPoint(topLeft.toFloatArray());
        MagPoint originalTopRight = new MagPoint(topRight.toFloatArray());
        MagPoint originalBottomLeft = new MagPoint(bottomLeft.toFloatArray());
        MagPoint originalBottomRight = new MagPoint(bottomRight.toFloatArray());

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
            //projection on planes

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
            float theta2 = (float)Math.atan((topLeft.zPoint - bottomLeft.zPoint)/(x12 - (float)(bottomLeft.xPoint * Math.cos(theta) - bottomLeft.yPoint * Math.sin(theta))));

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

            float newX2 = (float)(actualPointer.xPoint * Math.cos(theta2) - actualPointer.yPoint * Math.sin(theta2));
            //float newY2 = (float)(actualPointer.xPoint * Math.sin(theta2) + actualPointer.yPoint * Math.cos(theta2));

            float xRatio = (a*newX2 + i);
            float yRatio = (b*newY + j);
            float xLoc = xRatio * width;
            float yLoc = yRatio * height;


            float fixRatio = bottomLeft.yPoint / bottomRight.yPoint;
            float yDivider = fixRatio*2 - xRatio*(fixRatio*2-1.0f);

            xPos = xLoc;
            yPos = yLoc;// /yDivider;

        }
        else if(algoType == 3)
        {
            //rotational matricies

            //=====================
            //   Flatten Z Axis
            //=====================

            //find lowest calibration point with z value
            float lowestZValue = topLeft.zPoint;
            if(topRight.zPoint < lowestZValue)
                lowestZValue = topRight.zPoint;
            if(bottomLeft.zPoint < lowestZValue)
                lowestZValue = bottomLeft.zPoint;
            if(bottomRight.zPoint < lowestZValue)
                lowestZValue = bottomRight.zPoint;

            //flatten z axis: subtract every z value by this lowest calibration point
            topLeft.zPoint -= lowestZValue;
            topRight.zPoint -= lowestZValue;
            bottomLeft.zPoint -= lowestZValue;
            bottomRight.zPoint -= lowestZValue;

            //flatten z axis: rotate calibration points across the y axis to convert the z value to zero
            topLeft = flattenZAxis(topLeft);
            topRight = flattenZAxis(topRight);
            bottomLeft = flattenZAxis(bottomLeft);
            bottomRight = flattenZAxis(bottomRight);

            //make bottom left 0,0. subtract all values by top right (x,y).
            //by this point z values should all be 0.
            MagPoint subtractWithTopRight = new MagPoint(topRight.toFloatArray());
            topLeft.subtract(subtractWithTopRight);
            topRight.subtract(subtractWithTopRight);
            bottomRight.subtract(subtractWithTopRight);
            bottomLeft.subtract(subtractWithTopRight);

            //=====================
            //Modify Actual Pointer for Z Axis
            //=====================

            //flatten z axis: actual pointer - flatten z axis with lowest z value
            actualPointer.zPoint -= lowestZValue;

            //flatten z axis: rotate pointer
            actualPointer = flattenZAxis(actualPointer);

            //subtract actual pointer with bottom left values
            actualPointer.subtract(subtractWithTopRight);

            //=====================
            //Calculate Percentages
            //=====================

            //calculate distances between the actual pointer and topLeft before rotations
            //this is for future use to figure how how to rotate the actual pointer
            float distanceBetween_actualPointer_topLeft = calculateDistance(actualPointer, topLeft);
            float distanceBetween_actualPointer_topRight = calculateDistance(actualPointer, topRight);
            float distanceBetween_actualPointer_bottomLeft = calculateDistance(actualPointer, bottomLeft);
            float distanceBetween_actualPointer_bottomRight = calculateDistance(actualPointer, bottomRight);

            //we need to calculate the center of this quadilateral
            //to do that, first determine the lines that go through the center

            //these medians will create a vertical line
            MagPoint verticalLine_topMedian = new MagPoint((bottomLeft.xPoint+bottomRight.xPoint)/2, (bottomLeft.yPoint+bottomRight.yPoint)/2, 0);
            MagPoint verticalLine_bottomMedian = new MagPoint((topLeft.xPoint+topRight.xPoint)/2, (topLeft.yPoint+topRight.yPoint)/2, 0);

            //these medians will create a horizontal line
            MagPoint horizontalLine_leftMedian = new MagPoint((topLeft.xPoint+bottomLeft.xPoint)/2, (topLeft.yPoint+bottomLeft.yPoint)/2, 0);
            MagPoint horizontalLine_rightMedian = new MagPoint((topRight.xPoint+bottomRight.xPoint)/2, (topRight.yPoint+bottomRight.yPoint)/2, 0);

            //create a line equation for the vertical line
            float verticalLine_slope = (verticalLine_bottomMedian.yPoint - verticalLine_topMedian.yPoint)/(verticalLine_bottomMedian.xPoint - verticalLine_topMedian.xPoint);
            float horizontalLine_slope = (horizontalLine_rightMedian.yPoint - horizontalLine_leftMedian.yPoint)/(horizontalLine_rightMedian.xPoint - horizontalLine_leftMedian.xPoint);

            float verticalLine_bValue = verticalLine_bottomMedian.yPoint - (verticalLine_slope*verticalLine_bottomMedian.xPoint);
            float horizontal_bValue = horizontalLine_rightMedian.yPoint - (horizontalLine_slope*horizontalLine_rightMedian.xPoint);

            float centroid_xValue = (horizontal_bValue - verticalLine_bValue)/(verticalLine_slope-horizontalLine_slope);
            float centroid_yValue = (verticalLine_slope * centroid_xValue) + verticalLine_bValue;

            MagPoint centroidPoint = new MagPoint(centroid_xValue, centroid_yValue, 0);

            float distanceBetween_centroid_topLeft = calculateDistance(centroidPoint, topLeft);
            float distanceBetween_centroid_topRight = calculateDistance(centroidPoint, topRight);
            float distanceBetween_centroid_bottomLeft = calculateDistance(centroidPoint, bottomLeft);
            float distanceBetween_centroid_bottomRight = calculateDistance(centroidPoint, bottomRight);

            float topLeftPercentage = 0;
            float topRightPercentage = 0;
            float bottomLeftPercentage = 0;
            float bottomRightPercentage = 0;

            if(distanceBetween_actualPointer_topLeft < distanceBetween_centroid_topLeft)
                topLeftPercentage = 1 - (distanceBetween_actualPointer_topLeft/distanceBetween_centroid_topLeft);

            if(distanceBetween_actualPointer_topRight < distanceBetween_centroid_topRight)
                topRightPercentage = 1 - (distanceBetween_actualPointer_topRight/distanceBetween_centroid_topRight);

            if(distanceBetween_actualPointer_bottomLeft < distanceBetween_centroid_bottomLeft)
                bottomLeftPercentage = 1 - (distanceBetween_actualPointer_bottomLeft/distanceBetween_centroid_bottomLeft);

            if(distanceBetween_actualPointer_bottomLeft < distanceBetween_centroid_bottomLeft)
                bottomRightPercentage = 1 - (distanceBetween_actualPointer_bottomLeft/distanceBetween_centroid_bottomLeft);

            //=====================
            //   Rotate Corners
            //=====================

            //======Rotate Top Left=======
            //================================

            //rotate top left to align with TR
            float topLeft_rotation_hypotenuse = (float) Math.sqrt(Math.pow(topLeft.xPoint,2)+Math.pow(topLeft.yPoint,2));

            //multiply angle by negative to rotate towards the x axis and eliminate the y value
            float topLeft_rotationAngle_radians = (float) -Math.acos(topLeft.xPoint / topLeft_rotation_hypotenuse);

            //rotate with an angle
            topLeft = rotateWithAngle(topLeft_rotationAngle_radians, topLeft);

            //======Rotate Bottom Right=======
            //============================

            //rotate bottom right to line it up with TR's x value (which is 0)
            float bottomRight_rotation_hypotenuse = (float) Math.sqrt(Math.pow(bottomRight.xPoint,2)+Math.pow(bottomRight.yPoint,2));

            //positive sign
            float bottomRight_sign = 1;
            if(bottomRight.xPoint < 0) {
                bottomRight_sign = -1;
            }

            //rotate towards the y axis and eliminate the x value
            float bottomRight_rotationAngle_radians = (float) (bottomRight_sign * Math.acos(bottomRight.yPoint / bottomRight_rotation_hypotenuse));

            //rotate with an angle
            bottomRight = rotateWithAngle(bottomRight_rotationAngle_radians, bottomRight);

            //======Rotate Bottom Left=======
            //=============================

            //rotating the bottom left gets a little trickier.
            //we either need to scale up or scale down the bottom left value
            //the bottom left will also need to be rotated to be on the same slope as the intended bottom left value

            //basically we want to align the top lefts's x value with BL's x value and
            //align the BR's y with BL's Y

            float bottomLeft_rotation_hypotenuse = (float) Math.sqrt(Math.pow(bottomLeft.xPoint,2)+Math.pow(bottomLeft.yPoint,2));
            float bottomLeft_positive_rotationAngle_degrees = 0;
            boolean positiveRotationEnabled = false;
            if(bottomLeft.xPoint < 0)
            {
                positiveRotationEnabled = true;

                //rotate towards the y axis and eliminate the x value
                bottomLeft_positive_rotationAngle_degrees = (float) (-2*(Math.acos(bottomLeft.yPoint / bottomLeft_rotation_hypotenuse)));
                bottomLeft = rotateWithAngle(bottomLeft_positive_rotationAngle_degrees, bottomLeft);
            }


                //rotate towards the y axis and eliminate the x value
            float bottomLeft_rotationAngle_degrees = (float) Math.toDegrees(Math.acos(bottomLeft.xPoint / bottomLeft_rotation_hypotenuse));


            //the desired topRight in the square
            MagPoint desiredBottomLeft = new MagPoint(topLeft.xPoint, bottomRight.yPoint, bottomLeft.zPoint);

            //determine new top right on slope
            //this is done using the top right value that we do want and extending it or shortening it on the same line
            //float bottomLeft_desired_slope = desiredBottomLeft.yPoint/desiredBottomLeft.xPoint;

            //float bottomLeft_onSlope_x_value = topLeft.xPoint;
            //float bottomLeft_onSlope_y_value = (bottomLeft_desired_slope*(bottomLeft.xPoint-desiredBottomLeft.xPoint))+desiredBottomLeft.yPoint;

            //now we have the bottom left value on the slope
            //MagPoint new_bottomLeft_onSlope = new MagPoint(bottomLeft_onSlope_x_value, bottomLeft_onSlope_y_value, bottomLeft.zPoint);
            //float new_bottomLeft_onSlope_hypotenuse = (float) Math.sqrt(Math.pow(new_bottomLeft_onSlope.xPoint,2)+Math.pow(new_bottomLeft_onSlope.yPoint,2));

            float desiredSlope_hypotenuse = (float) Math.sqrt(Math.pow(desiredBottomLeft.xPoint,2)+Math.pow(desiredBottomLeft.yPoint,2));

            //determine angle to rotate. this is multiplied by negative to rotate in the clockwise direction
            float desiredSlope_angle_degrees = (float) Math.toDegrees(Math.acos(desiredBottomLeft.xPoint / desiredSlope_hypotenuse));
            float new_bottomLeft_onSlope_rotationAngle_radians = (float) -Math.toRadians(bottomLeft_rotationAngle_degrees - desiredSlope_angle_degrees);

            //now actually rotate this darn thing
            bottomLeft = rotateWithAngle(new_bottomLeft_onSlope_rotationAngle_radians, bottomLeft);

            //we are not done yet! we still have to scale down the top right value
            //don't worry, almost there! ill have to figure out how to optimize this later

            //scale down/up value
            float scaleFactor = bottomLeft.xPoint/topLeft.xPoint;

            //these new values should be close to BR's x and TL's y
            bottomLeft.xPoint = bottomLeft.xPoint/scaleFactor;
            bottomLeft.yPoint = bottomLeft.yPoint/scaleFactor;

            //=====================
            //Modify Actual Pointer for Rotations
            //=====================

            //percentages have been calculated above
            //use these percentages to see how much these rotations effect the actual pointer

            //actual pointer rotated with top left rotation angle
            MagPoint actualPointer_rotated_topLeft = rotateWithAngle(topLeft_rotationAngle_radians, actualPointer);
            float actualPointer_rotated_topLeft_x_diff = actualPointer_rotated_topLeft.xPoint - actualPointer.xPoint;
            float actualPointer_rotated_topLeft_y_diff = actualPointer_rotated_topLeft.yPoint - actualPointer.yPoint;

            //actual pointer rotated with bottom right rotation angle
            MagPoint actualPointer_rotated_bottomRight = rotateWithAngle(bottomRight_rotationAngle_radians, actualPointer);
            float actualPointer_rotated_bottomRight_x_diff = actualPointer_rotated_bottomRight.xPoint - actualPointer.xPoint;
            float actualPointer_rotated_bottomRight_y_diff = actualPointer_rotated_bottomRight.yPoint - actualPointer.yPoint;

            //actual pointer rotated with top right rotation angle
            MagPoint actualPointer_rotated_bottomLeft = actualPointer;
            if(positiveRotationEnabled)
            {
                actualPointer_rotated_bottomLeft = rotateWithAngle(bottomLeft_positive_rotationAngle_degrees, actualPointer_rotated_bottomLeft);
            }

            actualPointer_rotated_bottomLeft = rotateWithAngle(new_bottomLeft_onSlope_rotationAngle_radians, actualPointer_rotated_bottomLeft);

            //this value will also need to be scaled since top right is scaled
            actualPointer_rotated_bottomLeft.xPoint = actualPointer_rotated_bottomLeft.xPoint/scaleFactor;
            actualPointer_rotated_bottomLeft.yPoint = actualPointer_rotated_bottomLeft.yPoint/scaleFactor;

            float actualPointer_rotated_bottomLeft_x_diff = actualPointer_rotated_bottomLeft.xPoint - actualPointer.xPoint;
            float actualPointer_rotated_bottomLeft_y_diff = actualPointer_rotated_bottomLeft.yPoint - actualPointer.yPoint;

            //now after percentages and differences have been calculated
            //make adjustments to the actual pointer
            actualPointer.xPoint += actualPointer_rotated_topLeft_x_diff * topLeftPercentage;
            actualPointer.xPoint += actualPointer_rotated_bottomRight_x_diff * bottomRightPercentage;
            actualPointer.xPoint += actualPointer_rotated_bottomLeft_x_diff * bottomLeftPercentage;

            actualPointer.yPoint += actualPointer_rotated_topLeft_y_diff * topLeftPercentage;
            actualPointer.yPoint += actualPointer_rotated_bottomRight_y_diff * bottomRightPercentage;
            actualPointer.yPoint += actualPointer_rotated_bottomLeft_y_diff * bottomLeftPercentage;

            Log.d(TAG, "Top Left: "+topLeftPercentage);
            Log.d(TAG, "Top Right: "+bottomRightPercentage);
            Log.d(TAG, "Bottom Left: "+bottomLeftPercentage);
//            Log.d(TAG, "Bottom Right: "+bottomRight);

            xPos = topLeft.xPoint - actualPointer.xPoint;
            yPos = -topLeft.yPoint + actualPointer.yPoint;
        }

        return new MagPoint(xPos, yPos, 0);
    }

    //note this only calculates distance in the x and y axis
    private static float calculateDistance(MagPoint p1, MagPoint p2)
    {
        return (float) Math.sqrt((p1.xPoint-p2.xPoint)*(p1.xPoint-p2.xPoint) + (p1.yPoint-p2.yPoint)*(p1.yPoint-p2.yPoint));
    }

    //rotate around the y axis
    private static MagPoint flattenZAxis(MagPoint point)
    {
        float zFlatten_hypotenuse = (float) Math.sqrt(Math.pow(point.yPoint,2)+Math.pow(point.zPoint,2));

        //multiply angle by negative to rotate towards the y axis and eliminate the z value
        float zFlatten_rotationAngle_radians = (float) -Math.acos(point.yPoint/zFlatten_hypotenuse);

        float new_y_value = (float) ((float) (point.yPoint*Math.cos(zFlatten_rotationAngle_radians)) - (point.zPoint*Math.sin(zFlatten_rotationAngle_radians)));
        float new_z_value = (float) ((float) (point.yPoint*Math.sin(zFlatten_rotationAngle_radians)) + (point.zPoint*Math.cos(zFlatten_rotationAngle_radians)));

        return new MagPoint(point.xPoint, new_y_value, new_z_value);
    }

    private static MagPoint rotateWithAngle(float rotationAngleInRadians, MagPoint point)
    {
        float new_x_value = (float) ((float) (point.xPoint * Math.cos(rotationAngleInRadians)) - (point.yPoint * Math.sin(rotationAngleInRadians)));
        float new_y_value = (float) ((float) (point.yPoint * Math.cos(rotationAngleInRadians)) + (point.xPoint * Math.sin(rotationAngleInRadians)));

        return new MagPoint(new_x_value, new_y_value, point.zPoint);
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
