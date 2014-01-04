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
    public static final String DEFAULT_W_COMPONENT = "taste";

    private String user_;

    /**
     * For relative paths (i.e., doesn't start with a "/") this is prepended.
     * It is typically the path to the SD card.
     */
    private String root_;

    private SenseComponent[] standardComponents_;
    private SenseComponent[] adHocComponents_;
    private SenseComponent[] autoComponents_;

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

    private SenseComponent wComponent_;

    /**
     * Timestamp of the last time the media library has been scanned.
     */
    private long lastScan_;


    Config(String user, String root, SenseComponent xComponent,
           SenseComponent yComponent, SenseComponent zComponent,
           SenseComponent wComponent, long lastScan) {
        user_ = user;
        root_ = root;
        setXYZWcomponents(xComponent, yComponent, zComponent, wComponent);
        lastScan_ = lastScan;
        MusicPlayerApp.log(TAG, "Config created. X mask=" + Integer.toHexString(xComponent.getMask())
                + " Y mask=" + Integer.toHexString(yComponent.getMask())
                + " Z mask=" + Integer.toHexString(zComponent.getMask())
                + " W mask=" + Integer.toHexString(wComponent.getMask()));
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
    public SenseComponent getWcomponent() {
        return wComponent_;
    }

    public int getXYZMask() {
        return xComponent_.getMask()
             | yComponent_.getMask()
             | zComponent_.getMask();
    }

    public int getXYZWMask() {
        return xComponent_.getMask()
                | yComponent_.getMask()
                | zComponent_.getMask()
                | wComponent_.getMask();
    }

    public void setXYZWcomponents(SenseComponent xComponent, SenseComponent yComponent,
                                  SenseComponent zComponent, SenseComponent wComponent) {
        xComponent_ = xComponent;
        yComponent_ = yComponent;
        zComponent_ = zComponent;
        wComponent_ = wComponent;
    }
}
