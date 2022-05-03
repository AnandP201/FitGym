package com.application.fitgym.activities.customer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.application.fitgym.BuyPlanInterface;
import com.application.fitgym.R;
import com.application.fitgym.adapters.PacksAdapters;
import com.application.fitgym.helpers.GymApplication;
import com.application.fitgym.models.customers;
import com.application.fitgym.models.plans;

import org.bson.Document;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.mongodb.App;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.sync.SyncConfiguration;

public class CustomerPlansActivity extends AppCompatActivity implements BuyPlanInterface {

    Toolbar toolbar;
    RecyclerView recyclerView;
    App app;
    User user;
    Realm plansRealm,customerRealm,billsRealm;
    RealmResults<plans> plansList;
    RealmResults<customers> customerList;
    RealmChangeListener<RealmResults<plans>> listener;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_plans);
        toolbar=findViewById(R.id.customer_plans_activity_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("My Plans");

        progressBar=findViewById(R.id.customer_plans_loading_bar);
        recyclerView=findViewById(R.id.customer_plans_recycler_view);
        StaggeredGridLayoutManager staggeredGridLayoutManager=new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
    }


    @Override
    protected void onStart() {
        super.onStart();

        app= GymApplication.getGlobalAppInstance();
        user=app.currentUser();

        if(user!=null){
            plansRealm=Realm.getInstance(new SyncConfiguration
                    .Builder(user,"subscription")
                    .allowWritesOnUiThread(true)
                    .allowQueriesOnUiThread(true)
                    .build());
             plansList=plansRealm.where(plans.class).findAll();

             customerRealm=Realm.getInstance(new SyncConfiguration
                     .Builder(user,"users")
                     .allowWritesOnUiThread(true)
                     .allowQueriesOnUiThread(true)
                     .build());

        }

        if(plansList.size()>0){
            progressBar.setVisibility(View.INVISIBLE);
            PacksAdapters packsAdapters=new PacksAdapters(plansList,this);
            recyclerView.setAdapter(packsAdapters);
        }

        listener= res->{
           refresh();
        };
        plansList.addChangeListener(listener);
    }

    private void refresh(){
        progressBar.setVisibility(View.INVISIBLE);
        plansList=plansRealm.where(plans.class).findAll();
        PacksAdapters packsAdapters=new PacksAdapters(plansList,this);
        recyclerView.setAdapter(packsAdapters);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        plansList.removeChangeListener(listener);

        plansRealm.close();
    }

    @Override
    public void buyPlan(int position) {



        Toast.makeText(this, plansList.get(position).getPlanID()+" clicked !", Toast.LENGTH_SHORT).show();
    }


}