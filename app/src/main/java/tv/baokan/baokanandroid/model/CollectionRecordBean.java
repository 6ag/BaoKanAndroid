package tv.baokan.baokanandroid.model;

import org.json.JSONException;
import org.json.JSONObject;

import tv.baokan.baokanandroid.utils.LogUtils;

/**
 * 收藏记录
 */
public class CollectionRecordBean {

    private static final String TAG = "CollectionRecordBean";

    // 文章标题
    private String title;

    // 文章分类id
    private String classid;

    // 文章id
    private String id;

    // 表名
    private String tbname;

    // 收藏时间
    private String favatime;

    // 收藏id
    private String favaid;

    // 收藏夹分类id
    private String cid;

    public CollectionRecordBean(JSONObject jsonObject) {
        try {
            title = jsonObject.getString("title");
            classid = jsonObject.getString("classid");
            id = jsonObject.getString("id");
            tbname = jsonObject.getString("tbname");
            favatime = jsonObject.getString("favatime");
            favaid = jsonObject.getString("favaid");
            cid = jsonObject.getString("cid");
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.d(TAG, "数据解析异常");
        }
    }

    public String getTitle() {
        return title;
    }

    public String getClassid() {
        return classid;
    }

    public String getId() {
        return id;
    }

    public String getTbname() {
        return tbname;
    }

    public String getFavatime() {
        return favatime;
    }

    public String getFavaid() {
        return favaid;
    }

    public String getCid() {
        return cid;
    }
}
