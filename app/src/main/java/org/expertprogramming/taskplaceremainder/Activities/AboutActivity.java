package org.expertprogramming.taskplaceremainder.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import org.expertprogramming.taskplaceremainder.BuildConfig;
import org.expertprogramming.taskplaceremainder.R;

/**
 * Created by pramod on 24/1/18.
 */

public class AboutActivity extends AppCompatActivity {
    protected void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.activity_aboutapp);
        setTitle("About");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        TextView version=findViewById(R.id.version);
        version.setText("Version "+BuildConfig.VERSION_NAME);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onStop(){
        super.onStop();
    }
    protected void onDestroy(){
        finish();
        super.onDestroy();
    }
}
