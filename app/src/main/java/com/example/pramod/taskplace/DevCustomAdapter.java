package com.example.pramod.taskplace;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by pramod on 23/1/18.
 */

public class DevCustomAdapter extends RecyclerView.Adapter<DevCustomAdapter.ViewHolder> {
    String[] devnames;
    String[] devemails;
    String[] images;
    Context context;

    public DevCustomAdapter(Context context,String[] devnames,String[] devemails,String[] images){
        this.context=context;
        this.devnames=devnames;
        this.devemails=devemails;
        this.images=images;
    }
    @Override
    public DevCustomAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.developer_listitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DevCustomAdapter.ViewHolder holder, int position) {
        holder.name.setText(devnames[position]);
        holder.email.setText(devemails[position]);
        Picasso.with(context).load(images[position]).resize(240, 120).into(holder.dp);
        Log.i("data",devnames[position]);
    }

    @Override
    public int getItemCount() {
        return devemails.length;
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView name;
        private CircleImageView dp;
        private TextView email;
        public ViewHolder(View view) {
                super(view);
                email=view.findViewById(R.id.devEmail);
                name = (TextView)view.findViewById(R.id.devName);
                dp = (CircleImageView) view.findViewById(R.id.devImageProfile);
            }

    }
}
