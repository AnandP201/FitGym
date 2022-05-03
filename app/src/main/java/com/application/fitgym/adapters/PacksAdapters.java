package com.application.fitgym.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.application.fitgym.BuyPlanInterface;
import com.application.fitgym.R;
import com.application.fitgym.models.plans;

import io.realm.Realm;
import io.realm.RealmResults;

public class PacksAdapters extends RecyclerView.Adapter<PacksAdapters.PackViewHolder> {

    RealmResults<plans> plansList;
    BuyPlanInterface clickInterface;

    public PacksAdapters(RealmResults<plans> paramList,BuyPlanInterface paramInterface){
        this.plansList=paramList;
        this.clickInterface=paramInterface;
    }


    @NonNull
    @Override
    public PackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater=LayoutInflater.from(parent.getContext());

        View view=inflater.inflate(R.layout.plans_item_layout,parent,false);

        PackViewHolder packViewHolder=new PackViewHolder(view);

        return packViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PackViewHolder holder, int position) {
            plans current=plansList.get(position);

            holder.planName.setText(current.getTitle());
            holder.planDesc.setText(current.getDescription());
            holder.planDuration.setText(String.format("%s months validity",current.getDuration()));
            holder.planPrice.setText(String.format("₹ %s /-",current.getPrice()));
    }


    @Override
    public int getItemCount() {
        return plansList.size();
    }

    class PackViewHolder extends RecyclerView.ViewHolder{

        TextView planName,planDesc,planDuration,planPrice,buyPackBtn;
         public PackViewHolder(@NonNull View itemView) {
            super(itemView);

            planName=itemView.findViewById(R.id.plan_name_text);
            planDesc=itemView.findViewById(R.id.plan_desc_text);
            planDuration=itemView.findViewById(R.id.plan_duration_text);
            planPrice=itemView.findViewById(R.id.plan_price_text);

            buyPackBtn=itemView.findViewById(R.id.buy_plan_text_clickable);

            buyPackBtn.setOnClickListener(view -> {
                clickInterface.buyPlan(getAdapterPosition());
            });
        }
    }
}
