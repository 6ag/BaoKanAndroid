package tv.baokan.baokanandroid.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.adapter.PhotoDetailViewPageAdapter;
import tv.baokan.baokanandroid.model.ArticleDetailBean;
import tv.baokan.baokanandroid.utils.ProgressHUD;
import tv.baokan.baokanandroid.utils.StreamUtils;

public class PhotoBrowserActivity extends BaseActivity {

    private static final String TAG = "PhotoBrowserActivity";
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0; // 写入SD卡权限

    private List<ArticleDetailBean.InsetPhotoBean> insetPhotoBeanList;
    private int mIndex;
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
                // 保存图片
                saveCurrentImage();
            }
        });
    }

    /**
     * 准备数据
     */
    private void prepareData() {

        Intent intent = getIntent();
        mIndex = intent.getIntExtra("index_key", 0);
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

        mViewPager.setCurrentItem(mIndex);
        onPageChanged(mIndex);

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
        mIndex = position;
        mPageTextView.setText(position + 1 + "/" + insetPhotoBeanList.size());
        mCaptionTextView.setText(insetPhotoBeanList.get(position).getCaption());
    }

    /**
     * 图片单击手势 返回资讯内容页
     */
    private void onPhotoOneTapped() {
        finish();
    }

    /**
     * 判断有没有保存权限，有则保存，没有则申请权限
     */
    private void saveCurrentImage() {
        // 判断是否有写入SD权限
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(mContext,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            // 保存图片到相册中
            StreamUtils.saveImageToAlbum(mContext, insetPhotoBeanList.get(mIndex).getUrl());
        }
    }

    /**
     * 运行时权限请求回调结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 保存图片到相册中
                    StreamUtils.saveImageToAlbum(mContext, insetPhotoBeanList.get(mIndex).getUrl());
                } else {
                    Toast.makeText(getApplicationContext(), "你没有文件写入权限", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

}
