package com.application.fitgym.activities.customer;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.application.fitgym.R;
import com.application.fitgym.adapters.TaskAdapter;
import com.application.fitgym.helpers.GymApplication;
import com.application.fitgym.models.tasks;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.mongodb.App;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.SyncConfiguration;

public class TaskActivity extends AppCompatActivity {

    private View taskInputView;
    private RecyclerView taskRecyclerView;
    private RealmResults<tasks> taskList;
    private Realm taskRealm;
    private App app;
    private User user;
    private EditText search_editText;
    private TaskAdapter taskAdapter;
    private Toolbar toolbar;
    private FloatingActionButton addNewTaskButton;
    private Button saveTaskButton;
    private AlertDialog inputTaskDialog;
    private TextView createdOnTimeStampText,input_title,input_description,noTaskDisplayText;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_task);
        noTaskDisplayText=findViewById(R.id.no_task_display);
        app= GymApplication.getGlobalAppInstance();
        user=app.currentUser();

        taskRealm=Realm.getInstance(new SyncConfiguration
                .Builder(user,"work")
                .allowWritesOnUiThread(true)
                .allowWritesOnUiThread(true)
                .build());


        toolbar=findViewById(R.id.taskact_app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Daily notes/tasks");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        taskList=taskRealm.where(tasks.class).contains("userID",user.getId()).findAll();
        taskInputView=getLayoutInflater().inflate(R.layout.task_input_layout,null);


        taskRecyclerView=findViewById(R.id.tasks_recyclerview);

        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if(taskList.size()==0){
            toogleTaskVisibleViews(0);
        }else{
            toogleTaskVisibleViews(1);
        }

        taskAdapter=new TaskAdapter(taskList,this);
        taskRecyclerView.setAdapter(taskAdapter);

        addNewTaskButton=findViewById(R.id.add_task_fab);
        saveTaskButton=taskInputView.findViewById(R.id.add_task_button);
        createdOnTimeStampText=taskInputView.findViewById(R.id.task_creation_time_box);
        input_title=taskInputView.findViewById(R.id.input_task_title);
        input_description=taskInputView.findViewById(R.id.input_description_box);

        AlertDialog.Builder myDialogBuilder=new AlertDialog.Builder(this);

        myDialogBuilder.setView(taskInputView);
        myDialogBuilder.setCancelable(true);
        inputTaskDialog=myDialogBuilder.create();

        saveTaskButton.setOnClickListener(view->{

            String textTitle=input_title.getText().toString();
            String textDesc=input_description.getText().toString();

            if(textTitle.isEmpty() || textDesc.isEmpty()){
                inputTaskDialog.dismiss();
                Toast.makeText(this,"EMPTY TASK? Are you sure...",Toast.LENGTH_SHORT).show();
            }else{
                tasks task=new tasks();

                task.setCreatedOn(createdOnTimeStampText.getText().toString());
                task.setTitle(textTitle);
                task.setDescription(textDesc);
                task.set_partition();
                task.setUserID(user.getId());

                taskRealm.executeTransaction(realm->{
                    taskRealm.insertOrUpdate(task);
                });
                clearAndClose();
                Toast.makeText(this,"Task created!",Toast.LENGTH_SHORT).show();
            }
        });




        addNewTaskButton.setOnClickListener(view -> {
            createdOnTimeStampText.setText(getCurrentTime());
            inputTaskDialog.show();
        });

        taskAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                refreshAdapter();
            }
        });

        taskList.addChangeListener((tasks,changeSet)->{
            taskAdapter.notifyDataSetChanged();
        });

    }




    private void toogleTaskVisibleViews(int val){
        if(val==0){
            taskRecyclerView.setVisibility(View.INVISIBLE);
            if(noTaskDisplayText.getVisibility()==View.INVISIBLE){
                noTaskDisplayText.setVisibility(View.VISIBLE);
            }
            noTaskDisplayText.setText("Your tasks/notes appear here");
        }else{
            noTaskDisplayText.setVisibility(View.INVISIBLE);
            taskRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void refreshAdapter(){
        taskList=taskRealm.where(tasks.class).contains("userID",user.getId()).findAll();
        if(taskList.size()==0){
            toogleTaskVisibleViews(0);
        }else{
            toogleTaskVisibleViews(1);
        }
        TaskAdapter newAdapter=new TaskAdapter(taskList,this);
        taskRecyclerView.setAdapter(newAdapter);
    }

    private void clearAndClose() {
        input_title.setText("");
        input_description.setText("");
        inputTaskDialog.dismiss();
    }



    private String getCurrentTime(){
        SimpleDateFormat sdf=new SimpleDateFormat("EE MM yy HH:mm:ss");
        Calendar calendar=Calendar.getInstance();
        return sdf.format(calendar.getTime());
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        taskRealm.close();
    }
}
