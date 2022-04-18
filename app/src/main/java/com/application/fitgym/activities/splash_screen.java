package com.application.fitgym.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.application.fitgym.R;
import com.application.fitgym.activities.admin.AdminActivity;
import com.application.fitgym.activities.customer.LoginSignUpActivity;

public class splash_screen extends AppCompatActivity {

    ImageView image;
    TextView title;
    Animation translate,logo_scale_fade;
    Handler loadMainActivityHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        image=findViewById(R.id.logo);
        title=findViewById(R.id.title);
        logo_scale_fade= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.logo_anims);
        translate=AnimationUtils.loadAnimation(getApplicationContext(),R.anim.title_anim);
        image.startAnimation(logo_scale_fade);
        title.startAnimation(translate);

        loadMainActivityHandler=new Handler();

        loadMainActivityHandler.postDelayed(() -> {
            Intent mainActivityIntent=new Intent(splash_screen.this, LoginSignUpActivity.class);
//            Intent mainActivityIntent=new Intent(splash_screen.this, AdminActivity.class);
            startActivity(mainActivityIntent);
            finish();
        },2000);
        }
    }
