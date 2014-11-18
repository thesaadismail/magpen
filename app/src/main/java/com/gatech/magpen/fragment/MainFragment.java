package com.gatech.magpen.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    public HorizontalScrollView magpenGalleryScrollView;

    @InjectView(R.id.magpen_gallery)
    public LinearLayout magpenGallery;

    @InjectView(R.id.magpen_selected_drawing)
    public FrameLayout magpenSelectedDrawing;


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
            magpenSelectedDrawing.setVisibility(View.VISIBLE);
            documentsListEmptyTextView.setVisibility(View.GONE);
            for (File file : files)
            {
                magpenGallery.addView(createLayoutForPhoto(file.getAbsolutePath()));
            }
        }
        else
        {
            magpenGalleryScrollView.setVisibility(View.GONE);
            magpenSelectedDrawing.setVisibility(View.GONE);
            documentsListEmptyTextView.setVisibility(View.VISIBLE);
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

    View createLayoutForPhoto(String path){
        Bitmap bm = retrieveScaledBitmap(path, 220, 220);

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setLayoutParams(new LinearLayout.LayoutParams(250, 250));
        layout.setGravity(Gravity.CENTER);

        ImageView imageView = new ImageView(getActivity());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(220, 220));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(bm);
        imageView.setOnClickListener(new DrawingSelectedOnClickListener(path));

        layout.addView(imageView);
        return layout;
    }

    public Bitmap retrieveScaledBitmap(String path, int reqWidth, int reqHeight) {
        //decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateScaledSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        return bitmap;
    }

    public int calculateScaledSize(BitmapFactory.Options options, int requiredWidth, int requiredHeight) {
        // raw height and width of bitmap images
        final int height = options.outHeight;
        final int width = options.outWidth;
        int scaledSize = 1;

        if (height > requiredHeight || width > requiredWidth) {
            if (width > height)
            {
                scaledSize = Math.round((float)height / (float)requiredHeight);
            }
            else
            {
                scaledSize = Math.round((float)width / (float)requiredWidth);
            }
        }

        return scaledSize;
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
    class DrawingSelectedOnClickListener implements View.OnClickListener
    {
        String imagePath;

        public DrawingSelectedOnClickListener(String path)
        {
            imagePath = path;
        }

        public void onClick(View v)
        {
            magpenSelectedDrawing.removeAllViews();

            //get actual bitmap size
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Bitmap bmBounds = BitmapFactory.decodeFile(imagePath, options);

            //send the bitmap size to decoding sampled bitmap from Uri
            Bitmap bm = retrieveScaledBitmap(imagePath, options.outWidth,
                    options.outHeight);

            //save the bitmap in an image view
            final ImageView imageView = new ImageView(getActivity());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(options.outWidth,
                    options.outHeight));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageBitmap(bm);

            magpenSelectedDrawing.addView(imageView);
            magpenSelectedDrawing.invalidate();

        }
    }


}