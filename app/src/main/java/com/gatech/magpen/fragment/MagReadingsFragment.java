package com.gatech.magpen.fragment;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gatech.magpen.R;
import com.gatech.magpen.helper.MagPoint;
import com.gatech.magpen.util.MagPenUtils;

import java.util.ArrayList;

/*

    MagReadingsFragment will be the base class for reading magnetometer values.
    All screens will use this as a hidden fragment.

    Future work will be to implement custom dialogs/screens for MagReadingsFragment
    to calibrate and zero mag values.


*/
public class MagReadingsFragment extends Fragment implements SensorEventListener {

    //==================================
    //              Variables
    //==================================

    //view bundle arg names
    private static final String ARG_FILTER_ENABLED = "FILTER_ENABLED";
    private static final String ARG_ZEROING_REQUIRED = "ZEROING_REQUIRED";
    private static final String ARG_CALIBRATION_REQUIRED = "CALIBRATION_REQUIRED";

    //view bundle variables
    private boolean filterEnabled;
    private boolean zeroingRequired;
    private boolean calibrationRequired;

    //sensors
    private SensorManager mSensorManager;

    //sensor vars
    private float[] previousMagValue;
    private float[] currentMagValue;

    //zero vars
    final private int ZERO_BUFFER_SIZE = 30;
    private MagPoint zeroValue;
    private ArrayList<MagPoint> zeroValuesBuffer;

    //callback
    MagReadingsCallback magReadingsCallback;


    //current state of application
    private boolean currentlyZeroing;

    //==================================
    //         Static Methods
    //==================================

    public static MagReadingsFragment newInstance() {
        return newInstance(false, false, false);
    }

    public static MagReadingsFragment newInstance(boolean filterEnabled) {
        return newInstance(filterEnabled, false, false);
    }

    public static MagReadingsFragment newInstance(boolean filterEnabled, boolean zeroingRequired) {
        return newInstance(filterEnabled, zeroingRequired, false);
    }

    public static MagReadingsFragment newInstance(boolean filterEnabled, boolean zeroingRequired,
                                                  boolean calibrationRequired) {
        MagReadingsFragment fragment = new MagReadingsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_FILTER_ENABLED, filterEnabled);
        args.putBoolean(ARG_ZEROING_REQUIRED, zeroingRequired);
        args.putBoolean(ARG_CALIBRATION_REQUIRED, calibrationRequired);
        fragment.setArguments(args);
        return fragment;
    }

    //==================================
    //         Class Methods
    //==================================

    public MagReadingsFragment()
    {
        //required empty constructor
    }

    //==================================
    //     View Lifecycle Methods
    //==================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            filterEnabled = getArguments().getBoolean(ARG_FILTER_ENABLED);
            zeroingRequired = getArguments().getBoolean(ARG_ZEROING_REQUIRED);
            calibrationRequired = getArguments().getBoolean(ARG_CALIBRATION_REQUIRED);
        }

        mSensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_mag_readings, container, false);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onPause() {
        mSensorManager.unregisterListener(this);

        super.onPause();
    }

    //==================================
    //  Magnetometer Sensor Methods
    //==================================
    @Override
    public void onSensorChanged(SensorEvent event) {
        //retrieve raw magnetometer values
        float[] rawValues = event.values.clone();

        //currentValues will hold the post processed rawValues
        float[] processedRawValues;

        //if filter is enabled and previousMagValue are not null then filter this thing
        if(filterEnabled && previousMagValue !=null)
            processedRawValues = MagPenUtils.lowPass(rawValues.clone(), previousMagValue);
        else
            processedRawValues = rawValues;

        //if we are currently zeroing then do not do anything with the values
        //wait till we are finished zeroing
        if(currentlyZeroing)
        {
            //make sure we have at least 30 values, if not then continue adding to the zero array
            //if we have atleast 30 values then average the values out
            //if the method returns true then turn off zeroing
            if(zeroValuesBuffer.size() >= 30)
            {
                averageZeroBufferValues();
                currentlyZeroing = false;
            }
            else
            {
                zeroValuesBuffer.add(new MagPoint(processedRawValues));
            }
        }
        else // else pass values to the sensorReadingView
        {
            //if zeroValue is not empty, then subtract the actual values with the zero averages
            if(zeroValue != null)
            {
                processedRawValues[0] = processedRawValues[0] - zeroValue.xPoint;
                processedRawValues[1] = processedRawValues[1] - zeroValue.yPoint;
                processedRawValues[2] = processedRawValues[2] - zeroValue.zPoint;
            }

            currentMagValue = processedRawValues.clone();

            if(magReadingsCallback!=null)
            {
                magReadingsCallback.onSensorChange(currentMagValue.clone());
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //called when the accuracy of a sensor has changed
    }

    //==================================
    //          Zeroing Methods
    //==================================

    public boolean averageZeroBufferValues()
    {
        //only if the buffer is greater than the buffer size then average the values
        if(zeroValuesBuffer.size() >= ZERO_BUFFER_SIZE)
        {
            float xTotal = 0.0f;
            float yTotal = 0.0f;
            float zTotal = 0.0f;

            for(int i = 0; i < zeroValuesBuffer.size(); i++){
                xTotal += zeroValuesBuffer.get(i).xPoint;
                yTotal += zeroValuesBuffer.get(i).yPoint;
                zTotal += zeroValuesBuffer.get(i).zPoint;
            }

            int arraySize = zeroValuesBuffer.size();
            zeroValue = new MagPoint(xTotal/arraySize, yTotal/arraySize, zTotal/arraySize);
            zeroValuesBuffer.clear();

            return true;
        }

        return false;
    }

    //==================================
    //       Mag Readings Callback
    //==================================
    public interface MagReadingsCallback {
        public void onSensorChange(float[] processedMagReadings);
    }
}
