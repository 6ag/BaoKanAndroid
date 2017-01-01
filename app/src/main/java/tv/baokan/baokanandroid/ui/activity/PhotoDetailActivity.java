package tv.baokan.baokanandroid.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;

import tv.baokan.baokanandroid.R;

public class PhotoDetailActivity extends BaseActivity {

    /**
     * 便捷启动当前activity
     *
     * @param activity 来源activity
     * @param classid  栏目id
     * @param id       文章id
     */
    public static void start(Activity activity, String classid, String id) {
        Intent intent = new Intent(activity, PhotoDetailActivity.class);
        intent.putExtra("classid_key", classid);
        intent.putExtra("id_key", id);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.push_enter, R.anim.push_exit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

    }

}
