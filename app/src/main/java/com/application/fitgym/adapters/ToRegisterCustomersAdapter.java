package com.application.fitgym.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.application.fitgym.ApproveCustomerInterface;
import com.application.fitgym.R;
import com.application.fitgym.models.RealmModels.customers;
import com.application.fitgym.models.RealmModels.resources;

import io.realm.RealmResults;

public class ToRegisterCustomersAdapter extends RecyclerView.Adapter<ToRegisterCustomersAdapter.CustomerItemViewHolder> {

    private RealmResults<customers> customersList;
    private RealmResults<resources> resourcesList;
    ApproveCustomerInterface approveCustomerInterface;
    Context context;

    public ToRegisterCustomersAdapter(RealmResults<customers> paramListCustomers, RealmResults<resources> paramListResource,ApproveCustomerInterface paramInterface,Context paramContext){
        this.customersList=paramListCustomers;
        this.resourcesList=paramListResource;
        this.approveCustomerInterface=paramInterface;
        this.context=paramContext;
    }

    @NonNull
    @Override
    public CustomerItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater=LayoutInflater.from(parent.getContext());
        View view=layoutInflater.inflate(R.layout.customer_item_layout,parent,false);

        CustomerItemViewHolder customerViewHolder=new CustomerItemViewHolder(view,approveCustomerInterface);
        return customerViewHolder;
    }

    @Override
    public void onBindViewHolder(CustomerItemViewHolder holder, int position) {

        holder.buttonTextView.setOnClickListener(view -> {
            approveCustomerInterface.approveCustomer(position);
        });

        customers currC=customersList.get(position);
        resources r=resourcesList.where().beginsWith("userID",currC.getAuthID()).findFirst();

        byte b[] = new byte[0];
        if(r!=null){
            b=r.getData();
        }

        if(b.length!=0){
            Bitmap bitmap= BitmapFactory.decodeByteArray(b,0,b.length);
            RoundedBitmapDrawable roundedBitmapDrawable= RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
            roundedBitmapDrawable.setCircular(true);
            holder.profileImageView.setImageDrawable(roundedBitmapDrawable);
        }

        holder.extraTextView.setText(String.format("%s , %s ",currC.getAge(),currC.getGender()));
        holder.nameTextView.setText(currC.getName());

    }

    @Override
    public int getItemCount() {
       return customersList.size();
    }


    class CustomerItemViewHolder extends RecyclerView.ViewHolder{

        ImageView profileImageView;
        TextView nameTextView,extraTextView,buttonTextView;
        public CustomerItemViewHolder(@NonNull View itemView,ApproveCustomerInterface addInterface) {
            super(itemView);

            itemView.setOnClickListener(view -> {
                addInterface.approveCustomer(getAdapterPosition());
            });


            profileImageView=itemView.findViewById(R.id.customer_profile_picture);
            nameTextView=itemView.findViewById(R.id.customer_name_text);
            extraTextView=itemView.findViewById(R.id.customer_extra_dets);
            buttonTextView=itemView.findViewById(R.id.customer_action_button);
        }
    }
}
