package com.gatech.magpen.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.gatech.magpen.R;
import com.gatech.magpen.view.StrokeWidthView;

import butterknife.ButterKnife;

/**
 * Created by Brent on 11/13/2014.
 */
public class StrokeWidthFragment extends DialogFragment {

    // Interface for dialog callbacks
    public interface StrokeWidthListener {
        public void onStrokePositiveClick(DialogFragment dialog);
        public void onStrokeNegativeClick(DialogFragment dialog);
    }

    private StrokeWidthListener mListener;

    private StrokeWidthView strokeWidthView;

    private float currentStrokeValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make sure calling fragment uses the listener interface
        try{
            mListener = (StrokeWidthListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement the ColorPickerListener Interface");
        }

        currentStrokeValue = 0.0f;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_stroke_width,null,false);
        ButterKnife.inject(this, rootView);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),android.R.style.Theme_Holo_Light_Panel);
        builder//.setMessage("Stroke Width")
                // Confirm selected
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onStrokePositiveClick(StrokeWidthFragment.this);
                    }
                })
                // Cancel selected
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onStrokeNegativeClick(StrokeWidthFragment.this);
                    }
                })
                // Set Color Picker View as the content
                .setView(rootView);
        strokeWidthView = (StrokeWidthView) rootView.findViewById(R.id.stroke_width);
        return builder.create();
    }

    private float convertValueToWidth(float val){

        if(val > 100) return 100.0f;
        if(val < 0) return 0.0f;

        return 3.0f + 20.0f * (val/100.0f);

    }

    public float getStrokeWidth(){
        return convertValueToWidth(currentStrokeValue);
    }

    // SETTER (also sets value in ColorPickerView)
    public void setCurrentStrokeValue(float value,int color) {
        currentStrokeValue = value;
        if(strokeWidthView != null) {
            strokeWidthView.setProgress((int) value,color);
            strokeWidthView.invalidate();
        }
    }

}
