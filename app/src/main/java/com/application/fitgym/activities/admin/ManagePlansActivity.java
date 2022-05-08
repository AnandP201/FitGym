package com.application.fitgym.activities.admin;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.application.fitgym.PlanEditInterface;
import com.application.fitgym.R;
import com.application.fitgym.adapters.PlansAdapter;
import com.application.fitgym.helpers.GymApplication;
import com.application.fitgym.models.RealmModels.plans;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.mongodb.App;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.SyncConfiguration;

public class ManagePlansActivity extends AppCompatActivity implements PlanEditInterface {

    Toolbar toolbar;
    FloatingActionButton addNewPlanButton;
    private AlertDialog inputPlanDialog;
    View inputPlanDialogView;
    Button publishPlanButton;
    EditText planTitle,planDesc,planPrice,planValidity;
    private Spinner planTypeSpinner;
    String title,desc,price,validity,type;
    App app;
    private PlansAdapter plansAdapter;
    RealmChangeListener<RealmResults<plans>> plansListener;
    User user;
    private RecyclerView plansRecyclerView;
    private Realm realm;
    private RealmResults<plans> plansResults;
    private TextView plansLoadingView;

    private plans toEditPlan;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_plans);

        toolbar=findViewById(R.id.admin_manageplans_toolbar);
        addNewPlanButton=findViewById(R.id.add_plans_fab);

        plansRecyclerView=findViewById(R.id.plans_recyclerview);
        plansRecyclerView.setLayoutManager(new LinearLayoutManager(ManagePlansActivity.this));

        inputPlanDialogView=getLayoutInflater().inflate(R.layout.add_plan_dialog,null);
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


        app= GymApplication.getGlobalAppInstance();
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("FitGym Plans");

        plansLoadingView=findViewById(R.id.plans_loading_text);


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

        plansAdapter=new PlansAdapter(plansResults,this);
        plansRecyclerView.setAdapter(plansAdapter);

        //button for add plan alert dialog
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
                        IDCount=Integer.parseInt(lastPlan.getPlanID().substring(3))+1;
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

        plansAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                refreshAdapter();
            }
        });

        addNewPlanButton.setOnClickListener(view->{
            inputPlanDialog.show();
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        plansListener=(RealmChangeListener<RealmResults<plans>>)results->{
            plansAdapter.notifyDataSetChanged();
        } ;

        plansResults.addChangeListener(plansListener);
    }

    private void refreshAdapter() {
        plansResults=realm.where(plans.class).findAll();
        if(plansResults.size()==0){
            plansLoadingView.setText("All plans and subscriptions will appear here!");
            plansLoadingView.setVisibility(View.VISIBLE);
        }else{
            plansLoadingView.setVisibility(View.INVISIBLE);
        }
        PlansAdapter newAdapter=new PlansAdapter(plansResults,this);
        plansRecyclerView.setAdapter(newAdapter);
    }


    private void setViewForEditDialogAndShow(plans p){
        Button editButton;

        TextView captionEditPlan;
        Spinner editDialogSpinner;
        AlertDialog.Builder builderForEditPlanDialog;

        AlertDialog editPlanDialog;

        LayoutInflater inflater=this.getLayoutInflater();
        View editDialogPlanView=inflater.inflate(R.layout.edit_plan_dialog,null);

        final EditText editDialogDesc=(EditText) editDialogPlanView.findViewById(R.id.edit_plan_perks);
        editDialogDesc.setText(p.getDescription());

        final EditText editDialogTitle=(EditText)editDialogPlanView.findViewById(R.id.edit_plan_tts);
        editDialogTitle.setText(p.getTitle());

        final EditText editDialogDuration=(EditText) editDialogPlanView.findViewById(R.id.edit_plan_validity);
        editDialogDuration.setText(p.getDuration());

        final EditText editDialogPrice=(EditText) editDialogPlanView.findViewById(R.id.edit_plan_price);
        editDialogPrice.setText(p.getPrice());


        builderForEditPlanDialog=new AlertDialog
                .Builder(this)
                .setCancelable(true)
                .setView(editDialogPlanView);


        captionEditPlan=(TextView) editDialogPlanView.findViewById(R.id.caption_editdialog_plan);
        editDialogSpinner=(Spinner) editDialogPlanView.findViewById(R.id.edit_plan_type_spinner);
        editButton=(Button) editDialogPlanView.findViewById(R.id.edit_plan_button);

        captionEditPlan.setText(("Edit "+p.getPlanID()).toUpperCase());
        if(p.getPlanID().charAt(0)=='N'){
            editDialogSpinner.setSelection(0);
        }else{
            editDialogSpinner.setSelection(1);
        }
        editDialogSpinner.setEnabled(false);


        editPlanDialog=builderForEditPlanDialog.create();


        //button for edit plan alert dialog
        editButton.setOnClickListener(view->{
            String title=editDialogTitle.getText().toString();
            String desc=editDialogDesc.getText().toString();
            String price=editDialogPrice.getText().toString();
            String validity=editDialogDuration.getText().toString();


            if(!title.isEmpty()&&!desc.isEmpty()&&!price.isEmpty()&&!validity.isEmpty()){
                realm.executeTransaction(realm -> {
                    plans toeditPlan=this.toEditPlan;
                    toeditPlan.setDescription(desc);
                    toeditPlan.setTitle(title);
                    toeditPlan.setDuration(validity);
                    toeditPlan.setPrice(price);
                });

                Toast.makeText(this, "Edited successfully!",Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Fields must not be left empty!",Toast.LENGTH_SHORT).show();
            }
            editPlanDialog.dismiss();
        });

        editPlanDialog.show();




    }


    @Override
    public void editPlan(int position) {
        this.toEditPlan=plansResults.get(position);
        setViewForEditDialogAndShow(this.toEditPlan);
    }


    @Override
    protected void onPause() {
        super.onPause();

        plansResults.removeChangeListener(plansListener);

        realm.close();
    }

}