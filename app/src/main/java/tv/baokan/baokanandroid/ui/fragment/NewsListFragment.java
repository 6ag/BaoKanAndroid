package tv.baokan.baokanandroid.ui.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
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
import tv.baokan.baokanandroid.utils.APIs;
import tv.baokan.baokanandroid.utils.LogUtils;

public class NewsListFragment extends BaseFragment {

    private static final String TAG = "NewsListFragment";

    private String classid; // 栏目id
    private int pageIndex = 1; // 页码
    private RecyclerView mNewsListRecyclerView;
    private NewsListAdapter newsListAdapter;
    private List<ArticleListBean> articleListBeans = new ArrayList<>();

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
        LogUtils.d("NewsListFragment", this.toString());
        return view;
    }

    @Override
    protected void loadData() {

        // 取出构造里的分类id
        Bundle args = getArguments();
        if (args != null) {
            classid = args.getString("classid");
            LogUtils.d("NewsListFragment", "classid = " + classid);
        }

        mNewsListRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        newsListAdapter = new NewsListAdapter();
        mNewsListRecyclerView.setAdapter(newsListAdapter);

        // 加载网络数据
        loadNewsFromNetwork(classid, pageIndex, 0);

    }

    /**
     * 加载幻灯片数据
     *
     * @param classid 分类id
     */
    private void loadIsGoodFromNetwork(String classid) {

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

                            if (method == 0) { // 下拉刷新
                                if (maxId.compareTo(tempListBeans.get(0).getId()) <= -1) {
                                    articleListBeans = tempListBeans;
                                    LogUtils.d(TAG, "下拉刷新有新数据 classid = " + classid);
                                } else {
                                    LogUtils.d(TAG, "下拉刷新没有新数据了 classid = " + classid);
                                }
                            } else { // 上拉加载
                                if (minId.compareTo(tempListBeans.get(0).getId()) >= 1) {
                                    articleListBeans.addAll(tempListBeans);
                                    LogUtils.d(TAG, "上拉加载有更多数据 classid = " + classid);
                                } else {
                                    LogUtils.d(TAG, "上拉加载没有更多数据了 classid = " + classid);
                                }
                            }

                            // 刷新列表数据
                            newsListAdapter.notifyDataSetChanged();

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

    // item类型枚举
    public enum NEWS_ITEM_TYPE {
        NO_PIC,  // 无图
        ONE_PIC, // 单图
        MORE_PIC // 多图
    }

    private class NewsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public int getItemCount() {
            return articleListBeans.size();
        }

        @Override
        public int getItemViewType(int position) {
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
            if (viewType == NEWS_ITEM_TYPE.NO_PIC.ordinal()) {
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
            ArticleListBean bean = articleListBeans.get(position);
            BaseViewHolder viewHolder = (BaseViewHolder) holder;
            viewHolder.titleTextView.setText(bean.getTitle());
            viewHolder.timeTextView.setText(bean.getNewstime());
            viewHolder.plnumTextView.setText(bean.getPlnum());
            viewHolder.onclickTextView.setText(bean.getOnclick());

            if (holder instanceof OnePicViewHolder) {
                OnePicViewHolder onePicViewHolder = (OnePicViewHolder) holder;
                onePicViewHolder.imageView1.setImageURI(Uri.parse(bean.getTitlepic()));
            } else {
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
