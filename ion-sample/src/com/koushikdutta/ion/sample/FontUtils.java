package com.koushikdutta.ion.sample;

public class FontUtils {

    
    public static final String ZIP_SUFFIX = ".zip";
    public static final String TTF_SUFFIX = ".ttf";

    public static boolean isZipFile(String path) {
        return path != null && path.endsWith(ZIP_SUFFIX);
    }
    public static boolean isTtfFile(String path) {
        return path != null && path.endsWith(TTF_SUFFIX);
    }
    
    public static String getTtfFilePath(String path) {
        return path.replace(ZIP_SUFFIX, TTF_SUFFIX);
    }
}
