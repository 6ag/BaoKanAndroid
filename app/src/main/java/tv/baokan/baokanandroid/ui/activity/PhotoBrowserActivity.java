package tv.baokan.baokanandroid.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.adapter.PhotoDetailViewPageAdapter;
import tv.baokan.baokanandroid.model.ArticleDetailBean;
import tv.baokan.baokanandroid.utils.ProgressHUD;

public class PhotoBrowserActivity extends BaseActivity {

    private static final String TAG = "PhotoBrowserActivity";

    private List<ArticleDetailBean.InsetPhotoBean> insetPhotoBeanList;
    private int index;
    private PhotoDetailViewPageAdapter adapter;
    private ViewPager mViewPager;            // 图片载体
    private TextView mPageTextView;          // 页码
    private TextView mCaptionTextView;       // 图片文字介绍
    private ImageView mSaveImageView;        // 保存

    /**
     * 便捷启动当前activity
     *
     * @param activity           来源activity
     * @param insetPhotoBeanList 插图集合
     */
    public static void start(Activity activity, List<ArticleDetailBean.InsetPhotoBean> insetPhotoBeanList, int index) {
        Intent intent = new Intent(activity, PhotoBrowserActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("insetPhotoBeanList_key", (Serializable) insetPhotoBeanList);
        intent.putExtras(bundle);
        intent.putExtra("index_key", index);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_browser);

        prepareUI();
        prepareData();

    }

    /**
     * 准备UI
     */
    private void prepareUI() {
        mViewPager = (ViewPager) findViewById(R.id.vp_photo_browser_viewPager);
        mPageTextView = (TextView) findViewById(R.id.tv_photo_browser_page);
        mCaptionTextView = (TextView) findViewById(R.id.tv_photo_browser_caption);
        mSaveImageView = (ImageView) findViewById(R.id.iv_photo_browser_save);

        mSaveImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressHUD.showInfo(mContext, "保存图片");
            }
        });
    }

    /**
     * 准备数据
     */
    private void prepareData() {

        Intent intent = getIntent();
        index = intent.getIntExtra("index_key", 0);
        insetPhotoBeanList = (List<ArticleDetailBean.InsetPhotoBean>) intent.getSerializableExtra("insetPhotoBeanList_key");

        // 组合图片url集合
        List<String> photoList = new ArrayList<>();
        for (ArticleDetailBean.InsetPhotoBean photoBean :
                insetPhotoBeanList) {
            photoList.add(photoBean.getUrl());
        }

        // 设置ViewPager
        adapter = new PhotoDetailViewPageAdapter(this, photoList);
        adapter.setOnPhotoTapListener(new PhotoDetailViewPageAdapter.OnPhotoTapListener() {
            @Override
            public void onPhotoTap() {
                // 隐藏/显示 顶部/底部视图
                onPhotoOneTapped();
            }
        });
        mViewPager.setAdapter(adapter);

        // 默认从第一页开始浏览
        mViewPager.setCurrentItem(index);
        onPageChanged(index);

        // 监听viewPager的滚动
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                onPageChanged(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    /**
     * 页码发生改变 更新页码指示器和图片描述文字
     *
     * @param position 页码
     */
    private void onPageChanged(int position) {
        index = position;
        mPageTextView.setText(position + 1 + "/" + insetPhotoBeanList.size());
        mCaptionTextView.setText(insetPhotoBeanList.get(position).getCaption());
    }

    /**
     * 图片单击手势 隐藏/展开 顶部、底部视图
     */
    private void onPhotoOneTapped() {
        finish();
    }

}
