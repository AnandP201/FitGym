package com.application.fitgym.activities.admin;



import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.application.fitgym.ApproveCustomerInterface;
import com.application.fitgym.R;
import com.application.fitgym.adapters.ToRegisterCustomersAdapter;
import com.application.fitgym.helpers.GymApplication;
import com.application.fitgym.models.admin;
import com.application.fitgym.models.customers;
import com.application.fitgym.models.resources;
import com.application.fitgym.models.status;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.mongodb.App;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.SyncConfiguration;

public class ManageNewCustomersActivity extends AppCompatActivity implements ApproveCustomerInterface {

    Realm userRealm,adminRealm,statusRealm,resourceRealm;
    App app;
    User user;
    TextView infoText,confirmRegTextView,cancelDialogTextView,dialogCustomerInfo;
    ProgressBar loadingIcon;
    RecyclerView recyclerView;
    ToRegisterCustomersAdapter adapter;
    RealmChangeListener<RealmResults<customers>> customersListener;
    RealmChangeListener<RealmResults<resources>> resourcesListener;
    RealmResults<customers> customerRealmResults;
    RealmResults<resources> resourcesRealmResults;
    Toolbar toolbar;
    LinearLayoutManager linearLayoutManager;
    View dialogView;
    AlertDialog confirmCustomerDialog;
    String authID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_new_customers);

        infoText=findViewById(R.id.new_customers_text_info);
        loadingIcon=findViewById(R.id.new_customers_loading);
        toolbar=findViewById(R.id.admin_cnfnewcust_toolbar);
        setSupportActionBar(toolbar);

        dialogView=getLayoutInflater().inflate(R.layout.new_customer_id_input_layout,null);
        confirmRegTextView=dialogView.findViewById(R.id.register_customer_textview);
        cancelDialogTextView=dialogView.findViewById(R.id.cancel_new_customer_dialog);
        dialogCustomerInfo=dialogView.findViewById(R.id.dialog_new_customer_info);

        AlertDialog.Builder builder=new AlertDialog
                .Builder(this)
                .setView(dialogView)
                .setCancelable(false);

        confirmCustomerDialog=builder.create();

        getSupportActionBar().setTitle("Registration Actions");
        recyclerView=findViewById(R.id.new_customers_recyclerView);
        linearLayoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration=new DividerItemDecoration(this,linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        app= GymApplication.getGlobalAppInstance();
        user=app.currentUser();

    }

    @Override
    protected void onStart() {
        super.onStart();

        showProgressBar(true);
        showEmptyInfo(false);

        userRealm=Realm.getInstance(new SyncConfiguration
                .Builder(user,"users")
                .allowQueriesOnUiThread(true)
                .allowWritesOnUiThread(true)
                .build());
        adminRealm=Realm.getInstance(new SyncConfiguration
                .Builder(user,"admin")
                .allowWritesOnUiThread(true)
                .allowQueriesOnUiThread(true)
                .build());

        resourceRealm=Realm.getInstance(new SyncConfiguration
                .Builder(user,"data")
                .allowQueriesOnUiThread(true)
                .allowWritesOnUiThread(true)
                .build());

        statusRealm=Realm.getInstance(new SyncConfiguration
                .Builder(user,"members")
                .allowWritesOnUiThread(true)
                .allowQueriesOnUiThread(true)
                .build());

        customerRealmResults=userRealm.where(customers.class).findAll();
        resourcesRealmResults=resourceRealm.where(resources.class).findAll();

        customersListener= results->{
          refresh();
        };

        resourcesListener= results->{
          refresh();
        };

        customerRealmResults.addChangeListener(customersListener);
        resourcesRealmResults.addChangeListener(resourcesListener);


        if(!customerRealmResults.isEmpty() && !resourcesRealmResults.isEmpty()){
            showProgressBar(false);
            showEmptyInfo(false);
            RealmQuery<customers> query=customerRealmResults.where();
            RealmResults<customers> filteredList=query.contains("RegistrationStatus","NA").findAll();
            if(!filteredList.isEmpty()){
                adapter=new ToRegisterCustomersAdapter(filteredList,resourcesRealmResults,this,this);
                recyclerView.setAdapter(adapter);
            }else{
                showProgressBar(false);
                showEmptyInfo(true);
            }
        }else{
            showEmptyInfo(true);
        }
        confirmRegTextView.setOnClickListener(view->{
            approveFunc();
        });

        cancelDialogTextView.setOnClickListener(view->{
            confirmCustomerDialog.hide();
        });

    }


    private void refresh(){
        RealmQuery<customers> tempQuery=customerRealmResults.where();
        RealmResults<customers> tempFilter=tempQuery.contains("RegistrationStatus","NA").findAll();

        if(tempFilter.size()>0){
            showProgressBar(false);
            showEmptyInfo(false);
        }else{
            showEmptyInfo(true);
        }
        resourcesRealmResults=resourceRealm.where(resources.class).findAll();

        ToRegisterCustomersAdapter freshAdapter=new ToRegisterCustomersAdapter(tempFilter,resourcesRealmResults,this,this);
        recyclerView.setAdapter(freshAdapter);

    }


    private void approveFunc(){
        String toStore;

        customers current=customerRealmResults.where().equalTo("authID",authID).findFirst();

        Log.i("TAG",current.toString());

        //modifying the registration status attribute
        userRealm.executeTransaction(realm->{
            current.setRegistrationStatus("OK");
        });

        //adding to admin collection
        String ids=adminRealm.where(admin.class).findFirst().getCustomerIDs();

        if(ids.isEmpty()){
            String registerID="FGM100";
            toStore=registerID;
            adminRealm.executeTransaction(realm->{
                adminRealm.where(admin.class).findFirst().setCustomerIDs(String.format("[%s]", registerID));
            });
        }else{
            List<String> list=new ArrayList<>();
            list.addAll(Arrays.asList(ids.substring(1,ids.length()-1).split(", ")));

            int lastIdx=Integer.parseInt(list.get(list.size()-1).substring(3));
            String registerID="FGM"+(++lastIdx);

            list.add(registerID);
            toStore=list.get(list.size()-1);

            adminRealm.executeTransaction(realm->{
                adminRealm.where(admin.class).findFirst().setCustomerIDs(list.toString());
            });
        }



        //inserting into status collection
        status s=new status();

        statusRealm.executeTransaction(realm->{
            s.set_partition();
            s.setUserAuthID(current.getAuthID());
            s.setPlanActiveDuration("");
            s.setActivePlans("");
            s.setGymUserID(toStore);
            s.setStats("offline");

            SimpleDateFormat sdf=new SimpleDateFormat("dd-MMMM-yy");
            Calendar calendar=Calendar.getInstance();
            s.setMemberSince(sdf.format(calendar.getTime()));

            statusRealm.insertOrUpdate(s);
        });

        Toast.makeText(this, "Customer registered!", Toast.LENGTH_SHORT).show();

        confirmCustomerDialog.hide();
    }

    @Override
    public void approveCustomer(int position) {
        this.authID=customerRealmResults.get(position).getAuthID();
        dialogCustomerInfo.setText(String.format("Actions for : %s ",customerRealmResults.get(position).getName()));
        confirmCustomerDialog.show();
    }

    public void showEmptyInfo(boolean val){
        if(val){
            infoText.setVisibility(View.VISIBLE);
        }else{
            infoText.setVisibility(View.INVISIBLE);
        }
    }

    public void showProgressBar(boolean val) {
        if(val){
            loadingIcon.setVisibility(View.VISIBLE);
        }else{
            loadingIcon.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        customerRealmResults.removeChangeListener(customersListener);
        resourcesRealmResults.removeChangeListener(resourcesListener);

        resourceRealm.close();
        statusRealm.close();
        adminRealm.close();
        userRealm.close();
    }
}
