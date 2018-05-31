package org.expertprogramming.taskplace.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.expertprogramming.taskplace.R;

import java.util.ArrayList;

/**
 * Created by pramod on 13/1/18.
 */

public class TaskViewAdapter extends BaseAdapter {
    private ArrayList<String> contents;
    private ArrayList<String> placenames;
    private ArrayList<String> distance;
    private ArrayList<String> firebaseIds;
    private Context context;
    public TaskViewAdapter(Context context, ArrayList<String> content,  ArrayList<String> placenames, ArrayList<String> distance,ArrayList<String> firebaseIds)
    {
        this.firebaseIds=firebaseIds;
        this.context=context;
        this.contents=content;
        this.placenames=placenames;
        this.distance=distance;
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
            viewHolder.content_textView = convertView.findViewById(R.id.contentData);
            viewHolder.place_textView = convertView.findViewById(R.id.placeData);
            viewHolder.distance_textView=convertView.findViewById(R.id.distance);
            viewHolder.firebaseIds_invisible=convertView.findViewById(R.id.firebaseId);
            result=convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        viewHolder.content_textView.setText(contents.get(position));
        viewHolder.place_textView.setText(placenames.get(position));
        viewHolder.firebaseIds_invisible.setText(firebaseIds.get(position));
        if(distance.size()!=0 ){
            viewHolder.distance_textView.setText(String.valueOf(distance.get(position)));
        }else{
            viewHolder.distance_textView.setText("null");
        }
        return result;
    }
    class ViewHolder {
        TextView content_textView;
        TextView place_textView;
        TextView distance_textView;
        TextView firebaseIds_invisible;
    }
}
