package com.from.adsmaps;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.EditText;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;

import java.util.List;

public class MainActivity extends Activity implements LocationSource, AMapLocationListener,AMap.OnCameraChangeListener,GeocodeSearch.OnGeocodeSearchListener {
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    //声明定位回调监听器
    //public AMapLocationListener mLocationListener = new AMapLocationListener();
    //声明AMapLocationClientOption对象
    private  MyLocationStyle myLocationStyle;
    //定位需要的声明
    private AMapLocationClient mLocationClient = null;//定位发起端
    //标识，用于判断是否只显示一次定位信息和用户重新定位
    LocationSource.OnLocationChangedListener mListener;
    AMapLocationClient mlocationClient;
    AMapLocationClientOption mLocationOption;

    private MapView mapView;
    private AMap aMap;
    private MarkerOptions mMarkerOptions;
    private Marker mCenterMarker;
    private GeocodeSearch mGeocoderSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        if (aMap == null) {
            aMap = mapView.getMap();
            // 自定义系统定位小蓝点
            MyLocationStyle myLocationStyle = new MyLocationStyle();
            // 设置小蓝点的图标
//            myLocationStyle.myLocationIcon(BitmapDescriptorFactory.
//                    fromResource(R.mipmap.ic_location_marker));// 设置小蓝点的图标
            myLocationStyle.strokeColor(0x7F0070D9);// 设置圆形的边框颜色
            myLocationStyle.radiusFillColor(0x130070D9);// 设置圆形的填充颜色
//             myLocationStyle.anchor(int,int)//设置小蓝点的锚点
            myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
            aMap.setMyLocationStyle(myLocationStyle);
            aMap.setLocationSource(this);// 设置定位监听（1）
            aMap.setOnCameraChangeListener(this);//手动移动地图监听 （2）
            aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
            //设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
            aMap.setMyLocationEnabled(true);
            aMap.moveCamera(CameraUpdateFactory.zoomTo(17.5f));
        }
        //------------------------------------------添加中心标记
        mMarkerOptions = new MarkerOptions();
        mMarkerOptions.draggable(false);//可拖放性
        //mMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_tips_nearby));
        mCenterMarker = aMap.addMarker(mMarkerOptions);
        ViewTreeObserver vto = mapView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                mCenterMarker.setPositionByPixels(mapView.getWidth() >> 1, mapView.getHeight() >> 1);
                mCenterMarker.showInfoWindow();
            }
        });
        //---------------------------------------------初始化正反编码类  （3）
        mGeocoderSearch = new GeocodeSearch(this);
        mGeocoderSearch.setOnGeocodeSearchListener(this);

    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }



    @Override
    public void onCameraChange(CameraPosition position) {
        LatLng target = position.target;
       Log.i("tapfun",target.latitude + "jinjin------" + target.longitude);
    }



    @Override
    public void onCameraChangeFinish(CameraPosition position) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {

    }

    @Override
    public void activate(OnLocationChangedListener mlocationClient) {
        mListener = onLocationChangedListener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);（4）
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()
            // 方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    @Override
    public void deactivate() {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}