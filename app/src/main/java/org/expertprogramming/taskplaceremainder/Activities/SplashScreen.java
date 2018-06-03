package org.expertprogramming.taskplaceremainder.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.expertprogramming.taskplaceremainder.R;
import com.stephentuso.welcome.WelcomeHelper;

/**
 * Created by pramod on 24/1/18.
 */

public class SplashScreen extends AppCompatActivity {
    int totalcount;
    SharedPreferences prefs;
    protected void onCreate(final Bundle s){
        super.onCreate(s);
        setContentView(R.layout.activity_splashscreen);
        TextView t=findViewById(R.id.animtextView);
        prefs=getSharedPreferences("first_launch",0);
        Animation a= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.splashtextanim);
        t.startAnimation(a);
        final ProgressBar progressBar=findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.GONE);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#FF8C00"), PorterDuff.Mode.MULTIPLY);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (prefs.getBoolean("first_launch",true)) {
                    // The welcome screen appears
                    WelcomeHelper sampleWelcomeScreen = new WelcomeHelper(SplashScreen.this, StartPage.class);
                    sampleWelcomeScreen.forceShow();
                }
                else{
                    startActivity(new Intent(SplashScreen.this,LoginActivity.class));
                    finish();
                }
                //progressBar.setVisibility(View.GONE);
            }
        },700);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WelcomeHelper.DEFAULT_WELCOME_SCREEN_REQUEST) {
            if (resultCode == RESULT_OK) {
                // Code here will run if the welcome screen was completed
                prefs.edit().putBoolean("first_launch",false).commit();
                startActivity(new Intent(SplashScreen.this,LoginActivity.class));


            } else {
                // Code here will run if the welcome screen was canceled
                // In most cases you'll want to call finish() here
                finish();
            }

        }

    }

    protected void onStop(){
        super.onStop();
        finish();
    }

}
