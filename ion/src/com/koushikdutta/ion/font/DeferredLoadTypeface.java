
package com.koushikdutta.ion.font;

import com.koushikdutta.ion.Ion;

public class DeferredLoadTypeface extends TypefaceCallback {
    public TypefaceFetcher fetcher;

    public DeferredLoadTypeface(Ion ion, String key, TypefaceFetcher fetcher) {
        super(ion, key, false);
        this.fetcher = fetcher;
    }
}
