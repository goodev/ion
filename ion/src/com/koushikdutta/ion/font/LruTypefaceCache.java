
package com.koushikdutta.ion.font;

import com.koushikdutta.async.util.LruCache;
import com.koushikdutta.ion.bitmap.SoftReferenceHashtable;

public class LruTypefaceCache extends LruCache<String, TypefaceInfo> {
    private SoftReferenceHashtable<String, TypefaceInfo> soft = new SoftReferenceHashtable<String, TypefaceInfo>();

    public LruTypefaceCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected long sizeOf(String key, TypefaceInfo info) {
        return info.sizeOf();
    }

    public TypefaceInfo getTypefaceInfo(String key) {
        TypefaceInfo ret = get(key);
        if (ret != null)
            return ret;

        ret = soft.remove(key);
        if (ret != null)
            put(key, ret);

        return ret;
    }

    public TypefaceInfo removeTypefaceInfo(String key) {
        TypefaceInfo i1 = soft.remove(key);
        TypefaceInfo i2 = remove(key);
        if (i2 != null)
            return i2;
        return i1;
    }

    public void evictAllTypefaceInfo() {
        evictAll();
        soft.clear();
    }

    @Override
    protected void entryRemoved(boolean evicted, String key, TypefaceInfo oldValue, TypefaceInfo newValue) {
        super.entryRemoved(evicted, key, oldValue, newValue);

        // on eviction, put the bitmaps into the soft ref table
        if (evicted)
            soft.put(key, oldValue);
    }
}
