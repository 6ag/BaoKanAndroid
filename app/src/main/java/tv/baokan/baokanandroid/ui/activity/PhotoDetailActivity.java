package tv.baokan.baokanandroid.ui.activity;

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
     * @param context 上下文
     * @param classid 栏目id
     * @param id      文章id
     */
    public static void start(Context context, String classid, String id) {
        Intent intent = new Intent(context, PhotoDetailActivity.class);
        intent.putExtra("classid_key", classid);
        intent.putExtra("id_key", id);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

    }

    // 返回true则是不继续传播事件，自己处理。返回false则系统继续传播处理
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            overridePendingTransition(R.anim.pop_enter, R.anim.pop_exit);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
