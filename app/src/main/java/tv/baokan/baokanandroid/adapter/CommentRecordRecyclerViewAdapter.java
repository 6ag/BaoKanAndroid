package tv.baokan.baokanandroid.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tv.baokan.baokanandroid.model.CollectionRecordBean;
import tv.baokan.baokanandroid.model.CommentRecordBean;

/**
 * 收藏记录
 */
public class CommentRecordRecyclerViewAdapter extends RecyclerView.Adapter<CommentRecordRecyclerViewAdapter.ViewHolder> {

    public static interface OnItemTapListener {
        public abstract void onItemTapListener(CommentRecordBean commentRecordBean);
    }

    private List<CommentRecordBean> mCommentRecordBeanList = new ArrayList<>();
    private Context mContext;
    private OnItemTapListener mOnItemTapListener;

    /**
     * 设置item点击监听器
     *
     * @param onItemTapListener 监听器
     */
    public void setOnItemTapListener(OnItemTapListener onItemTapListener) {
        this.mOnItemTapListener = onItemTapListener;
    }

    /**
     * 刷新数据
     *
     * @param newCommentRecordBeanList 新的数据集合
     * @param method                   0下拉 1上拉
     */
    public void updateData(List<CommentRecordBean> newCommentRecordBeanList, int method) {
        String maxId = "0";
        String minId = "0";
        if (mCommentRecordBeanList.size() > 0) {
            maxId = mCommentRecordBeanList.get(0).getId();
            minId = mCommentRecordBeanList.get(mCommentRecordBeanList.size() - 1).getId();
        }

        if (method == 0) { // 下拉刷新

            if (maxId.compareTo(newCommentRecordBeanList.get(0).getId()) <= -1) {
                // 替换数据
                mCommentRecordBeanList.clear();
                mCommentRecordBeanList.addAll(newCommentRecordBeanList);
                // 刷新列表数据
                notifyDataSetChanged();
            }

        } else { // 上拉加载

            if (minId.compareTo(newCommentRecordBeanList.get(0).getId()) >= 1) {
                // 拼接数据
                mCommentRecordBeanList.addAll(newCommentRecordBeanList);
                // 刷新列表数据
                notifyDataSetChanged();
            }
        }
    }

    public CommentRecordRecyclerViewAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemTapListener != null) {
                    mOnItemTapListener.onItemTapListener(mCommentRecordBeanList.get(holder.getAdapterPosition()));
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CommentRecordBean commentRecordBean = mCommentRecordBeanList.get(position);
        holder.titleTextView.setText(commentRecordBean.getTitle());
    }

    @Override
    public int getItemCount() {
        return mCommentRecordBeanList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;

        ViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }

}
