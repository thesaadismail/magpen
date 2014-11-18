package com.gatech.magpen.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.gatech.magpen.R;
import com.gatech.magpen.view.FloatingActionButton;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";
    private ActionBarActivity parentActivity;
    private FloatingActionButton fabButton;

    @InjectView(R.id.documentsListEmptyTextView)
    public TextView documentsListEmptyTextView;

    @InjectView(R.id.magpen_gallery_scrollview)
    public ScrollView magpenGalleryScrollView;

    @InjectView(R.id.magpen_gallery)
    public LinearLayout magpenGallery;


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

        refreshGallery();

        return rootView;
    }

    public void refreshGallery()
    {
        magpenGallery.removeAllViews();

        File targetDirector = getActivity().getFilesDir();
        //Toast.makeText(getActivity(), targetDirector.getAbsolutePath(), Toast.LENGTH_LONG).show();

        File[] files = targetDirector.listFiles();
        if(files.length > 0)
        {
            magpenGalleryScrollView.setVisibility(View.VISIBLE);
            documentsListEmptyTextView.setVisibility(View.GONE);
            for (File file : files)
            {
                magpenGallery.addView(insertPhoto(file.getAbsolutePath()));
            }
        }
        else
        {
            magpenGalleryScrollView.setVisibility(View.GONE);
            documentsListEmptyTextView.setVisibility(View.VISIBLE);
        }
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

    View insertPhoto(String path){
        Bitmap bm = decodeSampledBitmapFromUri(path, 220, 220);

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setLayoutParams(new LinearLayout.LayoutParams(250, 250));
        layout.setGravity(Gravity.CENTER);

        ImageView imageView = new ImageView(getActivity());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(220, 220));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(bm);

        layout.addView(imageView);
        return layout;
    }

    public Bitmap decodeSampledBitmapFromUri(String path, int reqWidth, int reqHeight) {
        Bitmap bm = null;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(path, options);

        return bm;
    }

    public int calculateInSampleSize(

            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float)height / (float)reqHeight);
            } else {
                inSampleSize = Math.round((float)width / (float)reqWidth);
            }
        }

        return inSampleSize;
    }

}