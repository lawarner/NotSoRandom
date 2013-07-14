package org.apps.notsorandom;

/**
 * Per user persistent app configuration data.
 */
public class Config {

    public static final String DEFAULT_USER = "0";
    public static final String DEFAULT_X_COMPONENT = "tempo";
    public static final String DEFAULT_Y_COMPONENT = "roughness";

    private String user_;

    /**
     * For relative paths (i.e., doesn't start with a "/") this is prepended.
     * It is typically the path to the SD card.
     */
    private String root_;

    /**
     * Key of the sense component to show on X axis.
     */
//    private String xComponent_;
    private SenseComponent xComponent_;

    /**
     * Key of the sense component to show on Y axis.
     */
//    private String yComponent_;
    private SenseComponent yComponent_;

    /**
     * Timestamp of the last time the media library has been scanned.
     */
    private long lastScan_;


    Config() {

    }

    Config(String user, String root, SenseComponent xComponent, SenseComponent yComponent, long lastScan) {
        user_ = user;
        root_ = root;
        xComponent_ = xComponent;
        yComponent_ = yComponent;
        lastScan_ = lastScan;
    }

    public String getUser() {
        return user_;
    }

    public String getRoot() {
        return root_;
    }

    public SenseComponent getXcomponent() {
        return xComponent_;
    }

    public SenseComponent getYcomponent() {
        return yComponent_;
    }

    public long getLastScan() {
        return lastScan_;
    }
}
