package com.example.gaodemap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.core.PoiItem;
import com.bumptech.glide.Glide;
import com.example.utils.CustomDialog;
import com.example.utils.MapUtil;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GaoDeSearchResultAdapter extends RecyclerView.Adapter<GaoDeSearchResultAdapter.ViewHolder> {

    private Activity activity;
    private ArrayList<PoiItem> resultData = new ArrayList<>();
    private View inflator;
    private OnLocationClickListener clickListener;
    private int selectedPosition;
    private double myLongitude;
    private double myLatitude;
    private Handler handler;

    public GaoDeSearchResultAdapter(MainActivity mainActivity) {
        activity = mainActivity;
    }

    @NonNull
    @Override
    public GaoDeSearchResultAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        inflator = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location_list,null,false);
        return new ViewHolder(inflator);
    }

    @Override
    public void onBindViewHolder(@NonNull GaoDeSearchResultAdapter.ViewHolder holder, int position) {
        PoiItem poiItem = resultData.get(position);
        holder.tv_title.setText(poiItem.getTitle());
        holder.tv_address.setText(poiItem.getCityName()+poiItem.getAdName()+poiItem.getSnippet());
        Log.e("qm", "onBindViewHolder: ==="+poiItem.toString());
        holder.tv_address.setVisibility((position == 0 && poiItem.getPoiId().equals("regeo")) ? View.GONE : View.VISIBLE);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();
                clickListener.onItemClick(v,pos);
            }
        });
        holder.iv_nav.setVisibility(position == selectedPosition ? View.VISIBLE : View.INVISIBLE);
        Log.e("TAG", "onBindViewHolder: lat=="+myLatitude+"===lng==="+myLongitude+"==getLat=="+poiItem.getLatLonPoint().getLatitude()+"===getLng=="+poiItem.getLatLonPoint().getLongitude());
        holder.tv_distance.setText(String.valueOf(AmapDistanceUtils.getDistance(myLongitude,myLatitude,poiItem.getLatLonPoint().getLongitude(),poiItem.getLatLonPoint().getLatitude())));
        if (poiItem.getPhotos() != null && poiItem.getPhotos().size() > 0) {
            Log.e("TAG", "onBindViewHolder: photo==="+poiItem.getPhotos().get(0).getUrl());
            Glide.with(activity).load(poiItem.getPhotos().get(0).getUrl()).into(holder.iv_shop);
        }

        holder.itemView.setBackgroundResource(position == selectedPosition ? R.color.red_99 : R.color.white);
        holder.iv_nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDialog.showBottomDialog(activity);
                CustomDialog.setNavMapChooseListener(new CustomDialog.OnNavMapChooseListener() {
                    @Override
                    public void chooseMap(String map) {
                        MapUtil.openMap(activity,map,poiItem);
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        if (null != resultData && resultData.size() > 0) {
            return resultData.size();
        } else {
            return 0;
        }
    }

    public void setData(List<PoiItem> resultData) {
        if (null != resultData) {
            this.resultData.clear();
            this.resultData.addAll(resultData);
        }
        notifyDataSetChanged();

    }

    public Object getItem(int position) {
        return resultData.get(position);
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setLatLng(double longitude, double latitude) {
        this.myLatitude = latitude;
        this.myLongitude = longitude;
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_title;
        TextView tv_address;
        ImageView iv_nav;
        TextView tv_distance;
        ImageView iv_shop;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_address = itemView.findViewById(R.id.tv_address);
            iv_nav = itemView.findViewById(R.id.iv_nav);
            tv_distance = itemView.findViewById(R.id.tv_distance);
            iv_shop = itemView.findViewById(R.id.iv_shop);
        }
    }

    public void setOnItemClickListener(OnLocationClickListener listener) {
        this.clickListener = listener;
    }

    public interface OnLocationClickListener{
        void onItemClick(View view,int position);
    }


}
