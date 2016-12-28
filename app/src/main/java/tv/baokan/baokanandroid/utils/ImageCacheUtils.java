package tv.baokan.baokanandroid.utils;

import android.content.Context;
import android.graphics.Bitmap;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.SimpleCacheKey;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.image.CloseableImage;

import java.io.File;
import java.util.concurrent.Executor;

public class ImageCacheUtils {

    private static final String TAG = "ImageCacheUtils";

    // 检查文件缓存监听接口
    public interface OnCheckCacheInDiskListener {
        /**
         * 检查文件是否缓存到磁盘，如果有则回调文件路径
         *
         * @param isExist  是否存在
         * @param filePath 文件路径，没有则为null
         */
        public abstract void checkCacheInDisk(boolean isExist, String filePath);
    }

    // 下载图片到磁盘监听接口
    public interface OnDownloadImageToDiskListener {
        /**
         * 下载图片完成后回调
         *
         * @param success  是否下载成功
         * @param filePath 文件路径，没有则为null
         */
        public abstract void downloadFinished(boolean success, String filePath);
    }

    /**
     * 检查是否有缓存，子主线程操作
     *
     * @param url 文件url
     */
    public static void checkCacheInDisk(final String url, final OnCheckCacheInDiskListener cacheInDiskListener) {

        final DataSource<Boolean> dataSource = Fresco.getImagePipeline().isInDiskCache(Uri.parse(url));
        DataSubscriber<Boolean> subscriber = new BaseDataSubscriber<Boolean>() {
            @Override
            protected void onNewResultImpl(final DataSource<Boolean> dataSource) {
                LogUtils.d(TAG, "检查文件是否存在成功");
            }

            @Override
            protected void onFailureImpl(DataSource<Boolean> dataSource) {
                LogUtils.d(TAG, "检查文件是否存在失败");
            }
        };
        dataSource.subscribe(subscriber, new Executor() {
            @Override
            public void execute(@NonNull Runnable command) {
                String filePath = null;
                if (dataSource.getResult()) {
                    filePath = getDiskCacheFilePath(url);
                }
                cacheInDiskListener.checkCacheInDisk(dataSource.getResult(), filePath);
            }
        });

    }

    /**
     * 获取磁盘缓存的文件路径
     *
     * @param url 文件url
     * @return 文件路径
     */
    public static String getDiskCacheFilePath(String url) {
        FileBinaryResource resource = (FileBinaryResource) Fresco.getImagePipelineFactory()
                .getMainFileCache()
                .getResource(new SimpleCacheKey(url));
        File file = resource.getFile();
        return file.getAbsolutePath();
    }

    /**
     * 下载图片到磁盘 并回调下载结果
     *
     * @param context                     上下文
     * @param url                         文件url
     * @param downloadImageToDiskListener 回调接口
     */
    public static void downloadImage(Context context, final String url, final OnDownloadImageToDiskListener downloadImageToDiskListener) {
        LogUtils.d(TAG, "start download image, url =  " + url);

        FrescoBitmapUtils frescoBitmapUtils = new FrescoBitmapUtils(context, 1, new FrescoBitmapUtils.OnFrescoBmpDownloadStateChangedListener() {
            @Override
            public void onReferenceRelease(CloseableReference<CloseableImage> reference) {
                LogUtils.d(TAG, "onReferenceRelease, url =  " + url);
            }

            @Override
            public void onBitmapDownloadSuccess(Bitmap bitmap, String key) {
                LogUtils.d(TAG, "onBitmapDownloadSuccess, url =  " + url);

                // 下载完成后为了防止意外，再去检查一下文件是否存在
                checkCacheInDisk(url, new OnCheckCacheInDiskListener() {
                    @Override
                    public void checkCacheInDisk(boolean isExist, String filePath) {
                        downloadImageToDiskListener.downloadFinished(isExist, filePath);
                    }
                });

            }

            @Override
            public void onBitmapDownloadFail() {
                LogUtils.d(TAG, "onBitmapDownloadFail, url =  " + url);
            }

            @Override
            public void onRelaseAllReference() {
                LogUtils.d(TAG, "onRelaseAllReference, url =  " + url);
            }

            @Override
            public void onEmptyKey(String key) {
                LogUtils.d(TAG, "onEmptyKey, url =  " + url);
            }
        });

        // 下载图片
        frescoBitmapUtils.downloadBitmap(url);

    }

}
