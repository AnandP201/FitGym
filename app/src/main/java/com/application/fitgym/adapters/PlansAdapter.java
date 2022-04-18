package com.application.fitgym.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.application.fitgym.R;
import com.application.fitgym.models.plans;

import io.realm.RealmResults;

public class PlansAdapter extends RecyclerView.Adapter<PlansAdapter.TaskViewHolder> {

    RealmResults<plans> plansList;
    View dialogView;
    Button editButton;
    Spinner planTypeSpinner;
    int pos;
    String title,desc,validity,price;
    AlertDialog editPlanDialog;
    TextView editDialogTitleView,editDialogDescView,editDialogPriceView,editDialogDurationView,captionEditPlan;
    Context c;

    public PlansAdapter(RealmResults<plans> params,AlertDialog paramDialog,Context paramContext){
        this.plansList=params;
        this.editPlanDialog=paramDialog;
        this.c=paramContext;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater=LayoutInflater.from(parent.getContext());
        View view=layoutInflater.inflate(R.layout.admin_plan_item,parent,false);
        dialogView=layoutInflater.inflate(R.layout.add_plan_dialog,null);

        TaskViewHolder taskViewHolder=new TaskViewHolder(view);

        return taskViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {

        plans current=plansList.get(position);

        holder.planNameView.setText(current.getTitle());
        holder.planDurationView.setText(String.format("Valid for %s months",current.getDuration()));
        holder.planPriceView.setText(String.format("₹ %s",current.getPrice()));
        holder.planIDView.setText(current.getPlanID());

        Animation animation= AnimationUtils.loadAnimation(c, android.R.anim.slide_in_left);
        holder.itemView.setAnimation(animation);


        editButton=dialogView.findViewById(R.id.publish_button);
        editDialogTitleView=dialogView.findViewById(R.id.input_plan_title);
        editDialogDescView=dialogView.findViewById(R.id.input_plan_perks);
        editDialogPriceView=dialogView.findViewById(R.id.input_plan_price);
        editDialogDurationView=dialogView.findViewById(R.id.input_plan_validity);
        planTypeSpinner=dialogView.findViewById(R.id.input_plan_type_spinner);
        captionEditPlan=dialogView.findViewById(R.id.caption_edit_plan);

        editButton.setOnClickListener(view->{
            title=editDialogTitleView.getText().toString();
            desc=editDialogDescView.getText().toString();
            price=editDialogPriceView.getText().toString();
            validity=editDialogDurationView.getText().toString();


            if(!title.isEmpty()&&!desc.isEmpty()&&!price.isEmpty()&&!validity.isEmpty()){
                  plansList.getRealm().executeTransaction(realm -> {
                      plans toeditPlan=plansList.get(pos);
                      toeditPlan.setDescription(desc);
                      toeditPlan.setTitle(title);
                      toeditPlan.setDuration(validity);
                      toeditPlan.setPrice(price);
                  });

                Toast.makeText(dialogView.getContext(), "Edited successfully!",Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(dialogView.getContext(), "Fields must not be left empty!",Toast.LENGTH_SHORT).show();
            }

            editPlanDialog.dismiss();
        });

        editPlanDialog.setView(dialogView);
    }

    @Override
    public int getItemCount() {
        return plansList.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder{

        TextView planNameView,planDurationView,planPriceView,planIDView;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);


            itemView.setOnLongClickListener(view -> {
                plans currentPlan=plansList.get(getAdapterPosition());
                pos=getAdapterPosition();
                editButton.setText("EDIT");
                editDialogTitleView.setText(currentPlan.getTitle());
                editDialogDescView.setText(currentPlan.getDescription());
                editDialogDurationView.setText(currentPlan.getDuration());
                editDialogPriceView.setText(currentPlan.getPrice());
                captionEditPlan.setText(("Edit "+currentPlan.getPlanID()).toUpperCase());
                if(currentPlan.getPlanID().charAt(0)=='N'){
                    planTypeSpinner.setSelection(0);
                }else{
                    planTypeSpinner.setSelection(1);
                }
                planTypeSpinner.setEnabled(false);

                editPlanDialog.show();

                return false;
            });

            planNameView=itemView.findViewById(R.id.plan_name);
            planDurationView=itemView.findViewById(R.id.plan_duration);
            planPriceView=itemView.findViewById(R.id.plan_price);
            planIDView=itemView.findViewById(R.id.plan_id);


        }

    }
}
