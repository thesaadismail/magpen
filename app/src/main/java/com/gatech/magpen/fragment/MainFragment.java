package com.gatech.magpen.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gatech.magpen.R;
import com.gatech.magpen.view.FloatingActionButton;

import butterknife.ButterKnife;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";
    private ActionBarActivity parentActivity;
    private FloatingActionButton fabButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, rootView);

        parentActivity = (ActionBarActivity)getActivity();

        fabButton = new FloatingActionButton.Builder(parentActivity)
                .withDrawable(getResources().getDrawable(R.drawable.ic_note_add_grey600_24dp))
                .withButtonColor(Color.parseColor("#e5f5f9"))
                .withGravity(Gravity.BOTTOM | Gravity.RIGHT)
                .withMargins(0, 0, 16, 16)
                .create();

        fabButton.setOnClickListener(new CreateNewDocumentOnClickListener());

        return rootView;
    }

    class CreateNewDocumentOnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.container, new NewDocumentFragment());
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fabButton.setVisibility(View.VISIBLE);
        parentActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        fabButton.setVisibility(View.GONE);
    }

}