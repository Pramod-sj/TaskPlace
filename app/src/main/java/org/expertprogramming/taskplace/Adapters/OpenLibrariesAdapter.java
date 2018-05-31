package org.expertprogramming.taskplace.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.expertprogramming.taskplace.R;

/**
 * Created by pramod on 26/1/18.
 */

public class OpenLibrariesAdapter extends BaseAdapter {
    String[] libName;
    String[] libDesc;
    Context context;
    public OpenLibrariesAdapter(Context context,String[] libName,String[] linDesc){
        this.context=context;
        this.libName=libName;
        this.libDesc=linDesc;
    }
    @Override
    public int getCount() {
        return libName.length;
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
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.librarieslistitem, parent, false);
            viewHolder.libName_textView = convertView.findViewById(R.id.libName);
            viewHolder.libDesc_textView = convertView.findViewById(R.id.libDesc);
            view=convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            view=convertView;
        }
        viewHolder.libName_textView.setText(libName[position]);
        viewHolder.libDesc_textView.setText(libDesc[position]);

        return view;
    }
    class ViewHolder {
        TextView libName_textView;
        TextView libDesc_textView;

    }
}
