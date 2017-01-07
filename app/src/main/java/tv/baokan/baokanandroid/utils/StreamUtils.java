package tv.baokan.baokanandroid.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import tv.baokan.baokanandroid.app.BaoKanApp;

public class StreamUtils {

    private static final String TAG = "StreamUtils";

    /**
     * inputStrean转String
     *
     * @param in     InputStream
     * @param encode 编码
     * @return 转换后的字符串
     */
    public static String InputStreanToString(InputStream in, String encode) {

        String str = "";
        try {
            if (encode == null || encode.equals("")) {
                // 默认以utf-8形式
                encode = "utf-8";
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, encode));
            StringBuffer sb = new StringBuffer();

            while ((str = reader.readLine()) != null) {
                sb.append(str).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return str;
    }

    /**
     * 读取Assets下的文本文件
     *
     * @param context  上下文
     * @param fileName 文件名
     * @return 读取到的字符串
     */
    public static String readAssetsFile(Context context, String fileName) {

        StringBuilder stringBuffer = new StringBuilder();
        AssetManager assetManager = context.getAssets();
        try {
            InputStream is = assetManager.open(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String str = null;
            while ((str = br.readLine()) != null) {
                stringBuffer.append(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }

    /**
     * 写入字符串到手机存储卡内
     *
     * @param fileName 文件名
     * @param string   要存储的字符串
     */
    public static void writeStringToFile(String fileName, String string) {
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try {
            out = BaoKanApp.getContext().openFileOutput(fileName, Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(string);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从手机存储卡读取字符串数据
     *
     * @param fileName 文件名
     * @return 读取到的字符串
     */
    public static String readStringFromFile(String fileName) {
        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        try {
            in = BaoKanApp.getContext().openFileInput(fileName);
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content.toString();
    }

    /**
     * 判断文件是否存在
     *
     * @param filePath 文件绝对路径
     * @return 是否存在
     */
    public static boolean fileIsExists(String filePath) {
        try {
            File f = new File(filePath);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 保存远程图片到相册 - 会保存到2个目录
     */
    public static void saveImageToAlbum(final Context context, final String imageUrl) {
        Picasso.with(context).load(imageUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                // 创建目录
                File appDir = new File(Environment.getExternalStorageDirectory(), "BaoKanImage");
                if (!appDir.exists()) {
                    appDir.mkdir();
                }

                // 获取图片类型
                String[] imageTypes = new String[]{".jpg", ".png", ".jpeg", "webp"};
                String imageType = "";
                if (imageUrl.endsWith(imageTypes[0])) {
                    imageType = "jpg";
                } else if (imageUrl.endsWith(imageTypes[1])) {
                    imageType = "png";
                } else {
                    imageType = "jpeg";
                }
                String fileName = System.currentTimeMillis() + "." + imageType;
                File file = new File(appDir, fileName);
                // 保存图片
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    if (TextUtils.equals(imageType, "jpg")) imageType = "jpeg";
                    imageType = imageType.toUpperCase();
                    bitmap.compress(Bitmap.CompressFormat.valueOf(imageType), 100, fos);
                    fos.flush();
                    fos.close();
                    Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // 其次把文件插入到系统图库
                try {
                    MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                // 最后通知图库更新
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getPath())));
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }

    /**
     * 保存图片到sd卡
     *
     * @param photoBitmap
     * @param photoName
     * @param path
     */
    public static String savePhoto(Bitmap photoBitmap, String path, String photoName) {
        String localPath = null;
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File photoFile = new File(path, photoName + ".png");
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(photoFile);
                if (photoBitmap != null) {
                    if (photoBitmap.compress(Bitmap.CompressFormat.PNG, 100,
                            fileOutputStream)) { // 转换完成
                        localPath = photoFile.getPath();
                        fileOutputStream.flush();
                    }
                }
            } catch (IOException e) {
                photoFile.delete();
                localPath = null;
                e.printStackTrace();
            } finally {
                try {
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                        fileOutputStream = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return localPath;
    }

}
