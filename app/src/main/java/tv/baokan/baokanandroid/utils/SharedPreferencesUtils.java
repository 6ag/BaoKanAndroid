package tv.baokan.baokanandroid.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 全局的配置信息
 */
public class SharedPreferencesUtils {

    // 新闻详情字体
    public static final String DETAIL_FONT = "font_key";

    // 配置文件名称
    private static final String CONFIG_FILE_NAME = "config";

    /**
     * 设置一个boolean类型配置
     *
     * @param context context
     * @param key     键
     * @param value   值
     */
    public static void setBoolean(Context context, String key, boolean value) {
        SharedPreferences sp = context.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(key, value).apply();
    }

    /**
     * 获取一个boolean类型配置
     *
     * @param context  context
     * @param key      键
     * @param defValue 缺省值
     * @return 根据key查找到value
     */
    public static boolean getBoolean(Context context, String key, boolean defValue) {
        SharedPreferences sp = context.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(key, defValue);
    }

    /**
     * 设置一个String类型配置
     *
     * @param context context
     * @param key     键
     * @param value   值
     */
    public static void setString(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(key, value).apply();
    }

    /**
     * 获取一个String类型配置
     *
     * @param context  context
     * @param key      键
     * @param defValue 缺省值
     * @return 根据key查找到value
     */
    public static String getString(Context context, String key, String defValue) {
        SharedPreferences sp = context.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE);
        return sp.getString(key, defValue);
    }

    /**
     * 设置一个int类型配置
     *
     * @param context context
     * @param key     键
     * @param value   值
     */
    public static void setInt(Context context, String key, int value) {
        SharedPreferences sp = context.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE);
        sp.edit().putInt(key, value).apply();
    }

    /**
     * 获取一个int类型配置
     *
     * @param context  context
     * @param key      键
     * @param defValue 缺省值
     * @return 根据key查找到value
     */
    public static int getInt(Context context, String key, int defValue) {
        SharedPreferences sp = context.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE);
        return sp.getInt(key, defValue);
    }

}
