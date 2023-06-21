package com.example.gaodemap;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.PoiItemV2;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiResultV2;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearchV2;
import com.example.utils.CheckPermissionUtil;
import com.example.utils.GenerateSHA1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends Activity implements LocationSource, AMapLocationListener, PoiSearch.OnPoiSearchListener, GeocodeSearch.OnGeocodeSearchListener {

    private static final String TAG="qm";
    private MapView mMapView;
    private AMap aMap;
    LocationSource.OnLocationChangedListener mListener;
    AMapLocationClient mlocationClient;
    AMapLocationClientOption mLocationOption;
    MyLocationStyle myLocationStyle;
    private static final int LOCATION_CODE = 1;

    private LocationBean locationBean = new LocationBean();
    private PoiSearch.Query query;
    private AutoCompleteTextView et_keyword;
    private int currentPage=1;
    private PoiSearch poiSearch;
    private boolean isfirstinput=true;
    private List<Tip> autoTips;
    private GeocodeSearch geocoderSearch;
    private PoiItem firstItem;
    private ArrayList<PoiItem> poiItems;
    private List<PoiItem> resultData;
    private LatLonPoint searchLatlonPoint;
    private GaoDeSearchResultAdapter searchResultAdapter;
    private String inputSearchKey;
    private boolean isInputKeySearch;
    private boolean isItemClickAction;
    private RecyclerView rlv_location;
    private Marker locationMarker;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 权限被用户同意。
                    Toast.makeText(this, "定位权限已开启！", Toast.LENGTH_LONG).show();

                    initMap();

                } else {
                    // 权限被用户拒绝了。
                    Toast.makeText(this, "定位权限被禁止，相关地图功能无法使用！", Toast.LENGTH_LONG).show();
                }

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AMapLocationClient.updatePrivacyAgree(this.getApplicationContext(), true);
        AMapLocationClient.updatePrivacyShow(this.getApplicationContext(), true, true);
        GenerateSHA1.getSHA1(this);
        setContentView(R.layout.activity_main);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        initView();
        resultData = new ArrayList<>();
    }

    private void initView() {

        et_keyword = findViewById(R.id.et_keyword);
        CheckPermissionUtil.setCheckPermission(new OnCheckLocationPermission() {
            @Override
            public void permissionGranted() {
                initMap();
            }
        });
        CheckPermissionUtil.checkPermission(this);

        et_keyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.equals("")){
                    Log.e(TAG, "onTextChanged: "+s.toString());
//                    setPoiSearch(s.toString()); //입력한 내용으로 주변 주소 여러개 획득

                    String content = s.toString().trim();
                    //第二个参数传入null或者“”代表在全国进行检索，否则按照传入的city进行检索
                    InputtipsQuery inputquery = new InputtipsQuery(content, locationBean.getCity());
                    inputquery.setCityLimit(true);//限制在当前城市
                    Inputtips inputTips = new Inputtips(MainActivity.this, inputquery);
                    inputTips.setInputtipsListener(new OnInputTipsListener());
                    inputTips.requestInputtipsAsyn();


                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        et_keyword.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, "onItemClick: position"+position);
                if (autoTips != null && autoTips.size() > position) {
                    Tip tip = autoTips.get(position);
                    searchPoi(tip);
                }
            }
        });
        try {
            geocoderSearch = new GeocodeSearch(this);
            geocoderSearch.setOnGeocodeSearchListener(this);
        } catch (AMapException e) {
            throw new RuntimeException(e);
        }
        hideSoftKey(et_keyword);
        rlv_location = findViewById(R.id.rlv_location);
        searchResultAdapter = new GaoDeSearchResultAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rlv_location.setLayoutManager(layoutManager);
        rlv_location.setAdapter(searchResultAdapter);
    }

    private void searchPoi(Tip result) {
        try {
            isInputKeySearch = true;
            inputSearchKey = result.getName();//getAddress(); // + result.getRegeocodeAddress().getCity() + result.getRegeocodeAddress().getDistrict() + result.getRegeocodeAddress().getTownship();
            searchLatlonPoint = result.getPoint();
            firstItem = new PoiItem("tip", searchLatlonPoint, inputSearchKey, result.getAddress());
            firstItem.setCityName(result.getDistrict());
            firstItem.setAdName("");
            resultData.clear();
//            searchResultAdapter.setSelectedPosition(0);
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(result.getPoint().getLatitude(), result.getPoint().getLongitude()), 16f));
            hideSoftKey(et_keyword);
            doSearchQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void hideSoftKey(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void initMap() {

        //初始化地图控制器对象
        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        //设置地图的放缩级别
        aMap.moveCamera(CameraUpdateFactory.zoomTo(16));
        // 设置定位监听
        aMap.setLocationSource(this);
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationEnabled(true);
        // 设置定位的类型为定位模式，有定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);

        //蓝点初始化
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        //aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）默认执行此种模式。

        myLocationStyle.showMyLocation(true);
        aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                if (!isItemClickAction && !isInputKeySearch) {
                    geoAddress();
//                    startJumpAnimation();
                }
                searchLatlonPoint = new LatLonPoint(cameraPosition.target.latitude, cameraPosition.target.longitude);
                isInputKeySearch = false;
                isItemClickAction = false;
            }
        });
        aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
                addMarkerInScreenCenter(null);
            }
        });
//        aMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
//            @Override
//            public void onMyLocationChange(Location location) {
//                //从location对象中获取经纬度信息，地址描述信息，建议拿到位置之后调用逆地理编码接口获取
//                Log.e(TAG, "onMyLocationChange: ==="+location);
//            }
//        });
    }

    private void addMarkerInScreenCenter(LatLng locationLatLng) {
        LatLng latLng = aMap.getCameraPosition().target;
        Point screenPosition = aMap.getProjection().toScreenLocation(latLng);
        locationMarker = aMap.addMarker(new MarkerOptions()
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marker)));
        // 设置Marker在屏幕上,不跟随地图移动
        locationMarker.setPositionByPixels(screenPosition.x, screenPosition.y);
    }

    /**
     * 响应逆地理编码
     */
    public void geoAddress() {
//        et_keyword.setText("");
        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(searchLatlonPoint, 200, GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            //初始化定位
            try {
                mlocationClient = new AMapLocationClient(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            //初始化定位参数
            mLocationOption = new AMapLocationClientOption();
            //设置定位回调监听
            mlocationClient.setLocationListener(this);
            mLocationOption.setOnceLocation(true);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //关闭缓存机制
            mLocationOption.setLocationCacheEnable(false);
            //设置是否返回地址信息（默认返回地址信息）
            mLocationOption.setNeedAddress(true);
            //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
//            mLocationOption.setInterval(3000);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();//启动定位
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null&&amapLocation != null) {
            if (amapLocation != null
                    &&amapLocation.getErrorCode() == 0) {
                    mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
                    Log.e(TAG, "onLocationChanged: ===="+amapLocation);
                    LatLng curLatlng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
    //                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLatlng, 17f));
                    searchLatlonPoint = new LatLonPoint(curLatlng.latitude, curLatlng.longitude);
                    isInputKeySearch = false;
//                et_keyword.setText("");

                LocationBean bean = new LocationBean();
                bean.setLatitude(String.valueOf(amapLocation.getLatitude()));
                bean.setLongitude(String.valueOf(amapLocation.getLongitude()));
                bean.setProvince(String.valueOf(amapLocation.getProvince()));
                bean.setCoordType(String.valueOf(amapLocation.getCoordType()));
                bean.setCity(String.valueOf(amapLocation.getCity()));
                bean.setDistrict(String.valueOf(amapLocation.getDistrict()));
                bean.setCityCode(String.valueOf(amapLocation.getCityCode()));
//                bean.setAdCode(String.valueOf(amapLocation.getAdCode()));
                bean.setAddress(String.valueOf(amapLocation.getAddress()));
                bean.setCountry(String.valueOf(amapLocation.getCountry()));
                bean.setRoad(String.valueOf(amapLocation.getRoad()));
//                bean.setPoiName(String.valueOf(amapLocation.getPoiName()));
                bean.setStreet(String.valueOf(amapLocation.getStreet()));
                bean.setStreetNum(String.valueOf(amapLocation.getStreetNum()));
                bean.setAoiName(String.valueOf(amapLocation.getAoiName()));
                bean.setFloor(String.valueOf(amapLocation.getFloor()));
                bean.setErrorCode(String.valueOf(amapLocation.getErrorCode()));
                bean.setErrorInfo(String.valueOf(amapLocation.getErrorInfo()));
//                bean.setLocationDetail(String.valueOf(amapLocation.getLocationDetail()));
                bean.setDescription(String.valueOf(amapLocation.getDescription()));
//                bean.setLocationType(String.valueOf(amapLocation.getLocationType()));
//                bean.setConScenario(String.valueOf(amapLocation.getConScenario()));

                locationBean = bean;
                Log.e(TAG, "jsonData: ==="+bean);
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode()+ ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr===",errText);
            }
        }
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int resultCode) {
        if (resultCode == AMapException.CODE_AMAP_SUCCESS) {
            Log.e(TAG, "onPoiSearched:===PoiItem list=="+poiResult.getPois()+"===suggestKey=="+poiResult.getSearchSuggestionKeywords()+"==suggestCity=="+poiResult.getSearchSuggestionCitys());
            if (poiResult != null && poiResult.getQuery() != null) {
                if (poiResult.getQuery().equals(query)) {
                    poiItems = poiResult.getPois();
                    if (poiItems != null && poiItems.size() > 0) {
                        updateListview(poiItems);
                    }
                }
            }
        }
    }

    private void updateListview(ArrayList<PoiItem> poiItems) {
        resultData.clear();
//        searchResultAdapter.setSelectedPosition(0);
        resultData.add(firstItem);
        resultData.addAll(poiItems);
        Log.e(TAG, "updateListview: resultData=="+resultData);
        searchResultAdapter.setData(resultData);
        searchResultAdapter.notifyDataSetChanged();
        searchResultAdapter.setOnItemClickListener(new GaoDeSearchResultAdapter.OnLocationClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position != searchResultAdapter.getSelectedPosition()) {
                    PoiItem poiItem = (PoiItem) searchResultAdapter.getItem(position);
                    LatLng curLatlng = new LatLng(poiItem.getLatLonPoint().getLatitude(), poiItem.getLatLonPoint().getLongitude());
                    // 滞空
//                    saveClickLocationAddress = "";
//                    saveClickLocationAddress = poiItem.getCityName() + poiItem.getAdName() + poiItem.getSnippet();
                    isItemClickAction = true;
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLatlng, 16f));
                    searchResultAdapter.setSelectedPosition(position);
                    searchResultAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
//        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
//            if (result != null && result.getRegeocodeAddress() != null
//                    && result.getRegeocodeAddress().getFormatAddress() != null) {
//                String address = result.getRegeocodeAddress().getProvince() + result.getRegeocodeAddress().getCity() + result.getRegeocodeAddress().getDistrict() + result.getRegeocodeAddress().getTownship();
//                firstItem = new PoiItem("regeo", searchLatlonPoint, address, address);
//                doSearchQuery();
//            }
//        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    private void doSearchQuery() {
        currentPage = 0;
        // 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query = new PoiSearch.Query("", "", locationBean.getCityCode());
        query.setCityLimit(true);
        // 设置每页最多返回多少条poiitem
        query.setPageSize(20);
        query.setPageNum(currentPage);
        if (searchLatlonPoint != null) {
            try {
                poiSearch = new PoiSearch(this, query);
                poiSearch.setOnPoiSearchListener(this);
                poiSearch.setBound(new PoiSearch.SearchBound(searchLatlonPoint, 1000, true));
                poiSearch.searchPOIAsyn();
            } catch (AMapException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public interface OnCheckLocationPermission {
        void permissionGranted();
    }

    private class OnInputTipsListener implements Inputtips.InputtipsListener{

        @Override
        public void onGetInputtips(List<Tip> list, int code) {
            if (code == AMapException.CODE_AMAP_SUCCESS) {// 正确返回
                autoTips = list;
                List<String> listString = new ArrayList<String>();
                for (int i = 0; i < list.size(); i++) {
                    listString.add(list.get(i).getName());
                }
                ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(
                        getApplicationContext(),
                        android.R.layout.simple_list_item_1, listString);
                et_keyword.setAdapter(aAdapter);
//                et_keyword.setText("");
                aAdapter.notifyDataSetChanged();
                if (isfirstinput) {
                    isfirstinput = false;
                    et_keyword.showDropDown();
                }

            }
        }
    }
}
