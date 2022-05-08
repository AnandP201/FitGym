package com.application.fitgym.activities.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.application.fitgym.MemberDetailsInterface;
import com.application.fitgym.R;
import com.application.fitgym.adapters.MembersAdapter;
import com.application.fitgym.helpers.GymApplication;
import com.application.fitgym.models.customers;
import com.application.fitgym.models.resources;
import com.application.fitgym.models.status;

import org.bson.types.Symbol;

import java.util.Arrays;
import java.util.function.BiConsumer;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.mongodb.App;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.SyncConfiguration;

public class PeersActivity extends AppCompatActivity implements MemberDetailsInterface {

    Toolbar toolbar;
    Realm sRealm,cRealm,rRealm;
    RealmResults<status> res;
    RealmResults<customers> customersResults;
    RealmResults<resources> resourcesResults;
    ProgressBar loadingBar;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;

    TextView nameTV,contactTV,memberSinceTV;

    AlertDialog memberDetailsDialog;

    RealmChangeListener<RealmResults<customers>> customerListener;
    RealmChangeListener<RealmResults<resources>> resourceListener;

    MembersAdapter adapter;
    App app;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_peers);
        toolbar=findViewById(R.id.peers_activity_toolbar);
        setSupportActionBar(toolbar);

        linearLayoutManager=new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration=new DividerItemDecoration(this,linearLayoutManager.getOrientation());
        loadingBar=findViewById(R.id.peers_activity_loading_bar);
        recyclerView=findViewById(R.id.peers_activity_recycler_view);

        View dialogView= LayoutInflater.from(this).inflate(R.layout.member_details_layout,null);

        nameTV=dialogView.findViewById(R.id.dialog_member_name);
        contactTV=dialogView.findViewById(R.id.dialog_member_phno);
        memberSinceTV=dialogView.findViewById(R.id.dialog_member_since);

        AlertDialog.Builder builder=new AlertDialog
                .Builder(this)
                .setView(dialogView)
                .setCancelable(true);
        
        memberDetailsDialog=builder.create();
        memberDetailsDialog.getWindow()
                .setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);

        getSupportActionBar().setTitle("My Gym-mates");
    }

    @Override
    protected void onStart() {
        super.onStart();
        app= GymApplication.getGlobalAppInstance();
        user=app.currentUser();

        if(user!=null){
            sRealm=Realm.getInstance(new SyncConfiguration
                    .Builder(user,"members")
                    .allowQueriesOnUiThread(true)
                    .build());

            cRealm=Realm.getInstance(new SyncConfiguration
                    .Builder(user,"users")
                    .allowQueriesOnUiThread(true)
                    .build());

            rRealm=Realm.getInstance(new SyncConfiguration
                    .Builder(user,"data")
                    .allowQueriesOnUiThread(true)
                    .build());

            res=sRealm.where(status.class).findAll();
            customersResults=cRealm.where(customers.class).findAll();
            resourcesResults=rRealm.where(resources.class).findAll();

            if(res.size()>0 && customersResults.size()>0 && resourcesResults.size()>0){
                loadingBar.setVisibility(View.INVISIBLE);
                adapter=new MembersAdapter(PeersActivity.this,this,res,customersResults,resourcesResults);
                recyclerView.setAdapter(adapter);
            }
        }


        customerListener=c->{
            refresh();
        };

        resourceListener=r->{
            refresh();
        };

        res.addChangeListener((statuses, changeSet) -> {
            refresh();
        });

    }


    private void refresh() {
        res=sRealm.where(status.class).findAll();
        customersResults=cRealm.where(customers.class).findAll();
        resourcesResults=rRealm.where(resources.class).findAll();
        if(res.size()>0 && customersResults.size()>0 && resourcesResults.size()>0){
            loadingBar.setVisibility(View.INVISIBLE);
        }
        adapter=new MembersAdapter(PeersActivity.this,this,res,customersResults,resourcesResults);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        removeListeners();
        closeRealms();
    }

    private void removeListeners() {
        res.removeAllChangeListeners();
        customersResults.removeChangeListener(customerListener);
        resourcesResults.removeChangeListener(resourceListener);
    }

    private void closeRealms() {
        sRealm.close();
        cRealm.close();
        rRealm.close();
    }

    @Override
    protected void onPause() {
        super.onPause();

        closeRealms();
    }

    @Override
    public void openMemberDialog(int position) {

        status s=res.get(position);

        customers c=customersResults.where().contains("authID",s.getUserAuthID()).findFirst();

        nameTV.setText(c.getName());
        contactTV.setText(c.getPhone());
        memberSinceTV.setText(s.getMemberSince());

        memberDetailsDialog.show();
    }
}