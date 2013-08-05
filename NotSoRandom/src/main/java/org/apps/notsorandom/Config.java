package org.apps.notsorandom;

/**
 * Per user persistent app configuration data.
 */
public class Config {
    private static final String TAG = Config.class.getSimpleName();

    public static final String DEFAULT_USER = "0";
    public static final String DEFAULT_X_COMPONENT = "tempo";
    public static final String DEFAULT_Y_COMPONENT = "roughness";
    public static final String DEFAULT_Z_COMPONENT = "humor";

    private String user_;

    /**
     * For relative paths (i.e., doesn't start with a "/") this is prepended.
     * It is typically the path to the SD card.
     */
    private String root_;

    /**
     * Sense component to show on X axis.
     */
    private SenseComponent xComponent_;

    /**
     * Sense component to show on Y axis.
     */
    private SenseComponent yComponent_;

    /**
     * Sense component to show on Z axis.
     */
    private SenseComponent zComponent_;

    /**
     * Timestamp of the last time the media library has been scanned.
     */
    private long lastScan_;


    Config(String user, String root, SenseComponent xComponent,
           SenseComponent yComponent, SenseComponent zComponent, long lastScan) {
        user_ = user;
        root_ = root;
        xComponent_ = xComponent;
        yComponent_ = yComponent;
        zComponent_ = zComponent;
        lastScan_ = lastScan;
        MusicPlayerApp.log(TAG, "Config created. X mask=" + Integer.toHexString(xComponent.getMask())
                + " Y mask=" + Integer.toHexString(yComponent.getMask())
                + " Z mask=" + Integer.toHexString(zComponent.getMask()));
    }

    public long getLastScan() {
        return lastScan_;
    }

    public String getRoot() {
        return root_;
    }

    public String getUser() {
        return user_;
    }

    public SenseComponent getXcomponent() {
        return xComponent_;
    }

    public SenseComponent getYcomponent() {
        return yComponent_;
    }

    public SenseComponent getZcomponent() {
        return zComponent_;
    }

    public int getXYZMask() {
        return xComponent_.getMask()
             | yComponent_.getMask()
             | zComponent_.getMask();
    }
}
