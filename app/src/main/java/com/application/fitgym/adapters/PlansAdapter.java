package com.application.fitgym.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.application.fitgym.PlanEditInterface;
import com.application.fitgym.R;
import com.application.fitgym.models.RealmModels.plans;

import io.realm.RealmResults;

public class PlansAdapter extends RecyclerView.Adapter<PlansAdapter.TaskViewHolder> {

    RealmResults<plans> plansList;
    PlanEditInterface mainInterface;


    public PlansAdapter(RealmResults<plans> params,PlanEditInterface paramInterface){
        this.plansList=params;
        this.mainInterface=paramInterface;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater=LayoutInflater.from(parent.getContext());
        View view=layoutInflater.inflate(R.layout.admin_plan_item,parent,false);


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
                mainInterface.editPlan(getAdapterPosition());
                return false;
            });

            planNameView=itemView.findViewById(R.id.plan_name);
            planDurationView=itemView.findViewById(R.id.plan_duration);
            planPriceView=itemView.findViewById(R.id.plan_price);
            planIDView=itemView.findViewById(R.id.plan_id);
        }

    }
}
