package tv.baokan.baokanandroid.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.model.ArticleDetailBean;
import tv.baokan.baokanandroid.ui.activity.NewsDetailActivity;

/**
 * 新闻正文底部相关链接适配器
 */
public class LinkRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // 相关链接item类型枚举
    enum LINK_ITEM_TYPE {
        NO_TITLE_PIC, // 无图
        TITLE_PIC     // 有图
    }

    List<ArticleDetailBean.ArticleDetailLinkBean> linkBeanList;
    Context mContext;

    public LinkRecyclerViewAdapter(List<ArticleDetailBean.ArticleDetailLinkBean> linkBeanList, Context mContext) {
        this.linkBeanList = linkBeanList;
        this.mContext = mContext;
    }

    @Override
    public int getItemViewType(int position) {
        if (linkBeanList.get(position).getTitlepic() == null) {
            return LINK_ITEM_TYPE.NO_TITLE_PIC.ordinal();
        } else {
            return LINK_ITEM_TYPE.TITLE_PIC.ordinal();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final RecyclerView.ViewHolder holder;
        View view;
        if (viewType == LINK_ITEM_TYPE.NO_TITLE_PIC.ordinal()) {
            view = LayoutInflater.from(mContext).inflate(R.layout.cell_news_detail_link_notitlepic, parent, false);
            holder = new NoTitlePicViewHolder(view);
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.cell_news_detail_link_titlepic, parent, false);
            holder = new TitlePicViewHolder(view);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                // 进入相关链接的正文页面
                NewsDetailActivity.start((NewsDetailActivity) mContext, linkBeanList.get(position).getClassid(), linkBeanList.get(position).getId());
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ArticleDetailBean.ArticleDetailLinkBean linkBean = linkBeanList.get(position);
        LinkBaseViewHolder baseViewHolder = (LinkBaseViewHolder) holder;
        baseViewHolder.titleTextView.setText(linkBean.getTitle());
        baseViewHolder.classNameTextView.setText(linkBean.getClassname());
        baseViewHolder.onclickTextView.setText(linkBean.getOnclick());
        if (holder instanceof TitlePicViewHolder) {
            TitlePicViewHolder titlePicViewHolder = (TitlePicViewHolder) holder;
            titlePicViewHolder.titlePicView.setImageURI(linkBean.getTitlepic());
        }
        // 最后一个分割线隐藏
        if (position == linkBeanList.size() - 1) {
            baseViewHolder.lineView.setVisibility(View.INVISIBLE);
        } else {
            baseViewHolder.lineView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return linkBeanList.size();
    }

    // 相关链接item基类
    class LinkBaseViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        TextView classNameTextView;
        TextView onclickTextView;
        View lineView;
        View itemView;

        LinkBaseViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            titleTextView = (TextView) itemView.findViewById(R.id.tv_cell_news_detail_link_title);
            classNameTextView = (TextView) itemView.findViewById(R.id.tv_cell_news_detail_link_classname);
            onclickTextView = (TextView) itemView.findViewById(R.id.tv_cell_news_detail_link_onclick);
            lineView = itemView.findViewById(R.id.v_cell_news_detail_link_line);
        }
    }

    // 无图的item
    class NoTitlePicViewHolder extends LinkBaseViewHolder {

        NoTitlePicViewHolder(View itemView) {
            super(itemView);
        }
    }

    // 有图的item
    class TitlePicViewHolder extends LinkBaseViewHolder {

        SimpleDraweeView titlePicView;

        TitlePicViewHolder(View itemView) {
            super(itemView);
            titlePicView = (SimpleDraweeView) itemView.findViewById(R.id.sdv_cell_news_detail_link_pic);
        }
    }

}
