package com.application.fitgym.activities.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.application.fitgym.helpers.ProfileData;
import com.application.fitgym.misc.LoginSignUpActivity;
import com.application.fitgym.adapters.CustomersAdapter;

import com.application.fitgym.helpers.dashboardItems.CustomerDashMenuItems;
import com.application.fitgym.helpers.GymApplication;
import com.application.fitgym.R;

import com.application.fitgym.models.customers;
import com.application.fitgym.models.resources;
import com.application.fitgym.models.status;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.mongodb.App;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.SyncConfiguration;

public class HomeActivity extends AppCompatActivity{

    private App app;
    Toolbar toolbar;

    customers currentCustomer;
    resources currentCustomerResource;
    final static String NO_PLAN_PURCHASED="Buy a basic plan to be a member";
    final static String NO_ADDON_PURCHASED="No add-ons purchased";
    status currentStatus;
    private ImageView displayPictureImageView;
    private boolean IS_MEMBER=false;
    private boolean IS_ACCEPTED=false;
    TextView uniqueIDTextView,nameTextView,normalPlanTextView,addOnPlanTextView;
    GridView cDashboardGridview;
    List<CustomerDashMenuItems> customerDashMenuItems;
    Realm customersRealm,resourcesRealm,statusRealm;
    CardView cardView;
    private LinearLayout linearLayout;

    RealmResults<customers> customersRealmResults;
    RealmResults<resources> resourcesRealmResults;
    RealmResults<status> statusRealmResults;


    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        app= GymApplication.getGlobalAppInstance();
        View view=findViewById(R.id.profile_summary);

        cardView=findViewById(R.id.profile_card);
        linearLayout=findViewById(R.id.customer_home_layout);
        displayPictureImageView=view.findViewById(R.id.home_header_imageView);
        uniqueIDTextView=view.findViewById(R.id.gym_uniqueID_textView);
        nameTextView=view.findViewById(R.id.name_welcome_textView);
        cDashboardGridview=findViewById(R.id.customer_dashboard_gridview);
        normalPlanTextView=findViewById(R.id.membership_plan);
        addOnPlanTextView=findViewById(R.id.addon_plans);

        customerDashMenuItems= Arrays.asList(CustomerDashMenuItems.dashBoardItems);
        CustomersAdapter customersAdapter=new CustomersAdapter(HomeActivity.this,customerDashMenuItems);
        cDashboardGridview.setAdapter(customersAdapter);

        toolbar=findViewById(R.id.customer_dashboard_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Dashboard");

        cardView.setOnClickListener(view1 -> {
            String name=currentCustomer.getName();
            String age=currentCustomer.getAge();
            String gender=currentCustomer.getGender();
            String memberSince=currentStatus.getMemberSince();
            String activePlans=currentStatus.getActivePlans();
            String gymID=currentStatus.getGymUserID();

            ProfileData data=new ProfileData(name,gymID,memberSince,activePlans,age,gender,currentCustomerResource.getData());

            View myview= LayoutInflater.from(this).inflate(R.layout.profile_card_dialog,null);

            AlertDialog.Builder builder=new AlertDialog
                    .Builder(this);


            TextView nameText=myview.findViewById(R.id.profile_dialog_name_text);
            TextView ageGenText=myview.findViewById(R.id.profile_dialog_age_gen_text);
            TextView plansText=myview.findViewById(R.id.profile_dialog_plans_text);
            TextView gymIDtext=myview.findViewById(R.id.profile_dialog_uniqueid_text);
            TextView sinceText=myview.findViewById(R.id.profile_dialog_since_text);
            ImageView profileImg=myview.findViewById(R.id.profile_dialog_image_view);


            nameText.setText(data.getName());
            ageGenText.setText(data.getAge()+","+data.getGender());
            plansText.setText("Plans active : "+((data.getPlans().isEmpty())?"NA":data.getPlans()));
            gymIDtext.setText((data.getGymID().isEmpty())?"NA":data.getGymID());
            sinceText.setText("Member since : "+((data.getMembersince().isEmpty())?"NA":data.getMembersince()));
            byte []b=data.getImageData();

            Bitmap bitmap= BitmapFactory.decodeByteArray(b,0,b.length);
            RoundedBitmapDrawable roundedBitmapDrawable= RoundedBitmapDrawableFactory.create(getResources(),bitmap);
            roundedBitmapDrawable.setCircular(true);
            profileImg.setImageDrawable(roundedBitmapDrawable);

            builder.setView(myview);
            builder.setCancelable(true);



            AlertDialog profileDialog=builder.create();

            profileDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            profileDialog.show();

        });

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

            statusRealm=Realm.getInstance(new SyncConfiguration
                    .Builder(app.currentUser(),"members")
                    .allowWritesOnUiThread(true)
                    .allowQueriesOnUiThread(true)
                    .build());
            statusRealmResults=statusRealm.where(status.class).findAll();
            currentStatus=statusRealmResults.where().equalTo("userAuthID",app.currentUser().getId()).findFirst();
        }

        if(currentStatus!=null){
            statusRealm.executeTransaction(realm -> currentStatus.setStats("online"));
            checkAndSetUserPlans();
            uniqueIDTextView.setText(currentStatus.getGymUserID());
        }

        if(customersRealmResults.size()!=0){
            currentCustomer=customersRealm.where(customers.class)
                    .contains("authID",app.currentUser().getId()).findFirst();

            checkCurrentRegisteredStatus();

            if(currentCustomer.getRegistrationStatus().equalsIgnoreCase("NA")){
                uniqueIDTextView.setText("Currently, you are under-registration");
            }
            else{
                if(currentStatus!=null){
                    uniqueIDTextView.setText(currentStatus.getGymUserID());
                }
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
                checkCurrentRegisteredStatus();
                if(currentCustomer.getRegistrationStatus().equalsIgnoreCase("NA")){
                    uniqueIDTextView.setText("Currently, you are under-registration");
                }
                else{
                    if(currentStatus!=null){
                        uniqueIDTextView.setText(currentStatus.getGymUserID());
                    }
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

        statusRealmResults.addChangeListener(realm -> {
            currentStatus=statusRealm
                    .where(status.class)
                    .equalTo("userAuthID",app.currentUser().getId())
                    .findFirst();

            if(currentStatus.getActivePlans().equalsIgnoreCase("")){
                IS_MEMBER=false;
            }else{
                IS_MEMBER=true;
            }

            if(currentStatus.getStats().equalsIgnoreCase("offline")){
                statusRealm.executeTransaction(realm1 -> {
                    if(currentStatus!=null){
                        currentStatus.setStats("online");
                    }
                });
            }

            uniqueIDTextView.setText(currentStatus.getGymUserID());
            checkAndSetUserPlans();
        });


        View.OnClickListener snackBarListener= view->{
            startActivity(new Intent(HomeActivity.this,CustomerPlansActivity.class).putExtra("title","Purchase plan"));
        };

        cDashboardGridview.setOnItemClickListener((adapterView, view1, i, l) -> {
            closeRealms();
            if(customerDashMenuItems.get(i).getAction().equalsIgnoreCase("tasks")){
                startActivity(new Intent(this,TaskActivity.class));
            }
            else if (IS_ACCEPTED && IS_MEMBER) {
                String action = customerDashMenuItems.get(i).getAction();
                switch (action) {
                    case "plans":
                        startActivity(new Intent(this,CustomerPlansActivity.class));
                        break;
                    case "people":
                        startActivity(new Intent(this,PeersActivity.class));
                        break;
                    case "bills":
                        startActivity(new Intent(this,BillingActivity.class));
                        break;
                    default:
                        Toast.makeText(this, customerDashMenuItems.get(i).getAction(), Toast.LENGTH_SHORT).show();
                }
            }else if(IS_ACCEPTED && !IS_MEMBER){
                Snackbar.make(this,linearLayout,"Membership required!", BaseTransientBottomBar.LENGTH_LONG)
                        .setAction("BUY PLAN",snackBarListener)
                        .show();
            }
            else {
                Snackbar.make(this,linearLayout,"Your registration needs approval.Please wait!", BaseTransientBottomBar.LENGTH_LONG)
                        .show();
            }
        });
    }

    private void checkAndSetUserPlans(){
        String str=currentStatus.getActivePlans();

        if(str.equalsIgnoreCase("")){
            normalPlanTextView.setText(NO_PLAN_PURCHASED);
            addOnPlanTextView.setText(NO_ADDON_PURCHASED);
        }else if(str.contains("NP") && !str.contains("AP")){
            int days=Integer.parseInt(currentStatus.getPlanActiveDuration())*30;
            normalPlanTextView.setText(String.format("Membership Plan active. %d days left",days));
            addOnPlanTextView.setText(NO_ADDON_PURCHASED);
        }else if(str.contains("NP") && str.contains("AP")){
            int days=Integer.parseInt(currentStatus.getPlanActiveDuration())*30;
            normalPlanTextView.setText(String.format("Membership Plan active. %d days left",days));
            addOnPlanTextView.setText("Add-ons active!");
        }
    }


    private void checkCurrentRegisteredStatus(){

        if(currentCustomer.getRegistrationStatus().equalsIgnoreCase("OK")){
            IS_ACCEPTED=true;
        }else{
            IS_ACCEPTED=true;
        }
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


    private void closeRealms(){
        statusRealm.close();
        customersRealm.close();
        resourcesRealm.close();
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
        statusRealm.executeTransaction(realm -> currentStatus.setStats("offline"));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        statusRealmResults.removeAllChangeListeners();
        statusRealm.executeTransaction(realm -> currentStatus.setStats("offline"));


        customersRealmResults.removeAllChangeListeners();
        resourcesRealmResults.removeAllChangeListeners();


        closeRealms();
    }

    private void buyMembership() { }

    private void aboutUs() {
        showToast("About Us");
    }

    private void resetPassword() {
    showToast("Password reset!");
    }
}