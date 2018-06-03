package org.expertprogramming.taskplaceremainder.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.expertprogramming.taskplaceremainder.R;

import java.util.ArrayList;

/**
 * Created by pramod on 14/2/18.
 */

public class PlaceAdapter extends BaseAdapter{
    ArrayList<String> place;
    Context context;
    ArrayList<String> placeAddress;
    public PlaceAdapter(Context context,ArrayList<String> place,ArrayList<String> placeAddress) {
        this.place = place;
        this.placeAddress=placeAddress;
        this.context=context;
    }

    @Override
    public int getCount() {
        return place.size();
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
        PlaceAdapter.ViewHolder viewHolder;
        View result;
        if (convertView == null) {
            viewHolder = new PlaceAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.savedplace_listitem, parent, false);
            viewHolder.place_textView = convertView.findViewById(R.id.placeNameListItem);
            viewHolder.placeAddress_textView=convertView.findViewById(R.id.placeAddressListItem);
            result=convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (PlaceAdapter.ViewHolder) convertView.getTag();
            result=convertView;
        }
        viewHolder.place_textView.setText(place.get(position));
        viewHolder.placeAddress_textView.setText(placeAddress.get(position));
        return result;
    }
    class ViewHolder {
        TextView place_textView;
        TextView placeAddress_textView;
    }
}
