package org.apps.notsorandom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Supports the various choices for queue size.
 */
public class QueueSizeListAdapter extends BaseExpandableListAdapter {

    private List<String> sizeChoices_;
    private int[] valueChoices_;

    private Context context_;

    private int current_;

    public QueueSizeListAdapter(Context context) {
        this(context, 2);  // default to 1000
    }

    public QueueSizeListAdapter(Context context, int start) {
        sizeChoices_ = new ArrayList<String>();
        sizeChoices_.add("10");
        sizeChoices_.add("100");
        sizeChoices_.add("1,000");
        sizeChoices_.add("2,000");
        sizeChoices_.add("Half selected");
        sizeChoices_.add("Unlimited");
        valueChoices_ = new int[sizeChoices_.size()];
        valueChoices_[0] = 10;
        valueChoices_[1] = 100;
        valueChoices_[2] = 1000;
        valueChoices_[3] = 2000;
        valueChoices_[4] = -2;
        valueChoices_[5] = -1;

        context_ = context;
        current_ = start;
    }

    public int getCurrent() {
        if (current_ >= 0 && current_ < valueChoices_.length)
            return valueChoices_[current_];

        return 0;
    }

    public void setCurrent(int current) {
        current_ = current;
    }

    @Override
    public int getGroupCount() {
        return 1;
    }

    @Override
    public int getChildrenCount(int group) {
        return sizeChoices_.size();
    }

    @Override
    public Object getChild(int group, int child) {
        return sizeChoices_.get(child);
    }

    @Override
    public View getChildView(int group, int child, boolean isLastChild, View convertView,
                             ViewGroup parent) {
        final String qsName = (String) getChild(group, child);

        if (convertView == null) {
            LayoutInflater lif = (LayoutInflater) context_.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = lif.inflate(R.layout.queuesize_child_item, null);
        }
        TextView tv = (TextView) convertView.findViewById(R.id.queueSize);
        tv.setText("    " + qsName);

        ImageView iv = (ImageView) convertView.findViewById(R.id.selected);
        iv.setImageResource((child == current_) ? android.R.drawable.button_onoff_indicator_on
                : android.R.drawable.button_onoff_indicator_off);
        return convertView;
    }

    @Override
    public Object getGroup(int group) {
        return "Queue Size";
    }

    @Override
    public long getGroupId(int group) {
        return 0;
    }

    @Override
    public long getChildId(int group, int child) {
        return child;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int group, boolean isExpanded, View convertView,
                             ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater lif = (LayoutInflater) context_.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = lif.inflate(R.layout.queuesize_group_item, null);
        }
        String qsName = (String) getChild(0, current_);
        TextView tv = (TextView) convertView.findViewById(R.id.queueSize);
        tv.setText(qsName);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int group, int child) {
        return true;
    }
}
