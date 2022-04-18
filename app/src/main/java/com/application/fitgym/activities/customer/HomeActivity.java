package com.application.fitgym.activities.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.application.fitgym.adapters.CustomersAdapter;

import com.application.fitgym.helpers.dashboardItems.CustomerDashMenuItems;
import com.application.fitgym.helpers.GymApplication;
import com.application.fitgym.R;

import com.application.fitgym.models.customers;
import com.application.fitgym.models.resources;

import java.util.Arrays;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmModel;
import io.realm.RealmResults;
import io.realm.mongodb.App;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.SyncConfiguration;

public class HomeActivity extends AppCompatActivity{

    private App app;
    private Toolbar toolbar;

    customers currentCustomer;
    resources currentCustomerResource;
    private ImageView displayPictureImageView;
    private TextView uniqueIDTextView,unregisteredTextView,nameTextView;
    private GridView cDashboardGridview;
    List<CustomerDashMenuItems> customerDashMenuItems;
    private Realm customersRealm,resourcesRealm;

    private RealmResults<customers> customersRealmResults;
    private RealmResults<resources> resourcesRealmResults;


    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        app= GymApplication.getGlobalAppInstance();
        View view=findViewById(R.id.profile_summary);

        displayPictureImageView=view.findViewById(R.id.home_header_imageView);
        unregisteredTextView=view.findViewById(R.id.underregistration_textview);
        uniqueIDTextView=view.findViewById(R.id.gym_uniqueID_textView);
        nameTextView=view.findViewById(R.id.name_welcome_textView);
        cDashboardGridview=findViewById(R.id.customer_dashboard_gridview);

        customerDashMenuItems= Arrays.asList(CustomerDashMenuItems.dashBoardItems);
        CustomersAdapter customersAdapter=new CustomersAdapter(HomeActivity.this,customerDashMenuItems);
        cDashboardGridview.setAdapter(customersAdapter);

        toolbar=findViewById(R.id.customer_dashboard_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Dashboard");


    }

    @Override
    protected void onStart() {
        super.onStart();
        if(app.currentUser()!=null){

            customersRealm=Realm.getInstance(new SyncConfiguration
                    .Builder(app.currentUser(), "users")
                    .allowWritesOnUiThread(true)
                    .allowQueriesOnUiThread(true)
                    .build());

            customersRealmResults=customersRealm.where(customers.class).contains("authID",app.currentUser().getId()).findAll();

            resourcesRealm=Realm.getInstance(new SyncConfiguration
                    .Builder(app.currentUser(), "data")
                    .allowQueriesOnUiThread(true)
                    .allowWritesOnUiThread(true)
                    .build());
            resourcesRealmResults=resourcesRealm.where(resources.class).equalTo("userID",app.currentUser().getId()).findAll();
        }

        if(customersRealmResults.size()!=0){
            currentCustomer=customersRealm.where(customers.class)
                    .contains("authID",app.currentUser().getId()).findFirst();

            if(currentCustomer.getRegistrationStatus().equalsIgnoreCase("NA")){
                showTextViewAsPerRegistration(0);
            }
            else{
                showTextViewAsPerRegistration(1);
            }
            setHeaderName();

        }

        if(resourcesRealmResults.size()!=0){
            currentCustomerResource=resourcesRealm.where(resources.class)
                    .contains("userID",app.currentUser().getId()).findFirst();
            if(currentCustomerResource!=null){
                setHeaderImage();
            }
        }



        customersRealmResults.addChangeListener(customers -> {
            currentCustomer=customersRealm.where(customers.class)
                    .contains("authID",app.currentUser().getId()).findFirst();
            if(currentCustomer!=null){

                if(currentCustomer.getRegistrationStatus().equalsIgnoreCase("NA")){
                    showTextViewAsPerRegistration(0);
                }
                else{
                    showTextViewAsPerRegistration(1);

                }
            }
            setHeaderName();

        });

        resourcesRealmResults.addChangeListener(resources -> {
            currentCustomerResource=resourcesRealm.where(resources.class)
                    .contains("userID",app.currentUser().getId())
                    .findFirst();
            if(currentCustomerResource!=null){
                setHeaderImage();
            }
        });

        currentCustomer.addChangeListener(realmModel -> {
            Log.i("CHANGES IN CURRENT CUSTOMER","Changes occurred!");
        });



        cDashboardGridview.setOnItemClickListener((adapterView, view1, i, l) -> {
            String action=customerDashMenuItems.get(i).getAction();
            switch (action){
                case "tasks":
                    startActivity(new Intent(HomeActivity.this,TaskActivity.class));
                    break;
                case "plans":

                    break;
                case "people":

                    break;
                case "bills":

                    break;
                default:
                    Toast.makeText(this, customerDashMenuItems.get(i).getAction(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setHeaderName() {
        nameTextView.setText(String.format("Welcome, %s",currentCustomer.getName().split(" ")[0]));
    }

    private void setHeaderImage(){
        byte []b=currentCustomerResource.getData();

        Bitmap bitmap= BitmapFactory.decodeByteArray(b,0,b.length);
        RoundedBitmapDrawable roundedBitmapDrawable= RoundedBitmapDrawableFactory.create(getResources(),bitmap);
        roundedBitmapDrawable.setCircular(true);
        displayPictureImageView.setImageDrawable(roundedBitmapDrawable);
    }


    private void showTextViewAsPerRegistration(int i){
        if(i==0){
            unregisteredTextView.setVisibility(View.VISIBLE);
            if(uniqueIDTextView.getVisibility()==View.VISIBLE){
                uniqueIDTextView.setVisibility(View.INVISIBLE);
            }
        }
        else{
            uniqueIDTextView.setVisibility(View.VISIBLE);
            if(unregisteredTextView.getVisibility()==View.VISIBLE){
                unregisteredTextView.setVisibility(View.INVISIBLE);
            }
        }
    }

public void showToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void logout(){
        if(app.currentUser()!=null){
            User user=app.currentUser();
            user.logOutAsync(response->{
               if(response.isSuccess()){
                   customersRealm.close();
                   resourcesRealm.close();
                   startActivity(new Intent(HomeActivity.this, LoginSignUpActivity.class));
                   finish();
               }
            });

        }
        Toast.makeText(this,"Logged out successfully!",Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.logout_item:
                logout();
                return true;
            case R.id.reset_password_item:
                resetPassword();
                return true;
            case R.id.aboutus_item:
                aboutUs();
            case R.id.member_buy:
                buyMembership();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void buyMembership() {
    }

    private void aboutUs() {
        showToast("About Us");
    }

    private void resetPassword() {
    showToast("Password reset!");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        resourcesRealm.close();
        customersRealm.close();
    }
}