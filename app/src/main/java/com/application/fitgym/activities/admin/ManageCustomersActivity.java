package com.application.fitgym.activities.admin;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.application.fitgym.MemberActionInterface;
import com.application.fitgym.R;
import com.application.fitgym.adapters.MemberActionAdapter;
import com.application.fitgym.helpers.GymApplication;
import com.application.fitgym.models.CustomModels.MemberObject;

import org.bson.Document;
import org.bson.types.Binary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.mongodb.App;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.FindIterable;
import io.realm.mongodb.mongo.iterable.MongoCursor;


public class ManageCustomersActivity extends AppCompatActivity implements MemberActionInterface {

    Toolbar toolbar;
    EditText editText;
    SwitchCompat switchCompat;
    RecyclerView recyclerView;
    MemberActionAdapter adapter;
    ProgressBar loadingBar;
    Button button,removeButton,loadButton;
    static boolean STOP_LOADING=false;
    AlertDialog removeMemberDialog;
    TextView loadingText,dialogDetsTV,dialogPlansTV,dialogmemSinceTV;
    LinearLayoutManager linearLayoutManager;

    List<MemberObject> mainList;
    String TO_REMOVE_ID;
    String TO_REMOVE_GYM_ID;
    List<MemberObject> currentList;

    App app;
    User user;

    @Override
    public void showActionDialog(int position) {
        MemberObject o=currentList.get(position);

        TO_REMOVE_ID=o.authID;
        TO_REMOVE_GYM_ID=o.id;

        dialogDetsTV.setText(String.format("%s active for %s more days.",o.name.split(" ")[0],o.daysLeft));
        dialogmemSinceTV.setText(String.format("FitGym member since %s",o.memberSince));
        dialogPlansTV.setText(String.format("Plans active : %s",o.plans));

        removeMemberDialog.show();
    }


    class fetchList extends AsyncTask<Void,Void,List<MemberObject>>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingBar.setVisibility(View.VISIBLE);
            loadingText.setVisibility(View.VISIBLE);
            switchCompat.setEnabled(false);
            editText.setEnabled(false);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected List<MemberObject> doInBackground(Void... voids) {

            List<MemberObject> list=new ArrayList<>();
            MongoClient client=user.getMongoClient("mongodb-atlas");

            MongoDatabase db=client.getDatabase("GymDB");
            MongoCollection<Document> customerColl=db.getCollection("customers");
            MongoCollection<Document> statusColl=db.getCollection("status");
            MongoCollection<Document> resColl=db.getCollection("resources");

            Document filter=new Document("RegistrationStatus","OK");


            FindIterable<Document> cDocs=customerColl.find(filter);


            MongoCursor<Document> cursor1=cDocs.iterator().get();


            cursor1.forEachRemaining(doc->{
                String name=doc.getString("Name");
                String authID=doc.getString("authID");

                Document statusFilter=new Document("userAuthID",authID);
                Document resourceFilter=new Document("userID",authID);

                Document s=statusColl.find(statusFilter).first().get();
                Document r=resColl.find(resourceFilter).first().get();

                String id=s.getString("gymUserID");
                String plans=s.getString("activePlans");
                String memSince=s.getString("memberSince");
                String dur=s.getString("planActiveDuration");
                byte []b=((Binary)r.get("Data")).getData();

                MemberObject o=new MemberObject(id,name,dur,plans,authID,memSince,b);
                list.add(o);
            });


            return list;
        }

        @Override
        protected void onPostExecute(List<MemberObject> list) {
            super.onPostExecute(list);
            loadingBar.setVisibility(View.GONE);
            loadingText.setVisibility(View.INVISIBLE);
            switchCompat.setEnabled(true);

            mainList.addAll(list);
            currentList.addAll(mainList);
            adapter=new MemberActionAdapter(currentList,ManageCustomersActivity.this,ManageCustomersActivity.this);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_customers);
        toolbar=findViewById(R.id.admin_managecust_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();

        linearLayoutManager=new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration=new DividerItemDecoration(this,linearLayoutManager.getOrientation());

        recyclerView=findViewById(R.id.manage_cust_recycler_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);

        switchCompat=findViewById(R.id.switch1);
        loadingText=findViewById(R.id.manage_cust_loading_text);
        loadingBar=findViewById(R.id.manage_cust_loading);
        editText=findViewById(R.id.filter_text);
        button=findViewById(R.id.search_cust_filter);

        View dialogView=getLayoutInflater().inflate(R.layout.member_action_dialog,null);

        dialogDetsTV=dialogView.findViewById(R.id.member_action_dialog_primary_dets);
        dialogmemSinceTV=dialogView.findViewById(R.id.member_action_dialog_memSince);
        dialogPlansTV=dialogView.findViewById(R.id.member_action_dialog_plans);

        removeButton=dialogView.findViewById(R.id.member_action_remove_member);

        removeButton.setOnClickListener(view->{
            removeMember();
        });


        AlertDialog.Builder builder=new AlertDialog
                .Builder(this)
                .setView(dialogView)
                .setCancelable(true);

        removeMemberDialog=builder.create();


        actionBar.setTitle("FitGym Customers");
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStart() {
        super.onStart();

        app= GymApplication.getGlobalAppInstance();
        user=app.currentUser();

        if(!STOP_LOADING){
            mainList=new ArrayList<>();
            currentList=new ArrayList<>();


            new fetchList().execute();
        }

        switchCompat.setOnCheckedChangeListener((compoundButton, b) -> {
            loadingText.setVisibility(View.INVISIBLE);
            if(b){
                editText.setEnabled(true);
            }else{
                currentList.clear();
                editText.setText("");
                currentList.addAll(mainList);
                adapter=new MemberActionAdapter(currentList,this,this);
                recyclerView.setAdapter(adapter);
                editText.setEnabled(false);
            }
        });

        button.setOnClickListener(view->{
            currentList.clear();
            if(loadingText.getVisibility()==View.VISIBLE){
                loadingText.setVisibility(View.INVISIBLE);
            }
            String filter=editText.getText().toString();
            if(filter.isEmpty()){
                Toast.makeText(this, "ID cannot be empty!", Toast.LENGTH_SHORT).show();
            }else{
                for(MemberObject o:mainList){
                    if(o.id.contains(filter)){
                        currentList.add(o);
                        break;
                    }
                }
            }
            if(currentList.isEmpty()){
                loadingText.setText("No customer found!");
                loadingText.setVisibility(View.VISIBLE);
                recyclerView.setAdapter(null);
            }else{
                adapter=new MemberActionAdapter(currentList,this,this);
                recyclerView.setAdapter(adapter);
            }

        });
    }

    class RemoveAsync extends AsyncTask<String,Void,Void>{


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            removeMemberDialog.dismiss();
            recyclerView.setAdapter(null);
            loadingText.setText("Processing deletion. Don't exit the window!");
            loadingText.setVisibility(View.VISIBLE);
            loadingBar.setVisibility(View.VISIBLE);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Void doInBackground(String... str) {

            currentList.clear();
            mainList.clear();

            String rId=str[0];

            MongoClient client=user.getMongoClient("mongodb-atlas");

            MongoDatabase db=client.getDatabase("GymDB");

            MongoCollection<Document> customerColl=db.getCollection("customers");
            MongoCollection<Document> statusColl=db.getCollection("status");
            MongoCollection<Document> resourceColl=db.getCollection("resources");
            MongoCollection<Document> adminColl=db.getCollection("admin");

            Document targetC=new Document("authID",rId);
            Document targetS=new Document("userAuthID",rId);
            Document resourceC=new Document("userID",rId);

            Document update=new Document("$set",new Document("RegistrationStatus","INV"));

            String ids=adminColl.find().first().get().get("customerIDs").toString();

            List<String> idList=new ArrayList<>();
            idList.addAll(Arrays.asList(ids.substring(1,ids.length()-1).split(", ")));

            List<String> toRemoveList=new ArrayList<>();

            for(String id:idList){
                if(!id.equalsIgnoreCase(TO_REMOVE_GYM_ID)){
                    toRemoveList.add(id);
                }
            }

            String toReg=toRemoveList.toString();
            Document f=new Document("adminName","admin@fitgym");
            Document idDoc=new Document("customerIDs",toReg);

            runOnUiThread(() -> {
                adminColl.findOneAndUpdate(f,idDoc).getAsync(response->{
                    if(response.isSuccess()){
                        Toast.makeText(ManageCustomersActivity.this, "Admin list of IDs updated!", Toast.LENGTH_SHORT).show();
                    }
                });

                statusColl.findOneAndDelete(targetS).getAsync(response->{
                    if(response.isSuccess()){
                        Toast.makeText(ManageCustomersActivity.this, "Status document removed!", Toast.LENGTH_SHORT).show();
                    }
                });

                resourceColl.findOneAndDelete(resourceC).getAsync(response->{
                    if(response.isSuccess()){
                        Toast.makeText(ManageCustomersActivity.this, "Resource document removed!", Toast.LENGTH_SHORT).show();
                    }
                });
                customerColl.findOneAndUpdate(targetC,update).getAsync(response->{
                    if(response.isSuccess()){
                        Toast.makeText(ManageCustomersActivity.this, "Customer banned!", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            super.onPostExecute(voids);
            Toast.makeText(ManageCustomersActivity.this, "Customer deleted! Back to main activity!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ManageCustomersActivity.this,AdminActivity.class));
            finish();
        }
    }


    private void removeMember(){
        new RemoveAsync().execute(TO_REMOVE_ID);
    }

}