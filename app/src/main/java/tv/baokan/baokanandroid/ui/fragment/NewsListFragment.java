package tv.baokan.baokanandroid.ui.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.lcodecore.tkrefreshlayout.Footer.BottomProgressView;
import com.lcodecore.tkrefreshlayout.IHeaderView;
import com.lcodecore.tkrefreshlayout.OnAnimEndListener;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.lcodecore.tkrefreshlayout.header.SinaRefreshView;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.listener.OnBannerClickListener;
import com.youth.banner.loader.ImageLoader;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.app.BaoKanApp;
import tv.baokan.baokanandroid.model.ArticleListBean;
import tv.baokan.baokanandroid.utils.APIs;
import tv.baokan.baokanandroid.utils.DateUtils;
import tv.baokan.baokanandroid.utils.LogUtils;

public class NewsListFragment extends BaseFragment {

    private static final String TAG = "NewsListFragment";

    private String classid; // 栏目id
    private int pageIndex = 1; // 页码
    private TwinklingRefreshLayout refreshLayout;
    private RecyclerView mNewsListRecyclerView;
    private NewsListAdapter newsListAdapter;
    private List<ArticleListBean> articleListBeans = new ArrayList<>(); // 列表数据
    private List<ArticleListBean> isGoodArticleBeans = new ArrayList<>(); // 幻灯片数据
    private int headerCount = 3;

    public static NewsListFragment newInstance(String classid) {
        NewsListFragment newFragment = new NewsListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("classid", classid);
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

        // 取出构造里的分类id
        Bundle args = getArguments();
        if (args != null) {
            classid = args.getString("classid");
        }

        mNewsListRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        newsListAdapter = new NewsListAdapter();
        mNewsListRecyclerView.setAdapter(newsListAdapter);

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

        // 默认第一次加载网络数据
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
        OkHttpUtils
                .get()
                .url(APIs.ARTICLE_LIST)
                .addParams("table", "news")
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

                                    // 幻灯片数据
                                    if (tempListBeans.size() >= 3) {
                                        isGoodArticleBeans = new ArrayList<>();
                                        isGoodArticleBeans.add(tempListBeans.get(0));
                                        isGoodArticleBeans.add(tempListBeans.get(1));
                                        isGoodArticleBeans.add(tempListBeans.get(2));
                                    }

                                    // 刷新列表数据
                                    newsListAdapter.notifyDataSetChanged();
                                }
                                // 停止刷新
                                refreshLayout.finishRefreshing();
                            } else {
                                // 上拉加载
                                if (minId.compareTo(tempListBeans.get(0).getId()) >= 1) {
                                    articleListBeans.addAll(tempListBeans);
                                    // 刷新列表数据
                                    newsListAdapter.notifyDataSetChanged();
                                }
                                // 停止刷新
                                refreshLayout.finishLoadmore();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        LogUtils.d("loadNewsFromNetwork 失败", e.getMessage());
                    }

                });
    }

    /**
     * 配置recyclerView头部轮播
     */
    private void setupRecyclerViewHeader(Banner banner) {

        List<String> images = new ArrayList<>();
        List<String> titles = new ArrayList<>();

        for (ArticleListBean bean :
                isGoodArticleBeans) {
            images.add(bean.getTitlepic());
            titles.add(bean.getTitle());
        }

        banner.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (BaoKanApp.WINDOW_HEIGHT * 0.3)));

        // 配置banner
        banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE)
                .setImageLoader(new FrescoImageLoader())
                .setImages(images)
                .setBannerTitles(titles)
                .isAutoPlay(true)
                .setDelayTime(5000)
                .setBannerAnimation(Transformer.Default)
                .setIndicatorGravity(BannerConfig.RIGHT)
                .start();

        // 监听banner点击事件
        banner.setOnBannerClickListener(new OnBannerClickListener() {
            @Override
            public void OnBannerClick(int position) {
                Toast.makeText(mContext, isGoodArticleBeans.get(position).getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    // 轮播图片加载器
    public class FrescoImageLoader extends ImageLoader {

        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            imageView.setImageURI(Uri.parse((String) path));
        }

        @Override
        public ImageView createImageView(Context context) {
            return new SimpleDraweeView(context);
        }
    }

    // item类型枚举
    public enum NEWS_ITEM_TYPE {
        HEADER_VIEW, // 头部视图
        NO_PIC,      // 无图
        ONE_PIC,     // 单图
        MORE_PIC     // 多图
    }

    // 新闻列表数据适配器
    private class NewsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public int getItemCount() {
            if (articleListBeans.size() <= 3) {
                return 0;
            } else if (articleListBeans.size() > 3) {
                return articleListBeans.size() - 2;
            }
            return 0;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return NEWS_ITEM_TYPE.HEADER_VIEW.ordinal();
            }

            ArticleListBean bean = articleListBeans.get(position);
            if (!TextUtils.isEmpty(bean.getTitlepic()) && bean.getMorepic().length == 0) {
                return NEWS_ITEM_TYPE.ONE_PIC.ordinal();
            } else if (bean.getMorepic().length == 3) {
                return NEWS_ITEM_TYPE.MORE_PIC.ordinal();
            } else {
                return NEWS_ITEM_TYPE.NO_PIC.ordinal();
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            RecyclerView.ViewHolder holder;

            if (viewType == NEWS_ITEM_TYPE.HEADER_VIEW.ordinal()) {
                view = LayoutInflater.from(mContext).inflate(R.layout.header_cell_news_list_banner, parent, false);
                holder = new HeaderViewHolder(view);
            } else if (viewType == NEWS_ITEM_TYPE.NO_PIC.ordinal()) {
                view = LayoutInflater.from(mContext).inflate(R.layout.cell_news_list_nopic, parent, false);
                holder = new NoPicViewHolder(view);
            } else if (viewType == NEWS_ITEM_TYPE.ONE_PIC.ordinal()) {
                view = LayoutInflater.from(mContext).inflate(R.layout.cell_news_list_onepic, parent, false);
                holder = new OnePicViewHolder(view);
            } else {
                view = LayoutInflater.from(mContext).inflate(R.layout.cell_news_list_morepic, parent, false);
                holder = new MorePicViewHolder(view);
            }
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            // 头部视图则直接返回
            if (holder instanceof HeaderViewHolder) {
                HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                setupRecyclerViewHeader(headerViewHolder.banner);
                return;
            }

            ArticleListBean bean = articleListBeans.get(position);
            BaseViewHolder viewHolder = (BaseViewHolder) holder;
            viewHolder.titleTextView.setText(bean.getTitle());
            try {
                viewHolder.timeTextView.setText(DateUtils.timestampToDateString(bean.getNewstime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            viewHolder.plnumTextView.setText(bean.getPlnum());
            viewHolder.onclickTextView.setText(bean.getOnclick());

            if (holder instanceof OnePicViewHolder) {
                OnePicViewHolder onePicViewHolder = (OnePicViewHolder) holder;
                onePicViewHolder.imageView1.setImageURI(Uri.parse(bean.getTitlepic()));
            } else if (holder instanceof MorePicViewHolder) {
                MorePicViewHolder morePicViewHolder = (MorePicViewHolder) holder;
                morePicViewHolder.imageView1.setImageURI(Uri.parse(bean.getMorepic()[0]));
                morePicViewHolder.imageView2.setImageURI(Uri.parse(bean.getMorepic()[1]));
                morePicViewHolder.imageView3.setImageURI(Uri.parse(bean.getMorepic()[2]));
            }
        }

        // viewHolder基类
        class BaseViewHolder extends RecyclerView.ViewHolder {

            View itemView;
            TextView titleTextView;
            TextView timeTextView;
            TextView plnumTextView;
            TextView onclickTextView;

            BaseViewHolder(View itemView) {
                super(itemView);
                this.itemView = itemView;
                titleTextView = (TextView) itemView.findViewById(R.id.tv_cell_news_list_title);
                timeTextView = (TextView) itemView.findViewById(R.id.tv_cell_news_list_time);
                plnumTextView = (TextView) itemView.findViewById(R.id.tv_cell_news_list_plnum);
                onclickTextView = (TextView) itemView.findViewById(R.id.tv_cell_news_list_onclick);
            }
        }

        // 头部视图
        class HeaderViewHolder extends RecyclerView.ViewHolder {

            Banner banner;

            HeaderViewHolder(View itemView) {
                super(itemView);
                banner = (Banner) itemView.findViewById(R.id.b_news_list_banner);
            }
        }

        // 无图
        class NoPicViewHolder extends BaseViewHolder {

            NoPicViewHolder(View itemView) {
                super(itemView);
            }
        }

        // 单图
        class OnePicViewHolder extends BaseViewHolder {

            SimpleDraweeView imageView1;

            OnePicViewHolder(View itemView) {
                super(itemView);
                imageView1 = (SimpleDraweeView) itemView.findViewById(R.id.iv_cell_news_list_pic1);
            }
        }

        // 多图
        class MorePicViewHolder extends BaseViewHolder {

            SimpleDraweeView imageView1;
            SimpleDraweeView imageView2;
            SimpleDraweeView imageView3;

            MorePicViewHolder(View itemView) {
                super(itemView);
                imageView1 = (SimpleDraweeView) itemView.findViewById(R.id.iv_cell_news_list_pic1);
                imageView2 = (SimpleDraweeView) itemView.findViewById(R.id.iv_cell_news_list_pic2);
                imageView3 = (SimpleDraweeView) itemView.findViewById(R.id.iv_cell_news_list_pic3);
            }
        }
    }

}
