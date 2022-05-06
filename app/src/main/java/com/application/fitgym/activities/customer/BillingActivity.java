package com.application.fitgym.activities.customer;

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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.application.fitgym.R;
import com.application.fitgym.activities.admin.ManageInventoryActivity;
import com.application.fitgym.adapters.BillsAdapter;
import com.application.fitgym.helpers.Bill;
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

public class BillingActivity extends AppCompatActivity {

    Toolbar toolbar;
    BillsAdapter adapter;
    RecyclerView recyclerView;
    List<Bill> mainList;

    List<Bill> currentList;
    final String[] MONTHS={"SELECT A MONTH..","JANUARY","FEBRUARY","MARCH","APRIL","MAY","JUNE","JULY","AUGUST","SEPTEMBER","OCTOBER","NOVEMBER","DECEMBER"};
    String memberID;
    User user;
    App app;
    ProgressBar loadingBar;
    Spinner monthSpinner;
    RadioGroup filtersRG;
    CheckBox addFilterChk;
    TextView loadingText,monthlyFilterTextView,totalAmountText,monthlyAmountText,noFilterText;
    LinearLayout filtersLayout,extraInfoLayout;
    LinearLayoutManager linearLayoutManager;
    boolean FILTER_APPLIED=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);
        toolbar=findViewById(R.id.billing_activity_toolbar);

        loadingBar=findViewById(R.id.customer_bill_loading_bar);
        loadingText=findViewById(R.id.customer_bill_loading_text);
        filtersLayout=findViewById(R.id.customer_bill_filter_layout);

        monthlyAmountText=findViewById(R.id.customer_bill_monthly_amount);
        monthlyFilterTextView=findViewById(R.id.customer_bill_extra_info_title);
        totalAmountText=findViewById(R.id.customer_bill_total_amount);
        monthSpinner=findViewById(R.id.month_spinner_select);
        addFilterChk=findViewById(R.id.customer_bill_add_filter_chk);
        filtersRG=findViewById(R.id.customer_bill_filter_rg);
        extraInfoLayout=findViewById(R.id.customer_bill_extra_info_layout);
        noFilterText=findViewById(R.id.customer_bill_no_filter_text);

        linearLayoutManager=new LinearLayoutManager(this);
        recyclerView=findViewById(R.id.billing_activity_recycler_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration=new DividerItemDecoration(this,linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        filtersRG.setAlpha(0f);
        monthSpinner.setVisibility(View.GONE);

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Bills & Records");
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
        app=GymApplication.getGlobalAppInstance();
        user=app.currentUser();

        mainList=new ArrayList<>();
        currentList=new ArrayList<>();

        memberID=getIntent().getStringExtra("ID");


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
        monthSpinner.setAdapter(monthsAdapter);
        monthSpinner.setSelection(0);


        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            SimpleDateFormat sdf=new SimpleDateFormat("EE MM yy HH:mm:ss");
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
               if(!monthList.get(i).contains("SELECT")){
                   String month = monthSpinner.getSelectedItem().toString();
                   double sum=0;
                   int idx = getMonthIndexFromArray(month);
                   List<Bill> toList = new ArrayList<>();
                   toList.addAll(mainList);

                   currentList.clear();
                   for (Bill c : toList) {

                       try {
                           Date d = sdf.parse(c.getCreatedOn());
                           if ((d.getMonth() + 1) == idx) {
                               sum+=Double.parseDouble(c.getBillAmount());
                               currentList.add(c);
                           }
                       } catch (Exception p) {
                           Toast.makeText(BillingActivity.this, "Error caused while sorting based on month!", Toast.LENGTH_SHORT).show();
                       }
                   }
                   adapter = new BillsAdapter(currentList);
                   recyclerView.setAdapter(adapter);

                   extraInfoLayout.setVisibility(View.VISIBLE);
                   monthlyFilterTextView.setText("Expense in "+month);
                   if(sum!=0){
                       monthlyAmountText.setText(String.format("₹ %s",formatCurrency(sum)));
                   }else{
                       monthlyAmountText.setText("No purchases yet!");
                   }
                   if (currentList.size() > 0) {
                       if (loadingText.getVisibility() == View.VISIBLE) {
                           loadingText.setVisibility(View.INVISIBLE);
                       }
                   } else {
                       if (!month.equals(monthsAdapter.getItem(0))) {
                           loadingText.setText("No bills found for the applied filter");
                           loadingText.setVisibility(View.VISIBLE);
                       }
                   }
               }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        addFilterChk.setOnCheckedChangeListener((compoundButton, b) -> {
            currentList.clear();
            currentList.addAll(mainList);
            adapter=new BillsAdapter(currentList);
            recyclerView.setAdapter(adapter);
            if(b){
                noFilterText.animate()
                        .alpha(0f)
                        .setDuration(100)
                        .setListener(null);
                filtersRG.animate()
                        .alpha(1f)
                        .setDuration(100)
                        .setListener(null);
                monthSpinner.setVisibility(View.VISIBLE);

            }else{
                currentList.addAll(mainList);
                adapter=new BillsAdapter(currentList);
                recyclerView.setAdapter(adapter);
                if(extraInfoLayout.getVisibility()==View.VISIBLE){
                    extraInfoLayout.setVisibility(View.GONE);
                }
                noFilterText.animate()
                        .alpha(1f)
                        .setDuration(100)
                        .setListener(null);
                filtersRG.animate()
                        .alpha(0f)
                        .setDuration(100)
                        .setListener(null);
                filtersRG.clearCheck();
                monthSpinner.setSelection(0);
                monthSpinner.setVisibility(View.GONE);
            }
        });
        filtersRG.setOnCheckedChangeListener((radioGroup, i) -> {
            int id=radioGroup.getCheckedRadioButtonId();
            switch (id){
                case R.id.old_first_radio:
                    filterAsc();
                    break;
                case R.id.new_first_radio:
                    filterDesc();
                    break;
            }
        });

        new FetchCustomerBill().execute();
    }

    private String formatCurrency(Double amount){
        NumberFormat formatter=NumberFormat.getCurrencyInstance(new Locale("en","IN"));
        String moneyString=formatter.format(amount);

        return moneyString.substring(2);
    }

    class BillAscComparator implements Comparator<Bill> {

        @Override
        public int compare(Bill b1, Bill b2) {

            SimpleDateFormat sdf=new SimpleDateFormat("EE MM yy HH:mm:ss");
            try{
                Date d1=sdf.parse(b1.getCreatedOn());
                Date d2=sdf.parse(b2.getCreatedOn());

                return d1.compareTo(d2);
            }catch(ParseException p){
                Toast.makeText(BillingActivity.this, "Error while sorting!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(BillingActivity.this, "Error while sorting!", Toast.LENGTH_SHORT).show();
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

    class FetchCustomerBill extends AsyncTask<Void,Void,List<Bill>>{

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            loadingBar.setVisibility(View.VISIBLE);
            loadingText.setVisibility(View.VISIBLE);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected List<Bill> doInBackground(Void... voids) {

            List<Bill> response=new ArrayList<>();
            MongoClient client=user.getMongoClient("mongodb-atlas");
            MongoDatabase db=client.getDatabase("GymDB");

            MongoCollection<Document> coll=db.getCollection("payments");

            Document filter=new Document("billFor",memberID);
            FindIterable<Document> allDocs=coll.find(filter);

            MongoCursor<Document> cursor=allDocs.iterator().get();
            double total=0;
            cursor.forEachRemaining((document -> {
                String id=document.getString("billInvoiceID");
                String amount=document.getString("billAmount");
                String billFor=document.getString("billFor");
                String title=document.getString("billTitle");
                String on=document.getString("createdOn");

                response.add(new Bill(id,amount,billFor,title,on));
            }));

            for(Bill b:response){
                total+=Double.parseDouble(b.getBillAmount());
            }

            double money = total;
            runOnUiThread(() -> {
                totalAmountText.setText("₹ "+formatCurrency(money));
            });
            return response;
        }

        @Override
        protected void onPostExecute(List<Bill> bills) {
            super.onPostExecute(bills);
            mainList.addAll(bills);
            currentList.addAll(mainList);

            adapter=new BillsAdapter(currentList);
            recyclerView.setAdapter(adapter);

            filtersLayout.setVisibility(View.VISIBLE);
            loadingBar.setVisibility(View.INVISIBLE);
            loadingText.setVisibility(View.INVISIBLE);
        }
    }
}