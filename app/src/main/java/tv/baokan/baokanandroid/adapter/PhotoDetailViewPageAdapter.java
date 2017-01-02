package tv.baokan.baokanandroid.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.utils.LogUtils;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * 图片详情图片浏览器适配器
 */

public class PhotoDetailViewPageAdapter extends PagerAdapter {

    public static interface OnPhotoTapListener {
        public abstract void onPhotoTap();
    }

    private Context context;
    List<String> photoList;
    private SparseArray<View> cacheView; // 缓存展示图片的View
    private OnPhotoTapListener photoTapListener;

    public void setOnPhotoTapListener(OnPhotoTapListener photoTapListener) {
        this.photoTapListener = photoTapListener;
    }

    public PhotoDetailViewPageAdapter(Context context, List<String> photoList) {
        this.context = context;
        this.photoList = photoList;
        cacheView = new SparseArray<>(photoList.size());
    }

    @Override
    public Object instantiateItem(final ViewGroup container, int position) {
        View view = cacheView.get(position);
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.image_photo_detail, container, false);
            final ImageView imageView = (ImageView) view.findViewById(R.id.iv_photo_detail_item_imageview);
            final PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(imageView);
            photoViewAttacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {

                // 单点图片内区域
                @Override
                public void onPhotoTap(View view, float x, float y) {
                    if (photoTapListener != null) {
                        photoTapListener.onPhotoTap();
                    }
                }

                // 单点图片外区域
                @Override
                public void onOutsidePhotoTap() {
                    if (photoTapListener != null) {
                        photoTapListener.onPhotoTap();
                    }
                }
            });

            // 使用Picasso RGB_565高效加载图片
            Picasso.with(context).load(photoList.get(position)).config(Bitmap.Config.RGB_565).into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    // 加载成功需要更新一下 否则可能错位
                    photoViewAttacher.update();
                }

                @Override
                public void onError() {
                    LogUtils.d("ViewPageAdapter", "图片加载失败");
                }
            });
            cacheView.put(position, view);
        }
        container.addView(view);
        return view;
    }

    @Override
    public int getCount() {
        return photoList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
