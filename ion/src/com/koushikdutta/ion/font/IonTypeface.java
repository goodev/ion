
package com.koushikdutta.ion.font;

import android.annotation.SuppressLint;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.future.SimpleFuture;
import com.koushikdutta.ion.ContextReference;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.L;
import com.koushikdutta.ion.R;

import java.lang.ref.WeakReference;

public class IonTypeface {

    private TypefaceInfo info;
    @SuppressWarnings("unused")
    private int loadedFrom;
    private IonDrawableCallback callback;
    private Ion ion;

    public IonTypeface ion(Ion ion) {
        this.ion = ion;
        return this;
    }

    public TypefaceInfo getTypefaceInfo() {
        return info;
    }

    public SimpleFuture<TextView> getFuture() {
        return callback.imageViewFuture;
    }

    public static final int TAG = R.id.font_key;

    // create an internal static class that can act as a callback.
    // dont let it hold strong references to anything.
    static class IonDrawableCallback implements FutureCallback<TypefaceInfo> {
        private WeakReference<IonTypeface> ionDrawableRef;
        private ContextReference.TextViewContextReference imageViewRef;
        private String bitmapKey;
        private SimpleFuture<TextView> imageViewFuture = new SimpleFuture<TextView>();

        public IonDrawableCallback(IonTypeface drawable, TextView imageView) {
            ionDrawableRef = new WeakReference<IonTypeface>(drawable);
            imageViewRef = new ContextReference.TextViewContextReference(imageView);
        }

        @SuppressLint("Assert")
        @Override
        public void onCompleted(Exception e, TypefaceInfo result) {
            assert Thread.currentThread() == Looper.getMainLooper().getThread();
            assert result != null;
            // see if the imageview is still alive and cares about this result
            TextView imageView = imageViewRef.get();
            L.d("textView..."+imageView);
            if (imageView == null)
                return;

            IonTypeface drawable = ionDrawableRef.get();
            L.d("IonTypeface..."+drawable);
            if (drawable == null)
                return;

            L.d("IonTypeface tag..."+imageView.getTag(TAG));
            if (imageView.getTag(TAG) != drawable)
                return;

            imageView.setTag(TAG, null);
            drawable.setBitmap(result, result.loadedFrom);
            imageView.setTag(TAG, drawable);
            imageView.setTypeface(result.typeface);

            L.d("textView alive ..."+imageViewRef.isAlive());
            if (null != imageViewRef.isAlive()) {
                imageViewFuture.cancelSilently();
                return;
            }

            imageViewFuture.setComplete(e, imageView);
        }
    }

    public void cancel() {
        unregister(ion, callback.bitmapKey, callback);
        callback.bitmapKey = null;
    }

    private static void unregister(Ion ion, String key, IonDrawableCallback callback) {
        if (key == null)
            return;
        // unregister this drawable from the bitmaps that are
        // pending.

        // if this drawable was the only thing waiting for this bitmap,
        // then the removeItem call will return the TransformBitmap/LoadBitmap
        // instance
        // that was providing the result.
        if (ion.typefacesPending.removeItem(key, callback)) {
            // find out who owns this thing, to see if it is a candidate for
            // removal
            Object owner = ion.typefacesPending.tag(key);
            // only cancel deferred loads... LoadBitmap means a download is
            // already in progress.
            // due to view recycling, cancelling that may be bad, as it may be
            // rerequested again
            // during the recycle process.
            if (owner instanceof DeferredLoadTypeface) {
                DeferredLoadTypeface defer = (DeferredLoadTypeface) owner;
                ion.typefacesPending.remove(defer.key);
            }
        }

        ion.processTypefaceDeferred();
    }

    public void register(Ion ion, String bitmapKey) {
        String previousKey = callback.bitmapKey;
        if (TextUtils.equals(previousKey, bitmapKey))
            return;
        callback.bitmapKey = bitmapKey;
        ion.typefacesPending.add(bitmapKey, callback);
        unregister(ion, previousKey, callback);
    }

    public IonTypeface(TextView imageView) {
        callback = new IonDrawableCallback(this, imageView);
    }

    public IonTypeface setBitmap(TypefaceInfo info, int loadedFrom) {
        if (this.info == info)
            return this;

        cancel();
        this.loadedFrom = loadedFrom;
        this.info = info;
        if (info == null) {
            callback.bitmapKey = null;
            return this;
        }

        callback.bitmapKey = info.key;
        return this;
    }

    static IonTypeface getOrCreateIonDrawable(TextView imageView) {
        IonTypeface current = (IonTypeface) imageView.getTag(TAG);
        IonTypeface ret;
        if (current == null)
            ret = new IonTypeface(imageView);
        else
            ret = (IonTypeface) current;
        // invalidate self doesn't seem to trigger the dimension check to be
        // called by imageview.
        // are drawable dimensions supposed to be immutable?
        imageView.setTag(TAG, null);
        return ret;
    }
}
