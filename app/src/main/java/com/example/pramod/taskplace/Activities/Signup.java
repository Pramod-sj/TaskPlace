package com.example.pramod.taskplace.Activities;

/**
 * Created by pramod on 11/1/18.
 */
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pramod.taskplace.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class Signup extends AppCompatActivity {
    FirebaseAuth auth;
    EditText email,password;
    Button signup;
    TextView itoLogin;
    TextInputLayout emailWrap,passWrap;
    ProgressBar pg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);
        auth = FirebaseAuth.getInstance();
        itoLogin=findViewById(R.id.itologin);
        emailWrap=findViewById(R.id.emailWrapper);
        passWrap=findViewById(R.id.passwordWrapper);
        emailWrap.setHint("Email ID");
        passWrap.setHint("Password");
        email=findViewById(R.id.editText1);
        password=findViewById(R.id.editText2);
        signup=findViewById(R.id.button);
        pg=findViewById(R.id.progressBar);
        itoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Signup.this,Login.class);
                startActivity(i);
            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pg.setVisibility(View.VISIBLE);
                String email_id=email.getText().toString();
                String password_=password.getText().toString();
                if(TextUtils.isEmpty(email_id)){
                    Toast.makeText(getApplicationContext(),"Please enter your email id",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password_)){
                    Toast.makeText(getApplicationContext(),"Please enter password",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(password_.length()<6){
                    Toast.makeText(getApplicationContext(),"Password to short",Toast.LENGTH_SHORT).show();
                    return;

                }
                auth.createUserWithEmailAndPassword(email_id,password_).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(getApplicationContext(), "User with this email already exist.", Toast.LENGTH_SHORT).show();
                                pg.setVisibility(View.GONE);
                                return;
                            }
                        } else {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            user.sendEmailVerification();
                            pg.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(),"We have sent you an mail for verification",Toast.LENGTH_SHORT).show();

                        }
                    }
                });

            }
        });

    }

}

