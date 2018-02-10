package com.example.pramod.taskplace.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pramod.taskplace.CurrentUserData;
import com.example.pramod.taskplace.Database.FirebaseDatabaseHelper;
import com.example.pramod.taskplace.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.dmoral.toasty.Toasty;

/**
 * Created by pramod on 11/1/18.
 */

public class Login extends AppCompatActivity{
    ProgressDialog progressDialog;
    FirebaseAuth lauth;
    EditText lemail,lpassword;
    Button llogin;
    TextInputLayout lemailWrap,lpassWrap;
    ProgressBar lpg;
    TextView itoReg;
    LinearLayout l;
    FirebaseUser user;
    //
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog=new ProgressDialog(Login.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Logging in.....");
        setContentView(R.layout.activity_login);
        l=findViewById(R.id.layout);
        lauth = FirebaseAuth.getInstance();
        if(lauth.getCurrentUser()!=null){
            if(lauth.getCurrentUser().isEmailVerified()) {
                Intent i = new Intent(Login.this, MainActivity.class);
                // Closing all the Activities
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                // Add new Flag to start new Activity
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(i);
                finish();
            }
        }
        itoReg=findViewById(R.id.itoreg);
        lemailWrap=findViewById(R.id.e_emailWrapper);
        lpassWrap=findViewById(R.id.e_passwordWrapper);
        lemailWrap.setHint("Email ID");
        lpassWrap.setHint("Password");
        lemail=findViewById(R.id.e_editText1);
        lpassword=findViewById(R.id.e_editText2);
        llogin=findViewById(R.id.login);
        //lpg=findViewById(R.id.lprogressBar);
        itoReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Login.this,Signup.class);
                startActivity(i);
            }
        });
        llogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //lpg.setVisibility(View.VISIBLE);
                final String email_id=lemail.getText().toString();
                String password_=lpassword.getText().toString();
                if(TextUtils.isEmpty(email_id)){
                    //lpg.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(),"Please enter your email id",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password_)){
                    //lpg.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(),"Please enter password",Toast.LENGTH_SHORT).show();
                    return;
                }
                progressDialog.show();
                lauth.signInWithEmailAndPassword(email_id,password_).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            //lpg.setVisibility(View.GONE);
                            progressDialog.cancel();
                            Toast.makeText(getApplicationContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }

                        if(task.isSuccessful()){
                            FirebaseUser user1=FirebaseAuth.getInstance().getCurrentUser();
                            if(user1.isEmailVerified()) {
                                Toasty.success(getApplicationContext(),"Successfully logged in :)").show();
                                String UID=FirebaseAuth.getInstance().getCurrentUser().getUid();
                                CurrentUserData currentUserData=new CurrentUserData(Login.this);
                                currentUserData.setCurrentUID(UID);
                                currentUserData.setCurrentUserEmail(email_id);
                                new getDataFromFirebase().execute().getStatus();
                            }
                            else{
                                Toasty.warning(getApplicationContext(),"Please verify yourself... :|",Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                });

            }
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
        System.exit(0);
    }




    public class getDataFromFirebase extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i("Background","preExecute");

        }
        @Override
        protected Void doInBackground(Void... voids) {
            //super.doInBackground(Void... voids);
            //progressDialog.setMessage("we are setting up....");
            FirebaseDatabaseHelper db=new FirebaseDatabaseHelper(Login.this);
            db.insertDataToOffline();
            Log.i("inBackground","foInBackground");
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Log.i("Background","onPostExecute");
            //lpg.setVisibility(View.GONE);
            Intent i = new Intent(Login.this, MainActivity.class);
            progressDialog.cancel();
            startActivity(i);
            finish();
        }
    }


}
