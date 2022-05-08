package com.application.fitgym.misc;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.application.fitgym.R;
import com.application.fitgym.activities.admin.AdminActivity;
import com.application.fitgym.activities.customer.HomeActivity;
import com.application.fitgym.activities.customer.PeersActivity;
import com.application.fitgym.databinding.ActivityPeersBinding;
import com.application.fitgym.helpers.GymApplication;
import com.application.fitgym.helpers.UserInfo;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.bson.Document;

import io.realm.mongodb.App;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.auth.GoogleAuthType;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

public class LoginSignUpActivity extends AppCompatActivity implements View.OnClickListener{

    private Button login_button,google_signup_button;
    private TextView regnow_text_button;
    private EditText email_text,password_text;
    private View dialogView;
    private AlertDialog alert;

    TextView alert_dialog_text;
    public App app;
    private Credentials credentials;

    public GoogleSignInClient googleSignInClient;
    private GoogleSignInAccount account;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_sign_up);

        app= GymApplication.getGlobalAppInstance();

        LayoutInflater inflater=this.getLayoutInflater();
        dialogView=inflater.inflate(R.layout.loading_dialog,null);
        alert_dialog_text=dialogView.findViewById(R.id.dialog_textbox);
        alert=new AlertDialog.Builder(this)
                .setView(dialogView).create();

        alert.setCancelable(false);

        if(app.currentUser()!=null){
                alert.show();
                new DocumentFinder().execute(app.currentUser().getId());
        }

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(GymApplication.GOOGLE_CLIENT_ID)
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);


        login_button=findViewById(R.id.login_button);
        google_signup_button=findViewById(R.id.google_signup_button);
        regnow_text_button=findViewById(R.id.text_button);
        email_text=findViewById(R.id.email_text);
        password_text=findViewById(R.id.password_text);


        login_button.setOnClickListener(this);
        google_signup_button.setOnClickListener(this);
        regnow_text_button.setOnClickListener(this);
    }

    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try{
            if(requestCode == 100){
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                alert_dialog_text.setText("Google sign-in in progress...");
                alert.show();
                GoogleSignInAccount account = task.getResult(ApiException.class);
                handleSignInResult(account);
            }
        }
        catch(ApiException e) {
            Log.w("AUTH", "Failed to log in with Google OAuth: " + e.getMessage());
            alert.hide();
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    private void handleSignInResult(GoogleSignInAccount account){
        String token = account.getIdToken();
        Credentials googleCredentials =
                Credentials.google(token, GoogleAuthType.ID_TOKEN);
        app.loginAsync(googleCredentials, it -> {
            if (it.isSuccess()) {
                new DocumentFinder().execute(app.currentUser().getId());

            } else {
                Log.e("AUTH",
                        "Failed to log in to MongoDB Realm: ", it.getError());
                alert.hide();
                Toast.makeText(this,R.string.login_not_success,Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.login_button){

            String email=email_text.getText().toString();
            String password=password_text.getText().toString();

            if(email.trim().equalsIgnoreCase("")||password.trim().equalsIgnoreCase("")){
                Toast.makeText(this,R.string.input_not_valid,Toast.LENGTH_SHORT).show();
            }else{
                signInUsingEmailPassword(email,password);
            }


        }
        if(view.getId()==R.id.google_signup_button){
            signInUsingGoogle();
        }
        if(view.getId()==R.id.text_button){
            startActivity(new Intent(this, EmailPassUserRegistration.class));
        }
    }

    private void clearFields(){
        email_text.setText("");
        password_text.setText("");
    }

    private void signInUsingGoogle(){
        googleSignInClient.signOut();
        Intent signInIntent= googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,100);
    }


    private void signInUsingEmailPassword(String email,String password){

        credentials=Credentials.emailPassword(email,password);
        alert_dialog_text.setText("Signing you through email...");
        alert.show();


        app.loginAsync(credentials,res->{
            if(res.isSuccess()){
                new DocumentFinder().execute(app.currentUser().getId());

                clearFields();
            }
            else{
                alert.hide();
                Toast.makeText(this,R.string.login_not_success,Toast.LENGTH_LONG).show();
            }
        });
    }



    class DocumentFinder extends AsyncTask<String,Void,Integer>{

        @Override
        protected Integer doInBackground(String... str) {
            MongoClient client=app.currentUser().getMongoClient("mongodb-atlas");
            MongoDatabase db=client.getDatabase("GymDB");
            Document filter=new Document("authID",str[0]);
            MongoCollection<Document> collection=db.getCollection("customers");

            MongoCollection<Document> adminCollection=db.getCollection("admin");

            if(adminCollection.count(filter).get()>=1){
                return 2;
            }
            if(collection.count(filter).get()>=1){
                return 1;
            }

            return 0;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(app.currentUser().getId().equalsIgnoreCase("6245c493d56e7a43c2798da8")){
                alert_dialog_text.setText(R.string.admin_login);
            }
            else{
                alert_dialog_text.setText(R.string.fetching_records_txt);
            }
        }

        @Override
        protected void onPostExecute(Integer i) {
            super.onPostExecute(i);
            if(i==1){
                startActivity(new Intent(LoginSignUpActivity.this, HomeActivity.class));
                finish();
            }
            else if(i==2){
                startActivity(new Intent(LoginSignUpActivity.this, AdminActivity.class));
                finish();
            }
            else{
                if(app.currentUser().getProviderType()== Credentials.Provider.GOOGLE) {
                    UserInfo currentUser=getUserInfo();
                    startActivity(new Intent(LoginSignUpActivity.this, RegUserActivity.class).putExtra(RegUserActivity.USER_OBJECT, currentUser));
                    finish();
                }

                if(app.currentUser().getProviderType()== Credentials.Provider.EMAIL_PASSWORD){
                    startActivity(new Intent(LoginSignUpActivity.this,RegUserActivity.class));
                    finish();
                }
            }
        }
    }

    private UserInfo getUserInfo(){
        account=GoogleSignIn.getLastSignedInAccount(this);
        return UserInfo.storeAndGetUserInfoBundle(account.getDisplayName(),account.getPhotoUrl().toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        alert.dismiss();
    }
}