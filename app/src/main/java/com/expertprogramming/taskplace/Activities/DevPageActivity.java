package com.expertprogramming.taskplace.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.expertprogramming.taskplace.Adapters.DevAdapter;
import com.expertprogramming.taskplace.R;

/**
 * Created by pramod on 23/1/18.
 */

public class DevPageActivity extends AppCompatActivity{
    RecyclerView recyclerView;
    final String[] devemails={"pramodsinghjantwal@gmail.com","ashishkokane1605@gmail.com","abhishek.as208@gmail.com"};
    final String devnames[]={"Pramod SJ","Ashish K","Abhishek S"};
    final String devStatus[]={"Lead Developer | UI UX Designer","Developer","Logo Designer"};
    private final String devdp[] = {
            "https://github.com/Pramod-sj/TaskPlace/raw/master/DevDP/pramod.jpg",
            "https://github.com/Pramod-sj/TaskPlace/raw/master/DevDP/ashish.jpg",
            "https://github.com/Pramod-sj/TaskPlace/raw/master/DevDP/abhishek.jpg",
    };

    @SuppressLint("ResourceType")
    protected void onCreate(Bundle s){
        super.onCreate(s);
        setContentView(R.layout.activity_developer_page);
        setTitle("Developers");
        recyclerView=findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        DevAdapter adapter=new DevAdapter(this,devnames,devemails,devdp,devStatus);
        recyclerView.setAdapter(adapter);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
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
