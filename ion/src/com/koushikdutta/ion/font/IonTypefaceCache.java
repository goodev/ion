
package com.koushikdutta.ion.font;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Looper;
import android.util.Log;

import com.koushikdutta.ion.Ion;

import java.io.File;

public class IonTypefaceCache {
    public static final long DEFAULT_ERROR_CACHE_DURATION = 30000L;

    LruTypefaceCache cache;
    Ion ion;
    long errorCacheDuration = DEFAULT_ERROR_CACHE_DURATION;

    public long getErrorCacheDuration() {
        return errorCacheDuration;
    }

    public void setErrorCacheDuration(long errorCacheDuration) {
        this.errorCacheDuration = errorCacheDuration;
    }

    public IonTypefaceCache(Ion ion) {
        Context context = ion.getContext();
        this.ion = ion;
        cache = new LruTypefaceCache(getHeapSize(context) / 10);
    }

    public TypefaceInfo remove(String key) {
        return cache.removeTypefaceInfo(key);
    }

    public void clear() {
        cache.evictAllTypefaceInfo();
    }

    double heapRatio = 1d / 7d;

    public double getHeapRatio() {
        return heapRatio;
    }

    public void setHeapRatio(double heapRatio) {
        this.heapRatio = heapRatio;
    }

    @SuppressLint("Assert")
    public void put(TypefaceInfo info) {
        assert Thread.currentThread() == Looper.getMainLooper().getThread();
        int maxSize = (int) (getHeapSize(ion.getContext()) * heapRatio);
        if (maxSize != cache.maxSize())
            cache.setMaxSize(maxSize);
        cache.put(info.key, info);
    }

    public TypefaceInfo get(String key) {
        if (key == null)
            return null;

        // see if this thing has an immediate cache hit
        TypefaceInfo ret = cache.getTypefaceInfo(key);
        if (ret == null || ret.typeface != null)
            return ret;

        // if this bitmap load previously errored out, see if it is time to
        // retry
        // the fetch. connectivity error, server failure, etc, shouldn't be
        // cached indefinitely...
        if (ret.loadTime + errorCacheDuration > System.currentTimeMillis())
            return ret;

        cache.remove(key);
        return null;
    }

    public void dump() {
        Log.i("IonBitmapCache", "bitmap cache: " + cache.size());
        Log.i("IonBitmapCache", "freeMemory: " + Runtime.getRuntime().freeMemory());
    }

    @SuppressLint("Assert")
    public static Typeface loadTypeface(File file) {
        assert Thread.currentThread() != Looper.getMainLooper().getThread();

        return Typeface.createFromFile(file);
    }

    private static int getHeapSize(final Context context) {
        return ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass() * 1024 * 1024;
    }
}
