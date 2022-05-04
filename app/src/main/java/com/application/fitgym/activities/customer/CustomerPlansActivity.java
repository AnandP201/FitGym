package com.application.fitgym.activities.customer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
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
import com.application.fitgym.models.payments;
import com.application.fitgym.models.plans;
import com.application.fitgym.models.status;

import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

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
    Realm plansRealm,customerRealm,billsRealm,statusRealm;
    RealmResults<plans> plansList;
    status currentMember;
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
             customerList=customerRealm.where(customers.class).findAll();

             billsRealm=Realm.getInstance(new SyncConfiguration
                     .Builder(user,"bills")
                     .allowQueriesOnUiThread(true)
                     .allowWritesOnUiThread(true)
                     .build());

             statusRealm=Realm.getInstance(new SyncConfiguration
                     .Builder(user,"members")
                     .allowWritesOnUiThread(true)
                     .allowQueriesOnUiThread(true)
                     .build());
        }

        if(plansList.size()>0 && customerList.size()>0 && !statusRealm.isEmpty()){
            progressBar.setVisibility(View.INVISIBLE);
            currentMember=statusRealm.where(status.class).equalTo("userAuthID",user.getId()).findFirst();
            PacksAdapters packsAdapters=new PacksAdapters(plansList,this,currentMember);
            recyclerView.setAdapter(packsAdapters);
        }

        listener= res->{
           refresh();
        };
        plansList.addChangeListener(listener);
    }

    private void refresh(){
        progressBar.setVisibility(View.INVISIBLE);
        currentMember=statusRealm.where(status.class).equalTo("userAuthID",user.getId()).findFirst();
        plansList=plansRealm.where(plans.class).findAll();
        PacksAdapters packsAdapters=new PacksAdapters(plansList,this,currentMember);
        recyclerView.setAdapter(packsAdapters);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        customerRealm.close();
        plansList.removeChangeListener(listener);

        plansRealm.close();
        billsRealm.close();
        statusRealm.close();
    }


    @Override
    protected void onPause() {
        super.onPause();

        customerRealm.close();
        plansList.removeChangeListener(listener);

        plansRealm.close();
        billsRealm.close();
        statusRealm.close();
    }

    private void modifyStatusPlans(String planID, status curr, String planDuration){
        String plans=curr.getActivePlans();
        if(plans.isEmpty()){
            statusRealm.executeTransaction(realm -> {
                curr.setActivePlans(String.format("[%s]",planID));
                String duration=String.valueOf(Integer.parseInt(curr.getPlanActiveDuration())+Integer.parseInt(planDuration));
                curr.setPlanActiveDuration(duration);
            });
        }
        else{
            List<String> list=new ArrayList<>();
            list.addAll(Arrays.asList(plans.substring(1,plans.length()-1).split(", ")));

            list.add(planID);
            statusRealm.executeTransaction(realm -> {
                curr.setActivePlans(list.toString());
                String duration=String.valueOf(Integer.parseInt(curr.getPlanActiveDuration())+Integer.parseInt(planDuration));
                curr.setPlanActiveDuration(duration);
            });
        }

    }

    @Override
    public void buyPlan(int position) {

        payments payment=new payments();
        status current=statusRealm.where(status.class).equalTo("userAuthID",user.getId()).findFirst();
        billsRealm.executeTransaction(realm -> {
            payment.set_partition();
            payment.setBillAmount(plansList.get(position).getPrice());
            payment.setBillFor(current.getGymUserID());
            payment.setBillInvoiceID(UUID.randomUUID().toString());
            payment.setBillTitle(plansList.get(position).getTitle());
            SimpleDateFormat sdf=new SimpleDateFormat("EE MM yy HH:mm:ss");
            Calendar calendar=Calendar.getInstance();
            payment.setCreatedOn(sdf.format(calendar.getTime()));

            billsRealm.insertOrUpdate(payment);
        });

        plans p=plansList.get(position);
        modifyStatusPlans(p.getPlanID(),current,p.getDuration());

        startActivity(new Intent(this,HomeActivity.class));
        finish();
        Toast.makeText(this, "Plan "+plansList.get(position).getPlanID()+" purchased!", Toast.LENGTH_SHORT).show();
    }


}