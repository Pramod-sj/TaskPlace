package org.expertprogramming.taskplace;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.expertprogramming.taskplace.Model.CurrentUserData;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import cat.ereza.customactivityoncrash.config.CaocConfig;
import es.dmoral.toasty.Toasty;

/**
 * Created by pramod on 28/1/18.
 */

public class CustomErrorActivity extends AppCompatActivity {
    String log;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customeerror);
        log=CustomActivityOnCrash.getStackTraceFromIntent(getIntent());
        Button restartButton = findViewById(R.id.restart_button);
        String manufacturer= Build.MANUFACTURER;
        String model=Build.MODEL;
        String brand= Build.BRAND;
        String version= BuildConfig.VERSION_NAME;
        String mobileData="Mobile Info:\nManufacturer: "+manufacturer+"\nModel: "+model+"\nBrand: "+brand+"\nApp Version: "+version+"\nDisplay: "+Build.DISPLAY;
        log=log+"\n"+mobileData;
        final CaocConfig config = CustomActivityOnCrash.getConfigFromIntent(getIntent());
        if (config == null) {
            finish();
            return;
        }

        if (config.isShowRestartButton() && config.getRestartActivityClass() != null) {
            restartButton.setText(R.string.restart_app);
            restartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CustomActivityOnCrash.restartApplication(CustomErrorActivity.this, config);
                }
            });
        } else {
            restartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CustomActivityOnCrash.closeApplication(CustomErrorActivity.this, config);
                }
            });
        }
        Button send=findViewById(R.id.Send_button);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMail();
            }
        });
    }
    private void sendMail(){

        Log.i("sendMail()","Seding error message to developer");
        String TO="pramodsinghjantwal@gmail.com";
        Intent i=new Intent(Intent.ACTION_SENDTO,Uri.parse("mailto:"+TO));
        i.putExtra(Intent.EXTRA_SUBJECT,"Logcat");
        i.putExtra(Intent.EXTRA_TEXT,log);
        i.putExtra(Intent.EXTRA_CC,new CurrentUserData(getApplicationContext()).getCurrentUserEmail());
        try {
            startActivity(Intent.createChooser(i,"Select client"));
        }catch(Exception e){
            Toasty.error(getApplicationContext(),"Cannot send the log no email client installed.",Toast.LENGTH_LONG).show();
        }
        //String[] FROM={}

    }
}
