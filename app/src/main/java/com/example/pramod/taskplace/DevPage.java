package com.example.pramod.taskplace;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Created by pramod on 23/1/18.
 */

public class DevPage extends AppCompatActivity{
    RecyclerView recyclerView;
    final String[] devemails={"pramodsinghjantwal@gmail.com","ashish@gmail.com","abhishek@gmail.com"};
    final String devnames[]={"Pramod SJ","Ashish K","Abhishek S"};
    private final String devdp[] = {
            "http://api.learn2crack.com/android/images/donut.png",
            "http://api.learn2crack.com/android/images/eclair.png",
            "http://api.learn2crack.com/android/images/froyo.png",
    };

    @SuppressLint("ResourceType")
    protected void onCreate(Bundle s){
        super.onCreate(s);
        setContentView(R.layout.developer_page);
        setTitle("Developers");
        recyclerView=findViewById(R.id.recyclerView);
        //devemails=getResources().getStringArray(R.id.devEmail);
        //devnames=getResources().getStringArray(R.id.devName);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        DevCustomAdapter adapter=new DevCustomAdapter(this,devnames,devemails,devdp);
        recyclerView.setAdapter(adapter);
    }
}
