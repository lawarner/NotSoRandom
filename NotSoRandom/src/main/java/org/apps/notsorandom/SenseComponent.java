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

    SenseComponent() {
        name_ = null;
    }

    SenseComponent(String name, String label, int mask, int order, int defaultValue) {
        name_ = name;
        label_ = label;
        mask_ = mask;
        sortOrder_ = order;
        defaultValue_ = defaultValue;
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

    public int getSortOrder() {
        return sortOrder_;
    }

    public int getDefaultValue() {
        return defaultValue_;
    }

}
