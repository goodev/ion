
package com.koushikdutta.ion.font;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.concurrent.CancellationException;

public class TypefaceCallback {

    public String key;
    public Ion ion;

    protected TypefaceCallback(Ion ion, String key, boolean put) {
        this.key = key;
        this.put = put;
        this.ion = ion;

        ion.typefacesPending.tag(key, this);
    }

    boolean put;

    boolean put() {
        return put;
    }

    protected void onReported() {
        ion.processTypefaceDeferred();
    }

    protected void report(final Exception e, final TypefaceInfo info) {
        AsyncServer.post(Ion.mainHandler, new Runnable() {
            @Override
            public void run() {
                TypefaceInfo result = info;
                if (result == null) {
                    // cache errors, unless they were cancellation exceptions
                    result = new TypefaceInfo(key, null, 0);
                    result.exception = e;
                    if (!(e instanceof CancellationException))
                        ion.typefaceCache.put(result);
                } else if (put()) {
                    ion.typefaceCache.put(result);
                }

                final ArrayList<FutureCallback<TypefaceInfo>> callbacks = ion.typefacesPending.remove(key);
                if (callbacks == null || callbacks.size() == 0) {
                    onReported();
                    return;
                }

                for (FutureCallback<TypefaceInfo> callback : callbacks) {
                    callback.onCompleted(e, result);
                }
                onReported();
            }
        });
    }

}
