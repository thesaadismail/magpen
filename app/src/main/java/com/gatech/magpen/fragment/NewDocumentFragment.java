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
import android.widget.Button;
import android.widget.Toast;

import com.gatech.magpen.R;
import com.gatech.magpen.helper.MagPoint;
import com.gatech.magpen.util.MagPenUtils;
import com.gatech.magpen.view.DrawingView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by sismail on 11/2/14.
 */
public class NewDocumentFragment extends Fragment implements SensorEventListener {

    private enum CalibrationState {
        None,
        TopLeft,
        TopRight,
        BottomLeft,
        BottomRight,
        Done
    };

    private CalibrationState currentCalibrationState;

    //Parent Activity
    private ActionBarActivity parentActivity;

    //Views
    @InjectView(R.id.newDocumentDrawingView)
    public DrawingView documentDrawingView;

    @InjectView(R.id.calibrationButton)
    public Button calibrationButton;

    //Sensors
    private SensorManager mSensorManager;
    private Sensor magnetometer;

    //Sensor Readings
    //this value will be determined using the 4 corners
    private MagPoint lastKnownPenValue;

    //these value will be raw values from the magnetometer
    private MagPoint lastKnownMagValue;
    private MagPoint topLeftMagPoint;
    private MagPoint topRightMagPoint;
    private MagPoint bottomLeftMagPoint;
    private MagPoint bottomRightMagPoint;

    private float colorMinValue;
    private float colorMaxValue;

    private Menu actionBarMenu;

    //Misc
    private boolean penInputEnabled;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        parentActivity = (ActionBarActivity)getActivity();

        lastKnownMagValue = new MagPoint(0,0,0);
        lastKnownPenValue = new MagPoint(0,0,0);
        currentCalibrationState = CalibrationState.None;

        View rootView = inflater.inflate(R.layout.fragment_new_document, container, false);
        ButterKnife.inject(this, rootView);

        setHasOptionsMenu(true);
        parentActivity.getSupportActionBar().setHomeButtonEnabled(true);
        parentActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSensorManager = (SensorManager)parentActivity.getSystemService(Context.SENSOR_SERVICE);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        return rootView;
    }

    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.new_document, menu);
        actionBarMenu = menu;
        actionBarMenu.findItem(R.id.action_color_chooser).setVisible(false);
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
                if(currentCalibrationState == CalibrationState.Done)
                {
                   togglePenInput(false);
                   return true;
                }
                else
                {
                    Toast.makeText(getActivity(),
                            "Cannot enable pen input till the magnetometer has been calibrated. ",
                            Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.action_color_chooser:
                if(currentCalibrationState == CalibrationState.Done)
                {

                }
                else
                {
                    Toast.makeText(getActivity(),
                            "Magnetometer must be calibrated before the color chooser can be used. ",
                            Toast.LENGTH_LONG).show();
                }

        }

        return false;
    }

    public void togglePenInput(boolean forceDisable)
    {
        MenuItem item = (MenuItem) actionBarMenu.findItem(R.id.action_magnet_input_toggle);

        if(forceDisable)
        {
            penInputEnabled = false;
        }
        else
        {
            penInputEnabled = !penInputEnabled;
        }


        if(penInputEnabled)
        {
            item.setIcon(R.drawable.ic_edit_white_24dp);
            documentDrawingView.penDown(lastKnownPenValue);
        }
        else
        {
            item.setIcon(R.drawable.ic_edit_grey600_24dp);
            documentDrawingView.penUp();
        }
    }


    @OnClick(R.id.calibrationButton)
    public void calibrationButtonTapped(Button button)
    {
        if(currentCalibrationState == CalibrationState.None || currentCalibrationState == CalibrationState.Done) {
            togglePenInput(true);
            currentCalibrationState = CalibrationState.TopLeft;
            button.setText("Calibrate Top Left Position");
        }
        else if(currentCalibrationState == CalibrationState.TopLeft) {
            topLeftMagPoint = new MagPoint(lastKnownMagValue.toFloatArray());
            currentCalibrationState = CalibrationState.TopRight;
            button.setText("Calibrate Top Right Position");
        }
        else if(currentCalibrationState == CalibrationState.TopRight) {
            topRightMagPoint = new MagPoint(lastKnownMagValue.toFloatArray());
            currentCalibrationState = CalibrationState.BottomLeft;
            button.setText("Calibrate Bottom Left Position");
        }
        else if(currentCalibrationState == CalibrationState.BottomLeft) {
            bottomLeftMagPoint = new MagPoint(lastKnownMagValue.toFloatArray());
            currentCalibrationState = CalibrationState.BottomRight;
            button.setText("Calibrate Bottom Right Position");
        }
        else if(currentCalibrationState == CalibrationState.BottomRight) {
            bottomRightMagPoint = new MagPoint(lastKnownMagValue.toFloatArray());
            currentCalibrationState = CalibrationState.Done;
            button.setText("Calibration is done. Tap again to restart calibration.");
            colorMinValue = bottomRightMagPoint.magnitude();
            colorMaxValue = topLeftMagPoint.magnitude();
            actionBarMenu.findItem(R.id.action_color_chooser).setVisible(true);
        }

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }

    public void onSensorChanged(SensorEvent event) {
        float[] mGeomagnetic;

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
        {
            mGeomagnetic = event.values;

            float[] previousValues = lastKnownMagValue.toFloatArray();
            previousValues = MagPenUtils.lowPass(mGeomagnetic, previousValues);

            lastKnownMagValue.xPoint = previousValues[0];
            lastKnownMagValue.yPoint = previousValues[1];

            if(currentCalibrationState == CalibrationState.Done)
            {
                lastKnownPenValue = MagPenUtils.retrieveMagPoint(topLeftMagPoint, topRightMagPoint,
                        bottomLeftMagPoint, bottomRightMagPoint, lastKnownMagValue,
                        documentDrawingView.getWidth(), documentDrawingView.getHeight());

                if(penInputEnabled)
                {
                    documentDrawingView.penMove(lastKnownPenValue);
                }
            }
        }

    }

}
