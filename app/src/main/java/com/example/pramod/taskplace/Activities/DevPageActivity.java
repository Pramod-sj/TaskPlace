package com.example.pramod.taskplace.Activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.pramod.taskplace.Adapters.DevAdapter;
import com.example.pramod.taskplace.R;

/**
 * Created by pramod on 23/1/18.
 */

public class DevPageActivity extends AppCompatActivity{
    RecyclerView recyclerView;
    final String[] devemails={"pramodsinghjantwal@gmail.com","ashishkokane1605@gmail.com","abhishek.as208@gmail.com"};
    final String devnames[]={"Pramod SJ","Ashish K","Abhishek S"};
    final String devStatus[]={"Lead Developer","Developer","Logo Designer"};
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
        //devemails=getResources().getStringArray(R.id.devEmail);
        //devnames=getResources().getStringArray(R.id.devName);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        DevAdapter adapter=new DevAdapter(this,devnames,devemails,devdp,devStatus);
        recyclerView.setAdapter(adapter);
    }
}
