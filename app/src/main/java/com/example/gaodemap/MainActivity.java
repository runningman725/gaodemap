package com.example.gaodemap;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.net.wifi.aware.DiscoverySession;
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
import android.widget.ImageView;
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
import com.amap.api.services.poisearch.Photo;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiResultV2;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearchV2;
import com.example.utils.CheckPermissionUtil;
import com.example.utils.GenerateSHA1;
import com.google.gson.Gson;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
    private boolean scrollList = false;
    private boolean editText = false;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한 동의
                    Toast.makeText(this, "위치 권한 오픈！", Toast.LENGTH_LONG).show();

                    initMap();

                } else {
                    Toast.makeText(this, "위치 권한 거절, 맵 기능 사용 불가！", Toast.LENGTH_LONG).show();
                }

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AMapLocationClient.updatePrivacyAgree(this.getApplicationContext(), true);
        AMapLocationClient.updatePrivacyShow(this.getApplicationContext(), true, true);
//        GenerateSHA1.getSHA1(this);   //debug버전 SHA1 값 획득
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.map);
        //activity가 onCreate 집행시 mMapView.onCreate(savedInstanceState)로 지도 생성
        mMapView.onCreate(savedInstanceState);
        initView();
        resultData = new ArrayList<>();
    }

    private void initView() {
        et_keyword = findViewById(R.id.et_keyword);
        rlv_location = findViewById(R.id.rlv_location);
        searchResultAdapter = new GaoDeSearchResultAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rlv_location.setLayoutManager(layoutManager);
        rlv_location.setAdapter(searchResultAdapter);
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
                String content = s.toString().trim();
                if (!scrollList) {
                    rlv_location.setVisibility(View.GONE);
                }
                if(!content.equals("") && !scrollList){
                    //두번째 param 값이 null이거나 ""이면 도시 아닌 전국 범위에서 검색
                    InputtipsQuery inputquery = new InputtipsQuery(content, "");//locationBean.getCity()
//                    inputquery.setCityLimit(true);//도시 한정
                    Inputtips inputTips = new Inputtips(MainActivity.this, inputquery);
                    inputTips.setInputtipsListener(new OnInputTipsListener());
                    inputTips.requestInputtipsAsyn();
                }
                scrollList = false;
                editText = true;
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

    }

    private void searchPoi(Tip result) {
        try {
            isInputKeySearch = true;
            inputSearchKey = result.getName();
            searchLatlonPoint = result.getPoint();
            firstItem = new PoiItem("tip", searchLatlonPoint, inputSearchKey, result.getAddress());
            firstItem.setCityName(result.getDistrict());
            firstItem.setAdName("");
            resultData.clear();
            searchResultAdapter.setSelectedPosition(0);
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
        //지도 공제 대상 초기화
        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        //지도 축소 레벨 설정
        aMap.moveCamera(CameraUpdateFactory.zoomTo(16));
        //위치 모니터링
        aMap.setLocationSource(this);
        // true이면 위치 촉발,디폴트는 false
        aMap.setMyLocationEnabled(true);
        // 위치 유형 설정
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);

        //bluepoint 초기화
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.interval(2000); //연속 위치 측정 시간 설정
        aMap.setMyLocationStyle(myLocationStyle);//bluepoint style설정
        aMap.getUiSettings().setMyLocationButtonEnabled(true); //화면 오른쪽 윗쪽에 나의 현재 위치로 이동하는 버튼 설정
        aMap.setMyLocationEnabled(true);//bluepoint 표시 설정,false하면 위치 측정도 하지 않음
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//연속 위치 측정,디폴트 모식,1초에 한번씩 측정,bluepoint 화살 표시 방향이 설비 방향에 따라 이동

        myLocationStyle.showMyLocation(true);
        aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                if (!isItemClickAction && !isInputKeySearch) {
                    geoAddress();
                }
                searchLatlonPoint = new LatLonPoint(cameraPosition.target.latitude, cameraPosition.target.longitude);
                Log.e(TAG, "onCameraChangeFinish: searchLatlonPoint=="+searchLatlonPoint);
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

    }

    private void addMarkerInScreenCenter(LatLng locationLatLng) {
        LatLng latLng = aMap.getCameraPosition().target;
        Point screenPosition = aMap.getProjection().toScreenLocation(latLng);
        locationMarker = aMap.addMarker(new MarkerOptions()
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marker)));
        //marker가 지도와 함께 움직이지 않고 화면에 고정 설정
        locationMarker.setPositionByPixels(screenPosition.x, screenPosition.y);
    }

    /**
     * response inverse geocoding
     */
    public void geoAddress() {
//        et_keyword.setText("");
        //첫번째 param은 Latlng, 두번째 param은 m범위, 세번째 param은 좌표계 유형
        RegeocodeQuery query = new RegeocodeQuery(searchLatlonPoint, 200, GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //지도 상태 저장
        mMapView.onSaveInstanceState(outState);
    }

    //setLocationSource() 적용후 respond
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            //initialization positioning
            try {
                mlocationClient = new AMapLocationClient(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            //initialization positioning parameter
            mLocationOption = new AMapLocationClientOption();
            //set up location callback monitoring
            mlocationClient.setLocationListener(this);
            mLocationOption.setOnceLocation(true);
            //Set to High Accuracy Positioning Mode
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //disable caching mechanism
            mLocationOption.setLocationCacheEnable(false);
            //Set whether to return address information (default return address information)
            mLocationOption.setNeedAddress(true);
            //Set the positioning interval in milliseconds, 2000 ms by default, 1000 ms minimum.
//            mLocationOption.setInterval(3000);
            //set positioning parameters
            mlocationClient.setLocationOption(mLocationOption);
            mlocationClient.startLocation();//start positioning
        }
    }

    //setLocationSource() 적용후 respond
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    //setLocationListener()적용후 respond
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null&&amapLocation != null) {
            if (amapLocation != null
                    &&amapLocation.getErrorCode() == 0) {
                    mListener.onLocationChanged(amapLocation);
                    Log.e(TAG, "onLocationChanged: ===="+amapLocation);
                    LatLng curLatlng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                    searchLatlonPoint = new LatLonPoint(curLatlng.latitude, curLatlng.longitude);
                    isInputKeySearch = false;

                LocationBean bean = new LocationBean();
                bean.setLatitude(amapLocation.getLatitude());
                bean.setLongitude(amapLocation.getLongitude());
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
                String errText = "위치 지정 실패," + amapLocation.getErrorCode()+ ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr===",errText);
            }
        }
    }

    //setOnPoiSearchListener() 적용후 respond
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
        resultData.add(firstItem);
        resultData.addAll(poiItems);
        Log.e(TAG, "updateListview: poiItems=="+new Gson().toJson(poiItems));
        for(PoiItem item:poiItems){
            Log.e(TAG, "updateListview: item==="+item.getPhotos().size());
            List<Photo> photos = item.getPhotos();
            for (int i = 0; i < photos.size(); i++) {
                String title = photos.get(i).getTitle();
                String url = photos.get(i).getUrl();
                Log.e(TAG, "updateListview: photo==="+url);
            }
        }
        searchResultAdapter.setData(resultData);
        searchResultAdapter.setLatLng(locationBean.getLongitude(),locationBean.getLatitude());
        if (editText) {
            rlv_location.setVisibility(View.VISIBLE);
        }
        searchResultAdapter.setSelectedPosition(0);
        rlv_location.scrollToPosition(0);
        searchResultAdapter.notifyDataSetChanged();
        searchResultAdapter.setOnItemClickListener(new GaoDeSearchResultAdapter.OnLocationClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position != searchResultAdapter.getSelectedPosition()) {
                    scrollList = true;
                    PoiItem poiItem = (PoiItem) searchResultAdapter.getItem(position);
                    LatLng curLatlng = new LatLng(poiItem.getLatLonPoint().getLatitude(), poiItem.getLatLonPoint().getLongitude());
                    isItemClickAction = true;
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLatlng, 16f));
                    searchResultAdapter.setSelectedPosition(position);
                    searchResultAdapter.notifyDataSetChanged();
                    et_keyword.setText(poiItem.getTitle());
                }
            }
        });
    }
    //加载图片
    public Bitmap getURLimage(String url) {
        Bitmap bmp = null;
        try {
            URL myurl = new URL(url);
            // 获得连接
            HttpURLConnection conn = (HttpURLConnection) myurl.openConnection();
            conn.setConnectTimeout(6000);//设置超时
            conn.setDoInput(true);
            conn.setUseCaches(false);//不缓存
            conn.connect();
            InputStream is = conn.getInputStream();//获得图片的数据流
            bmp = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bmp;
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    //setOnGeocodeSearchListener() 실행후 respond
    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getRegeocodeAddress() != null
                    && result.getRegeocodeAddress().getFormatAddress() != null) {
                String address = result.getRegeocodeAddress().getProvince() + result.getRegeocodeAddress().getCity() + result.getRegeocodeAddress().getDistrict() + result.getRegeocodeAddress().getTownship();
                firstItem = new PoiItem("regeo", searchLatlonPoint, address, address);
                Log.e(TAG, "onRegeocodeSearched: address==="+address);
                doSearchQuery();
            }
        }
    }

    //setOnGeocodeSearchListener() 실행후 respond
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    private void doSearchQuery() {
        currentPage = 0;
        //첫 번째 인자는 검색 문자열을 나타내고, 두 번째 인자는 poi 검색 유형을 나타내며, 세 번째 인자는 poi 검색 영역을 나타냅니다(빈 문자열은 전국을 나타냄).
        query = new PoiSearch.Query("", "","");//locationBean.getCityCode()
        query.setCityLimit(true);
        query.setExtensions("all");
        // 페이지당 최대 몇 개의 poiitem을 반환할지 설정합니다
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
            if (code == AMapException.CODE_AMAP_SUCCESS) {// 성공
                autoTips = list;
                List<String> listString = new ArrayList<String>();
                for (int i = 0; i < list.size(); i++) {
                    listString.add(list.get(i).getName());
                }
                ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(
                        getApplicationContext(),
                        android.R.layout.simple_list_item_1, listString);
                et_keyword.setAdapter(aAdapter);
                aAdapter.notifyDataSetChanged();
                if (isfirstinput) {
                    isfirstinput = false;
                    et_keyword.showDropDown();
                }

            }
        }
    }
}
