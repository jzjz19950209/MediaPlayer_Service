package com.example.qf.mediaplayer_service;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by qf on 2016/9/27.
 */
public class MyAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    private List<String> list;

    public MyAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.lv_item, parent, false);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.image_music);
            holder.musicName = (TextView) convertView.findViewById(R.id.textView);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        holder.musicName.setText(list.get(position));
        holder.image.setImageResource(R.drawable.a58);
        return convertView;
    }
    class ViewHolder{
        TextView musicName;
        ImageView image;
    }
}
