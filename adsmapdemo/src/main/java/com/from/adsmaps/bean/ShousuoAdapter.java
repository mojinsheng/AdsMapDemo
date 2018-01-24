package com.from.adsmaps.bean;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.from.adsmaps.R;

import java.util.List;

/**
 * Created by Administrator on 2018/1/23.
 */
public class ShousuoAdapter extends RecyclerView.Adapter<ShousuoAdapter.MyViewHolder> {
     final Context context;
    private List<PoiItem> poiItems;
    @Override
    public int getItemCount() {
        return poiItems.size();
    }
    public ShousuoAdapter(Context _context, List<PoiItem> _poiItems){
        context=_context;
        poiItems=_poiItems;
    }

    @Override
    public ShousuoAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(
                context).inflate(R.layout.items, parent,
                false));
        return holder;
    }

    @Override
    public void onBindViewHolder(ShousuoAdapter.MyViewHolder holder, int position) {
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PoiItem poiItem=poiItems.get(position);
        holder.tv.setText(poiItem.getSnippet());
        holder.textView.setText(poiItem.getProvinceName()+poiItem.getCityName()+poiItem.getAdName()+poiItem.getSnippet());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent();
                intent.putExtra("backEntity",poiItem);
                ((Activity)context).setResult(1000,intent);
                ((Activity)context).finish();

            }
        });

    }

    class MyViewHolder extends RecyclerView.ViewHolder
    {

        TextView tv,textView;

        public MyViewHolder(View view)
        {
            super(view);
            tv = (TextView) view.findViewById(R.id.id_title);
            textView= (TextView) view.findViewById(R.id.id_jiedao);
        }
    }
}
