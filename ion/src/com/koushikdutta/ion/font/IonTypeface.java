
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
    private IonTypefaceCallback callback;
    private Ion ion;

    public IonTypeface ion(Ion ion) {
        this.ion = ion;
        return this;
    }

    public TypefaceInfo getTypefaceInfo() {
        return info;
    }

    public SimpleFuture<TextView> getFuture() {
        return callback.textViewFuture;
    }

    public static final int TAG = R.id.font_key;

    // create an internal static class that can act as a callback.
    // dont let it hold strong references to anything.
    static class IonTypefaceCallback implements FutureCallback<TypefaceInfo> {
        private WeakReference<IonTypeface> ionTypefaceRef;
        private ContextReference.TextViewContextReference textViewRef;
        private String typefaceKey;
        private SimpleFuture<TextView> textViewFuture = new SimpleFuture<TextView>();

        public IonTypefaceCallback(IonTypeface typeface, TextView textView) {
            ionTypefaceRef = new WeakReference<IonTypeface>(typeface);
            textViewRef = new ContextReference.TextViewContextReference(textView);
        }

        @SuppressLint("Assert")
        @Override
        public void onCompleted(Exception e, TypefaceInfo result) {
            assert Thread.currentThread() == Looper.getMainLooper().getThread();
            assert result != null;
            // see if the textView is still alive and cares about this result
            TextView textView = textViewRef.get();
            if (textView == null)
                return;

            IonTypeface typeface = ionTypefaceRef.get();
            if (typeface == null)
                return;

            if (textView.getTag(TAG) != typeface)
                return;

            textView.setTag(TAG, null);
            typeface.setBitmap(result, result.loadedFrom);
            textView.setTag(TAG, typeface);
            textView.setTypeface(result.typeface);

            L.d("textView alive ..."+textViewRef.isAlive());
            if (null != textViewRef.isAlive()) {
                textViewFuture.cancelSilently();
                return;
            }

            textViewFuture.setComplete(e, textView);
        }
    }

    public void cancel() {
        unregister(ion, callback.typefaceKey, callback);
        callback.typefaceKey = null;
    }

    private static void unregister(Ion ion, String key, IonTypefaceCallback callback) {
        if (key == null)
            return;
        // unregister this typeface from the typefaces that are pending.

        // if this typeface was the only thing waiting for this bitmap,
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

    public void register(Ion ion, String typefaceKey) {
        String previousKey = callback.typefaceKey;
        if (TextUtils.equals(previousKey, typefaceKey))
            return;
        callback.typefaceKey = typefaceKey;
        ion.typefacesPending.add(typefaceKey, callback);
        unregister(ion, previousKey, callback);
    }

    public IonTypeface(TextView textView) {
        callback = new IonTypefaceCallback(this, textView);
    }

    public IonTypeface setBitmap(TypefaceInfo info, int loadedFrom) {
        if (this.info == info)
            return this;

        cancel();
        this.loadedFrom = loadedFrom;
        this.info = info;
        if (info == null) {
            callback.typefaceKey = null;
            return this;
        }

        callback.typefaceKey = info.key;
        return this;
    }

    static IonTypeface getOrCreateIonTypeface(TextView textView) {
        IonTypeface current = (IonTypeface) textView.getTag(TAG);
        IonTypeface ret;
        
        if (current == null)
            ret = new IonTypeface(textView);
        else
            ret = (IonTypeface) current;
        
        textView.setTag(TAG, null);
        return ret;
    }
}
