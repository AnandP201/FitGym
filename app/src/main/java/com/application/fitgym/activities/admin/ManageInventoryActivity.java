package com.application.fitgym.activities.admin;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

import com.application.fitgym.R;

public class ManageInventoryActivity extends AppCompatActivity {


    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_inventory);
        toolbar=findViewById(R.id.admin_manageinv_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("FitGym Inventory");
    }
}