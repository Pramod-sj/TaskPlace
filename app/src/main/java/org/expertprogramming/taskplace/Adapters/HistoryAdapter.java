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
 * Created by pramod on 24/3/18.
 */

public class HistoryAdapter extends BaseAdapter {
    private ArrayList<String> taskTitles;
    private ArrayList<String> placenames;
    private ArrayList<String> firebaseIds;
    private ArrayList<String> dates;
    private ArrayList<String> taskDescs;
    private Context context;
    public HistoryAdapter(Context context, ArrayList<String> taskTitles, ArrayList<String> taskDescs, ArrayList<String> dates, ArrayList<String> placenames, ArrayList<String> firebaseIds)
    {
        this.firebaseIds=firebaseIds;
        this.context=context;
        this.taskTitles=taskTitles;
        this.taskDescs=taskDescs;
        this.dates=dates;
        this.placenames=placenames;
    }
    @Override
    public int getCount() {
        return firebaseIds.size();
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
            convertView = inflater.inflate(R.layout.history_listitem, parent, false);
            viewHolder.tasktitle_textView = convertView.findViewById(R.id.hist_taskTitle);
            viewHolder.place_textView = convertView.findViewById(R.id.hist_placeData);
            viewHolder.firebaseIds_invisible=convertView.findViewById(R.id.hist_firebaseId);
            viewHolder.taskdesc_textView=convertView.findViewById(R.id.hist_taskDesc);
            viewHolder.date_textView=convertView.findViewById(R.id.hist_date);
            result=convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        viewHolder.tasktitle_textView.setText(taskTitles.get(position));
        viewHolder.taskdesc_textView.setText(taskDescs.get(position));
        viewHolder.date_textView.setText(dates.get(position));
        viewHolder.place_textView.setText(placenames.get(position));
        viewHolder.firebaseIds_invisible.setText(firebaseIds.get(position));

        return result;
    }

    class ViewHolder {
        public TextView tasktitle_textView;
        public TextView place_textView;
        public TextView firebaseIds_invisible;
        public TextView date_textView;
        public TextView taskdesc_textView;

    }
}
