package com.example.pramod.taskplace;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

/**
 * Created by pramod on 24/1/18.
 */

public class SplashScreen extends AppCompatActivity {
    protected void onCreate(Bundle s){
        super.onCreate(s);
        setContentView(R.layout.splashscreen);
        final ProgressBar progressBar=findViewById(R.id.progressBar2);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#FF8C00"), PorterDuff.Mode.MULTIPLY);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i=new Intent(SplashScreen.this,Login.class);
                startActivity(i);
                finish();
                progressBar.setVisibility(View.GONE);
            }
        },2000);
    }
}
