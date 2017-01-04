package tv.baokan.baokanandroid.model;

import java.io.Serializable;

/**
 * 分类栏目
 */
public class ColumnBean implements Serializable {

    // 分类id
    private String classId;

    // 分类名称
    private String className;

    public ColumnBean(String classId, String className) {
        this.classId = classId;
        this.className = className;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
