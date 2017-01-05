package tv.baokan.baokanandroid.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tv.baokan.baokanandroid.utils.LogUtils;

public abstract class BaseFragment extends Fragment {

    private static final String TAG = "BaseFragment";

    // 承载fragment的activity的上下文
    protected Context mContext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return prepareUI();
    }

    /**
     * 强制子类重写，准备UI
     *
     * @return fragment加载的视图
     */
    protected abstract View prepareUI();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData();
    }

    /**
     * 在activity创建成功后才会调用，加载页面需要的数据
     */
    protected void loadData() {
    }

}
