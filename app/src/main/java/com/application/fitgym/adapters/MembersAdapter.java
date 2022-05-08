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

import com.application.fitgym.MemberDetailsInterface;
import com.application.fitgym.R;
import com.application.fitgym.models.RealmModels.customers;
import com.application.fitgym.models.RealmModels.resources;
import com.application.fitgym.models.RealmModels.status;


import io.realm.RealmResults;


public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MemberItemHolder> {

    RealmResults<status> membersList;
    RealmResults<customers> customersList;
    RealmResults<resources> resourcesList;
    Context context;
    MemberDetailsInterface memberDetailsInterface;

    public MembersAdapter(Context c,MemberDetailsInterface param,RealmResults<status> results, RealmResults<customers> customersRealmResults, RealmResults<resources> resourcesRealmResults){
        this.membersList=results;
        this.customersList=customersRealmResults;
        this.context=c;
        this.resourcesList=resourcesRealmResults;
        this.memberDetailsInterface=param;
    }

    @NonNull
    @Override
    public MemberItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());

        View view=inflater.inflate(R.layout.member_item_layout,parent,false);


        MemberItemHolder memberItemHolder=new MemberItemHolder(view);

        return memberItemHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MemberItemHolder holder, int position) {

        status s;
        customers c;
        resources r;

        s=membersList.get(position);
        c=customersList.where().contains("authID",s.getUserAuthID()).findFirst();
        r=resourcesList.where().contains("userID",s.getUserAuthID()).findFirst();


        holder.nameTV.setText(c.getName().split(" ")[0]);

        byte b[]=r.getData();
        Bitmap bitmap= BitmapFactory.decodeByteArray(b,0,b.length);
        RoundedBitmapDrawable roundedBitmapDrawable= RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
        roundedBitmapDrawable.setCircular(true);
        holder.profileImgView.setImageDrawable(roundedBitmapDrawable);

        holder.idTv.setText(s.getGymUserID());

        String status=s.getStats();

        if(status.equalsIgnoreCase("offline")){
            holder.indicatorTv.setBackground(context.getResources().getDrawable(R.drawable.offline_icon));
        }else{
            holder.indicatorTv.setBackground(context.getResources().getDrawable(R.drawable.online_icon));
        }
    }

    @Override
    public int getItemCount() {
        return membersList.size();
    }

    class MemberItemHolder extends RecyclerView.ViewHolder{

        TextView nameTV,idTv,indicatorTv;
        ImageView profileImgView;
        public MemberItemHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(view->{
                memberDetailsInterface.openMemberDialog(getAdapterPosition());
            });

            nameTV=itemView.findViewById(R.id.member_name_text);
            idTv=itemView.findViewById(R.id.member_gym_id_text);
            indicatorTv= itemView.findViewById(R.id.member_status_indicator);

            profileImgView=itemView.findViewById(R.id.member_profile_picture);
        }
    }
}
