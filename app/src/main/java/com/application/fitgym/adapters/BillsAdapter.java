package com.application.fitgym.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.application.fitgym.R;
import com.application.fitgym.helpers.Bill;

import java.util.List;

public class BillsAdapter extends RecyclerView.Adapter<BillsAdapter.BillHolder> {

    List<Bill> list;

    public BillsAdapter(List<Bill> billList){
        this.list=billList;
    }

    @NonNull
    @Override
    public BillHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        View view=inflater.inflate(R.layout.bill_item_layout,parent,false);

        BillHolder billHolder=new BillHolder(view);

        return billHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BillHolder holder, int position) {

        Bill b=list.get(position);

        holder.amtText.setText(String.format("₹ %s",b.getBillAmount()));
        holder.onText.setText(String.format("Issued On : %s",b.getCreatedOn()));
        holder.forText.setText(String.format("Bill For : %s",b.getBillFor()));
        holder.invoiceText.setText(b.getInvoiceID());
        holder.titleText.setText(b.getBillTitle());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class BillHolder extends RecyclerView.ViewHolder{

        TextView invoiceText,titleText,forText,onText,amtText;

        public BillHolder(@NonNull View itemView) {
            super(itemView);

            Animation animation= AnimationUtils.loadAnimation(itemView.getContext(), android.R.anim.slide_in_left);
            itemView.setAnimation(animation);

            invoiceText=itemView.findViewById(R.id.bill_invoice_text);
            titleText=itemView.findViewById(R.id.bill_title_text);
            forText=itemView.findViewById(R.id.bill_for_text);
            onText=itemView.findViewById(R.id.bill_createdOn_text);
            amtText=itemView.findViewById(R.id.bill_amount_text);
        }
    }
}
