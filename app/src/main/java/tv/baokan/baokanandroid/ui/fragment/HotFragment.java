package tv.baokan.baokanandroid.ui.fragment;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.widget.NavigationViewRed;

public class HotFragment extends BaseFragment {

    private NavigationViewRed mNavigationViewRed;
    private NewsListFragment mNewsListFragment;

    @Override
    protected View prepareUI() {
        View view = View.inflate(mContext, R.layout.fragment_hot, null);
        mNavigationViewRed = (NavigationViewRed) view.findViewById(R.id.nav_hot);
        mNavigationViewRed.setupNavigationView(false, false, "近期热门", new NavigationViewRed.OnClickListener() {
            @Override
            public void onRightClick(View v) {
                Toast.makeText(mContext, "搜索", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    @Override
    protected void loadData() {
        mNewsListFragment = NewsListFragment.newInstance("461", false);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (mNewsListFragment.isAdded()) {
            transaction.show(mNewsListFragment);
        } else {
            transaction.add(R.id.fl_hot_content, mNewsListFragment);
        }
        transaction.commit();
    }
}
