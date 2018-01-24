package com.example.pramod.taskplace;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ThrowOnExtraProperties;

import java.util.ArrayList;

/**
 * Created by pramod on 13/1/18.
 */

public class CustomDataAdapter extends BaseAdapter {
    private ArrayList<String> contents;
    private ArrayList<String> taskdates;
    private ArrayList<String> placenames;
    private Context context;
    public CustomDataAdapter(Context context,ArrayList<String> content,ArrayList<String> taskdates,ArrayList<String> placenames)
    {
        this.context=context;
        this.contents=content;
        this.taskdates=taskdates;
        this.placenames=placenames;
    }

    @Override
    public int getCount() {
        return contents.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        View result;
        if (convertView == null) {
            viewHolder = new ViewHolder();

            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.listitem, parent, false);
            viewHolder.content_textView = (TextView) convertView.findViewById(R.id.contentData);
            viewHolder.date_textView = (TextView) convertView.findViewById(R.id.timeData);
            viewHolder.place_textView = (TextView) convertView.findViewById(R.id.placeData);
            result=convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        viewHolder.content_textView.setText(contents.get(position));
        viewHolder.place_textView.setText(placenames.get(position));
        viewHolder.date_textView.setText(taskdates.get(position));
        return convertView;
    }
    class ViewHolder {
        TextView content_textView;
        TextView date_textView;
        TextView place_textView;
    }
}
