package com.android.approjects.soundrecorder.util;

public class SystemPropertiesWrapper {

    public static String get(String key, String def) {
        // return SystemProperties.get(key,def);
        return "false";
    }

    public static boolean getBoolean(String key, boolean def) {
        // SystemProperties.getBoolean(key,def);
        return false;
    }
}
