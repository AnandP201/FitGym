package com.application.fitgym.activities.customer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

import com.application.fitgym.R;

public class PeersActivity extends AppCompatActivity {

    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peers);
        toolbar=findViewById(R.id.peers_activity_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Fitgym Members");
    }
}