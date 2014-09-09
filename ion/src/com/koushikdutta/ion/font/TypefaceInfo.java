
package com.koushikdutta.ion.font;

import android.graphics.Typeface;

import java.io.File;

public class TypefaceInfo {
    public Typeface typeface;
    public int fileSize;
    public long loadTime = System.currentTimeMillis();
    public long drawTime;
    final public String key;
    public int loadedFrom;
    public Exception exception;
    public File fontFile;

    public TypefaceInfo(String key, Typeface typeface, int fileSize) {
        this.typeface = typeface;
        this.key = key;
        this.fileSize = fileSize;
    }

    public int sizeOf() {
        return fileSize;
    }
}
