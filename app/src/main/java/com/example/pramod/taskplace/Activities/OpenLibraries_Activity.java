package com.example.pramod.taskplace.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.pramod.taskplace.Adapters.OpenLibrariesAdapter;
import com.example.pramod.taskplace.R;

/**
 * Created by pramod on 26/1/18.
 */

public class OpenLibraries_Activity extends AppCompatActivity {
    ListView listView;
    String[] LibDesc={
            "A fast circular ImageView perfect for profile images. This is based on RoundedImageView from Vince Mi which itself is based on techniques recommended by Romain Guy.",
            "A powerful image downloading and caching library for Android.",
            "This library allows launching a custom activity when the app crashes, instead of showing the hated Unfortunately, X has stopped dialog.",
            "An easy to use and customizable welcome screen for Android apps.",
            "An Customized Toast"
    };
    String[] LibName={
                    "CircleImageView",
                    "Picasso",
                    "CustomActivityOnCrash",
                    "Welcome",
                    "Toasty"
                        };
    protected void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.activity_openlibraries);
        listView=findViewById(R.id.openLibrariesList);
        setTitle("Open Source Libraries");
        OpenLibrariesAdapter adapter=new OpenLibrariesAdapter(OpenLibraries_Activity.this,LibName,LibDesc);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0:
                        Uri u=Uri.parse("https://github.com/Clans/FloatingActionButton");
                        Intent i=new Intent(Intent.ACTION_VIEW,u);
                        startActivity(i);
                        break;
                    case 1:
                        Uri u1=Uri.parse("https://github.com/hdodenhof/CircleImageView");
                        Intent i1=new Intent(Intent.ACTION_VIEW,u1);
                        startActivity(i1);
                        break;
                    case 2:
                        Uri u2=Uri.parse("https://github.com/square/picasso");
                        Intent i2=new Intent(Intent.ACTION_VIEW,u2);
                        startActivity(i2);
                }
            }
        });
        ActionBar actionBar=getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    protected void onStop(){
        super.onStop();
    }

    protected void onDestroy(){
        finish();
        super.onDestroy();
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

}
