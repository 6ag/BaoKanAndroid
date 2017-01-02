package tv.baokan.baokanandroid.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.model.CommentBean;

/**
 * 评论适配器
 */
public class CommentRecyclerViewAdapter extends RecyclerView.Adapter<CommentRecyclerViewAdapter.ViewHolder> {

    List<CommentBean> commentBeanList;
    Context mContext;

    public CommentRecyclerViewAdapter(List<CommentBean> commentBeanList, Context mContext) {
        this.commentBeanList = commentBeanList;
        this.mContext = mContext;
    }

    @Override
    public CommentRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.cell_news_detail_comment, parent, false);
        CommentRecyclerViewAdapter.ViewHolder holder = new CommentRecyclerViewAdapter.ViewHolder(view);
        holder.starLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "点赞", Toast.LENGTH_SHORT).show();
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
        if (position == commentBeanList.size() - 1) {
            holder.lineView.setVisibility(View.INVISIBLE);
        } else {
            holder.lineView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return commentBeanList.size();
    }

    // 相关链接item基类
    class ViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView portraitView;// 头像
        TextView nicknameTextView;    // 昵称
        TextView timeTextView;        // 时间
        TextView starNumTextView;     // 点赞数
        TextView commentNumTextView;  // 楼层
        TextView commentContentTextView; // 评论内容
        LinearLayout starLayout;      // 赞
        View lineView;                // 分割线

        ViewHolder(View itemView) {
            super(itemView);
            portraitView = (SimpleDraweeView) itemView.findViewById(R.id.sdv_cell_news_detail_comment_portrait);
            nicknameTextView = (TextView) itemView.findViewById(R.id.tv_cell_news_detail_comment_name);
            timeTextView = (TextView) itemView.findViewById(R.id.tv_cell_news_detail_comment_time);
            starNumTextView = (TextView) itemView.findViewById(R.id.tv_cell_news_detail_star_num);
            commentNumTextView = (TextView) itemView.findViewById(R.id.tv_cell_news_detail_comment_num);
            commentContentTextView = (TextView) itemView.findViewById(R.id.tv_cell_news_detail_comment_content);
            lineView = itemView.findViewById(R.id.v_cell_news_detail_comment_line);
            starLayout = (LinearLayout) itemView.findViewById(R.id.ll_news_detail_comment_star);
        }
    }

}
