package tv.baokan.baokanandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import tv.baokan.baokanandroid.R;
import tv.baokan.baokanandroid.model.ColumnBean;

/**
 * 待选择的栏目适配器
 */
public class OptionalGridViewAdapter extends BaseAdapter {

    private Context context;
    public List<ColumnBean> optionalList;
    private TextView item_text;

    // 是否可见 在移动动画完毕之前不可见，动画完毕后可见
    boolean isVisible = true;

    // 要删除的position
    public int remove_position = -1;

    // 是否是已经选择的，也就是selectedList里的
    private boolean isSelected = false;

    public OptionalGridViewAdapter(Context context, List<ColumnBean> optionalList, boolean isSelected) {
        this.context = context;
        this.optionalList = optionalList;
        this.isSelected = isSelected;
    }

    @Override
    public int getCount() {
        return optionalList == null ? 0 : optionalList.size();
    }

    @Override
    public ColumnBean getItem(int position) {
        if (optionalList != null && optionalList.size() != 0) {
            return optionalList.get(position);
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
        if (isSelected) {
            if ((position == 0) || (position == 1)) {
                item_text.setEnabled(false);
            }
        }
        if (!isVisible && (position == -1 + optionalList.size())) {
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
     * 获取可选栏目数据集合
     */
    public List<ColumnBean> getOptionalList() {
        return optionalList;
    }

    /**
     * 添加频道列表
     */
    public void addItem(ColumnBean channel) {
        optionalList.add(channel);
        notifyDataSetChanged();
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
        optionalList.remove(remove_position);
        remove_position = -1;
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

}
