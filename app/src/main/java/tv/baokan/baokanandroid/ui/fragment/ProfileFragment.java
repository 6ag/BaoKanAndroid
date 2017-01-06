package tv.baokan.baokanandroid.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.kaopiz.kprogresshud.KProgressHUD;

import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.app.BaoKanApp;
import tv.baokan.baokanandroid.model.UserBean;
import tv.baokan.baokanandroid.ui.activity.AboutUsActivity;
import tv.baokan.baokanandroid.ui.activity.FeedbackActivity;
import tv.baokan.baokanandroid.ui.activity.LoginActivity;
import tv.baokan.baokanandroid.ui.activity.UserInfoActivity;
import tv.baokan.baokanandroid.utils.FileCacheUtils;
import tv.baokan.baokanandroid.utils.ProgressHUD;
import tv.baokan.baokanandroid.utils.SharedPreferencesUtils;

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
    private TextView versionTextView;           // 版本号
    private AlertDialog mClearCacheDiglog;      // 清除缓存
    private AlertDialog mSetFontDialog;         // 设置字体
    private int mFontSize;                       // 字体大小

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
        return view;
    }

    @Override
    protected void loadData() {

        // 字体
        fontTextView.setText(getFontSizeString());

        // 版本号
        versionTextView.setText("v" + ((BaoKanApp) getActivity().getApplication()).getVersionName());

    }

    @Override
    public void onStart() {
        super.onStart();

        // 更新头像
        if (UserBean.isLogin()) {
            portraitImageView.setImageURI(UserBean.shared().getAvatarUrl());
            nicknameTextView.setText(UserBean.shared().getNickname());
        } else {
            portraitImageView.setImageURI("");
            nicknameTextView.setText("登录账号");
        }

        // 更新缓存 - 如果缓存太大，计算缓存大小应该放到子线程。然后计算完在主线程更新UI
        cacheTextView.setText(FileCacheUtils.getTotalCacheSize(mContext));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_profile_portrait_layout:
                if (UserBean.isLogin()) {
                    Toast.makeText(mContext, "修改头像", Toast.LENGTH_SHORT).show();
                } else {
                    LoginActivity.start(getActivity());
                }
                break;
            case R.id.ll_profile_collection_layout:
                if (UserBean.isLogin()) {
                    Toast.makeText(mContext, "收藏", Toast.LENGTH_SHORT).show();
                } else {
                    LoginActivity.start(getActivity());
                }
                break;
            case R.id.ll_profile_comment_layout:
                if (UserBean.isLogin()) {
                    Toast.makeText(mContext, "评论", Toast.LENGTH_SHORT).show();
                } else {
                    LoginActivity.start(getActivity());
                }
                break;
            case R.id.ll_profile_info_layout:
                if (UserBean.isLogin()) {
                    UserInfoActivity.start(getActivity());
                } else {
                    LoginActivity.start(getActivity());
                }
                break;
            case R.id.rl_profile_clear_cache_layout:
                showClearCacheDialog();
                break;
            case R.id.rl_profile_set_font_layout:
                showSetFontDialog();
                break;
            case R.id.rl_profile_feekback_layout:
                FeedbackActivity.start(getActivity());
                break;
            case R.id.rl_profile_aboutme_layout:
                AboutUsActivity.start(getActivity());
                break;
            case R.id.rl_profile_commend_layout:

                break;
        }
    }

    /**
     * 清除缓存前需要询问一下用户
     */
    private void showClearCacheDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(true);
        builder.setTitle("您确定要清除缓存吗？");
        builder.setMessage("缓存可以节省您的流量哦！");
        builder.setPositiveButton("确定清除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mClearCacheDiglog.dismiss();
                clearCache();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mClearCacheDiglog.dismiss();
            }
        });
        mClearCacheDiglog = builder.create();
        mClearCacheDiglog.show();

    }

    /**
     * 清除缓存
     */
    private void clearCache() {
        // 清理新闻json数据 - 不清理json数据
//        NewsDALManager.shared.clearCache();

        // Fresco清除图片缓存
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        imagePipeline.clearCaches();

        // 清除缓存目录 - 清除所有缓存目录文件
        FileCacheUtils.clearAllCache(mContext);

        final KProgressHUD hud = ProgressHUD.show(mContext, "正在清理...");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                hud.dismiss();
                ProgressHUD.showInfo(mContext, "清理缓存完成");
                cacheTextView.setText(FileCacheUtils.getTotalCacheSize(mContext));
            }
        }, 2000);

    }

    /**
     * 弹出显示选择字体的会话框
     */
    private void showSetFontDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View view = View.inflate(mContext, R.layout.dialog_set_font, null);
        builder.setView(view);
        builder.setCancelable(true);
        mSetFontDialog = builder.create();
        mSetFontDialog.show();

        RadioGroup setFontGroup = (RadioGroup) view.findViewById(R.id.rg_set_font_group);
        // 根据缓存的字体选择默认的item
        switch (SharedPreferencesUtils.getInt(mContext, SharedPreferencesUtils.DETAIL_FONT, 18)) {
            case 16:
                setFontGroup.check(R.id.rb_set_font_small);
                break;
            case 18:
                setFontGroup.check(R.id.rb_set_font_middle);
                break;
            case 20:
                setFontGroup.check(R.id.rb_set_font_big);
                break;
            case 22:
                setFontGroup.check(R.id.rb_set_font_verybig);
                break;
        }
        setFontGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 不会直接修改，而是先存储起来，点击确认的时候才修改
                switch (checkedId) {
                    case R.id.rb_set_font_verybig:
                        mFontSize = 22;
                        break;
                    case R.id.rb_set_font_big:
                        mFontSize = 20;
                        break;
                    case R.id.rb_set_font_middle:
                        mFontSize = 18;
                        break;
                    case R.id.rb_set_font_small:
                        mFontSize = 16;
                        break;
                }
            }
        });
        Button cancelButton = (Button) view.findViewById(R.id.btn_set_font_cancel);
        Button confirmButton = (Button) view.findViewById(R.id.btn_set_font_confirm);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSetFontDialog.dismiss();
            }
        });
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 修改字体
                SharedPreferencesUtils.setInt(mContext, SharedPreferencesUtils.DETAIL_FONT, mFontSize);
                fontTextView.setText(getFontSizeString());
                mSetFontDialog.dismiss();
            }
        });
    }

    /**
     * 更新显示字体大小的ui
     */
    private String getFontSizeString() {
        String fontSizeString = "";
        switch (SharedPreferencesUtils.getInt(mContext, SharedPreferencesUtils.DETAIL_FONT, 18)) {
            case 16:
                fontSizeString = "小号字体";
                break;
            case 18:
                fontSizeString = "中号字体";
                break;
            case 20:
                fontSizeString = "大号字体";
                break;
            case 22:
                fontSizeString = "特大号字体";
                break;
        }
        return fontSizeString;
    }

}
