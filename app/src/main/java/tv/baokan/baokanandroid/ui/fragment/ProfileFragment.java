package tv.baokan.baokanandroid.ui.fragment;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.ui.activity.LoginActivity;
import tv.baokan.baokanandroid.widget.NavigationViewRed;

public class ProfileFragment extends BaseFragment implements View.OnClickListener {

    private View portraitView;                  // 头像（包括头像和昵称）
    private SimpleDraweeView portraitImageView; // 头像
    private TextView nicknameTextView;          // 昵称
    private View collectionView;                // 收藏
    private View commentView;                   // 评论
    private View infoView;                      // 资料
    private View clearCacheView;                // 清理缓存
    private TextView cacheTextView;             // 缓存
    private View setFontView;                   // 设置字体
    private TextView fontTextView;              // 字体
    private View feedbackView;                  // 意见反馈
    private View aboutView;                     // 关于我们
    private View commendView;                   // 推荐给好友
    private View versionView;                   // 当前版本
    private TextView versionTextView;           // 版本号

    @Override
    protected View prepareUI() {
        // 加载各种控件
        View view = View.inflate(mContext, R.layout.fragment_profile, null);
        portraitView = view.findViewById(R.id.ll_profile_portrait_layout);
        portraitImageView = (SimpleDraweeView) view.findViewById(R.id.sdv_profile_portrait);
        nicknameTextView = (TextView) view.findViewById(R.id.tv_profile_nickname);
        collectionView = view.findViewById(R.id.ll_profile_collection_layout);
        commentView = view.findViewById(R.id.ll_profile_comment_layout);
        infoView = view.findViewById(R.id.ll_profile_info_layout);
        clearCacheView = view.findViewById(R.id.rl_profile_clear_cache_layout);
        cacheTextView = (TextView) view.findViewById(R.id.tv_profile_cache);
        setFontView = view.findViewById(R.id.rl_profile_set_font_layout);
        fontTextView = (TextView) view.findViewById(R.id.tv_profile_font);
        feedbackView = view.findViewById(R.id.rl_profile_feekback_layout);
        aboutView = view.findViewById(R.id.rl_profile_aboutme_layout);
        commendView = view.findViewById(R.id.rl_profile_commend_layout);
        versionView = view.findViewById(R.id.rl_profile_version_layout);
        versionTextView = (TextView) view.findViewById(R.id.tv_profile_current_version);

        // 添加点击事件
        portraitView.setOnClickListener(this);
        collectionView.setOnClickListener(this);
        commentView.setOnClickListener(this);
        infoView.setOnClickListener(this);
        clearCacheView.setOnClickListener(this);
        setFontView.setOnClickListener(this);
        feedbackView.setOnClickListener(this);
        aboutView.setOnClickListener(this);
        commendView.setOnClickListener(this);
        versionView.setOnClickListener(this);
        return view;
    }

    @Override
    protected void loadData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_profile_portrait_layout:
                LoginActivity.start(getActivity());
                break;
            case R.id.ll_profile_collection_layout:

                break;
            case R.id.ll_profile_comment_layout:

                break;
            case R.id.ll_profile_info_layout:

                break;
            case R.id.rl_profile_clear_cache_layout:

                break;
            case R.id.rl_profile_set_font_layout:

                break;
            case R.id.rl_profile_feekback_layout:

                break;
            case R.id.rl_profile_aboutme_layout:

                break;
            case R.id.rl_profile_commend_layout:

                break;
            case R.id.rl_profile_version_layout:

                break;
        }
    }

}
