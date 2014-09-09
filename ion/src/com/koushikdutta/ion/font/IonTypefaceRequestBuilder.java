
package com.koushikdutta.ion.font;

import android.annotation.SuppressLint;
import android.os.Looper;
import android.widget.TextView;

import com.koushikdutta.async.future.SimpleFuture;
import com.koushikdutta.async.util.FileCache;
import com.koushikdutta.ion.ContextReference;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.IonRequestBuilder;
import com.koushikdutta.ion.L;
import com.koushikdutta.ion.Loader;
import com.koushikdutta.ion.builder.Builders;

@SuppressWarnings("rawtypes")
public class IonTypefaceRequestBuilder implements Builders.Any.TF, TypefaceFutureBuilder {
    private static final SimpleFuture<TextView> FUTURE_TEXTVIEW_NULL_URI = new SimpleFuture<TextView>() {
        {
            setComplete(new NullPointerException("uri"));
        }
    };

    IonRequestBuilder builder;
    Ion ion;
    ContextReference.TextViewContextReference textViewPostRef;

    public IonTypefaceRequestBuilder(IonRequestBuilder builder) {
        this.builder = builder;
        ion = builder.ion;
    }

    public IonTypefaceRequestBuilder(Ion ion) {
        this.ion = ion;
    }

    public IonTypefaceRequestBuilder withTextView(TextView textView) {
        textViewPostRef = new ContextReference.TextViewContextReference(textView);
        return this;
    }

    @SuppressLint("Assert")
    @Override
    public SimpleFuture<TextView> intoTextView(TextView textView) {
        assert Thread.currentThread() == Looper.getMainLooper().getThread();
        if (textView == null)
            throw new NullPointerException("textView");

        // no uri? just set a placeholder and bail
        if (builder.uri == null) {
            return FUTURE_TEXTVIEW_NULL_URI;
        }

        withTextView(textView);

        // executeCache the request, see if we get a bitmap from cache.
        TypefaceFetcher bitmapFetcher = executeCache();
        if (bitmapFetcher.info != null) {
            IonTypeface drawable = setIonTypeface(textView, bitmapFetcher.info, Loader.LoaderEmitter.LOADED_FROM_MEMORY);
            drawable.cancel();
            SimpleFuture<TextView> textViewFuture = drawable.getFuture();
            textViewFuture.reset();
            L.d("---- load from cache.... " + bitmapFetcher.info.typeface);
            textView.setTypeface(bitmapFetcher.info.typeface);
            textViewFuture.setComplete(bitmapFetcher.info.exception, textView);
            return textViewFuture;
        }
        IonTypeface typeface = setIonTypeface(textView, null, 0);
        SimpleFuture<TextView> textViewFuture = typeface.getFuture();
        textViewFuture.reset();
        typeface.register(ion, bitmapFetcher.typefaceKey);
        // nothing from cache, check to see if there's too many imageview loads
        // already in progress
        if (TypefaceFetcher.shouldDeferTextView(ion)) {
            bitmapFetcher.defer();
        } else {
            bitmapFetcher.execute();
        }

        return textViewFuture;
    }

    private IonTypeface setIonTypeface(TextView textView, TypefaceInfo info, int loadedFrom) {
        IonTypeface ret = IonTypeface.getOrCreateIonTypeface(textView).ion(ion);
        textView.setTag(IonTypeface.TAG, ret);
        return ret;
    }

    public String computeTypefaceKey(String downloadKey) {
        assert downloadKey != null;

        return downloadKey;
    }

    private String computeDownloadKey() {
        String downloadKey = builder.uri;
        // although a gif is always same download, the decode (non/animated)
        // result may different
        return FileCache.toKeyString(downloadKey);
    }

    TypefaceFetcher executeCache() {
        final String downloadKey = computeDownloadKey();
        String typefaceKey = computeTypefaceKey(downloadKey);

        // TODO: eliminate this allocation?
        TypefaceFetcher ret = new TypefaceFetcher();
        ret.downloadKey = downloadKey;
        ret.typefaceKey = typefaceKey;
        ret.builder = builder;
        // ret.postProcess = postProcess;

        // see if this request can be fulfilled from the cache
        if (!builder.noCache) {
            TypefaceInfo typeface = builder.ion.typefaceCache.get(typefaceKey);
            L.d("get cache..... " + typeface);
            if (typeface != null) {
                ret.info = typeface;
                return ret;
            }
        }

        return ret;
    }

}
