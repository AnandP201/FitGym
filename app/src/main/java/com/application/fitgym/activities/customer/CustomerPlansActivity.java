package com.application.fitgym.activities.customer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.application.fitgym.R;

public class CustomerPlansActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_plans);
        toolbar=findViewById(R.id.customer_plans_activity_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("My Plans");
    }
}