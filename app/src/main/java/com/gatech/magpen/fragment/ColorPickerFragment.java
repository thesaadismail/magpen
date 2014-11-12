package com.gatech.magpen.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import butterknife.ButterKnife;
import com.gatech.magpen.R;
import com.gatech.magpen.view.ColorPickerView;

/**
 * Created by Brent on 11/11/2014.
 */
public class ColorPickerFragment extends DialogFragment {

    public interface ColorPickerListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    private ColorPickerListener mListener;
    ColorPickerView colorPickerView;

    private float currentColorValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            mListener = (ColorPickerListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement the ColorPickerListener Interface");
        }

        currentColorValue = 0.0f;

    }

    public Dialog onCreateDialog(Bundle savedInstanceState){

        View rootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_color_picker,null,false);
        ButterKnife.inject(this, rootView);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("message")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogPositiveClick(ColorPickerFragment.this);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogNegativeClick(ColorPickerFragment.this);
                    }
                })
                .setView(rootView);
        colorPickerView = (ColorPickerView) rootView.findViewById(R.id.color_picker);
        return builder.create();
    }

    public int getColor() {
        return convertValueToColor(currentColorValue);
    }

    private int convertValueToColor(float val){
        float red,green,blue;

        if(val >= 50.0f){
            red = 0.0f;
            green = 255.0f - ((val - 50.0f)/50.0f)*255.0f;
            blue = ((val - 50.0f)/50.0f)*255.0f;
        }
        else{
            red = ((50.0f - val)/50.0f)*255.0f;
            green = 255.0f - ((50.0f - val)/50.0f)*255.0f;
            blue = 0.0f;
        }

        return Color.rgb((int)red,(int)green,(int)blue);

    }

    public void setCurrentColorValue(float value) {
        currentColorValue = value;
        if(colorPickerView != null) {
            colorPickerView.setProgress((int) value);
            colorPickerView.invalidate();
        }
    }

}
