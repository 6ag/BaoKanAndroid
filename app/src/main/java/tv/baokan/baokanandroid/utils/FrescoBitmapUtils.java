package tv.baokan.baokanandroid.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;

import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FrescoBitmapUtils {
    /**
     * 默认的引用key
     */
    public static final String KEY_DEFAULT_REFERENCE = "fresco_bitmap_utils_default_reference_key";

    //线程池
    private Executor mExecutor = null;
    //引用存放的位置(可能需要的下载图片引用不只一个)
    private HashMap<String, CloseableReference<CloseableImage>> mRefMap = null;
    private Context mApplicationContext = null;
    //下载监听回调事件
    private OnFrescoBmpDownloadStateChangedListener mFrescoListener;

    /**
     * 设置可能需要使用的线程个数(根据需要下载的图片的频率及数量确定)
     *
     * @param context 上下文
     * @param threadCount 下载线程个数,用于线程池的初始化
     */
    public FrescoBitmapUtils(Context context, int threadCount) {
        this(context, threadCount, null);
    }

    /**
     * 设置初始化的数据
     *
     * @param context 上下文
     * @param threadCount 下载线程个数,用于线程池的初始化
     * @param listener    监听回调事件
     */
    public FrescoBitmapUtils(Context context, int threadCount, OnFrescoBmpDownloadStateChangedListener listener) {
        mExecutor = Executors.newFixedThreadPool(threadCount);
        mApplicationContext = context.getApplicationContext();
        mFrescoListener = listener;

        mRefMap = new HashMap<>();
        //将默认引用添加到map中
        mRefMap.put(KEY_DEFAULT_REFERENCE, null);
    }

    /**
     * 设置监听回调事件
     *
     * @param listener
     */
    public void setOnFrescoBitmapDownloadStateChangedListsener(OnFrescoBmpDownloadStateChangedListener listener) {
        mFrescoListener = listener;
    }

    /**
     * 使用默认的引用下载保存图片,使用此方法时,之前默认引用的图片将会被回收
     *
     * @param imageUrl 图片URL
     * @return
     */
    public boolean downloadBitmap(String imageUrl) {
        return downloadBitmap(imageUrl, KEY_DEFAULT_REFERENCE);
    }

    /**
     * 使用指定的key用于保存下载图片的引用,不建议创建大量的引用,会造成内存的问题
     *
     * @param imageUrl
     * @param key
     * @return
     */
    public boolean downloadBitmap(String imageUrl, final String key) {
        if (TextUtils.isEmpty(imageUrl)) {
            return false;
        }

        //创建请求
        ImageRequest request = ImageRequest.fromUri(imageUrl);
        ImagePipeline pipeline = ImagePipelineFactory.getInstance().getImagePipeline();
        DataSource<CloseableReference<CloseableImage>>
                dataSource = pipeline.fetchDecodedImage(request, mApplicationContext);
        DataSubscriber<CloseableReference<CloseableImage>> dataSubscriber =
                new BaseDataSubscriber<CloseableReference<CloseableImage>>() {
                    @Override
                    protected void onNewResultImpl(
                            DataSource<CloseableReference<CloseableImage>> dataSource) {
                        if (!dataSource.isFinished()) {
                            // if we are not interested in the intermediate images,
                            // we can just return here.
                            return;
                        }
                        //检测key是否有效
                        if (TextUtils.isEmpty(key)) {
                            //回调无效key方法
                            if (mFrescoListener != null) {
                                mFrescoListener.onEmptyKey(key);
                            }
                            return;
                        } else {
                            //创建保存下载图片的引用
                            // keep the closeable reference
                            CloseableReference<CloseableImage> reference = dataSource.getResult();
                            // do something with the result
                            CloseableImage image = reference.get();
                            //获取当前key的旧引用
                            CloseableReference oldRef = mRefMap.get(key);
                            //回收旧引用(同时会回调回收事件)
                            releaseCloseReference(oldRef, false);
                            //将新引用添加到map中
                            mRefMap.put(key, reference);

                            //获取bitmap
                            if (image instanceof CloseableBitmap) {
                                // do something with the bitmap
                                Bitmap bitmap = ((CloseableBitmap) image).getUnderlyingBitmap();
                                //回调使用bitmap
                                if (mFrescoListener != null) {
                                    mFrescoListener.onBitmapDownloadSuccess(bitmap, key);
                                }
                            }
                        }
                    }

                    @Override
                    protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                        //回调下载失败方法
                        if (mFrescoListener != null) {
                            mFrescoListener.onBitmapDownloadFail();
                        }
                    }
                };
        //启动下载
        dataSource.subscribe(dataSubscriber, mExecutor);
        return true;
    }

    /**
     * 释放引用
     *
     * @param reference    需要被释放的引用
     * @param isReleaseAll 当前释放请求是否在释放全部引用时调用
     */
    public void releaseCloseReference(CloseableReference reference, boolean isReleaseAll) {
        if (mFrescoListener != null && !isReleaseAll) {
            //独自通知功能只在单独释放某个引用时才调用
            mFrescoListener.onReferenceRelease(reference);
        }
        CloseableReference.closeSafely(reference);
        reference = null;
    }

    /**
     * 释放全部引用
     */
    public void releaseAllReference() {
        //回调释放所有引用的方法
        if (mFrescoListener != null) {
            mFrescoListener.onRelaseAllReference();
        }
        for (CloseableReference reference : mRefMap.values()) {
            //释放全部引用不调用
            releaseCloseReference(reference, true);
        }
    }

    /**
     * fresco下载bitmap的状态回调监听事件
     */
    public interface OnFrescoBmpDownloadStateChangedListener {
        /**
         * 每当引用被回收时被调用
         *
         * @param reference
         */
        public void onReferenceRelease(CloseableReference<CloseableImage> reference);

        /**
         * 图片下载成功时回调
         *
         * @param bitmap 下载成功的图片
         * @param key    该图片对应引用的key
         */
        public void onBitmapDownloadSuccess(Bitmap bitmap, String key);

        /**
         * 图片下载失败回调
         */
        public void onBitmapDownloadFail();

        /**
         * 释放所有引用时的回调
         */
        public void onRelaseAllReference();

        /**
         * 指定存放引用的key无效时回调
         *
         * @param key
         */
        public void onEmptyKey(String key);
    }
}
