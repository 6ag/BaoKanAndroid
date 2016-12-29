package tv.baokan.baokanandroid.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.lcodecore.tkrefreshlayout.header.SinaRefreshView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.model.ArticleListBean;
import tv.baokan.baokanandroid.ui.activity.PhotoDetailActivity;
import tv.baokan.baokanandroid.utils.APIs;
import tv.baokan.baokanandroid.utils.LogUtils;

public class PhotoListFragment extends BaseFragment {

    private String classid;     // 栏目id
    private int pageIndex = 1;  // 当前页码

    private TwinklingRefreshLayout refreshLayout;  // 上下拉刷新
    private RecyclerView mPhotoListRecyclerView;    // 列表视图
    private PhotoListAdapter newsListAdapter;       // 列表视图的适配器
    private List<ArticleListBean> articleListBeans = new ArrayList<>(); // 列表数据

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

        mPhotoListRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        newsListAdapter = new PhotoListAdapter();
        mPhotoListRecyclerView.setAdapter(newsListAdapter);

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

        // 设置刷新监听器
        setupRefresh();
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
        if (articleListBeans.size() == 0) {
            refreshLayout.startRefresh();
        }

    }

    /**
     * 加载列表数据
     *
     * @param classid   分类id
     * @param pageIndex 页码
     * @param method    加载方式 0下拉 1上拉
     */
    private void loadNewsFromNetwork(final String classid, int pageIndex, final int method) {
        OkHttpUtils
                .get()
                .url(APIs.ARTICLE_LIST)
                .addParams("table", "photo")
                .addParams("classid", classid)
                .addParams("pageIndex", String.valueOf(pageIndex))
                .addParams("pageSize", String.valueOf(20))
                .build()
                .execute(new StringCallback() {

                    @Override
                    public void onResponse(String response, int id) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            List<ArticleListBean> tempListBeans = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                ArticleListBean bean = new ArticleListBean(jsonArray.getJSONObject(i));
                                tempListBeans.add(bean);
                            }

                            String maxId = "0";
                            String minId = "0";
                            if (articleListBeans.size() > 0) {
                                maxId = articleListBeans.get(0).getId();
                                minId = articleListBeans.get(articleListBeans.size() - 1).getId();
                            }

                            if (method == 0) {
                                // 下拉刷新
                                if (maxId.compareTo(tempListBeans.get(0).getId()) <= -1) {
                                    articleListBeans = tempListBeans;
                                    // 刷新列表数据
                                    newsListAdapter.notifyDataSetChanged();
                                }
                            } else {
                                // 上拉加载
                                if (minId.compareTo(tempListBeans.get(0).getId()) >= 1) {
                                    articleListBeans.addAll(tempListBeans);
                                    // 刷新列表数据
                                    newsListAdapter.notifyDataSetChanged();
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

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        LogUtils.d("loadNewsFromNetwork 失败", e.getMessage());
                    }

                });
    }

    /**
     * 打开图片详情页面
     *
     * @param articleBean 文章模型
     */
    private void openPhotoDetail(ArticleListBean articleBean) {
        PhotoDetailActivity.start(mContext, articleBean.getClassid(), articleBean.getId());
        getActivity().overridePendingTransition(R.anim.push_enter, R.anim.push_exit);
    }

    // 新闻列表数据适配器
    private class PhotoListAdapter extends RecyclerView.Adapter<PhotoListAdapter.ViewHolder> {

        @Override
        public int getItemCount() {
            return articleListBeans.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.cell_photo_list, parent, false);
            final ViewHolder holder = new ViewHolder(view);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 进入图片详情
                    openPhotoDetail(articleListBeans.get(holder.getAdapterPosition()));
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ArticleListBean bean = articleListBeans.get(position);
            holder.titleTextView.setText(bean.getTitle());
            holder.titlePicView.setImageURI(bean.getTitlepic());
        }

        // viewHolder基类
        class ViewHolder extends RecyclerView.ViewHolder {

            View itemView;
            SimpleDraweeView titlePicView;
            TextView titleTextView;

            ViewHolder(View itemView) {
                super(itemView);
                this.itemView = itemView;
                titlePicView = (SimpleDraweeView) itemView.findViewById(R.id.sdv_cell_photo_list_pic);
                titleTextView = (TextView) itemView.findViewById(R.id.tv_cell_photo_list_title);
            }
        }

    }

}
