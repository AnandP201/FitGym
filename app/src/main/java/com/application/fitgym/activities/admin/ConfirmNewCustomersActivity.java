package com.application.fitgym.activities.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

import com.application.fitgym.R;

public class ConfirmNewCustomersActivity extends AppCompatActivity {

    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_new_customers);
        toolbar=findViewById(R.id.admin_cnfnewcust_toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Registration actions");
    }
}