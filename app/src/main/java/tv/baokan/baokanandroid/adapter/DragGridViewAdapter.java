package tv.baokan.baokanandroid.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.model.ColumnBean;

public class DragGridViewAdapter extends BaseAdapter {

    private final static String TAG = "DragGridViewAdapter";

    // 是否显示底部的item
    private boolean isItemShow = false;
    private Context context;

    // 控制的postion
    private int holdPosition;

    // 是否改变
    private boolean isChanged = false;

    // 列表数据是否改变
    private boolean isListChanged = false;

    // 是否可见
    boolean isVisible = true;

    // 可以拖动的列表（即用户选择的频道列表）
    public List<ColumnBean> selectedList;

    // TextView 频道内容
    private TextView item_text;

    // 要删除的position
    public int remove_position = -1;

    // 是否是用户频道
    private boolean isUser = false;

    public DragGridViewAdapter(Context context, List<ColumnBean> selectedList, boolean isUser) {
        this.context = context;
        this.selectedList = selectedList;
        this.isUser = isUser;
    }

    @Override
    public int getCount() {
        return selectedList == null ? 0 : selectedList.size();
    }

    @Override
    public ColumnBean getItem(int position) {
        if (selectedList != null && selectedList.size() != 0) {
            return selectedList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_column_item, null);
        item_text = (TextView) view.findViewById(R.id.text_item);
        String className = getItem(position).getClassName();
        item_text.setText(className);
        if (isUser) {
            if ((position == 0) || (position == 1)) {
                item_text.setEnabled(false);
            }
        }
        if (isChanged && (position == holdPosition) && !isItemShow) {
            item_text.setText("");
            item_text.setSelected(true);
            item_text.setEnabled(true);
            isChanged = false;
        }
        if (!isVisible && (position == -1 + selectedList.size())) {
            item_text.setText("");
            item_text.setSelected(true);
            item_text.setEnabled(true);
        }
        if (remove_position == position) {
            item_text.setText("");
        }
        return view;
    }

    /**
     * 添加频道列表
     */
    public void addItem(ColumnBean columnBean) {
        selectedList.add(columnBean);
        isListChanged = true;
        notifyDataSetChanged();
    }

    /**
     * 拖动变更频道排序
     */
    public void exchange(int dragPostion, int dropPostion) {
        holdPosition = dropPostion;
        ColumnBean dragItem = getItem(dragPostion);
        Log.d(TAG, "startPostion=" + dragPostion + ";endPosition=" + dropPostion);
        if (dragPostion < dropPostion) {
            selectedList.add(dropPostion + 1, dragItem);
            selectedList.remove(dragPostion);
        } else {
            selectedList.add(dropPostion, dragItem);
            selectedList.remove(dragPostion + 1);
        }
        isChanged = true;
        isListChanged = true;
        notifyDataSetChanged();
    }

    /**
     * 获取已经选择的栏目集合
     */
    public List<ColumnBean> getSelectedList() {
        return selectedList;
    }

    /**
     * 设置删除的position
     */
    public void setRemove(int position) {
        remove_position = position;
        notifyDataSetChanged();
    }

    /**
     * 删除频道列表
     */
    public void remove() {
        selectedList.remove(remove_position);
        remove_position = -1;
        isListChanged = true;
        notifyDataSetChanged();
    }

    /**
     * 获取是否可见
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * 设置是否可见
     */
    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    /**
     * 显示放下的item
     */
    public void setShowDropItem(boolean show) {
        isItemShow = show;
    }
}