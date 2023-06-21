package com.example.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.gaodemap.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class CheckPermissionUtil {

    private static final int LOCATION_CODE = 1;
    private static LocationManager lm;//【位置管理】
    private static Activity activity;

    private static MainActivity.OnCheckLocationPermission locationPermission;

    private static String[] needPermissions = {

            android.Manifest.permission.ACCESS_COARSE_LOCATION,

            android.Manifest.permission.ACCESS_FINE_LOCATION

    };

    public static void checkPermission(MainActivity mainActivity){
        activity = mainActivity;
        lm = (LocationManager) activity.getSystemService(activity.LOCATION_SERVICE);
        boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (ok) {//开了定位服务
            if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e("BRG","没有权限");
                // 没有权限，申请权限。
                // 申请授权。
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_CODE);
                checkPermissions(needPermissions);
                Toast.makeText(activity, "没有权限", Toast.LENGTH_SHORT).show();

            } else {
                // 有权限了，去放肆吧。
                Toast.makeText(activity, "有权限", Toast.LENGTH_SHORT).show();

                locationPermission.permissionGranted();
//                initMap();
            }
        } else {
            Log.e("BRG","系统检测到未开启GPS定位服务");
            Toast.makeText(activity, "系统检测到未开启GPS定位服务", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            activity.startActivityForResult(intent, 1315);
        }
    }

    private static void checkPermissions(String... permissions) {

        List needRequestPermissonList = findDeniedPermissions(permissions);

        if (null != needRequestPermissonList

                && needRequestPermissonList.size() > 0) {

            ActivityCompat.requestPermissions(activity, (String[]) needRequestPermissonList.toArray(new String[needRequestPermissonList.size()]),

                    LOCATION_CODE);

        }

    }

    private static List findDeniedPermissions(String[] permissions) {

        List needRequestPermissonList = new ArrayList();

        for (String perm : permissions) {

            if (ContextCompat.checkSelfPermission(activity,

                    perm) != PackageManager.PERMISSION_GRANTED

                    || ActivityCompat.shouldShowRequestPermissionRationale(

                    activity, perm)) {

                needRequestPermissonList.add(perm);

            }

        }

        return needRequestPermissonList;

    }

    public static void setCheckPermission(MainActivity.OnCheckLocationPermission onCheckLocationPermission){
        locationPermission = onCheckLocationPermission;
    }

}
