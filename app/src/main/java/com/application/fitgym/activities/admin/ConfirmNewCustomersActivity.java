package com.application.fitgym.activities.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.application.fitgym.R;
import com.application.fitgym.adapters.ToRegisterCustomersAdapter;
import com.application.fitgym.helpers.GymApplication;
import com.application.fitgym.models.customers;
import com.application.fitgym.models.resources;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.mongodb.App;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.SyncConfiguration;

public class ConfirmNewCustomersActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Realm customerRealm,resourceRealm;
    private SyncConfiguration configuration,resourceConfiguration;
    private App app;
    private User admin;
    private RealmResults<customers> customersList;
    private RealmResults<resources> resourcesList;
    private RecyclerView recyclerView;
    private ProgressBar loadingBar;
    private TextView infoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_new_customers);
        toolbar=findViewById(R.id.admin_cnfnewcust_toolbar);
        app= GymApplication.getGlobalAppInstance();
        admin=app.currentUser();
        loadingBar=findViewById(R.id.new_customers_loading);
        infoText=findViewById(R.id.new_customers_text_info);
        setSupportActionBar(toolbar);
        recyclerView=findViewById(R.id.new_customers_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        getSupportActionBar().setTitle("Registration actions");
    }


    @Override
    protected void onStart() {
        super.onStart();

        configuration=new SyncConfiguration
                .Builder(admin,"users")
                .allowQueriesOnUiThread(true)
                .allowWritesOnUiThread(true)
                .build();
        resourceConfiguration=new SyncConfiguration
                .Builder(admin,"data")
                .allowQueriesOnUiThread(true)
                .allowWritesOnUiThread(true)
                .build();

        resourceRealm=Realm.getInstance(resourceConfiguration);
        customerRealm=Realm.getInstance(configuration);

        customersList=customerRealm.where(customers.class).contains("RegistrationStatus","NA").findAll();
        resourcesList=resourceRealm.where(resources.class).findAll();

        infoText.setVisibility(View.INVISIBLE);

        if(customersList.size()==0){
            loadingBar.setVisibility(View.INVISIBLE);
            infoText.setVisibility(View.VISIBLE);
        }else{
            loadingBar.setVisibility(View.INVISIBLE);
        }

        ToRegisterCustomersAdapter customersAdapter=new ToRegisterCustomersAdapter(customersList,resourcesList,this);
        recyclerView.setAdapter(customersAdapter);




        customersAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                refreshAdapter();
            }
        });

        customersList.addChangeListener((customers ,changeSet)->{
            customersAdapter.notifyDataSetChanged();
        });


    }

    private void refreshAdapter(){
        customersList=customerRealm.where(customers.class).contains("RegistrationStatus","NA").findAll();
        resourcesList=resourceRealm.where(resources.class).findAll();

        if(customersList.size()==0){
            loadingBar.setVisibility(View.INVISIBLE);
            infoText.setVisibility(View.VISIBLE);
        }
        else{
            loadingBar.setVisibility(View.INVISIBLE);
        }

        ToRegisterCustomersAdapter newAdapter=new ToRegisterCustomersAdapter(customersList,resourcesList,this);
        recyclerView.setAdapter(newAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        customerRealm.close();
        resourceRealm.close();
    }

    @Override
    protected void onPause() {
        super.onPause();

        customerRealm.close();
        resourceRealm.close();

    }
}

