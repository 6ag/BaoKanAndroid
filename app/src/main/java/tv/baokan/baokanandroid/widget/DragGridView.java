package tv.baokan.baokanandroid.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;


import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.adapter.DragGridViewAdapter;
import tv.baokan.baokanandroid.utils.SizeUtils;

/**
 * 可拖拽的GridView
 */
public class DragGridView extends GridView {

    // 点击时候的X位置
    public int downX;

    // 点击时候的Y位置
    public int downY;

    // 点击时候对应整个界面的X位置
    public int windowX;

    // 点击时候对应整个界面的Y位置
    public int windowY;

    // view相对自己的X
    private int win_view_x;

    // view相对自己的Y
    private int win_view_y;

    // 长按时候对应postion
    public int dragPosition;

    // Up后对应的ITEM的Position
    private int dropPosition;

    // 开始拖动的ITEM的Position
    private int startPosition;

    // item高
    private int itemHeight;

    // item宽
    private int itemWidth;

    // 拖动的时候对应item的view
    private View dragImageView = null;

    // 长按的时候item的view
    private ViewGroup dragItemView = null;

    // WindowManager管理器
    private WindowManager windowManager = null;

    private WindowManager.LayoutParams windowParams = null;

    // item总量
    private int itemTotalCount;

    // 一行的item数量
    private int nColumns = 4;

    // 行数
    private int nRows;

    // 剩余部分
    private int Remainder;

    // 是否在移动
    private boolean isMoving = false;

    private int holdPosition;

    // 拖动的时候放大的倍数
    private double dragScale = 1.2D;

    // 震动器
    private Vibrator mVibrator;

    // 每个ITEM之间的水平间距
    private int mHorizontalSpacing = 15;

    // 每个ITEM之间的竖直间距
    private int mVerticalSpacing = 15;

    // 移动时候最后个动画的ID
    private String LastAnimationID;

    public DragGridView(Context context) {
        super(context);
        init(context);
    }

    public DragGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public DragGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        if (isInEditMode()) { return; }
    }

    public void init(Context context) {
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        // 将布局文件中设置的间距dp转为px
        mHorizontalSpacing = SizeUtils.dip2px(context, mHorizontalSpacing);
    }

    /**
     * 在ScrollView内，所以要进行计算高度
     */
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            downX = (int) ev.getX();
            downY = (int) ev.getY();
            windowX = (int) ev.getX();
            windowY = (int) ev.getY();
            setOnItemClickListener(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 停止拖动 ，释放并初始化
     */
    private void stopDrag() {
        if (dragImageView != null) {
            windowManager.removeView(dragImageView);
            dragImageView = null;
        }
    }

    /**
     * 在拖动的情况更新View的位置
     */
    private void onDrag(int x, int y, int rawx, int rawy) {
        if (dragImageView != null) {
            windowParams.alpha = 0.6f;
            windowParams.x = rawx - win_view_x;
            windowParams.y = rawy - win_view_y;
            windowManager.updateViewLayout(dragImageView, windowParams);
        }
    }

    /**
     * 创建窗口对象、添加我们要移动的View
     *
     * @param dragBitmap
     * @param x
     * @param y
     */
    public void startDrag(Bitmap dragBitmap, int x, int y) {
        stopDrag();
        // 获取window界面的Params
        windowParams = new WindowManager.LayoutParams();
        // Gravity.TOP|Gravity.START;这个必须加
        windowParams.gravity = Gravity.TOP | Gravity.START;
        // 得到要移动的View左上角相对于屏幕的坐标
        windowParams.x = x - win_view_x;
        windowParams.y = y - win_view_y;
        // 设置拖拽item的宽和高
        windowParams.width = (int) (dragScale * dragBitmap.getWidth());   // 放大dragScale倍，可以设置拖动后的倍数
        windowParams.height = (int) (dragScale * dragBitmap.getHeight()); // 放大dragScale倍，可以设置拖动后的倍数
        this.windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        this.windowParams.format = PixelFormat.TRANSLUCENT;
        this.windowParams.windowAnimations = 0;
        ImageView iv = new ImageView(getContext());
        iv.setImageBitmap(dragBitmap);
        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(iv, windowParams);
        dragImageView = iv;
    }

    /**
     * 隐藏放下的item
     */
    private void hideDropItem() {
        ((DragGridViewAdapter) getAdapter()).setShowDropItem(false);
    }

    /**
     * 在松手下放的情况，更新界面
     */
    private void onDrop(int x, int y) {
        // 根据拖动到的x,y坐标获取拖动位置下方的ITEM对应的POSTION
        dropPosition = pointToPosition(x, y);
        DragGridViewAdapter mDragAdapter = (DragGridViewAdapter) getAdapter();
        // 显示刚拖动的ITEM
        mDragAdapter.setShowDropItem(true);
        // 刷新适配器，让对应的ITEM显示
        mDragAdapter.notifyDataSetChanged();
    }

    /**
     * 长按点击监听
     *
     * @param ev
     */
    public void setOnItemClickListener(final MotionEvent ev) {
        setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int x = (int) ev.getX();  // 长按事件的X位置
                int y = (int) ev.getY();  // 长按事件的y位置
                startPosition = position; // 第一次点击的postion
                dragPosition = position;
                if (startPosition <= 1) {
                    // 前2个默认不点击、可以设置
                    return false;
                }
                ViewGroup dragViewGroup = (ViewGroup) getChildAt(dragPosition - getFirstVisiblePosition());
                TextView dragTextView = (TextView) dragViewGroup.findViewById(R.id.text_item);
                dragTextView.setSelected(true);
                dragTextView.setEnabled(false);
                itemHeight = dragViewGroup.getHeight();
                itemWidth = dragViewGroup.getWidth();
                itemTotalCount = DragGridView.this.getCount();
                // 如果特殊的这个不等于拖动的那个,并且不等于-1
                if (dragPosition != AdapterView.INVALID_POSITION) {
                    // 释放的资源使用的绘图缓存。如果你调用buildDrawingCache()手动没有调用setDrawingCacheEnabled(真正的),你应该清理缓存使用这种方法。
                    win_view_x = windowX - dragViewGroup.getLeft();  // view相对自己的x
                    win_view_y = windowY - dragViewGroup.getTop();  // view相对自己的y
                    dragItemView = dragViewGroup;
                    dragViewGroup.destroyDrawingCache();
                    dragViewGroup.setDrawingCacheEnabled(true);
                    Bitmap dragBitmap = Bitmap.createBitmap(dragViewGroup.getDrawingCache());
                    mVibrator.vibrate(50); // 设置震动时间
                    startDrag(dragBitmap, (int) ev.getRawX(), (int) ev.getRawY());
                    hideDropItem();
                    dragViewGroup.setVisibility(View.INVISIBLE);
                    isMoving = false;
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (dragImageView != null && dragPosition != AdapterView.INVALID_POSITION) {
            // 移动时候的对应x,y位置
            int x = (int) ev.getX();
            int y = (int) ev.getY();
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = (int) ev.getX();
                    windowX = (int) ev.getX();
                    downY = (int) ev.getY();
                    windowY = (int) ev.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    onDrag(x, y, (int) ev.getRawX(), (int) ev.getRawY());
                    if (!isMoving) {
                        OnMove(x, y);
                    }
                    if (pointToPosition(x, y) != AdapterView.INVALID_POSITION) {
                        break;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    stopDrag();
                    onDrop(x, y);
                    requestDisallowInterceptTouchEvent(false);
                    break;

                default:
                    break;
            }
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 移动的时候触发，移动所有改变的Item
     */
    public void OnMove(int x, int y) {
        // 拖动的view下方的position
        int dPosition = pointToPosition(x, y);
        // 判断下方的position是否是最开始2个不能拖动的
        if (dPosition > 1) {
            if ((dPosition == -1) || (dPosition == dragPosition)) {
                return;
            }
            dropPosition = dPosition;
            if (dragPosition != startPosition) {
                dragPosition = startPosition;
            }
            int movecount;
            // 拖动的=开始拖的，并且 拖动的 不等于放下的
            // 移需要移动的动ITEM数量
            movecount = dropPosition - dragPosition;
            if (movecount == 0) {
                return;
            }
            int movecount_abs = Math.abs(movecount);
            if (dPosition != dragPosition) {
                // dragGroup设置为不可见
                ViewGroup dragGroup = (ViewGroup) getChildAt(dragPosition);
                dragGroup.setVisibility(View.INVISIBLE);
                float to_x = 1; // 移动的X偏移量
                float to_y; // 移动的Y偏移量
                // x_vlaue移动的距离百分比（相对于自己长度的百分比）
                float x_vlaue = ((float) mHorizontalSpacing / (float) itemWidth) + 1.0f;
                // y_vlaue移动的距离百分比（相对于自己宽度的百分比）
                float y_vlaue = ((float) mVerticalSpacing / (float) itemHeight) + 1.0f;
                Log.d("x_vlaue", "x_vlaue = " + x_vlaue);
                for (int i = 0; i < movecount_abs; i++) {
                    to_x = x_vlaue;
                    to_y = y_vlaue;
                    // 向右
                    if (movecount > 0) {
                        // 判断是不是同一行的
                        holdPosition = dragPosition + i + 1;
                        if (dragPosition / nColumns == holdPosition / nColumns) {
                            to_x = -x_vlaue;
                            to_y = 0;
                        } else if (holdPosition % 4 == 0) {
                            to_x = 3 * x_vlaue;
                            to_y = -y_vlaue;
                        } else {
                            to_x = -x_vlaue;
                            to_y = 0;
                        }
                    } else {
                        // 向左,下移到上，右移到左
                        holdPosition = dragPosition - i - 1;
                        if (dragPosition / nColumns == holdPosition / nColumns) {
                            to_x = x_vlaue;
                            to_y = 0;
                        } else if ((holdPosition + 1) % 4 == 0) {
                            to_x = -3 * x_vlaue;
                            to_y = y_vlaue;
                        } else {
                            to_x = x_vlaue;
                            to_y = 0;
                        }
                    }
                    ViewGroup moveViewGroup = (ViewGroup) getChildAt(holdPosition);
                    Animation moveAnimation = getMoveAnimation(to_x, to_y);
                    moveViewGroup.startAnimation(moveAnimation);
                    // 如果是最后一个移动的，那么设置他的最后个动画ID为LastAnimationID
                    if (holdPosition == dropPosition) {
                        LastAnimationID = moveAnimation.toString();
                    }
                    moveAnimation.setAnimationListener(new Animation.AnimationListener() {

                        @Override
                        public void onAnimationStart(Animation animation) {
                            isMoving = true;
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            // 如果为最后个动画结束，那执行下面的方法
                            if (animation.toString().equalsIgnoreCase(LastAnimationID)) {
                                DragGridViewAdapter mDragAdapter = (DragGridViewAdapter) getAdapter();
                                mDragAdapter.exchange(startPosition, dropPosition);
                                startPosition = dropPosition;
                                dragPosition = dropPosition;
                                isMoving = false;
                            }
                        }
                    });
                }
            }
        }
    }

    /**
     * 获取移动的动画
     */
    public Animation getMoveAnimation(float toXValue, float toYValue) {
        TranslateAnimation mTranslateAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0F,
                Animation.RELATIVE_TO_SELF, toXValue,
                Animation.RELATIVE_TO_SELF, 0.0F,
                Animation.RELATIVE_TO_SELF, toYValue);// 当前位置移动到指定位置
        mTranslateAnimation.setFillAfter(true);// 设置一个动画效果执行完毕后，View对象保留在终止的位置。
        mTranslateAnimation.setDuration(300L);
        return mTranslateAnimation;
    }
}
