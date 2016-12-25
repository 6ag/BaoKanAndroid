package tv.baokan.baokanandroid.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.utils.LogUtils;

public class NewsListFragment extends BaseFragment {

    public String classid; // 栏目id
    private RecyclerView mNewsListRecyclerView;
    private String[] datas;

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

        datas = new String[]{"OKHttp", "xUtils3", "Retrofit2", "Fresco", "Glide", "greenDao", "RxJava", "volley", "Gson", "FastJson", "picasso", "evenBus", "jcvideoplayer", "pulltorefresh", "Expandablelistview", "UniversalVideoView", "....."};
        mNewsListRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mNewsListRecyclerView.setAdapter(new NewsListAdapter());

    }

    private class NewsListAdapter extends RecyclerView.Adapter<NewsListAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent, false);
            final ViewHolder holder = new ViewHolder(view);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    Toast.makeText(mContext, datas[position], Toast.LENGTH_SHORT).show();
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.textView.setText(datas[position]);
        }

        @Override
        public int getItemCount() {
            return datas.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView textView;
            View itemView;

            ViewHolder(View itemView) {
                super(itemView);
                this.itemView = itemView;
                textView = (TextView) itemView.findViewById(android.R.id.text1);
            }
        }
    }

}
