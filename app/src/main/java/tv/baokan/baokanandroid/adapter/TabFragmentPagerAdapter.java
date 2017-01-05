package tv.baokan.baokanandroid.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import tv.baokan.baokanandroid.model.ColumnBean;
import tv.baokan.baokanandroid.ui.fragment.BaseFragment;

/**
 * 资讯、图秀里viewPager的适配器
 */
public class TabFragmentPagerAdapter extends FragmentPagerAdapter {

    private List<ColumnBean> mSelectedList = new ArrayList<>();
    private List<BaseFragment> mListFragments = new ArrayList<>();

    public TabFragmentPagerAdapter(FragmentManager fm, List<? extends BaseFragment> listFragments, List<ColumnBean> selectedList) {
        super(fm);
        this.mListFragments.addAll(listFragments);
        this.mSelectedList.addAll(selectedList);
    }

    /**
     * 重新加载数据
     *
     * @param newListFragments 新的fragment集合
     * @param newSelectedList  新的选中分类集合
     */
    public void reloadData(List<? extends BaseFragment> newListFragments, List<ColumnBean> newSelectedList) {

        // 清除原有数据源
        this.mListFragments.clear();
        this.mSelectedList.clear();

        // 重新添加数据源
        this.mListFragments.addAll(newListFragments);
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
        return mListFragments.get(position);
    }

    @Override
    public int getCount() {
        return mListFragments.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // 重写父类销毁方法，就切换viewPager上的列表就不会重复去加载数据，但是会增加内存占用
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mSelectedList.get(position).getClassname();
    }

}
