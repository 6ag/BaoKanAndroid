package tv.baokan.baokanandroid.ui.fragment;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.adapter.TabFragmentPagerAdapter;
import tv.baokan.baokanandroid.app.BaoKanApp;
import tv.baokan.baokanandroid.model.ColumnBean;
import tv.baokan.baokanandroid.ui.activity.ColumnActivity;
import tv.baokan.baokanandroid.utils.LogUtils;
import tv.baokan.baokanandroid.utils.StreamUtils;

import static android.app.Activity.RESULT_OK;

public class NewsFragment extends BaseFragment {

    private static final String TAG = "NewsFragment";

    public static final int REQUEST_CODE_COLUMN = 0;

    private TabLayout mNewsTabLayout;
    private ViewPager mNewsViewPager;
    private ImageButton mNewsClassAdd;
    private List<ColumnBean> selectedList = new ArrayList<>();
    private List<ColumnBean> optionalList = new ArrayList<>();
    List<NewsListFragment> newsListFragments = new ArrayList<>();
    private TabFragmentPagerAdapter mFragmentPageAdapter;

    @Override
    protected View prepareUI() {
        View view = View.inflate(mContext, R.layout.fragment_news, null);
        mNewsTabLayout = (TabLayout) view.findViewById(R.id.tl_news_tabLayout);
        mNewsViewPager = (ViewPager) view.findViewById(R.id.vp_news_viewPager);
        mNewsClassAdd = (ImageButton) view.findViewById(R.id.ib_news_class_add);

        // 点击加号进入栏目编辑activity
        mNewsClassAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 弹出栏目管理activity
                ColumnActivity.start(getActivity(), selectedList, optionalList);
            }
        });
        return view;
    }

    @Override
    protected void loadData() {

        // 判断手机缓存里有没有栏目数据，有则加载，无咋加载默认json数据
        if (StreamUtils.fileIsExists(BaoKanApp.getContext().getFileStreamPath("column.json").getAbsolutePath())) {
            String jsonString = StreamUtils.readStringFromFile("column.json");
            loadNewsColumn(jsonString);
        } else {
            String jsonString = StreamUtils.readAssetsFile(mContext, "column.json");
            loadNewsColumn(jsonString);
        }

        // 配置ViewPager
        mFragmentPageAdapter = new TabFragmentPagerAdapter(getChildFragmentManager(), newsListFragments, selectedList);
        mNewsViewPager.setAdapter(mFragmentPageAdapter);
        mNewsViewPager.setOffscreenPageLimit(3);
        mNewsTabLayout.setupWithViewPager(mNewsViewPager);
        mNewsTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mNewsViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                mNewsViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                mNewsViewPager.setCurrentItem(tab.getPosition());
            }
        });

    }

    /**
     * 初始化新闻栏目数据 - 这里数据存数据库
     */
    private void loadNewsColumn(String jsonString) {

        // 清空集合数据
        selectedList.clear();
        optionalList.clear();
        newsListFragments.clear();

        LogUtils.d(TAG, "jsonString = " + jsonString);
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray selectedJSONArray = jsonObject.getJSONArray("selected");
            JSONArray optionalJSONArray = jsonObject.getJSONArray("optional");

            for (int i = 0; i < selectedJSONArray.length(); i++) {
                ColumnBean columnBean = new ColumnBean(
                        selectedJSONArray.getJSONObject(i).getString("classid"),
                        selectedJSONArray.getJSONObject(i).getString("classname"));
                selectedList.add(columnBean);
            }

            for (int i = 0; i < optionalJSONArray.length(); i++) {
                ColumnBean columnBean = new ColumnBean(
                        optionalJSONArray.getJSONObject(i).getString("classid"),
                        optionalJSONArray.getJSONObject(i).getString("classname"));
                optionalList.add(columnBean);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.d(TAG, "数据解析失败");
        }

        for (int i = 0; i < selectedList.size(); i++) {
            NewsListFragment newsListFragment = NewsListFragment.newInstance(selectedList.get(i).getClassid(), true);
            newsListFragments.add(newsListFragment);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_COLUMN:
                // 处理修改栏目顺序或数量后重新加载
                if (resultCode == RESULT_OK) {
                    // 清空集合数据
                    selectedList.clear();
                    optionalList.clear();
                    newsListFragments.clear();

                    selectedList.addAll((List<ColumnBean>) data.getSerializableExtra("selectedList_key"));
                    optionalList.addAll((List<ColumnBean>) data.getSerializableExtra("optionalList_key"));
                    for (int i = 0; i < selectedList.size(); i++) {
                        NewsListFragment newsListFragment = NewsListFragment.newInstance(selectedList.get(i).getClassid(), true);
                        newsListFragments.add(newsListFragment);
                    }

                    // 重新加载ViewPager数据
                    mFragmentPageAdapter.reloadData(newsListFragments, selectedList);
                }
                break;
        }
    }

}
