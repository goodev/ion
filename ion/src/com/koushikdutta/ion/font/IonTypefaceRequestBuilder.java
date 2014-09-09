
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
    ContextReference.TextViewContextReference imageViewPostRef;

    public IonTypefaceRequestBuilder(IonRequestBuilder builder) {
        this.builder = builder;
        ion = builder.ion;
    }

    public IonTypefaceRequestBuilder(Ion ion) {
        this.ion = ion;
    }

    public IonTypefaceRequestBuilder withTextView(TextView imageView) {
        imageViewPostRef = new ContextReference.TextViewContextReference(imageView);
        return this;
    }

    @SuppressLint("Assert")
    @Override
    public SimpleFuture<TextView> intoTextView(TextView imageView) {
        L.d("------------- 1");
        assert Thread.currentThread() == Looper.getMainLooper().getThread();
        L.d("------------- 2");
        if (imageView == null)
            throw new NullPointerException("imageView");

        // no uri? just set a placeholder and bail
        if (builder.uri == null) {
            return FUTURE_TEXTVIEW_NULL_URI;
        }

        L.d("------------- 3");
        withTextView(imageView);

        L.d("------------- 4");
        // executeCache the request, see if we get a bitmap from cache.
        TypefaceFetcher bitmapFetcher = executeCache();
        L.d("------------- 5");
        if (bitmapFetcher.info != null) {
            IonTypeface drawable = setIonDrawable(imageView, bitmapFetcher.info, Loader.LoaderEmitter.LOADED_FROM_MEMORY);
            drawable.cancel();
            SimpleFuture<TextView> imageViewFuture = drawable.getFuture();
            imageViewFuture.reset();
            L.d("---- load from cache.... " + bitmapFetcher.info.typeface);
            imageView.setTypeface(bitmapFetcher.info.typeface);
            imageViewFuture.setComplete(bitmapFetcher.info.exception, imageView);
            return imageViewFuture;
        }
        L.d("------------- 6");
        // TODO TextViewFutureImpl 想办法和 textview 关联起来
        // TextViewFutureImpl imageViewFuture = new TextViewFutureImpl();
        // IonDrawable drawable = setIonDrawable(imageView, null, 0);
        // doAnimation(imageView, loadAnimation, loadAnimationResource);
        // IonDrawable.ImageViewFutureImpl imageViewFuture =
        // drawable.getFuture();
        // imageViewFuture.reset();
        // drawable.register(ion, bitmapFetcher.bitmapKey);
        IonTypeface drawable = setIonDrawable(imageView, null, 0);
        SimpleFuture<TextView> imageViewFuture = drawable.getFuture();
        imageViewFuture.reset();
        drawable.register(ion, bitmapFetcher.bitmapKey);
        // nothing from cache, check to see if there's too many imageview loads
        // already in progress
        L.d("------------- 7");
        if (TypefaceFetcher.shouldDeferTextView(ion)) {
            bitmapFetcher.defer();
        } else {
            L.d("------------- 8");
            bitmapFetcher.execute();
        }
        L.d("------------- 9");

        return imageViewFuture;
    }

    private IonTypeface setIonDrawable(TextView imageView, TypefaceInfo info, int loadedFrom) {
        IonTypeface ret = IonTypeface.getOrCreateIonDrawable(imageView).ion(ion);
        imageView.setTag(IonTypeface.TAG, ret);
        return ret;
    }

    public String computeBitmapKey(String downloadKey) {
        assert downloadKey != null;

        // determine the key for this bitmap after all transformations
        String bitmapKey = downloadKey;

        return bitmapKey;
    }

    private String computeDownloadKey() {
        String downloadKey = builder.uri;
        // although a gif is always same download, the decode (non/animated)
        // result may different
        return FileCache.toKeyString(downloadKey);
    }

    TypefaceFetcher executeCache() {
        final String downloadKey = computeDownloadKey();
        String bitmapKey = computeBitmapKey(downloadKey);

        // TODO: eliminate this allocation?
        TypefaceFetcher ret = new TypefaceFetcher();
        ret.downloadKey = downloadKey;
        ret.bitmapKey = bitmapKey;
        ret.builder = builder;
        // ret.postProcess = postProcess;

        // see if this request can be fulfilled from the cache
        if (!builder.noCache) {
            TypefaceInfo bitmap = builder.ion.typefaceCache.get(bitmapKey);
            L.d("get cache..... " + bitmap);
            if (bitmap != null) {
                ret.info = bitmap;
                return ret;
            }
        }

        return ret;
    }

}
