package com.gatech.magpen.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.gatech.magpen.R;
import com.gatech.magpen.view.FloatingActionButton;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainFragment extends Fragment {

    private ActionBarActivity parentActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, rootView);

        parentActivity = (ActionBarActivity)getActivity();

        FloatingActionButton fabButton = new FloatingActionButton.Builder(parentActivity)
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
            ft.add(R.id.container, new NewDocumentFragment());
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

}