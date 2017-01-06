package tv.baokan.baokanandroid.utils;

import android.content.Context;
import android.graphics.Bitmap;

import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.SimpleCacheKey;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.image.CloseableImage;

import java.io.File;
import java.math.BigDecimal;
import java.util.concurrent.Executor;

public class FileCacheUtils {

    private static final String TAG = "FileCacheUtils";

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
                    // 防止获取磁盘文件失败
                    if (TextUtils.isEmpty(filePath)) {
                        cacheInDiskListener.checkCacheInDisk(false, filePath);
                        return;
                    }
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
    private static String getDiskCacheFilePath(String url) {
        FileBinaryResource resource = (FileBinaryResource) Fresco.getImagePipelineFactory()
                .getMainFileCache()
                .getResource(new SimpleCacheKey(url));
        // 防止中途清除磁盘缓存 导致获取不到
        if (resource == null) {
            return "";
        }
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

    /**
     * 获取缓存总大小
     *
     * @param context 上下文
     * @return 格式化后的缓存大小
     */
    public static String getTotalCacheSize(Context context) {
        long cacheSize = 0;
        try {
            cacheSize = getFolderSize(context.getCacheDir());
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                cacheSize += getFolderSize(context.getExternalCacheDir());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getFormatSize(cacheSize);
    }

    /**
     * 清理所有缓存
     *
     * @param context 上下文
     */
    public static void clearAllCache(Context context) {
        deleteDir(context.getCacheDir());
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            deleteDir(context.getExternalCacheDir());
        }
    }

    /**
     * 删除目录
     *
     * @param dir 目录
     * @return 是否删除成功
     */
    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
        }
        return dir != null && dir.delete();
    }

    // 获取文件
    //Context.getExternalFilesDir() --> SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据
    //Context.getExternalCacheDir() --> SDCard/Android/data/你的应用包名/cache/目录，一般存放临时缓存数据
    private static long getFolderSize(File file) throws Exception {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (File aFileList : fileList) {
                // 如果下面还有文件
                if (aFileList.isDirectory()) {
                    size = size + getFolderSize(aFileList);
                } else {
                    size = size + aFileList.length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 格式化单位
     *
     * @param size
     * @return
     */
    private static String getFormatSize(double size) {
        double kiloByte = size / 1024L;
        if (kiloByte < 1) {
            return "0KB";
        }

        double megaByte = kiloByte / 1024L;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024L;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024L;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
                + "TB";
    }

}
