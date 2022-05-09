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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.application.fitgym.ImageActionSheet;
import com.application.fitgym.ImageUploadInterface;
import com.application.fitgym.models.CustomModels.ProfileData;
import com.application.fitgym.misc.LoginSignUpActivity;
import com.application.fitgym.adapters.CustomersAdapter;

import com.application.fitgym.helpers.dashboardItems.CustomerDashMenuItems;
import com.application.fitgym.helpers.GymApplication;
import com.application.fitgym.R;

import com.application.fitgym.models.RealmModels.customers;
import com.application.fitgym.models.RealmModels.resources;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.bson.Document;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.mongodb.App;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.sync.SyncConfiguration;


class object{
    String activePlans;
    String gymID;
    String memberSince;
    String duration;

    public object(String a,String g,String m,String d){
        this.activePlans=a;
        this.gymID=g;
        this.memberSince=m;
        this.duration=d;
    }
}
public class HomeActivity extends AppCompatActivity implements ImageUploadInterface {

    private App app;
    Toolbar toolbar;

    customers currentCustomer;
    final public static int SELECT_PICTURE = 200;
    resources currentCustomerResource;
    final static String NO_PLAN_PURCHASED = "Buy a basic plan to be a member";
    final static String NO_ADDON_PURCHASED = "No add-ons purchased";

    private ImageView displayPictureImageView;
    private boolean IS_MEMBER = false;
    private boolean IS_ACCEPTED = false;

    String MEMBER_SINCE="",ACTIVE_PLANS="",ID="",DURATION="";

    final static int SET_STATUS_OFFLINE=1;
    final static int SET_STATUS_ONLINE=2;
    final static int FETCH_STATUS_DOC=3;

    TextView uniqueIDTextView, nameTextView, normalPlanTextView, addOnPlanTextView;
    GridView cDashboardGridview;
    List<CustomerDashMenuItems> customerDashMenuItems;
    Realm customersRealm, resourcesRealm;
    CardView cardView;
    ImageView profileImg;
    private LinearLayout linearLayout;
    LinearLayout planDetsLayout;


    RealmResults<customers> customersRealmResults;
    RealmResults<resources> resourcesRealmResults;

    RealmChangeListener<RealmResults<customers>> customersListener;
    RealmChangeListener<RealmResults<resources>> resourceListener;

    ImageActionSheet imageActionSheet;

    class fetchOrSetStatus extends AsyncTask<Integer,Void,Void>{

        @Override
        protected Void doInBackground(Integer... integers) {
            try{
                MongoClient client=app.currentUser().getMongoClient("mongodb-atlas");
                MongoDatabase db=client.getDatabase("GymDB");
                MongoCollection<Document> coll=db.getCollection("status");
                Document filter=new Document("userAuthID",app.currentUser().getId());
                Document curr=coll.find(filter).first().get();

               if(curr==null){
                   if(integers[0]==SET_STATUS_OFFLINE){
                      runOnUiThread(()->{
                          app.currentUser().logOutAsync(response->{
                              if(response.isSuccess()){
                                  startActivity(new Intent(HomeActivity.this, LoginSignUpActivity.class));
                                  finish();
                              }
                          });
                          Toast.makeText(HomeActivity.this,"Logged out successfully!",Toast.LENGTH_LONG).show();
                      });
                   }
                   return null;
               }else{
                   String a=curr.getString("activePlans");
                   String g=curr.getString("gymUserID");
                   String m=curr.getString("memberSince");
                   String d=curr.getString("planActiveDuration");

                   object o=new object(a,g,m,d);

                   int option=integers[0];

                   if(option==1){
                       runOnUiThread(()->{
                           Document update=new Document("$set",new Document("stats","offline"));
                           coll.updateOne(filter,update).getAsync(result->{
                               if(result.isSuccess()){
                                   app.currentUser().logOutAsync(response->{
                                       if(response.isSuccess()){
                                           startActivity(new Intent(HomeActivity.this, LoginSignUpActivity.class));
                                           finish();
                                       }
                                   });
                                   Toast.makeText(HomeActivity.this,"Logged out successfully!",Toast.LENGTH_LONG).show();
                               }
                           });
                       });
                   }else if(option==2){
                       runOnUiThread(()->{
                           Document update=new Document("$set",new Document("stats","online"));
                           coll.updateOne(filter,update).getAsync(result->{
                               if(result.isSuccess()){
                                   Log.i("UPDATED",curr.toString());
                               }
                           });
                       });

                   }else{
                       runOnUiThread(() -> {
                           ACTIVE_PLANS = o.activePlans;
                           DURATION = o.duration;
                           ID = o.gymID;
                           MEMBER_SINCE = o.memberSince;

                           uniqueIDTextView.setText(o.gymID==null ? "Currently, you are under-registration" : String.format("FitGym ID: %s", o.gymID));
                           IS_MEMBER = true;
                           checkAndSetUserPlans();
                       });
                   }
               }


            }catch (Exception e){
                Log.i("Tag",e.toString());
            }
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        app = GymApplication.getGlobalAppInstance();


        View view = findViewById(R.id.profile_summary);

        cardView = findViewById(R.id.profile_card);
        linearLayout = findViewById(R.id.customer_home_layout);
        displayPictureImageView = view.findViewById(R.id.home_header_imageView);
        uniqueIDTextView = view.findViewById(R.id.gym_uniqueID_textView);
        nameTextView = view.findViewById(R.id.name_welcome_textView);
        cDashboardGridview = findViewById(R.id.customer_dashboard_gridview);
        normalPlanTextView = findViewById(R.id.membership_plan);
        imageActionSheet = new ImageActionSheet(HomeActivity.this);
        addOnPlanTextView = findViewById(R.id.addon_plans);
        planDetsLayout = findViewById(R.id.plan_details);

        customerDashMenuItems = Arrays.asList(CustomerDashMenuItems.dashBoardItems);
        CustomersAdapter customersAdapter = new CustomersAdapter(HomeActivity.this, customerDashMenuItems);
        cDashboardGridview.setAdapter(customersAdapter);

        toolbar = findViewById(R.id.customer_dashboard_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Dashboard");

        new fetchOrSetStatus().execute(FETCH_STATUS_DOC);

        cardView.setOnClickListener(view1 -> {
            if(currentCustomer!=null){
                String name=currentCustomer.getName();
                String age=currentCustomer.getAge();
                String gender=currentCustomer.getGender();
                String memberSince=MEMBER_SINCE;
                String activePlans=ACTIVE_PLANS;
                String gymID=ID;
                ProfileData data;

if(currentCustomerResource.getData()==null){
    data=new ProfileData(name,gymID,memberSince,activePlans,age,gender,null);
}else{
    data=new ProfileData(name,gymID,memberSince,activePlans,age,gender,currentCustomerResource.getData());
}

                View myview= LayoutInflater.from(this).inflate(R.layout.profile_card_dialog,null);

                AlertDialog.Builder builder=new AlertDialog
                        .Builder(this);


                TextView nameText=myview.findViewById(R.id.profile_dialog_name_text);
                TextView ageGenText=myview.findViewById(R.id.profile_dialog_age_gen_text);
                TextView plansText=myview.findViewById(R.id.profile_dialog_plans_text);
                TextView gymIDtext=myview.findViewById(R.id.profile_dialog_uniqueid_text);
                TextView sinceText=myview.findViewById(R.id.profile_dialog_since_text);
                profileImg=myview.findViewById(R.id.profile_dialog_image_view);


                profileImg.setOnClickListener(v ->{
                    imageActionSheet.show(getSupportFragmentManager(),"ModalImageUploadSheet");
                });

                nameText.setText(data.getName());
                ageGenText.setText(data.getAge()+","+data.getGender());
                plansText.setText("Plans active : "+((data.getPlans().isEmpty())?"NA":data.getPlans()));
                gymIDtext.setText((data.getGymID().isEmpty())?"NA":data.getGymID());
                sinceText.setText("Member since : "+((data.getMembersince().isEmpty())?"NA":data.getMembersince()));
                if(data.getImageData()!=null){
                    byte []b=data.getImageData();

                    Bitmap bitmap= BitmapFactory.decodeByteArray(b,0,b.length);
                    RoundedBitmapDrawable roundedBitmapDrawable= RoundedBitmapDrawableFactory.create(getResources(),bitmap);
                    roundedBitmapDrawable.setCircular(true);
                    profileImg.setImageDrawable(roundedBitmapDrawable);
                }else{
                    profileImg.setImageDrawable(null);
                }

                builder.setView(myview);
                builder.setCancelable(true);



                AlertDialog profileDialog=builder.create();

                profileDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                profileDialog.show();

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (app.currentUser() != null) {

            new fetchOrSetStatus().execute(SET_STATUS_ONLINE);

            customersRealm = Realm.getInstance(new SyncConfiguration
                    .Builder(app.currentUser(), "users")
                    .allowWritesOnUiThread(true)
                    .allowQueriesOnUiThread(true)
                    .build());

            customersRealmResults = customersRealm.where(customers.class).contains("authID", app.currentUser().getId()).findAll();

            resourcesRealm = Realm.getInstance(new SyncConfiguration
                    .Builder(app.currentUser(), "data")
                    .allowQueriesOnUiThread(true)
                    .allowWritesOnUiThread(true)
                    .build());
            resourcesRealmResults = resourcesRealm.where(resources.class).equalTo("userID", app.currentUser().getId()).findAll();
        }

        if (customersRealmResults.size() != 0) {
            currentCustomer = customersRealm.where(customers.class)
                    .contains("authID", app.currentUser().getId()).findFirst();
            checkCurrentRegisteredStatus();
            setHeaderName();
        }

        if (resourcesRealmResults.size() != 0) {
            currentCustomerResource = resourcesRealm.where(resources.class)
                    .contains("userID", app.currentUser().getId()).findFirst();
            if (currentCustomerResource != null) {
                setHeaderImage();
            }
        }


        customersListener = customers -> {
            currentCustomer = customersRealm.where(customers.class)
                    .contains("authID", app.currentUser().getId()).findFirst();
            checkCurrentRegisteredStatus();
            setHeaderName();
        };

        resourceListener = resources -> {
            currentCustomerResource = resourcesRealm.where(resources.class)
                    .contains("userID", app.currentUser().getId())
                    .findFirst();
            if (currentCustomerResource != null) {
                setHeaderImage();
            }
        };


        customersRealmResults.addChangeListener(customersListener);

        resourcesRealmResults.addChangeListener(resourceListener);


        View.OnClickListener snackBarListener = view -> {
            startActivity(new Intent(HomeActivity.this, CustomerPlansActivity.class).putExtra("title", "Purchase Plan"));
        };

        cDashboardGridview.setOnItemClickListener((adapterView, view1, i, l) -> {

            if(IS_ACCEPTED && IS_MEMBER){
                resourcesRealmResults.removeChangeListener(resourceListener);
                customersRealmResults.removeChangeListener(customersListener);

                closeRealms();
            }
            if (customerDashMenuItems.get(i).getAction().equalsIgnoreCase("tasks")) {
                resourcesRealmResults.removeChangeListener(resourceListener);
                customersRealmResults.removeChangeListener(customersListener);
                closeRealms();
                startActivity(new Intent(this, TaskActivity.class));
            } else if (IS_ACCEPTED && IS_MEMBER) {

                String action = customerDashMenuItems.get(i).getAction();
                switch (action) {
                    case "plans":
                        startActivity(new Intent(this, CustomerPlansActivity.class));
                        break;
                    case "people":
                        startActivity(new Intent(this, PeersActivity.class));
                        break;
                    case "bills":
                        if (currentCustomer != null) {
                            startActivity(new Intent(this, BillingActivity.class).putExtra("ID", ID));
                        } else {
                            Toast.makeText(HomeActivity.this, "Please wait! Data is loading....", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
                        Toast.makeText(this, customerDashMenuItems.get(i).getAction(), Toast.LENGTH_SHORT).show();
                }
            } else if (IS_ACCEPTED && !IS_MEMBER) {
                Snackbar.make(this, linearLayout, "Membership required!", BaseTransientBottomBar.LENGTH_LONG)
                        .setAction("BUY PLAN", snackBarListener)
                        .show();
            } else {
                Snackbar.make(this, linearLayout, "Your registration needs approval.Please wait!", BaseTransientBottomBar.LENGTH_LONG)
                        .show();
            }
        });
    }

    private void checkAndSetUserPlans() {
        planDetsLayout.setVisibility(View.VISIBLE);
        String str=ACTIVE_PLANS;
        if(str.equalsIgnoreCase("")){
            normalPlanTextView.setText(NO_PLAN_PURCHASED);
            addOnPlanTextView.setText(NO_ADDON_PURCHASED);
        }else if(str.contains("NP") && !str.contains("AP")){
            int days=Integer.parseInt(DURATION);
            normalPlanTextView.setText(String.format("Membership Plan active. %d days left",days));
            addOnPlanTextView.setText(NO_ADDON_PURCHASED);
        }else if(str.contains("NP") && str.contains("AP")){
            int days=Integer.parseInt(DURATION);
            normalPlanTextView.setText(String.format("Membership Plan active. %d days left",days));
            addOnPlanTextView.setText("Add-ons active!");
        }
    }


    private void checkCurrentRegisteredStatus(){
        if(currentCustomer.getRegistrationStatus().equalsIgnoreCase("OK")){
            IS_ACCEPTED=true;
        }else{
            checkAndSetUserPlans();
            uniqueIDTextView.setText("Currently, you are under-registration");
            IS_ACCEPTED=false;
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
        customersRealm.close();
        resourcesRealm.close();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void logout(){
        new fetchOrSetStatus().execute(SET_STATUS_OFFLINE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.logout_item:
                logout();
                return true;
            case R.id.aboutus_item:
                aboutUs();
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        new fetchOrSetStatus().execute(SET_STATUS_OFFLINE);

       if(!customersRealm.isClosed() && !resourcesRealm.isClosed()){
          closeRealms();
       }

    }

    private void aboutUs() {
        closeRealms();
        startActivity(new Intent(HomeActivity.this,AboutUs.class));
    }

    @Override
    public void startImageAddActivityForResult() {
        Intent intent=new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        startActivityForResult(Intent.createChooser(intent,"Select picture"),SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        InputStream in=null;
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    try{
                        in= getContentResolver().openInputStream(selectedImageUri);
                    }
                    catch (Exception e){
                        Toast.makeText(this,"Some error occured! Try again!",Toast.LENGTH_LONG);
                    }
                    finally {
                        Bitmap bitmap=BitmapFactory.decodeStream(in);
                        RoundedBitmapDrawable roundedBitmapDrawable= RoundedBitmapDrawableFactory.create(getResources(),bitmap);
                        roundedBitmapDrawable.setCircular(true);

                        profileImg.setImageDrawable(roundedBitmapDrawable);

                        Bitmap photo=((RoundedBitmapDrawable)profileImg.getDrawable()).getBitmap();
                        ByteArrayOutputStream bos=new ByteArrayOutputStream();
                        photo.compress(Bitmap.CompressFormat.PNG,100,bos);
                        byte[] b=bos.toByteArray();

                        resourcesRealm.executeTransaction(realm -> {
                            currentCustomerResource.setData(new byte[0]);
                            currentCustomerResource.setData(b);
                        });

                        Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public void callDeleteConfirmationDialog() {
            resourcesRealm.executeTransaction(realm->{
                if(currentCustomerResource.getData().length>0){
                    byte []b=new byte[0];
                    currentCustomerResource.setData(b);
                    profileImg.setImageDrawable(null);
                }else{
                    Toast.makeText(this, "Profile picture is already empty!", Toast.LENGTH_SHORT).show();
                }

            });
    }
}
