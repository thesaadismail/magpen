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

    //Listeners
    private CompoundButton.OnCheckedChangeListener filterListener;
    private CompoundButton.OnCheckedChangeListener readingsListener;
    private View.OnClickListener zeroButtonListener;
    private View.OnClickListener calibrateButtonlistener;

    // Components
    private TextView tv;
    private CheckBox fBox;
    private CheckBox rBox;
    private Button zb;
    private Button cb;

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
        cb = (Button) rootView.findViewById(R.id.calibrate_button);

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

        calibrateButtonlistener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sensorReadingsView.calibrationState < 4){
                    sensorReadingsView.setCalibration(prev.clone());
                    switch(sensorReadingsView.calibrationState){
                        case 1:
                            cb.setText("Calibrate Top Right");
                            break;
                        case 2:
                            cb.setText("Calibrate Bottom Left");
                            break;
                        case 3:
                            cb.setText("Calibrate Bottom Right");
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
        fBox.setOnCheckedChangeListener(filterListener);
        rBox.setOnCheckedChangeListener(readingsListener);
        zb.setOnClickListener(zeroButtonListener);
        cb.setOnClickListener(calibrateButtonlistener);

    }

    @Override
    public void onStop() {
        mSensorManager.unregisterListener(this);
        fBox.setOnCheckedChangeListener(null);
        rBox.setOnCheckedChangeListener(null);
        zb.setOnClickListener(null);
        cb.setOnClickListener(null);
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
            prev = sensorReadingsView.lowPass(event.values.clone(),prev);
            sensorReadingsView.addValues(event.values.clone(), tv);
            sensorReadingsView.invalidate();
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    // Return the avg value of floats in the array
    private float avgArray(ArrayList<Float> in){

        float total = 0.0f;

        for(int i = 0; i < in.size(); i++){
            total += in.get(i);
        }

        return total/(float)in.size();

    }

}
