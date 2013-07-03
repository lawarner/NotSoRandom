package org.apps.notsorandom;

/**
 * Created by andy on 6/27/13.
 */
public class SenseComponent {
    private String name_;
    private String label_;
    private int mask_;
    private int sortOrder_;
    private int defaultValue_;
    private int bitsPos_;

    SenseComponent() {
        name_ = null;
        bitsPos_ = 0;
    }

    SenseComponent(String name, String label, int mask, int order, int defaultValue) {
        name_ = name;
        label_ = label;
        mask_ = mask;
        sortOrder_ = order;

        bitsPos_ = 0;
        if (mask > 0)
            while ((mask & 1) == 0) {
                mask >>= 1;
                bitsPos_++;
            }

        if (defaultValue < 0 || defaultValue > 7)
            defaultValue_ = 0;
        else
            defaultValue_ = defaultValue << bitsPos_;

    }


    public String getName() {
        return name_;
    }

    public String getLabel() {
        return label_;
    }

    public int getMask() {
        return mask_;
    }

    public int getComponentValue(int senseValue) {
        int ret = (senseValue & mask_) >> bitsPos_;
        return ret;
    }

    public int getMaskedValue(int senseValue) {
        int ret = (senseValue << bitsPos_) & mask_;
        return ret;
    }

    public int getSortOrder() {
        return sortOrder_;
    }

    public int getDefaultValue() {
        return defaultValue_;
    }

    public int setSortOrder(int sortOrder) {
        int oldSortOrder = sortOrder_;
        sortOrder_ = sortOrder;
        return oldSortOrder;
    }
}
