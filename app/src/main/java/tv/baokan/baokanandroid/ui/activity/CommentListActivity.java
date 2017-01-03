package tv.baokan.baokanandroid.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.lcodecore.tkrefreshlayout.header.SinaRefreshView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.adapter.CommentRecyclerViewAdapter;
import tv.baokan.baokanandroid.model.CommentBean;
import tv.baokan.baokanandroid.utils.APIs;
import tv.baokan.baokanandroid.utils.LogUtils;
import tv.baokan.baokanandroid.utils.NetworkUtils;
import tv.baokan.baokanandroid.utils.ProgressHUD;
import tv.baokan.baokanandroid.widget.NavigationViewRed;

public class CommentListActivity extends BaseActivity {

    private static final String TAG = "CommentListActivity";
    private String origin; // 来源  news  photo
    private String classid;
    private String id;
    private List<CommentBean> commentBeanList;
    private CommentRecyclerViewAdapter mCommentRecyclerViewAdapter; // 适配器

    private int pageIndex = 1; // 页码
    private NavigationViewRed mNavigationViewRed;
    private TwinklingRefreshLayout refreshLayout;  // 上下拉刷新
    private RecyclerView mCommentListRecyclerView;    // 列表视图

    /**
     * 便捷启动当前activity
     *
     * @param activity 启动当前activity的activity
     */
    public static void start(Activity activity, String classid, String id, List<CommentBean> commentBeanList, String origin) {
        Intent intent = new Intent(activity, CommentListActivity.class);
        if (commentBeanList != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("commentBeanList_key", (Serializable) commentBeanList);
            intent.putExtras(bundle);
        }
        intent.putExtra("classid_key", classid);
        intent.putExtra("id_key", id);
        intent.putExtra("origin_key", origin);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.push_enter, R.anim.push_exit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_list);

        // 取出传递过来的数据
        Intent intent = getIntent();
        origin = intent.getStringExtra("origin_key");
        if (origin.equals("news")) {
            // 来自资讯详情，则有默认评论数据
            commentBeanList = (List<CommentBean>) intent.getSerializableExtra("commentBeanList_key");
        }
        classid = intent.getStringExtra("classid_key");
        id = intent.getStringExtra("id_key");

        mNavigationViewRed = (NavigationViewRed) findViewById(R.id.nav_comment_list);
        refreshLayout = (TwinklingRefreshLayout) findViewById(R.id.srl_comment_list_refresh);
        mCommentListRecyclerView = (RecyclerView) findViewById(R.id.rv_comment_list_recyclerview);

        mNavigationViewRed.setupNavigationView(true, false, "评论", new NavigationViewRed.OnClickListener() {
            @Override
            public void onBackClick(View v) {
                finish();
            }
        });

        // 配置评论列表
        setupRecyclerView();

        // 配置刷新控件
        setupRefresh();

    }

    /**
     * 配置评论数据
     */
    private void setupRecyclerView() {

        // 加载评论数据
        mCommentRecyclerViewAdapter = new CommentRecyclerViewAdapter(this);
        mCommentListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCommentListRecyclerView.setAdapter(mCommentRecyclerViewAdapter);

        // 如果是来自资讯详情，则有初始数据，直接刷新数据
        if (origin.equals("news")) {
            mCommentRecyclerViewAdapter.updateData(commentBeanList, 0);
        }

        // 监听评论里的各种tap事件
        mCommentRecyclerViewAdapter.setOnCommentTapListener(new CommentRecyclerViewAdapter.OnCommentTapListener() {

            // 评论点赞
            @Override
            public void onStarTap(final CommentBean commentBean, final int position) {
                HashMap<String, String> parameters = new HashMap<>();
                parameters.put("classid", commentBean.getClassid());
                parameters.put("id", commentBean.getId());
                parameters.put("plid", commentBean.getPlid());
                parameters.put("dopl", "1");
                parameters.put("action", "DoForPl");

                NetworkUtils.shared.post(APIs.TOP_DOWN, parameters, new NetworkUtils.StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        ProgressHUD.showInfo(mContext, "您的网络不给力哦");
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getString("err_msg").equals("success")) {
                                int newZcnum = Integer.valueOf(commentBean.getZcnum()).intValue() + 1;
                                commentBean.setZcnum(String.valueOf(newZcnum));
                                commentBean.setStar(true);
                                mCommentRecyclerViewAdapter.notifyItemChanged(position);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ProgressHUD.showInfo(mContext, "数据解析失败");
                        }
                    }
                });
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
                        loadCommentListFromNetwork(1, 0);
                    }
                }, 500);
            }

            @Override
            public void onLoadMore(final TwinklingRefreshLayout refreshLayout) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pageIndex += 1;
                        loadCommentListFromNetwork(pageIndex, 1);
                    }
                }, 500);
            }
        });

        // 如果是来自图库，则没有初始数据，需要去加载
        if (origin.equals("photo")) {
            refreshLayout.startRefresh();
        }

    }

    /**
     * 加载评论数据
     *
     * @param pageIndex 页码
     * @param method    加载方式
     */
    private void loadCommentListFromNetwork(int pageIndex, final int method) {

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("classid", classid);
        parameters.put("id", id);
        parameters.put("pageIndex", String.valueOf(pageIndex));
        parameters.put("pageSize", "10");

        NetworkUtils.shared.get(APIs.GET_COMMENT, parameters, new NetworkUtils.StringCallback() {
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
                    // 如果所有接口响应格式是统一的，这些判断是可以封装在网络请求工具类里的哦
                    if (new JSONObject(response).getString("err_msg").equals("success")) {
                        List<CommentBean> tempBeanList = new ArrayList<>();
                        JSONArray jsonArray = new JSONObject(response).getJSONArray("data");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            CommentBean commentBean = new CommentBean(jsonArray.getJSONObject(i));
                            tempBeanList.add(commentBean);
                        }
                        commentBeanList = tempBeanList;

                        if (tempBeanList.size() == 0) {
                            ProgressHUD.showInfo(mContext, "没有数据了~");
                        } else {
                            // 刷新数据
                            mCommentRecyclerViewAdapter.updateData(tempBeanList, method);
                        }
                    } else {
                        String errorInfo = new JSONObject(response).getString("info");
                        if (errorInfo != null) {
                            ProgressHUD.showInfo(mContext, errorInfo);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ProgressHUD.showInfo(mContext, "数据解析失败");
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

}
