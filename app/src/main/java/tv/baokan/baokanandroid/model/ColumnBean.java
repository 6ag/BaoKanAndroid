package tv.baokan.baokanandroid.model;

import java.io.Serializable;

/**
 * 分类栏目
 */
public class ColumnBean implements Serializable {

    // 分类id
    private String classid;

    // 分类名称
    private String classname;

    public ColumnBean(String classid, String classname) {
        this.classid = classid;
        this.classname = classname;
    }

    public String getClassid() {
        return classid;
    }

    public void setClassid(String classid) {
        this.classid = classid;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }
}
