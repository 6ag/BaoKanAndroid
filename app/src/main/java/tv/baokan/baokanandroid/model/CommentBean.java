package tv.baokan.baokanandroid.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class CommentBean implements Serializable {

    // 楼层
    private String plstep;

    // 评论id
    private String plid;

    // 评论用户名
    private String plusername;

    // 评论昵称
    private String plnickname;

    // 评论id
    private String id;

    // 栏目id
    private String classid;

    // 赞数量
    private String zcnum;

    // 评论内容
    private String saytext;

    // 评论时间
    private String saytime;

    // 用户头像 需要拼接
    private String userpic;

    // 是否被赞过 - 默认没有
    private boolean isStar = false;

    public CommentBean(JSONObject jsonObject) {
        try {
            plstep = jsonObject.getString("plstep");
            plid = jsonObject.getString("plid");
            plusername = jsonObject.getString("plusername");
            plnickname = jsonObject.getString("plnickname");
            id = jsonObject.getString("id");
            classid = jsonObject.getString("classid");
            zcnum = jsonObject.getString("zcnum");
            saytext = jsonObject.getString("saytext");
            saytime = jsonObject.getString("saytime");
            userpic = jsonObject.getString("userpic");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "CommentBean{" +
                "plstep='" + plstep + '\'' +
                ", plid='" + plid + '\'' +
                ", plusername='" + plusername + '\'' +
                ", plnickname='" + plnickname + '\'' +
                ", id='" + id + '\'' +
                ", classid='" + classid + '\'' +
                ", zcnum='" + zcnum + '\'' +
                ", saytext='" + saytext + '\'' +
                ", saytime='" + saytime + '\'' +
                ", userpic='" + userpic + '\'' +
                '}';
    }

    public String getPlstep() {
        return plstep;
    }

    public String getPlid() {
        return plid;
    }

    public String getPlusername() {
        return plusername;
    }

    public String getPlnickname() {
        return plnickname;
    }

    public String getId() {
        return id;
    }

    public String getClassid() {
        return classid;
    }

    public String getZcnum() {
        return zcnum;
    }

    public String getSaytext() {
        return saytext;
    }

    public String getSaytime() {
        return saytime;
    }

    public String getUserpic() {
        return userpic;
    }

    public void setZcnum(String zcnum) {
        this.zcnum = zcnum;
    }

    public boolean isStar() {
        return isStar;
    }

    public void setStar(boolean star) {
        isStar = star;
    }
}
