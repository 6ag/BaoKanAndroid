package tv.baokan.baokanandroid.utils;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.SimpleCacheKey;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;

import java.io.File;
import java.util.concurrent.Executor;

public class ImageCacheUtils {

    private static final String TAG = "ImageCacheUtils";

    public interface OnCheckCacheInDiskListener {
        /**
         * 检查文件是否缓存到磁盘，如果有则回调文件路径
         *
         * @param isExist  是否存在
         * @param filePath 文件路径，没有则为null
         */
        public abstract void checkCacheInDisk(boolean isExist, String filePath);
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


}
