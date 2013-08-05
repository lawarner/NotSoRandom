package org.apps.notsorandom;

/**
 * A sense component describes one range of values (0-7).  These components can be stored
 * compactly as 4 bits (with the highest bit reserved).  Within the music player, each
 * component represents a dimension and it uses 32 bits to store them, giving up to 8
 * dimensions.  The values range from 1 to 6 and 0 meaning undefined.
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

    /**
     * Returns the component value from 0 to 7 where 0 means uninitialized.
     * @param senseValue A sense value.
     * @return The sense value for this component.
     */
    public int getComponentValue(int senseValue) {
        int ret = (senseValue & mask_) >> bitsPos_;
        return ret;
    }

    /**
     * Returns component value as an index from 0 to 6.  Used to index arrays.
     * @param senseValue
     * @return
     */
    public int getComponentIndex(int senseValue) {
        int ret = getComponentValue(senseValue);
        if (ret > 0) ret--;
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
