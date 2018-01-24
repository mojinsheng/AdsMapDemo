package com.from.adsmaps;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amap.api.maps2d.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.from.adsmaps.bean.HomeAdapter;
import com.from.adsmaps.bean.ShousuoAdapter;

import java.util.List;

/**
 * Created by Administrator on 2018/1/23.
 */
public class ShousouActivity extends Activity implements PoiSearch.OnPoiSearchListener{
    private Button btn=null;
    private EditText editText;
    private LatLonPoint lp;//
    private RecyclerView mRvSearchText;
    private String str;
    private PoiSearch.Query query;
    private PoiSearch poiSearch;
    private PoiResult poiResult; // poi返回的结果
    private List<PoiItem> poiItems;// poi数据
    private ShousuoAdapter shousuoAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shousou);
       Button button=(Button) findViewById(R.id.btn);
        editText=(EditText)findViewById(R.id.edit_queryss);
        mRvSearchText=(RecyclerView)findViewById(R.id.id_recyclerview);

        InputLenLimit.lengthFilter(this,editText);
        LatLng point = (LatLng) getIntent().getParcelableExtra("point");
        lp=new LatLonPoint(point.latitude,point.longitude);

        LinearLayoutManager layoutManager = new LinearLayoutManager(ShousouActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRvSearchText.setLayoutManager(layoutManager);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str=editText.getText().toString().trim();
                if (TextUtils.isEmpty(str)) {
                    Toast.makeText(ShousouActivity.this, "请输入搜索关键字",Toast.LENGTH_LONG).show();
                    return;
                } else {
                    doSearchQueryWithKeyWord(str);
                    //KeyBoardUtils.closeKeybord(mEtContent,SeaechTextAddressActivity.this);
                }

            }
        });

    }
    protected void doSearchQueryWithKeyWord(String key) {
       int currentPage = 0;
        query = new PoiSearch.Query(key, "", "广州市");// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
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

    @Override
    public void onPoiSearched(PoiResult result, int rcode) {
        if (rcode == 1000) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息

//                    mDatas.clear();
//                    //mDatas.add(mAddressTextFirst);// 第一个元素
//                    AddressSearchTextEntity addressEntity = null;
                    mRvSearchText.setVisibility(View.VISIBLE);
                    mRvSearchText.setAdapter(shousuoAdapter = new ShousuoAdapter(ShousouActivity.this,poiItems));
                    for (int i=0;i<poiItems.size();i++) {
                        PoiItem poiItem = poiItems.get(i);
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

                    }
                }
            } else {
                       Toast.makeText(ShousouActivity.this, "对不起，没有搜索到相关数据！",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }
}
