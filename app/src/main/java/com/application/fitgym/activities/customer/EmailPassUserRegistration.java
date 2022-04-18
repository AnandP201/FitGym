package com.application.fitgym.activities.customer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.application.fitgym.helpers.GymApplication;
import com.application.fitgym.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.mongodb.App;

public class EmailPassUserRegistration extends AppCompatActivity implements View.OnClickListener{

    private String email,password1,password2;
    private LinearLayout linearLayout;
    private EditText textEmail,textPassword1,textPassword2;
    private CheckBox showPasswordCheck;
    private Button registerButton;
    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_pass_user_registration);

        app= GymApplication.getGlobalAppInstance();

        textEmail=findViewById(R.id.email_text);
        textPassword1=findViewById(R.id.password_text);
        textPassword2=findViewById(R.id.password2_text);
        registerButton=findViewById(R.id.register_button_email_registration);
        showPasswordCheck=findViewById(R.id.show_password_check);
        linearLayout=findViewById(R.id.register_new_user_mainlayout);

        Toolbar toolbar=findViewById(R.id.app_bar_reg_form);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("About You");

        showPasswordCheck.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b){
                textPassword1.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                textPassword2.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
            else{
                textPassword1.setTransformationMethod(PasswordTransformationMethod.getInstance());
                textPassword2.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });

        registerButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.register_button_email_registration){
            email=textEmail.getText().toString();
            password1=textPassword1.getText().toString();
            password2=textPassword2.getText().toString();

            String regex="(?=.*?[#!@$%*]).{8,}$";

            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher matcher1 = pattern.matcher(password1);

            if(email.equalsIgnoreCase("")||password1.equalsIgnoreCase("")||password2.equalsIgnoreCase("")){
                Toast.makeText(this, "Please fill out every details!", Toast.LENGTH_SHORT).show();
            }
            else if(!password1.equals(password2)){
                Toast.makeText(this, "Passwords provided don't match !", Toast.LENGTH_SHORT).show();
            }else if(!matcher1.find()){
                Snackbar snackbar=Snackbar.make(this,linearLayout,"Password should have 8 characters and 1 special character out of #!@$%*", BaseTransientBottomBar.LENGTH_LONG);
                snackbar.show();
            }else{
                app.getEmailPassword().registerUserAsync(email,password1,res->{
                    if (res.isSuccess()){
                        Toast.makeText(EmailPassUserRegistration.this,"You are registered successfully! Login with your credentials !",Toast.LENGTH_LONG).show();

                        startActivity(new Intent(EmailPassUserRegistration.this, LoginSignUpActivity.class));
                        finish();
                    }
                });
            }



        }

    }
}