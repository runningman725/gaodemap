package com.example.gaodemap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.core.PoiItem;
import com.example.utils.MapUtil;

import java.util.ArrayList;
import java.util.List;

public class GaoDeSearchResultAdapter extends RecyclerView.Adapter<GaoDeSearchResultAdapter.ViewHolder> {

    private Activity activity;
    private ArrayList<PoiItem> resultData = new ArrayList<>();
    private View inflator;
    private OnLocationClickListener clickListener;
    private int selectedPosition;

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
//        holder.iv_nav.setVisibility(position == selectedPosition ? View.VISIBLE : View.INVISIBLE);
//        holder.itemView.setBackgroundResource(position == selectedPosition ? R.color.red_99 : R.color.white);
//        holder.iv_nav.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openGaoDeMap(poiItem.getLatLonPoint().getLatitude(),poiItem.getLatLonPoint().getLongitude(),poiItem.getAdName());
//            }
//        });
    }
    //高德
    private void openGaoDeMap(double latitude, double longitude, String locationName) {
        if (MapUtil.isGdMapInstalled()) {
            MapUtil.openGaoDeNavi(activity, 0, 0, null, latitude, longitude, locationName);
        } else {
            //这里必须要写逻辑，不然如果手机没安装该应用，程序会闪退，这里可以实现下载安装该地图应用
            Toast.makeText(activity,"尚未安装高德地图",Toast.LENGTH_SHORT);
        }
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

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_title;
        TextView tv_address;
        ImageView iv_nav;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_address = itemView.findViewById(R.id.tv_address);
            iv_nav = itemView.findViewById(R.id.iv_nav);
        }
    }

    public void setOnItemClickListener(OnLocationClickListener listener) {
        this.clickListener = listener;
    }

    public interface OnLocationClickListener{
        void onItemClick(View view,int position);
    }
}
