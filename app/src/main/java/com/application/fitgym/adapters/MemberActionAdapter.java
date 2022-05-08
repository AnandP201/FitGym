package com.application.fitgym.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.application.fitgym.MemberActionInterface;
import com.application.fitgym.R;
import com.application.fitgym.models.CustomModels.MemberObject;

import java.util.List;

public class MemberActionAdapter extends RecyclerView.Adapter<MemberActionAdapter.MemberHolder> {

    List<MemberObject> list;
    Context context;
    MemberActionInterface actionInterface;
    public MemberActionAdapter(List<MemberObject> param, Context pc, MemberActionInterface memberActionInterface){
        this.list=param;
        this.context=pc;
        this.actionInterface=memberActionInterface;
    }

    @NonNull
    @Override
    public MemberHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.member_action_item,parent,false);
        MemberHolder memberHolder=new MemberHolder(view);
        return memberHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MemberHolder holder, int position) {
        MemberObject o=list.get(position);

        holder.nameText.setText(o.name);
        holder.idText.setText(String.format("GYM ID : %s",o.id));
        byte []imgBytes=o.b;

        Bitmap bitmap= BitmapFactory.decodeByteArray(imgBytes,0,imgBytes.length);
        RoundedBitmapDrawable roundedBitmapDrawable= RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
        roundedBitmapDrawable.setCircular(true);
        holder.img.setImageDrawable(roundedBitmapDrawable);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MemberHolder extends RecyclerView.ViewHolder{

        TextView nameText,idText,memberAction;
        ImageView img;
        public MemberHolder(@NonNull View itemView) {
            super(itemView);

            Animation slideIn= AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            itemView.setAnimation(slideIn);

            nameText=itemView.findViewById(R.id.member_action_name_text);
            idText=itemView.findViewById(R.id.member_action_gym_id_text);
            img=itemView.findViewById(R.id.member_action_profile_picture);
            memberAction=itemView.findViewById(R.id.member_action);

            memberAction.setOnClickListener(view->{
                actionInterface.showActionDialog(getAdapterPosition());
            });

        }
    }
}
