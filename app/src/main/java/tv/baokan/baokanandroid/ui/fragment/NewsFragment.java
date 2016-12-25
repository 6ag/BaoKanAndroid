package tv.baokan.baokanandroid.ui.fragment;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.baokan.baokanandroid.R;

public class NewsFragment extends BaseFragment {

    private TabLayout mNewsTabLayout;
    private ViewPager mNewsViewPager;
    private ImageButton mNewsClassAdd;
    private List<Map<String, String>> selectedList;
    private List<Map<String, String>> optionalList;
    List<NewsListFragment> newsListFragments;

    @Override
    protected View prepareUI() {
        View view = View.inflate(mContext, R.layout.fragment_news, null);
        mNewsTabLayout = (TabLayout) view.findViewById(R.id.tl_news_tabLayout);
        mNewsViewPager = (ViewPager) view.findViewById(R.id.vp_news_viewPager);
        mNewsClassAdd = (ImageButton) view.findViewById(R.id.ib_news_class_add);
        return view;
    }

    @Override
    protected void loadData() {
        selectedList = new ArrayList<>();
        optionalList = new ArrayList<>();
        newsListFragments = new ArrayList<>();

        // 初始化数据
        initNewsColumn();

        mNewsViewPager.setAdapter(new NewsFragmentPagerAdapter(getChildFragmentManager()));
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

        mNewsClassAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "点击了加号", Toast.LENGTH_SHORT).show();
            }
        });

     }

     /**
     * 初始化新闻栏目数据 - 这里数据存数据库
     */
    private void initNewsColumn() {
        String[] selectedIds = new String[]{"0", "2", "21", "12", "264", "33", "34", "212", "132", "396", "119"};
        String[] selectedNames = new String[]{"今日头条", "网文快讯", "媒体视角", "网文IP", "企业资讯", "作家风采", "维权在线", "业者动态", "风花雪月", "独家报道", "求职招聘"};
        String[] optionalIds = new String[]{"32", "102", "111", "115", "51", "440", "209", "208", "405", "394", "414", "281", "57", "58", "56"};
        String[] optionalNames = new String[]{"高端访谈", "政策解读", "写作指导", "征稿信息", "精彩活动", "写作常识", "数据分析", "统计图表", "名家专栏", "传统文学", "写作素材", "游戏世界", "娱乐八卦", "社会杂谈", "影视动画"};

        for (int i = 0; i < selectedIds.length; i++) {
            Map<String, String> item = new HashMap<>();
            item.put("classid", selectedIds[i]);
            item.put("classname", selectedNames[i]);
            selectedList.add(item);
        }

        for (int i = 0; i < optionalIds.length; i++) {
            Map<String, String> item = new HashMap<>();
            item.put("classid", optionalIds[i]);
            item.put("classname", optionalNames[i]);
            optionalList.add(item);
        }

        for (int i = 0; i < selectedList.size(); i++) {
            NewsListFragment newsListFragment = NewsListFragment.newInstance(selectedList.get(i).get("classid"));
            newsListFragments.add(newsListFragment);
        }

    }

    private class NewsFragmentPagerAdapter extends FragmentPagerAdapter {

        public NewsFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return newsListFragments.get(position);
        }

        @Override
        public int getCount() {
            return selectedList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return selectedList.get(position).get("classname");
        }
    }

}
