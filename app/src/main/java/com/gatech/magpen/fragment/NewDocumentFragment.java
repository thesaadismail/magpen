package com.gatech.magpen.fragment;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by sismail on 11/2/14.
 */
public class NewDocumentFragment extends Fragment implements SensorEventListener, ColorPickerFragment.ColorPickerListener,StrokeWidthFragment.StrokeWidthListener {

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
    //private float baselineIntensity;
    private Queue<MagPoint> previousMagPointsBuffer;

    // Color Picker Dialog
    private ColorPickerFragment colorPickerDialog;
    private float colorMinValue;
    private float colorMaxValue;
    private boolean choosingColor;
    private int currentColor = 0xFF660000;

    private StrokeWidthFragment strokeWidthDialog;
    private float strokeMinValue;
    private float strokeMaxValue;
    private boolean choosingStroke;

    private Menu actionBarMenu;

    //Misc
    private boolean penInputEnabled;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        parentActivity = (ActionBarActivity)getActivity();

        lastKnownMagValue = new MagPoint(0,0,0);
        lastKnownPenValue = new MagPoint(0,0,0);
        previousMagPointsBuffer = new LinkedList<MagPoint>();
        currentCalibrationState = CalibrationState.None;

        View rootView = inflater.inflate(R.layout.fragment_new_document, container, false);
        ButterKnife.inject(this, rootView);

        setHasOptionsMenu(true);
        parentActivity.getSupportActionBar().setHomeButtonEnabled(true);
        parentActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSensorManager = (SensorManager)parentActivity.getSystemService(Context.SENSOR_SERVICE);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        colorPickerDialog = new ColorPickerFragment();
        colorPickerDialog.setTargetFragment(this,0);
        choosingColor = false;

        strokeWidthDialog = new StrokeWidthFragment();
        strokeWidthDialog.setTargetFragment(this,0);
        choosingStroke = false;

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
        actionBarMenu.findItem(R.id.action_stroke_chooser).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                getFragmentManager().popBackStack();
                return true;
            case R.id.action_save_drawing:
                saveDrawing();
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
                    colorPickerDialog.show(getFragmentManager(),"dialog");
                    choosingColor = true;
                    return true;
                }
                else
                {
                    Toast.makeText(getActivity(),
                            "Magnetometer must be calibrated before the color chooser can be used. ",
                            Toast.LENGTH_LONG).show();
                    return false;
                }
            case R.id.action_stroke_chooser:
                if(currentCalibrationState == CalibrationState.Done)
                {
                    strokeWidthDialog.show(getFragmentManager(),"dialog");
                    choosingStroke = true;
                    return true;
                }
                else
                {
                    Toast.makeText(getActivity(),
                            "Magnetometer must be calibrated before the stroke chooser can be used. ",
                            Toast.LENGTH_LONG).show();
                    return false;
                }

        }

        return false;
    }

    public void saveDrawing() {
        boolean success = false;
        String currentDateandTime = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date());

        File file = new File(getActivity().getFileStreamPath("magpen-drawing-"+currentDateandTime+".png")
                .getPath());

        if (!file.exists()) {
            try {
                success = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileOutputStream outputStream = null;
        try
        {
            outputStream = new FileOutputStream(file);

            System.out.println(outputStream);

            Bitmap drawingViewBitmap = documentDrawingView.getBitmap();
            Bitmap bitmapToBeSaved = Bitmap.createBitmap(drawingViewBitmap.getWidth(),
                    drawingViewBitmap.getHeight(), Bitmap.Config.ARGB_8888);

            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            Canvas currentCanvas = new Canvas(bitmapToBeSaved);

            //draw white rectangle in the background
            currentCanvas.drawRect(new Rect(0, 0, drawingViewBitmap.getWidth(),
                    drawingViewBitmap.getHeight()), paint);

            //draw the actual bitmap on the canvas
            currentCanvas.drawBitmap(drawingViewBitmap,
                    new Rect(0, 0, drawingViewBitmap.getWidth(), drawingViewBitmap.getHeight()),
                    new Rect(0, 0, drawingViewBitmap.getWidth(), drawingViewBitmap.getHeight()), null);
;

            //error checking
            if (bitmapToBeSaved == null)
            {
                Log.d("NewDocument", "bitMapToBeSavedIsNull");
            }

            //save bitmap to the file
            bitmapToBeSaved.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        } catch (NullPointerException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "NullPointerException", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "FileNotFoundException", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "IOException", Toast.LENGTH_SHORT).show();
        }
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
            strokeMinValue = bottomRightMagPoint.magnitude();
            strokeMaxValue = topLeftMagPoint.magnitude();
            actionBarMenu.findItem(R.id.action_color_chooser).setVisible(true);
            actionBarMenu.findItem(R.id.action_stroke_chooser).setVisible(true);
        }

    }

    private boolean initialThrowAway = false;
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
            lastKnownMagValue.zPoint = previousValues[2];

            if(previousMagPointsBuffer.size() == 10)
            {

                previousMagPointsBuffer.remove();
                previousMagPointsBuffer.add(new MagPoint(event.values));

                //single click is disabled
                boolean singleClickDetected = false;//MagPenUtils.runSingleClickDetection((List)previousMagPointsBuffer);
                boolean doubleClickDetected = MagPenUtils.runDoubleClickDetection((List)previousMagPointsBuffer);

                if(doubleClickDetected)
                {
                    if(penInputEnabled)
                    {
                        Toast.makeText(parentActivity, "Disabling Pen Input", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(parentActivity, "Enabling Pen Input", Toast.LENGTH_SHORT).show();
                    }
                    togglePenInput(false);
                    previousMagPointsBuffer.clear();
                }
                else if(singleClickDetected)
                {
                    Toast.makeText(parentActivity, "Single Click Detected", Toast.LENGTH_SHORT).show();
                    togglePenInput(false);
                    previousMagPointsBuffer.clear();
                }
            }
            else
            {
                previousMagPointsBuffer.add(new MagPoint(event.values));
            }

            if(currentCalibrationState == CalibrationState.Done)
            {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(parentActivity);
                int algoTypeSetting = Integer.parseInt(prefs.getString("pref_interpolation_algorithm_type", "0"));

                lastKnownPenValue = MagPenUtils.retrieveMagPoint(algoTypeSetting, topLeftMagPoint, topRightMagPoint,
                        bottomLeftMagPoint, bottomRightMagPoint, lastKnownMagValue,
                        documentDrawingView.getWidth(), documentDrawingView.getHeight());

                if(choosingColor){
                    float colorValue = 100.0f - ((lastKnownMagValue.magnitude() - colorMinValue) / (colorMaxValue - colorMinValue)) * 100.0f;
                    if(colorValue > 100.0f)
                        colorValue = 100.0f;
                    else if(colorValue < 1.0f)
                        colorValue = 1.0f;
                    colorPickerDialog.setCurrentColorValue(colorValue);
                }
                else if(choosingStroke){
                    float strokeValue = 100.0f - ((lastKnownMagValue.magnitude() - strokeMinValue) / (strokeMaxValue - strokeMinValue)) * 100.0f;
                    if(strokeValue > 100.0f)
                        strokeValue = 100.0f;
                    else if(strokeValue < 0.0f)
                        strokeValue = 0.0f;
                    strokeWidthDialog.setCurrentStrokeValue(strokeValue,currentColor);
                }
                else if(penInputEnabled)
                {
                    documentDrawingView.penMove(lastKnownPenValue);
                }
            }
        }

    }

    // Callback from Color Picker dialog (confirm selected)
    @Override
    public void onDialogPositiveClick(DialogFragment dialog){
        documentDrawingView.getDrawPaint().setColor(((ColorPickerFragment) dialog).getColor());
        currentColor = ((ColorPickerFragment) dialog).getColor();
        Toast.makeText(getActivity(),
                "Paint Color Set",
                Toast.LENGTH_LONG).show();
        choosingColor = false;
    }

    // Callback from Color Picker dialog (Cancel selected)
    @Override
    public void onDialogNegativeClick(DialogFragment dialog){
        Toast.makeText(getActivity(),
                "Color Picker Canceled",
                Toast.LENGTH_LONG).show();
        choosingColor = false;
    }

    @Override
    public void onStrokePositiveClick(DialogFragment dialog){
        documentDrawingView.getDrawPaint().setStrokeWidth(((StrokeWidthFragment) dialog).getStrokeWidth());
        Toast.makeText(getActivity(),
                "Stroke Width Set",
                Toast.LENGTH_LONG).show();
        choosingStroke = false;
    }

    @Override
    public void onStrokeNegativeClick(DialogFragment dialog){
        Toast.makeText(getActivity(),
                "Stroke Picker Canceled",
                Toast.LENGTH_LONG).show();
        choosingStroke = false;
    }

}
