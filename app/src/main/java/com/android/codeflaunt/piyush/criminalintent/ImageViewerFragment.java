package com.android.codeflaunt.piyush.criminalintent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by Piyush on 06-Mar-16.
 */
public class ImageViewerFragment extends DialogFragment {

    private static final String ARG_IMAGE = "image.crime";

    private ImageView mCrimeImage;

    public static ImageViewerFragment newInstance(File photoFile){
        Bundle args = new Bundle();
        args.putSerializable(ARG_IMAGE, photoFile);

        ImageViewerFragment fragment = new ImageViewerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState){

        File photoFile = (File) getArguments().getSerializable(ARG_IMAGE);
        Bitmap bitmap = PictureUtils.getScaledBitmap(
                photoFile.getPath(), getActivity());

        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_photo, null);

        mCrimeImage = (ImageView) v.findViewById(R.id.dialog_crime_image_view);
        mCrimeImage.setImageBitmap(bitmap);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .create();
    }
}
