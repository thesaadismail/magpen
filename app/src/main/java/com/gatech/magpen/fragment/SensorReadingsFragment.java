package com.gatech.magpen.fragment;

import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.*;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.gatech.magpen.R;
import com.gatech.magpen.helper.MagPoint;
import com.gatech.magpen.helper.MagPointsHelper;
import com.gatech.magpen.view.SensorReadingsView;

import java.util.ArrayList;

/**
 * Created by Brent on 11/4/2014.
 */
public class SensorReadingsFragment extends Fragment implements SensorEventListener {

    //Parent Activity
    private ActionBarActivity parentActivity;

    //Views
    @InjectView(R.id.sensorReadingsView)
    public SensorReadingsView sensorReadingsView;

    //Sensors
    private SensorManager mSensorManager;
    private Sensor magnetometer;

    //Sensor Readings
    private ArrayList<Float> zeroX;
    private ArrayList<Float> zeroY;
    private ArrayList<Float> zeroZ;
    private float[] prev;
    private float[] actualValues;
    private float[] zeroes;

    //Listeners
    private CompoundButton.OnCheckedChangeListener filterListener;
    private CompoundButton.OnCheckedChangeListener readingsListener;
    private View.OnClickListener zeroButtonListener;

    // Components
    private TextView tv;
    private CheckBox fBox;
    private CheckBox rBox;
    private Button zb;

    private MagPoint topLeftMagPoint;
    private MagPoint topRightMagPoint;
    private MagPoint bottomLeftMagPoint;
    private MagPoint bottomRightMagPoint;
    // Flags
    private boolean isZeroing;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        parentActivity = (ActionBarActivity)getActivity();

        View rootView = inflater.inflate(R.layout.fragment_sensor_readings, container, false);
        ButterKnife.inject(this, rootView);

        setHasOptionsMenu(true);
        parentActivity.getSupportActionBar().setHomeButtonEnabled(true);
        parentActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSensorManager = (SensorManager)parentActivity.getSystemService(Context.SENSOR_SERVICE);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        tv = (TextView) rootView.findViewById(R.id.readings_text);
        fBox = (CheckBox) rootView.findViewById(R.id.filter_box);
        rBox = (CheckBox) rootView.findViewById(R.id.readings_box);
        zb = (Button) rootView.findViewById(R.id.zero_button);

        isZeroing = false;

        zeroX = new ArrayList<Float>();
        zeroY = new ArrayList<Float>();
        zeroZ = new ArrayList<Float>();

        setUpListeners();

        return rootView;
    }

    // Listeners for checkboxes and zero button
    private void setUpListeners() {

        filterListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sensorReadingsView.setFilter(compoundButton.isChecked());
            }
        };

        readingsListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sensorReadingsView.setReadings(compoundButton.isChecked());
            }
        };

        zeroButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isZeroing = true;
            }
        };

    }

    @OnClick(R.id.calibrateTopLeftButton)
    public void calibrateTopLeft()
    {
        topLeftMagPoint = new MagPoint(actualValues[0] - zeroes[0], actualValues[1]-zeroes[0], actualValues[2]-zeroes[0]);
    }

    @OnClick(R.id.calibrateTopRightButton)
    public void calibrateTopRight()
    {
        topRightMagPoint = new MagPoint(actualValues[0]-zeroes[0], actualValues[1]-zeroes[0], actualValues[2]-zeroes[0]);
    }

    @OnClick(R.id.calibrateBottomLeftButton)
    public void calibrateBottomLeft()
    {
        bottomLeftMagPoint = new MagPoint(actualValues[0]-zeroes[0], actualValues[1]-zeroes[0], actualValues[2]-zeroes[0]);
    }


    @OnClick(R.id.calibrateBottomRightButton)
    public void calibrateBottomRight()
    {
        bottomRightMagPoint = new MagPoint(actualValues[0], actualValues[1], actualValues[2]);
    }


    @Override
    public void onResume(){
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
        fBox.setOnCheckedChangeListener(filterListener);
        rBox.setOnCheckedChangeListener(readingsListener);
        zb.setOnClickListener(zeroButtonListener);

    }

    @Override
    public void onStop() {
        mSensorManager.unregisterListener(this);
        fBox.setOnCheckedChangeListener(null);
        rBox.setOnCheckedChangeListener(null);
        zb.setOnClickListener(null);
        super.onStop();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                getFragmentManager().popBackStack();
                return true;
        }

        return false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // if currently trying to zero the sensor
        if(isZeroing){

            if(zeroX.size() >= 30){
                float avgX = avgArray(zeroX);
                float avgY = avgArray(zeroY);
                float avgZ = avgArray(zeroZ);
                zeroes = new MagPoint(avgX, avgY, avgZ).toFloatArray();
                sensorReadingsView.setZeros(avgX, avgY, avgZ);

                zeroX.clear();
                zeroY.clear();
                zeroZ.clear();

                prev = null;
                isZeroing = false;
            }

            else {
                prev = sensorReadingsView.lowPass(event.values.clone(), prev);
                zeroX.add(prev[0]);
                zeroY.add(prev[1]);
                zeroZ.add(prev[2]);
            }

        }
        // else pass values to the sensorReadingView
        else {
            actualValues = event.values.clone();

            float[] calculatedValues = {0,0,0};
            if(topRightMagPoint != null && topLeftMagPoint != null && bottomLeftMagPoint != null && bottomRightMagPoint!= null)
            {
                calculatedValues = MagPointsHelper.interpolationUsingFourEdges(topLeftMagPoint.toFloatArray(),
                        topRightMagPoint.toFloatArray(),
                        bottomLeftMagPoint.toFloatArray(),
                        bottomRightMagPoint.toFloatArray(),
                        actualValues);
            }

            sensorReadingsView.addValues(event.values.clone(), calculatedValues, tv);
            sensorReadingsView.invalidate();
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    // Return the avg value of floats in the array
    private float avgArray(ArrayList<Float> in) {

        float total = 0.0f;

        for (int i = 0; i < in.size(); i++) {
            total += in.get(i);
        }

        return total / (float) in.size();

    }
}
