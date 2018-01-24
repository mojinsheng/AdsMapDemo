package com.from.adsmaps;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.from.adsmaps.bean.DateBean;
import com.from.adsmaps.bean.HomeAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationSource, AMapLocationListener,AMap.OnCameraChangeListener,PoiSearch.OnPoiSearchListener,GeocodeSearch.OnGeocodeSearchListener {
    private MapView mapView;
    private AMap aMap;
    private PoiResult poiResult; // poi返回的结果
    //声明定位回调监听器
    //public AMapLocationListener mLocationListener = new AMapLocationListener();
    //声明AMapLocationClientOption对象
    private List<PoiItem> poiItems;// poi数据
    private  MyLocationStyle myLocationStyle;
    private LatLonPoint lp;//
    private LatLng mFinalChoosePosition; //最终选择的点
    //定位需要的声明
    private AMapLocationClient mLocationClient = null;//定位发起端
    private List<DateBean> dateBeanList;
    //标识，用于判断是否只显示一次定位信息和用户重新定位
    private boolean isFirstLoc = true;
    private static final int OPEN_SEARCH = 0X0001;
    private PoiSearch.Query query;// Poi查询条件类
    private LatLonPoint mCurrentPoint;
    private boolean isHandDrag = true;
    private boolean isFirstLoadList = true;
    private boolean isBackFromSearchChoose = false;
    private EditText editText;
LocationSource.OnLocationChangedListener mListener;
    AMapLocationClient mlocationClient;
    AMapLocationClientOption mLocationOption;
    private MarkerOptions mMarkerOptions;
    private Marker mCenterMarker;
    private GeocodeSearch geocoderSearch;
    private List<SuggestionCity> suggestionCities;
    private GeocodeSearch mGeocoderSearch;
    private RecyclerView mRecyclerView;
    private Button btn;
    private HomeAdapter mAdapter;
//    private RvAddressSearchTextAdapter mRvAddressSearchTextAdapter;
//    private ArrayList<AddressSearchTextEntity> mDatas = new ArrayList<>();
    private PoiSearch.Query mPoiQuery;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        editText=(EditText)findViewById(R.id.edit_query);
        btn=(Button)findViewById(R.id.btn);
        mRecyclerView = (RecyclerView) findViewById(R.id.id_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        InputLenLimit.lengthFilter(this,editText);


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ShousouActivity.class);
                intent.putExtra("point", mFinalChoosePosition);
                startActivityForResult(intent, OPEN_SEARCH);
                isBackFromSearchChoose = false;

            }
        });
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ShousouActivity.class);
                intent.putExtra("point", mFinalChoosePosition);
                startActivityForResult(intent, OPEN_SEARCH);
                isBackFromSearchChoose = false;

            }
        });

        mapView=(MapView)findViewById(R.id.btnmap);
        mapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mapView.getMap();
            // 自定义系统定位小蓝点
            MyLocationStyle myLocationStyle = new MyLocationStyle();
            // 设置小蓝点的图标
            myLocationStyle.myLocationIcon(BitmapDescriptorFactory.
                    fromResource(R.drawable.weizhi));// 设置小蓝点的图标
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
//        LatLng point = (LatLng) getIntent().getParcelableExtra("point");
//        lp=new LatLonPoint(point.latitude,point.longitude);
        //------------------------------------------添加中心标记
        mMarkerOptions = new MarkerOptions();
        mMarkerOptions.draggable(false);//可拖放性
        mMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.weizhi));
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
        mGeocoderSearch = new GeocodeSearch(this);
                //geocoderSearch = new GeocodeSearch(this);

    }

    /**
     * 根据关键字收搜赴京
     * @param key
     */

    public void doSearchQueryWithKeyWord(String key) {
        int currentPage = 0;
        query = new PoiSearch.Query(key, "", "广州");// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query.setPageSize(20);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页
        query.setCityLimit(true); //限定城市

        if (lp != null) {
            poiSearch = new PoiSearch(this, query);
            poiSearch.setOnPoiSearchListener(this);   // 实现  onPoiSearched  和  onPoiItemSearched
            poiSearch.setBound(new PoiSearch.SearchBound(lp, 5000, true));//
            // 设置搜索区域为以lp点为圆心，其周围5000米范围
            poiSearch.searchPOIAsyn();// 异步搜索
        }
    }
    //定位
    private void initLoc() {
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (null != mlocationClient) {
            mlocationClient.onDestroy();
        }
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        //Log.i("tapfuns","onMarkerClick==========="+amapLocation.getLatitude()+","+ amapLocation.getLongitude());
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见官方定位类型表
                amapLocation.getLatitude();//获取纬度
                amapLocation.getLongitude();//获取经度
                amapLocation.getAccuracy();//获取精度信息
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(amapLocation.getTime());
                df.format(date);//定位时间
                amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                amapLocation.getCountry();//国家信息
                amapLocation.getProvince();//省信息
                amapLocation.getCity();//城市信息
                amapLocation.getDistrict();//城区信息
                amapLocation.getStreet();//街道信息
                amapLocation.getStreetNum();//街道门牌号信息
                amapLocation.getCityCode();//城市编码
                amapLocation.getAdCode();//地区编码
//                    aMap.moveCamera(CameraUpdateFactory.zoomTo(20));
//                    //将地图移动到定位点
//                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude())));
//                    //点击定位按钮 能够将地图的中心移动到定位点
//                    mListener.onLocationChanged(amapLocation);
//                // 如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
                if (isFirstLoc) {
                    //设置缩放级别
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(20));
                    //将地图移动到定位点
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude())));
                    //点击定位按钮 能够将地图的中心移动到定位点
                    mListener.onLocationChanged(amapLocation);
//                    //添加图钉
//                    //getMarkerOptionsd(amapLocation);
//                    marker=aMap.addMarker(getMarkerOptions(amapLocation));
//                    //获取定位信息
//                    StringBuffer buffer = new StringBuffer();
//                    buffer.append(amapLocation.getCountry() + "" + amapLocation.getProvince() + "" + amapLocation.getCity() + "" + amapLocation.getProvince() + "" + amapLocation.getDistrict() + "" + amapLocation.getStreet() + "" + amapLocation.getStreetNum());
//                    Toast.makeText(getApplicationContext(), buffer.toString(), Toast.LENGTH_LONG).show();
                    isFirstLoc = false;
                }
           }
        }
    }
    //自定义一个图钉，并且设置图标，当我们点击图钉时，显示设置的信息
    private MarkerOptions getMarkerOptions(final AMapLocation amapLocation) {
        //设置图钉选项
        MarkerOptions options = new MarkerOptions();
        //图标
       options.icon(BitmapDescriptorFactory.fromResource(R.drawable.weizhi));
        //位置
        options.position(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude()));
        //options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.mapicon));

        //options.position(new LatLng(amapLocation.getLatitude()-3, amapLocation.getLongitude()-8));
//        StringBuffer buffer = new StringBuffer();
//        buffer.append(amapLocation.getCountry() + "" + amapLocation.getProvince() + "" + amapLocation.getCity() +  "" + amapLocation.getDistrict() + "" + amapLocation.getStreet() + "" + amapLocation.getStreetNum());
//        //标题
//        options.title(buffer.toString());
//        //子标题
//        options.snippet("这里好火");
        //设置多少帧刷新一次图片资源
        options.period(60);
       options.draggable(true);//设置Marker可拖动
      //options.setFlat(true);//设置marker平贴地图效果

        final Marker mCenterMarker = aMap.addMarker(options);
       ViewTreeObserver vto = mapView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                mCenterMarker.setPositionByPixels(mapView.getWidth() >> 1, mapView.getHeight() >> 1);
                mCenterMarker.showInfoWindow();
            }
        });
        return options;

    }
    //自定义一个图钉，并且设置图标，当我们点击图钉时，显示设置的信息
    private ArrayList<MarkerOptions> getMarkerOptionsd(AMapLocation amapLocation) {
        //设置图钉选项
        ArrayList<MarkerOptions> markerOptions = new ArrayList<MarkerOptions>();
        MarkerOptions options=null;
        Log.i("tapfuns","Latitude:"+amapLocation.getLatitude()+",Location:"+amapLocation.getLongitude());
        //图标

        //位置
        for(int i=0;i<dateBeanList.size();i++){
            options=new MarkerOptions();
            options.position(new LatLng(dateBeanList.get(i).getLatitude(), dateBeanList.get(i).getLongitude()-i));
          options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.mapicon));
            StringBuffer buffer = new StringBuffer();
            buffer.append(amapLocation.getCountry() + "" + amapLocation.getProvince() + "" + amapLocation.getCity() +  "" + amapLocation.getDistrict() + "" + amapLocation.getStreet() + "" + amapLocation.getStreetNum());
            //标题
            options.draggable(true);//设置Marker可拖动
            options.title(buffer.toString());
            //子标题
            options.snippet("这里好火");
            //设置多少帧刷新一次图片资源
            options.period(60);
            //markerOptions.add(options);
            aMap.addMarker(options);
        }
        //options.position(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude()));
        // options.position(new LatLng(amapLocation.getLatitude()-3, amapLocation.getLongitude()-8));


        return markerOptions;

    }
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            //mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
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
//    @Override
//    public void onPointerCaptureChanged(boolean hasCapture) {
//
//    }
    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        LatLng target = cameraPosition.target;
        Log.i("tapfun",target.latitude + "jinjin------" + target.longitude);
    }



    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        // TODO Auto-generated method stub
        Log.i("tapfun", "jinjin------" );
        mCurrentPoint = new LatLonPoint(cameraPosition.target.
                latitude, cameraPosition.target.longitude);
        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        mFinalChoosePosition=cameraPosition.target;
        RegeocodeQuery query = new RegeocodeQuery(mCurrentPoint, 500, GeocodeSearch.AMAP);
        Log.i("tapfun", "onCameraChangeFinish------" );
        mGeocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
       // getAddress(cameraPosition.target);
        doSearchQuery();
    }
    public void getAddress(final LatLng latLonPoint) {
        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(convertToLatLonPoint(latLonPoint), 200, GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
    }
    /**
     * 把LatLng对象转化为LatLonPoint对象
     */
    public static LatLonPoint convertToLatLonPoint(LatLng latlon) {
        return new LatLonPoint(latlon.latitude, latlon.longitude);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(this,"回到主界面",Toast.LENGTH_LONG).show();
        if (requestCode == OPEN_SEARCH && resultCode == 1000) {
            final PoiItem poiItem = data.getParcelableExtra("backEntity");


            isBackFromSearchChoose = true;
            isHandDrag = false;
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(poiItem.getLatLonPoint().getLatitude(), poiItem.getLatLonPoint().getLongitude()), 20));
        }


    }

    @Override
    public void onPoiSearched(PoiResult result, int rcode) {
        Log.i("tapfun", "onPoiSearched------result:"+result+",rcode:"+rcode );
        if (rcode ==1000) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    // 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();
                    mRecyclerView.setAdapter(mAdapter = new HomeAdapter(MainActivity.this,poiItems));
                  //showSuggestCity(suggestionCities);
                    for (int i=0;i<poiItems.size();i++) {
                        PoiItem poiItem = poiItems.get(i);
//                        if(i==0){
//                            addressEntity = new AddressSearchTextEntity(poiItem.getTitle(),poiItem.getSnippet(),true,poiItem.getLatLonPoint());
//                        }else{
//                            addressEntity = new AddressSearchTextEntity(poiItem.getTitle(),poiItem.getSnippet(),false,poiItem.getLatLonPoint());
//                        }
                        Log.i("tapfuns","得到的数据 poiItem "
                                + "\npoiItem.getSnippet()"+poiItem.getSnippet()
                                + "\npoiItem.getAdCode()"+poiItem.getAdCode()
                                + "\npoiItem.getAdName()"+poiItem.getAdName()
                                + "\npoiItem.getDirection()"+poiItem.getDirection()
                                + "\npoiItem.getBusinessArea()"+poiItem.getBusinessArea()
                                + "\npoiItem.getCityCode()"+poiItem.getCityCode()
                                + "\npoiItem.getEmail()"+poiItem.getEmail()
                                + "\npoiItem.getParkingType()"+poiItem.getParkingType()
                                + "\npoiItem.getCityName()"+poiItem.getCityName()
                                + "\npoiItem.getProvinceName()"+poiItem.getProvinceName()
                                + "\npoiItem.getSnippet()"+poiItem.getSnippet()
                                + "\npoiItem.getTitle()"+poiItem.getTitle()
                                + "\npoiItem.getTypeDes()"+poiItem.getTypeDes()
                                + "\npoiItem.getDistance()"+poiItem.getDistance()
                                + "\npoiItem.getWebsite()"+poiItem.getWebsite()
                        );

                        //mDatas.add(addressEntity);
                    }

                    /**
                     * listviw具体操作逻辑
                     */
                }
            } else if (suggestionCities != null
                    && suggestionCities.size() > 0) {
                showSuggestCity(suggestionCities);
            }else {
                Toast.makeText(this, "对不起，没有搜索到相关数据！",Toast.LENGTH_LONG);
            }
        }
    }
    private PoiSearch poiSearch;
    protected void doSearchQuery() {
        Log.i("tapfuns","doSearchQuery");
       int currentPage = 0;
        query = new PoiSearch.Query("", "", "广州市");// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query.setPageSize(20);// 设置每页最多返回多少条poiitem
        query.setPageNum(0);// 设置查第一页
        query.setCityLimit(true); //限定城市

        LatLonPoint lpTemp = convertToLatLonPoint(mFinalChoosePosition);
        Log.i("tapfuns",lpTemp+"");
        if (lpTemp != null) {
            poiSearch = new PoiSearch(this, query);
            poiSearch.setOnPoiSearchListener(this);  // 实现  onPoiSearched  和  onPoiItemSearched
            poiSearch.setBound(new PoiSearch.SearchBound(lpTemp, 5000, true));//
            // 设置搜索区域为以lp点为圆心，其周围5000米范围
            poiSearch.searchPOIAsyn();// 异步搜索
        }
    }


    @Override
    public void onPoiItemSearched(PoiItem poiitem, int rcode) {

    }
    /**
     * poi没有搜索到数据，返回一些推荐城市的信息
     */
    private void showSuggestCity(List<SuggestionCity> cities) {
        String infomation = "推荐城市\n"+cities.size();
        for (int i = 0; i < cities.size(); i++) {
            infomation += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
                    + cities.get(i).getCityCode() + "城市编码:"
                    + cities.get(i).getAdCode() + "\n";
        }
        Log.i("tapfuns", infomation);
    }


    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
//        if (rCode == 0) {
//            if (result != null && result.getRegeocodeAddress() != null &&
//                    result.getRegeocodeAddress().getFormatAddress() != null) {
//                /**
//                 * 汽车服务|汽车销售|汽车维修|摩托车服务|餐饮服务|购物服务|生活服务|
//                 * 体育休闲服务|医疗保健服务|住宿服务|风景名胜|商务住宅|政府机构及社会团体
//                 * |科教文化服务|交通设施服务|金融保险服务|公司企业|道路附属设施|地名地址信息|公共设施
//                 */
//                mPoiQuery = new PoiSearch.Query("", "住宿服务|公司企业",
//                        result.getRegeocodeAddress().getCityCode());
//                mPoiQuery.setPageSize(10);// 设置每页最多返回多少条poiitem
//                mPoiQuery.setPageNum(0);//设置查第一页
//                PoiSearch poiSearch = new PoiSearch(this, mPoiQuery);
//                poiSearch.setOnPoiSearchListener(this);//设置数据返回的监听器 (5)
//                //设置周边搜索的中心点以及区域
//                poiSearch.setBound(new PoiSearch.SearchBound(mCurrentPoint, 1500, true));
//                poiSearch.searchPOIAsyn();//开始搜索
//            } else {
//                Toast.makeText(this, "失败",Toast.LENGTH_LONG).show();
//            }
//        } else {
//            Toast.makeText(this, "失败",Toast.LENGTH_LONG).show();
//        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }
}
