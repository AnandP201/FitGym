package com.application.fitgym.activities.admin;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.application.fitgym.R;
import com.application.fitgym.activities.customer.LoginSignUpActivity;
import com.application.fitgym.adapters.AdminMenuAdapter;
import com.application.fitgym.helpers.dashboardItems.AdminDashMenuItems;
import com.application.fitgym.helpers.GymApplication;
import com.application.fitgym.models.admin;
import com.application.fitgym.models.customers;
import com.application.fitgym.models.plans;

import java.util.Arrays;
import java.util.List;


import io.realm.Realm;

import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.mongodb.App;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.SyncConfiguration;

public class AdminActivity extends AppCompatActivity {


    private Toolbar toolbar;
    private User adminUser;
    private Realm adminRealm,customerRealm,plansRealm;
    private AlertDialog alertDialog;
    private App app;
    private RealmResults<customers> customersList;
    private RealmResults<plans> plansList;
    private int unregisteredCount,registeredCount;
    RealmChangeListener<RealmResults<customers>> custCountListener;
    RealmChangeListener<RealmResults<plans>> plansCountListener;
    private RealmResults<admin> adminList;
    private SyncConfiguration adminSyncConfigurationFile,customerSyncConfigurationFile,plansSyncConfigurationFile;
    private TextView adminLogoutCancelView,adminLogoutView,adminNameTextView,totalCustomersCountTextView,newRegistrationCountTextView,plansCountTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        adminNameTextView=findViewById(R.id.admin_dashboard_adminnameText);
        totalCustomersCountTextView=findViewById(R.id.admin_dashboard_crcustomerscount_text);
        newRegistrationCountTextView=findViewById(R.id.admin_dashboard_newcustomerscount_text);
        plansCountTextView=findViewById(R.id.admin_dashboard_planscount_text);
        app= GymApplication.getGlobalAppInstance();


        View logoutConfirmationView=this.getLayoutInflater().inflate(R.layout.admin_logout_confirmation_dialog,null);
        toolbar=findViewById(R.id.admin_dashboard_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("DashBoard");


        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        List<AdminDashMenuItems> list=Arrays.asList(AdminDashMenuItems.dashBoardItems);
        GridView gridView=findViewById(R.id.gridView);
        AdminMenuAdapter adapter=new AdminMenuAdapter(AdminActivity.this,list);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener((adapterView, view, i, l) -> {
            switch(list.get(i).getAction()){
                case "customers":
                    startActivity(new Intent(this,ManageNewCustomersActivity.class));
                    break;
                case "plans":
                    startActivity(new Intent(this,ManagePlansActivity.class));
                    break;
                case "manage":
                    startActivity(new Intent(this,ManageCustomersActivity.class));
                    break;
                case "money":
                    startActivity(new Intent(this,ManageInventoryActivity.class));
                    break;
            }
        });

        alertDialog=dialog.setView(logoutConfirmationView).setCancelable(false).create();

        adminLogoutView=logoutConfirmationView.findViewById(R.id.admin_ok_alertdialog_btn);
        adminLogoutCancelView=logoutConfirmationView.findViewById(R.id.admin_cancel_alertdialog_btn);

        adminLogoutView.setOnClickListener(view -> {
            User user=app.currentUser();
            if(user!=null){
                user.logOutAsync(response->{
                    if(response.isSuccess()){
                        Toast.makeText(getApplicationContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AdminActivity.this, LoginSignUpActivity.class));
                        finish();
                    }
                });
            }
            else{
                alertDialog.dismiss();
            }
        });

        adminLogoutCancelView.setOnClickListener(view->{
            alertDialog.dismiss();
        });



    }

    @Override
    protected void onStart() {
        super.onStart();
        adminUser=app.currentUser();

        if(adminUser!=null){
            adminSyncConfigurationFile=new SyncConfiguration.Builder(adminUser,"admin")
                    .allowQueriesOnUiThread(true)
                    .allowWritesOnUiThread(true)
                    .build();
            customerSyncConfigurationFile=new SyncConfiguration.Builder(adminUser,"users")
                    .allowQueriesOnUiThread(true)
                    .allowWritesOnUiThread(true)
                    .build();

            plansSyncConfigurationFile=new SyncConfiguration.Builder(adminUser,"subscription")
                    .allowWritesOnUiThread(true)
                    .allowQueriesOnUiThread(true)
                    .build();


            adminRealm=Realm.getInstance(adminSyncConfigurationFile);
            customerRealm=Realm.getInstance(customerSyncConfigurationFile);
            plansRealm=Realm.getInstance(plansSyncConfigurationFile);

            customersList=customerRealm.where(customers.class).findAll();
            adminList=adminRealm.where(admin.class).findAll();
            plansList=plansRealm.where(plans.class).findAll();


            if(customersList.size()!=0){
                RealmQuery<customers>query1=customersList.where();
                registeredCount=(int)query1.contains("RegistrationStatus","OK").count();

                RealmQuery<customers>query2=customersList.where();
                unregisteredCount=(int)query2.contains("RegistrationStatus","NA").count();

                totalCustomersCountTextView.setText(registeredCount+"");
                newRegistrationCountTextView.setText(unregisteredCount+"");
            }

            if(adminList.size()!=0){
                setName();
            }

            setPlansCount();

        }



        custCountListener= (RealmChangeListener<RealmResults<customers>>) realm -> {
            setCount();
        };

        plansCountListener= (RealmChangeListener<RealmResults<plans>>) realm->{
            setPlansCount();
        };

        customersList.addChangeListener(custCountListener);
        plansList.addChangeListener(plansCountListener);

        adminList.addChangeListener(admins->{
            setName();
        });
    }


    @Override
    protected void onPause() {
        super.onPause();

        customersList.removeChangeListener(custCountListener);
        plansList.removeChangeListener(plansCountListener);

        if(adminRealm!=null){
            adminRealm.close();
        }
        if(customerRealm!=null){
            customerRealm.close();
        }
        if(plansRealm!=null){
            plansRealm.close();
        }


    }

    private void setPlansCount() {
        plansList=plansRealm.where(plans.class).findAll();
        plansCountTextView.setText(plansList.size()+"");

    }

    void setCount(){
        RealmQuery<customers>query1=customersList.where();
        registeredCount=(int)query1.contains("RegistrationStatus","OK").count();

        RealmQuery<customers>query2=customersList.where();
        unregisteredCount=(int)query2.contains("RegistrationStatus","NA").count();

        totalCustomersCountTextView.setText(registeredCount+"");
        newRegistrationCountTextView.setText(unregisteredCount+"");
    }

    void setName(){
        adminList=adminRealm.where(admin.class).findAll();
        adminNameTextView.setText(String.format("Welcome, %s",adminList.first().getAdminName()));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.admin_dashboard_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.admin_logout:
                alertDialog.show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        customersList.removeChangeListener(custCountListener);
        plansList.removeChangeListener(plansCountListener);

        if(adminRealm!=null){
            adminRealm.close();
        }
        if(customerRealm!=null){
            customerRealm.close();
        }
        if(plansRealm!=null){
            plansRealm.close();
        }
    }
}