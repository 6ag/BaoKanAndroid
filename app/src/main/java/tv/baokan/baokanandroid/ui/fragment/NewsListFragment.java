package tv.baokan.baokanandroid.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.lcodecore.tkrefreshlayout.header.SinaRefreshView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.adapter.NewsListRecyclerViewAdapter;
import tv.baokan.baokanandroid.cache.NewsDALManager;
import tv.baokan.baokanandroid.model.ArticleListBean;
import tv.baokan.baokanandroid.ui.activity.NewsDetailActivity;
import tv.baokan.baokanandroid.utils.APIs;
import tv.baokan.baokanandroid.utils.LogUtils;
import tv.baokan.baokanandroid.utils.NetworkUtils;
import tv.baokan.baokanandroid.utils.ProgressHUD;

public class NewsListFragment extends BaseFragment {

    private static final String TAG = "NewsListFragment";
    private String classid;      // 栏目id
    private int pageIndex = 1;   // 当前页码

    private TwinklingRefreshLayout refreshLayout;  // 上下拉刷新
    private RecyclerView mNewsListRecyclerView;    // 列表视图
    private NewsListRecyclerViewAdapter newsListAdapter;       // 列表视图的适配器
    private boolean isShowBanner; // 是否显示列表顶部的banner

    public static NewsListFragment newInstance(String classid, boolean isShowBanner) {
        NewsListFragment newFragment = new NewsListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("classid", classid);
        bundle.putBoolean("isShowBanner", isShowBanner);
        newFragment.setArguments(bundle);
        return newFragment;
    }

    @Override
    protected View prepareUI() {
        View view = View.inflate(mContext, R.layout.fragment_news_list, null);
        mNewsListRecyclerView = (RecyclerView) view.findViewById(R.id.rv_news_list_recyclerview);
        refreshLayout = (TwinklingRefreshLayout) view.findViewById(R.id.srl_news_list_refresh);
        return view;
    }

    @Override
    protected void loadData() {

        // 取出构造里的分类id和isShowBanner
        Bundle args = getArguments();
        if (args != null) {
            classid = args.getString("classid");
            isShowBanner = args.getBoolean("isShowBanner");
        }

        // 配置recyclerView资讯列表
        setupRecyclerView();

        // 配置刷新
        setupRefresh();

    }

    /**
     * 配置recyclerView资讯列表
     */
    private void setupRecyclerView() {
        mNewsListRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        newsListAdapter = new NewsListRecyclerViewAdapter(mContext, isShowBanner);
        mNewsListRecyclerView.setAdapter(newsListAdapter);
        newsListAdapter.setOnItemTapListener(new NewsListRecyclerViewAdapter.OnItemTapListener() {
            @Override
            public void onItemTapListener(ArticleListBean articleListBean) {
                // 打开文章详情
                openArticleDetail(articleListBean);
            }
        });

        // 监听滚动
        mNewsListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        if (!Fresco.getImagePipeline().isPaused()) {
                            Fresco.getImagePipeline().pause();
                        }
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                    case RecyclerView.SCROLL_STATE_IDLE:
                        if (Fresco.getImagePipeline().isPaused()) {
                            Fresco.getImagePipeline().resume();
                        }
                        break;
                }
            }
        });
    }

    /**
     * 配置刷新控件
     */
    private void setupRefresh() {

        // 顶部刷新视图
        SinaRefreshView sinaRefreshView = new SinaRefreshView(mContext);
        sinaRefreshView.setArrowResource(R.drawable.pull_refresh_arrow);
        refreshLayout.setHeaderView(sinaRefreshView);

        // 到达底部自动加载更多
        refreshLayout.setAutoLoadMore(true);

        // 监听刷新
        refreshLayout.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onRefresh(final TwinklingRefreshLayout refreshLayout) {
                // 下拉刷新数据时，在有网络情况下清除本地缓存的数据
                if (NetworkUtils.shared.isNetworkConnected(mContext)) {
                    NewsDALManager.shared.removeNewsList(classid);
                }

                // 重新加载并缓存数据
                loadNewsFromNetwork(classid, 1, 0);

            }

            @Override
            public void onLoadMore(final TwinklingRefreshLayout refreshLayout) {
                pageIndex += 1;
                loadNewsFromNetwork(classid, pageIndex, 1);
            }
        });

        // 默认加载一次数据 不使用下拉刷新
        loadNewsFromNetwork(classid, 1, 0);

    }

    /**
     * 加载列表数据
     *
     * @param classid   分类id
     * @param pageIndex 页码
     * @param method    加载方式 0下拉 1上拉
     */
    private void loadNewsFromNetwork(final String classid, int pageIndex, final int method) {

        // 从数据访问层加载数据
        NewsDALManager.shared.loadNewsList("news", classid, pageIndex, new NewsDALManager.NewsListCallback() {
            @Override
            public void onSuccess(JSONArray jsonArray) {
                try {
                    List<ArticleListBean> tempListBeans = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        ArticleListBean bean = new ArticleListBean(jsonArray.getJSONObject(i));
                        tempListBeans.add(bean);
                    }
                    if (tempListBeans.size() == 0) {
                        ProgressHUD.showInfo(mContext, "没有数据了~");
                    } else {
                        // 刷新数据
                        newsListAdapter.updateData(tempListBeans, method);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    if (method == 0) {
                        refreshLayout.finishRefreshing();
                    } else {
                        refreshLayout.finishLoadmore();
                    }
                }
            }

            @Override
            public void onError(String tipString) {
                ProgressHUD.showInfo(mContext, tipString);
                if (method == 0) {
                    refreshLayout.finishRefreshing();
                } else {
                    refreshLayout.finishLoadmore();
                }
            }
        });

    }

    /**
     * 打开文章详情页面
     *
     * @param articleBean 文章模型
     */
    private void openArticleDetail(ArticleListBean articleBean) {
        NewsDetailActivity.start(getActivity(), articleBean.getClassid(), articleBean.getId());
    }

}
