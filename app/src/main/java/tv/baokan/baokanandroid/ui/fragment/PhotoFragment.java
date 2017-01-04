package tv.baokan.baokanandroid.ui.fragment;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.baokan.baokanandroid.R;

public class PhotoFragment extends BaseFragment {

    private TabLayout mPhotoTabLayout;
    private ViewPager mPhotoViewPager;
    private List<Map<String, String>> selectedList;
    List<PhotoListFragment> mPhotoListFragments;

    @Override
    protected View prepareUI() {
        View view = View.inflate(mContext, R.layout.fragment_photo, null);
        mPhotoTabLayout = (TabLayout) view.findViewById(R.id.tl_photo_tabLayout);
        mPhotoViewPager = (ViewPager) view.findViewById(R.id.vp_photo_viewPager);
        return view;
    }

    @Override
    protected void loadData() {
        selectedList = new ArrayList<>();
        mPhotoListFragments = new ArrayList<>();

        // 初始化数据
        initNewsColumn();

        mPhotoViewPager.setAdapter(new NewsFragmentPagerAdapter(getChildFragmentManager()));
        mPhotoTabLayout.setupWithViewPager(mPhotoViewPager);
        mPhotoTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mPhotoViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                mPhotoViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                mPhotoViewPager.setCurrentItem(tab.getPosition());
            }
        });

    }

    /**
     * 初始化新闻栏目数据 - 这里数据存数据库
     */
    private void initNewsColumn() {
        String[] selectedIds = new String[]{"322", "434", "366", "338", "354", "357", "433"};
        String[] selectedNames = new String[]{"图话网文", "精品封面", "游戏图库", "娱乐八卦", "社会百态", "旅游视野", "军事图秀"};

        for (int i = 0; i < selectedIds.length; i++) {
            Map<String, String> item = new HashMap<>();
            item.put("classid", selectedIds[i]);
            item.put("classname", selectedNames[i]);
            selectedList.add(item);
        }

        for (int i = 0; i < selectedList.size(); i++) {
            PhotoListFragment photoListFragment = PhotoListFragment.newInstance(selectedList.get(i).get("classid"));
            mPhotoListFragments.add(photoListFragment);
        }

    }

    private class NewsFragmentPagerAdapter extends FragmentPagerAdapter {

        NewsFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return super.instantiateItem(container, position);
        }

        @Override
        public Fragment getItem(int position) {
            return mPhotoListFragments.get(position);
        }

        @Override
        public int getCount() {
            return selectedList.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // 重写父类销毁方法，就切换viewPager上的列表就不会重复去加载数据，但是会增加内存占用
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return selectedList.get(position).get("classname");
        }
    }

}
