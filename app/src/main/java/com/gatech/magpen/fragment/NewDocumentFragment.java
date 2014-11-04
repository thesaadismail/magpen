package com.gatech.magpen.fragment;

import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.gatech.magpen.R;
import com.gatech.magpen.view.DrawingView;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by sismail on 11/2/14.
 */
public class NewDocumentFragment extends Fragment implements SensorEventListener {

    //Parent Activity
    private ActionBarActivity parentActivity;

    //Views
    @InjectView(R.id.newDocumentDrawingView)
    public DrawingView documentDrawingView;

    //Sensors
    private SensorManager mSensorManager;
    private Sensor magnetometer;

    //Sensor Readings
    private float lastKnownXValue;
    private float lastKnownYValue;

    //Misc
    private boolean penInputEnabled;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        parentActivity = (ActionBarActivity)getActivity();

        View rootView = inflater.inflate(R.layout.fragment_new_document, container, false);
        ButterKnife.inject(this, rootView);

        setHasOptionsMenu(true);
        parentActivity.getSupportActionBar().setHomeButtonEnabled(true);
        parentActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSensorManager = (SensorManager)parentActivity.getSystemService(Context.SENSOR_SERVICE);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        lastKnownXValue = 0;
        lastKnownYValue = 0;

        return rootView;
    }

    public void onResume() {
        super.onResume();
        if(penInputEnabled)
        {
            mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.new_document, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                getFragmentManager().popBackStack();
                return true;
            case R.id.action_magnet_input_toggle:
                penInputEnabled = !penInputEnabled;
                if(penInputEnabled)
                {
                    item.setIcon(R.drawable.ic_edit_white_24dp);
                    mSensorManager.unregisterListener(this);
                    mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
                    documentDrawingView.penDown(lastKnownXValue, lastKnownYValue);
                }
                else
                {
                    item.setIcon(R.drawable.ic_edit_grey600_24dp);
                    documentDrawingView.penUp();
                }
                return true;
        }

        return false;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }

    public void onSensorChanged(SensorEvent event) {
        float[] mGeomagnetic;

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
        {
            mGeomagnetic = event.values;
            lastKnownXValue = mGeomagnetic[0];
            lastKnownYValue = mGeomagnetic[1];
            if(penInputEnabled)
            {
                documentDrawingView.penMove(lastKnownXValue, lastKnownYValue);
            }
        }

    }

}
