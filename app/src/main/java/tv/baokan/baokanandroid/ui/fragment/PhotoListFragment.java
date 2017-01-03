package tv.baokan.baokanandroid.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

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
import tv.baokan.baokanandroid.adapter.PhotoListRecyclerViewAdapter;
import tv.baokan.baokanandroid.model.ArticleListBean;
import tv.baokan.baokanandroid.ui.activity.PhotoDetailActivity;
import tv.baokan.baokanandroid.utils.APIs;
import tv.baokan.baokanandroid.utils.NetworkUtils;
import tv.baokan.baokanandroid.utils.ProgressHUD;

public class PhotoListFragment extends BaseFragment {

    private String classid;     // 栏目id
    private int pageIndex = 1;  // 当前页码

    private TwinklingRefreshLayout refreshLayout;  // 上下拉刷新
    private RecyclerView mPhotoListRecyclerView;    // 列表视图
    private PhotoListRecyclerViewAdapter newsListAdapter;       // 列表视图的适配器

    public static PhotoListFragment newInstance(String classid) {
        PhotoListFragment newFragment = new PhotoListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("classid", classid);
        newFragment.setArguments(bundle);
        return newFragment;
    }

    @Override
    protected View prepareUI() {
        View view = View.inflate(mContext, R.layout.fragment_news_list, null);
        mPhotoListRecyclerView = (RecyclerView) view.findViewById(R.id.rv_news_list_recyclerview);
        refreshLayout = (TwinklingRefreshLayout) view.findViewById(R.id.srl_news_list_refresh);
        return view;
    }

    @Override
    protected void loadData() {

        // 取出构造里的分类id
        Bundle args = getArguments();
        if (args != null) {
            classid = args.getString("classid");
        }

        // 配置recyclerView图库列表
        setupRecyclerView();

        // 设置刷新监听器
        setupRefresh();
    }

    /**
     * 配置recyclerView图库列表
     */
    private void setupRecyclerView() {
        mPhotoListRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        newsListAdapter = new PhotoListRecyclerViewAdapter(mContext);
        mPhotoListRecyclerView.setAdapter(newsListAdapter);
        newsListAdapter.setOnItemTapListener(new PhotoListRecyclerViewAdapter.OnItemTapListener() {
            @Override
            public void onItemTapListener(ArticleListBean articleListBean) {
                openPhotoDetail(articleListBean);
            }
        });

        // 监听滚动
        mPhotoListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadNewsFromNetwork(classid, 1, 0);
                    }
                }, 500);
            }

            @Override
            public void onLoadMore(final TwinklingRefreshLayout refreshLayout) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pageIndex += 1;
                        loadNewsFromNetwork(classid, pageIndex, 1);
                    }
                }, 500);
            }
        });

        // 默认加载一次数据
        refreshLayout.startRefresh();

    }

    /**
     * 加载列表数据
     *
     * @param classid   分类id
     * @param pageIndex 页码
     * @param method    加载方式 0下拉 1上拉
     */
    private void loadNewsFromNetwork(final String classid, int pageIndex, final int method) {

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("table", "photo");
        parameters.put("classid", classid);
        parameters.put("pageIndex", String.valueOf(pageIndex));
        parameters.put("pageSize", String.valueOf(20));

        NetworkUtils.shared.get(APIs.ARTICLE_LIST, parameters, new NetworkUtils.StringCallback() {

            @Override
            public void onError(Call call, Exception e, int id) {
                ProgressHUD.showInfo(mContext, "您的网络不给力哦");
                // 结束刷新
                if (method == 0) {
                    refreshLayout.finishRefreshing();
                } else {
                    refreshLayout.finishLoadmore();
                }
            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    if (new JSONObject(response).getString("err_msg").equals("success")) {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        List<ArticleListBean> tempListBeans = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            ArticleListBean bean = new ArticleListBean(jsonArray.getJSONObject(i));
                            tempListBeans.add(bean);
                        }
                        if (tempListBeans.size() == 0) {
                            ProgressHUD.showInfo(mContext, "没有数据了~");
                        } else {
                            // 更新图片列表数据
                            newsListAdapter.updateData(tempListBeans, method);
                        }
                    } else {
                        String errorInfo = new JSONObject(response).getString("info");
                        if (errorInfo != null) {
                            ProgressHUD.showInfo(mContext, errorInfo);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    // 结束刷新
                    if (method == 0) {
                        refreshLayout.finishRefreshing();
                    } else {
                        refreshLayout.finishLoadmore();
                    }
                }

            }
        });

    }

    /**
     * 打开图片详情页面
     *
     * @param articleBean 文章模型
     */
    private void openPhotoDetail(ArticleListBean articleBean) {
        PhotoDetailActivity.start(getActivity(), articleBean.getClassid(), articleBean.getId());
    }

}
