package tv.baokan.baokanandroid.ui.fragment;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import tv.baokan.baokanandroid.R;
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
    private NewsFragmentPagerAdapter mFragmentPageAdapter;

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
        LogUtils.d(TAG, "prepareUI");
        return view;
    }

    @Override
    protected void loadData() {

        LogUtils.d(TAG, "loadData");
        // 如果没有缓存则加载assets里的默认数据
        String jsonString = StreamUtils.readAssetsFile(mContext, "column.json");

        // 读取本地json数据
        loadNewsColumn(jsonString);

        // 配置ViewPager
        mFragmentPageAdapter = new NewsFragmentPagerAdapter(getChildFragmentManager(), newsListFragments, selectedList);
        mNewsViewPager.setAdapter(mFragmentPageAdapter);
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
            NewsListFragment newsListFragment = NewsListFragment.newInstance(selectedList.get(i).getClassId(), true);
            newsListFragments.add(newsListFragment);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_COLUMN:
                if (resultCode == RESULT_OK) {
                    // 清空集合数据
                    selectedList.clear();
                    optionalList.clear();
                    newsListFragments.clear();

                    selectedList.addAll((List<ColumnBean>) data.getSerializableExtra("selectedList_key"));
                    optionalList.addAll((List<ColumnBean>) data.getSerializableExtra("optionalList_key"));
                    for (int i = 0; i < selectedList.size(); i++) {
                        NewsListFragment newsListFragment = NewsListFragment.newInstance(selectedList.get(i).getClassId(), true);
                        newsListFragments.add(newsListFragment);
                    }

                    // 重新加载数据
                    mFragmentPageAdapter.reloadData(newsListFragments, selectedList);
                }
                break;
        }
    }

    // tab viewPager适配器
    private class NewsFragmentPagerAdapter extends FragmentPagerAdapter {

        private List<ColumnBean> mSelectedList = new ArrayList<>();
        private List<NewsListFragment> mNewsListFragments = new ArrayList<>();

        NewsFragmentPagerAdapter(FragmentManager fm, List<NewsListFragment> newsListFragments, List<ColumnBean> selectedList) {
            super(fm);
            this.mNewsListFragments.addAll(newsListFragments);
            this.mSelectedList.addAll(selectedList);
        }

        /**
         * 重新加载数据
         *
         * @param newNewsListFragments 新的fragment集合
         * @param newSelectedList      新的选中分类集合
         */
        public void reloadData(List<NewsListFragment> newNewsListFragments, List<ColumnBean> newSelectedList) {

            // 清除原有数据源
            this.mNewsListFragments.clear();
            this.mSelectedList.clear();

            // 重新添加数据源
            this.mNewsListFragments.addAll(newNewsListFragments);
            this.mSelectedList.addAll(newSelectedList);

            // 刷新数据
            notifyDataSetChanged();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return super.instantiateItem(container, position);
        }

        @Override
        public Fragment getItem(int position) {
            return mNewsListFragments.get(position);
        }

        @Override
        public int getCount() {
            return mNewsListFragments.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // 重写父类销毁方法，就切换viewPager上的列表就不会重复去加载数据，但是会增加内存占用
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mSelectedList.get(position).getClassName();
        }
    }

}
