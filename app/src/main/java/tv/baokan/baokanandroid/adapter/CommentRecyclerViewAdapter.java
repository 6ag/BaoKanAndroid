package tv.baokan.baokanandroid.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.model.CommentBean;

/**
 * 评论适配器
 */
public class CommentRecyclerViewAdapter extends RecyclerView.Adapter<CommentRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "CommentRecyclerViewAdap";

    public static interface OnCommentTapListener {
        // 点赞
        public abstract void onStarTap(CommentBean commentBean, int position);
    }

    private List<CommentBean> commentBeanList = new ArrayList<>();
    private Context mContext;
    private OnCommentTapListener commentTapListener;

    public CommentRecyclerViewAdapter(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * 设置评论点击监听器
     *
     * @param commentTapListener 监听器
     */
    public void setOnCommentTapListener(OnCommentTapListener commentTapListener) {
        this.commentTapListener = commentTapListener;
    }

    /**
     * 更新数据
     *
     * @param newCommentBeanList 新数据
     * @param method             0下拉刷新 1上拉加载更多
     */
    public void updateData(List<CommentBean> newCommentBeanList, int method) {

        String maxId = "0";
        String minId = "0";
        if (commentBeanList != null && commentBeanList.size() > 0) {
            maxId = commentBeanList.get(0).getPlid();
            minId = commentBeanList.get(commentBeanList.size() - 1).getPlid();
        }

        if (method == 0) { // 下拉刷新
            if (maxId.compareTo(newCommentBeanList.get(0).getPlid()) <= -1) {
                // 替换数据
                commentBeanList.clear();
                commentBeanList.addAll(newCommentBeanList);

                // 刷新列表数据
                notifyDataSetChanged();
            }

        } else { // 上拉加载

            if (minId.compareTo(newCommentBeanList.get(0).getPlid()) >= 1) {
                // 拼接数据
                commentBeanList.addAll(newCommentBeanList);
                // 刷新列表数据
                notifyDataSetChanged();
            }
        }
    }

    @Override
    public int getItemCount() {
        return commentBeanList.size();
    }

    @Override
    public CommentRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.cell_news_detail_comment, parent, false);
        final CommentRecyclerViewAdapter.ViewHolder holder = new CommentRecyclerViewAdapter.ViewHolder(view);
        holder.starLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (commentTapListener != null) {
                    commentTapListener.onStarTap(commentBeanList.get(holder.getAdapterPosition()), holder.getAdapterPosition());
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(CommentRecyclerViewAdapter.ViewHolder holder, int position) {
        CommentBean commentBean = commentBeanList.get(position);
        holder.portraitView.setImageURI(commentBean.getUserpic());
        holder.nicknameTextView.setText(commentBean.getPlnickname());
        holder.commentContentTextView.setText(commentBean.getSaytext());
        holder.timeTextView.setText(commentBean.getSaytime());
        holder.starNumTextView.setText(commentBean.getZcnum());
        holder.commentNumTextView.setText(commentBean.getPlstep());
        // 最后一个分割线隐藏
        if (position == commentBeanList.size() - 1 && commentBeanList.size() < 10) {
            holder.lineView.setVisibility(View.INVISIBLE);
        } else {
            holder.lineView.setVisibility(View.VISIBLE);
        }
        if (commentBean.isStar()) {
            holder.starImageView.setImageResource(R.drawable.comment_support_highlighted);
            holder.starNumTextView.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
        } else {
            holder.starImageView.setImageResource(R.drawable.comment_support);
            holder.starNumTextView.setTextColor(mContext.getResources().getColor(R.color.colorCommentGray));
        }

    }

    // 相关链接item基类
    class ViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView portraitView;// 头像
        TextView nicknameTextView;    // 昵称
        TextView timeTextView;        // 时间
        TextView starNumTextView;     // 点赞数
        ImageView starImageView;      // 点赞图标
        TextView commentNumTextView;  // 楼层
        TextView commentContentTextView; // 评论内容
        LinearLayout starLayout;      // 赞
        View lineView;                // 分割线

        ViewHolder(View itemView) {
            super(itemView);
            portraitView = (SimpleDraweeView) itemView.findViewById(R.id.sdv_cell_news_detail_comment_portrait);
            nicknameTextView = (TextView) itemView.findViewById(R.id.tv_cell_news_detail_comment_name);
            timeTextView = (TextView) itemView.findViewById(R.id.tv_cell_news_detail_comment_time);
            starNumTextView = (TextView) itemView.findViewById(R.id.tv_cell_news_detail_comment_star_num);
            starImageView = (ImageView) itemView.findViewById(R.id.iv_cell_news_comment_star_image);
            commentNumTextView = (TextView) itemView.findViewById(R.id.tv_cell_news_detail_comment_num);
            commentContentTextView = (TextView) itemView.findViewById(R.id.tv_cell_news_detail_comment_content);
            lineView = itemView.findViewById(R.id.v_cell_news_detail_comment_line);
            starLayout = (LinearLayout) itemView.findViewById(R.id.ll_news_detail_comment_star);
        }
    }

}
