package com.gatech.magpen.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gatech.magpen.R;

import butterknife.ButterKnife;

/**
 * Created by sismail on 11/2/14.
 */
public class NewDocumentFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_new_document, container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }
}
