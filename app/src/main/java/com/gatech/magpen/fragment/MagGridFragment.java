package com.gatech.magpen.fragment;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.gatech.magpen.R;
import com.gatech.magpen.helper.MagPoint;

import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MagGridFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MagGridFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MagGridFragment extends Fragment implements MagReadingsFragment.MagReadingsCallback {

    //Parent Activity
    private ActionBarActivity parentActivity;

    private static final String TAG = "MagGridFragment";
    private Button generateRowsColsButton;
    private EditText numOfColsEditText;
    private EditText numOfRowsEditText;
    private TableLayout magGridTableLayout;
    private Button saveValuesButton;

    private MagPoint currentMagValue;

    private MagPoint[][] magValueGrid;

    private int numOfRows;
    private int numOfCols;

    //==================================
    //     View Lifecycle Methods
    //==================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_mag_grid, container, false);

        parentActivity = (ActionBarActivity)getActivity();

        setHasOptionsMenu(true);
        parentActivity.getSupportActionBar().setHomeButtonEnabled(true);
        parentActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        magGridTableLayout = (TableLayout) rootView.findViewById(R.id.magGridTableLayout);

        numOfColsEditText = (EditText) rootView.findViewById(R.id.numOfColsEditText);
        numOfRowsEditText = (EditText) rootView.findViewById(R.id.numOfRowsEditText);

        saveValuesButton = (Button) rootView.findViewById(R.id.saveValuesButton);
        saveValuesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logGridValues();
            }
        });

        generateRowsColsButton = (Button) rootView.findViewById(R.id.generateButton);
        generateRowsColsButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                numOfRows = Integer.parseInt(numOfRowsEditText.getText().toString());
                numOfCols = Integer.parseInt(numOfColsEditText.getText().toString());

                magValueGrid = new MagPoint[numOfRows][numOfCols];

                saveValuesButton.setVisibility(View.VISIBLE);

                generateRowsAndColsOnClick();
            }
        });

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        MagReadingsFragment magReadingsFragment = MagReadingsFragment.newInstance(true);
        magReadingsFragment.magReadingsCallback = this;

        fragmentTransaction.add(magReadingsFragment, "magReadingsFragment_in_magGridFragment");
        fragmentTransaction.commit();

        // Inflate the layout for this fragment
        return rootView;
    }

    //==================================
    //     Grid Row Generation
    //==================================

    private void generateRowsAndColsOnClick()
    {
        magGridTableLayout.removeAllViews();

        TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.WRAP_CONTENT);

        android.widget.TableRow.LayoutParams buttonParams =
                new TableRow.LayoutParams(android.widget.TableRow.LayoutParams.WRAP_CONTENT,
                        android.widget.TableRow.LayoutParams.WRAP_CONTENT);

        for(int row = 0; row < numOfRows; row++)
        {
            TableRow tableRow = new TableRow(this.getActivity());
            tableRow.setLayoutParams(tableRowParams);

            for(int col = 0; col < numOfCols; col++)
            {
                Button cellButton = new Button(this.getActivity());
                cellButton.setLayoutParams(buttonParams);
                cellButton.setText(row+" x "+col);

                GridCellButtonListener buttonListener = new GridCellButtonListener(row, col);
                cellButton.setOnClickListener(buttonListener);

                tableRow.addView(cellButton);
            }

            magGridTableLayout.addView(tableRow);
        }

        magGridTableLayout.requestLayout();
    }

    //==================================
    //     Save Grid Values
    //==================================

    private void logGridValues(){
        int numOfValues = numOfCols * numOfRows;

        float[] xValues = new float[numOfValues];
        float[] yValues = new float[numOfValues];
        float[] zValues = new float[numOfValues];

        for(int row = 0; row<numOfRows; row++)
        {
            for(int col = 0; col<numOfCols; col++)
            {
                MagPoint magValue = magValueGrid[row][col];
                if(magValue!=null)
                {
                    int position = (numOfCols*row)+col;
                    xValues[position] = magValue.xPoint;
                    yValues[position] = magValue.yPoint;
                    zValues[position] = magValue.zPoint;
                }
            }
        }


        String xValuesStr = "x = ["+convertFloatArrayToCSV(xValues)+"]";
        String yValuesStr = "y = ["+convertFloatArrayToCSV(yValues)+"]";
        String zValuesStr = "z = ["+convertFloatArrayToCSV(zValues)+"]";

        Log.d(TAG, xValuesStr);
        Log.d(TAG, yValuesStr);
        Log.d(TAG, zValuesStr);
    }

    private String convertFloatArrayToCSV(float[] array)
    {
        StringBuffer csv = new StringBuffer();
        for(int i = 0; i<array.length; i++)
        {
            csv.append(array[i]);

            if(i+1 < array.length)
            {
                csv.append(",");
            }
        }
        return csv.toString();
    }

    //==================================
    //  Action Bar Menu Items Selected
    //==================================

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

    //==================================
    //     On Click Listener
    //==================================

    private class GridCellButtonListener implements Button.OnClickListener
    {
        private int row;
        private int col;

        public GridCellButtonListener(int row, int col)
        {
            this.row = row;
            this.col = col;
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "Button in Grid was Clicked. (" + row + "," + col + ")");
            Log.d(TAG, "MagValue: "+currentMagValue);
            ((Button)v).setText(row + " √ " + col);
            magValueGrid[row][col] = currentMagValue;
        }
    }

    //==================================
    //     MagReadingsCallback
    //==================================

    @Override
    public void onSensorChange(float[] processedMagReadings) {
        currentMagValue = new MagPoint(processedMagReadings);
    }



}
