package tv.baokan.baokanandroid.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.model.ArticleListBean;

/**
 * 图库列表数据适配器
 */

public class PhotoListRecyclerViewAdapter extends RecyclerView.Adapter<PhotoListRecyclerViewAdapter.ViewHolder> {

    public static interface OnItemTapListener {
        public abstract void onItemTapListener(ArticleListBean articleListBean);
    }

    private List<ArticleListBean> mArticleListBeans = new ArrayList<>(); // 列表数据
    private Context mContext;
    private OnItemTapListener mOnItemTapListener;

    /**
     * 更新数据
     *
     * @param newArticleListBeans 新数据
     * @param method              0下拉刷新 1上拉加载更多
     */
    public void updateData(List<ArticleListBean> newArticleListBeans, int method) {

        String maxId = "0";
        String minId = "0";
        if (mArticleListBeans.size() > 0) {
            maxId = mArticleListBeans.get(0).getId();
            minId = mArticleListBeans.get(mArticleListBeans.size() - 1).getId();
        }

        if (method == 0) { // 下拉刷新

            if (maxId.compareTo(newArticleListBeans.get(0).getId()) <= -1) {
                // 替换数据
                mArticleListBeans = newArticleListBeans;
                // 刷新列表数据
                notifyDataSetChanged();
            }

        } else { // 上拉加载

            if (minId.compareTo(newArticleListBeans.get(0).getId()) >= 1) {
                // 拼接数据
                mArticleListBeans.addAll(newArticleListBeans);
                // 刷新列表数据
                notifyDataSetChanged();
            }
        }
    }

    /**
     * 设置item点击监听器
     *
     * @param onItemTapListener 监听器
     */
    public void setOnItemTapListener(OnItemTapListener onItemTapListener) {
        this.mOnItemTapListener = onItemTapListener;
    }

    public PhotoListRecyclerViewAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public int getItemCount() {
        return mArticleListBeans.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.cell_photo_list, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemTapListener != null) {
                    mOnItemTapListener.onItemTapListener(mArticleListBeans.get(holder.getAdapterPosition()));
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ArticleListBean bean = mArticleListBeans.get(position);
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
