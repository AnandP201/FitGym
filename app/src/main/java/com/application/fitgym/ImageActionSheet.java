package com.application.fitgym;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContentInfo;
import android.view.LayoutInflater;
import android.view.OnReceiveContentListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.application.fitgym.models.customers;
import com.application.fitgym.models.resources;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;
import java.io.IOException;

import io.realm.Realm;
import io.realm.RealmResults;


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
