package org.expertprogramming.taskplaceremainder.Activities;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import org.expertprogramming.taskplaceremainder.Helper.DialogHelper;
import org.expertprogramming.taskplaceremainder.Model.CurrentUserData;
import org.expertprogramming.taskplaceremainder.Database.FirebaseDatabaseHelper;
import org.expertprogramming.taskplaceremainder.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import es.dmoral.toasty.Toasty;

/**
 * Created by pramod on 11/1/18.
 */

public class LoginActivity extends AppCompatActivity{
    DialogHelper dialogHelper;
    FirebaseAuth lauth;
    EditText lemail,lpassword;
    Button llogin;
    TextInputLayout lemailWrap,lpassWrap;
    TextView itoReg;
    LinearLayout l;
    com.google.android.gms.common.SignInButton gsignInButton;
    GoogleSignInClient mGoogleSignInClient;
    Button forgetPassword;
    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialogHelper=new DialogHelper(LoginActivity.this);
        setContentView(R.layout.activity_login);
        //google acc config
        configSignin();
        //
        forgetPassword=findViewById(R.id.forget_password);
        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgetPassword();
            }
        });
        l=findViewById(R.id.layout);
        gsignInButton=findViewById(R.id.googlesignin);
        lauth = FirebaseAuth.getInstance();
        if(lauth.getCurrentUser()!=null){
            if(lauth.getCurrentUser().isEmailVerified()) {
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
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
        itoReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(LoginActivity.this,SignupActivity.class);
                startActivity(i);
            }
        });
        llogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email_id=lemail.getText().toString();
                String password_=lpassword.getText().toString();
                if(TextUtils.isEmpty(email_id)){
                     Toasty.warning(getApplicationContext(),"Please enter your email id",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password_)){
                    Toasty.warning(getApplicationContext(),"Please enter password",Toast.LENGTH_SHORT).show();
                    return;
                }
                dialogHelper.showProgressDialog("Logging in...");
                lauth.signInWithEmailAndPassword(email_id,password_).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            dialogHelper.dismissProgressDialog();
                            if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                forgetPassword.setVisibility(View.VISIBLE);
                            }
                            Toasty.error(getApplicationContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }

                        if(task.isSuccessful()){
                            FirebaseUser user1=FirebaseAuth.getInstance().getCurrentUser();
                            if(user1.isEmailVerified()) {
                                Toasty.success(getApplicationContext(),"Successfully logged in :)").show();
                                String UID=FirebaseAuth.getInstance().getCurrentUser().getUid();
                                CurrentUserData currentUserData=new CurrentUserData(LoginActivity.this);
                                currentUserData.setCurrentUID(UID);
                                currentUserData.setCurrentUserEmail(email_id);
                                SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                prefs.edit().putBoolean("firstlogin",true).commit();
                                new getDataFromFirebase().execute();
                            }
                            else{
                                Toasty.warning(getApplicationContext(),"Please verify yourself... :|",Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                });

            }
        });
        gsignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogHelper.showProgressDialog("please wait...");
                signInIntent();

            }
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }




    public class getDataFromFirebase extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i("Background","preExecute");
            dialogHelper.updateDialogMessage("we are setting up....");
        }
        @Override
        protected Void doInBackground(Void... voids) {
            //super.doInBackground(Void... voids);
            FirebaseDatabaseHelper db=new FirebaseDatabaseHelper(LoginActivity.this);
            db.insertDataToOffline();
            Log.i("inBackground","foInBackground");
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Log.i("Background","onPostExecute");
            //lpg.setVisibility(View.GONE);
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            dialogHelper.dismissProgressDialog();
            startActivity(i);
            finish();
        }
    }
    public void configSignin(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


    }
    public void signInIntent(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 1001);
    }
    public void firebaseAuthWithGoogle(final GoogleSignInAccount account){
        dialogHelper.showProgressDialog("please wait signing you in..");
        final AuthCredential credential=GoogleAuthProvider.getCredential(account.getIdToken(),null);
        lauth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            String UID=FirebaseAuth.getInstance().getCurrentUser().getUid();
                            CurrentUserData currentUserData=new CurrentUserData(LoginActivity.this);
                            currentUserData.setCurrentUID(UID);
                            currentUserData.setCurrentUserEmail(account.getEmail());
                            new getDataFromFirebase().execute();
                        }
                        else if(!task.isSuccessful()){
                            Toasty.error(getApplicationContext(),task.getException().getMessage().toString(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 1001) {
            dialogHelper.dismissProgressDialog();
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.i("Error Code", String.valueOf(e.getStatusCode()));

                Log.i("Error Code", String.valueOf(e.getStatusMessage()));
                Toasty.warning(getApplicationContext(),"Please update your google service", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onStop(){
        super.onStop();
        finish();
    }
    private void forgetPassword(){
        View view=getLayoutInflater().inflate(R.layout.reset_password_dialog,null);
        final EditText email=view.findViewById(R.id.password_reset_email_id);
        AlertDialog.Builder builder=new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String email_id=email.getText().toString();
                        if(email_id!=null) {
                            lauth.sendPasswordResetEmail(email_id).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toasty.success(LoginActivity.this,"Successfully sent you a mail.",Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toasty.success(LoginActivity.this,task.getException().getMessage().toString(),Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        else{
                            Toasty.warning(LoginActivity.this,"Please enter your email id",Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false);
        builder.setView(view);
        builder.show();
    }
}
