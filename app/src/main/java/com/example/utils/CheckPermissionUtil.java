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
    private static LocationManager lm;//위치 관리
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
        if (ok) {//got a location service
            if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e("qm","권한이 없음");
                // 권한 신청
                checkPermissions(needPermissions);

            } else {
                // 권한이 있어요
                locationPermission.permissionGranted();
            }
        } else {
            Log.e("qm","GPS 위치추적 서비스가 켜지지 않는 것을 감지했습니다");
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
