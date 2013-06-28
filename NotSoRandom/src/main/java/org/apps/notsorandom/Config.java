package org.apps.notsorandom;

/**
 * Created by andy on 6/27/13.
 */
public class Config {
    private String user_;
    private String root_;
    private String xComponent_;
    private String yComponent_;
    private long lastScan_;

    Config() {

    }

    Config(String user, String root, String xComponent, String yComponent, long lastScan) {
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

    public String getXcomponent() {
        return xComponent_;
    }

    public String getYcomponent_() {
        return yComponent_;
    }

    public long getLastScan() {
        return lastScan_;
    }
}
