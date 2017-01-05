package tv.baokan.baokanandroid.ui.fragment;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.adapter.TabFragmentPagerAdapter;
import tv.baokan.baokanandroid.model.ColumnBean;

public class PhotoFragment extends BaseFragment {

    private TabLayout mPhotoTabLayout;
    private ViewPager mPhotoViewPager;
    private List<ColumnBean> mSelectedList;
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
        mSelectedList = new ArrayList<>();
        mPhotoListFragments = new ArrayList<>();

        // 初始化数据
        initNewsColumn();

        mPhotoViewPager.setAdapter(new TabFragmentPagerAdapter(getChildFragmentManager(), mPhotoListFragments, mSelectedList));
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
     * 初始化新闻栏目数据 - 这个模块是写死的，无需缓存。所以直接在项目中写死了
     */
    private void initNewsColumn() {
        String[] selectedIds = new String[]{"322", "434", "366", "338", "354", "357", "433"};
        String[] selectedNames = new String[]{"图话网文", "精品封面", "游戏图库", "娱乐八卦", "社会百态", "旅游视野", "军事图秀"};

        for (int i = 0; i < selectedIds.length; i++) {
            ColumnBean columnBean = new ColumnBean(selectedIds[i], selectedNames[i]);
            mSelectedList.add(columnBean);
        }

        for (int i = 0; i < mSelectedList.size(); i++) {
            PhotoListFragment photoListFragment = PhotoListFragment.newInstance(mSelectedList.get(i).getClassid());
            mPhotoListFragments.add(photoListFragment);
        }

    }

}
