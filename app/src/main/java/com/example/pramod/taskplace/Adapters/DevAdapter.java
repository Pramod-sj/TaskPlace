package com.example.pramod.taskplace.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pramod.taskplace.R;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by pramod on 23/1/18.
 */

public class DevAdapter extends RecyclerView.Adapter<DevAdapter.ViewHolder> {
    String[] devnames;
    String[] devemails;
    String[] images;
    String[] devstatus;
    Context context;
    String[] links={"http://pramod-sj.github.io","http://www.facebook.com","http://www.facebook.com"};
    public DevAdapter(Context context, String[] devnames, String[] devemails, String[] images, String[] devstatus){
        this.context=context;
        this.devnames=devnames;
        this.devemails=devemails;
        this.images=images;
        this.devstatus=devstatus;
    }
    @Override
    public DevAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.developer_listitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DevAdapter.ViewHolder holder, int position) {
        holder.setLink(links[position]);
        holder.name.setText(devnames[position]);
        holder.email.setText(devemails[position]);
        holder.devStatus.setText(devstatus[position]);
        Picasso.with(context).load(images[position]).placeholder(R.drawable.placeholder).into(holder.dp);
        Log.i("data",devnames[position]);
    }

    @Override
    public int getItemCount() {
        return devemails.length;
    }
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView name;
        private CircleImageView dp;
        private TextView email;
        private TextView devStatus;
        private String link;
        public ViewHolder(View view) {
                super(view);
                view.setOnClickListener(this);
                email=view.findViewById(R.id.devEmail);
                name = view.findViewById(R.id.devName);
                devStatus=view.findViewById(R.id.devStatus);
                dp = view.findViewById(R.id.devImageProfile);
            }
        public void setLink(String link){
            this.link=link;
        }
        @Override
        public void onClick(View v) {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(link));
                context.startActivity(i);
            }catch(Exception e){
                Toast.makeText(context,"No Client", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
