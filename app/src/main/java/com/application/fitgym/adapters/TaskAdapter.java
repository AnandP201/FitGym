package com.application.fitgym.adapters;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.application.fitgym.R;
import com.application.fitgym.models.tasks;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import io.realm.RealmList;
import io.realm.RealmResults;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    RealmResults<tasks> taskList;
    Context c;
    public TaskAdapter(RealmResults<tasks> paramList,Context paramContext) {
        this.taskList=paramList;
        this.c=paramContext;
    }



    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater=LayoutInflater.from(parent.getContext());
        View view=layoutInflater.inflate(R.layout.task_card_layout,parent,false);

        TaskViewHolder taskViewHolder=new TaskViewHolder(view);

        return taskViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
            holder.taskNoT.setText(String.format("%d",position+1));

            tasks t=taskList.get(position);

            holder.titleT.setText(t.getTitle());
            holder.descT.setText(t.getDescription());
            holder.createdOnT.setText(t.getCreatedOn());


    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }


    class TaskViewHolder extends RecyclerView.ViewHolder{
        TextView titleT,descT,taskNoT,createdOnT;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnLongClickListener(view->{
                tasks remove = taskList.get(getAdapterPosition());
                taskList.getRealm().executeTransaction(realm->{
                    remove.deleteFromRealm();
                });
                Toast.makeText(view.getContext(), "Task deleted!", Toast.LENGTH_SHORT).show();
                return true;
            });

            titleT=itemView.findViewById(R.id.task_card_title);
            descT=itemView.findViewById(R.id.task_card_description);
            taskNoT=itemView.findViewById(R.id.task_card_number_text);
            createdOnT=itemView.findViewById(R.id.task_card_creation_time);
        }
    }
}
