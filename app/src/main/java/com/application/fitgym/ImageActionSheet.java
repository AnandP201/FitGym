package com.application.fitgym;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class ImageActionSheet extends BottomSheetDialogFragment{


    LinearLayout addImage,deleteImage;
    ImageUploadInterface imageUploadInterface;


    public ImageActionSheet(ImageUploadInterface imageUploadInterfaceActivity){
        this.imageUploadInterface=imageUploadInterfaceActivity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.photo_action_layout,container,false);

       addImage=v.findViewById(R.id.upload_picture_button);
       deleteImage=v.findViewById(R.id.delete_picture_button);

       addImage.setOnClickListener(view->{
           imageUploadInterface.startImageAddActivityForResult();
           dismiss();
       });


       deleteImage.setOnClickListener(view->{
           imageUploadInterface.callDeleteConfirmationDialog();
           dismiss();
       });

        return v;
    }

}
