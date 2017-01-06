package tv.baokan.baokanandroid.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 评论记录
 */
public class CommentRecordBean {

    private String title;

    private String saytext;

    private String saytime;

    private String id;

    private String classid;

    private String tbname;

    private String plid;

    private String plstep;

    private String plusername;

    private String zcnum;

    private String userpic;

    public CommentRecordBean(JSONObject jsonObject) {
        try {
            title = jsonObject.getString("title");
            saytext = jsonObject.getString("saytext");
            saytime = jsonObject.getString("saytime");
            id = jsonObject.getString("id");
            classid = jsonObject.getString("classid");
            tbname = jsonObject.getString("tbname");
            plid = jsonObject.getString("plid");
            plstep = jsonObject.getString("plstep");
            plusername = jsonObject.getString("plusername");
            zcnum = jsonObject.getString("zcnum");
            userpic = jsonObject.getString("userpic");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getTitle() {
        return title;
    }

    public String getSaytext() {
        return saytext;
    }

    public String getSaytime() {
        return saytime;
    }

    public String getId() {
        return id;
    }

    public String getClassid() {
        return classid;
    }

    public String getTbname() {
        return tbname;
    }

    public String getPlid() {
        return plid;
    }

    public String getPlstep() {
        return plstep;
    }

    public String getPlusername() {
        return plusername;
    }

    public String getZcnum() {
        return zcnum;
    }

    public String getUserpic() {
        return userpic;
    }
}
