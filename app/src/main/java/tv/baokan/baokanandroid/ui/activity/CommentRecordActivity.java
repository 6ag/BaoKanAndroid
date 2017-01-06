package tv.baokan.baokanandroid.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

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
import tv.baokan.baokanandroid.adapter.CommentRecordRecyclerViewAdapter;
import tv.baokan.baokanandroid.model.CommentRecordBean;
import tv.baokan.baokanandroid.model.UserBean;
import tv.baokan.baokanandroid.utils.APIs;
import tv.baokan.baokanandroid.utils.NetworkUtils;
import tv.baokan.baokanandroid.utils.ProgressHUD;
import tv.baokan.baokanandroid.widget.NavigationViewRed;

public class CommentRecordActivity extends BaseActivity {

    private static final String TAG = "CommentRecordActivity";
    private int pageIndex = 1;
    private NavigationViewRed mNavigationViewRed;
    private TwinklingRefreshLayout mRefreshLayout;         // 上下拉刷新
    private RecyclerView mCollectionRecordRecyclerView;    // 列表视图
    private CommentRecordRecyclerViewAdapter mAdapter;  // 适配器

    /**
     * 便捷启动当前activity
     *
     * @param activity 启动当前activity的activity
     */
    public static void start(Activity activity) {
        Intent intent = new Intent(activity, CommentRecordActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.push_enter, R.anim.push_exit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_record);

        prepareUI();
        prepareData();
        setupRefresh();
    }

    /**
     * 准备UI
     */
    private void prepareUI() {
        mNavigationViewRed = (NavigationViewRed) findViewById(R.id.nav_comment);
        mRefreshLayout = (TwinklingRefreshLayout) findViewById(R.id.srl_comment_record_list_refresh);
        mCollectionRecordRecyclerView = (RecyclerView) findViewById(R.id.rv_comment_record_list);
        mNavigationViewRed.setupNavigationView(true, false, "我的评论", new NavigationViewRed.OnClickListener() {
            @Override
            public void onBackClick(View v) {
                finish();
            }
        });
    }

    /**
     * 准备数据
     */
    private void prepareData() {
        mCollectionRecordRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new CommentRecordRecyclerViewAdapter(mContext);
        mCollectionRecordRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemTapListener(new CommentRecordRecyclerViewAdapter.OnItemTapListener() {
            @Override
            public void onItemTapListener(CommentRecordBean commentRecordBean) {
                if (commentRecordBean.getTbname().equals("photo")) {
                    PhotoDetailActivity.start(mContext, commentRecordBean.getClassid(), commentRecordBean.getId());
                } else {
                    NewsDetailActivity.start(mContext, commentRecordBean.getClassid(), commentRecordBean.getId());
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
        mRefreshLayout.setHeaderView(sinaRefreshView);

        // 到达底部自动加载更多
        mRefreshLayout.setAutoLoadMore(true);

        // 监听刷新
        mRefreshLayout.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onRefresh(final TwinklingRefreshLayout refreshLayout) {

                // 重新加载并缓存数据
                loadCollectionFromNetwork(1, 0);
            }

            @Override
            public void onLoadMore(final TwinklingRefreshLayout refreshLayout) {
                pageIndex += 1;
                loadCollectionFromNetwork(pageIndex, 1);
            }
        });

        // 默认加载一次数据 不使用下拉刷新
        mRefreshLayout.startRefresh();

    }

    /**
     * 加载收藏数据从网络
     *
     * @param pageIndex 页码
     * @param method    0下拉 1上拉
     */
    private void loadCollectionFromNetwork(int pageIndex, final int method) {

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("username", UserBean.shared().getUsername());
        parameters.put("userid", UserBean.shared().getUserid());
        parameters.put("token", UserBean.shared().getToken());
        parameters.put("pageIndex", String.valueOf(pageIndex));

        NetworkUtils.shared.get(APIs.GET_USER_COMMENT, parameters, new NetworkUtils.StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                ProgressHUD.showInfo(mContext, "您的网络不给力");
                if (method == 0) {
                    mRefreshLayout.finishRefreshing();
                } else {
                    mRefreshLayout.finishLoadmore();
                }
            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("err_msg").equals("success")) {
                        if (jsonObject.getString("data").equals("null")) {
                            ProgressHUD.showInfo(mContext, "没有更多数据了");
                        } else {
                            List<CommentRecordBean> commentRecordBeanList = new ArrayList<>();
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                CommentRecordBean commentRecordBean = new CommentRecordBean(jsonArray.getJSONObject(i));
                                commentRecordBeanList.add(commentRecordBean);
                            }

                            // 更新数据
                            mAdapter.updateData(commentRecordBeanList, method);
                        }
                    } else {
                        ProgressHUD.showInfo(mContext, jsonObject.getString("info"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ProgressHUD.showInfo(mContext, "数据解析异常");
                } finally {
                    if (method == 0) {
                        mRefreshLayout.finishRefreshing();
                    } else {
                        mRefreshLayout.finishLoadmore();
                    }
                }
            }
        });

    }

}

