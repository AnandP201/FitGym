package com.application.fitgym.activities.admin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.application.fitgym.R;
import com.application.fitgym.adapters.BillsAdapter;
import com.application.fitgym.models.CustomModels.Bill;
import com.application.fitgym.helpers.GymApplication;

import org.bson.Document;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.mongodb.App;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.FindIterable;
import io.realm.mongodb.mongo.iterable.MongoCursor;

public class ManageInventoryActivity extends AppCompatActivity {


    Toolbar toolbar;
    App app;
    User Admin;
    List<Bill> billList;
    ProgressBar progressBar;
    RecyclerView recyclerView;
    final String[] MONTHS={"SELECT A MONTH..","JANUARY","FEBRUARY","MARCH","APRIL","MAY","JUNE","JULY","AUGUST","SEPTEMBER","OCTOBER","NOVEMBER","DECEMBER"};
    BillsAdapter adapter;

    Spinner usersListSpinner,monthFilterSpinner;
    RadioGroup filtersRG;
    CheckBox addFilter;
    Animation fadeIn;
    TextView loadingTextView,filtersAppliedText,totalBillAmountText,extraInfoText,extraInfoTitle;
    View filterLayout;
    List<Bill> currentList;
    LinearLayout filterLayoutView,extraInfoLayout,amountInfoLayout;


    LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_inventory);
        toolbar=findViewById(R.id.admin_manageinv_toolbar);
        progressBar=findViewById(R.id.admin_inventory_loading_bar);

        loadingTextView=findViewById(R.id.admin_inventory_loading_text);
        recyclerView=findViewById(R.id.admin_inventory_recycler_view);
        monthFilterSpinner=findViewById(R.id.month_filter_spinner_select);
        usersListSpinner=findViewById(R.id.admin_inventory_spinner);
        filtersRG=findViewById(R.id.admin_inventory_filter_rg);
        addFilter=findViewById(R.id.admin_inventory_add_filter_chk);
        filterLayout=findViewById(R.id.filters_layout);
        filtersAppliedText=findViewById(R.id.no_filters_text);
        filterLayoutView=findViewById(R.id.admin_inventory_filter_layout);
        amountInfoLayout=findViewById(R.id.amount_info_layout);

        totalBillAmountText=findViewById(R.id.total_bill_amount);
        extraInfoText=findViewById(R.id.extra_info_bill_amount);
        extraInfoLayout=findViewById(R.id.extra_info_layout);
        extraInfoTitle=findViewById(R.id.extra_info_title);


        linearLayoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration=new DividerItemDecoration(this,linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);


        filtersRG.setAlpha(0f);
        monthFilterSpinner.setVisibility(View.GONE);
        filtersAppliedText.setAlpha(1f);
        filterLayout.setAnimation(fadeIn);



        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("FitGym Inventory");
    }

    private int getMonthIndexFromArray(String month){
        for(int i=0;i<MONTHS.length;i++){
            if(month.equals(MONTHS[i])){
                return i;
            }
        }
        return 0;
    }


    @Override
    protected void onStart() {
        super.onStart();

        billList=new ArrayList<>();

        currentList=new ArrayList<>();


        List<String> monthList=new ArrayList<>(Arrays.asList(MONTHS));

        ArrayAdapter<String> monthsAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, monthList){
            @Override
            public boolean isEnabled(int position) {
                if(position==0){
                    return false;
                }
                else{
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view=super.getDropDownView(position, convertView, parent);
                TextView tv=(TextView) view;
                if(position==0){
                    tv.setTextColor(Color.GRAY);
                }
                return view;
            }
        };
        monthsAdapter.setDropDownViewResource(R.layout.spinner_item);
        monthFilterSpinner.setAdapter(monthsAdapter);
        monthFilterSpinner.setSelection(0);


        app= GymApplication.getGlobalAppInstance();
        Admin=app.currentUser();

        addFilter.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b){
                filtersRG.animate()
                        .alpha(1f)
                        .setDuration(100)
                        .setListener(null);
                filtersAppliedText.animate()
                        .alpha(0f)
                        .setDuration(100)
                        .setListener(null);
               monthFilterSpinner.setVisibility(View.VISIBLE);
            }else{
                adapter=new BillsAdapter(currentList);
                recyclerView.setAdapter(adapter);
                filtersRG.animate()
                        .alpha(0f)
                        .setDuration(100)
                        .setListener(null);
                filtersAppliedText.animate()
                        .alpha(1f)
                        .setDuration(100)
                        .setListener(null);
                monthFilterSpinner.setVisibility(View.GONE);
            }
        });


        monthFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            SimpleDateFormat sdf=new SimpleDateFormat("EE MM yy HH:mm:ss");
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String month=monthFilterSpinner.getSelectedItem().toString();

                int idx=getMonthIndexFromArray(month);

                List<Bill> toList=new ArrayList<>();

                for(Bill c:currentList){
                    try{
                        Date d=sdf.parse(c.getCreatedOn());
                        if((d.getMonth()+1)==idx){
                            toList.add(c);
                        }
                    }catch(Exception p){
                        Toast.makeText(ManageInventoryActivity.this, "Error caused while sorting based on month!", Toast.LENGTH_SHORT).show();
                    }
                }
                adapter=new BillsAdapter(toList);
                recyclerView.setAdapter(adapter);

                if(toList.size()>0){
                    if(loadingTextView.getVisibility()==View.VISIBLE){
                        loadingTextView.setVisibility(View.INVISIBLE);
                    }
                }else{
                   if(!month.equals(monthsAdapter.getItem(0))){
                       loadingTextView.setText("No bills found for the applied filter");
                       loadingTextView.setVisibility(View.VISIBLE);
                   }
                }



            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        filtersRG.setOnCheckedChangeListener((radioGroup, i) -> {
            int id=radioGroup.getCheckedRadioButtonId();

            switch (id){
                case R.id.older_first_radio:
                    filterAsc();
                    break;
                case R.id.newer_first_radio:
                    filterDesc();
                    break;
            }
        });

        usersListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String query=usersListSpinner.getSelectedItem().toString();
                currentList.clear();
                if(query.equalsIgnoreCase("ALL")){
                    if(extraInfoLayout.getVisibility()==View.VISIBLE){
                        extraInfoLayout.animate().translationX(View.GONE).setDuration(100).setListener(null);
                        extraInfoLayout.setVisibility(View.GONE);
                    }
                    if(loadingTextView.getVisibility()==View.VISIBLE){
                        loadingTextView.setVisibility(View.INVISIBLE);
                    }
                    currentList.addAll(billList);
                    adapter=new BillsAdapter(currentList);
                    recyclerView.setAdapter(adapter);
                }else{
                    List<Bill> toDisplay=new ArrayList<>();
                    for(Bill B:billList){
                        if(B.getBillFor().equalsIgnoreCase(query)){
                            toDisplay.add(B);
                        }
                    }
                    double sum=0;
                    for(Bill m:toDisplay){
                        sum+=Double.parseDouble(m.getBillAmount());
                    }
                    extraInfoLayout.animate().translationX(View.VISIBLE).setDuration(100).setListener(null);
                   extraInfoLayout.setVisibility(View.VISIBLE);
                    extraInfoTitle.setText(String.format("Billing amount %s",query));
                    extraInfoText.setText(String.format("₹ %s",formatCurrency(sum)));
                    if(toDisplay.size()>0){
                        if(loadingTextView.getVisibility()==View.VISIBLE){
                            loadingTextView.setVisibility(View.INVISIBLE);
                        }
                        currentList.addAll(toDisplay);
                        adapter=new BillsAdapter(currentList);
                        recyclerView.setAdapter(adapter);
                    }else{
                        loadingTextView.setText("No bills found for "+query);
                        loadingTextView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                currentList.clear();
                currentList.addAll(billList);
                adapter=new BillsAdapter(currentList);
                recyclerView.setAdapter(adapter);
            }
        });



        new FetchBills().execute();
    }

    class BillAscComparator implements Comparator<Bill>{

        @Override
        public int compare(Bill b1, Bill b2) {

            SimpleDateFormat sdf=new SimpleDateFormat("EE MM yy HH:mm:ss");
           try{
               Date d1=sdf.parse(b1.getCreatedOn());
               Date d2=sdf.parse(b2.getCreatedOn());

               return d1.compareTo(d2);
           }catch(ParseException p){
               Toast.makeText(ManageInventoryActivity.this, "Error while sorting!", Toast.LENGTH_SHORT).show();
           }
            return 0;
        }
    }

    class BillDescComparator implements Comparator<Bill>{

        @Override
        public int compare(Bill b1, Bill b2) {

            SimpleDateFormat sdf=new SimpleDateFormat("EE MM yy HH:mm:ss");
            try{
                Date d1=sdf.parse(b1.getCreatedOn());
                Date d2=sdf.parse(b2.getCreatedOn());

                return d2.compareTo(d1);
            }catch(ParseException p){
                Toast.makeText(ManageInventoryActivity.this, "Error while sorting!", Toast.LENGTH_SHORT).show();
            }
            return 0;
        }
    }


    private void filterAsc() {

        List<Bill> ascSortedList=new ArrayList<>();
        ascSortedList.addAll(currentList);
        Collections.sort(ascSortedList,new BillAscComparator());

        adapter=new BillsAdapter(ascSortedList);
        recyclerView.setAdapter(adapter);
    }

    private void filterDesc() {
        List<Bill> descSortedList=new ArrayList<>();
        descSortedList.addAll(currentList);
        Collections.sort(descSortedList,new BillDescComparator());

        adapter=new BillsAdapter(descSortedList);
        recyclerView.setAdapter(adapter);
    }

    private String formatCurrency(Double amount){
        NumberFormat formatter=NumberFormat.getCurrencyInstance(new Locale("en","IN"));
        String moneyString=formatter.format(amount);

        return moneyString.substring(1);
    }

    class FetchBills extends AsyncTask<Void,Void,List<Bill>>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            filterLayout.setVisibility(View.GONE);
            amountInfoLayout.setVisibility(View.GONE);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected List<Bill> doInBackground(Void... voids) {

            List<Bill> tempList=new ArrayList<>();
            List<String> idList=new ArrayList<>();

            double total = 0;

            MongoClient client=Admin.getMongoClient("mongodb-atlas");
            MongoDatabase db=client.getDatabase("GymDB");
            MongoCollection<Document> coll=db.getCollection("payments");

            Document adminDocument=db.getCollection("admin").find().first().get();

            String ids=adminDocument.get("customerIDs").toString();

            idList.addAll(Arrays.asList(ids.substring(1,ids.length()-1).split(", ")));
            idList.add("ALL");

            FindIterable<Document> allDocs=coll.find();

            MongoCursor<Document> cursor=allDocs.iterator().get();



            cursor.forEachRemaining((document -> {

                String id=document.getString("billInvoiceID");
                String amount=document.getString("billAmount");
                String billFor=document.getString("billFor");
                String title=document.getString("billTitle");
                String on=document.getString("createdOn");

                tempList.add(new Bill(id,amount,billFor,title,on));
            }));



            for(Bill b:tempList){
                total+=Double.parseDouble(b.getBillAmount());
            }


            final Double money=total;
            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                        R.layout.spinner_item, idList);

                adapter.setDropDownViewResource(R.layout.spinner_item);
                totalBillAmountText.setText("₹ "+formatCurrency(money));

                usersListSpinner.setAdapter(adapter);
                usersListSpinner.setSelection(idList.indexOf("ALL"));
            });
            return tempList;
        }

        @Override
        protected void onPostExecute(List<Bill> list) {
            super.onPostExecute(list);
            progressBar.setVisibility(View.INVISIBLE);
            loadingTextView.setVisibility(View.GONE);
            filterLayout.setVisibility(View.VISIBLE);
            amountInfoLayout.setVisibility(View.VISIBLE);

            billList.addAll(list);
            currentList.addAll(billList);
            adapter=new BillsAdapter(billList);
            recyclerView.setAdapter(adapter);
        }
    }
}