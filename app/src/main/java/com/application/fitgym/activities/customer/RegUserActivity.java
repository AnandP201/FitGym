package com.application.fitgym.activities.customer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.application.fitgym.helpers.GymApplication;
import com.application.fitgym.ImageActionSheet;
import com.application.fitgym.ImageUploadInterface;
import com.application.fitgym.R;
import com.application.fitgym.helpers.UserInfo;
import com.application.fitgym.models.customers;
import com.application.fitgym.models.resources;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.sync.SyncConfiguration;

public class RegUserActivity extends AppCompatActivity implements ImageUploadInterface {
    public static final int SELECT_PICTURE=200;

    private ImageView imageView;
    private TextView name_text, age_text, number_text,dialog_text_view;
    private TextView cancel,delete;
    private RadioGroup genderRadioGroup;
    private RadioButton maleRadio, femaleRadio;
    private Button registerBtn;
    private View dialogView,dpLayout,yes_no_confirmation_dialog_view;
    private Realm customerRealm,imageRealm;
    private FloatingActionButton addImageBtn;

    private AlertDialog alert,removeImageConfirmationDialog;
    private App app;
    ImageActionSheet imageActionSheet;
    private String name,age,gender,contact,authid;

    public final static String USER_OBJECT = "USER_OBJECT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_user);
        LayoutInflater inflater=this.getLayoutInflater();
        dialogView=inflater.inflate(R.layout.loading_dialog,null);
        dpLayout=inflater.inflate(R.layout.dp_layout,null);
        yes_no_confirmation_dialog_view=inflater.inflate(R.layout.yes_no_confirmation_dialog,null);

        imageView = findViewById(R.id.dp_image);
        name_text = findViewById(R.id.name_text);
        age_text = findViewById(R.id.age_text);
        number_text = findViewById(R.id.number_text);
        genderRadioGroup = findViewById(R.id.radioGroup);
        maleRadio = findViewById(R.id.male_radio);
        femaleRadio = findViewById(R.id.female_radio);
        registerBtn=findViewById(R.id.register_button);
        dialog_text_view=dialogView.findViewById(R.id.dialog_textbox);
        addImageBtn=findViewById(R.id.add_image_btn);
        cancel=yes_no_confirmation_dialog_view.findViewById(R.id.cancel_text_view);
        delete=yes_no_confirmation_dialog_view.findViewById(R.id.delete_text_view);

        app = GymApplication.getGlobalAppInstance();
        alert=new AlertDialog.Builder(this)
                .setView(dialogView).create();
        alert.setCancelable(false);

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        removeImageConfirmationDialog=builder.create();
        removeImageConfirmationDialog.setView(yes_no_confirmation_dialog_view);
        removeImageConfirmationDialog.setCancelable(false);



        if(app.currentUser()!=null){

            customerRealm=Realm.getInstance(new SyncConfiguration
                    .Builder(app.currentUser(), "users")
                    .allowWritesOnUiThread(true)
                    .allowQueriesOnUiThread(true)
                    .build());


            imageRealm=Realm.getInstance(new SyncConfiguration
                    .Builder(app.currentUser(), "data")
                    .allowQueriesOnUiThread(true)
                    .allowWritesOnUiThread(true)
                    .build());

            if (app.currentUser().getProviderType() == Credentials.Provider.GOOGLE) {
                UserInfo userInfo = (UserInfo) getIntent().getSerializableExtra(USER_OBJECT);

                String name = userInfo.getName();
                String imageURL = userInfo.getImageURL();

                name_text.setText(name != null ? name : "");
                loadImage(imageURL);
            }


        }


        Toolbar toolbar = findViewById(R.id.app_bar_reg);
        toolbar.setTitle("About You");
        setSupportActionBar(toolbar);

        imageActionSheet=new ImageActionSheet(RegUserActivity.this);

        cancel.setOnClickListener(view -> {
            removeImageConfirmationDialog.dismiss();
        });

        delete.setOnClickListener(view -> {
            resources res=imageRealm.where(resources.class).beginsWith("userID",app.currentUser().getId()).findFirst();
            if(res!=null){
                imageRealm.executeTransaction(realm->{
                    res.deleteFromRealm();
                });
                imageView.setImageDrawable(null);
            }
            else{
                imageView.setImageDrawable(null);
            }
            removeImageConfirmationDialog.dismiss();
            Toast.makeText(this, "Picture removed!", Toast.LENGTH_SHORT).show();
        });



        registerBtn.setOnClickListener(this::registerToDB);
        maleRadio.setOnClickListener(this::onRadioButtonClicked);
        femaleRadio.setOnClickListener(this::onRadioButtonClicked);
        addImageBtn.setOnClickListener(view -> {
                imageActionSheet.show(getSupportFragmentManager(),"ModalActionSheet");
        });


    }

    private void onRadioButtonClicked(View view){
        int radioId=genderRadioGroup.getCheckedRadioButtonId();
        switch (radioId){
            case -1:
                gender="";
                break;
            case R.id.female_radio:
                gender="Female";
                break;
            case R.id.male_radio:
                gender="Male";
                break;
        }
    }




    public void emptyFields(){
        name_text.setText("");
        age_text.setText("");
        genderRadioGroup.clearCheck();
        number_text.setText("");

        LinearLayout linearLayout=findViewById(R.id.mainLayout);

        String str=String.format("%s , you are all set-up!",name);

        Snackbar snackbar=Snackbar.make(linearLayout,str,BaseTransientBottomBar.LENGTH_SHORT);
        snackbar.setAction("CLOSE", view -> {
            snackbar.dismiss();
        });


        customerRealm.close();
        imageRealm.close();

        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void registerToDB(View view) {

        name=name_text.getText().toString();
        age=age_text.getText().toString();
        contact=number_text.getText().toString();


        if(name.equalsIgnoreCase("")||age.equalsIgnoreCase("")||contact.equalsIgnoreCase("")){
            Toast.makeText(this,"Please fill out every details!",Toast.LENGTH_LONG).show();
        }
        else{
            dialog_text_view.setText("Setting up your profile..");
            alert.show();
            authid=app.currentUser().getId();
            if(imageView.getDrawable()!=null){
                Bitmap photo=((RoundedBitmapDrawable)imageView.getDrawable()).getBitmap();
                ByteArrayOutputStream bos=new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG,100,bos);
                byte[] b=bos.toByteArray();

                imageStore(b);
            }
            else{
                byte[]b=new byte[0];
                imageStore(b);
            }

          customerRealm.executeTransaction(realm->{
                customers customer=new customers();
                customer.setAge(age);
                customer.setName(name);
                customer.setGender(gender);
                customer.setPhone(contact);
                customer.set_partition("users");
                customer.setAuthID(authid);
                customer.setRegistrationStatus("NA");
                customerRealm.insertOrUpdate(customer);
            });
            emptyFields();
        }
    }

    public void imageStore(byte[] binData){

        dialog_text_view.setText("Setting up your avatar.....");
        imageRealm.executeTransaction(realm->{
            resources res=new resources();
            res.setUserID(app.currentUser().getId());
            res.set_partition();
            res.setData(binData);
            realm.insertOrUpdate(res);
        });
    }

    public void loadImage(String URL) {
        new LoadImage().execute(URL);
    }

    @Override
    public void startImageAddActivityForResult() {
        Intent intent=new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        startActivityForResult(Intent.createChooser(intent,"Select picture"),SELECT_PICTURE);
    }

    @Override
    public void callDeleteConfirmationDialog() {
        removeImageConfirmationDialog.show();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        InputStream in=null;
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    try{
                        in= getContentResolver().openInputStream(selectedImageUri);
                    }
                    catch (Exception e){
                        Toast.makeText(this,"Some error occured! Try again!",Toast.LENGTH_LONG);
                    }
                    finally {
                        Bitmap bitmap=BitmapFactory.decodeStream(in);
                        RoundedBitmapDrawable roundedBitmapDrawable= RoundedBitmapDrawableFactory.create(getResources(),bitmap);
                        roundedBitmapDrawable.setCircular(true);

                        imageView.setImageDrawable(roundedBitmapDrawable);
                    }
                }
            }
        }
    }


    public class LoadImage extends AsyncTask<String,Void,Bitmap>{
        @Override
        protected Bitmap doInBackground(String... strings) {
            String imageURL=strings[0];
            Bitmap image=null;
            try {
                URL url = new URL(imageURL);
                InputStream in=url.openStream();
                image = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                new Handler().post(()->{
                    Toast.makeText(RegUserActivity.this,"Image cannot be loaded!",Toast.LENGTH_SHORT).show();
                });
            }
            return image;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            RoundedBitmapDrawable roundedBitmapDrawable= RoundedBitmapDrawableFactory.create(getResources(),bitmap);
            roundedBitmapDrawable.setCircular(true);
            imageView.setImageDrawable(roundedBitmapDrawable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        customerRealm.close();
        imageRealm.close();
        alert.dismiss();
    }
}