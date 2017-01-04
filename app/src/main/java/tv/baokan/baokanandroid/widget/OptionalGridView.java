package tv.baokan.baokanandroid.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * 待选择的GridView
 */
public class OptionalGridView extends GridView {

    public OptionalGridView(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
