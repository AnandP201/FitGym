package com.application.fitgym.activities.admin;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.application.fitgym.R;
import com.application.fitgym.adapters.PlansAdapter;
import com.application.fitgym.adapters.TaskAdapter;
import com.application.fitgym.helpers.GymApplication;
import com.application.fitgym.models.plans;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.mongodb.App;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.SyncConfiguration;

public class ManagePlansActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FloatingActionButton addNewPlanButton;
    private AlertDialog inputPlanDialog;
    private View inputPlanDialogView;
    private Button publishPlanButton;
    private EditText planTitle,planDesc,planPrice,planValidity;
    private Spinner planTypeSpinner;
    private String title,desc,price,validity,type;
    private App app;
    private PlansAdapter plansAdapter;
    private User user;
    private RecyclerView plansRecyclerView;
    private Realm realm;
    private RealmResults<plans> plansResults;
    private TextView plansLoadingView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_plans);

        toolbar=findViewById(R.id.admin_manageplans_toolbar);

        addNewPlanButton=findViewById(R.id.add_plans_fab);

        inputPlanDialogView=getLayoutInflater().inflate(R.layout.add_plan_dialog,null);
        app= GymApplication.getGlobalAppInstance();
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("FitGym Plans");
        plansLoadingView=findViewById(R.id.plans_loading_text);


        plansRecyclerView=findViewById(R.id.plans_recyclerview);
        plansRecyclerView.setLayoutManager(new LinearLayoutManager(ManagePlansActivity.this));

        user=app.currentUser();

        if(user!=null){
            realm=Realm.getInstance(new SyncConfiguration
                    .Builder(user,"subscription")
                    .allowWritesOnUiThread(true)
                    .allowQueriesOnUiThread(true)
                    .build());
            plansResults=realm.where(plans.class).findAll();
            if(plansResults.size()!=0){
                plansLoadingView.setVisibility(View.INVISIBLE);
            }
        }

        publishPlanButton=inputPlanDialogView.findViewById(R.id.publish_button);
        planTitle=inputPlanDialogView.findViewById(R.id.input_plan_title);
        planDesc=inputPlanDialogView.findViewById(R.id.input_plan_perks);
        planPrice=inputPlanDialogView.findViewById(R.id.input_plan_price);
        planValidity=inputPlanDialogView.findViewById(R.id.input_plan_validity);
        planTypeSpinner=inputPlanDialogView.findViewById(R.id.input_plan_type_spinner);

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setView(inputPlanDialogView);
        builder.setCancelable(true);
        inputPlanDialog= builder.create();

        plansAdapter=new PlansAdapter(plansResults,inputPlanDialog,this);
        plansRecyclerView.setAdapter(plansAdapter);

        publishPlanButton.setOnClickListener(view->{
            title=planTitle.getText().toString();
            desc=planDesc.getText().toString();
            price=planPrice.getText().toString();
            validity=planValidity.getText().toString();
            type=planTypeSpinner.getSelectedItem().toString();

            if(!title.isEmpty()&&!desc.isEmpty()&&!price.isEmpty()&&!validity.isEmpty()&&!type.isEmpty()){

                int IDCount;
                if(plansResults.size()==0){
                    IDCount=0;
                }
                else{
                    plans lastPlan=plansResults.last();
                    if(lastPlan!=null){
                        IDCount=Integer.parseInt(lastPlan.getPlanID().substring(1))+1;
                    }else{
                        IDCount=0;
                    }
                }
                    plans p=new plans();

                    p.setPrice(price);
                    p.setDescription(desc);
                    p.setDuration(validity);
                    p.setTitle(title);
                    p.set_partition();
                    p.setPlanID((type.charAt(0) + "P-").toUpperCase()+IDCount);

                    realm.executeTransaction(realm -> {
                        realm.insertOrUpdate(p);
                    });
                Toast.makeText(this, "Plan added!",Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this,"Fields must not be left empty!",Toast.LENGTH_SHORT).show();
            }

            inputPlanDialog.dismiss();
        });

        addNewPlanButton.setOnClickListener(view->{
            inputPlanDialog.show();
        });

        plansAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                refreshAdapter();
            }
        });


        plansResults.addChangeListener((plans,changeSet)->{
            plansAdapter.notifyDataSetChanged();
        });
    }

    private void refreshAdapter() {
        plansResults=realm.where(plans.class).findAll();
        if(plansResults.size()==0){
            plansLoadingView.setText("All plans and subscriptions will appear here!");
            plansLoadingView.setVisibility(View.VISIBLE);
        }else{
            plansLoadingView.setVisibility(View.INVISIBLE);
        }
        PlansAdapter newAdapter=new PlansAdapter(plansResults,inputPlanDialog,this);
        plansRecyclerView.setAdapter(newAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        realm.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("PLANS ACTIVITY","Destroyed!!");
        realm.close();
    }
}