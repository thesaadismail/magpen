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
import com.gatech.magpen.R;
import com.gatech.magpen.helper.MagPoint;
import com.gatech.magpen.util.MagPenUtils;
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
    private ArrayList<MagPoint> zeroValuesBuffer;
    final private int ZERO_BUFFER_SIZE = 30;
    private MagPoint zeroValues;
    private MagPoint previousValues;

    //Listeners
    private CompoundButton.OnCheckedChangeListener filterListener;
    private CompoundButton.OnCheckedChangeListener readingsListener;
    private View.OnClickListener zeroButtonListener;
    private View.OnClickListener calibrateButtonlistener;

    private boolean isFiltered;

    // Components
    private TextView axisValuesTextView;
    private CheckBox filterCheckbox;
    private CheckBox displayReadingsCheckBox;
    private Button zeroButton;
    private Button calibrateButton;

    // Flags
    private boolean isZeroing;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        isFiltered = false;

        parentActivity = (ActionBarActivity)getActivity();

        View rootView = inflater.inflate(R.layout.fragment_sensor_readings, container, false);
        ButterKnife.inject(this, rootView);

        setHasOptionsMenu(true);
        parentActivity.getSupportActionBar().setHomeButtonEnabled(true);
        parentActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSensorManager = (SensorManager)parentActivity.getSystemService(Context.SENSOR_SERVICE);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        axisValuesTextView = (TextView) rootView.findViewById(R.id.readings_text);
        filterCheckbox = (CheckBox) rootView.findViewById(R.id.filter_box);
        displayReadingsCheckBox = (CheckBox) rootView.findViewById(R.id.readings_box);
        zeroButton = (Button) rootView.findViewById(R.id.zero_button);
        calibrateButton = (Button) rootView.findViewById(R.id.calibrate_button);

        isZeroing = false;

        zeroValuesBuffer = new ArrayList<MagPoint>();

        setUpListeners();

        return rootView;
    }

    // Listeners for checkboxes and zero button
    private void setUpListeners() {

        filterListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isFiltered = compoundButton.isChecked();
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

        calibrateButtonlistener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sensorReadingsView.calibrationState < 4){
                    sensorReadingsView.setCalibration(previousValues.toFloatArray());
                    switch(sensorReadingsView.calibrationState){
                        case 1:
                            calibrateButton.setText("Calibrate Top Right");
                            break;
                        case 2:
                            calibrateButton.setText("Calibrate Bottom Left");
                            break;
                        case 3:
                            calibrateButton.setText("Calibrate Bottom Right");
                            break;
                        default:
                            break;
                    }
                }
            }
        };

    }

    @Override
    public void onResume(){
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
        filterCheckbox.setOnCheckedChangeListener(filterListener);
        displayReadingsCheckBox.setOnCheckedChangeListener(readingsListener);
        zeroButton.setOnClickListener(zeroButtonListener);
        calibrateButton.setOnClickListener(calibrateButtonlistener);

    }

    @Override
    public void onStop() {
        mSensorManager.unregisterListener(this);
        filterCheckbox.setOnCheckedChangeListener(null);
        displayReadingsCheckBox.setOnCheckedChangeListener(null);
        zeroButton.setOnClickListener(null);
        calibrateButton.setOnClickListener(null);
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
        float[] rawValues = event.values.clone();
        float[] currentValues;

        // Apply filter
        if(isFiltered && previousValues != null)
            currentValues = MagPenUtils.lowPass(rawValues.clone(), previousValues.toFloatArray());
        else
            currentValues = rawValues;

        // if currently trying to zero the sensor
        if(isZeroing)
        {
            //make sure we have at least 30 values, if not then continue adding to the zero array
            //if we have atleast 30 values then average the values out
            //if the method returns true then turn off zeroing
            if(zeroValuesBuffer.size() >= 30 && averageZeroBufferValues())
            {
                isZeroing = false;
            }
            else
            {
                zeroValuesBuffer.add(new MagPoint(currentValues));
            }

        }
        else // else pass values to the sensorReadingView
        {
            //if zeroValues is not empty, then subtract the actual values with the zero averages
            if(zeroValues != null)
            {
                currentValues[0] = currentValues[0] - zeroValues.xPoint;
                currentValues[1] = currentValues[1] - zeroValues.yPoint;
                currentValues[2] = currentValues[2] - zeroValues.zPoint;
            }

            sensorReadingsView.addValues(currentValues);
            sensorReadingsView.invalidate();

            double intensity = Math.sqrt(Math.pow(currentValues[0], 2)+Math.pow(currentValues[1], 2)+Math.pow(currentValues[2], 2));

            axisValuesTextView.setText("X: " + Float.toString(currentValues[0]) + "\n" +
                    "Y: " + Float.toString(currentValues[1]) + "\n" +
                    "Z: " + Float.toString(currentValues[2]) + "\n" +
                    "Intensity: " + intensity);

            setPreviousValues(currentValues);
        }

    }

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
            zeroValues = new MagPoint(xTotal/arraySize, yTotal/arraySize, xTotal/arraySize);
            zeroValuesBuffer.clear();

            return true;
        }

        return false;
    }

    public void setPreviousValues(float[] previousValuesArray)
    {
        if(previousValues == null)
        {
            previousValues = new MagPoint(previousValuesArray);
        }
        else
        {
            previousValues.xPoint = previousValuesArray[0];
            previousValues.yPoint = previousValuesArray[1];
            previousValues.zPoint = previousValuesArray[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}
